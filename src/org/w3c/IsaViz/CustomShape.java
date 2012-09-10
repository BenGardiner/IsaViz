/*   FILE: CustomShape.java
 *   DATE OF CREATION:   Fri Mar 14 14:53:58 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Apr 18 10:53:23 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */ 

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

public class CustomShape {

    float[] vertices;
    Float orientation;
    
    public CustomShape(){
	vertices=new float[3];
	vertices[0]=1.0f;vertices[1]=1.0f;vertices[2]=1.0f;
	orientation=new Float(0);
    }

    public CustomShape(float[] v,Float o){
	vertices=v;
	orientation=o;
    }

    public void setVertices(float[] v){
	vertices=v;
    }

    public void setOrientation(Float o){
	orientation=o;
    }

    public float[] getVertices(){
	return vertices;
    }

    public Float getOrientation(){
	return orientation;
    }
    
    public String toString(){
	String res="CustomShape: [";
	if (vertices!=null){
	    for (int i=0;i<vertices.length-1;i++){
		res+=Float.toString(vertices[i])+",";
	    }
	    res+=Float.toString(vertices[vertices.length-1]);
	}
	res+="] ";
	if (orientation!=null){res+=orientation.toString();}
	return res;
    }

}
