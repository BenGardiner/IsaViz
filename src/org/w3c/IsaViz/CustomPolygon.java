/*   FILE: CustomPolygon.java
 *   DATE OF CREATION:   Tue May 13 16:41:08 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Tue May 13 17:41:43 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

public class CustomPolygon {

    float[] vertices;
    
    public CustomPolygon(){
	vertices=new float[3];
	vertices[0]=1.0f;vertices[1]=1.0f;vertices[2]=1.0f;
    }

    public CustomPolygon(float[] v){
	vertices=v;
    }

    public void setVertices(float[] v){
	vertices=v;
    }

    public float[] getVertices(){
	return vertices;
    }
    
    public String toString(){
	String res="CustomPolygon: [";
	if (vertices!=null){
	    for (int i=0;i<vertices.length-1;i++){
		res+=Float.toString(vertices[i])+",";
	    }
	    res+=Float.toString(vertices[vertices.length-1]);
	}
	res+="] ";
	return res;
    }

}
