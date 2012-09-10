/*   FILE: StyleInfo.java
 *   DATE OF CREATION:   Tue Apr 01 14:25:37 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Aug 07 13:30:49 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */ 

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

import java.awt.Color;

abstract class StyleInfo {

    Color fill;
    Color stroke;

    Float strokeWidth;
    float[] strokeDashArray;

    String fontFamily;
    Integer fontSize;
    Short fontWeight;
    Short fontStyle;

    Integer visibility;

    Integer layout;

    float[] getStrokeDashArray(){
	if (strokeDashArray!=null){
	    return (strokeDashArray.length>0) ? strokeDashArray : null;
	}
	else return null;
    }

}
