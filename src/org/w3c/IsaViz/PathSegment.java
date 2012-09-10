/*   FILE: PathSegment.java
 *   DATE OF CREATION:   12/13/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:53:42 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 




package org.w3c.IsaViz;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

class PathSegment {
    
    int type;
    double[] cds=new double[6];

    PathSegment(double[] c,int t){
	type=t;
	cds[0]=c[0];
	cds[1]=-c[1];
	cds[2]=c[2];
	cds[3]=-c[3];
	cds[4]=c[4];
	cds[5]=-c[5];
    }

    int getType(){return type;}

    double[] getCoords(){return cds;}

    Point2D getMainPoint(){
	if (type==PathIterator.SEG_MOVETO || type==PathIterator.SEG_LINETO){
	    return new Point2D.Double(cds[0],cds[1]);
	}
	else if (type==PathIterator.SEG_QUADTO){
	    return new Point2D.Double(cds[2],cds[3]);
	}
	else if (type==PathIterator.SEG_CUBICTO){
	    return new Point2D.Double(cds[4],cds[5]);
	}
	return null;
    }

    void setMainPoint(Point2D p){
	if (type==PathIterator.SEG_MOVETO || type==PathIterator.SEG_LINETO){
	    cds[0]=p.getX();
	    cds[1]=p.getY();
	}
	else if (type==PathIterator.SEG_QUADTO){
	    cds[2]=p.getX();
	    cds[3]=p.getY();
	}
	else if (type==PathIterator.SEG_CUBICTO){
	    cds[4]=p.getX();
	    cds[5]=p.getY();
	}
    }
    
}
