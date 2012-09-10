/*   FILE: PropResizer.java
 *   DATE OF CREATION:   12/08/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 23 09:32:02 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;

import java.util.Vector;

import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.RectangleNR;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.VPolygon;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VEllipse;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VTriangleOr;
import com.xerox.VTM.glyphs.VPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpace;
import net.claribole.zvtm.glyphs.GlyphUtils;

/*Class that contains resizing handles (small black boxes) that are used to modify the geometry of a property's path + methods to update*/

class PropResizer extends Resizer {

    ControlPoint[] cps;  //list of control points used to edit this path
    VPath path;          //the main path, made of bezier curves
    IProperty prop;

    PropResizer(IProperty p){
	this.prop=p;
	//destroy the path's head so that it does not conflict with the handles (it overlaps with END_POINT) - besides
	//we do not want ot compute it every time, but just when we finish the resizing operation
	Glyph head=p.getGlyphHead();
	if (head!=null){
	    Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).destroyGlyph(head);
	    prop.setGlyphHead(null);
	}
	path=(VPath)prop.getGlyph();
	cps=this.constructPathResizer((VPath)prop.getGlyph());
    }

    void updateMainGlyph(Glyph g){//don't care about g (for compatibility purposes with other resizers)
	//first check that start/end points are on the boundary of the subject/object node
	//begin with start point
	Glyph el1=prop.getSubject().getGlyph();
	Shape el2=GlyphUtils.getJava2DShape(el1);
	Point2D newPoint=new Point2D.Double(cps[0].handle.vx,cps[0].handle.vy);
	Point2D delta;
	if (el2.contains(newPoint)){//start point is inside subject - walk outbounds
	    if (el1 instanceof VPolygon){
		delta=GeometryManager.computeStepValue(((VPolygon)el1).getCentroid(),new LongPoint(cps[0].handle.vx,cps[0].handle.vy));
	    }
	    else {
		delta=GeometryManager.computeStepValue(new LongPoint(el1.vx,el1.vy),new LongPoint(cps[0].handle.vx,cps[0].handle.vy));
	    }
	    while (el2.contains(newPoint)){
		newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
	    }
	    cps[0].handle.moveTo(Math.round(newPoint.getX()),Math.round(newPoint.getY()));
	}
	else {//start point is outside subject - walk inbound
	    if (el1 instanceof VPolygon){
		delta=GeometryManager.computeStepValue(new LongPoint(cps[0].handle.vx,cps[0].handle.vy),((VPolygon)el1).getCentroid());
	    }
	    else {
		delta=GeometryManager.computeStepValue(new LongPoint(cps[0].handle.vx,cps[0].handle.vy),new LongPoint(el1.vx,el1.vy));
	    }
	    while (!el2.contains(newPoint)){
		newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
	    }
	    cps[0].handle.moveTo(Math.round(newPoint.getX()),Math.round(newPoint.getY()));
	}
	cps[0].update();  //update the segment(s) linked to this handle
	//do the same thing with end point
	newPoint=new Point2D.Double(cps[cps.length-1].handle.vx,cps[cps.length-1].handle.vy);
	//distinction between resource and literal is no longer relevant as both can have any shape
// 	if (prop.getObject() instanceof IResource){
	if (prop.isLaidOutInTableForm()){
	    Vector v=prop.getSubject().getOutgoingPredicates();//get all properties that might potentially belong to the table
	    IProperty tmpP;
	    Vector cells=new Vector();
	    for (int i=0;i<v.size();i++){//retrieve all properties in this table form
		tmpP=(IProperty)v.elementAt(i);
		if (tmpP.getGlyph()==prop.getGlyph()){//a property is in the same table form of it shares the same edge (VPath)
		    cells.add(tmpP.getTableCellGlyph());
		}
	    }//then select the first cell (highest row) to have the table's incoming edge attached
// 	    el1=GeometryManager.getNorthMostGlyph(cells);
// 	    el2=GlyphUtils.getJava2DShape(el1);
	    /*the code below replaces the above 2 lines: it considers the whole property column as the rectangular border to which the edge should be attached, instead of considering only the first cell*/
	    VRectangle el1a=(VRectangle)GeometryManager.getNorthMostGlyph(cells);
	    VRectangle el1b=(VRectangle)GeometryManager.getSouthMostGlyph(cells);
 	    el1=new VRectangle(el1a.vx,(el1a.vy+el1b.vy)/2,0,el1a.getWidth(),(Math.abs(el1b.vy-el1a.vy)+el1a.getHeight()+el1b.getHeight())/2,Color.black);
	    el2=GlyphUtils.getJava2DShape(el1);
	}
	else {
	    el1=prop.getObject().getGlyph();
	    el2=GlyphUtils.getJava2DShape(el1);
	}
	if (el2.contains(newPoint)){//end point is inside subject - walk outbounds
	    if (el1 instanceof VPolygon){
		delta=GeometryManager.computeStepValue(((VPolygon)el1).getCentroid(),new LongPoint(cps[cps.length-1].handle.vx,cps[cps.length-1].handle.vy));
	    }
	    else {
		delta=GeometryManager.computeStepValue(new LongPoint(el1.vx,el1.vy),new LongPoint(cps[cps.length-1].handle.vx,cps[cps.length-1].handle.vy));
	    }
	    while (el2.contains(newPoint)){
		newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
	    }
	    cps[cps.length-1].handle.moveTo(Math.round(newPoint.getX()),Math.round(newPoint.getY()));
	}
	else {//start point is outside subject - walk inbound
	    if (el1 instanceof VPolygon){
		delta=GeometryManager.computeStepValue(new LongPoint(cps[cps.length-1].handle.vx,cps[cps.length-1].handle.vy),((VPolygon)el1).getCentroid());
	    }
	    else {
		delta=GeometryManager.computeStepValue(new LongPoint(cps[cps.length-1].handle.vx,cps[cps.length-1].handle.vy),new LongPoint(el1.vx,el1.vy));
	    }
	    while (!el2.contains(newPoint)){
		newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
	    }
	    cps[cps.length-1].handle.moveTo(Math.round(newPoint.getX()),Math.round(newPoint.getY()));
	}
	//distinction between resource and literal is no longer relevant as both can have any shape
	cps[cps.length-1].update();  //update the segment(s) linked to this handle
	//then update the VPath
	path.resetPath();
	for (int i=0;i<cps.length;i++){
	    switch (cps[i].type){
	    case ControlPoint.CURVE_POINT:{
		if (cps[i-1].type==ControlPoint.CUBIC_CURVE_CP2){//construct a cubic curve
		    path.addCbCurve(cps[i].handle.vx,cps[i].handle.vy,cps[i-2].handle.vx,cps[i-2].handle.vy,cps[i-1].handle.vx,cps[i-1].handle.vy,true);
		}
		else if (cps[i-1].type==ControlPoint.QUAD_CURVE_CP){//construct a quadratic curve
		    path.addQdCurve(cps[i].handle.vx,cps[i].handle.vy,cps[i-1].handle.vx,cps[i-1].handle.vy,true);
		}
		else if ((cps[i-1].type==ControlPoint.CURVE_POINT) || (cps[i-1].type==ControlPoint.START_POINT)){//construct a segment
		    path.addSegment(cps[i].handle.vx,cps[i].handle.vy,true);
		}
		break;
	    }
	    case ControlPoint.QUAD_CURVE_CP:{break;}
	    case ControlPoint.CUBIC_CURVE_CP1:{break;}
	    case ControlPoint.CUBIC_CURVE_CP2:{break;}
	    case ControlPoint.START_POINT:{
		path.jump(cps[i].handle.vx,cps[i].handle.vy,true);
		break;
	    }
	    case ControlPoint.END_POINT:{
		if (cps[i-1].type==ControlPoint.CUBIC_CURVE_CP2){//construct a cubic curve
		    path.addCbCurve(cps[i].handle.vx,cps[i].handle.vy,cps[i-2].handle.vx,cps[i-2].handle.vy,cps[i-1].handle.vx,cps[i-1].handle.vy,true);
		}
		else if (cps[i-1].type==ControlPoint.QUAD_CURVE_CP){//construct a quadratic curve
		    path.addQdCurve(cps[i].handle.vx,cps[i].handle.vy,cps[i-1].handle.vx,cps[i-1].handle.vy,true);
		}
		else if ((cps[i-1].type==ControlPoint.CURVE_POINT) || (cps[i-1].type==ControlPoint.START_POINT)){//construct a segment
		    path.addSegment(cps[i].handle.vx,cps[i].handle.vy,true);
		}
		break;
	    }
	    }
	}

    }

    void updateHandles(){}//for compatibility purposes

    void destroy(){//have to destroy all handles and segments - and put the path head (VTriangle) back
	//compute angle for triangle, from last segment's orientation
	LongPoint lp1=cps[cps.length-1].prevHandle.getLocation();
	LongPoint lp2=cps[cps.length-1].handle.getLocation();
	if (!prop.isLaidOutInTableForm()){
	    VTriangleOr tr=GeometryManager.createPathArrowHead(lp1,lp2,null);
	    Editor.vsm.addGlyph(tr,Editor.mainVirtualSpace);
	    tr.setColor(ConfigManager.colors[prop.strokeIndex]);
	    prop.setGlyphHead(tr);
	    //adapt arrow head size to edge's thickness (otherwise it might be too small, or even not visible)
	    if (!prop.isSelected() && prop.getGlyph().getStrokeWidth()>2.0f){
		tr.reSize(prop.getGlyph().getStrokeWidth()/2.0f);
	    }//selecting an edge temporarily increases its thickness by 2, take that into account
	    else if (prop.isSelected() && prop.getGlyph().getStrokeWidth()>4.0f){
		tr.reSize((prop.getGlyph().getStrokeWidth()-2.0f)/2.0f);
	    }
	}
	VirtualSpace vs=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace);
	for (int i=0;i<cps.length-1;i++){
	    vs.destroyGlyph(cps[i].handle); //destroy handle associated to the glyph
	    vs.destroyGlyph(cps[i].s2);     //plus second segment
	}
	vs.destroyGlyph(cps[cps.length-1].handle); //for last glyph control point only destroy handle (everything else has been taken care of)
    }

    ControlPoint[] constructPathResizer(VPath p){
	PathIterator pi=p.getJava2DPathIterator();
	Vector ctrlPoints=new Vector();
	Vector segments=new Vector();
	float[] cds=new float[6];
	int segType;
	LongPoint p1=new LongPoint(0,0);
	LongPoint p2,p3,p4;
	RectangleNR r1,r2,r3;
	VSegment s1,s2,s3;
	ControlPoint rememberLastPoint=null;
	ControlPoint newPoint=null;
	while (!pi.isDone()){
	    segType=pi.currentSegment(cds);
	    if (segType==PathIterator.SEG_CUBICTO){//all paths generated by graphviz/dot are made only of cubic curves
		//should encounter this one often
		p2=new LongPoint((long)cds[4],(long)-cds[5]);  //curve's end point
		p4=new LongPoint((long)cds[2],(long)-cds[3]);  //second control point
		p3=new LongPoint((long)cds[0],(long)-cds[1]);  //first control point
		r1=new RectangleNR(p2.x,p2.y,0,4,4,Color.black);//curve's end point
		r2=new RectangleNR(p3.x,p3.y,0,4,4,Color.red);  //first control point
		r3=new RectangleNR(p4.x,p4.y,0,4,4,Color.red);  //second control point
		Editor.vsm.addGlyph(r1,Editor.mainVirtualSpace);
		Editor.vsm.addGlyph(r2,Editor.mainVirtualSpace);
		Editor.vsm.addGlyph(r3,Editor.mainVirtualSpace);

		s1=new VSegment((p1.x+p3.x)/2,(p1.y+p3.y)/2,0,(p3.x-p1.x)/2,(p1.y-p3.y)/2,Color.red);
		s2=new VSegment((p3.x+p4.x)/2,(p3.y+p4.y)/2,0,(p4.x-p3.x)/2,(p3.y-p4.y)/2,Color.red);
		s3=new VSegment((p4.x+p2.x)/2,(p4.y+p2.y)/2,0,(p2.x-p4.x)/2,(p4.y-p2.y)/2,Color.red);
		Editor.vsm.addGlyph(s1,Editor.mainVirtualSpace);
		Editor.vsm.addGlyph(s2,Editor.mainVirtualSpace);
		Editor.vsm.addGlyph(s3,Editor.mainVirtualSpace);

		rememberLastPoint.setSecondSegment(s1,r2);
		newPoint=new ControlPoint(r2,rememberLastPoint.handle,s1,ControlPoint.CUBIC_CURVE_CP1,this);
		rememberLastPoint=newPoint;
		ctrlPoints.add(rememberLastPoint);
		rememberLastPoint.setSecondSegment(s2,r3);
		newPoint=new ControlPoint(r3,rememberLastPoint.handle,s2,ControlPoint.CUBIC_CURVE_CP2,this);
		rememberLastPoint=newPoint;
		ctrlPoints.add(rememberLastPoint);
		rememberLastPoint.setSecondSegment(s3,r1);
		newPoint=new ControlPoint(r1,rememberLastPoint.handle,s3,ControlPoint.CURVE_POINT,this);
		rememberLastPoint=newPoint;
		ctrlPoints.add(rememberLastPoint);

		segments.add(s1);segments.add(s2);segments.add(s3);
		p1.setLocation(p2.x,p2.y); //prepare curve's start point for next iteration
	    }
	    else if (segType==PathIterator.SEG_QUADTO){//user-made paths are made of quad curves and lines (first and last)
		//should encounter them equally often
		p2=new LongPoint((long)cds[2],(long)-cds[3]);  //curve s end point
		p3=new LongPoint((long)cds[0],(long)-cds[1]);  //first control point
		r1=new RectangleNR(p2.x,p2.y,0,4,4,Color.black);//curve s end point
		r2=new RectangleNR(p3.x,p3.y,0,4,4,Color.red);  //first control point
		Editor.vsm.addGlyph(r1,Editor.mainVirtualSpace);
		Editor.vsm.addGlyph(r2,Editor.mainVirtualSpace);

		s1=new VSegment((p1.x+p3.x)/2,(p1.y+p3.y)/2,0,(p3.x-p1.x)/2,(p1.y-p3.y)/2,Color.red);
		s2=new VSegment((p3.x+p2.x)/2,(p3.y+p2.y)/2,0,(p2.x-p3.x)/2,(p3.y-p2.y)/2,Color.red);
		Editor.vsm.addGlyph(s1,Editor.mainVirtualSpace);
		Editor.vsm.addGlyph(s2,Editor.mainVirtualSpace);

		rememberLastPoint.setSecondSegment(s1,r2);
		newPoint=new ControlPoint(r2,rememberLastPoint.handle,s1,ControlPoint.QUAD_CURVE_CP,this);
		rememberLastPoint=newPoint;
		ctrlPoints.add(rememberLastPoint);
		rememberLastPoint.setSecondSegment(s2,r1);
		newPoint=new ControlPoint(r1,rememberLastPoint.handle,s2,ControlPoint.CURVE_POINT,this);
		rememberLastPoint=newPoint;
		ctrlPoints.add(rememberLastPoint);
		segments.add(s1);segments.add(s2);
		p1.setLocation(p2.x,p2.y); //prepare curve's start point for next iteration
	    }
	    else if (segType==PathIterator.SEG_LINETO){//user-made paths are made of quad curves and lines (first and last)
		//should encounter them less often
		p2=new LongPoint((long)cds[0],(long)-cds[1]);  //curve's end point
		r1=new RectangleNR(p2.x,p2.y,0,4,4,Color.black);
		Editor.vsm.addGlyph(r1,Editor.mainVirtualSpace);

		s1=new VSegment((p1.x+p2.x)/2,(p1.y+p2.y)/2,0,(p2.x-p1.x)/2,(p1.y-p2.y)/2,Color.red);
		Editor.vsm.addGlyph(s1,Editor.mainVirtualSpace);

		rememberLastPoint.setSecondSegment(s1,r1);
		newPoint=new ControlPoint(r1,rememberLastPoint.handle,s1,ControlPoint.CURVE_POINT,this);
		rememberLastPoint=newPoint;
		ctrlPoints.add(rememberLastPoint);
		segments.add(s1);
		p1.setLocation(p2.x,p2.y); //prepare curve's start point for next iteration
	    }
	    else if (segType==PathIterator.SEG_MOVETO){//for the first segement, points to the origin
		//should only occur once in the iteration (first)
		p1.setLocation((long)cds[0],(long)-cds[1]); //prepare curve's start point for next iteration
		r1=new RectangleNR(p1.x,p1.y,0,4,4,Color.black);
		Editor.vsm.addGlyph(r1,Editor.mainVirtualSpace);
		rememberLastPoint=new ControlPoint(r1,null,null,ControlPoint.START_POINT,this);
		ctrlPoints.add(rememberLastPoint);
	    }
	    pi.next();
	}
	rememberLastPoint.setType(ControlPoint.END_POINT);  //change type of last point (we don't know it before exiting the loop)
	ControlPoint[] res=new ControlPoint[ctrlPoints.size()];  //convert the vector in an array
	for (int i=0;i<ctrlPoints.size();i++){
	    res[i]=(ControlPoint)ctrlPoints.elementAt(i);
	}
	return res;
    }

    Glyph getMainGlyph(){return path;}


}
