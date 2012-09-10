/*   FILE: SplineGeom.java
 *   DATE OF CREATION:   01/09/2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:57:10 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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

class SplineGeom {

    String svgCoords;
    long tvx,tvy,hvx,hvy;
    float hor;

    SplineGeom(VPath p,long tx,long ty,long hx,long hy,float hr){
	svgCoords=(new SVGWriter()).getSVGPathCoordinates(p);  //spline
	tvx=tx;    //text's x coord
	tvy=ty;    //text's y coord
	hvx=hx;    //arrow head's x coord
	hvy=hy;    //arrow head's y coord
	hor=hr;    //arrow head's orientation
    }

}
