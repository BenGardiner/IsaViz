/*   FILE: ControlPoint.java
 *   DATE OF CREATION:   12/10/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:46:38 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.RectangleNR;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.VSegment;

class ControlPoint {

    static final int CUBIC_CURVE_CP1=4;  //do not modify
    static final int CUBIC_CURVE_CP2=5;  //some numerical tests (<, >)
    static final int QUAD_CURVE_CP=3;    //depend on the ordering of these
    static final int START_POINT=1;
    static final int END_POINT=2;
    static final int CURVE_POINT=0;

    PropResizer owner;

    int type;
    RectangleNR handle;
    RectangleNR prevHandle;
    RectangleNR nextHandle;
    VSegment s1;   //first segment controled by this point
    VSegment s2;   //second segment controlled by this point

    boolean alsoDragSiblings=false;  //should direct siblings of this control point be dragged too

    ControlPoint(RectangleNR h,RectangleNR ph,VSegment seg1,int t,PropResizer ow){
	handle=h;
	prevHandle=ph;
	s1=seg1;
	h.setOwner(this);
	h.setType("rszp");  //ReSiZe Path
	type=t;
	owner=ow;
    }

    void setSecondSegment(VSegment seg2,RectangleNR nh){
	s2=seg2;
	nextHandle=nh;
    }
    
    void setType(int t){
	type=t;
    }

    void update(){
	if (type!=START_POINT){
	    s1.vx=(prevHandle.vx+handle.vx)/2;
	    s1.vy=(prevHandle.vy+handle.vy)/2;
	    s1.setWidthHeight((handle.vx-prevHandle.vx)/2,(prevHandle.vy-handle.vy)/2);
	}
	if (type!=END_POINT){
	    s2.vx=(handle.vx+nextHandle.vx)/2;
	    s2.vy=(handle.vy+nextHandle.vy)/2;
	    s2.setWidthHeight((nextHandle.vx-handle.vx)/2,(handle.vy-nextHandle.vy)/2);
	}
	if (alsoDragSiblings){
	    if (prevHandle!=null){((ControlPoint)prevHandle.getOwner()).update();}
	    if (nextHandle!=null){((ControlPoint)nextHandle.getOwner()).update();}
	}
    }

    //should direct siblings of this control point be dragged too
    void dragSiblings(boolean b){
	if (b!=alsoDragSiblings){
	    alsoDragSiblings=b;
	    if (alsoDragSiblings){
		if (prevHandle!=null){
		    if (((ControlPoint)prevHandle.getOwner()).type!=ControlPoint.START_POINT){
			Editor.vsm.stickToGlyph(prevHandle,handle);
		    }
		}
		if (nextHandle!=null){
		    if (((ControlPoint)nextHandle.getOwner()).type!=ControlPoint.END_POINT){
			Editor.vsm.stickToGlyph(nextHandle,handle);}
		}
	    }
	    else {
		if (prevHandle!=null){
		    if (((ControlPoint)prevHandle.getOwner()).type!=ControlPoint.START_POINT){
			Editor.vsm.unstickFromGlyph(prevHandle,handle);
		    }
		}
		if (nextHandle!=null){
		    if (((ControlPoint)nextHandle.getOwner()).type!=ControlPoint.END_POINT){
			Editor.vsm.unstickFromGlyph(nextHandle,handle);
		    }
		}
	    }
	}
    }

    VPath getPath(){return (VPath)owner.getMainGlyph();}

}
