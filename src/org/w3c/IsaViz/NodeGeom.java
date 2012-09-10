/*   FILE: NodeGeom.java
 *   DATE OF CREATION:   01/09/2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:53:10 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

/*remember parameters of a node (position and size)*/

class NodeGeom {

    long vx,vy,width,height,tvx,tvy;

    NodeGeom(long x,long y,long w,long h,long tx,long ty){
	vx=x;      //shape's x coord
	vy=y;      //shape's y coord
	width=w;   //shape's half width
	height=h;  //shape's half height
	tvx=tx;    //text's x coord
	tvy=ty;    //text's y coord
    }

}
