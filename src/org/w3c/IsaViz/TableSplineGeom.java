/*   FILE: TableSplineGeom.java
 *   DATE OF CREATION:   01/09/2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Jul 10 11:14:01 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import org.w3c.dom.Element;

import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.svg.SVGWriter;

/*remember parameters of a node (position and size)*/

class TableSplineGeom {

    String svgCoords;
    long vx,vy,width,height,tvx,tvy;

    TableSplineGeom(VPath p,long x,long y,long w,long h,long tx,long ty){
	svgCoords=(new SVGWriter()).getSVGPathCoordinates(p);  //spline
	vx=x;      //cell's x coord
	vy=y;      //cell's y coord
	width=w;   //cell's half width
	height=h;  //cell's half height
	tvx=tx;    //text's x coord
	tvy=ty;    //text's y coord
    }

}
