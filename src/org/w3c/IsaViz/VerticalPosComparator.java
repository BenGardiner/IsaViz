/*   FILE: VerticalPosComparator.java
 *   DATE OF CREATION:   Wed Jun 11 11:15:50 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Jun 13 11:37:25 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import java.util.Vector;
import java.util.Arrays;
import com.xerox.VTM.glyphs.Glyph;

public class VerticalPosComparator implements java.util.Comparator {

    VerticalPosComparator(){}

    public int compare(Object o1, Object o2){
	if ((o1 instanceof Glyph) && (o2 instanceof Glyph)){
	    return (int)(((Glyph)o2).vy-((Glyph)o1).vy);
	}
	else {return 0;}
    }
    
    public boolean equals(Object obj){
	if (obj instanceof VerticalPosComparator){
	    return true;
	}
	else return false;
    }

}
