/*   FILE: LitResizer.java
 *   DATE OF CREATION:   12/05/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jun 25 17:51:01 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 
 

package org.w3c.IsaViz;

import java.awt.Color;

import com.xerox.VTM.glyphs.RectangularShape;
import com.xerox.VTM.glyphs.RectangleNR;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VCircle;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.ClosedShape;
import com.xerox.VTM.engine.VirtualSpace;

/*Class that contains resizing handles (small black boxes) that are used to modify the geometry of a literal's glyph + methods to update*/

class LitResizer extends Resizer {

    Glyph g0;        //ILiteral's main shape
    RectangularShape g0rs; //cast of g0 as a RectangularShape if it implements this interface (null if not)
    VImage g0im;     //cast of g0 as a VImage (null if not a VImage)
    RectangleNR r1;  //East handle or single handle
    ClosedShape r2;  //North handle or bounding circle
    RectangleNR r3;  //West handle
    RectangleNR r4;  //South handle

    LitResizer(ILiteral l){
	g0=l.getGlyph();
	if (g0 instanceof RectangularShape){
	    if (g0 instanceof VImage){
		g0im=(VImage)g0;
		r1=new RectangleNR(g0.vx+g0im.getWidth(),g0.vy,0,4,4,Color.black);
		r2=new RectangleNR(g0.vx,g0.vy+g0im.getHeight(),0,4,4,Color.black);
		r3=new RectangleNR(g0.vx-g0im.getWidth(),g0.vy,0,4,4,Color.black);
		r4=new RectangleNR(g0.vx,g0.vy-g0im.getHeight(),0,4,4,Color.black);
	    }
	    else {
		g0rs=(RectangularShape)g0;
		r1=new RectangleNR(g0.vx+g0rs.getWidth(),g0.vy,0,4,4,Color.black);
		r2=new RectangleNR(g0.vx,g0.vy+g0rs.getHeight(),0,4,4,Color.black);
		r3=new RectangleNR(g0.vx-g0rs.getWidth(),g0.vy,0,4,4,Color.black);
		r4=new RectangleNR(g0.vx,g0.vy-g0rs.getHeight(),0,4,4,Color.black);
	    }
	    Editor.vsm.addGlyph(r1,Editor.mainVirtualSpace);r1.setType("rszl");  //ReSiZe Literal
	    Editor.vsm.addGlyph(r2,Editor.mainVirtualSpace);r2.setType("rszl");
	    Editor.vsm.addGlyph(r3,Editor.mainVirtualSpace);r3.setType("rszl");
	    Editor.vsm.addGlyph(r4,Editor.mainVirtualSpace);r4.setType("rszl");
	}
	else {
	    r1=new RectangleNR(g0.vx,Math.round(g0.vy+g0.getSize()),0,4,4,Color.black);
	    Editor.vsm.addGlyph(r1,Editor.mainVirtualSpace);r1.setType("rszl");
	    if (!(g0 instanceof VCircle)){
		r2=new VCircle(g0.vx,g0.vy,0,Math.round(g0.getSize()),Color.black);
		r2.setSensitivity(false);
		r2.setFilled(false);
		Editor.vsm.addGlyph(r2,Editor.mainVirtualSpace);
	    }
	}
    }

    void updateMainGlyph(Glyph g){//g should be a handle (small black box)
	if (g0rs!=null){//implements RectangularShape
	    if (g==r1){long newWidth=g.vx-g0.vx;if (newWidth>0){r1.vy=g0.vy;g0rs.setWidth(newWidth);r3.vx=g0.vx-g0rs.getWidth();}}
	    else if (g==r2){long newHeight=g.vy-g0.vy;if (newHeight>0){r2.vx=g0.vx;g0rs.setHeight(newHeight);r4.vy=g0.vy-g0rs.getHeight();}}
	    else if (g==r3){long newWidth=g0.vx-g.vx;if (newWidth>0){r3.vy=g0.vy;g0rs.setWidth(newWidth);r1.vx=g0.vx+g0rs.getWidth();}}
	    else if (g==r4){long newHeight=g0.vy-g.vy;if (newHeight>0){r4.vx=g0.vx;g0rs.setHeight(newHeight);r2.vy=g0.vy+g0rs.getHeight();}}
	}
	else if (g0im!=null){//implements RectangularShape and is a VImage
	    if (g==r1){
		long newWidth=g.vx-g0.vx;
		if (newWidth>0){
		    r1.vy=g0.vy;
		    g0im.setWidth(newWidth);
		    r3.vx=g0.vx-g0im.getWidth();
		    r2.vy=g0.vy+g0im.getHeight();
		    r4.vy=g0.vy-g0im.getHeight();
		}
	    }
	    else if (g==r2){
		long newHeight=g.vy-g0.vy;
		if (newHeight>0){
		    r2.vx=g0.vx;
		    g0im.setHeight(newHeight);
		    r4.vy=g0.vy-g0im.getHeight();
		    r1.vx=g0.vx+g0im.getWidth();
		    r3.vx=g0.vx-g0im.getWidth();
		}
	    }
	    else if (g==r3){
		long newWidth=g0.vx-g.vx;
		if (newWidth>0){
		    r3.vy=g0.vy;
		    g0im.setWidth(newWidth);
		    r1.vx=g0.vx+g0im.getWidth();
		    r2.vy=g0.vy+g0im.getHeight();
		    r4.vy=g0.vy-g0im.getHeight();
		}
	    }
	    else if (g==r4){
		long newHeight=g0.vy-g.vy;
		if (newHeight>0){
		    r4.vx=g0.vx;
		    g0im.setHeight(newHeight);
		    r2.vy=g0.vy+g0im.getHeight();
		    r1.vx=g0.vx+g0im.getWidth();
		    r3.vx=g0.vx-g0im.getWidth();
		}
	    }
	}
	else {
	    if (g==r1){
		r1.vx=g0.vx;
		long newSize=g.vy-g0.vy;
		if (newSize>0){
		    g0.sizeTo(newSize);
		    if (r2!=null){r2.sizeTo(newSize);}
		}
	    }
	}
    }

    void updateHandles(){
	if (g0rs!=null){//implements RectangularShape
	    r1.vx=g0.vx+g0rs.getWidth();r1.vy=g0.vy;
	    r2.vx=g0.vx;r2.vy=g0.vy+g0rs.getHeight();
	    r3.vx=g0.vx-g0rs.getWidth();r3.vy=g0.vy;
	    r4.vx=g0.vx;r4.vy=g0.vy-g0rs.getHeight();
	}
	else if (g0im!=null){//implements RectangularShape and is a VImage
	    r1.vx=g0.vx+g0im.getWidth();r1.vy=g0.vy;
	    r2.vx=g0.vx;r2.vy=g0.vy+g0im.getHeight();
	    r3.vx=g0.vx-g0im.getWidth();r3.vy=g0.vy;
	    r4.vx=g0.vx;r4.vy=g0.vy-g0im.getHeight();
	}
	else {
	    r1.vx=g0.vx;r1.vy=Math.round(g0.vy+g0.getSize());
	    if (r2!=null){
		r2.vx=g0.vx;
		r2.vy=g0.vy;
	    }
	}
    }

    void destroy(){
	VirtualSpace vs=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace);
	vs.destroyGlyph(r1);
	if (g0rs!=null || g0im!=null){vs.destroyGlyph(r2);vs.destroyGlyph(r3);vs.destroyGlyph(r4);}
	else if (r2!=null){vs.destroyGlyph(r2);}
    }

    Glyph getMainGlyph(){return g0;}

}
