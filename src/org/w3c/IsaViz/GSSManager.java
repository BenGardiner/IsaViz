/*   FILE: GSSManager.java
 *   DATE OF CREATION:   Fri Mar 14 09:37:24 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: GSSManager.java,v 1.27 2006/05/11 09:05:12 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 */


package org.w3c.IsaViz;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import javax.swing.ImageIcon;

import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.datatypes.RDFDatatype;

class GSSManager {

    static File lastStyleDir=null;

    static boolean ALLOW_INCREMENTAL_STYLING=false;

    Editor application;

    Hashtable stylesheetFiles;   //key=java.io.File or java.net.URL where the stylesheet comes from; value=corresponding GraphStylesheet

    /*
      rule hashtable informal mapping
      _gssVisibility -> Integer (GraphStylesheet.SHOW || GraphStylesheet.HIDE)

      _gssLayout -> Integer (GraphStylesheet.TABLE_FORM || GraphStylesheet.NODE_EDGE)

      _gssSort -> Integer (GraphStylesheet.SORT_BY_NAME || GraphStylesheet.SORT_BY_NAME_REV || GraphStylesheet.SORT_BY_NAMESPACE || GraphStylesheet.SORT_BY_NAMESPACE_REV)  or CustomOrdering

      _gssFill -> Color
      _gssStroke -> Color
      _gssStrokeWidth -> Float (positive)
      _gssFontFamily -> String
      _gssFontSize -> Integer  (positive)
      _gssFontWeight -> Short  (one of Style.CSS_FONT_WEIGHT*)
      _gssFontStyle -> Short  (one of Style.CSS_FONT_STYLE*)
      _gssShape -> Integer (one of Style.{ELLIPSE,RECTANGLE,CIRCLE,DIAMOND,OCTAGON,TRIANGLEN,TRIANGLES,TRIANGLEE,TRIANGLEW})
                       || CustomShape || CustomPolygon
      _gssTextAlign -> Integer (one of Style.{TA_CENTER,TA_ABOVE,TA_BELOW,TA_LEFT,TA_RIGHT})
    */

    /*temporarily maps actual RDF resources to the final style/visibility/layout/sort that should be applied to them*/
    Hashtable resource2styleTable;
    /*temporarily maps actual RDF properties to the final style/visibility/layout that should be applied to them*/
    Hashtable property2styleTable;
    /*temporarily maps actual RDF literals to the final style/visibility/layout that should be applied to them*/
    Hashtable literal2styleTable;
    /*stylesheets in reverse order of application (last one to be applied is first in the array)*/
    GraphStylesheet[] stylesheets;
    /*image icons (shared by all stylesheets) key=icon URL, value=ImageIcon - acts like a memory cache, for now it is dumb, won't compare to actual Web resource and reload it if changed, but there is a "clear cache" button in the user pref panel*/
    Hashtable url2icon;

    /*returns the rdf:type of a resource, if it exists, null otherwise*/
    public static String getType(Resource r){
	String res=null;
	StmtIterator it=r.listProperties();
	Statement st;
	while (it.hasNext()){
	    st=it.nextStatement();
	    if (st.getPredicate().getURI().equals(GraphStylesheet._rdfType)){
		Object o=st.getObject();
		if (o!=null){res=o.toString();}
		break;
	    }
	}
	it.close();
	return res;
    }

    /*returns the rdf:type of a resource, if it exists, null otherwise*/
    public static String getType(IResource r){
	String res=null;
	Vector outgoingPredicates=r.getOutgoingPredicates();
	if (outgoingPredicates!=null){
	    IProperty p;
	    for (int i=0;i<outgoingPredicates.size();i++){
		p=(IProperty)outgoingPredicates.elementAt(i);
		if (p.getIdent().equals(GraphStylesheet._rdfType)){
		    INode n=p.getObject();
		    if (n instanceof IResource){res=((IResource)n).getIdentity();break;} //break only if everything goes fine
		    else if (n instanceof ILiteral){res=((ILiteral)n).getValue();break;} //otherwise try to find another statement defining an rdf:type
		}//although there is little chance this is going to be the case, try it (for robustness)
	    }
	}
 	return res;
    }

    /*telles whether a resource object can be displayed in a table form or not (it is possible only if it is the value of a single statement and if it is not the subject of any statement <- this cuold actually be supported, but for now we forbid it, we'll see later)*/
    public static boolean objectCanBeDisplayedInTable(IResource object){
	Vector ip=object.getIncomingPredicates();
	Vector op=object.getOutgoingPredicates();
	if (op==null || op.size()==0){
	    if (ip!=null){
		if (ip.size()==1){return true;}//if the object has more than one incoming property, it cannot be displayed in a table form
		else return false;//if it has only one, it is assumed to be the appropriate predicate (we could check, but there is no real point in doing so)
	    }
	    else return false;//should never happen
	}
	else return false;
    }

    GSSManager(Editor app){
	application=app;
	stylesheetFiles=new Hashtable();
	url2icon=new Hashtable();
    }

    void reset(){
	stylesheetFiles.clear();
	url2icon.clear();
    }
    
    /*load graph stylesheet (do not apply it)*/
    public void loadStylesheet(final File f,final int whichReader){
	loadStylesheet(f, whichReader, -1);
    }

    /*load graph stylesheet (do not apply it)*/
    public void loadStylesheet(final File f, final int whichReader, final int index){
	lastStyleDir=f.getParentFile();
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    application.tblp.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
		    Editor.tblp.setSTPBValue(0);
		    GraphStylesheet gss=new GraphStylesheet();
		    gss.load(f,application.isvMngr.application,whichReader);
		    if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
		    stylesheetFiles.put(f,gss);
		    if (index >= 0){
			application.tblp.insertStylesheet(f, index);
		    }
		    else {
			application.tblp.addStylesheet(f);
		    }
		    Editor.tblp.setSTPBValue(100);
		    application.tblp.setCursor(java.awt.Cursor.getDefaultCursor());
		    return null; 
		}
	    };
	worker.start();
    }

    /*load graph stylesheet (do not apply it)*/
    public void loadStylesheet(final java.net.URL url,final int whichReader){
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    application.tblp.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
		    Editor.tblp.setSTPBValue(0);
		    GraphStylesheet gss=new GraphStylesheet();
		    gss.load(url,application.isvMngr.application,whichReader);
		    if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
		    stylesheetFiles.put(url,gss);
		    application.tblp.addStylesheet(url);
		    Editor.tblp.setSTPBValue(100);
		    application.tblp.setCursor(java.awt.Cursor.getDefaultCursor());
		    return null; 
		}
	    };
	worker.start();
    }

    void removeSelectedStylesheet(){
	Object o=application.tblp.removeSelectedStylesheet();
	if (o!=null){
	    stylesheetFiles.remove(o);
	}
    }

    void editSelectedStylesheet(Object stylesheetLocation){
	//we have to pass a reference to the stylesheet selected in the table (File/URL)
	if (stylesheetLocation!=null){
	    if (stylesheetLocation instanceof File){
		(new GSSEditor(application)).loadStylesheet((File)stylesheetLocation,false);
	    }
	    else if (stylesheetLocation instanceof java.net.URL){
		(new GSSEditor(application)).loadStylesheet((java.net.URL)stylesheetLocation,false);
	    }
	}
    }

    /*when a stylesheet is saved through GSSEditor (called through IsaViz), reload it in IsaViz automatically*/
    void refreshStylesheet(File f){
	for (int i=0;i<application.tblp.gssTableModel.getRowCount();i++){
	    if (application.tblp.gssTableModel.getValueAt(i, 0) == f){
		// remove old version of stylesheet
		application.tblp.removeStylesheet(i);
		// load new version, put it at right index
		loadStylesheet(f, RDFLoader.RDF_XML_READER, i);
		break;
	    }
	}
    }

    /*returns the list of stylesheets (GraphStylesheet objects) in their order of application (empty vector if none)*/
    Vector getStylesheetList(){
	Vector v=application.tblp.getStylesheetList();
	Vector res=new Vector();
	for (int i=0;i<v.size();i++){
	    res.addElement(stylesheetFiles.get(v.elementAt(i)));
	}
	return res;
    }

    void applyStylesheets(){//to current model
	Vector list=getStylesheetList();
	if (list.size()>0){//this will be checked again later by RDFLoader.loadAndStyle, but we do it here to prevent 
	    final SwingWorker worker=new SwingWorker(){//exporting and then importing the current model if there is no point (i.e. no stylesheet to apply)
		    public Object construct(){
			Editor.vsm.getView(Editor.mainView).setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
			//generate Jena model for current model
			application.generateJenaModel();
			//serialize this model in a temporary file (RDF/XML)
			File tmpF=Utils.createTempFile(Editor.m_TmpDir.toString(),"mrg",".rdf");
			boolean wasAbbrevSyntax=Editor.ABBREV_SYNTAX; //serialize using
			Editor.ABBREV_SYNTAX=true;                    //abbreviated syntax
			application.rdfLdr.save(application.rdfModel,tmpF);                   //but restore user prefs after
			if (!wasAbbrevSyntax){Editor.ABBREV_SYNTAX=false;}//we are done
			//import this file
			application.reset(false);   //do not reset NS bindings
			//tmp file is generated as RDF/XML
			application.rdfLdr.loadAndStyle(tmpF,RDFLoader.RDF_XML_READER);
			if (Editor.dltOnExit && tmpF!=null){tmpF.deleteOnExit();}
			application.updatePrefixBindingsInGraph();
			Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
			if (GraphStylesheet.DEBUG_GSS){TextViewer tv=new TextViewer(GraphStylesheet.debugBuffer2,"GSS Debugger - Rule Evaluation",0,true);}
			return null; 
		    }
		};
	    worker.start();
	}
    }

    protected void initStyleTables(){
	Vector list=getStylesheetList();
	stylesheets=new GraphStylesheet[list.size()];
	for (int i=list.size()-1;i>=0;i--){
	    stylesheets[list.size()-1-i]=(GraphStylesheet)list.elementAt(i);
	}
	resource2styleTable=new Hashtable();
	property2styleTable=new Hashtable();
	literal2styleTable=new Hashtable();
    }

    protected void cleanStyleTables(){
	resource2styleTable.clear();
	property2styleTable.clear();
	literal2styleTable.clear();
	resource2styleTable=null;
	property2styleTable=null;
	literal2styleTable=null;
    }

    StyleInfoR getStyle(IResource r){
	return (StyleInfoR)resource2styleTable.get(r);
    }

    StyleInfoR computeAndGetStyle(IResource r){
	StyleInfoR res;
	if (resource2styleTable.containsKey(r)){
	    res=(StyleInfoR)resource2styleTable.get(r);
	}
	else {
	    res=new StyleInfoR();
	    resource2styleTable.put(r,res);
	    Vector matchingRules;
	    Object selector;
	    Vector styleIDs;
	    Object sortRule;
	    for (int i=0;i<stylesheets.length;i++){//for each stylesheet beginning with the one to be applied last
		matchingRules=stylesheets[i].evaluateRules(r);
		for (int j=0;j<matchingRules.size();j++){//for each selector matching this resource, sorted by weight (begin with strongest weight)
		    selector=matchingRules.elementAt(j);
		    if (stylesheets[i].rStyleRules.containsKey(selector)){
			styleIDs=(Vector)stylesheets[i].rStyleRules.get(selector);
			for (int k=0;k<styleIDs.size();k++){
			    res.applyStyle((Style)stylesheets[i].styles.get(styleIDs.elementAt(k)));
			}
		    }
		    res.applyLayout((Integer)stylesheets[i].rLayoutRules.get(selector));
		    res.applyVisibility((Integer)stylesheets[i].rVisRules.get(selector));
		    if ((sortRule=stylesheets[i].sortRules.get(selector))!=null){
			if (sortRule instanceof String){//ordering defined by an enumeration (sortRule is the sortID)
			    res.setPropertyOrdering(stylesheets[i].orderings.get(sortRule));//retrieve this enumeration
			}//from orderings
			else if (sortRule instanceof Integer){//ordering is a predefined one: name, namespace, etc.
			    res.setPropertyOrdering(sortRule);
			}
			else if (GraphStylesheet.DEBUG_GSS){System.err.println("Error: GSSManager.computeAndGetStyle() failed to build an ordering of properties for resource "+r.toString()+" because the specification of this ordering is incorrect: "+sortRule);}
		    }
		    /*just not to loose time evaluating rules that won't have any effect
		      test that shape is specified because it can influence the layout even though it is not visible, 
		      and we do not want to stop processing potential rules in case one changes the shape
		    */
		    if (res.isFullySpecified() || res.isDisplayNone() || res.isVisibilityHiddenAndShapeSpecified()){return res;}
		}
	    }
	}
	return res;
    }

    StyleInfoP getStyle(IProperty p){
	return (StyleInfoP)property2styleTable.get(p);
    }

    StyleInfoP computeAndGetStyle(IProperty p){
	StyleInfoP res;
	if (property2styleTable.containsKey(p)){
	    res=(StyleInfoP)property2styleTable.get(p);
	}
	else {
	    res=new StyleInfoP();
	    property2styleTable.put(p,res);
	    Vector matchingRules;
	    Object selector;
	    Vector styleIDs;
	    for (int i=0;i<stylesheets.length;i++){//for each stylesheet beginning with the one to be applied last
		matchingRules=stylesheets[i].evaluateRules(p);
		for (int j=0;j<matchingRules.size();j++){//for each selector matching this property, sorted by weight (begin with strongest weight)
		    selector=matchingRules.elementAt(j);
		    if (stylesheets[i].pStyleRules.containsKey(selector)){
			styleIDs=(Vector)stylesheets[i].pStyleRules.get(selector);
			for (int k=0;k<styleIDs.size();k++){
			    res.applyStyle((Style)stylesheets[i].styles.get(styleIDs.elementAt(k)));
			}
		    }
		    res.applyLayout((Integer)stylesheets[i].pLayoutRules.get(selector));
		    res.applyVisibility((Integer)stylesheets[i].pVisRules.get(selector));
		    /*just not to loose time evaluating rules that won't have any effect
		      test that shape is specified because it can influence the layout even though it is not visible, 
		      and we do not want to stop processing potential rules in case one changes the shape
		    */
		    if (res.isFullySpecified() || res.isDisplayNone() || res.isVisibilityHidden()){return res;}
		}
	    }
	}
	return res;
    }

    StyleInfoL getStyle(ILiteral l){
	return (StyleInfoL)literal2styleTable.get(l);
    }

    StyleInfoL computeAndGetStyle(ILiteral l){
	StyleInfoL res;
	if (literal2styleTable.containsKey(l)){
	    res=(StyleInfoL)literal2styleTable.get(l);
	}
	else {
	    res=new StyleInfoL();
	    literal2styleTable.put(l,res);
	    Vector matchingRules;
	    Object selector;
	    Vector styleIDs;
	    for (int i=0;i<stylesheets.length;i++){//for each stylesheet beginning with the one to be applied last
		matchingRules=stylesheets[i].evaluateRules(l);
		for (int j=0;j<matchingRules.size();j++){//for each selector matching this literal, sorted by weight (begin with strongest weight)
		    selector=matchingRules.elementAt(j);
		    if (stylesheets[i].lStyleRules.containsKey(selector)){
			styleIDs=(Vector)stylesheets[i].lStyleRules.get(selector);
			for (int k=0;k<styleIDs.size();k++){
			    res.applyStyle((Style)stylesheets[i].styles.get(styleIDs.elementAt(k)));
			}
		    }
		    res.applyLayout((Integer)stylesheets[i].lLayoutRules.get(selector));
		    res.applyVisibility((Integer)stylesheets[i].lVisRules.get(selector));
		    /*just not to loose time evaluating rules that won't have any effect
		      test that shape is specified because it can influence the layout even though it is not visible, 
		      and we do not want to stop processing potential rules in case one changes the shape
		    */
		    if (res.isFullySpecified() || res.isDisplayNone() || res.isVisibilityHiddenAndShapeSpecified()){return res;}
		}
	    }
	}
	return res;
    }

    /*method for incrementally applying styling to a specific resource (because its context (properties) or URI has changed*/
    void incStyling(IResource r){
	if (r!=null && ALLOW_INCREMENTAL_STYLING){
	    initStyleTables();
	    assignStyle(r);
	    //propagate the changes (might affect other nodea and edges, as it might be part of a selector involving subjectOfStatement, objectOfStatement)
	    if (r.getIncomingPredicates()!=null){
		IProperty p;
		for (Enumeration e=r.getIncomingPredicates().elements();e.hasMoreElements();){
		    p=(IProperty)e.nextElement();
		    assignStyle(p);
		    if (p.getSubject()!=null){
			assignStyle(p.getSubject());
			if (p.getSubject().getIncomingPredicates()!=null){
			    IProperty p2;
			    for (Enumeration e2=p.getSubject().getIncomingPredicates().elements();e2.hasMoreElements();){
				p2=(IProperty)e2.nextElement();
				assignStyle(p2);
				if (p2.getSubject()!=null){assignStyle(p2.getSubject());}
				//no need to assignStyle(p2.getObject()) as they have been processed by assignStyle(p.getSubject())
			    }
			}
		    }
		    if (p.getObject()!=null && p.getObject()!=r){//don't want to apply style to r twice
			if (p.getObject() instanceof IResource){assignStyle((IResource)p.getObject());}
			else {assignStyle((ILiteral)p.getObject());}
		    }
		}
	    }
	    cleanStyleTables();
	}
    }

    /*method for incrementally applying styling to a specific resource (because its context (property) or value has changed*/
    void incStyling(ILiteral l){
	if (l!=null && ALLOW_INCREMENTAL_STYLING){
	    initStyleTables();
	    assignStyle(l);
	    if (l.getIncomingPredicate()!=null){
		assignStyle(l.getIncomingPredicate());
		if (l.getIncomingPredicate().getSubject()!=null){assignStyle(l.getIncomingPredicate().getSubject());}
	    }
	    cleanStyleTables();
	}
    }

    /*method for incrementally applying styling to a specific property (because its context (subject, object) or URI has changed*/
    void incStyling(IProperty p){
	if (p!=null && ALLOW_INCREMENTAL_STYLING){
	    initStyleTables();
	    assignStyle(p);
	    if (p.getObject()!=null){
		if (p.getObject() instanceof IResource){assignStyle((IResource)p.getObject());}
		else {assignStyle((ILiteral)p.getObject());}
	    }
	    if (p.getSubject()!=null){
		assignStyle(p.getSubject());
		if (p.getSubject().getOutgoingPredicates()!=null){
		    IProperty p2;
		    for (Enumeration e=p.getSubject().getOutgoingPredicates().elements();e.hasMoreElements();){
			p2=(IProperty)e.nextElement();
			if (p2!=p){
			    assignStyle(p2);
			    if (p2.getObject()!=null){
				if (p2.getObject() instanceof IResource){assignStyle((IResource)p2.getObject());}
				else {assignStyle((ILiteral)p2.getObject());}
			    }
			}
		    }
		}
		if (p.getSubject().getIncomingPredicates()!=null){
		    IProperty p2;
		    for (Enumeration e=p.getSubject().getIncomingPredicates().elements();e.hasMoreElements();){
			p2=(IProperty)e.nextElement();
			assignStyle(p2);
			if (p2.getSubject()!=null){assignStyle(p2.getSubject());}
		    }
		}
	    }
	    cleanStyleTables();
	}
    }

    void assignStyle(IResource r){
	StyleInfoR sir=computeAndGetStyle(r);
	if (r.isVisuallyRepresented()){
	    int fillind;
	    int strokeind;
	    Color fill=sir.getFillColor();
	    Color stroke=sir.getStrokeColor();
	    float width=sir.getStrokeWidth().floatValue();
	    float[] dashArray=sir.getStrokeDashArray();
	    String ffamily=sir.getFontFamily();
	    int fsize=sir.getFontSize().intValue();
	    short fweight=sir.getFontWeight().shortValue();
	    short fstyle=sir.getFontStyle().shortValue();
	    Integer textal=sir.getTextAlignment();
	    Glyph g1=r.getGlyph();
	    VText g2=r.getGlyphText();
	    if (!r.isLaidOutInTableForm()){//don't want to change shape if resource laid out in table as it is necessarily a rectangle
		Glyph newShape=application.geomMngr.getNodeShape(r,sir);
		if (newShape!=null){
		    Editor.vsm.addGlyph(newShape,Editor.mainVirtualSpace);
		    r.setGlyph(newShape);
		    Editor.mSpace.destroyGlyph(g1);
		    if (r.getGlyphText()!=null){Editor.mSpace.above(r.getGlyphText(),newShape);}
		}
	    }
	    if (fill!=null){
		fillind=ConfigManager.addColor(fill);
		r.setFillColor(fillind);
	    }
	    else {
		fillind=ConfigManager.defaultRFIndex;
		r.setFillColor(fillind);
	    }
	    if (stroke!=null){
		strokeind=ConfigManager.addColor(stroke);
		r.setStrokeColor(strokeind);
	    }
	    else {
		strokeind=ConfigManager.defaultRTBIndex;
		r.setStrokeColor(strokeind);
	    }
	    //now that the node's main color(s) has(ve) been updated, we should put it back to its apparent color, if it is selected or commented
	    if (r.isSelected()){
		r.getGlyph().setHSVColor(ConfigManager.selFh,ConfigManager.selFs,ConfigManager.selFv);
		r.getGlyph().setHSVbColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
		if (r.getGlyphText()!=null){r.getGlyphText().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
	    }
	    else if (r.isCommented()){
		r.getGlyph().setHSVColor(ConfigManager.comFh,ConfigManager.comFs,ConfigManager.comFv);
		r.getGlyph().setHSVbColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
		if (r.getGlyphText()!=null){r.getGlyphText().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
	    }
	    ConfigManager.assignStrokeToGlyph(r.getGlyph(),width,dashArray);
	    Font font;
	    if ((font=ConfigManager.rememberFont(ConfigManager.fonts,ffamily,fsize,fweight,fstyle))!=null && g2!=null){
		g2.setSpecialFont(font);
	    }
	    if ((!textal.equals(Style.TA_CENTER)) && g2!=null){//if label is not centered and label actually exists, align it
// 		application.geomMngr.alignText(g1,g2,textal);
		r.setTextAlign(textal.intValue());
	    }
	    application.geomMngr.correctResourceTextAndShape(r);
	}
    }

    void assignStyle(ILiteral l){
    	StyleInfoL sil=computeAndGetStyle(l);
	if (l.isVisuallyRepresented()){
	    int fillind;
	    int strokeind;
	    Color fill=sil.getFillColor();
	    Color stroke=sil.getStrokeColor();
	    float[] dashArray=sil.getStrokeDashArray();
	    float width=sil.getStrokeWidth().floatValue();
	    String ffamily=sil.getFontFamily();
	    int fsize=sil.getFontSize().intValue();
	    short fweight=sil.getFontWeight().shortValue();
	    short fstyle=sil.getFontStyle().shortValue();
	    Integer textal=sil.getTextAlignment();
	    Glyph g1=l.getGlyph();
	    VText g2=l.getGlyphText();
	    if (!l.isLaidOutInTableForm()){//don't want to change shape if literal laid out in table as it is necessarily a rectangle
		Glyph newShape=application.geomMngr.getNodeShape(l,sil);
		if (newShape!=null){
		    Editor.vsm.addGlyph(newShape,Editor.mainVirtualSpace);
		    l.setGlyph(newShape);
		    Editor.mSpace.destroyGlyph(g1);
		    if (l.getGlyphText()!=null){Editor.mSpace.above(l.getGlyphText(),newShape);}
		}
	    }
	    if (fill!=null){
		fillind=ConfigManager.addColor(fill);
		l.setFillColor(fillind);
	    }
	    else {
		fillind=ConfigManager.defaultLFIndex;
		l.setFillColor(fillind);
	    }
	    if (stroke!=null){
		strokeind=ConfigManager.addColor(stroke);
		l.setStrokeColor(strokeind);
	    }
	    else {
		strokeind=ConfigManager.defaultLTBIndex;
		l.setStrokeColor(strokeind);
	    }
	    //now that the node's main color(s) has(ve) been updated, we should put it back to its apparent color, if it is selected or commented
	    if (l.isSelected()){
		l.getGlyph().setHSVColor(ConfigManager.selFh,ConfigManager.selFs,ConfigManager.selFv);
		l.getGlyph().setHSVbColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
		if (l.getGlyphText()!=null){l.getGlyphText().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
	    }
	    else if (l.isCommented()){
		l.getGlyph().setHSVColor(ConfigManager.comFh,ConfigManager.comFs,ConfigManager.comFv);
		l.getGlyph().setHSVbColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
		if (l.getGlyphText()!=null){l.getGlyphText().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
	    }
	    ConfigManager.assignStrokeToGlyph(l.getGlyph(),width,dashArray);
	    Font font;
	    if ((font=ConfigManager.rememberFont(ConfigManager.fonts,ffamily,fsize,fweight,fstyle))!=null && g2!=null){
		g2.setSpecialFont(font);
	    }
	    if ((!textal.equals(Style.TA_CENTER)) && g2!=null){//if label is not centered and label actually exists, align it
		application.geomMngr.alignText(g1,g2,textal);
		l.setTextAlign(textal.intValue());
	    }
	}
    }

    void assignStyle(IProperty p){
	StyleInfoP sip=computeAndGetStyle(p);
	if (p.isVisuallyRepresented()){
	    int fillind;
	    int strokeind;
	    Color fill=sip.getFillColor();
	    Color stroke=sip.getStrokeColor();
	    float width=sip.getStrokeWidth().floatValue();
	    float[] dashArray=sip.getStrokeDashArray();
	    String ffamily=sip.getFontFamily();
	    int fsize=sip.getFontSize().intValue();
	    short fweight=sip.getFontWeight().shortValue();
	    short fstyle=sip.getFontStyle().shortValue();
	    Glyph g1=p.getGlyph();
	    VText g2=p.getGlyphText();
	    Glyph g3=p.getGlyphHead();
	    if (p.isLaidOutInTableForm()){//table form layout means that we have to take fill into account if it exists
		if (fill!=null){//in this style, for the cell background ; cannot use sip.getLayout() here as the table
		    fillind=ConfigManager.addColor(fill);//form layout could be originating from the object
		    p.setCellFillColor(fillind);
		    if (stroke!=null){
			strokeind=ConfigManager.addColor(stroke);
			p.setStrokeColor(strokeind);
			p.setTextColor(strokeind);
		    }
		    else {
			    p.setStrokeColor(ConfigManager.defaultPBIndex);
			    if (p.getObject() instanceof ILiteral){
				p.setTextColor(ConfigManager.defaultLTBIndex);
			    }
			    else {//instanceof IResource
				p.setTextColor(ConfigManager.defaultRTBIndex);
			    }
		    }
		}
		else {//if there is no fill information, the cell should be colored following the same scheme as the associated object
		    if (p.getObject() instanceof ILiteral){
			fillind=ConfigManager.defaultLFIndex;
			p.setCellFillColor(fillind);
			if (stroke!=null){
			    strokeind=ConfigManager.addColor(stroke);
			    p.setStrokeColor(strokeind);
			    p.setTextColor(strokeind);
			}
			else {
			    p.setStrokeColor(ConfigManager.defaultPBIndex);
			    //p.setCellStrokeColor(ConfigManager.defaultLTBIndex);
			    p.setTextColor(ConfigManager.defaultLTBIndex);
			}
		    }
		    else {//instanceof IResource
			fillind=ConfigManager.defaultRFIndex;
			p.setCellFillColor(fillind);
			if (stroke!=null){
			    strokeind=ConfigManager.addColor(stroke);
			    p.setStrokeColor(strokeind);
			    p.setTextColor(strokeind);
			}
			else {
			    p.setStrokeColor(ConfigManager.defaultPBIndex);
			    //p.setCellStrokeColor(ConfigManager.defaultRTBIndex);
			    p.setTextColor(ConfigManager.defaultRTBIndex);
			}
		    }
		}
		//now that the edge's main color(s) has(ve) been updated, we should put it back to its apparent color, if it is selected or commented
		if (p.isSelected()){
		    p.getGlyph().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
		    if (p.getGlyphHead()!=null){p.getGlyphHead().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
		    if (p.getGlyphText()!=null){p.getGlyphText().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
		    if (p.getTableCellGlyph()!=null){
			p.getTableCellGlyph().setHSVColor(ConfigManager.selFh,ConfigManager.selFs,ConfigManager.selFv);
			p.getTableCellGlyph().setHSVbColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
		    }
}
		else if (p.isCommented()){
		    p.getGlyph().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
		    if (p.getGlyphHead()!=null){p.getGlyphHead().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
		    if (p.getGlyphText()!=null){p.getGlyphText().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
		    if (p.getTableCellGlyph()!=null){
			p.getTableCellGlyph().setHSVColor(ConfigManager.comFh,ConfigManager.comFs,ConfigManager.comFv);
			p.getTableCellGlyph().setHSVbColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
		    }
		}
	    }
	    else {//standard node-edge layout, color everything in blue if stroke unspecified (or according to it if specified)
		if (stroke!=null){
		    strokeind=ConfigManager.addColor(stroke);
		    p.setStrokeColor(strokeind);
		    p.setTextColor(strokeind);
		}
		else {
		    p.setStrokeColor(ConfigManager.defaultPBIndex);
		    p.setTextColor(ConfigManager.defaultPTIndex);
		}
		//now that the edge's main color(s) has(ve) been updated, we should put it back to its apparent color, if it is selected or commented
		if (p.isSelected()){
		    p.getGlyph().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
		    if (p.getGlyphHead()!=null){p.getGlyphHead().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
		    if (p.getGlyphText()!=null){p.getGlyphText().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
		}
		else if (p.isCommented()){
		    p.getGlyph().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
		    if (p.getGlyphHead()!=null){p.getGlyphHead().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
		    if (p.getGlyphText()!=null){p.getGlyphText().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
		}
	    }
	    ConfigManager.assignStrokeToGlyph(p.getGlyph(),width,dashArray);
	    Font font;
	    if ((font=ConfigManager.rememberFont(ConfigManager.fonts,ffamily,fsize,fweight,fstyle))!=null && g2!=null){
		g2.setSpecialFont(font);
	    }
	}
    }

    /*retrieve icon at iconURL and store it in memory*/
    boolean storeIcon(java.net.URL iconURL){
	if (!url2icon.containsKey(iconURL)){
	    ImageIcon ii=new ImageIcon(iconURL);
	    if (ii!=null && ii.getIconWidth()>0 && ii.getIconHeight()>0){
		url2icon.put(iconURL,ii);
		return true;
	    }
	    else return false;  //return false if retrieving the icon failed or the content is not an icon
	}
	else {return true;}//the ImageIcon was already stored only if it could be retrieved and the resource did contain an icon
    }

    /*get the in-memory ImageIcon of icon at iconURL*/
    ImageIcon getIcon(java.net.URL iconURL){
 	if (url2icon.containsKey(iconURL)){
	    return (ImageIcon)url2icon.get(iconURL);
 	}
 	else {
 	    if (storeIcon(iconURL)){return (ImageIcon)url2icon.get(iconURL);}
 	    else {return null;}
 	}
    }

//     void clearBitmapCache(){
// 	url2icon.clear();
//     }

}
