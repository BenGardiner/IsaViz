/*   FILE: IProperty.java
 *   DATE OF CREATION:   10/18/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Aug 08 17:37:14 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.util.Vector;
import java.awt.BasicStroke;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.IsaViz.fresnel.*;

import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.VTriangleOr;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import net.claribole.zvtm.engine.PostAnimationAction;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFException;

/*Our internal model class for RDF Properties. Instances of this class are not property types, but predicates (instances of a property type). So there can be many IProperty with the same URI.*/ 

public class IProperty extends INode {

    int textIndex;

    IResource subject;
    INode object;

    private String namespace="";          //namespace+localname = URI
    private String localname="";

    //spline (points to the edge common to all properties if laid out in table form)
    VPath gl1;
    //arrow head (null if edge points to a table form)
    VTriangleOr gl2;
    //property label
    VText gl3;    //if no text has been entered yet, this glyph is null (use this test to find out if there is text)
    //cell for table form layout (null if laid out as edge/node)
    VRectangle gl4;

    String mapID;

    /**
     *@param rs Jena property representing this edge
     */
    public IProperty(Property p){
	strokeIndex=ConfigManager.defaultPBIndex;
	textIndex=ConfigManager.defaultPTIndex;
	namespace=p.getNameSpace();
	localname=p.getLocalName();
    }

    /**Create a new IProperty from scratch (information will be added later)*/
    public IProperty(){
	strokeIndex=ConfigManager.defaultPBIndex;
	textIndex=ConfigManager.defaultPTIndex;
    }

    void setNamespace(String n){namespace=n;}

    void setLocalname(String l){localname=l;}

    public String getIdent(){
	try {
	    String res=namespace+localname;
	    return (res.equals("nullnull")) ? null : res ;
	}
	catch (NullPointerException ex){return null;}
    }

    public String getNamespace(){
	return namespace;
    }

    public String getLocalname(){
	return localname;
    }

    public void setMapID(String s){mapID=s;}

    public String getMapID(){return mapID;}

    public void setSubject(IResource r){
	subject=r;
    }
    
    public IResource getSubject(){
	return subject;
    }

    public void setObject(IResource r){
	object=r;
    }
    
    public void setObject(ILiteral l){
	object=l;
    }
    
    /**can be an IResource or an ILiteral*/
    public INode getObject(){
	return object;
    }

    /**selects this node (and assigns colors to glyph and text)*/
    public void setSelected(boolean b,boolean selectTableCell){
	super.setSelected(b);
	if (this.isVisuallyRepresented()){
	    if (selected){
		gl1.setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
		if (gl2!=null){gl2.setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
		if (gl3!=null){gl3.setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);}
		if (selectTableCell && gl4!=null){
		    gl4.setHSVColor(ConfigManager.selFh,ConfigManager.selFs,ConfigManager.selFv);
		    gl4.setHSVbColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
		}
		//the stroke gets thicker (+2 w.r.t its original width)
		if (!table){//don't do it for table layout as the thickness gets increased as many times as the number of rows in the table
		    ConfigManager.makeGlyphStrokeThicker(gl1,2.0f);
		}
		displayOnTop();
	    }
	    else {
		if (commented){
		    gl1.setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
		    if (gl2!=null){gl2.setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
		    if (gl3!=null){gl3.setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);}
		    if (gl4!=null){
			gl4.setHSVColor(ConfigManager.comFh,ConfigManager.comFs,ConfigManager.comFv);
			gl4.setHSVbColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
		    }
		}
		else {
		    gl1.setColor(ConfigManager.colors[strokeIndex]);
		    if (gl2!=null){gl2.setColor(ConfigManager.colors[strokeIndex]);}
		    if (gl3!=null){gl3.setColor(ConfigManager.colors[textIndex]);}
		    if (gl4!=null){
			gl4.setColor(ConfigManager.colors[fillIndex]);
			gl4.setBorderColor(ConfigManager.colors[textIndex]);
		    }
		}
		//the stroke gets back to its original thickness (-2 w.r.t selected width)
		if (!table){//don't do it for table layout as the thickness gets decreased as many times as the number of rows in the table
		    ConfigManager.makeGlyphStrokeThicker(gl1,-2.0f);
		}
	    }
	}
    }

    public void gray(){
	gl1.setHSVColor(FresnelManager.grayTh,FresnelManager.grayTs,FresnelManager.grayTv);
	if (gl2!=null){gl2.setHSVColor(FresnelManager.grayTh,FresnelManager.grayTs,FresnelManager.grayTv);}
	if (gl3!=null){gl3.setHSVColor(FresnelManager.grayTh,FresnelManager.grayTs,FresnelManager.grayTv);}
	if (gl4!=null){
	    gl4.setHSVColor(FresnelManager.grayFh,FresnelManager.grayFs,FresnelManager.grayFv);
	    gl4.setHSVbColor(FresnelManager.grayTh,FresnelManager.grayTs,FresnelManager.grayTv);
	}
    }

    public void colorize(){
	gl1.setColor(ConfigManager.colors[strokeIndex]);
	if (gl2!=null){gl2.setColor(ConfigManager.colors[strokeIndex]);}
	if (gl3!=null){gl3.setColor(ConfigManager.colors[textIndex]);}
	if (gl4!=null){
	    gl4.setColor(ConfigManager.colors[fillIndex]);
	    gl4.setBorderColor(ConfigManager.colors[textIndex]);
	}
    }

    public void grayAnimated(long d){
	grayAnimated(d, false, false);
    }

    public void grayAnimated(long d, boolean grayArcItself, boolean appearingArc){
	float[] f1 = {FresnelManager.grayTh - ConfigManager.prpTh,
		      FresnelManager.grayTs - ConfigManager.prpTs,
		      FresnelManager.grayTv - ConfigManager.prpTv,
		      0,
		      0,
		      0};
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, f1, gl3.getID(), null);
	if (grayArcItself){
	    float[] f2 = {FresnelManager.grayTh - ConfigManager.prpBh,
			  FresnelManager.grayTs - ConfigManager.prpBs,
			  FresnelManager.grayTv - ConfigManager.prpBv,
			  0,
			  0,
			  0};
	    Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, f2, gl1.getID(), null);
	    float[] f3 = {FresnelManager.grayTh - ConfigManager.prpBh,
			  FresnelManager.grayTs - ConfigManager.prpBs,
			  FresnelManager.grayTv - ConfigManager.prpBv,
			  FresnelManager.grayTh - ConfigManager.prpBh,
			  FresnelManager.grayTs - ConfigManager.prpBs,
			  FresnelManager.grayTv - ConfigManager.prpBv};
	    Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, f3, gl2.getID(), null);
	}
 	if (appearingArc){
	    gl1.setVisible(true);
	    gl2.setVisible(true);
	    // 	    data = new Vector();
	    // 	    data.add(new Float(0));
	    // 	    data.add(new Float(0));
	    // 	    data.add(new Float(0));
	    // 	    data.add(new Float(0));
	    // 	    data.add(new Float(0));
	    // 	    data.add(new Float(0));
	    // 	    data.add(new Float(1.0f));
	    // 	    Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, data, gl2.getID(), null);
 	}
    }

    public void colorizeAnimated(long d){
	colorizeAnimated(d, false, false);
    }

    public void colorizeAnimated(long d, boolean colorizeArcItself, boolean disappearingArc){
	float[] f1 = {ConfigManager.prpTh - FresnelManager.grayTh,
		      ConfigManager.prpTs - FresnelManager.grayTs,
		      ConfigManager.prpTv - FresnelManager.grayTv,
		      0,
		      0,
		      0};
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, f1, gl3.getID(), null);
	if (colorizeArcItself){
	    // arc color
	    float[] f2 = {ConfigManager.prpBh - FresnelManager.grayTh,
			  ConfigManager.prpBs - FresnelManager.grayTs,
			  ConfigManager.prpBv - FresnelManager.grayTv,
			  0,
			  0,
			  0};
	    Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, f2, gl1.getID(), null);
	    float[] f3 = {ConfigManager.prpBh - FresnelManager.grayTh,
			  ConfigManager.prpBs - FresnelManager.grayTs,
			  ConfigManager.prpBv - FresnelManager.grayTv,
			  ConfigManager.prpBh - FresnelManager.grayTh,
			  ConfigManager.prpBs - FresnelManager.grayTs,
			  ConfigManager.prpBv - FresnelManager.grayTv};
	    Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, f3, gl2.getID(), null);
	}
	if (disappearingArc){
	    gl1.setVisible(false);
	    gl2.setVisible(false);
// 	    data = new Vector();
// 	    data.add(new Float(0));
// 	    data.add(new Float(0));
// 	    data.add(new Float(0));
// 	    data.add(new Float(0));
// 	    data.add(new Float(0));
// 	    data.add(new Float(0));
// 	    data.add(new Float(-1.0f));
// 	    Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_COLOR_LIN, data, gl2.getID(), null);
 	}
    }

    public void translate(ItemInfo ii, long d, long nx, long ny, PostAnimationAction paa){
	Editor.mSpace.onTop(gl3);
	ArcInfo ai = (ArcInfo)ii;
	long dx = nx - gl3.vx;
	long dy = ny - gl3.vy;
	LongPoint t = new LongPoint(dx, dy);
	Editor.vsm.animator.createGlyphAnimation(d, 0 , AnimManager.GL_TRANS_SIG, t, gl3.getID(), paa);
	ai.tl = new LongPoint(-dx, -dy);
    }

    public void showFresnelLabel(ArcInfo ai, String newLabel){
	ai.originalLabel = gl3.getText();
	gl3.setText(newLabel);
    }

    public void comment(boolean b, Editor e, boolean propagate){
	if (b){//comment
	    commented=b;
	    if (this.isVisuallyRepresented()){
		gray();
	    }
	}
	else {//uncomment
	    if (subject!=null){//do not uncomment predicate if either subject or object is still null
		if (object!=null){
		    if ((!subject.isCommented()) && (!object.isCommented())){
			commented=b;
			if (this.isVisuallyRepresented()){
			    colorize();
			}
		    }
		}
		else {
		    if (!subject.isCommented()){
			commented=b;
			if (this.isVisuallyRepresented()){
			    colorize();
			}
		    }
		}
	    }
	    else {
		if (object!=null){
		    if (!object.isCommented()){
			commented=b;
			if (this.isVisuallyRepresented()){
			    colorize();
			}
		    }
		}//else should never happen (a predicate alone cannot exist)
	    }
	}
    }

    public void setVisible(boolean b){
	if (gl1!=null){gl1.setVisible(b);gl1.setSensitivity(b);}
	if (gl2!=null){gl2.setVisible(b);gl2.setSensitivity(b);}
	if (gl3!=null){gl3.setVisible(b);gl3.setSensitivity(b);}
	if (gl4!=null){gl4.setVisible(b);gl4.setSensitivity(b);}
    }

    public void setGlyph(VPath p,VTriangleOr t){
	gl1=p;
	gl1.setType(Editor.propPathType);   //means predicate glyph (glyph type is a quick way to retrieve glyphs from VTM)
	gl1.setOwner(this);  
	gl2=t;
	if (gl2!=null){
	    gl2.setType(Editor.propHeadType);   //means predicate head (glyph type is a quick way to retrieve glyphs from VTM)
	    gl2.setOwner(this);
	    gl2.setDrawBorder(false);
	}
    }

    //called when initializing/destroying a resizer for this property's path - accepts null as input
    protected void setGlyphHead(VTriangleOr t){
	gl2=t;
	if (gl2!=null){
	    gl2.setDrawBorder(false);
	    gl2.setType(Editor.propHeadType);   //means predicate head (glyph type is a quick way to retrieve glyphs from VTM)
	    gl2.setOwner(this);
	}
    }

    public void setGlyphText(VText t){
	gl3=t;
	gl3.setType(Editor.propTextType);   //means predicate glyph (glyph type is a quick way to retrieve glyphs from VTM)
	gl3.setOwner(this);
    }

    public void setTableCellGlyph(VRectangle r){
	gl4=r;
	gl4.setType(Editor.propCellType);
	gl4.setOwner(this);
    }

    public Glyph getGlyph(){
	return gl1;
    }

    /*can be null*/
    public VTriangleOr getGlyphHead(){
	return gl2;
    }

    public VText getGlyphText(){
	return gl3;
    }

    /*can return null if not laid out in a table*/
    public VRectangle getTableCellGlyph(){
	return gl4;
    }
    
    public boolean isVisuallyRepresented(){
	//the entity might not be present in the graph (visual) if it has a visibility attribute set to GraphStylesheet._gssHide
	//- added gl4 for robustness, but the gl1 test should be sufficient
	//the test for isVisible() is necessary because INodes for which visibility=hidden do have glyphs associated with them, they are just not visible
	if ((this.gl1!=null && this.gl1.isVisible()) || (this.gl4!=null && this.gl4.isVisible())){return true;}
	else return false;
    }

   public Element toISV(Document d,ISVManager e,Vector fonts){
	Element res=d.createElementNS(Editor.isavizURI,"isv:iproperty");
	Element uri=d.createElementNS(Editor.isavizURI,"isv:uri");
	Element namespaceEL=d.createElementNS(Editor.isavizURI,"isv:namespace");
	namespaceEL.appendChild(d.createTextNode(namespace));
	Element localnameEL=d.createElementNS(Editor.isavizURI,"isv:localname");
	localnameEL.appendChild(d.createTextNode(localname));
	uri.appendChild(namespaceEL);uri.appendChild(localnameEL);
	res.appendChild(uri);
	if (this.isVisuallyRepresented()){//it might actually be worth to save the geom info when visibility=hidden (since it exists)
	    //for now, we do not save anything geom info, no matter whether display=none or visibility=hidden
	    res.setAttribute("display","true");
	    Element path=d.createElementNS(Editor.isavizURI,"isv:path");
	    if (gl3!=null){
		uri.setAttribute("x",String.valueOf(gl3.vx));
		uri.setAttribute("y",String.valueOf(gl3.vy));
		//save font
		int index=fonts.indexOf(gl3.getFont());
		if (index==-1){
		    fonts.add(gl3.getFont());
		    index=fonts.size()-1;
		}
		//do not save font info if font is default zvtm/graph font
		if (index!=0){res.setAttribute("font",String.valueOf(index));}
	    }
	    StringBuffer coords=new StringBuffer();
	    java.awt.geom.PathIterator pi=gl1.getJava2DPathIterator();
	    float[] seg=new float[6];
	    int type;
	    char lastOp='Z';  //anything but M, L, Q, C since we want the first command to explicitely appear in any case
	    while (!pi.isDone()){//save the path as a sequence of instructions following the SVG model for "d" attributes
		//we save it in SVG coordinates (not VTM) because we already have an SVG path interpreter built in VTM's SVGCreator
		type=pi.currentSegment(seg);
		switch (type){
		case java.awt.geom.PathIterator.SEG_MOVETO:{
		    if (lastOp!='M'){coords.append('M');} else {coords.append(' ');}
		    lastOp='M';
		    coords.append(Utils.abl2c(seg[0])+" "+Utils.abl2c(seg[1]));
		    break;
		}
		case java.awt.geom.PathIterator.SEG_LINETO:{
		    if (lastOp!='L'){coords.append('L');} else {coords.append(' ');}
		    lastOp='L';
		    coords.append(Utils.abl2c(seg[0])+" "+Utils.abl2c(seg[1]));
		    break;
		}
		case java.awt.geom.PathIterator.SEG_QUADTO:{
		    if (lastOp!='Q'){coords.append('Q');} else {coords.append(' ');}
		    lastOp='Q';
		    coords.append(Utils.abl2c(seg[0])+" "+Utils.abl2c(seg[1])+" "+Utils.abl2c(seg[2])+" "+Utils.abl2c(seg[3]));
		    break;
		}
		case java.awt.geom.PathIterator.SEG_CUBICTO:{
		    if (lastOp!='C'){coords.append('C');} else {coords.append(' ');}
		    lastOp='C';
		    coords.append(Utils.abl2c(seg[0])+" "+Utils.abl2c(seg[1])+" "+Utils.abl2c(seg[2])+" "+Utils.abl2c(seg[3])+" "+Utils.abl2c(seg[4])+" "+Utils.abl2c(seg[5]));
		    break;
		}
		}
		pi.next();
	    }
	    path.setAttribute("d",coords.toString());
	    res.appendChild(path);
	    if (table){
		res.setAttribute("table","true");//omitted if node-edge (but parser will understand table=false)
		res.setAttribute("fill",String.valueOf(fillIndex));
		res.setAttribute("tstroke",String.valueOf(textIndex));	
		if (gl4!=null){
		    Element cell=d.createElementNS(Editor.isavizURI,"isv:cell");
		    cell.setAttribute("x",String.valueOf(gl4.vx));
		    cell.setAttribute("y",String.valueOf(gl4.vy));
		    cell.setAttribute("w",String.valueOf(gl4.getWidth()));
		    cell.setAttribute("h",String.valueOf(gl4.getHeight()));
		    res.appendChild(cell);
		}
	    }
	    else {//arrow head exists only for node-edge props, not for table layout
		if (gl2!=null){
		    Element head=d.createElementNS(Editor.isavizURI,"isv:head");
		    head.setAttribute("x",String.valueOf(gl2.vx));
		    head.setAttribute("y",String.valueOf(gl2.vy));
		    head.setAttribute("w",Utils.abl2c(String.valueOf(gl2.getSize())));  //only this one will be saved/read (w=h)
		    head.setAttribute("or",String.valueOf(gl2.getOrient()));  
		    res.appendChild(head);
		}
	    }
	    res.setAttribute("stroke",String.valueOf(strokeIndex));
	    if (gl1.getStroke()!=null){
		if (gl1.getStroke().getLineWidth()!=Glyph.DEFAULT_STROKE_WIDTH){
		    res.setAttribute("stroke-width",String.valueOf(gl1.getStroke().getLineWidth()));
		}
		if (gl1.getStroke().getDashArray()!=null){
		    res.setAttribute("stroke-dasharray",Utils.arrayOffloatAsCSStrings(gl1.getStroke().getDashArray()));
		}
	    }
	    if (gl1.getStrokeWidth()!=1.0f){
		
	    }
	    if (this.getTextAlign()!=Style.TA_CENTER.intValue()){
		res.setAttribute("text-align",String.valueOf(this.getTextAlign()));
	    }
	}
	else {
	    res.setAttribute("display","false");
	}
	if (subject!=null){res.setAttribute("sb",e.getPrjId(subject));}
	if (object!=null){res.setAttribute("ob",e.getPrjId(object));}
	if (commented){res.setAttribute("commented","true");}  //do not put this attr if not commented (although commented="false" is supported by the ISV loader)
	return res;
    }
    
    public String toString(){return super.toString()+" "+getIdent();}

    //a meaningful string representation of this IProperty
    public String getText(){return getIdent()!=null ? getIdent() : "";}
    
    public void displayOnTop(){
	if (gl4!=null){Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).onTop(gl4);}
	Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).onTop(gl1);
	if (gl2!=null){Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).onTop(gl2);}
	if (gl3!=null){Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).onTop(gl3);}
    }

    public void setTextColor(int i){//index of color in ConfigManager.colors
	textIndex=i;
	if (gl3!=null){
	    gl3.setColor(ConfigManager.colors[textIndex]);
	}
	if (gl4!=null){
	    gl4.setBorderColor(ConfigManager.colors[textIndex]);
	}
    }

    public int getTextIndex(){return textIndex;}    

    public void setStrokeColor(int i){//index of color in ConfigManager.colors
	strokeIndex=i;
	gl1.setColor(ConfigManager.colors[strokeIndex]);
	if (gl2!=null){gl2.setColor(ConfigManager.colors[strokeIndex]);}
    }

    public int getStrokeIndex(){return strokeIndex;}

    public void setCellFillColor(int i){//index of color in ConfigManager.colors
	fillIndex=i;
	if (gl4!=null){gl4.setColor(ConfigManager.colors[fillIndex]);}
    }

    public int getCellFillIndex(){return fillIndex;}

    /*returns true if the VPath representing the edge for IProperty p is shared with at least one other IProperty (in table layout)*/
    public static boolean sharedPropertyArc(IProperty p){
	boolean res=false;
	if (p.getSubject()!=null){
	    Vector v=p.getSubject().getOutgoingPredicates();
	    if (v!=null){
		IProperty tmpP;
		for (int i=0;i<v.size();i++){
		    tmpP=(IProperty)v.elementAt(i);
		    if (tmpP.getGlyph()==p.getGlyph()){return true;}
		}
	    }
	}
	return res;
    }

    /*returns a vector containing all properties in the same table as p (but not p)*/
    public static Vector getAllPropertiesInSameTableAs(IProperty p){
	Vector res=new Vector();
	if (p.isLaidOutInTableForm() && p.getSubject()!=null){
	    Vector v=p.getSubject().getOutgoingPredicates();
	    if (v!=null){
		IProperty tmpP;
		for (int i=0;i<v.size();i++){
		    tmpP=(IProperty)v.elementAt(i);
		    if (tmpP.getGlyph()==p.getGlyph() && tmpP!=p){res.add(tmpP);}
		}
	    }
	}
	return res;
    }

    public static Vector getTableIncomingEdge(IProperty p){
	Vector res=null;
	if (p.getSubject()!=null){
	    Vector props=p.getSubject().getOutgoingPredicates();
	    if (props!=null){
		IProperty tmpP;
		for (int i=0;i<props.size();i++){
		    tmpP=(IProperty)props.elementAt(i);
		    if (tmpP.getGlyph()!=null && p.getGlyph()==tmpP.getGlyph()){//this test works, even for p being in the undo delete stack
			res=new Vector();//as deleted properties keep a ref to their VPath edge ; it is a little weird, and I am
			res.add(tmpP.getGlyph());//not sure this is really robust, but for now it works, and I don't know of any other 
			res.add(tmpP.getGlyphHead());//way to do that efficiently (and I like efficiency :-)
			return res;
		    }
		}
	    }
	}
	return res;
    }

}
