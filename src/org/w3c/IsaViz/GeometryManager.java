/*   FILE: GeometryManager.java
 *   DATE OF CREATION:   12/17/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.util.Vector;
import java.util.Enumeration;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.Shape;
import java.awt.Graphics;
import java.net.URL;
import java.net.MalformedURLException;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.glyphs.GlyphUtils;
// import net.claribole.zvtm.lens.Lens;
// import net.claribole.zvtm.lens.LensListener;
import net.claribole.zvtm.engine.AnimationListener;
// import net.claribole.zvtm.lens.FSCenteredInverseCosineLens;

/*methods to compute new geometry (ellipse width, text position, paths, etc...)*/

public class GeometryManager {

    public static int ARROW_HEAD_SIZE=5;       //size of the VTriangle used as head of arrows (edges)
    public static int DEFAULT_NODE_WIDTH=40;   //widht and height of new nodes (resources and literals)
    public static int DEFAULT_NODE_HEIGHT=18;

//     static int LENS_R1 = 200;
//     static int LENS_R2 = 100;
//     Integer lensID;

    Editor application;

    GeometryManager(Editor e){
	application=e;
    }

    /*object created to edit the geometric attributes of a node/edge - remember it to be able to quickly destroy it*/
    Resizer lastResizer;

    void resetLastResizer(){
	lastResizer=null;
    }

    //adjust the start and end points of a path when resizing one of the nodes it is attached to (passed as parameter)
    /*this could be greatly enhanced - we are just modifying the coordinates of the start/end point whereas we could 
      translate all points of the path so that it keeps the same shape, but with a different aspect ratio - this should
      not be too hard ('amount' of translation is proportional to the delta from old to new position of the node)
      -actually, not sure this would be a good idea: would that be the expected behavior form the user point of view?
    */
    public void adjustPaths(INode n){
	if (n.isVisuallyRepresented()){
	    Vector v;
	    Point2D delta;
	    Point2D newPoint;
	    VPath p;
	    Vector segs=new Vector();
	    double[] cds=new double[6];
	    int type;
	    IProperty ip;
	    if (n instanceof IResource){
		IResource r=(IResource)n;
		Glyph el1=n.getGlyph();
		Shape el2=GlyphUtils.getJava2DShape(el1);
		if ((v=r.getOutgoingPredicates())!=null){
		    for (int i=0;i<v.size();i++){//for all outgoing edges
			ip=(IProperty)v.elementAt(i);
			if (ip.isVisuallyRepresented()){
			    p=(VPath)ip.getGlyph(); //get the path
			    PathIterator pi=p.getJava2DPathIterator();
			    segs.removeAllElements();
			    while (!pi.isDone()){ //store all its segments
				type=pi.currentSegment(cds);
				segs.add(new PathSegment(cds,type));
				pi.next();
			    }
			    newPoint=((PathSegment)segs.firstElement()).getMainPoint();
			    if (el2.contains(newPoint)){//path start point is inside the node shape
				if (el1 instanceof VPolygon){
				    LongPoint lp=((VPolygon)el1).getCentroid();
				    delta=computeStepValue(lp.x,lp.y,newPoint.getX(),newPoint.getY());
				}
				else {
				    delta=computeStepValue(el1.vx,el1.vy,newPoint.getX(),newPoint.getY());
				}
				while (el2.contains(newPoint)){
				    newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
				}
			    }
			    else {//path start point is outside the node shape
				if (el1 instanceof VPolygon){
				    LongPoint lp=((VPolygon)el1).getCentroid();
				    delta=computeStepValue(newPoint.getX(),newPoint.getY(),lp.x,lp.y);
				}
				else {
				    delta=computeStepValue(newPoint.getX(),newPoint.getY(),el1.vx,el1.vy);
				}
				while (!el2.contains(newPoint)){
				    newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
				}
			    }
			    ((PathSegment)segs.firstElement()).setMainPoint(newPoint);
			    //then update the VPath
			    reconstructVPathFromPathSegments(p,segs);
			}
		    }
		}
		if ((v=r.getIncomingPredicates())!=null){
		    for (int i=0;i<v.size();i++){//for all incoming edges
			ip=(IProperty)v.elementAt(i);
			if (ip.isVisuallyRepresented()){
			    p=(VPath)ip.getGlyph(); //get the path
			    PathIterator pi=p.getJava2DPathIterator();
			    segs.removeAllElements();
			    while (!pi.isDone()){ //store all its segments
				type=pi.currentSegment(cds);
				segs.add(new PathSegment(cds,type));
				pi.next();
			    }
			    newPoint=((PathSegment)segs.lastElement()).getMainPoint();
			    if (el2.contains(newPoint)){//path start point is inside the ellipse
				delta=computeStepValue(el1.vx,el1.vy,newPoint.getX(),newPoint.getY());
				while (el2.contains(newPoint)){
				    newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
				}
			    }
			    else {//path start point is outside the ellipse
				delta=computeStepValue(newPoint.getX(),newPoint.getY(),el1.vx,el1.vy);
				while (!el2.contains(newPoint)){
				    newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
				}
			    }
			    ((PathSegment)segs.lastElement()).setMainPoint(newPoint);
			    //then update the VPath
			    reconstructVPathFromPathSegments(p,segs);
			    //and the VTriangle (arrow head)
			    VTriangleOr t=(VTriangleOr)((IProperty)v.elementAt(i)).getGlyphHead(); //get the arrow head
			    double[] last2points=getLastTwoVPathPoints(segs);
			    GeometryManager.createPathArrowHead(last2points[0],last2points[1],last2points[2],last2points[3],t);
			}
		    }
		}
	    }
	    else {//n instanceof ILiteral
		ILiteral l=(ILiteral)n;
		Glyph el1=n.getGlyph();
		Shape el2=GlyphUtils.getJava2DShape(el1);
		if (l.getIncomingPredicate()!=null){
		    ip=(IProperty)l.getIncomingPredicate();
		    if (ip.isVisuallyRepresented()){//in theory this is not necessary, as a visible literal has necessarily a visible incoming property
			//but you never now... :-)
			p=(VPath)ip.getGlyph(); //get the path
			PathIterator pi=p.getJava2DPathIterator();
			segs.removeAllElements();
			while (!pi.isDone()){ //store all its segments
			    type=pi.currentSegment(cds);
			    segs.add(new PathSegment(cds,type));
			    pi.next();
			}
			newPoint=((PathSegment)segs.lastElement()).getMainPoint();
			if (el2.contains(newPoint)){//path start point is inside the ellipse
			    delta=computeStepValue(el1.vx,el1.vy,newPoint.getX(),newPoint.getY());
			    while (el2.contains(newPoint)){
				newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
			    }
			}
			else {//path start point is outside the ellipse
			    delta=computeStepValue(newPoint.getX(),newPoint.getY(),el1.vx,el1.vy);
			    while (!el2.contains(newPoint)){
				newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
			    }
			}
			((PathSegment)segs.lastElement()).setMainPoint(newPoint);
			//then update the VPath
			reconstructVPathFromPathSegments(p,segs);
			//and the VTriangle (arrow head)
			VTriangleOr t=(VTriangleOr)l.getIncomingPredicate().getGlyphHead(); //get the arrow head
			double[] last2points=getLastTwoVPathPoints(segs);
			GeometryManager.createPathArrowHead(last2points[0],last2points[1],last2points[2],last2points[3],t);
		    }
		}
	    }
	    Editor.vsm.repaintNow();
	}
    }

    public void adjustTablePath(IProperty p){//in theory, it is not necessary to adjust the edge's tail, as this is taken care of by adjustPaths() when called
	//on the subject (but I suppose I should check that it is actually the case at some point - anyway it looks fine the way it is)
	if (p.isVisuallyRepresented()){
	    Vector v=p.getSubject().getOutgoingPredicates();//get all properties that might potentially belong to the table
	    IProperty tmpP;
	    Vector cells=new Vector();
	    for (int i=0;i<v.size();i++){//retrieve all properties in this table form
		tmpP=(IProperty)v.elementAt(i);
		if (tmpP.getGlyph()==p.getGlyph()){//a property is in the same table form of it shares the same edge (VPath)
		    cells.add(tmpP.getTableCellGlyph());
		}
	    }//then select the first cell (highest row) to have the table's incoming edge attached
	    //then select the first cell (highest row) to have the table's incoming edge attached
// 	    el1=GeometryManager.getNorthMostGlyph(cells);
// 	    el2=GlyphUtils.getJava2DShape(el1);
	    /*the code below replaces the above 2 lines: it considers the whole property column as the rectangular border to which the edge should be attached, instead of considering only the first cell*/
	    VRectangle el1a=(VRectangle)GeometryManager.getNorthMostGlyph(cells);
	    VRectangle el1b=(VRectangle)GeometryManager.getSouthMostGlyph(cells);
 	    VRectangle el1=new VRectangle(el1a.vx,(el1a.vy+el1b.vy)/2,0,el1a.getWidth(),(Math.abs(el1b.vy-el1a.vy)+el1a.getHeight()+el1b.getHeight())/2,java.awt.Color.black);
	    Shape el2=GlyphUtils.getJava2DShape(el1);
	    VPath pt=(VPath)p.getGlyph(); //get the path;
	    PathIterator pi=pt.getJava2DPathIterator();
	    Vector segs=new Vector();
	    double[] cds=new double[6];
	    int type;
	    while (!pi.isDone()){ //store all its segments
		type=pi.currentSegment(cds);
		segs.add(new PathSegment(cds,type));
		pi.next();
	    }
	    Point2D newPoint=((PathSegment)segs.lastElement()).getMainPoint();
	    if (el2.contains(newPoint)){//path start point is inside the ellipse
		Point2D delta=computeStepValue(el1.vx,el1.vy,newPoint.getX(),newPoint.getY());
		while (el2.contains(newPoint)){
		    newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
		}
	    }
	    else {//path start point is outside the ellipse
		Point2D delta=computeStepValue(newPoint.getX(),newPoint.getY(),el1.vx,el1.vy);
		while (!el2.contains(newPoint)){
		    newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
		}
	    }
	    ((PathSegment)segs.lastElement()).setMainPoint(newPoint);
	    //then update the VPath
	    reconstructVPathFromPathSegments(pt,segs);
	    //and the VTriangle (arrow head)
	    VTriangleOr t=(VTriangleOr)p.getGlyphHead(); //get the arrow head
	    double[] last2points=getLastTwoVPathPoints(segs);
	    GeometryManager.createPathArrowHead(last2points[0],last2points[1],last2points[2],last2points[3],t);
	    Editor.vsm.repaintNow();
	}
    }

    //adjust a resource's ellipse width and center text in it - also adjust paths since the ellipse might change
    public void adjustResourceTextAndShape(IResource r,String newText){//newText==null if text is left unchanged
	if (r.isVisuallyRepresented()){
	    if (r.isLaidOutInTableForm()){
		VText g=r.getGlyphText();
		if (newText!=null){g.setText(newText);}
		Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
		gc.setFont(g.getFont());
		Rectangle2D r2d=gc.getFontMetrics().getStringBounds(g.getText(),gc);//have to do it this way because the paint thread might not yet have (and probably has not yet) computed the new String's bounds
		VRectangle el=(VRectangle)r.getGlyph();//sure it is a VRectangle as it is a table cell
		alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),el,new Integer(r.getTextAlign()));
		//no adjust path done here (it would happen several times for the same edge, as it is shared by all properties in the table)
	    }
	    else {
		VText g=r.getGlyphText();
		if (newText!=null){g.setText(newText);}
		Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
		gc.setFont(g.getFont());
		Rectangle2D r2d=gc.getFontMetrics().getStringBounds(g.getText(),gc);//have to do it this way because the paint thread might not yet have (and probably has not yet) computed the new String's bounds
		Glyph el=r.getGlyph();
		if (el instanceof RectangularShape && !(el instanceof VImage)){
		    RectangularShape rs=(RectangularShape)el;
		    rs.setWidth(Math.round(0.6*r2d.getWidth()));  //0.6 comes from 1.2*Math.round(r2d.getWidth())/2
		    //shape should always have width > height  (just for aesthetics)
		    if (rs.getWidth()<(1.5*rs.getHeight())){rs.setWidth(Math.round(1.5*rs.getHeight()));}
		    //center VText in rectangle
		    //g.moveTo(el.vx-(long)r2d.getWidth()/2,el.vy-(long)r2d.getHeight()/4);
		    alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),el,new Integer(r.getTextAlign()));
		    adjustPaths(r);
		}
		else {/*else we don't want to adjust the width of non-rectangular shapes (also true for icons. hence the exception for VImage)*/
		    //center VText in rectangle
		    //g.moveTo(el.vx-(long)r2d.getWidth()/2,el.vy-(long)r2d.getHeight()/4);
		    alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),el,new Integer(r.getTextAlign()));
		    adjustPaths(r);
		}
	    }
	}
    }

    //called after RDF import (graphviz positioning can be bad, e.g. under Linux)
    public void correctResourceTextAndShape(IResource r){
	if (r.isVisuallyRepresented()){
	    if (r.isLaidOutInTableForm()){
		VText g=r.getGlyphText();
		VRectangle el=(VRectangle)r.getGlyph();//sure it is a VRectangle as it is a table cell
		Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
		gc.setFont(g.getFont());
		Rectangle2D r2d=gc.getFontMetrics().getStringBounds(g.getText(),gc);//have to do it this way because the paint thread might not yet have (and probably has not yet) computed the new String's bounds
		alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),el,new Integer(r.getTextAlign()));
		//no adjust path done here (it would happen several times for the same edge, as it is shared by all properties in the table)
	    }
	    else {
		VText g=r.getGlyphText();
		Glyph el=r.getGlyph();
		Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
		gc.setFont(g.getFont());
		Rectangle2D r2d=gc.getFontMetrics().getStringBounds(g.getText(),gc);//have to do it this way because the paint thread might not yet have (and probably has not yet) computed the new String's bounds 
		if (el instanceof RectangularShape && !(el instanceof VImage)){
		    RectangularShape rs=(RectangularShape)el;
		    rs.setWidth(Math.round(0.6*r2d.getWidth()));  //0.6 comes from 1.2*bounds/2
		    //ellipse should always have width > height  (just for aesthetics)
		    if (rs.getWidth()<(1.5*rs.getHeight())){rs.setWidth(Math.round(1.5*rs.getHeight()));}
		    //center VText in rectangle
		    //g.moveTo(el.vx-(long)r2d.getWidth()/2,el.vy-(long)r2d.getHeight()/4);
		    alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),el,new Integer(r.getTextAlign()));
		    adjustPaths(r);
		}
		else {/*else we don't want to adjust the width of non-rectangular shapes (also true for icons. hence the exception for VImage)*/
		    //center VText in rectangle
		    //g.moveTo(el.vx-(long)r2d.getWidth()/2,el.vy-(long)r2d.getHeight()/4);
		    alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),el,new Integer(r.getTextAlign()));
		    adjustPaths(r);
		}
	    }
	}
    }

    //called after RDF import (graphviz positioning can be bad, e.g. under Linux)
    public void correctLiteralTextAndShape(ILiteral l){
	if (l.isVisuallyRepresented()){
	    VText g=l.getGlyphText();
	    if (g!=null && g.getText().length()>0){
		if (l.isLaidOutInTableForm()){
		    VRectangle rl=(VRectangle)l.getGlyph();//sure it is a VRectangle as it is a table cell
		    Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
		    gc.setFont(g.getFont());
		    Rectangle2D r2d=gc.getFontMetrics().getStringBounds(g.getText(),gc);//have to do it this way because the paint thread might not yet have (and probably has not yet) computed the new String's bounds
		    alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),rl,new Integer(l.getTextAlign()));
		    //no adjust path done here (it would happen several times for the same edge, as it is shared by all properties in the table)
		}
		else {
		    Glyph rl=l.getGlyph();
		    Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
		    gc.setFont(g.getFont());
		    Rectangle2D r2d=gc.getFontMetrics().getStringBounds(g.getText(),gc);//have to do it this way because the paint thread might not yet have (and probably has not yet) computed the new String's bounds 
		    if (rl instanceof RectangularShape && !(rl instanceof VImage)){
			RectangularShape rs=(RectangularShape)rl;
			rs.setWidth(Math.round(0.6*r2d.getWidth()));  //0.6 comes from 1.2*bounds/2
			//rectangles should always have width > height  (just for aesthetics)
			if (rs.getWidth()<(1.5*rs.getHeight())){rs.setWidth(Math.round(1.5*rs.getHeight()));}
			//center VText in rectangle
			//g.moveTo(rl.vx-(long)r2d.getWidth()/2,rl.vy-(long)r2d.getHeight()/4);
			alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),rl,new Integer(l.getTextAlign()));
			adjustPaths(l);
		    }
		    else {/*else we don't want to adjust the width of non-rectangular shapes (also true for icons. hence the exception for VImage)*/
			//center VText in rectangle
			//g.moveTo(rl.vx-(long)r2d.getWidth()/2,rl.vy-(long)r2d.getHeight()/4);
			alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),rl,new Integer(l.getTextAlign()));
			adjustPaths(l);
		    }
		}
	    }
	}
    }

    //called after RDF import (graphviz positioning can be bad, e.g. under Linux)
    public void correctPropertyTextAndShape(IProperty p){
	if (p.isVisuallyRepresented()){
	    if (p.isLaidOutInTableForm()){
		VText g=p.getGlyphText();
		VRectangle el=(VRectangle)p.getTableCellGlyph();//sure it is a VRectangle as it is a table cell
		Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
		gc.setFont(g.getFont());
		Rectangle2D r2d=gc.getFontMetrics().getStringBounds(g.getText(),gc);//have to do it this way because the paint thread might not yet have (and probably has not yet) computed the new String's bounds
		alignText(g,new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight())),el,new Integer(p.getTextAlign()));
		//doing the adjust path here so that it gets done only once per table/edge
		adjustTablePath(p);
	    }
	    //else do nothing (there isn't really anything cleaver to do as the property text is positioned w.r.t an edge
	}
    }

    //just centers the text inside the shape
    public void adjustResourceText(IResource r){
	if (r.isVisuallyRepresented()){
	    VText g=r.getGlyphText();
	    if (g!=null){
		Glyph el=r.getGlyph();
		LongPoint bounds=g.getBounds(Editor.vsm.getActiveCamera().getIndex());
		alignText(g,bounds,el,new Integer(r.getTextAlign()));
	    }
	}
    }

    //just centers the text inside the shape
    public void adjustLiteralText(ILiteral l){
	if (l.isVisuallyRepresented()){
	    VText g=l.getGlyphText();
	    if (g!=null){
		Glyph rl=l.getGlyph();
		LongPoint bounds=g.getBounds(Editor.vsm.getActiveCamera().getIndex());
		alignText(g,bounds,rl,new Integer(l.getTextAlign()));
	    }
	}
    }

    //just centers the text inside the shape
    public void adjustPropertyText(IProperty p){
	if (p.isVisuallyRepresented() && p.isLaidOutInTableForm()){
	    VText g=p.getGlyphText();
	    if (g!=null){
		Glyph rl=p.getTableCellGlyph();
		LongPoint bounds=g.getBounds(Editor.vsm.getActiveCamera().getIndex());
		alignText(g,bounds,rl,Style.TA_CENTER);
	    }
	}
    }

    /*align text w.r.t shape based on alignment*/
    void alignText(VText text,LongPoint bounds,Glyph shape,Integer alignment){
	Rectangle2D r2d=GlyphUtils.getJava2DShape(shape).getBounds2D();
	if (alignment.equals(Style.TA_CENTER)){
	    if (text.getTextAnchor()==VText.TEXT_ANCHOR_START){text.moveTo(shape.vx-bounds.x/2,shape.vy-bounds.y/4);}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){text.moveTo(shape.vx,shape.vy-bounds.y/4);}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_END){text.moveTo(shape.vx+bounds.x/2,shape.vy-bounds.y/4);}
	}
	else if (alignment.equals(Style.TA_BELOW)){
	    if (text.getTextAnchor()==VText.TEXT_ANCHOR_START){text.moveTo(shape.vx-bounds.x/2,Math.round(r2d.getMinY()-bounds.y));}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){text.moveTo(shape.vx,Math.round(r2d.getMinY()-bounds.y));}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_END){text.moveTo(shape.vx+bounds.x/2,Math.round(r2d.getMinY()-bounds.y));}
	}
	else if (alignment.equals(Style.TA_ABOVE)){
	    if (text.getTextAnchor()==VText.TEXT_ANCHOR_START){text.moveTo(shape.vx-bounds.x/2,Math.round(r2d.getMaxY()+bounds.y/2));}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){text.moveTo(shape.vx,Math.round(r2d.getMaxY()+bounds.y/2));}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_END){text.moveTo(shape.vx+bounds.x/2,Math.round(r2d.getMaxY()+bounds.y/2));}
	}
	else if (alignment.equals(Style.TA_LEFT)){
	    if (text.getTextAnchor()==VText.TEXT_ANCHOR_START){text.moveTo(Math.round(r2d.getMinX()-bounds.x),shape.vy-bounds.y/4);}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){text.moveTo(Math.round(r2d.getMinX()-bounds.x/2),shape.vy-bounds.y/4);}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_END){text.moveTo(Math.round(r2d.getMinX()),shape.vy-bounds.y/4);}
	}
	else if (alignment.equals(Style.TA_RIGHT)){
	    if (text.getTextAnchor()==VText.TEXT_ANCHOR_START){text.moveTo(Math.round(r2d.getMaxX()),shape.vy-bounds.y/4);}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){text.moveTo(Math.round(r2d.getMaxX()+bounds.x/2),shape.vy-bounds.y/4);}
	    else if (text.getTextAnchor()==VText.TEXT_ANCHOR_END){text.moveTo(Math.round(r2d.getMaxX()+bounds.x),shape.vy-bounds.y/4);}
	}
    }
    
    /*aligns text w.r.t shape based on alignment (value in Style.TA_*)
     *
     */
    void alignText(Glyph shape,VText text,Integer alignment){
	if (shape!=null && text!=null){
	    Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
	    gc.setFont(text.getFont());
	    Rectangle2D r2d=gc.getFontMetrics().getStringBounds(text.getText(),gc);
	    LongPoint textbounds=new LongPoint(Math.round(r2d.getWidth()),Math.round(r2d.getHeight()));
	    r2d=GlyphUtils.getJava2DShape(shape).getBounds2D();
	    if (alignment.equals(Style.TA_BELOW)){
		text.moveTo(text.vx,Math.round(r2d.getMinY()-textbounds.y));
	    }
	    else if (alignment.equals(Style.TA_ABOVE)){
		text.moveTo(text.vx,Math.round(r2d.getMaxY()+textbounds.y));
	    }
	    else if (alignment.equals(Style.TA_LEFT)){
		if (text.getTextAnchor()==VText.TEXT_ANCHOR_START){text.moveTo(Math.round(r2d.getMinX()-textbounds.x),text.vy);}
		else if (text.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){text.moveTo(Math.round(r2d.getMinX()-textbounds.x/2),text.vy);}
		else if (text.getTextAnchor()==VText.TEXT_ANCHOR_END){text.moveTo(Math.round(r2d.getMinX()),text.vy);}
	    }
	    else if (alignment.equals(Style.TA_RIGHT)){
		if (text.getTextAnchor()==VText.TEXT_ANCHOR_START){text.moveTo(Math.round(r2d.getMaxX()),text.vy);}
		else if (text.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){text.moveTo(Math.round(r2d.getMaxX()+textbounds.x/2),text.vy);}
		else if (text.getTextAnchor()==VText.TEXT_ANCHOR_END){text.moveTo(Math.round(r2d.getMaxX()+textbounds.x),text.vy);}
	    }
	    //TA_CENTER: nothing to do
	}
    }

    //update the VText of an IProperty, change its position so that the center of the String does not move
    void updateAPropertyText(IProperty p,String text){
	VText g=p.getGlyphText();
	if (g!=null){/*using Graphics.getFontMetrics() instead of VText.getBounds() because there is little 
		       chance the bounds will be actually updated between the first and second query*/
	    Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
	    gc.setFont(g.getFont());
	    Rectangle2D r2d=gc.getFontMetrics().getStringBounds(g.getText(),gc);
	    long oldX=Math.round(g.vx+r2d.getWidth()/2);
	    long oldY=Math.round(g.vy+r2d.getHeight()/2);
	    g.setText(text);
	    r2d=gc.getFontMetrics().getStringBounds(text,gc);
	    g.moveTo(Math.round(oldX-r2d.getWidth()/2),Math.round(oldY-r2d.getHeight()/2));
	}
    }

    /*graphical objects to resize a resource's ellipse in the graph*/
    void initResourceResizer(IResource r){
	destroyLastResizer();
	r.displayOnTop();
	if (r.isLaidOutInTableForm()){
	    lastResizer=new TableColResizer(r,TableColResizer.RIGHT_COLUMN);
	    Vector pit=((TableColResizer)lastResizer).getPropertiesInTable();
	    //attach all other table cells to the one being dragged, and get geom info for undo
	    //we cannot do this statically at table creation time, as thew ZVTM stick thing is an oriented graph, which depends on the entry point
	    IProperty p;
	    //also remember geometry of other property/objects in the same table as this literal
	    //so that they can be restored if user undoes the operation
	    Vector dependentProps=new Vector();
	    Vector dependentRess=new Vector();
	    Vector dependentLits=new Vector();
	    dependentRess.add(r);
 	    IProperty incomingPred=(IProperty)r.getIncomingPredicates().firstElement();
	    //note that the simplicity of the code above to retrieve the incoming predicate is due to the fact that for now, a resource laid out in a table can only be there if it has no outgoing predicate and ONE incoming predicate (which is the assumption behind the firstElement() thing here)
	    for (int i=0;i<pit.size();i++){
		p=(IProperty)pit.elementAt(i);
		Editor.vsm.stickToGlyph(p.getTableCellGlyph(),r.getGlyph());
		Editor.vsm.stickToGlyph(p.getGlyphText(),r.getGlyph());
		dependentProps.add(p);
		if (p!=incomingPred){//do not stick the dragged object cell to itself (its text will be attached later)!
		    Editor.vsm.stickToGlyph(p.getObject().getGlyph(),r.getGlyph());
		    try {Editor.vsm.stickToGlyph(p.getObject().getGlyphText(),r.getGlyph());}
		    catch (Exception ex){/*text might be null*/}
		    if (p.getObject() instanceof IResource){dependentRess.add(p.getObject());}
		    else {dependentLits.add(p.getObject());}//necessarily an ILiteral
		}
	    }
	    ISVGeom cmd=new ISVGeom(application,dependentProps,dependentRess,dependentLits);
	    application.addCmdToUndoStack(cmd);
	}
	else {
	    Vector v=new Vector();v.add(r);
	    Vector dependencies=new Vector();
	    IProperty p;
	    if (r.getIncomingPredicates()!=null){//also remember geometry of properties attached to this resource
		for (Enumeration e=r.getIncomingPredicates().elements();e.hasMoreElements();){//so that it can be
		    p=(IProperty)e.nextElement();
		    if (p.isVisuallyRepresented()){dependencies.add(p);}//restored if user undoes the operation
		}
	    }
	    if (r.getOutgoingPredicates()!=null){
		for (Enumeration e=r.getOutgoingPredicates().elements();e.hasMoreElements();){
		    p=(IProperty)e.nextElement();
		    if (p.isVisuallyRepresented()){dependencies.add(p);}
		}
	    }
	    ISVGeom cmd=new ISVGeom(application,dependencies,v,new Vector());
	    application.addCmdToUndoStack(cmd);
	    lastResizer=new ResResizer(r);
	}
	if (r.getGlyphText()!=null){//also update the resource text's position
	    Editor.vsm.stickToGlyph(r.getGlyphText(),r.getGlyph());
	}
    }

    /*graphical objects to resize a literal's rectangle in the graph*/
    void initLiteralResizer(ILiteral l){
	destroyLastResizer();
	l.displayOnTop();
	if (l.isLaidOutInTableForm()){
	    lastResizer=new TableColResizer(l,TableColResizer.RIGHT_COLUMN);
	    Vector pit=((TableColResizer)lastResizer).getPropertiesInTable();
	    //attach all other table cells to the one being dragged, and get geom info for undo
	    //we cannot do this statically at table creation time, as thew ZVTM stick thing is an oriented graph, which depends on the entry point
	    IProperty p;
	    //also remember geometry of other property/objects in the same table as this literal
	    //so that they can be restored if user undoes the operation
	    Vector dependentProps=new Vector();
	    Vector dependentRess=new Vector();
	    Vector dependentLits=new Vector();
	    dependentLits.add(l);
	    for (int i=0;i<pit.size();i++){
		p=(IProperty)pit.elementAt(i);
		Editor.vsm.stickToGlyph(p.getTableCellGlyph(),l.getGlyph());
		Editor.vsm.stickToGlyph(p.getGlyphText(),l.getGlyph());
		dependentProps.add(p);
		if (p!=l.getIncomingPredicate()){//do not stick the dragged object cell to itself (its text will be attached later)!
		    Editor.vsm.stickToGlyph(p.getObject().getGlyph(),l.getGlyph());
		    try {Editor.vsm.stickToGlyph(p.getObject().getGlyphText(),l.getGlyph());}
		    catch (Exception ex){/*text might be null*/}
		    if (p.getObject() instanceof IResource){dependentRess.add(p.getObject());}
		    else {dependentLits.add(p.getObject());}//necessarily an ILiteral
		}
	    }
	    ISVGeom cmd=new ISVGeom(application,dependentProps,dependentRess,dependentLits);
	    application.addCmdToUndoStack(cmd);
	}
	else {
	    Vector v=new Vector();v.add(l);
	    Vector dependencies=new Vector();
	    //also remember geometry of properties attached to this resource so that it can be restored if 
	    //user undoes the operation
	    if (l.getIncomingPredicate()!=null && l.getIncomingPredicate().isVisuallyRepresented()){dependencies.add(l.getIncomingPredicate());}
	    ISVGeom cmd=new ISVGeom(application,dependencies,new Vector(),v);
	    application.addCmdToUndoStack(cmd);
	    lastResizer=new LitResizer(l);
	}
	if (l.getGlyphText()!=null){//also update the literal text's position
	    Editor.vsm.stickToGlyph(l.getGlyphText(),l.getGlyph());
	}
    }

    /*graphical objects to edit a predicate's path in the graph*/
    void initPropertyResizer(IProperty p){
	destroyLastResizer();
	Vector v=new Vector();v.add(p);
	ISVGeom cmd=new ISVGeom(application,v,new Vector(),new Vector());
	application.addCmdToUndoStack(cmd);
	p.displayOnTop();
	lastResizer=new PropResizer(p);
    }

    /*graphical objects to edit a predicate's table cell*/
    void initPropCellResizer(IProperty p){
	destroyLastResizer();
	lastResizer=new TableColResizer(p,TableColResizer.LEFT_COLUMN);
	Vector pit=((TableColResizer)lastResizer).getPropertiesInTable();
	//attach all other table cells to the one being dragged, and get geom info for undo
	//we cannot do this statically at table creation time, as thew ZVTM stick thing is an oriented graph, which depends on the entry point
	IProperty p2;
	//also remember geometry of other property/objects in the same table as this literal
	//so that they can be restored if user undoes the operation
	Vector dependentProps=new Vector();
	Vector dependentRess=new Vector();
	Vector dependentLits=new Vector();
	dependentProps.add(p);
	for (int i=0;i<pit.size();i++){
	    p2=(IProperty)pit.elementAt(i);
	    if (p2!=p){//do not stick the dragged object cell to itself (its text will be attached later)!
		Editor.vsm.stickToGlyph(p2.getTableCellGlyph(),p.getTableCellGlyph());
		Editor.vsm.stickToGlyph(p2.getGlyphText(),p.getTableCellGlyph());
		dependentProps.add(p2);
	    }
	    Editor.vsm.stickToGlyph(p2.getObject().getGlyph(),p.getTableCellGlyph());
	    try {Editor.vsm.stickToGlyph(p2.getObject().getGlyphText(),p.getTableCellGlyph());}
	    catch (Exception ex){/*text might be null*/}
	    if (p2.getObject() instanceof IResource){dependentRess.add(p2.getObject());}
	    else {dependentLits.add(p2.getObject());}//necessarily an ILiteral
	}
	ISVGeom cmd=new ISVGeom(application,dependentProps,dependentRess,dependentLits);
	application.addCmdToUndoStack(cmd);
	if (p.getGlyphText()!=null){//also update the property text's position
	    Editor.vsm.stickToGlyph(p.getGlyphText(),p.getTableCellGlyph());
	}
    }

    /*destroy graphical objects (handles) used to resize/move the last node/edge edited*/
    void destroyLastResizer(){
	if (lastResizer!=null){lastResizer.destroy();lastResizer=null;}
    }

    /*resize a resource/literal*/
    void resize(Glyph handle){
	try {lastResizer.updateMainGlyph(handle);}
	catch (NullPointerException e){}
    }

    /*end resizing a resource/literal or property in table*/
    void endResize(){
	Editor.vsm.unstickFromMouse();
	//then have to adjust edges start and end points attached to this resource/literal
	INode o=(INode)lastResizer.getMainGlyph().getOwner();
	if (o.isLaidOutInTableForm()){//for resources (and literals), we are necessarily resizing them as the object of
	    //there single incoming statement, that's why we can access getIncomginPredicate().firstElement() without further testing
	    IProperty p;
	    Glyph edge;
	    Vector props;
	    if (o instanceof IResource){
		p=(IProperty)((IResource)o).getIncomingPredicates().firstElement();
		edge=p.getGlyph();
		adjustTablePath(p);
		props=p.getSubject().getOutgoingPredicates();
		if (props!=null){
		    for (int i=0;i<props.size();i++){
			p=(IProperty)props.elementAt(i);
			if (p.getGlyph()==edge){
			    if (p.getObject() instanceof IResource){adjustResourceText((IResource)p.getObject());}
			    else {adjustLiteralText((ILiteral)p.getObject());}
			}
		    }
		}
	    }
	    else if (o instanceof ILiteral){
		p=((ILiteral)o).getIncomingPredicate();
		edge=p.getGlyph();
		adjustTablePath(p);
		props=p.getSubject().getOutgoingPredicates();
		if (props!=null){
		    for (int i=0;i<props.size();i++){
			p=(IProperty)props.elementAt(i);
			if (p.getGlyph()==edge){
			    if (p.getObject() instanceof IResource){adjustResourceText((IResource)p.getObject());}
			    else {adjustLiteralText((ILiteral)p.getObject());}
			}
		    }
		}
	    }
	    else if (o instanceof IProperty){
		p=(IProperty)o;
		edge=p.getGlyph();
		adjustTablePath(p);
		props=p.getSubject().getOutgoingPredicates();
		if (props!=null){
		    for (int i=0;i<props.size();i++){
			p=(IProperty)props.elementAt(i);
			if (p.getGlyph()==edge){
			    adjustPropertyText(p);
			}
		    }
		}
	    }
	}
	else {
	    if (o instanceof IResource){adjustResourceText((IResource)o);adjustPaths((INode)o);}
	    else if (o instanceof ILiteral){adjustLiteralText((ILiteral)o);adjustPaths((INode)o);}
	    //nothing to do for properties not laid out in a table form
	}
    }

    /*move a resource/literal*/
    void move(Glyph mainGlyph){
	try {lastResizer.updateHandles();}
	catch (NullPointerException e){}
    }

    /*end moving a resource/literal*/
    void endMove(){
	INode in=(INode)lastResizer.getMainGlyph().getOwner();
	//no longer needed as unstickAllGlyphs takes care of everything
// 	try {Editor.vsm.unstickFromGlyph(in.getGlyphText(),lastResizer.getMainGlyph());}
// 	catch (NullPointerException ex){}
	try {Editor.vsm.unstickAllGlyphs(lastResizer.getMainGlyph());}
	catch (NullPointerException ex){}
	Editor.vsm.unstickFromMouse();
	//then have to adjust edges start and end points attached to this resource/literal
	if (in.isLaidOutInTableForm()){
	    //we know there is only one incoming property as this is mandatory for a resource to be laid out in table form
	    if (in instanceof IResource){adjustTablePath((IProperty)((IResource)in).getIncomingPredicates().firstElement());}
	    else if (in instanceof ILiteral){adjustTablePath(((ILiteral)in).getIncomingPredicate());}
	    else if (in instanceof IProperty){adjustTablePath((IProperty)in);}
	}
	else {adjustPaths(in);}
	application.centerRadarView();
    }

    /*draw the VPath matching a broken line*/
    void updatePathAfterResize(){
	try {
	    lastResizer.updateMainGlyph(null);
	}
	catch (NullPointerException e){}
    }

    /*given a list of segments describing a broken line, reconstruct the VPath matching it*/
    void reconstructVPathFromPathSegments(VPath p,Vector segs){//segs is a vector of PathSegment
	double[] cds;
	PathSegment ps;
	p.resetPath();
	for (int j=0;j<segs.size();j++){
	    ps=(PathSegment)segs.elementAt(j);
	    cds=ps.getCoords();
	    switch (ps.getType()){
	    case PathIterator.SEG_CUBICTO:{
		p.addCbCurve((long)cds[4],(long)cds[5],(long)cds[0],(long)cds[1],(long)cds[2],(long)cds[3],true);
		break;
	    }
	    case PathIterator.SEG_QUADTO:{
		p.addQdCurve((long)cds[2],(long)cds[3],(long)cds[0],(long)cds[1],true);
		break;
	    }
	    case PathIterator.SEG_LINETO:{
		p.addSegment((long)cds[0],(long)cds[1],true);
		break;
	    }
	    case PathIterator.SEG_MOVETO:{
		p.jump((long)cds[0],(long)cds[1],true);
		break;
	    }
	    }
	}
    }

    /*returns the last two points of a vpath (no matter their type (start point, control point curve point,etc))*/
    double[] getLastTwoVPathPoints(Vector segs){//segs is a vector of PathSegment
	double[] res=new double[4];
	double[] cds=((PathSegment)segs.lastElement()).getCoords();
	int type=((PathSegment)segs.lastElement()).getType();
	if (type==PathIterator.SEG_LINETO){
	    Point2D oneButLast=((PathSegment)segs.elementAt(segs.size()-2)).getMainPoint();
	    res[0]=oneButLast.getX();
	    res[1]=oneButLast.getY();
	    res[2]=cds[0];
	    res[3]=cds[1];
	}
	else if (type==PathIterator.SEG_CUBICTO){
	    res[0]=cds[2];
	    res[1]=cds[3];
	    res[2]=cds[4];
	    res[3]=cds[5];
	}
	else if (type==PathIterator.SEG_QUADTO){
	    res[0]=cds[0];
	    res[1]=cds[1];
	    res[2]=cds[2];
	    res[3]=cds[3];
	}
	else {System.err.println("Error: Editor.getLastTwoVPathPoints: bad segment type "+type);}
	return res;
    }

    void insertSegmentInPath(Glyph g){//should only receive "rszp" VRectangles  (resizing path handles)
	ControlPoint cpA=(ControlPoint)g.getOwner();
	if (cpA.type<ControlPoint.END_POINT){//if cp is a START_POINT or CURVE_POINT  (i.e. a black handle except the last one)
	    PropResizer pr=cpA.owner;
	    //retrieve next handle
	    ControlPoint cpB=(ControlPoint)cpA.nextHandle.getOwner();
	    //destroy old segment linking cpA to cpB
	    Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).destroyGlyph(cpA.s2);
	    cpA.s2=null;
	    cpB.s1=null;
	    //compute coordinates of two new intermediate points between cpA and cpB -  we are creating a Quadratic curve
	    LongPoint p1=new LongPoint(Math.round(cpA.handle.vx+(cpB.handle.vx-cpA.handle.vx)/3),Math.round(cpA.handle.vy+(cpB.handle.vy-cpA.handle.vy)/3));
	    LongPoint p2=new LongPoint(Math.round(cpA.handle.vx+(cpB.handle.vx-cpA.handle.vx)*2/3),Math.round(cpA.handle.vy+(cpB.handle.vy-cpA.handle.vy)*2/3));
	    RectangleNR r1=new RectangleNR(p1.x,p1.y,0,4,4,java.awt.Color.red);
	    RectangleNR r2=new RectangleNR(p2.x,p2.y,0,4,4,java.awt.Color.black);
	    Editor.vsm.addGlyph(r1,Editor.mainVirtualSpace);
	    Editor.vsm.addGlyph(r2,Editor.mainVirtualSpace);
	    LongPoint pA=new LongPoint(cpA.handle.vx,cpA.handle.vy);
	    LongPoint pB=new LongPoint(cpB.handle.vx,cpB.handle.vy);
	    VSegment s1=new VSegment((pA.x+p1.x)/2,(pA.y+p1.y)/2,0,(p1.x-pA.x)/2,(pA.y-p1.y)/2,java.awt.Color.red);
	    VSegment s2=new VSegment((p1.x+p2.x)/2,(p1.y+p2.y)/2,0,(p2.x-p1.x)/2,(p1.y-p2.y)/2,java.awt.Color.red);
	    VSegment s3=new VSegment((p2.x+pB.x)/2,(p2.y+pB.y)/2,0,(pB.x-p2.x)/2,(p2.y-pB.y)/2,java.awt.Color.red);
	    Editor.vsm.addGlyph(s1,Editor.mainVirtualSpace);
	    Editor.vsm.addGlyph(s2,Editor.mainVirtualSpace);
	    Editor.vsm.addGlyph(s3,Editor.mainVirtualSpace);
	    cpA.setSecondSegment(s1,r1);
	    ControlPoint cp1=new ControlPoint(r1,cpA.handle,s1,ControlPoint.QUAD_CURVE_CP,pr);
	    cp1.setSecondSegment(s2,r2);
	    ControlPoint cp2=new ControlPoint(r2,r1,s2,ControlPoint.CURVE_POINT,pr);
	    cp2.setSecondSegment(s3,cpB.handle);
	    cpB.prevHandle=r2;
	    cpB.s1=s3;
	    //new points have been inserted and linked. Reconstruct the array of CPs
	    //for the resizer based on the new linked list
	    ControlPoint[] res=new ControlPoint[pr.cps.length+2];
	    int i=0;
	    ControlPoint iterator=pr.cps[i];
	    while (i<res.length-1){
		res[i]=iterator;
		iterator=(ControlPoint)iterator.nextHandle.getOwner();
		i++;
	    }
	    res[i]=iterator;  //last point is not inside the loop because its nextHandle is null
	    pr.cps=res;
	    pr.updateMainGlyph(null);
	}
    }

    void deleteSegmentInPath(Glyph g){//should only receive "rszp" VRectangles  (resizing path handles)
	ControlPoint cp1=(ControlPoint)g.getOwner();
	if (cp1.type==ControlPoint.CURVE_POINT){//if cp is a CURVE_POINT  (i.e. a black handle except the first and last one)
	    PropResizer pr=cp1.owner;
	    ControlPoint cpA=(ControlPoint)cp1.prevHandle.getOwner(); //first point before the ones to destroy
	    ControlPoint cpB; //first point after the ones to destroy
	    ControlPoint cpIt=(ControlPoint)cp1.nextHandle.getOwner();
	    VirtualSpace vs=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace);
	    vs.destroyGlyph(cp1.handle);
	    vs.destroyGlyph(cp1.s1);
	    vs.destroyGlyph(cp1.s2);
	    while (cpIt.type>ControlPoint.END_POINT){//seek next curve point and destroy all intermediate quad or cub control points
		vs.destroyGlyph(cpIt.handle);
		vs.destroyGlyph(cpIt.s2);
		cpIt=(ControlPoint)cpIt.nextHandle.getOwner();
	    }
	    cpB=cpIt;
	    LongPoint pA=new LongPoint(cpA.handle.vx,cpA.handle.vy);
	    LongPoint pB=new LongPoint(cpB.handle.vx,cpB.handle.vy);
	    VSegment s=new VSegment((pA.x+pB.x)/2,(pA.y+pB.y)/2,0,(pB.x-pA.x)/2,(pA.y-pB.y)/2,java.awt.Color.red);
	    Editor.vsm.addGlyph(s,Editor.mainVirtualSpace);
	    cpA.setSecondSegment(s,cpB.handle);
	    cpB.prevHandle=cpA.handle;
	    cpB.s1=s;
	    Vector v=new Vector();
	    cpIt=pr.cps[0];
	    v.add(cpIt);
	    while (cpIt.nextHandle!=null){
		cpIt=(ControlPoint)cpIt.nextHandle.getOwner();
		v.add(cpIt);
	    }
	    pr.cps=(ControlPoint[])v.toArray(new ControlPoint[v.size()]);
	    pr.updateMainGlyph(null);
	}
    }

    /*returns the same path but reversed (start point becomes end point) if point tx,ty is closer to first point on path cds than last point on the same path */
    static VPath invertPath(long tx,long ty,VPath pt){
	PathIterator pi=pt.getJava2DPathIterator();
	double[] cds=new double[6];
	//retrieve first point on path
	int type=pi.currentSegment(cds);
	pi.next();
	double fpx,fpy,lpx,lpy;  //first and last points on path
	if (type==PathIterator.SEG_MOVETO){//first instruction in a jump so the path begins at the coords specified by this jump
	    fpx=cds[0];
	    fpy=cds[1];
	}
	else {//first instructions is not a jump so the path begins at the current coordinates, i.e. 0,0 (should not happen)
	    fpx=0;
	    fpy=0;
	}
	while (!pi.isDone()){type=pi.currentSegment(cds);pi.next();}//go to last point (ignore intermediate points)
	if (type==PathIterator.SEG_CUBICTO){//last instruction is a cubic curve (in theory, it should always be this one, unless graphviz changes its SVG output format)
	    lpx=cds[4];
	    lpy=cds[5];
	}
	else if (type==PathIterator.SEG_QUADTO){//last instruction is a quadratic curve
	    lpx=cds[2];
	    lpy=cds[3];
	}
	else if (type==PathIterator.SEG_LINETO){//last instruction is a segment
	    lpx=cds[0];
	    lpy=cds[1];
	}
	else if (type==PathIterator.SEG_CLOSE){//last instruction closes the path
	    lpx=fpx;
	    lpy=fpy;
	}
	else {//last instruction is a jump (makes no sense)
	    lpx=0;
	    lpy=0;
	}
	double d1=Math.sqrt(Math.pow(tx-fpx,2)+Math.pow(ty-fpy,2));
	double d2=Math.sqrt(Math.pow(tx-lpx,2)+Math.pow(ty-lpy,2));
	if (d1<d2){//if point tx,ty is closer to start point than end point, invert path
	    pi=pt.getJava2DPathIterator();
	    Vector segs=new Vector();
	    while (!pi.isDone()){
		type=pi.currentSegment(cds);
		segs.add(new PathSegment(cds,type));
		pi.next();
	    }
	    VPath newPt=new VPath();
	    PathSegment seg1,seg2;
	    //first, move to last point (which becomes first point)
	    seg1=(PathSegment)segs.elementAt(segs.size()-1);
	    if (seg1.getType()==PathIterator.SEG_CUBICTO){
		newPt.jump((long)seg1.cds[4],(long)seg1.cds[5],true);
	    }
	    else if (seg1.getType()==PathIterator.SEG_MOVETO){
		newPt.jump((long)seg1.cds[0],(long)seg1.cds[1],true);
	    }
	    else if (seg1.getType()==PathIterator.SEG_QUADTO){
		newPt.jump((long)seg1.cds[2],(long)seg1.cds[3],true);
	    }
	    else if (seg1.getType()==PathIterator.SEG_LINETO){
		newPt.jump((long)seg1.cds[0],(long)seg1.cds[1],true);
	    }
	    //then process the points in reverse order
	    for (int j=segs.size()-1;j>0;j--){
		seg1=(PathSegment)segs.elementAt(j);
		seg2=(PathSegment)segs.elementAt(j-1);
		if (seg1.getType()==PathIterator.SEG_CUBICTO){
		    if (seg2.getType()==PathIterator.SEG_CUBICTO){newPt.addCbCurve((long)seg2.cds[4],(long)seg2.cds[5],(long)seg1.cds[2],(long)seg1.cds[3],(long)seg1.cds[0],(long)seg1.cds[1],true);}
		    else if (seg2.getType()==PathIterator.SEG_MOVETO){newPt.addCbCurve((long)seg2.cds[0],(long)seg2.cds[1],(long)seg1.cds[2],(long)seg1.cds[3],(long)seg1.cds[0],(long)seg1.cds[1],true);}
		    else if (seg2.getType()==PathIterator.SEG_QUADTO){newPt.addCbCurve((long)seg2.cds[2],(long)seg2.cds[3],(long)seg1.cds[2],(long)seg1.cds[3],(long)seg1.cds[0],(long)seg1.cds[1],true);}
		    else if (seg2.getType()==PathIterator.SEG_LINETO){newPt.addCbCurve((long)seg2.cds[0],(long)seg2.cds[1],(long)seg1.cds[2],(long)seg1.cds[3],(long)seg1.cds[0],(long)seg1.cds[1],true);}
		}
		else if (seg1.getType()==PathIterator.SEG_MOVETO){
		    if (seg2.getType()==PathIterator.SEG_CUBICTO){newPt.jump((long)seg2.cds[4],(long)seg2.cds[5],true);}
		    else if (seg2.getType()==PathIterator.SEG_MOVETO){newPt.jump((long)seg2.cds[0],(long)seg2.cds[1],true);}
		    else if (seg2.getType()==PathIterator.SEG_QUADTO){newPt.jump((long)seg2.cds[2],(long)seg2.cds[3],true);}
		    else if (seg2.getType()==PathIterator.SEG_LINETO){newPt.jump((long)seg2.cds[0],(long)seg2.cds[1],true);}
		}
		else if (seg1.getType()==PathIterator.SEG_QUADTO){
		    if (seg2.getType()==PathIterator.SEG_CUBICTO){newPt.addQdCurve((long)seg2.cds[4],(long)seg2.cds[5],(long)seg1.cds[4],(long)seg1.cds[5],true);}
		    else if (seg2.getType()==PathIterator.SEG_MOVETO){newPt.addQdCurve((long)seg2.cds[0],(long)seg2.cds[1],(long)seg1.cds[0],(long)seg1.cds[1],true);}
		    else if (seg2.getType()==PathIterator.SEG_QUADTO){newPt.addQdCurve((long)seg2.cds[2],(long)seg2.cds[3],(long)seg1.cds[2],(long)seg1.cds[3],true);}
		    else if (seg2.getType()==PathIterator.SEG_LINETO){newPt.addQdCurve((long)seg2.cds[0],(long)seg2.cds[1],(long)seg1.cds[0],(long)seg1.cds[1],true);}
		}
		else if (seg1.getType()==PathIterator.SEG_LINETO){
		    if (seg2.getType()==PathIterator.SEG_CUBICTO){newPt.addSegment((long)seg2.cds[4],(long)seg2.cds[5],true);}
		    else if (seg2.getType()==PathIterator.SEG_MOVETO){newPt.addSegment((long)seg2.cds[0],(long)seg2.cds[1],true);}
		    else if (seg2.getType()==PathIterator.SEG_QUADTO){newPt.addSegment((long)seg2.cds[2],(long)seg2.cds[3],true);}
		    else if (seg2.getType()==PathIterator.SEG_LINETO){newPt.addSegment((long)seg2.cds[0],(long)seg2.cds[1],true);}
		}
	    }
	    return newPt;
	}
	else {return pt;}
    }

    public static LongPoint[] computeVPolygonCoords(long x,long y,long h,float[] vertices){
	if (vertices.length>=2){
 	    LongPoint[] res=new LongPoint[vertices.length/2];
	    for (int i=0;i<res.length;i++){
		res[i]=new LongPoint(x+(long)vertices[2*i],y+(long)vertices[2*i+1]);
	    }
	    return res;
// 	    LongPoint[] res=new LongPoint[4];
// 	    res[0]=new LongPoint(x+h,y);
// 	    res[1]=new LongPoint(x,y+h);
// 	    res[2]=new LongPoint(x-h,y);
// 	    res[3]=new LongPoint(x,y-h);
// 	    return res;
	}
	else return null;
    }

    /*returns the north-most glyph (greatest vy coordinate) */
    public static Glyph getNorthMostGlyph(Vector glyphs){
	if (glyphs!=null && glyphs.size()>0){
	    Glyph res=(Glyph)glyphs.firstElement();
	    for (int i=1;i<glyphs.size();i++){
		if (((Glyph)glyphs.elementAt(i)).vy>res.vy){res=(Glyph)glyphs.elementAt(i);}
	    }
	    return res;
	}
	else return null;
    }

    /*returns the south-most glyph (lowest vy coordinate) */
    public static Glyph getSouthMostGlyph(Vector glyphs){
	if (glyphs!=null && glyphs.size()>0){
	    Glyph res=(Glyph)glyphs.firstElement();
	    for (int i=1;i<glyphs.size();i++){
		if (((Glyph)glyphs.elementAt(i)).vy<res.vy){res=(Glyph)glyphs.elementAt(i);}
	    }
	    return res;
	}
	else return null;
    }

    /**creates a VTriangle whose orientation matches the direction of the line passing by both argument points (direction is from p1 to p2) - triangle is created at coordinates of p2*/
    public static VTriangleOr createPathArrowHead(LongPoint p1,LongPoint p2,VTriangleOr t){
	return createPathArrowHead(p1.x,p1.y,p2.x,p2.y,t);
    }
    
    /**creates a VTriangle whose orientation matches the direction of the line passing by both argument points (direction is from p2 to p1) - triangle is created at coordinates of p2*/
    public static VTriangleOr createPathArrowHead(double p1x,double p1y,double p2x,double p2y,VTriangleOr t){
	Point2D deltaor=computeStepValue(p1x,p1y,p2x,p2y);
	double angle=0;
	if (deltaor.getX()==0){
	    angle=0;
	    if (deltaor.getY()<0){angle=Math.PI;}
	}
	else {
	    angle=Math.atan(deltaor.getY()/deltaor.getX());
	    //align with VTM's system coordinates (a VTriangle's "head" points to the north when orient=0, not to the east)
	    if (deltaor.getX()<0){angle+=Math.PI/2;}   //comes from angle+PI-PI/2 (first PI due to the fact that ddx is <0 and the use of the arctan function - otherwise, head points in the opposite direction)
	    else {angle-=Math.PI/2;}
	}
	if (t!=null){
	    t.moveTo((long)p2x,(long)p2y);
	    t.orientTo((float)angle);
	    return t;
	}
	else {return new VTriangleOr((long)p2x,(long)p2y,0,ARROW_HEAD_SIZE,ConfigManager.propertyColorB,(float)angle);}
    }

    public static Point2D computeStepValue(LongPoint p1,LongPoint p2){
	int signOfX=(p2.x>=p1.x) ? 1 : -1 ;
	int signOfY=(p2.y>=p1.y) ? 1 : -1 ;
	double ddx,ddy;
	if (p2.x==p1.x){//vertical direction (ar is infinite) - to prevent division by 0
	    ddx=0;
	    ddy=signOfY;
	}
	else {
	    double ar=(p2.y-p1.y)/((double)(p2.x-p1.x));
	    if (Math.abs(ar)>1.0f){
		ddx=signOfX/Math.abs(ar);
		ddy=signOfY;
	    }
	    else {
		ddx=signOfX;
		ddy=signOfY*Math.abs(ar);
	    }
	}
	return new Point2D.Double(ddx,ddy);
    }

    public static Point2D computeStepValue(double p1x,double p1y,double p2x,double p2y){
	int signOfX=(p2x>=p1x) ? 1 : -1 ;
	int signOfY=(p2y>=p1y) ? 1 : -1 ;
	double ddx,ddy;
	if (p2x==p1x){//vertical direction (ar is infinite) - to prevent division by 0
	    ddx=0;
	    ddy=signOfY;
	}
	else {
	    double ar=(p2y-p1y)/((double)(p2x-p1x));
	    if (Math.abs(ar)>1.0f){
		ddx=signOfX/Math.abs(ar);
		ddy=signOfY;
	    }
	    else {
		ddx=signOfX;
		ddy=signOfY*Math.abs(ar);
	    }
	}
	return new Point2D.Double(ddx,ddy);
    }

    protected Glyph getNodeShape(INode n,StyleInfo si){//n should be an IResource or ILiteral, si a StyleInfoR or a StyleInfoL
	Object shape=null;
	URL icon=null;
	if (si instanceof StyleInfoR){
	    shape=((StyleInfoR)si).getShape();
	    icon=((StyleInfoR)si).getIcon();
	}
	else {
	    shape=((StyleInfoL)si).getShape();
	    icon=((StyleInfoL)si).getIcon();
	}
	if (shape==null){
	    if (icon!=null){
		if (n instanceof IResource){//shaping an IResource
		    VImage vim=null;
		    if (icon.toString().equals(GraphStylesheet._gssFetch)){//dynamic icon
			try {
			    URL iconURL=new URL(((IResource)n).getIdentity());
			    if (iconURL!=null && application.gssMngr.storeIcon(iconURL)){
				//storeIcon() returns true only if the ImageIcon could be retrieved, instantiated and stored
				vim=new VImage(n.getGlyph().vx,n.getGlyph().vy,0,application.gssMngr.getIcon(iconURL).getImage());
				vim.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
			    }
			    else {//assign default to shape and go to next test (shape instanceof Integer)
				if (GraphStylesheet.DEBUG_GSS){System.err.println("Error: there does not seem to be any icon at the following URI :"+iconURL);}
				shape=GraphStylesheet.DEFAULT_RESOURCE_SHAPE;
			    }
			}
			catch (MalformedURLException mue){if (GraphStylesheet.DEBUG_GSS){System.err.println("Error:RDFLoader.getNodeShape(): malformed icon URI: "+((IResource)n).getIdentity());mue.printStackTrace();}}
		    }
		    else {
			vim=new VImage(n.getGlyph().vx,n.getGlyph().vy,0,application.gssMngr.getIcon(icon).getImage());
			vim.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
		    }
		    if (vim!=null){
			if (n.getGlyph() instanceof RectangularShape){
			    if (vim.getWidth()>=vim.getHeight()){
				vim.setWidth(((RectangularShape)n.getGlyph()).getWidth());
			    }
			    else {
				vim.setHeight(((RectangularShape)n.getGlyph()).getHeight());
			    }
			}
			else {
			    if (vim.getWidth()>=vim.getHeight()){
				vim.setWidth(Math.round(n.getGlyph().getSize()));
			    }
			    else {
				vim.setHeight(Math.round(n.getGlyph().getSize()));
			    }
			}
			return vim;
		    }
		}
		else {//shaping an ILiteral
		    VImage vim=new VImage(n.getGlyph().vx,n.getGlyph().vy,0,application.gssMngr.getIcon(icon).getImage());
		    vim.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
		    if (n.getGlyph() instanceof RectangularShape){
			if (vim.getWidth()>=vim.getHeight()){
			    vim.setWidth(((RectangularShape)n.getGlyph()).getWidth());
			}
			else {
			    vim.setHeight(((RectangularShape)n.getGlyph()).getHeight());
			}
		    }
		    else {
			if (vim.getWidth()>=vim.getHeight()){
			    vim.setWidth(Math.round(n.getGlyph().getSize()));
			}
			else {
			    vim.setHeight(Math.round(n.getGlyph().getSize()));
			}
		    }
		    return vim;
		}
	    }
	    else {
		if (n instanceof IResource){shape=GraphStylesheet.DEFAULT_RESOURCE_SHAPE;}
		else {shape=GraphStylesheet.DEFAULT_LITERAL_SHAPE;}
	    }//assign default to shape and go to next test (shape instanceof Integer)
	}
	if (shape!=null && shape instanceof Integer){
	    if (shape.equals(Style.ELLIPSE)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VEllipse(n.getGlyph().vx,n.getGlyph().vy,0,w,h,n.getGlyph().getColor());
		}
		else {
		    long s=Math.round(n.getGlyph().getSize());
		    return new VEllipse(n.getGlyph().vx,n.getGlyph().vy,0,s,s,n.getGlyph().getColor());
		}
	    }
	    else if (shape.equals(Style.RECTANGLE)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VRectangle(n.getGlyph().vx,n.getGlyph().vy,0,w,h,n.getGlyph().getColor());
		}
		else {
		    long s=Math.round(n.getGlyph().getSize());
		    return new VRectangle(n.getGlyph().vx,n.getGlyph().vy,0,s,s,n.getGlyph().getColor());
		}
	    }
	    else if (shape.equals(Style.CIRCLE)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VCircle(n.getGlyph().vx,n.getGlyph().vy,0,(w > h) ? h : w,n.getGlyph().getColor());
		}
		else {
		    return new VCircle(n.getGlyph().vx,n.getGlyph().vy,0,Math.round(n.getGlyph().getSize()),n.getGlyph().getColor());
		}
	    }
	    else if (shape.equals(Style.DIAMOND)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VDiamond(n.getGlyph().vx,n.getGlyph().vy,0,(w > h) ? h : w,n.getGlyph().getColor());
		}
		else {
		    return new VDiamond(n.getGlyph().vx,n.getGlyph().vy,0,Math.round(n.getGlyph().getSize()),n.getGlyph().getColor());
		}
	    }
	    else if (shape.equals(Style.OCTAGON)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VOctagon(n.getGlyph().vx,n.getGlyph().vy,0,(w > h) ? h : w,n.getGlyph().getColor());
		}
		else {
		    return new VOctagon(n.getGlyph().vx,n.getGlyph().vy,0,Math.round(n.getGlyph().getSize()),n.getGlyph().getColor());
		}
	    }
	    else if (shape.equals(Style.ROUND_RECTANGLE)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VRoundRect(n.getGlyph().vx,n.getGlyph().vy,0,w,h,n.getGlyph().getColor(),Math.round(SVGReader.RRARCR*Math.min(w,h)),Math.round(SVGReader.RRARCR*Math.min(w,h)));
		}
		else {
		    long s=Math.round(n.getGlyph().getSize());
		    return new VRoundRect(n.getGlyph().vx,n.getGlyph().vy,0,s,s,n.getGlyph().getColor(),Math.round(SVGReader.RRARCR*s),Math.round(SVGReader.RRARCR*s));
		}
	    }
	    else if (shape.equals(Style.TRIANGLEN)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VTriangle(n.getGlyph().vx,n.getGlyph().vy,0,(w > h) ? h : w,n.getGlyph().getColor());
		}
		else {
		    return new VTriangle(n.getGlyph().vx,n.getGlyph().vy,0,Math.round(n.getGlyph().getSize()),n.getGlyph().getColor());
		}
	    }
	    else if (shape.equals(Style.TRIANGLES)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VTriangleOr(n.getGlyph().vx,n.getGlyph().vy,0,(w > h) ? h : w,n.getGlyph().getColor(),(float)Math.PI);
		}
		else {
		    return new VTriangleOr(n.getGlyph().vx,n.getGlyph().vy,0,Math.round(n.getGlyph().getSize()),n.getGlyph().getColor(),(float)Math.PI);
		}
	    }
	    else if (shape.equals(Style.TRIANGLEE)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VTriangleOr(n.getGlyph().vx,n.getGlyph().vy,0,(w > h) ? h : w,n.getGlyph().getColor(),(float)-Math.PI/2.0f);
		}
		else {
		    return new VTriangleOr(n.getGlyph().vx,n.getGlyph().vy,0,Math.round(n.getGlyph().getSize()),n.getGlyph().getColor(),(float)-Math.PI/2.0f);
		}
	    }
	    else if (shape.equals(Style.TRIANGLEW)){
		if (n.getGlyph() instanceof RectangularShape){
		    long w=((RectangularShape)n.getGlyph()).getWidth();
		    long h=((RectangularShape)n.getGlyph()).getHeight();
		    return new VTriangleOr(n.getGlyph().vx,n.getGlyph().vy,0,(w > h) ? h : w,n.getGlyph().getColor(),(float)Math.PI/2.0f);
		}
		else {
		    return new VTriangleOr(n.getGlyph().vx,n.getGlyph().vy,0,Math.round(n.getGlyph().getSize()),n.getGlyph().getColor(),(float)Math.PI/2.0f);
		}
	    }
	    else {
		System.err.println("Error: GeometryManager.getNodeShape(): requested shape type unknown: "+shape.toString());
		return null;
	    }
	}
	else if (shape!=null && shape instanceof CustomShape){
	    float[] vertices=((CustomShape)shape).getVertices();
	    Float orient=((CustomShape)shape).getOrientation();
	    if (n.getGlyph() instanceof RectangularShape){
		long w=((RectangularShape)n.getGlyph()).getWidth();
		long h=((RectangularShape)n.getGlyph()).getHeight();
		return new VShape(n.getGlyph().vx,n.getGlyph().vy,0,(w > h) ? h : w,vertices,n.getGlyph().getColor(),(orient!=null) ? orient.floatValue(): 0.0f);
	    }
	    else {
		return new VShape(n.getGlyph().vx,n.getGlyph().vy,0,Math.round(n.getGlyph().getSize()),vertices,n.getGlyph().getColor(),(orient!=null) ? orient.floatValue(): 0.0f);
	    }
	}
	else if (shape!=null && shape instanceof CustomPolygon){
	    float[] vertices=((CustomPolygon)shape).getVertices();
	    if (n.getGlyph() instanceof RectangularShape){
		long w=((RectangularShape)n.getGlyph()).getWidth();
		long h=((RectangularShape)n.getGlyph()).getHeight();
		VPolygon res=new VPolygon(GeometryManager.computeVPolygonCoords(n.getGlyph().vx,n.getGlyph().vy,(w > h) ? h : w,vertices),n.getGlyph().getColor());
		return res;
	    }
	    else {
		VPolygon res=new VPolygon(GeometryManager.computeVPolygonCoords(n.getGlyph().vx,n.getGlyph().vy,Math.round(n.getGlyph().getSize()),vertices),n.getGlyph().getColor());
		return res;
	    }
	}
	else {//for robustness (should not happen)
	    System.err.println("Error: GeometryManager.getNodeShape(): requested shape type unknown: "+shape.toString());
	    return null;
	}
    }

//     /*(un)set distortion lens*/
//     void setLens(boolean b){
// 	if (b){
// 	    if (lensID == null){
// 		lensID = (application.mView.setLens(new FSCenteredInverseCosineLens(1.0f, LENS_R1, LENS_R2))).getID();
// 		application.vsm.animator.createLensAnimation(500, AnimManager.LS_MM_LIN, new Float(1.0f), lensID, false);
// 	    }
// 	}
// 	else {
// 	    if (lensID != null){
// 		application.vsm.animator.createLensAnimation(500, AnimManager.LS_MM_LIN, new Float(-1.0f), lensID, true);
// 		lensID = null;
// 	    }
// 	}
//     }

//     /*Lens listener interface*/
//     public void lensLargerThanView(View v){
// 	v.setLens(null);
// 	lensID = null;
// 	application.cmp.lensMn.setSelected(false);
//     }

}
