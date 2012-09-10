/*   FILE: Style.java
 *   DATE OF CREATION:   Fri Feb 28 10:09:59 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Aug 07 13:33:57 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */ 

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

import java.awt.Color;
import java.util.StringTokenizer;
import com.xerox.VTM.svg.SVGReader;

public class Style {

 
    public static short CSS_FONT_WEIGHT_NORMAL=0;
    public static short CSS_FONT_WEIGHT_BOLD=1;
    public static short CSS_FONT_WEIGHT_BOLDER=2;
    public static short CSS_FONT_WEIGHT_LIGHTER=3;
    public static short CSS_FONT_WEIGHT_100=4;
    public static short CSS_FONT_WEIGHT_200=5;
    public static short CSS_FONT_WEIGHT_300=6;
    public static short CSS_FONT_WEIGHT_400=0;  //because according to the CSS spec normal=400
    public static short CSS_FONT_WEIGHT_500=7;
    public static short CSS_FONT_WEIGHT_600=8;
    public static short CSS_FONT_WEIGHT_700=1;  //because according to the CSS spec bold=700
    public static short CSS_FONT_WEIGHT_800=9;
    public static short CSS_FONT_WEIGHT_900=10;

    public static short CSS_FONT_STYLE_NORMAL=0;
    public static short CSS_FONT_STYLE_ITALIC=1;
    public static short CSS_FONT_STYLE_OBLIQUE=2;

    String styleID;

    /*interior color of the node shape representing the literal (can be null if undefined)*/
    protected Color fill;

    /*color of the spline representing the property (can be null if undefined)*/
    protected Color stroke;

    /*thickness of the spline representing the property (>=0, 0=no border) (can be null if undefined)*/
    protected Float strokeWidth;

    protected float[] strokeDashArray;
    
    /*font family  (we should accept CSS families and multiple choices (fallback): serif, sans-serif, cursive, monospace, fantasy  -not done yet) (can be null if undefined)*/
    protected String fontFamily;
    /*font size (we should accept all CSS sizes : length, percentage, etc.  -not done yet) (can be null if undefined)*/
    protected Integer fontSize;
    /*font weight  (can be normal or bold - we do not support bolder and lighter - we accept values 100 to 900, but this has no real impact: 100 to 400 will be equivalent to normal, 500 to 900 to bold) (can be null if undefined)*/
    protected Short fontWeight;
    /*font style  (can be normal, italic, oblique, knowing that for now we treat oblique as italic) (can be null if undefined)*/
    protected Short fontStyle;

    public static final Integer ELLIPSE=new Integer(0);
    public static final Integer RECTANGLE=new Integer(1);
    public static final Integer CIRCLE=new Integer(2);
    public static final Integer DIAMOND=new Integer(3);
    public static final Integer OCTAGON=new Integer(4);
    public static final Integer TRIANGLEN=new Integer(5);
    public static final Integer TRIANGLES=new Integer(6);
    public static final Integer TRIANGLEE=new Integer(7);
    public static final Integer TRIANGLEW=new Integer(8);
    public static final Integer CUSTOM_SHAPE=new Integer(9);
    public static final Integer CUSTOM_POLYGON=new Integer(10);
    public static final Integer ROUND_RECTANGLE=new Integer(11);

    /*text alignement*/
    public static Integer TA_CENTER=new Integer(0);
    public static Integer TA_LEFT=new Integer(1);
    public static Integer TA_RIGHT=new Integer(2);
    public static Integer TA_ABOVE=new Integer(3);
    public static Integer TA_BELOW=new Integer(4);

    /*relative size of node (can be null if undefined)*/
    protected Float rel_size;

    /*node shape (can be null if undefined)*/
    protected Integer shape;
    /*custom shape (can be null if undefined)*/
    protected float[] vertices;
    /*shape orientation (can be null if undefined) - only for custom shapes and polygons*/
    protected Float orientation;

    /*image icon (can be null if undefined)*/
    protected java.net.URL iconURL;

    /*text alignment (can be null if undefined)*/
    protected Integer text_align;

    /*Constructor*/
    public Style(String id){
	styleID=id;
	//do nothing else at init, as unset values should be null
    }

    /*shoud be a String representing the color in one of the formats allowed by SVG 1.0 or a java.awt.Color*/
    public void setFill(Object color){
	if (color!=null){
	    if (color instanceof String){
		fill=SVGReader.getColor((String)color);
	    }
	    else if (color instanceof Color){
		fill=(Color)color;
	    }
	}
	else {fill=null;}
    }

    Color getFill(){
	return fill;
    }

    /*shoud be a String representing the color in one of the formats allowed by SVG 1.0 or a java.awt.Color*/
    public void setStroke(Object color){
	if (color!=null){
	    if (color instanceof String){
		stroke=SVGReader.getColor((String)color);
	    }
	    else if (color instanceof Color){
		stroke=(Color)color;
	    }
	}
	else {stroke=null;}
    }

    public Color getStroke(){
	return stroke;
    }

    public void setStrokeWidth(String width){
	if (width!=null && width.length()>0){
	    if (width.endsWith("px")){
		width=width.substring(0,width.length()-2);
	    }
	    try {
		strokeWidth=new Float(width);
	    }
	    catch (NumberFormatException ex){strokeWidth=null;}
	}
	else {
	    strokeWidth=null;
	}
    }

    public Float getStrokeWidth(){
	return strokeWidth;
    }

    public void setStrokeDashArray(String dashArray){
	if (dashArray.equals(GraphStylesheet._gssDADashed)){
	    strokeDashArray=new float[2];
	    strokeDashArray[0]=10.0f;
	    strokeDashArray[1]=20.0f;
	}
	else if (dashArray.equals(GraphStylesheet._gssDADotted)){
	    strokeDashArray=new float[2];
	    strokeDashArray[0]=1.0f;
	    strokeDashArray[1]=5.0f;
	}
	else if (dashArray.equals(GraphStylesheet._gssDASolid)){
	    strokeDashArray=new float[0];
	}
	else {
	    StringTokenizer st=new StringTokenizer(dashArray,",");
	    strokeDashArray=new float[st.countTokens()];
	    int i=0;
	    String s=null;
	    while (st.hasMoreTokens()){
		try {
		    s=st.nextToken();
		    if (s.endsWith("px")){s=s.substring(0,s.length()-2);}
		    strokeDashArray[i++]=Float.parseFloat(s);
		}
		catch (NumberFormatException ex){
		    strokeDashArray[i-1]=1.0f;
		    System.err.println("Style: Error whie parsing a stroke dash array: "+s+" is not a positive float value");
		}
	    }
	    //check the array
	    if (strokeDashArray.length==0){strokeDashArray=null;}
	    else {
		boolean nonZero=false;
		for (int j=0;j<strokeDashArray.length;j++){
		    if (strokeDashArray[j]!=0.0f){nonZero=true;break;}
		}
		if (!nonZero){strokeDashArray=null;}
	    }
	}
    }

    //can be null if not set
    public float[] getStrokeDashArray(){
	return strokeDashArray;
    }

    public void setFontFamily(String family){
	if (family!=null && family.length()>0){fontFamily=family;}
	else {fontFamily=null;}
    }

    public String getFontFamily(){
	return fontFamily;
    }

    public void setFontSize(String size){
	if (size!=null && size.length()>0){
	    if (size.endsWith("pt")){
		size=size.substring(0,size.length()-2);
	    }
	    try {
		fontSize=new Integer(size);
	    }
	    catch (NumberFormatException ex){fontSize=null;}
	}
	else {
	    fontSize=null;
	}
    }

    public Integer getFontSize(){
	return fontSize;
    }

    public void setFontWeight(String weight){
	if (weight!=null && weight.length()>0){
	    String lc=weight.toLowerCase();
	    if (lc.equals("normal") || lc.equals("400")){fontWeight=new Short(CSS_FONT_WEIGHT_NORMAL);}
	    else if (lc.equals("bold") || lc.equals("700")){fontWeight=new Short(CSS_FONT_WEIGHT_BOLD);}
	    else if (lc.equals("bolder")){fontWeight=new Short(CSS_FONT_WEIGHT_BOLDER);}
	    else if (lc.equals("lighter")){fontWeight=new Short(CSS_FONT_WEIGHT_LIGHTER);}
	    else if (lc.equals("100")){fontWeight=new Short(CSS_FONT_WEIGHT_100);}
	    else if (lc.equals("200")){fontWeight=new Short(CSS_FONT_WEIGHT_200);}
	    else if (lc.equals("300")){fontWeight=new Short(CSS_FONT_WEIGHT_300);}
	    else if (lc.equals("500")){fontWeight=new Short(CSS_FONT_WEIGHT_500);}
	    else if (lc.equals("600")){fontWeight=new Short(CSS_FONT_WEIGHT_600);}
	    else if (lc.equals("800")){fontWeight=new Short(CSS_FONT_WEIGHT_800);}
	    else if (lc.equals("900")){fontWeight=new Short(CSS_FONT_WEIGHT_900);}
	    else {fontWeight=null;}
	}
	else {fontWeight=null;}
    }

    /*returns one of Style.CSS_FONT_WEIGHT (as a Short, not a short)*/
    public Short getFontWeight(){
	return fontWeight;
    }

    public void setFontStyle(String style){
	if (style!=null && style.length()>0){
	    String lc=style.toLowerCase();
	    if (lc.equals("normal")){fontStyle=new Short(CSS_FONT_STYLE_NORMAL);}
	    else if (lc.equals("italic")){fontStyle=new Short(CSS_FONT_STYLE_ITALIC);}
	    else if (lc.equals("oblique")){fontStyle=new Short(CSS_FONT_STYLE_OBLIQUE);}
	    else {fontStyle=null;}
	}
	else {fontStyle=null;}
    }

    /*returns one of Style.CSS_FONT_STYLE (as a Short, not a short)*/
    public Short getFontStyle(){
	return fontStyle;
    }

    public void setTextAlignment(String a){
	if (a.equals(GraphStylesheet._gssTACenter)){text_align=TA_CENTER;}
	else if (a.equals(GraphStylesheet._gssTABelow)){text_align=TA_BELOW;}
	else if (a.equals(GraphStylesheet._gssTAAbove)){text_align=TA_ABOVE;}
	else if (a.equals(GraphStylesheet._gssTALeft)){text_align=TA_LEFT;}
	else if (a.equals(GraphStylesheet._gssTARight)){text_align=TA_RIGHT;}
    }

    /*returns one of Style.TA_* */
    public Integer getTextAlignment(){
	return text_align;
    }

    public void setRelativeSize(String s){
	try {
	    rel_size = Float.parseFloat(s) / 100.0f;
	}
	catch (NumberFormatException ex){rel_size = null;}
    }

    /*returns the node's relative size as a float representing a percentage (divided by 100) of the standard size*/
    public Float getRelativeSize(){
	return rel_size;
    }

    public void setPredefShape(String s){
	if (s.equals(GraphStylesheet._gssEllipse)){shape=ELLIPSE;}
	else if (s.equals(GraphStylesheet._gssRectangle)){shape=RECTANGLE;}
	else if (s.equals(GraphStylesheet._gssRoundRect)){shape=ROUND_RECTANGLE;}
	else if (s.equals(GraphStylesheet._gssCircle)){shape=CIRCLE;}
	else if (s.equals(GraphStylesheet._gssDiamond)){shape=DIAMOND;}
	else if (s.equals(GraphStylesheet._gssOctagon)){shape=OCTAGON;}
	else if (s.equals(GraphStylesheet._gssTriangleN)){shape=TRIANGLEN;}
	else if (s.equals(GraphStylesheet._gssTriangleS)){shape=TRIANGLES;}
	else if (s.equals(GraphStylesheet._gssTriangleE)){shape=TRIANGLEE;}
	else if (s.equals(GraphStylesheet._gssTriangleW)){shape=TRIANGLEW;}
    }

    public void setCustomShape(String s){//s has format '[' (float ',' )+ ']' float   
	//the floats inside [] are in range [0.0,1.0], the last one is in range [0,2*Pi]
	//vertices
	vertices=parseCustomShape(s);
	if (vertices!=null){
	    shape=CUSTOM_SHAPE;
	    //orientation
	    String orient=null;
	    if (s.lastIndexOf("]")<s.length()-1){//there might be an orientation value
		orient=s.substring(s.lastIndexOf("]")+1);
		if (!Utils.isWhiteSpaceCharsOnly(orient)){
		    try {orientation=new Float(orient);}
		    catch (NumberFormatException ex){
			/*if orientation not well-formed, just ignore it, but issue an error*/
			System.err.println("Error:Style.setCustomShape: "+orient+" in "+s+" is not a well formed orientation value");
		    }
		}
	    }
	}
	else {shape=RECTANGLE;}
    }

    public void setCustomPolygon(String s){//s has format '{' (float ',' float) (';' float ',' float)* '}'
	//the floats inside {} are in range [-inf,+inf]
	//vertices
	vertices=parseCustomPolygon(s);
	if (vertices!=null){
	    shape=CUSTOM_POLYGON;
	}
	else {
	    shape=RECTANGLE;
	}
    }

    Integer getShape(){
	return shape;
    }

    float[] getVertexList(){
	return vertices;
    }

    Float getShapeOrient(){
	return orientation;
    }

    void setIcon(java.net.URL u){
	iconURL=u;
    }

    java.net.URL getIcon(){
	return iconURL;
    }
    
    public String toString(){
	String res="";
	res+="ID="+styleID+"\n";
	if (fill!=null){res+="\tfill="+fill.toString()+"\n";}
	if (stroke!=null){res+="\tstroke="+stroke.toString()+"\n";}
	if (strokeWidth!=null){res+="\tstroke width="+strokeWidth.toString()+"\n";}
	if (fontFamily!=null){res+="\tfont family="+fontFamily+"\n";}
	if (fontWeight!=null){res+="\tfont weight="+fontWeight.toString()+"\n";}
	if (fontStyle!=null){res+="\tfont style="+fontStyle.toString()+"\n";}
	if (fontSize!=null){res+="\tfont size="+fontSize.toString()+"\n";}
	if (shape!=null){res+="\tshape="+shape.toString();}
	if (iconURL!=null){res+="\ticon="+iconURL.toString();}
	if (vertices!=null){res+="\tvertices "+Utils.arrayOffloatAsCSStrings(vertices)+"\n";}
	if (text_align!=null){res+="\ttext-align "+text_align.toString();}
	if (rel_size != null){res += "\tsize " + rel_size.toString();}
	return res;
    }

    public static float[] parseCustomShape(String s){//s has format '[' (float ',' )+ ']' float   
	float[] far=null;
	if (s.indexOf("[")!=-1 && s.indexOf("]")!=-1){
	    String vertexList=s.substring(s.indexOf("[")+1,s.indexOf("]"));
	    StringTokenizer st=new StringTokenizer(vertexList,",");
	    int nbVertex=st.countTokens();
	    if (nbVertex>=3){
		far=new float[nbVertex];
		int i=0;
		try {
		    while (st.hasMoreTokens()){
			far[i]=Float.parseFloat(st.nextToken());
			if ((far[i]<0.0f) || (far[i]>1.0f)){
			    /*if a vertex value is not in range [0.0,1.0], stop everything and issue error - no shape is defined*/
			    System.err.println("Error:Style.parseCustomShape: "+far[i]+" is not in range [0.0,1.0] in "+s);
			    break;
			}
			i++;
		    }
		}
		catch (NumberFormatException ex){
		    /*if a vertex value is not well-formed, stop everything and issue error - no shape is defined*/
		    System.err.println("Error:Style.parseCustomShape: bad float format in "+vertexList);
		}
	    }
	    else {
		System.err.println("Error:Style.parseCustomShape: "+nbVertex+" is inferior to the minimum amount of vertices required for a shape (3)");
	    }
	}
	return far;
    }
    
    /*s has format '{' (float ',' float) (';' float ',' float)* '}'*/
    public static float[] parseCustomPolygon(String s){
        float[] far=null;
        if (s.indexOf("{")!=-1 && s.indexOf("}")!=-1){
            String vertexList=s.substring(s.indexOf("{")+1,s.indexOf("}"));
	    StringTokenizer st=new StringTokenizer(vertexList,";");
	    int nbVertex=st.countTokens();
	    if (nbVertex>=3){
		far=new float[nbVertex*2];  // *2 because we have to store 2 coordinates for each vertex
		int i=0;
		String pair,x,y;
		int commaIndex;
		try {
		    while (st.hasMoreTokens()){
			pair=st.nextToken();
			commaIndex=pair.indexOf(",");
			if (commaIndex>0){//check that there is indeed a comma separating x and y, and that x exists (there is sometinhg before the comma)
			    x=pair.substring(0,commaIndex);
			    y=pair.substring(commaIndex+1,pair.length());
			    far[i]=Float.parseFloat(x);
			    far[i+1]=Float.parseFloat(y);
			    i+=2;
			}
		    }
		}
		catch (NumberFormatException ex){
		    /*if a vertex value is not well-formed, stop everything and issue error - no shape is defined*/
		    System.err.println("Error:Style.parseCustomPolygon: bad float format in "+vertexList);
		}
            }
	    else {
		System.err.println("Error:Style.parseCustomPolygon: "+nbVertex+" is inferior to the minimum amount of vertices required for a polygon (3)");
	    }
        }
	return far;
    }

}
