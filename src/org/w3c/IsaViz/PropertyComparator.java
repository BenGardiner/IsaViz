/*   FILE: PropertyComparator.java
 *   DATE OF CREATION:   Wed Jun 11 11:15:50 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jun 11 11:55:19 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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


public class PropertyComparator implements java.util.Comparator {

    int order=GraphStylesheet.SORT_BY_NAMESPACE.intValue();

    PropertyComparator(Integer ordering){
	order=ordering.intValue();
    }

    public int compare(Object o1, Object o2){
	if ((o1 instanceof Vector) && (o2 instanceof Vector)){
	    IProperty p1=(IProperty)((Vector)o1).elementAt(1);
	    IProperty p2=(IProperty)((Vector)o2).elementAt(1);
	    if (order==GraphStylesheet.SORT_BY_NAME.intValue()){
		int diff=p1.getLocalname().compareTo(p2.getLocalname());
		if (diff!=0){return diff;}
		else return p1.getNamespace().compareTo(p2.getNamespace());
	    }
	    else if (order==GraphStylesheet.SORT_BY_NAMESPACE.intValue()){
		int diff=p1.getNamespace().compareTo(p2.getNamespace());
		if (diff!=0){return diff;}
		else return p1.getLocalname().compareTo(p2.getLocalname());
	    }
	    else if (order==GraphStylesheet.SORT_BY_NAME_REV.intValue()){
		int diff=p2.getLocalname().compareTo(p1.getLocalname());
		if (diff!=0){return diff;}
		else return p2.getNamespace().compareTo(p1.getNamespace());
	    }
	    else if (order==GraphStylesheet.SORT_BY_NAMESPACE_REV.intValue()){
		int diff=p2.getNamespace().compareTo(p1.getNamespace());
		if (diff!=0){return diff;}
		else return p2.getLocalname().compareTo(p1.getLocalname());
	    }
	    else return 0;
	}
	else {return 0;}
    }
    
    public boolean equals(Object obj){
	if (obj instanceof PropertyComparator && ((PropertyComparator)obj).order==this.order){
	    return true;
	}
	else return false;
    }

}
