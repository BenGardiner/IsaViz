/*   FILE: ISVGeom.java
 *   DATE OF CREATION:   01/09/2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Jul 10 11:23:17 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.RectangularShape;
import com.xerox.VTM.glyphs.VPath;

/*ISV command: geometry (move or resize)*/

class ISVGeom extends ISVCommand {

    Editor application;
    Hashtable rl;   //list of IResource + their old position/size
    Hashtable ll;   //list of ILiteral + their old position/size
    Hashtable pl;   //list of IProperty + their old position/size

    ISVGeom(Editor e,Vector props,Vector ress,Vector lits){
	application=e;
	rl=new Hashtable();
	ll=new Hashtable();
	pl=new Hashtable();
	Glyph g1;
	Glyph g2;
	IResource r;
	for (int i=0;i<ress.size();i++){
	    r=(IResource)ress.elementAt(i);
	    g1=r.getGlyph();
	    g2=r.getGlyphText();
	    if (g1 instanceof RectangularShape){
		rl.put(r,new NodeGeom(g1.vx,g1.vy,((RectangularShape)g1).getWidth(),((RectangularShape)g1).getHeight(),g2.vx,g2.vy));
	    }
	    else {
		rl.put(r,new NodeGeom(g1.vx,g1.vy,Math.round(g1.getSize()),Math.round(g1.getSize()),g2.vx,g2.vy));
	    }
	}
	ILiteral l;
	for (int i=0;i<lits.size();i++){
	    l=(ILiteral)lits.elementAt(i);
	    g1=l.getGlyph();
	    g2=l.getGlyphText();
	    if (g2!=null){
		if (g1 instanceof RectangularShape){
		    ll.put(l,new NodeGeom(g1.vx,g1.vy,((RectangularShape)g1).getWidth(),((RectangularShape)g1).getHeight(),g2.vx,g2.vy));
		}
		else {
		    ll.put(l,new NodeGeom(g1.vx,g1.vy,Math.round(g1.getSize()),Math.round(g1.getSize()),g2.vx,g2.vy));
		}
	    }

	    else {
		if (g1 instanceof RectangularShape){
		    ll.put(l,new NodeGeom(g1.vx,g1.vy,((RectangularShape)g1).getWidth(),((RectangularShape)g1).getHeight(),0,0));
		}
		else {
		    ll.put(l,new NodeGeom(g1.vx,g1.vy,Math.round(g1.getSize()),Math.round(g1.getSize()),0,0));
		}
	    }
	}
	IProperty p;
	Glyph g3;
	for (int i=0;i<props.size();i++){
	    p=(IProperty)props.elementAt(i);
	    g1=p.getGlyph();
	    g2=p.getGlyphText();
	    if (p.isLaidOutInTableForm()){//remember the cell's geom info
		g3=p.getTableCellGlyph();
		if (g3!=null){
		    pl.put(p,new TableSplineGeom((VPath)g1,g3.vx,g3.vy,((RectangularShape)g3).getWidth(),((RectangularShape)g3).getHeight(),g2.vx,g2.vy));
		}
	    }
	    else {//remember the arrow head geom info
		g3=p.getGlyphHead();
		if (g3!=null){pl.put(p,new SplineGeom((VPath)g1,g2.vx,g2.vy,g3.vx,g3.vy,g3.getOrient()));}
		else {pl.put(p,new SplineGeom((VPath)g1,g2.vx,g2.vy,0,0,0));}
	    }
	}
    }

    /*there is nothing to do. Everything is done by the user directly moving 
      stuff on screen. We use a command because we want to be able to undo.
      The command is here to store the state of objects modified by the moving/resizing
      before it happens. Undo will restore objects as they were.
    */

    void _do(){}

    void _undo(){
	IResource r;
	NodeGeom ng;
	Glyph g1,g2;
	for (Enumeration e=rl.keys();e.hasMoreElements();){
	    r=(IResource)e.nextElement();
	    g1=r.getGlyph();
	    g2=r.getGlyphText();
	    ng=(NodeGeom)rl.get(r);
	    g1.moveTo(ng.vx,ng.vy);
	    if (g1 instanceof RectangularShape){
		((RectangularShape)g1).setWidth(ng.width);
		((RectangularShape)g1).setHeight(ng.height);
	    }
	    else {
		g1.sizeTo(ng.width);
	    }
	    g2.vx=ng.tvx;
	    g2.vy=ng.tvy;
	}
	ILiteral l;
	for (Enumeration e=ll.keys();e.hasMoreElements();){
	    l=(ILiteral)e.nextElement();
	    g1=l.getGlyph();
	    g2=l.getGlyphText();
	    ng=(NodeGeom)ll.get(l);
	    g1.moveTo(ng.vx,ng.vy);
	    if (g1 instanceof RectangularShape){
		((RectangularShape)g1).setWidth(ng.width);
		((RectangularShape)g1).setHeight(ng.height);
	    }
	    else {
		g1.sizeTo(ng.width);
	    }
	    if (g2!=null){
		g2.vx=ng.tvx;
		g2.vy=ng.tvy;
	    }
	}
	IProperty p;
	Glyph g3;
	for (Enumeration e=pl.keys();e.hasMoreElements();){
	    p=(IProperty)e.nextElement();
	    g1=p.getGlyph();
	    g2=p.getGlyphText();
	    if (p.isLaidOutInTableForm()){
		TableSplineGeom sg=(TableSplineGeom)pl.get(p);
		((VPath)g1).setSVGPath(sg.svgCoords);
		g3=p.getTableCellGlyph();
		if (g3!=null){
		    g3.moveTo(sg.vx,sg.vy);
		    ((RectangularShape)g3).setWidth(sg.width);
		    ((RectangularShape)g3).setHeight(sg.height);
		}
		g2.vx=sg.tvx;
		g2.vy=sg.tvy;
	    }
	    else {
		SplineGeom sg=(SplineGeom)pl.get(p);
		((VPath)g1).setSVGPath(sg.svgCoords);
		g2.moveTo(sg.tvx,sg.tvy);
		g3=p.getGlyphHead();
		if (g3!=null){
		    g3.moveTo(sg.hvx,sg.hvy);
		    g3.orientTo(sg.hor);
		}
	    }
	}
	if (application.geomMngr.lastResizer!=null){application.geomMngr.lastResizer.updateHandles();}
	Editor.vsm.repaintNow();
    }


}
