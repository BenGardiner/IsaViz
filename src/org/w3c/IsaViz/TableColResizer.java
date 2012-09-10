/*   FILE: TableColResizer.java
 *   DATE OF CREATION:   Thu Jun 12 17:28:25 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Jul 10 12:52:34 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.awt.Color;
import java.util.Arrays;
import java.util.Vector;
import com.xerox.VTM.glyphs.RectangularShape;
import com.xerox.VTM.glyphs.RectangleNR;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VCircle;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.engine.VirtualSpace;

/*Class that contains resizing handles (small black boxes) that are used to modify the geometry of a table column's rectangular glyphs + methods to update*/

class TableColResizer extends Resizer {

    protected static short LEFT_COLUMN=0;
    protected static short RIGHT_COLUMN=1;

    short column;

    VRectangle g0;  //cell in which the user clicked

    Glyph r1;  //East handle
    Glyph r3;  //West handle

    VRectangle[] propertyCells;
    VRectangle[] objectCells;

    /*used to tell the GeometryManager what properties are in the table to build the ISVGeom command (for undo) - destroy right after getting it*/
    Vector propertiesInTable;

    TableColResizer(IResource r,short whichCol){
	column=whichCol;
	//we know there is only one incoming property as this is mandatory for a resource to ba laid out in table form
	IProperty p=(IProperty)r.getIncomingPredicates().firstElement();
	Vector v=p.getSubject().getOutgoingPredicates();//get all properties that might potentially belong to the table
	IProperty tmpP;
	Vector leftCells=new Vector();
	Vector rightCells=new Vector();
	propertiesInTable=new Vector();
	for (int i=0;i<v.size();i++){//retrieve all properties in this table form
	    tmpP=(IProperty)v.elementAt(i);
	    if (tmpP.getGlyph()==p.getGlyph()){//a property is in the same table form if it shares the same edge (VPath)
		leftCells.add(tmpP.getTableCellGlyph());
		rightCells.add(tmpP.getObject().getGlyph());
 		propertiesInTable.add(tmpP);
	    }
	}
	propertyCells=new VRectangle[leftCells.size()];
	objectCells=new VRectangle[rightCells.size()];
	for (int i=0;i<propertyCells.length;i++){
	    propertyCells[i]=(VRectangle)leftCells.elementAt(i);
	    objectCells[i]=(VRectangle)rightCells.elementAt(i);
	}
	//sort glyphs in each table based on their Y coordinate (highest one first)
	Arrays.sort(propertyCells,new VerticalPosComparator());
	Arrays.sort(objectCells,new VerticalPosComparator());
	g0=(VRectangle)r.getGlyph();
	createHandles();
    }

    TableColResizer(ILiteral l,short whichCol){
	column=whichCol;
	IProperty p=l.getIncomingPredicate();
	Vector v=p.getSubject().getOutgoingPredicates();//get all properties that might potentially belong to the table
	IProperty tmpP;
	Vector leftCells=new Vector();
	Vector rightCells=new Vector();
	propertiesInTable=new Vector();
	for (int i=0;i<v.size();i++){//retrieve all properties in this table form
	    tmpP=(IProperty)v.elementAt(i);
	    if (tmpP.getGlyph()==p.getGlyph()){//a property is in the same table form if it shares the same edge (VPath)
		leftCells.add(tmpP.getTableCellGlyph());
		rightCells.add(tmpP.getObject().getGlyph());
		propertiesInTable.add(tmpP);
	    }
	}
	propertyCells=new VRectangle[leftCells.size()];
	objectCells=new VRectangle[rightCells.size()];
	for (int i=0;i<propertyCells.length;i++){
	    propertyCells[i]=(VRectangle)leftCells.elementAt(i);
	    objectCells[i]=(VRectangle)rightCells.elementAt(i);
	}
	//sort glyphs in each table based on their Y coordinate (highest one first)
	Arrays.sort(propertyCells,new VerticalPosComparator());
	Arrays.sort(objectCells,new VerticalPosComparator());
	g0=(VRectangle)l.getGlyph();
	createHandles();
    }

    TableColResizer(IProperty p,short whichCol){
	column=whichCol;
	Vector v=p.getSubject().getOutgoingPredicates();//get all properties that might potentially belong to the table
	IProperty tmpP;
	Vector leftCells=new Vector();
	Vector rightCells=new Vector();
	propertiesInTable=new Vector();
	for (int i=0;i<v.size();i++){//retrieve all properties in this table form
	    tmpP=(IProperty)v.elementAt(i);
	    if (tmpP.getGlyph()==p.getGlyph()){//a property is in the same table form if it shares the same edge (VPath)
		leftCells.add(tmpP.getTableCellGlyph());
		rightCells.add(tmpP.getObject().getGlyph());
		propertiesInTable.add(tmpP);
	    }
	}
	propertyCells=new VRectangle[leftCells.size()];
	objectCells=new VRectangle[rightCells.size()];
	for (int i=0;i<propertyCells.length;i++){
	    propertyCells[i]=(VRectangle)leftCells.elementAt(i);
	    objectCells[i]=(VRectangle)rightCells.elementAt(i);
	}
	//sort glyphs in each table based on their Y coordinate (highest one first)
	Arrays.sort(propertyCells,new VerticalPosComparator());
	Arrays.sort(objectCells,new VerticalPosComparator());
	g0=(VRectangle)p.getTableCellGlyph();
	createHandles();
    }

    protected Vector getPropertiesInTable(){
	Vector res=propertiesInTable;
	propertiesInTable=null;
	return res;
    }

    private void createHandles(){
	if (column==RIGHT_COLUMN){
	    r1=new RectangleNR(objectCells[0].vx+objectCells[0].getWidth(),(objectCells[0].vy+objectCells[objectCells.length-1].vy)/2,0,4,4,Color.black);
	    Editor.vsm.addGlyph(r1,Editor.mainVirtualSpace);r1.setType("rszt");  //ReSiZe Table
	}
	else {//column==LEFT_COLUMN
	    r3=new RectangleNR(propertyCells[0].vx-propertyCells[0].getWidth(),(propertyCells[0].vy+propertyCells[propertyCells.length-1].vy)/2,0,4,4,Color.black);
	    Editor.vsm.addGlyph(r3,Editor.mainVirtualSpace);r3.setType("rszt");
	}
    }

    void updateMainGlyph(Glyph g){//g should be a handle (small black box)
	if (g==r3){
	    //column is necessarily LEFT_COLUMN
	    long halfDelta=(g0.vx-g0.getWidth()-g.vx)/2;
	    long newWidth=g0.getWidth()+halfDelta;
	    if (newWidth>0){
		synchronized(this){
		    for (int i=0;i<objectCells.length;i++){
			propertyCells[i].setWidth(newWidth);
			propertyCells[i].move(-halfDelta,0);
		    }
		    r3.vy=(propertyCells[0].vy+propertyCells[propertyCells.length-1].vy)/2;
		}
	    }
	}
	else if (g==r1){
	    //column is necessarily RIGHT_COLUMN
	    long halfDelta=(g.vx-g0.vx-g0.getWidth())/2;
	    long newWidth=g0.getWidth()+halfDelta;
	    if (newWidth>0){
		synchronized(this){
		    for (int i=0;i<objectCells.length;i++){
			objectCells[i].setWidth(newWidth);
			objectCells[i].move(halfDelta,0);
		    }
		    r3.vy=(objectCells[0].vy+objectCells[objectCells.length-1].vy)/2;
		}
	    }
	}
    }

    void updateHandles(){
	if (r1!=null){
	    r1.vx=objectCells[0].vx+objectCells[0].getWidth();
	    r1.vy=(objectCells[0].vy+objectCells[objectCells.length-1].vy)/2;
	}
	if (r3!=null){
	    r3.vx=propertyCells[0].vx-propertyCells[0].getWidth();
	    r3.vy=(propertyCells[0].vy+propertyCells[propertyCells.length-1].vy)/2;
	}
    }

    void destroy(){
	VirtualSpace vs=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace);
	if (r1!=null){vs.destroyGlyph(r1);}
	if (r3!=null){vs.destroyGlyph(r3);}
    }

    Glyph getMainGlyph(){return g0;}

}
