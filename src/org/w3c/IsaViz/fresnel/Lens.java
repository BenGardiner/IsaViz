/*   FILE: Lens.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: Lens.java,v 1.16 2006/10/29 11:09:03 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import org.w3c.IsaViz.IResource;
import org.w3c.IsaViz.ILiteral;
import org.w3c.IsaViz.IProperty;
import org.w3c.IsaViz.INode;
import org.w3c.IsaViz.Editor;
import org.w3c.IsaViz.ConfigManager;

import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.VText;
import net.claribole.zvtm.engine.PostAnimationAction;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

public class Lens {

    /* the lens' URI */
    String uri;

    /* holds the lens's label (used to denote the lens in the GUI).
       This is the value of the rdfs:label property associated with the lens,
       or the lens' local name part of its URI if no rdfs:label is associated with it*/
    String lensLabel;
    /* holds comments about this lens (declared via an rdfs:comment property) */
    String lensComment;

    /* domains expressed with various selector languages */
    String[] basicInstanceDomains = null;
    String[] basicClassDomains = null;
    FSLPath[] fslInstanceDomains = null;
//     String[] sparqlInstanceDomains = null;

    int apIndex = -1;

    /* properties to show (an items can be a property URI or an FSL path expr
       starting with and ending by an arc step) */
    PropertyVisibility[] p2s;
    
    /* properties to hide (an items can be a property URI or an FSL path expr
       starting with and ending by an arc step) */
    PropertyVisibility[] p2h;

    Format[] associatedFormats;

    Hashtable nodesToUnrender = new Hashtable();
    Hashtable arcsToUnrender = new Hashtable();
    Hashtable additionalContentToUnrender = new Hashtable();

    public Lens(String uri, String baseURI){
	this.uri = uri;
	if (uri.startsWith(baseURI)){
	    lensLabel = uri.substring(baseURI.length());
	}
	else {
	    lensLabel = uri;
	}
	if (lensLabel.startsWith("#")){lensLabel = lensLabel.substring(1);}
    }

    // expr is a String for basic selectors, an FSLPath for FSL selectors, and ? for SPARQL selectors
    void addInstanceDomain(Object expr, short selectorLanguage){
	if (selectorLanguage == FresnelParser._FSL_SELECTOR){// FSL selector
	    if (fslInstanceDomains == null){
		fslInstanceDomains = new FSLPath[1];
		fslInstanceDomains[0] = (FSLPath)expr;
	    }
	    else {
		FSLPath[] tmpA = new FSLPath[fslInstanceDomains.length+1];
		System.arraycopy(fslInstanceDomains, 0, tmpA, 0, fslInstanceDomains.length);
		tmpA[fslInstanceDomains.length] = (FSLPath)expr;
		fslInstanceDomains = tmpA;
	    }
	}
	else if (selectorLanguage == FresnelParser._SPARQL_SELECTOR){// SPARQL selector
	    //XXX: TBW
	}
	else {// basic selector
	    if (basicInstanceDomains == null){
		basicInstanceDomains = new String[1];
		basicInstanceDomains[0] = (String)expr;
	    }
	    else {
		String[] tmpA = new String[basicInstanceDomains.length+1];
		System.arraycopy(basicInstanceDomains, 0, tmpA, 0, basicInstanceDomains.length);
		tmpA[basicInstanceDomains.length] = (String)expr;
		basicInstanceDomains = tmpA;
	    }
	}
    }

    void addClassDomain(String expr){
	// we only support basic selectors for class lens domains
	if (basicClassDomains == null){
	    basicClassDomains = new String[1];
	    basicClassDomains[0] = (String)expr;
	}
	else {
	    String[] tmpA = new String[basicClassDomains.length+1];
	    System.arraycopy(basicClassDomains, 0, tmpA, 0, basicClassDomains.length);
	    tmpA[basicClassDomains.length] = (String)expr;
	    basicClassDomains = tmpA;
	}
    }


    boolean selectsByBIS(IResource r){
	if (basicInstanceDomains != null){
	    for (int i=0;i<basicInstanceDomains.length;i++){
		if (basicInstanceDomains[i].equals(r.getIdentity())){return true;}
	    }
	}
	return false;
    }

    boolean selectsByBCS(IResource r){
	if (basicClassDomains != null){
	    for (int i=0;i<basicClassDomains.length;i++){
		if (r.hasRDFType(basicClassDomains[i])){return true;}
	    }
	}
	return false;
    }

    void setPropertiesVisibility(Vector ts, Vector th, int api){
	apIndex = api;
	p2s = new PropertyVisibility[ts.size()];
	Object o;
	for (int i=0;i<p2s.length;i++){
	    o = ts.elementAt(i);
	    if (o instanceof FSLPath){p2s[i] = new FSLVisibility((FSLPath)o);}
	    else {p2s[i] = new BasicVisibility((String)o);}
	}
	if (apIndex != -1){
	    p2h = new PropertyVisibility[th.size()];
	    for (int i=0;i<p2s.length;i++){
		o = ts.elementAt(i);
		if (o instanceof FSLPath){p2s[i] = new FSLVisibility((FSLPath)o);}
		else {p2s[i] = new BasicVisibility((String)o);}
	    }
	}
    }

    void addAssociatedFormats(Format[] f){
	if (associatedFormats == null){
	    associatedFormats = f;
	}
	else {
	    Format[] tmpA = new Format[associatedFormats.length + f.length];
	    System.arraycopy(associatedFormats,0,tmpA,0,associatedFormats.length);
	    System.arraycopy(f,0,tmpA,associatedFormats.length,f.length);
	    associatedFormats = tmpA;
	}
    }

    Format[] getAssociatedFormats(){
	return (associatedFormats != null) ? associatedFormats : new Format[0];
    }

    Hashtable statements2formats = new Hashtable();

    void render(IResource r, FresnelManager fm){
	IProperty p;
	INode n;
	ArcInfo ai;
	NodeInfo ni;
	Vector ais = new Vector();
	Vector nis = new Vector();
	Vector acs = new Vector();
	Vector statementsToDisplay = new Vector();
	Vector ip = r.getIncomingPredicates();
	Vector op = r.getOutgoingPredicates();
	if (apIndex != -1){
	    //XXX: TBW
	}
	else {
	    // we only have to deal with properties in showProperties as hideProperties
	    // is a black list applied to elements of fresnel:allProperties only 
	    if (p2s != null){
		// ordering is reflected by the ordering of items in p2s
		for (int i=0;i<p2s.length;i++){
		    p2s[i].getPropertiesToShow(r, statementsToDisplay, ip, op);
		}
		statements2formats.clear();
		for (int i=0;i<statementsToDisplay.size();i++){
		    p = (IProperty)statementsToDisplay.elementAt(i);
		    statements2formats.put(p, fm.getAssociatedPropertyFormat(this, p));
		}
		Vector lines = new Vector();
		Vector v = new Vector();
		Object currentStatement = statementsToDisplay.firstElement();
		v.add(currentStatement);
		lines.add(v);
		Object previousStatement;
		Object formatOfPreviousStatement, formatOfCurrentStatement;
		// (A)
		for (int i=1;i<statementsToDisplay.size();i++){
		    previousStatement = currentStatement;
		    currentStatement = statementsToDisplay.elementAt(i);
		    formatOfPreviousStatement = statements2formats.get(previousStatement);
		    formatOfCurrentStatement = statements2formats.get(currentStatement);
		    if (formatOfCurrentStatement != null && formatOfCurrentStatement == formatOfPreviousStatement &&
			((Format)formatOfCurrentStatement).hasValueFormattingInstructions()){
			// new value goes on same line as previous value
			((Vector)lines.lastElement()).add(currentStatement);
		    }
		    else {// new value goes on a new line
			v = new Vector();
			v.add(currentStatement);
			lines.add(v);
		    }
		}
		// compute new location of property values (node and arc)
		LongPoint[] positionOfFirstNodeOnLine = new LongPoint[lines.size()];
		LongPoint[] positionOfArcForLine = new LongPoint[lines.size()];
		FresnelManager.computeTranslations(r, positionOfFirstNodeOnLine, positionOfArcForLine);
		Format f;
		AdditionalContentInfo aci;
		VText ac;
		PostAnimationAction paa;
		// apply transformation
		for (int i=0;i<lines.size();i++){
		    v = (Vector)lines.elementAt(i); // v contains all statements for the current line (IProperty instance)
		    // the same format applies to all elements on a line (ensured by above loop (A))
		    f = (Format)statements2formats.get(v.firstElement());
		    long positionOfNodeOnLine = positionOfFirstNodeOnLine[i].x - Math.round((double)((IProperty)v.firstElement()).getObject().getGlyph().getSize());
		    if (f != null && f.hasValueFormattingInstructions()){
			aci = new AdditionalContentInfo();
			f.computeAdditionalContentWidth(Editor.mView.getGraphicsContext());
			if (f.contentFirstV != null){
			    ac = new VText(positionOfNodeOnLine, positionOfFirstNodeOnLine[i].y, 0,
					   ConfigManager.propertyColorT, f.contentFirstV, VText.TEXT_ANCHOR_END);
			    aci.add(ac);
			    fm.showAdditionalContent(ac);
			}
		    }
		    else {
			aci = null;
		    }
		    for (int j=0;j<v.size();j++){
			p = (IProperty)v.elementAt(j);
			n = p.getObject();
			ai = new ArcInfo(p);
			ni = new NodeInfo(n);
			// the following takes care of both formatting the property arc and value node
			if (f != null){
			    paa = new FormatCaller(f, p, ai, ni);
			}
			else {
			    paa = null;
			}
			if (aci != null){// equiv. to f != null && f.hasValueFormattingInstructions()
			    if (f.contentBeforeV != null){
				ac = new VText(positionOfNodeOnLine, positionOfFirstNodeOnLine[i].y, 0,
					       ConfigManager.propertyColorT, f.contentBeforeV, VText.TEXT_ANCHOR_START);
				aci.add(ac);
				fm.showAdditionalContent(ac);
				positionOfNodeOnLine += f.contentBeforeVwidth;
			    }
			}
			positionOfNodeOnLine += Math.round((double)n.getGlyph().getSize());
			fm.colorize(n);
			fm.bringCloser(n, ni, positionOfNodeOnLine, positionOfFirstNodeOnLine[i].y, paa);
			if (j == 0){// animate arc only for first item of each line (we only want one arc)
			    fm.colorize(p, false, true);
			    fm.bringCloser(p, ai, positionOfArcForLine[i].x, positionOfArcForLine[i].y, null);
			    if (f != null){
				fm.changeLabel(p, ai, f.getLabel());
			    }
			    fm.createTemporaryArc(r.getGlyph().vx, r.getGlyph().vy,
						  positionOfFirstNodeOnLine[i].x, positionOfFirstNodeOnLine[i].y, ai);
			    Editor.mSpace.onTop(ai.replacementArc);
			    ais.add(ai);
			}
			nis.add(ni);
			// show subject and object nodes on top of arc as the latter
			// goes from the center of one to the center of the other
			Editor.mSpace.onTop(r.getGlyph());
			Editor.mSpace.onTop(r.getGlyphText());
			Editor.mSpace.onTop(n.getGlyph());
			Editor.mSpace.onTop(n.getGlyphText());
			positionOfNodeOnLine += Math.round((double)n.getGlyph().getSize());
			if (j < v.size() - 1){// do not apply contentAfter to last item
			    if (aci != null){// equiv. to f != null && f.hasValueFormattingInstructions()
				if (f.contentAfterV != null){
				    ac = new VText(positionOfNodeOnLine, positionOfFirstNodeOnLine[i].y, 0,
						   ConfigManager.propertyColorT, f.contentAfterV, VText.TEXT_ANCHOR_START);
				    aci.add(ac);
				    fm.showAdditionalContent(ac);
				    positionOfNodeOnLine += f.contentAfterVwidth;
				}
			    }
			}
		    }
		    if (aci != null){// equiv. to f != null && f.hasValueFormattingInstructions()
			if (f.contentLastV != null){
			    ac = new VText(positionOfNodeOnLine, positionOfFirstNodeOnLine[i].y, 0,
					   ConfigManager.propertyColorT, f.contentLastV, VText.TEXT_ANCHOR_START);
			    aci.add(ac);
			    fm.showAdditionalContent(ac);
			}
			acs.add(aci);
		    }
		}
		arcsToUnrender.put(r, ais);
		nodesToUnrender.put(r, nis);
		additionalContentToUnrender.put(r, acs);
	    }
	}
    }

    void unrender(INode n, FresnelManager fm){
	ArcInfo ai;
	for (Enumeration e=((Vector)arcsToUnrender.get(n)).elements();e.hasMoreElements();){
	    ai = (ArcInfo)e.nextElement();
	    Format.unrender(ai);
	    fm.gray(ai.owner, false, true);
	    fm.putAway(ai);
	    fm.destroyTemporaryArc(ai);
	}
	arcsToUnrender.remove(n);
	INode n2;
	NodeInfo ni;
	for (Enumeration e=((Vector)nodesToUnrender.get(n)).elements();e.hasMoreElements();){
	    ni = (NodeInfo)e.nextElement();
	    Format.unrender(ni);
	    fm.gray(ni.owner);
	    fm.putAway(ni);
	}
	nodesToUnrender.remove(n);
	for (Enumeration e=((Vector)additionalContentToUnrender.get(n)).elements();e.hasMoreElements();){
	    ((AdditionalContentInfo)e.nextElement()).destroy(fm);
	}
	additionalContentToUnrender.remove(n);
    }


    /* debugging */
//    void printDomains(){
// 	System.out.println("DOMAINS");
// 	if (basicInstanceDomains != null){
// 	    System.out.println("-------------------\nBasic selectors: instance domains\n-------------------");
// 	    for (int i=0;i<basicInstanceDomains.length;i++){
// 		System.out.println(basicInstanceDomains[i]);
// 	    }
// 	}
// 	if (basicClassDomains != null){
// 	    System.out.println("-------------------\nBasic selectors: class domains\n-------------------");
// 	    for (int i=0;i<basicClassDomains.length;i++){
// 		System.out.println(basicClassDomains[i]);
// 	    }
// 	}
// 	if (fslInstanceDomains != null){
// 	    System.out.println("-------------------\nFSL selectors: instance domains\n-------------------");
// 	    for (int i=0;i<fslInstanceDomains.length;i++){
// 		System.out.println(fslInstanceDomains[i]);
// 	    }
// 	}
//     }

   void printVisibility(){
	System.out.println("VISIBILITY, allProperties at "+apIndex);
	if (p2s != null){
	    System.out.println("-------------------\nShow properties\n-------------------");
	    for (int i=0;i<p2s.length;i++){
		System.out.println(p2s[i]);
	    }
	}
	if (p2h != null){
	    System.out.println("-------------------\nHide properties\n-------------------");
	    for (int i=0;i<p2h.length;i++){
		System.out.println(p2h[i]);
	    }
	}
    }

    void setLabel(String s){
	this.lensLabel = s;
    }

    void setComment(String s){
	this.lensComment = s;
    }

    public String toString(){
	return lensLabel;
    }

    String getComment(){
	return (lensComment != null) ? lensComment : "";
    }    

}
