/*   FILE: ILiteral.java
 *   DATE OF CREATION:   10/18/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Aug 08 17:37:07 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.util.Hashtable;
import java.util.Vector;
import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.IsaViz.fresnel.*;

import com.xerox.VTM.glyphs.*;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import net.claribole.zvtm.engine.PostAnimationAction;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.datatypes.RDFDatatype;

/*Our internal model class for RDF Literals*/

public class ILiteral extends INode {
    
    private boolean escapeXML=true;
    private String language;
    private String value;
    private RDFDatatype datatype; //null if plain literal

    IProperty incomingPred;

    Glyph gl1;
    VText gl2;   //if not text has been entered yet, this glyph is null (use this test to find out if there is text)

    String mapID;

    /**
     *@param lt Jena literal representing this node
     */
    public ILiteral(Literal l){
	fillIndex=ConfigManager.defaultLFIndex;
	strokeIndex=ConfigManager.defaultLTBIndex;
	try {
	    escapeXML = l.isWellFormedXML();
	    //XXX: check whether the following is still true or not in Jena 2.3
	    // right now, Jena always say false - do not know why Bug? 
	    // anyway does not seem to have an impact on serialization, even if it is indeed well-formed XML
	    if (l.getLanguage().length()>0){language=l.getLanguage();}
	    datatype=l.getDatatype();
	    value=l.getLexicalForm();
	}
	catch (RDFException ex){System.err.println("Error: ILiteral(Literal - Jena): "+ex);}
    }

    /**Create a new ILiteral from scratch (information will be added later)*/
    public ILiteral(){
	fillIndex=ConfigManager.defaultLFIndex;
	strokeIndex=ConfigManager.defaultLTBIndex;
    }

    public void setLanguage(String l){language=l;}

    public String getLang(){return language;}
    
    public void setEscapeXMLChars(boolean b){escapeXML=b;}

    public boolean escapesXMLChars(){return escapeXML;}

    public void setValue(String v){value=v;}


//     /**returns the Jena literal*/
//     public Literal getJenaLiteral(){
// 	return l;
//     }
    
//     public void setJenaLiteral(Literal lit){
// 	l=lit;
//     }
    
    public String getValue(){
	return value;
    }

    //null for plain literal
    public void setDatatype(String uri){
	if (uri==null){datatype=null;}
	else {
	    if (uri.length()!=0 && !Utils.isWhiteSpaceCharsOnly(uri)){datatype=com.hp.hpl.jena.datatypes.TypeMapper.getInstance().getSafeTypeByName(uri);}
	    else {datatype=null;}
	}
    }

    //null for plain literal
    public void setDatatype(RDFDatatype dt){
	datatype=dt;
    }

    //null if plain literal
    public RDFDatatype getDatatype(){
	return datatype;
    }

    public void setMapID(String s){mapID=s;}

    public String getMapID(){return mapID;}

    public void setIncomingPredicate(IProperty p){
	incomingPred=p;
    }

    public IProperty getIncomingPredicate(){
	return incomingPred;
    }

    /**selects this node (and assigns colors to glyph and text)*/
    public void setSelected(boolean b){
	super.setSelected(b);
	if (this.isVisuallyRepresented()){
	    if (selected){
		gl1.setHSVColor(ConfigManager.selFh,ConfigManager.selFs,ConfigManager.selFv);
		gl1.setHSVbColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
		if (gl2!=null){gl2.setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
		VirtualSpace vs=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace);
		vs.onTop(gl1);vs.onTop(gl2);
	    }
	    else {
		if (commented){
		    gl1.setHSVColor(ConfigManager.comFh,ConfigManager.comFs,ConfigManager.comFv);
		    gl1.setHSVbColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
		    if (gl2!=null){gl2.setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
		}
		else {
		    gl1.setColor(ConfigManager.colors[fillIndex]);
		    gl1.setBorderColor(ConfigManager.colors[strokeIndex]);
		    if (gl2!=null){gl2.setColor(ConfigManager.colors[strokeIndex]);}
		}
	    }
	}
    }

    public void gray(){
	gl1.setHSVColor(FresnelManager.grayFh,FresnelManager.grayFs,FresnelManager.grayFv);
	gl1.setHSVbColor(FresnelManager.grayTh,FresnelManager.grayTs,FresnelManager.grayTv);
	if (gl2!=null){gl2.setHSVColor(FresnelManager.grayTh,FresnelManager.grayTs,FresnelManager.grayTv);}
    }

    public void colorize(){
	gl1.setColor(ConfigManager.colors[fillIndex]);
	gl1.setBorderColor(ConfigManager.colors[strokeIndex]);
	if (gl2!=null){gl2.setColor(ConfigManager.colors[strokeIndex]);}
    }

    public void grayAnimated(long d){
	// node fill and border color
	float[] fill = {FresnelManager.grayFh - ConfigManager.litFh,
			FresnelManager.grayFs - ConfigManager.litFs,
			FresnelManager.grayFv - ConfigManager.litFv,
			FresnelManager.grayTh - ConfigManager.litTBh,
			FresnelManager.grayTs - ConfigManager.litTBs,
			FresnelManager.grayTv - ConfigManager.litTBv};
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, fill, gl1.getID(), null);
	// text color
	float[] text = {FresnelManager.grayTh - ConfigManager.litTBh,
			FresnelManager.grayTs - ConfigManager.litTBs,
			FresnelManager.grayTv - ConfigManager.litTBv,
			0,
			0,
			0};
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, text, gl2.getID(), null);
    }

    public void colorizeAnimated(long d){
	// node fill and border color
	float[] fill = {ConfigManager.litFh - FresnelManager.grayFh,
			ConfigManager.litFs - FresnelManager.grayFs,
			ConfigManager.litFv - FresnelManager.grayFv,
			ConfigManager.litTBh - FresnelManager.grayTh,
			ConfigManager.litTBs - FresnelManager.grayTs,
			ConfigManager.litTBv - FresnelManager.grayTv};
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, fill, gl1.getID(), null);
	// text color
	float[] text = {ConfigManager.litTBh - FresnelManager.grayTh,
			ConfigManager.litTBs - FresnelManager.grayTs,
			ConfigManager.litTBv - FresnelManager.grayTv,
			0,
			0,
			0};
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, text, gl2.getID(), null);
    }

    public void translate(ItemInfo ii, long d, long nx, long ny, PostAnimationAction paa){
	Editor.mSpace.onTop(gl1);
	Editor.mSpace.onTop(gl2);
	NodeInfo ni = (NodeInfo)ii;
	long dx = nx - gl1.vx;
	long dy = ny - gl1.vy;
	LongPoint t = new LongPoint(dx, dy);
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_TRANS_SIG, t, gl1.getID(), paa);
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_TRANS_SIG, t, gl2.getID(), null);
	ni.sl = new LongPoint(-dx, -dy);
	ni.tl = ni.sl;
    }

    public void comment(boolean b, Editor e, boolean propagate){
	commented=b;
	if (commented){//comment
	    if (this.isVisuallyRepresented()){
		gray();
	    }
	    if (propagate && incomingPred!=null){
		e.commentPredicate(incomingPred,true, false);
	    }
	}
	else {//uncomment
	    if (this.isVisuallyRepresented()){
		colorize();
	    }
	    if (propagate && incomingPred!=null){
		e.commentPredicate(incomingPred,false, false);
	    }
	}
    }

    public void setVisible(boolean b){
	if (gl1!=null){gl1.setVisible(b);gl1.setSensitivity(b);}
	if (gl2!=null){gl2.setVisible(b);gl2.setSensitivity(b);}
    }

    public void setGlyph(Glyph r){
	gl1=r;
	gl1.setType(Editor.litShapeType);  //means literal glyph (glyph type is a quick way to retrieve glyphs from VTM)
	gl1.setOwner(this);
    }

    public void setGlyphText(VText t){
	gl2=t;
	if (gl2!=null){
	    gl2.setType(Editor.litTextType);  //means literal text (glyph type is a quick way to retrieve glyphs from VTM)
	    gl2.setOwner(this);
	}
    }

    public Glyph getGlyph(){
	return gl1;
    }

    public VText getGlyphText(){
	return gl2;
    }

    public Element toISV(Document d,ISVManager e,Hashtable bitmapImages,File prjFile,Vector fonts){
	Element res=d.createElementNS(Editor.isavizURI,"isv:iliteral");
	if (gl2!=null && getValue()!=null && getValue().length()>0){
	    Element val=d.createElementNS(Editor.isavizURI,"isv:value");
	    val.appendChild(d.createTextNode(getValue()));
	    val.setAttribute("x",String.valueOf(gl2.vx));
	    val.setAttribute("y",String.valueOf(gl2.vy));
	    res.appendChild(val);
	}
	if (!escapeXML){res.setAttribute("escapeXML","false");} //if value is not well-formed XML, signal it
	if (language!=null){
	    res.setAttribute("xml:lang",language);
	}
	if (datatype!=null){
	    res.setAttribute("dtURI",datatype.getURI());
	}
	if (this.isVisuallyRepresented()){
	    //it might actually be worth to save the geom info when visibility=hidden (since it exists)
	    //for now, we do not save anything geom info, no matter whether display=none or visibility=hidden
	    res.setAttribute("display","true");
	    if (table){res.setAttribute("table","true");}//omitted if node-edge (but parser will understand table=false)
	    res.setAttribute("x",String.valueOf(gl1.vx));
	    res.setAttribute("y",String.valueOf(gl1.vy));
	    res.setAttribute("fill",String.valueOf(fillIndex));
	    res.setAttribute("stroke",String.valueOf(strokeIndex));
	    if (gl1.getStroke()!=null){
		if (gl1.getStroke().getLineWidth()!=Glyph.DEFAULT_STROKE_WIDTH){
		    res.setAttribute("stroke-width",String.valueOf(gl1.getStroke().getLineWidth()));
		}
		if (gl1.getStroke().getDashArray()!=null){
		    res.setAttribute("stroke-dasharray",Utils.arrayOffloatAsCSStrings(gl1.getStroke().getDashArray()));
		}
	    }
	    if (this.getTextAlign()!=Style.TA_CENTER.intValue()){
		res.setAttribute("text-align",String.valueOf(this.getTextAlign()));
	    }
	    if (gl1 instanceof VEllipse){
		res.setAttribute("shape",Style.ELLIPSE.toString());
		res.setAttribute("w",String.valueOf(((RectangularShape)gl1).getWidth()));
		res.setAttribute("h",String.valueOf(((RectangularShape)gl1).getHeight()));
	    }
	    else if (gl1 instanceof VRoundRect){
		res.setAttribute("shape",Style.ROUND_RECTANGLE.toString());
		res.setAttribute("w",String.valueOf(((RectangularShape)gl1).getWidth()));
		res.setAttribute("h",String.valueOf(((RectangularShape)gl1).getHeight()));
	    }
	    else if (gl1 instanceof VImage){
		res.setAttribute("shape","icon");
		//here we must save the bitmap icon using a mechanism close to what we have for SVG export in the ZVTM
		File bitmapFile=Utils.exportBitmap((VImage)gl1,prjFile,bitmapImages);
		/*relative URI as the png files are supposed
		  to be in img_subdir w.r.t the SVG file*/
		if (bitmapFile!=null){
		    res.setAttribute("shape","icon");
		    res.setAttributeNS(com.xerox.VTM.svg.SVGWriter.xlinkURI,"xlink:href",ISVManager.img_subdir.getName()+"/"+bitmapFile.getName());
		}
		else {//if the bitmap export process fails in any way, replace it by a standard ellipse
		    res.setAttribute("shape",Style.RECTANGLE.toString());
		}
		res.setAttribute("w",String.valueOf(((RectangularShape)gl1).getWidth()));
		res.setAttribute("h",String.valueOf(((RectangularShape)gl1).getHeight()));
	    }
	    else if (gl1 instanceof VPolygon){
		res.setAttribute("shape","["+((VPolygon)gl1).getVerticesAsText()+"]");
	    }
	    else if (gl1 instanceof VShape){
		res.setAttribute("shape","{"+((VShape)gl1).getVerticesAsText()+"}");
		res.setAttribute("sz",String.valueOf(gl1.getSize()));
		res.setAttribute("or",String.valueOf(gl1.getOrient()));
	    }
	    else if (gl1 instanceof VCircle){
		res.setAttribute("shape",Style.CIRCLE.toString());
		res.setAttribute("sz",String.valueOf(gl1.getSize()));
	    }
	    else if (gl1 instanceof VDiamond){
		res.setAttribute("shape",Style.DIAMOND.toString());
		res.setAttribute("sz",String.valueOf(gl1.getSize()));
	    }
	    else if (gl1 instanceof VOctagon){
		res.setAttribute("shape",Style.OCTAGON.toString());
		res.setAttribute("sz",String.valueOf(gl1.getSize()));
	    }
	    else if (gl1 instanceof VTriangle){
		if (gl1.getOrient()==(float)Math.PI){
		    res.setAttribute("shape",Style.TRIANGLES.toString());
		}
		else if (gl1.getOrient()==(float)-Math.PI/2.0f){
		    res.setAttribute("shape",Style.TRIANGLEE.toString());
		}
		else if (gl1.getOrient()==(float)Math.PI/2.0f){
		    res.setAttribute("shape",Style.TRIANGLEW.toString());
		}
		else {
		    res.setAttribute("shape",Style.TRIANGLEN.toString());
		}
		res.setAttribute("sz",String.valueOf(gl1.getSize()));
	    }
	    else if (gl1 instanceof VRectangle){
		res.setAttribute("shape",Style.RECTANGLE.toString());
		res.setAttribute("w",String.valueOf(((RectangularShape)gl1).getWidth()));
		res.setAttribute("h",String.valueOf(((RectangularShape)gl1).getHeight()));
	    }
	    else {//for robustness
		res.setAttribute("shape",Style.ELLIPSE.toString());
		res.setAttribute("w",String.valueOf(((RectangularShape)gl1).getWidth()));
		res.setAttribute("h",String.valueOf(((RectangularShape)gl1).getHeight()));
	    }
	    //save font
	    if (gl2!=null){
		int index=fonts.indexOf(gl2.getFont());
		if (index==-1){
		    fonts.add(gl2.getFont());
		    index=fonts.size()-1;
		}
		//do not save font info if font is default zvtm/graph font
		if (index!=0){res.setAttribute("font",String.valueOf(index));}
	    }
	}
	else {
	    res.setAttribute("display","false");
	}
	if (commented){res.setAttribute("commented","true");}  //do not put this attr if not commented (although commented="false" is supported by the ISV loader)
	res.setAttribute("id",e.getPrjId(this));
	return res;
    }

    public String toString(){
	String res=super.toString();
	if (getValue()!=null){res+=" "+getValue();}
	if (getDatatype()!=null){res+=" ["+getDatatype().getURI()+"]";}
	return res;
    }

    //a possibly truncated version of this ILiteral's value
    public String getText(){
	if (value!=null){return (value.length()>=Editor.MAX_LIT_CHAR_COUNT) ? value.substring(0,Editor.MAX_LIT_CHAR_COUNT) : value;}
	else return "";
    }

    public void displayOnTop(){
	Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).onTop(gl1);
	if (gl2!=null){Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).onTop(gl2);}
    }

    public void setFillColor(int i){//index of color in ConfigManager.colors
	fillIndex=i;
	gl1.setColor(ConfigManager.colors[fillIndex]);
    }

    public int getFillIndex(){return fillIndex;}
    
    public void setStrokeColor(int i){//index of color in ConfigManager.colors
	strokeIndex=i;
	gl1.setBorderColor(ConfigManager.colors[strokeIndex]);
	if (gl2!=null){gl2.setColor(ConfigManager.colors[strokeIndex]);}
    }

    public int getStrokeIndex(){return strokeIndex;}
    
}
