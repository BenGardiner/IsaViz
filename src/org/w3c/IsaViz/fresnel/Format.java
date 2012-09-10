/*   FILE: Format.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: Format.java,v 1.8 2006/10/29 11:09:03 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import org.w3c.IsaViz.Editor;
import org.w3c.IsaViz.INode;
import org.w3c.IsaViz.IProperty;
import org.w3c.IsaViz.IResource;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.glyphs.VImage;

import java.awt.Graphics;
import java.awt.FontMetrics;
import javax.swing.ImageIcon;

import java.net.URL;

public class Format {

    static final String EMPTY = "";

    String uri;

    String[] basicPropertyDomains;
    FSLPath[] fslPropertyDomains;

    static final short NOT_SPECIFIED = 0;
    static final short VALUE_NONE = 1;
    static final short VALUE_IMAGE = 2;
    static final short VALUE_URI = 3;
    static final short VALUE_EXTERNAL_LINK = 4;
    /* how to handle the value (as an image, as a link, as text, etc.) */
    short value = NOT_SPECIFIED;


    static final short NO_LABEL = 0;
    static final short PROPERTY_RDFS_LABEL = 1;
    static final short CUSTOM_LABEL = 2;
    /* what label to show in front of the value */
    short label = PROPERTY_RDFS_LABEL;
    String customLabel;

    /* strings to preppend/append to values (null if none) */
    String contentFirstV, contentBeforeV, contentAfterV, contentLastV;
    int contentFirstVwidth, contentBeforeVwidth, contentAfterVwidth, contentLastVwidth;

    public Format(String uri){
	this.uri = uri;
    }

    // expr is a String for basic selectors, an FSLPath for FSL selectors, and ? for SPARQL selectors
    void addPropertyDomain(Object expr, short selectorLanguage){
	if (selectorLanguage == FresnelParser._FSL_SELECTOR){// FSL selector
	    if (fslPropertyDomains == null){
		fslPropertyDomains = new FSLPath[1];
		fslPropertyDomains[0] = (FSLPath)expr;
	    }
	    else {
		FSLPath[] tmpA = new FSLPath[fslPropertyDomains.length+1];
		System.arraycopy(fslPropertyDomains, 0, tmpA, 0, fslPropertyDomains.length);
		tmpA[fslPropertyDomains.length] = (FSLPath)expr;
		fslPropertyDomains = tmpA;
	    }
	}
	else {// basic selector
	    if (basicPropertyDomains == null){
		basicPropertyDomains = new String[1];
		basicPropertyDomains[0] = (String)expr;
	    }
	    else {
		String[] tmpA = new String[basicPropertyDomains.length+1];
		System.arraycopy(basicPropertyDomains, 0, tmpA, 0, basicPropertyDomains.length);
		tmpA[basicPropertyDomains.length] = (String)expr;
		basicPropertyDomains = tmpA;
	    }
	}
    }

    boolean selectsByBPS(IProperty p){
	if (basicPropertyDomains != null){
	    for (int i=0;i<basicPropertyDomains.length;i++){
		if (p.getIdent().equals(basicPropertyDomains[i])){return true;}
	    }
	}
	return false;
    }

    boolean selectsByFPS(IProperty p){
	if (fslPropertyDomains != null){
	    for (int i=0;i<fslPropertyDomains.length;i++){
		//XXX: TBW if (){return true;}
	    }
	}
	return false;
    }

    void setValue(String expr){
	if (expr.equals(FresnelParser._image)){
	    value = VALUE_IMAGE;
	}
	else if (expr.equals(FresnelParser._none)){
	    value  = VALUE_NONE;
	}
	else if (expr.equals(FresnelParser._uri)){
	    value = VALUE_URI;
	}
	else if (expr.equals(FresnelParser._externalLink)){
	    value = VALUE_EXTERNAL_LINK;
	}
	else {
	    value = NOT_SPECIFIED;
	}
    }

    void setLabel(RDFNode n){
	if (n.isResource()){
	    if (n.toString().equals(FresnelParser._none)){label = NO_LABEL;}
	    else if (n.toString().equals(FresnelParser._show)){label = PROPERTY_RDFS_LABEL;}
	    else {label = NO_LABEL;}
	    customLabel = null;
	}
	else {
	    label = CUSTOM_LABEL;
	    customLabel = ((Literal)n).getLexicalForm();
	}
    }

    String getLabel(){
	switch(label){
	case NO_LABEL:{return EMPTY;}
	case PROPERTY_RDFS_LABEL:{return null;}  //XXX: retrieve RDFS label associated with property and return it
	case CUSTOM_LABEL:{return customLabel;}
	default:{return null;}  //XXX: retrieve RDFS label associated with property and return it
	}
    }

    /* process instructions such as contentAfter, contentLast, etc. */
    void addValueFormattingInstruction(Resource r){
	StmtIterator si = r.listProperties();
	Statement s;
	String pred;
	while (si.hasNext()){
	    s = si.nextStatement();
	    pred = s.getPredicate().getURI();
	    if (pred.equals(FresnelParser.FRESNEL_NAMESPACE_URI+FresnelParser._contentAfter)){
		contentAfterV = s.getLiteral().getLexicalForm();
	    }
	    else if (pred.equals(FresnelParser.FRESNEL_NAMESPACE_URI+FresnelParser._contentBefore)){
		contentBeforeV = s.getLiteral().getLexicalForm();
	    }
	    else if (pred.equals(FresnelParser.FRESNEL_NAMESPACE_URI+FresnelParser._contentLast)){
		contentLastV = s.getLiteral().getLexicalForm();
	    }
	    else if (pred.equals(FresnelParser.FRESNEL_NAMESPACE_URI+FresnelParser._contentFirst)){
		contentFirstV = s.getLiteral().getLexicalForm();
	    }
	}
    }

    boolean hasValueFormattingInstructions(){
	return (contentFirstV != null || contentBeforeV != null || contentAfterV != null || contentLastV != null);
    }

    void computeAdditionalContentWidth(Graphics g){
	FontMetrics fmetrics = g.getFontMetrics(Editor.vtmFont);
	if (contentFirstV != null){
	    contentFirstVwidth = fmetrics.stringWidth(contentFirstV);
	}
	if (contentBeforeV != null){
	    contentBeforeVwidth = fmetrics.stringWidth(contentBeforeV);
	}
	if (contentAfterV != null){
	    contentAfterVwidth = fmetrics.stringWidth(contentAfterV);
	}
	if (contentLastV != null){
	    contentLastVwidth = fmetrics.stringWidth(contentLastV);
	}
    }

    // takes care of both the property arc and value node
    void render(final IProperty p, final ArcInfo ai, final NodeInfo ni){
	if (value == VALUE_IMAGE){
	    // launch this in a separate thread as retrieving
	    // the image from the Web can take time
	    final SwingWorker worker = new SwingWorker(){
		    public Object construct(){
			Format.this.displayImage(p, ai, ni);
			return null; 
		    }
		};
	    worker.start();
	}
	//XXX: we have to deal with all the other instructions
    }

    void displayImage(IProperty p, ArcInfo ai, NodeInfo ni){
	INode object = p.getObject();
	try {
	    ImageIcon ii = FresnelManager.getIcon(new URL(((IResource)object).getIdentity()));
	    if (ii != null){
		ni.replacementImage = new VImage(object.getGlyph().vx, object.getGlyph().vy,
						 0, ii.getImage());
		Editor.vsm.addGlyph(ni.replacementImage, Editor.mSpace);
		object.getGlyph().setVisible(false);
		object.getGlyphText().setVisible(false);
	    }
	}
	catch (Exception ex){
	    System.err.println("Problem fetching image at " + object.toString());
	}
    }

    static void unrender(ArcInfo ai){
	if (ai.originalLabel != null){
	    ai.owner.getGlyphText().setText(ai.originalLabel);
	}
    }

    static void unrender(NodeInfo ni){
	if (ni.replacementImage != null){
	    Editor.mSpace.destroyGlyph(ni.replacementImage);
	    ni.owner.getGlyph().setVisible(true);
	    ni.owner.getGlyphText().setVisible(true);
	}
    }

    public String toString(){
	return uri;
    }

    /* debugging */
//    void printDomains(){
// 	System.out.println("DOMAINS");
// 	if (basicPropertyDomains != null){
// 	    System.out.println("-------------------\nBasic selectors: instance domains\n-------------------");
// 	    for (int i=0;i<basicPropertyDomains.length;i++){
// 		System.out.println(basicPropertyDomains[i]);
// 	    }
// 	}
// 	if (fslPropertyDomains != null){
// 	    System.out.println("-------------------\nFSL selectors: instance domains\n-------------------");
// 	    for (int i=0;i<fslPropertyDomains.length;i++){
// 		System.out.println(fslPropertyDomains[i]);
// 	    }
// 	}
//     }

}