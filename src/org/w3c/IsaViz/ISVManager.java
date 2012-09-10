/*   FILE: ISVManager.java
 *   DATE OF CREATION:   12/24/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: ISVManager.java,v 1.16 2006/05/12 09:01:55 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: ISVManager.java,v 1.16 2006/05/12 09:01:55 epietrig Exp $
 */ 

package org.w3c.IsaViz;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import javax.swing.ImageIcon;
import java.net.MalformedURLException;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.*;
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.engine.Location;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.*;

/*methods related to ISV file format*/

class ISVManager {

    Editor application;

    /*Temporary data structures used to modelize the graph structure when parsing/serializing ISV project files*/
    Hashtable uniqueIDs2INodes;  //only used when loading a project file to 
                                 //modelize the graph structure in the XML file
    Hashtable inodes2UniqueIDs;  //only used when saving a project file to 
    StringBuffer nextUniqueID;   //modelize the graph structure in the XML file

    /*subdirectory where bitmap images should be stored (set when exporting 1st bitmap, reset to null at the end of the process)*/
    protected static File img_subdir=null;
    
    /*temporarily holds fonts declared in an ISV project file (set when opening project, reset to null at the end of the process)*/
    protected Vector fonts=null;

    /*temporarily holds strokes declared in an ISV project file (set when opening project, reset to null at the end of the process)*/
    protected Hashtable strokes=null;

    protected Hashtable subject2sharededge=null;
    
    ISVManager(Editor e){
	this.application=e;
    }

    /*open an ISV project file*/
    public void openProject(File f){
	ProgPanel pp=new ProgPanel("Resetting...","Loading ISV");
	application.reset(true);
	application.resetGraphStylesheets();
	Editor.lastOpenPrjDir=f.getParentFile();
	pp.setPBValue(10);
	pp.setLabel("Loading file "+f.toString()+" ...");
	Editor.projectFile=f;
	Editor.vsm.getView(Editor.mainView).setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	Editor.vsm.getView(Editor.mainView).setStatusBarText("Loading project to "+f.toString()+" ...");
	uniqueIDs2INodes=new Hashtable();
	pp.setPBValue(20);
	pp.setLabel("Parsing...");
 	try {
	    Document d=application.xmlMngr.parse(f,false);
	    d.normalize();
	    Element rt=d.getDocumentElement();
	    NodeList nl;
	    //base URI
	    if (rt.getElementsByTagNameNS(Editor.isavizURI,"baseURI").getLength()>0){
		Editor.BASE_URI=((Element)rt.getElementsByTagNameNS(Editor.isavizURI,"baseURI").item(0)).getAttribute("value");
	    }
	    //namespace bindings
	    pp.setPBValue(30);
	    pp.setLabel("Processing namespace bindings...");
	    if (rt.getElementsByTagNameNS(Editor.isavizURI,"nsBindings").getLength()>0){
		nl=((Element)(rt.getElementsByTagNameNS(Editor.isavizURI,"nsBindings")).item(0)).getElementsByTagNameNS(Editor.isavizURI,"nsBinding");
		Element tmpEl;
		for (int i=0;i<nl.getLength();i++){
		    tmpEl=(Element)nl.item(i);
		    application.addNamespaceBinding(tmpEl.getAttribute("prefix"),tmpEl.getAttribute("uri"),new Boolean(tmpEl.getAttribute("dispPrefix")),true,true);
		}
		application.schemaMngr.updateNamespaces();
	    }	    
	    //property types
	    pp.setPBValue(40);
	    pp.setLabel("Processing property types...");
	    if (rt.getElementsByTagNameNS(Editor.isavizURI,"propertyTypes").getLength()>0){
		nl=((Element)(rt.getElementsByTagNameNS(Editor.isavizURI,"propertyTypes")).item(0)).getElementsByTagNameNS(Editor.isavizURI,"propertyType");
		Element tmpEl;
		for (int i=0;i<nl.getLength();i++){
		    tmpEl=(Element)nl.item(i);
		    application.addPropertyType(tmpEl.getAttribute("ns"),tmpEl.getAttribute("name"),true);
		}
	    }
	    //colors
	    pp.setPBValue(45);
	    pp.setLabel("Processing color and font tables...");
	    if (rt.getElementsByTagNameNS(Editor.isavizURI,"colorTable").getLength()>0){
		nl=((Element)(rt.getElementsByTagNameNS(Editor.isavizURI,"colorTable")).item(0)).getElementsByTagNameNS(Editor.isavizURI,"color");
		Element tmpEl;
		/*if we arrived here, this means that the ISV file does contain a color index, which should declare the default 
		  colors, so we can erase the initial index safely, and replace it with the values declared in the project file*/
		ConfigManager.colors=new Color[nl.getLength()];
		String r=null,g=null,b=null;
		for (int i=0;i<nl.getLength();i++){
		    tmpEl=(Element)nl.item(i);
		    try {
			r=tmpEl.getAttribute("r");
			g=tmpEl.getAttribute("g");
			b=tmpEl.getAttribute("b");
			ConfigManager.colors[i]=new Color(Integer.parseInt(r),Integer.parseInt(g),Integer.parseInt(b));
		    }
		    catch (Exception ex){//for robustness
			System.err.println("ISVManager:Error: failed to instantiate color ("+r+","+g+","+b+")");
			ConfigManager.colors[i]=Color.white;
		    }
		}
	    }
	    //fonts
	    fonts=new Vector();
	    if (rt.getElementsByTagNameNS(Editor.isavizURI,"fontTable").getLength()>0){
		nl=((Element)(rt.getElementsByTagNameNS(Editor.isavizURI,"fontTable")).item(0)).getElementsByTagNameNS(Editor.isavizURI,"font");
		Element tmpEl;
		/*if we arrived here, this means that the ISV file does contain a font index, which should declare the default 
		  font in 1st position*/
		/*as for colors, the ordering of fonts in the ISV/XML document matters, as it corresponds to the indices refered to by all graph entities, so the vector of fonts should be built according to the ordering in the ISV/XML file*/
		for (int i=0;i<nl.getLength();i++){
		    tmpEl=(Element)nl.item(i);
		    fonts.add(Font.decode(tmpEl.getAttribute("desc")));
		}
	    }
	    pp.setPBValue(48);
	    pp.setLabel("Processing bookmarks...");
	    if (rt.getElementsByTagNameNS(Editor.isavizURI, "bookmarks").getLength()>0){
		nl=((Element)(rt.getElementsByTagNameNS(Editor.isavizURI, "bookmarks")).item(0)).getElementsByTagNameNS(Editor.isavizURI,"bookmark");
		String bkTitle;
		long lx,ly;
		float la;
		Element tmpEl;
		for (int i=0;i<nl.getLength();i++){
		    bkTitle = "";
		    try {
			tmpEl = (Element)nl.item(i);
			lx = Long.parseLong(tmpEl.getAttribute("x"));
			ly = Long.parseLong(tmpEl.getAttribute("y"));
			la = Float.parseFloat(tmpEl.getAttribute("z"));
			bkTitle = tmpEl.getFirstChild().getNodeValue();
			application.bkp.addBookmark(bkTitle, new Location(lx, ly, la));
		    }
		    catch (Exception ex){System.err.println("Error: could not parse bookmark '"+bkTitle+"'");}
		}
	    }
	    //strokes
	    strokes=new Hashtable();
	    //resources
	    pp.setPBValue(50);
	    pp.setLabel("Processing resources...");
	    if (rt.getElementsByTagNameNS(Editor.isavizURI,"resources").getLength()>0){
		nl=((Element)(rt.getElementsByTagNameNS(Editor.isavizURI,"resources")).item(0)).getElementsByTagNameNS(Editor.isavizURI,"iresource");
		for (int i=0;i<nl.getLength();i++){
		    createIResourceFromISV((Element)nl.item(i));
		}
	    }
	    //literals
	    pp.setPBValue(60);
	    pp.setLabel("Processing literals...");
	    if (rt.getElementsByTagNameNS(Editor.isavizURI,"literals").getLength()>0){
		nl=((Element)(rt.getElementsByTagNameNS(Editor.isavizURI,"literals")).item(0)).getElementsByTagNameNS(Editor.isavizURI,"iliteral");
		for (int i=0;i<nl.getLength();i++){
		    createILiteralFromISV((Element)nl.item(i));
		}
	    }
	    //properties
	    pp.setPBValue(80);
	    pp.setLabel("Processing properties...");
	    subject2sharededge=new Hashtable();
	    if (rt.getElementsByTagNameNS(Editor.isavizURI,"properties").getLength()>0){
		nl=((Element)(rt.getElementsByTagNameNS(Editor.isavizURI,"properties")).item(0)).getElementsByTagNameNS(Editor.isavizURI,"iproperty");
		for (int i=0;i<nl.getLength();i++){//statements are added to the model at this time, since we have a bijection between statements and iproperty(s) and since everything else has been created (meaning also that iproperty should always be created after literals and resources)
		    createIPropertyFromISV((Element)nl.item(i));
		}
	    }
	    subject2sharededge.clear();
	    pp.setLabel("Building graphical representation...");
	    pp.setPBValue(100);
	    //application.cfgMngr.assignColorsToGraph();
	    application.showAnonIds(ConfigManager.SHOW_ANON_ID);  //show/hide IDs of anonymous resources
	    application.showResourceLabels(Editor.DISP_AS_LABEL);
	    uniqueIDs2INodes.clear();
	    uniqueIDs2INodes=null;
	    fonts.removeAllElements();
	    strokes.clear();
	    Editor.vsm.getGlobalView(Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0),ConfigManager.ANIM_DURATION);
	    application.centerRadarView();
	    Editor.vsm.getView(Editor.mainView).setStatusBarText("Loading project to "+f.toString()+" ...done");
	    
	}
 	catch (Exception ex){application.errorMessages.append("An error occured while loading file "+f+"\nThis might not be a valid ISV project file.\n"+ex);application.reportError=true;ex.printStackTrace();}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
	Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
	pp.destroy();
    }

    /*save an ISV project file*/
    public void saveProject(File f){
	Editor.projectFile=f;
	Editor.lastSavePrjDir=f.getParentFile();
	Editor.vsm.getView(Editor.mainView).setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	Editor.vsm.getView(Editor.mainView).setStatusBarText("Saving project to "+f.toString()+" ...");
	DOMImplementation di=new DOMImplementationImpl();
	//DocumentType dtd=di.createDocumentType("isv:project",null,"isv.dtd");
	Document prj=di.createDocument(Editor.isavizURI,"isv:project",null);
	//generate the XML document
	Element rt=prj.getDocumentElement();
	rt.setAttribute("xmlns:isv",Editor.isavizURI);
	rt.setAttribute("xmlns:xlink",com.xerox.VTM.svg.SVGWriter.xlinkURI);
	//base URI
	Element buri=prj.createElementNS(Editor.isavizURI,"isv:baseURI");
	rt.appendChild(buri);
	buri.setAttribute("value",Editor.BASE_URI);
	//namespace bindings
	Element bindings=prj.createElementNS(Editor.isavizURI,"isv:nsBindings");
	rt.appendChild(bindings);
	Element aBinding;
	for (int i=0;i<application.tblp.nsTableModel.getRowCount();i++){
	    if (((String)application.tblp.nsTableModel.getValueAt(i,0)).length()>0 && ((String)application.tblp.nsTableModel.getValueAt(i,1)).length()>0){
		aBinding=prj.createElementNS(Editor.isavizURI,"isv:nsBinding");
		aBinding.setAttribute("prefix",(String)application.tblp.nsTableModel.getValueAt(i,0));
		aBinding.setAttribute("uri",(String)application.tblp.nsTableModel.getValueAt(i,1));
		aBinding.setAttribute("dispPrefix",((Boolean)application.tblp.nsTableModel.getValueAt(i,2)).toString());
		bindings.appendChild(aBinding);
	    }
	}
	//property types (Property Types panel)
	Element propTypes=prj.createElementNS(Editor.isavizURI,"isv:propertyTypes");
	rt.appendChild(propTypes);
	Element aPropType;
	DefaultTableModel tm=(DefaultTableModel)application.tblp.prTable.getModel();
	for (int i=0;i<tm.getRowCount();i++){
	    aPropType=prj.createElementNS(Editor.isavizURI,"isv:propertyType");
	    aPropType.setAttribute("ns",(String)tm.getValueAt(i,0));
	    aPropType.setAttribute("name",(String)tm.getValueAt(i,2));
	    propTypes.appendChild(aPropType);
	}
	//initialize table of unique IDs to save in the XML project file (just for nodes of the graph, not edges)
	inodes2UniqueIDs=new Hashtable();
	nextUniqueID=new StringBuffer("0");
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    inodes2UniqueIDs.put(e.nextElement(),nextUniqueID.toString());
	    incPrjID();
	}
	for (Enumeration e=application.literals.elements();e.hasMoreElements();){
	    inodes2UniqueIDs.put(e.nextElement(),nextUniqueID.toString());
	    incPrjID();
	}
	//color index
	/*saving color table (colors often being used by several entities, they are stored in
	  an index to which the entity refers to, thus making it possible to share the definition
	  without repeating it (reduces the ISV/XML file size)
	*/
	Element colors=prj.createElementNS(Editor.isavizURI,"isv:colorTable");	
	rt.appendChild(colors);
	Element aColor;
	for (int i=0;i<ConfigManager.colors.length;i++){
	    /*as the ordering of children is important (and kept) in an XML file, we do not need to save the index itself
	      this is also possible because the only possible operation on the color table is adding a new color ; it is 
	      not possible to remove an existing color, or to overwrite one/put it to null. If this were to be allowed in the 
	      future, we would probably need to store the index itself in the ISV file, as there might be gaps in the index sequence
	    */
	    aColor=prj.createElementNS(Editor.isavizURI,"isv:color");
	    aColor.setAttribute("r",Integer.toString(ConfigManager.colors[i].getRed()));
	    aColor.setAttribute("g",Integer.toString(ConfigManager.colors[i].getGreen()));
	    aColor.setAttribute("b",Integer.toString(ConfigManager.colors[i].getBlue()));
	    colors.appendChild(aColor);
	}
	//fonts
	Element fonts=prj.createElementNS(Editor.isavizURI,"isv:fontTable");
	rt.appendChild(fonts);
	//fonts will be stored as needed, creating an entry in the index if required for each entity
	//used to remember bitmap images that have already been exported, so that they get exported once, and not as many times as they appear in the model
	Hashtable bitmapImages=new Hashtable();
	Vector fontIndex=new Vector();
	//init fontIndex with the default ZVTM/Graph font
	fontIndex.add(Editor.vtmFont);
	// bookmarks
	Element bookmarks = prj.createElementNS(Editor.isavizURI, "isv:bookmarks");
	rt.appendChild(bookmarks);
	String bkTitle;
	Location bkLoc;
	Element bookmark;
	for (Enumeration e=application.bkp.bookmarks.keys();e.hasMoreElements();){
	    bkTitle = (String)e.nextElement();
	    bkLoc = (Location)application.bkp.bookmarks.get(bkTitle);
	    bookmark = prj.createElementNS(Editor.isavizURI, "isv:bookmark");
	    bookmark.setAttribute("x", String.valueOf(bkLoc.getX()));
	    bookmark.setAttribute("y", String.valueOf(bkLoc.getY()));
	    bookmark.setAttribute("z", String.valueOf(bkLoc.getAltitude()));
	    bookmark.appendChild(prj.createTextNode(bkTitle));
	    bookmarks.appendChild(bookmark);
	}
	//resources
	Element ress=prj.createElementNS(Editor.isavizURI,"isv:resources");
	rt.appendChild(ress);
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    IResource r=(IResource)e.nextElement();
	    ress.appendChild(r.toISV(prj,this,bitmapImages,f,fontIndex));
	}
	//literals
	Element lits=prj.createElementNS(Editor.isavizURI,"isv:literals");
	rt.appendChild(lits);
	for (Enumeration e=application.literals.elements();e.hasMoreElements();){
	    lits.appendChild(((ILiteral)e.nextElement()).toISV(prj,this,bitmapImages,f,fontIndex));
	}
	bitmapImages.clear();
	img_subdir=null;
	//properties
	Element props=prj.createElementNS(Editor.isavizURI,"isv:properties");
	rt.appendChild(props);
	Vector v;
	for (Enumeration e=application.propertiesByURI.elements();e.hasMoreElements();){
	    v=(Vector)e.nextElement();
	    for (Enumeration e2=v.elements();e2.hasMoreElements();){
		props.appendChild(((IProperty)e2.nextElement()).toISV(prj,this,fontIndex));
	    }
	}
	//now that all glyphs have been serialized, the fontIndex is in its final state and can be saved as XML
	Element font;
	for (int i=0;i<fontIndex.size();i++){
	    font=prj.createElementNS(Editor.isavizURI,"isv:font");
	    font.setAttribute("desc",Utils.encodeFont((Font)fontIndex.elementAt(i)));
	    fonts.appendChild(font);
	}	
	fontIndex.removeAllElements();
	inodes2UniqueIDs.clear();  //do not need them any longer
	inodes2UniqueIDs=null; 
	//serialize the DOM representation of the ISV/XML project
	application.xmlMngr.serialize(prj,f);
	Editor.vsm.getView(Editor.mainView).setStatusBarText("Saving project to "+f.toString()+" ...done");
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
	Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
    }

    /*given an INode, get its unique project ID (used when loading ISV projects)*/
    protected String getPrjId(Object n){//should be an INode but we do not cast it for efficiency reasons
	return (String)inodes2UniqueIDs.get(n);
    }

    //generate unique IDs to encode the graph structure in ISV project files (used when saving ISV projects)
    private void incPrjID(){
	boolean done=false;
	for (int i=0;i<nextUniqueID.length();i++){
	    byte b=(byte)nextUniqueID.charAt(i);
	    if (b<0x7a){
		nextUniqueID.setCharAt(i,(char)Utils.incByte(b));
		done=true;
		for (int j=0;j<i;j++){nextUniqueID.setCharAt(j,'0');}
		break;
	    }
	}
	if (!done){
	    for (int i=0;i<nextUniqueID.length();i++){nextUniqueID.setCharAt(i,'0');}
	    nextUniqueID.append('0');
	}
    }


    /*create a new resource from ISV projetc file*/
    IResource createIResourceFromISV(Element e){
	IResource res=null;
	boolean anonRes=false;
	if (e.hasAttribute("isAnon")){anonRes=(new Boolean(e.getAttribute("isAnon"))).booleanValue();}
	//display can be omitted if true
	boolean display=true;
	if (e.hasAttribute("display")){display=(e.getAttribute("display").equals("false")) ? false : true;}
	long xt=0;//coords for text label, retrieved from element URIorID if node displayed
	long yt=0;
	String qname=null;
	NodeList nl=e.getElementsByTagNameNS(Editor.isavizURI,"URIorID");
	if (nl.getLength()>0){
	    Element e2=(Element)nl.item(0);
	    if (display){
		xt=(new Long(e2.getAttribute("x"))).longValue();
		yt=(new Long(e2.getAttribute("y"))).longValue();
	    }
	    if (anonRes){
		String anID=null;
		if (e2.getElementsByTagNameNS(Editor.isavizURI,"anonID").getLength()>0){
		    anID=Editor.ANON_NODE+e2.getElementsByTagNameNS(Editor.isavizURI,"anonID").item(0).getFirstChild().getNodeValue();
		}
		res=application.addAnonymousResource(anID);
	    }
	    else {
		if (e2.getElementsByTagNameNS(Editor.isavizURI,"uri").getLength()>0){
		    //new ISV/XML format for IsaViz 2.0 (namespace and localname are no longer split)
		    String uri;
		    boolean fragID=false;
		    try {
			Element e3=(Element)e2.getElementsByTagNameNS(Editor.isavizURI,"uri").item(0);
			uri=e3.getFirstChild().getNodeValue();
			if (e3.hasAttribute("fID")){fragID=Boolean.valueOf(e3.getAttribute("fID")).booleanValue();}
		    }
		    catch (NullPointerException ex){uri="";}
		    res=application.addResource(uri);  //create IResource and add to internal model
		    
		    res.setURIFragment(fragID);
		}
		else {//means that e2.getElementsByTagNameNS(Editor.isavizURI,"namespace").getLength()>0 || e2.getElementsByTagNameNS(Editor.isavizURI,"localname").getLength()>0,a lthough we don;t check for it. This is the old ISV 1.0 format, when namespace and localname were split
		    String ns;
		    String ln;
		    try {
			ns=e2.getElementsByTagNameNS(Editor.isavizURI,"namespace").item(0).getFirstChild().getNodeValue();
		    }//if there is no namespace element, it means that it is equal to the base URI
		    catch (NullPointerException ex){ns=Editor.BASE_URI;}
		    try {
			ln=e2.getElementsByTagNameNS(Editor.isavizURI,"localname").item(0).getFirstChild().getNodeValue();
		    }
		    catch (NullPointerException ex){ln="";}
		    res=application.addResource(ns+ln);  //create IResource and add to internal model
		}
	    }
	    qname=res.getGraphLabel();
	    String ns;
	    //search for a prefix binding for the resource's namespace, if exists and active, replace NS by prefix in text label
	    for (int i=0;i<application.tblp.nsTableModel.getRowCount();i++){
		ns=(String)application.tblp.nsTableModel.getValueAt(i,1);
		if (qname.startsWith(ns)){
		    if (((Boolean)application.tblp.nsTableModel.getValueAt(i,2)).booleanValue()){
			qname=((String)application.tblp.nsTableModel.getValueAt(i,0))+":"+qname.substring(ns.length(),qname.length());
		    }
		    break;
		}
	    }
	}
	else {res=new IResource();}
	if (display){
	    long x=(new Long(e.getAttribute("x"))).longValue();
	    long y=(new Long(e.getAttribute("y"))).longValue();
	    Glyph r=null;
	    String shape=e.getAttribute("shape");
	    if (shape.length()>0){
		r=buildShape(shape.trim(),e,true,x,y);
	    }
	    else {//support for the old IsaViz 1.x ISV/XML format (no shape attribute was specified)
		long w=(new Long(e.getAttribute("w"))).longValue();
		long h=(new Long(e.getAttribute("h"))).longValue();
		r=new VEllipse(x,y,0,w,h,ConfigManager.resourceColorF);
	    }
	    Editor.vsm.addGlyph(r,Editor.mainVirtualSpace);
	    res.setGlyph(r);
	    VText t=null;
	    if (qname!=null){
		t=new VText(xt,yt,0,ConfigManager.resourceColorTB,qname);
		Editor.vsm.addGlyph(t,Editor.mainVirtualSpace);
		res.setGlyphText(t);
	    }
	    if (e.hasAttribute("fill")){
		int fill=Integer.parseInt(e.getAttribute("fill"));
		res.setFillColor(fill);
	    }
	    else {
		res.setFillColor(ConfigManager.defaultRFIndex);
	    }
	    if (e.hasAttribute("stroke")){
		int stroke=Integer.parseInt(e.getAttribute("stroke"));
		res.setStrokeColor(stroke);
	    }
	    else {
		res.setStrokeColor(ConfigManager.defaultRTBIndex);
	    }
	    if (e.hasAttribute("stroke-width")){
		float sw=Float.parseFloat(e.getAttribute("stroke-width"));
		if (e.hasAttribute("stroke-dasharray")){
		    ConfigManager.assignStrokeToGlyph(sw,e.getAttribute("stroke-dasharray").trim(),r);
		}
		else {
		    ConfigManager.assignStrokeToGlyph(r,sw);
		}
	    }
	    else if (e.hasAttribute("stroke-dasharray")){
		ConfigManager.assignStrokeToGlyph(1.0f,e.getAttribute("stroke-dasharray").trim(),r);
	    }
	    if (t!=null && e.hasAttribute("font")){
		int fi=Integer.parseInt(e.getAttribute("font"));
		t.setSpecialFont((Font)fonts.elementAt(fi));
	    }
	    if (e.hasAttribute("text-align")){
		res.setTextAlign(Integer.parseInt(e.getAttribute("text-align")));
	    }
	    if (e.hasAttribute("table")){
		//this can be omitted from the ISV/XML file if false, in which case there is nothing to do as the 
		//default value is false and INode.setTableFormLayout() does not trigger any other particular operation
		res.setTableFormLayout((new Boolean(e.getAttribute("table"))).booleanValue());
	    }
	}
	uniqueIDs2INodes.put(e.getAttribute("id"),res);
	if (e.hasAttribute("commented")){//is node commented out?
	    boolean b=(new Boolean(e.getAttribute("commented"))).booleanValue();
	    if (b){application.commentNode(res,true,true);}
	}
	return res;
    }

    /*create a new literal from ISV project file*/
    ILiteral createILiteralFromISV(Element e){
	ILiteral res=null;
	//display can be omitted if true
	boolean display=true;
	if (e.hasAttribute("display")){display=(e.getAttribute("display").equals("false")) ? false : true;}
	boolean escapeXML=true;
	if (e.hasAttribute("escapeXML")){
	    escapeXML=(new Boolean(e.getAttribute("escapeXML"))).booleanValue();
	}
	long xt=0;//coords for text label
	long yt=0;
	String displayedValue="";
	NodeList nl=e.getElementsByTagNameNS(Editor.isavizURI,"value");
	if (nl.getLength()>0){
	    Element e2=(Element)nl.item(0);
	    if (display){
		xt=(new Long(e2.getAttribute("x"))).longValue();
		yt=(new Long(e2.getAttribute("y"))).longValue();
	    }
	    String value="";
	    if (e2.getFirstChild()!=null){value=e2.getFirstChild().getNodeValue();}
	    displayedValue=((value.length()>=Editor.MAX_LIT_CHAR_COUNT) ? value.substring(0,Editor.MAX_LIT_CHAR_COUNT)+" ..." : value);
	    res=application.addLiteral(value,null,escapeXML);	    
	}
	else {res=application.addLiteral("",null,true);}
	if (display){
	    long x=(new Long(e.getAttribute("x"))).longValue();
	    long y=(new Long(e.getAttribute("y"))).longValue();
	    Glyph r=null;
	    String shape=e.getAttribute("shape");
	    if (shape.length()>0){
		r=buildShape(shape.trim(),e,false,x,y);
	    }
	    else {//support for the old IsaViz 1.x ISV/XML format (no shape attribute was specified)
		long w=(new Long(e.getAttribute("w"))).longValue();
		long h=(new Long(e.getAttribute("h"))).longValue();
		r=new VRectangle(x,y,0,w,h,ConfigManager.literalColorF);
	    }
	    Editor.vsm.addGlyph(r,Editor.mainVirtualSpace);
	    res.setGlyph(r);
	    VText t=null;
	    if (displayedValue!=null && displayedValue.length()>0){
		t=new VText(xt,yt,0,ConfigManager.literalColorTB,displayedValue);
		Editor.vsm.addGlyph(t,Editor.mainVirtualSpace);
		res.setGlyphText(t);
	    }
	    if (e.hasAttribute("fill")){
		int fill=Integer.parseInt(e.getAttribute("fill"));
		res.setFillColor(fill);
	    }
	    else {
		res.setFillColor(ConfigManager.defaultLFIndex);
	    }
	    if (e.hasAttribute("stroke")){
		int stroke=Integer.parseInt(e.getAttribute("stroke"));
		res.setStrokeColor(stroke);
	    }
	    else {
		res.setStrokeColor(ConfigManager.defaultLTBIndex);
	    }
	    if (e.hasAttribute("stroke-width")){
		float sw=Float.parseFloat(e.getAttribute("stroke-width"));
		if (e.hasAttribute("stroke-dasharray")){
		    ConfigManager.assignStrokeToGlyph(sw,e.getAttribute("stroke-dasharray").trim(),r);
		}
		else {
		    ConfigManager.assignStrokeToGlyph(r,sw);
		}
	    }
	    else if (e.hasAttribute("stroke-dasharray")){
		ConfigManager.assignStrokeToGlyph(1.0f,e.getAttribute("stroke-dasharray").trim(),r);
	    }
	    if (t!=null && e.hasAttribute("font")){
		int fi=Integer.parseInt(e.getAttribute("font"));
		t.setSpecialFont((Font)fonts.elementAt(fi));
	    }
	    if (e.hasAttribute("text-align")){
		res.setTextAlign(Integer.parseInt(e.getAttribute("text-align")));
	    }
	    if (e.hasAttribute("table")){
		//this can be omitted from the ISV/XML file if false, in which case there is nothing to do as the 
		//default value is false and INode.setTableFormLayout() does not trigger any other particular operation
		res.setTableFormLayout((new Boolean(e.getAttribute("table"))).booleanValue());
	    }
	}
	if (e.hasAttribute("dtURI")){res.setDatatype(e.getAttribute("dtURI"));}
	if (e.hasAttribute("xml:lang")){res.setLanguage(e.getAttribute("xml:lang"));}
	else if (e.hasAttribute("lang")){res.setLanguage(e.getAttribute("lang"));} //in theory, we should no accept this one, but ISV until 1.1 has been generating lang attrib for project files without the xml: prefix (mistake) so we allow it for compatibility reasons
	uniqueIDs2INodes.put(e.getAttribute("id"),res);
	if (e.hasAttribute("commented")){//is node commented out?
	    boolean b=(new Boolean(e.getAttribute("commented"))).booleanValue();
	    if (b){application.commentNode(res,true,true);}
	}
	return res;
    }

    /*create a new property instance from ISV project file*/
    IProperty createIPropertyFromISV(Element e){
	IProperty res=null;
	//display can be omitted if true
	boolean display=true;
	if (e.hasAttribute("display")){display=(e.getAttribute("display").equals("false")) ? false : true;}
	NodeList nl=e.getElementsByTagNameNS(Editor.isavizURI,"uri");
	String ns="";  //namespace
	String ln="";  //localname
	long xt=0;//coords for text label
	long yt=0;
	String uri=null;
	if (nl.getLength()>0){
	    Element e2=(Element)nl.item(0);
	    ns=e2.getElementsByTagNameNS(Editor.isavizURI,"namespace").item(0).getFirstChild().getNodeValue();
	    ln=e2.getElementsByTagNameNS(Editor.isavizURI,"localname").item(0).getFirstChild().getNodeValue();
	    if (display){
		xt=(new Long(e2.getAttribute("x"))).longValue();
		yt=(new Long(e2.getAttribute("y"))).longValue();
	    }
	    boolean bindingDefined=false;
	    for (int i=0;i<application.tblp.nsTableModel.getRowCount();i++){
		if (((String)application.tblp.nsTableModel.getValueAt(i,1)).equals(ns)){
		    if (((Boolean)application.tblp.nsTableModel.getValueAt(i,2)).booleanValue()){
			uri=((String)application.tblp.nsTableModel.getValueAt(i,0))+":"+ln;
		    }
		    else {uri=ns+ln;}
		    bindingDefined=true;
		    break;
		}
	    }
	    if (!bindingDefined){uri=ns+ln;}
	    res=application.addProperty(ns,ln);
	}
	else {res=new IProperty();}
	//sb and op should always exist, since a predicate cannot exist on its own ; it is always linked to a subject and an object (when one of these is deleted, the predicate is automatically destroyed)
	IResource subject=(IResource)uniqueIDs2INodes.get(e.getAttribute("sb"));
	res.setSubject(subject);
	subject.addOutgoingPredicate(res);
	Object o1=uniqueIDs2INodes.get(e.getAttribute("ob"));
	if (o1 instanceof IResource){
	    IResource object=(IResource)o1;
	    res.setObject(object);
	    object.addIncomingPredicate(res);
	}
	else {//o1 is an ILiteral (or we have an error)
	    ILiteral object=(ILiteral)o1;
	    res.setObject(object);
	    object.setIncomingPredicate(res);
	    if (res.getIdent().equals(Editor.RDFS_NAMESPACE_URI+"label")){//if property is rdfs:label, set label for the resource
		subject.setLabel(object.getValue());
	    }
	}
	if (display){
	    if (e.hasAttribute("table")){
		//this can be omitted from the ISV/XML file if false, in which case there is nothing to do as the 
		//default value is false and INode.setTableFormLayout() does not trigger any other particular operation
		res.setTableFormLayout((new Boolean(e.getAttribute("table"))).booleanValue());
		if (res.isLaidOutInTableForm()){//if property laid out in table - (there is no arrow head)
		    if (subject2sharededge.containsKey(res.getSubject())){//if edge already constructed for another property in the same table
			Vector edge=(Vector)subject2sharededge.get(res.getSubject());//get it and assign it tot he new property
			VPath p=(VPath)edge.elementAt(0);
// 			VTriangleOr tr=(VTriangleOr)edge.elementAt(1);
			res.setGlyph(p,null);
		    }
		    else {//if first property constructed for this table
			Vector edge=buildEdge(e);//construct the edge and store it so that it can be accessed by other
			VPath p=(VPath)edge.elementAt(0);//properties in the same table
// 			VTriangleOr tr=(VTriangleOr)edge.elementAt(1);
// 			Editor.vsm.addGlyph(tr,Editor.mainVirtualSpace);
			Editor.vsm.addGlyph(p,Editor.mainVirtualSpace);
			res.setGlyph(p,null);
			subject2sharededge.put(res.getSubject(),edge);
		    }
		}
		else {//if not laid out in a table (no edge sharing, nothing special to do)
		    Vector edge=buildEdge(e);
		    VPath p=(VPath)edge.elementAt(0);
		    Editor.vsm.addGlyph(p,Editor.mainVirtualSpace);
		    VTriangleOr tr=null;
		    if (edge.size()>=2){
			tr=(VTriangleOr)edge.elementAt(1);
			Editor.vsm.addGlyph(tr,Editor.mainVirtualSpace);
		    }
		    res.setGlyph(p,tr);
		}
	    }
	    else {//if not laid out in a table (no edge sharing, nothing special to do)
		Vector edge=buildEdge(e);
		VPath p=(VPath)edge.elementAt(0);
		Editor.vsm.addGlyph(p,Editor.mainVirtualSpace);
		VTriangleOr tr=null;
		if (edge.size()>=2){
		    tr=(VTriangleOr)edge.elementAt(1);
		    Editor.vsm.addGlyph(tr,Editor.mainVirtualSpace);
		}
		res.setGlyph(p,tr);
	    }
	    VText t=null;
	    if (uri!=null){
		if (res.isLaidOutInTableForm()){
		    if (res.getObject() instanceof IResource){
			t=new VText(xt,yt,0,ConfigManager.resourceColorTB,uri);
			res.textIndex=ConfigManager.defaultRTBIndex;
		    }
		    else {
			t=new VText(xt,yt,0,ConfigManager.literalColorTB,uri);
			res.textIndex=ConfigManager.defaultLTBIndex;
		    }
		}
		else {
		    t=new VText(xt,yt,0,ConfigManager.propertyColorT,uri);
		    res.textIndex=ConfigManager.defaultPTIndex;
		}
		res.setGlyphText(t);
	    }
	    if (e.hasAttribute("stroke")){
		int stroke=Integer.parseInt(e.getAttribute("stroke"));
		res.setStrokeColor(stroke);
		if (!res.isLaidOutInTableForm()){res.setTextColor(stroke);}
	    }
	    else {
		res.setStrokeColor(ConfigManager.defaultPBIndex);
	    }
	    if (e.hasAttribute("stroke-width")){
		float sw=Float.parseFloat(e.getAttribute("stroke-width"));
		if (e.hasAttribute("stroke-dasharray")){
		    ConfigManager.assignStrokeToGlyph(sw,e.getAttribute("stroke-dasharray").trim(),res.getGlyph());
		}
		else {
		    ConfigManager.assignStrokeToGlyph(res.getGlyph(),sw);
		}
	    }
	    else if (e.hasAttribute("stroke-dasharray")){
		ConfigManager.assignStrokeToGlyph(1.0f,e.getAttribute("stroke-dasharray").trim(),res.getGlyph());
	    }
	    //no need to adjust arrow head size when loading from ISV as the geom info is saved in the file
// 	    if (!res.isLaidOutInTableForm() && res.getGlyphHead()!=null){//in std node-edge layout,
// 		//adapt arrow head size to edge's thickness (otherwise it might be too small, or even not visible)
// 		if (res.getGlyph().getStrokeWidth()>2.0f){
// 		    res.getGlyphHead().reSize(res.getGlyph().getStrokeWidth()/2.0f);
// 		}
// 	    }
	    if (t!=null && e.hasAttribute("font")){
		int fi=Integer.parseInt(e.getAttribute("font"));
		t.setSpecialFont((Font)fonts.elementAt(fi));
	    }
	    if (e.hasAttribute("text-align")){
		res.setTextAlign(Integer.parseInt(e.getAttribute("text-align")));
	    }
	    if (res.isLaidOutInTableForm()){
		if (e.getElementsByTagNameNS(Editor.isavizURI,"cell").getLength()>0){
		    Element e3=(Element)(e.getElementsByTagNameNS(Editor.isavizURI,"cell")).item(0);
		    long xc=(new Long(e3.getAttribute("x"))).longValue();
		    long yc=(new Long(e3.getAttribute("y"))).longValue();
		    long wc=(new Long(e3.getAttribute("w"))).longValue();
		    long hc=(new Long(e3.getAttribute("h"))).longValue();
		    VRectangle cellGlyph=new VRectangle(xc,yc,0,wc,hc,Color.white);
		    Editor.vsm.addGlyph(cellGlyph,Editor.mainVirtualSpace);
		    res.setTableCellGlyph(cellGlyph);
		    if (e.hasAttribute("fill")){
			int fill=Integer.parseInt(e.getAttribute("fill"));
			res.setCellFillColor(fill);
		    }
		    if (e.hasAttribute("tstroke")){
			int cstroke=Integer.parseInt(e.getAttribute("tstroke"));
			res.setTextColor(cstroke);
		    }
		}
	    }
	    //done at the end so that it gets above the table cell (if any) in the drawing list
	    if (t!=null){Editor.vsm.addGlyph(t,Editor.mainVirtualSpace);}
	}
	if (e.hasAttribute("commented")){//is edge commented out?
	    boolean b=(new Boolean(e.getAttribute("commented"))).booleanValue();
	    if (b){application.commentPredicate(res,true,false);}
	}
	return res;
    }

    public static Glyph buildShape(String shape,Element e,boolean resource,long x,long y){//if resource=false, means we are dealing with a literal
	Glyph r=null;
	if (shape.startsWith("[") && shape.endsWith("]")){
	    float[] vertices=Style.parseCustomShape(shape);
	    if (vertices!=null){
		try {
		    float sz=Float.parseFloat(e.getAttribute("sz"));
		    float or=Float.parseFloat(e.getAttribute("or"));
		    r=new VShape(x,y,0,(long)sz,vertices,ConfigManager.resourceColorF,or);
		}
		catch (NumberFormatException nfe){
		    if (resource){
			r=new VEllipse(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.resourceColorF);
		    }
		    else {
			r=new VRectangle(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.literalColorF);
		    }
		}
	    }
	    else {
		if (resource){
		    r=new VEllipse(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.resourceColorF);
		}
		else {
		    r=new VRectangle(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.literalColorF);
		}
	    }
	}
	else if (shape.startsWith("{") && shape.endsWith("}")){
	    float[] vertices=Style.parseCustomPolygon(shape);
	    if (vertices!=null){
		LongPoint[] coords=new LongPoint[vertices.length/2];
		for (int i=0;i<coords.length;i++){
		    coords[i]=new LongPoint((long)vertices[2*i],(long)vertices[2*i+1]);
		}
		r=new VPolygon(coords,Color.white);
	    }
	    else {
		if (resource){
		    r=new VEllipse(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.resourceColorF);
		}
		else {
		    r=new VRectangle(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.literalColorF);
		}
	    }
	}
	else if (shape.equals("icon")){
	    File f=new File(Editor.projectFile.getParent(),e.getAttributeNS(com.xerox.VTM.svg.SVGWriter.xlinkURI,"href"));
	    if (f.exists()){
		try {
		    ImageIcon ii=new ImageIcon(f.toURL());
		    if (ii!=null){r=new VImage(x,y,0,ii.getImage());}
		}
		catch (MalformedURLException mue){System.err.println("ISVManager.openProject()/buildShape():Error: failed to instantiate bitmap image file "+f.toString());mue.printStackTrace();}
	    }
	    else {System.err.println("ISVManager.openProject()/buildShape():Error: failed to instantiate bitmap image file "+f.toString());}
	    long w=(new Long(e.getAttribute("w"))).longValue();
	    long h=(new Long(e.getAttribute("h"))).longValue();
	    if (r!=null){
		((VImage)r).setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
		RectangularShape rs=(RectangularShape)r;
		if (rs.getWidth()>=rs.getHeight()){//adjust the icon to the width/height declared in the ISV/XML file
		    rs.setWidth(w);
		}
		else {
		    rs.setHeight(h);
		}
	    }
	    else {
		r=new VRectangle(x,y,0,w,h,Color.white);
	    }
	}
	else {
	    try {
		Integer predefShape=new Integer(shape);
		if (predefShape.equals(Style.ELLIPSE)){
		    long w=(new Long(e.getAttribute("w"))).longValue();
		    long h=(new Long(e.getAttribute("h"))).longValue();
		    r=new VEllipse(x,y,0,w,h,ConfigManager.resourceColorF);
		}
		else if (predefShape.equals(Style.RECTANGLE)){
		    long w=(new Long(e.getAttribute("w"))).longValue();
		    long h=(new Long(e.getAttribute("h"))).longValue();
		    r=new VRectangle(x,y,0,w,h,ConfigManager.resourceColorF);
		}
		else if (predefShape.equals(Style.ROUND_RECTANGLE)){
		    long w=(new Long(e.getAttribute("w"))).longValue();
		    long h=(new Long(e.getAttribute("h"))).longValue();
		    r=new VRoundRect(x,y,0,w,h,ConfigManager.resourceColorF,Math.round(SVGReader.RRARCR*Math.min(w,h)),Math.round(SVGReader.RRARCR*Math.min(w,h)));
		}
		else if (predefShape.equals(Style.CIRCLE)){
		    float sz=Float.parseFloat(e.getAttribute("sz"));
		    r=new VCircle(x,y,0,(long)sz,ConfigManager.resourceColorF);
		}
		else if (predefShape.equals(Style.DIAMOND)){
		    float sz=Float.parseFloat(e.getAttribute("sz"));
		    r=new VDiamond(x,y,0,(long)sz,ConfigManager.resourceColorF);
		}
		else if (predefShape.equals(Style.OCTAGON)){
		    float sz=Float.parseFloat(e.getAttribute("sz"));
		    r=new VOctagon(x,y,0,(long)sz,ConfigManager.resourceColorF);
		}
		else if (predefShape.equals(Style.TRIANGLEN)){
		    float sz=Float.parseFloat(e.getAttribute("sz"));
		    r=new VTriangleOr(x,y,0,(long)sz,ConfigManager.resourceColorF,0);
		}
		else if (predefShape.equals(Style.TRIANGLES)){
		    float sz=Float.parseFloat(e.getAttribute("sz"));
		    r=new VTriangleOr(x,y,0,(long)sz,ConfigManager.resourceColorF,(float)Math.PI);
		}
		else if (predefShape.equals(Style.TRIANGLEE)){
		    float sz=Float.parseFloat(e.getAttribute("sz"));
		    r=new VTriangleOr(x,y,0,(long)sz,ConfigManager.resourceColorF,(float)-Math.PI/2.0f);
		}
		else if (predefShape.equals(Style.TRIANGLEW)){
		    float sz=Float.parseFloat(e.getAttribute("sz"));
		    r=new VTriangleOr(x,y,0,(long)sz,ConfigManager.resourceColorF,(float)Math.PI/2.0f);
		}
		else {//default/error
		    long w=(new Long(e.getAttribute("w"))).longValue();
		    long h=(new Long(e.getAttribute("h"))).longValue();
		    r=new VEllipse(x,y,0,w,h,ConfigManager.resourceColorF);
		}
	    }
	    catch (Exception ex){
		if (resource){
		    r=new VEllipse(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.resourceColorF);
		}
		else {
		    r=new VRectangle(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.literalColorF);
		}
	    }
	}
	return r;
    }

    public static Vector buildEdge(Element e){
	Vector res=new Vector();
	Element e1=(Element)(e.getElementsByTagNameNS(Editor.isavizURI,"path")).item(0);
	String d=e1.getAttribute("d");
	VPath p=new VPath(0,ConfigManager.propertyColorB,d);
	//VPath p=new VClippedPath(0,ConfigManager.propertyColorB,d);
	res.add(p);
	if (e.getElementsByTagNameNS(Editor.isavizURI,"head").getLength()>0){
	    Element e3=(Element)(e.getElementsByTagNameNS(Editor.isavizURI,"head")).item(0);
	    long x=(new Long(e3.getAttribute("x"))).longValue();
	    long y=(new Long(e3.getAttribute("y"))).longValue();
	    long w=(new Long(e3.getAttribute("w"))).longValue();
	    float h=(new Float(e3.getAttribute("or"))).floatValue();
	    VTriangleOr tr=new VTriangleOr(x,y,0,w,ConfigManager.propertyColorB,h);
	    //tr.setPaintBorder(false);
	    res.add(tr);
	}
	return res;
    }

}
