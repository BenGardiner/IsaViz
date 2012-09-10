/*   FILE: PropEnumComparator.java
 *   DATE OF CREATION:   Wed Jun 11 14:41:25 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jun 11 15:36:02 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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


public class PropEnumComparator implements java.util.Comparator {

    Vector enumeration;

    PropEnumComparator(CustomOrdering ordering){
	enumeration=ordering.getEnumeration();
    }

    public int compare(Object o1, Object o2){
	if ((o1 instanceof Vector) && (o2 instanceof Vector)){
	    String uri1=((IProperty)((Vector)o1).elementAt(1)).getIdent();
	    String uri2=((IProperty)((Vector)o2).elementAt(1)).getIdent();
	    if (uri1.equals(uri2)){return 0;}
	    else {
		int index1=enumeration.indexOf(uri1);
		int index2=enumeration.indexOf(uri2);
		if (index1==-1){
		    if (index2==-1){//both properties are missing from the enumeration, send them to the end of the table, sorted lexicographically
			return uri1.compareTo(uri2);
		    }
		    else {//index2!=-1, 2nd property is missing, send them to the end of the table
			return 1;
		    }
		}
		else {//index1!=-1, 1st property is missing, send them to the end of the table
		    if (index2==-1){
			return -1;
		    }
		    else {//index2!=-1, both properties are specified in the enumeration, sort accordingly
			return index1-index2;
		    }
		}
	    }
	}
	else {return 0;}
    }
    
    public boolean equals(Object obj){
	if (obj instanceof PropEnumComparator && PropEnumComparator.equivalentStringEnums(this.enumeration,((PropEnumComparator)obj).enumeration)){
	    return true;
	}
	else return false;
    }

    /*checks whether two String enumerations are equivalent or not (i.e. contain the items in the same order)*/
    public static boolean equivalentStringEnums(Vector a,Vector b){
	if (a==null || b==null){
	    return false;
	}
	else {
	    if (a.size()!=b.size()){return false;}
	    else {
		for (int i=0;i<a.size();i++){
		    if (!a.elementAt(i).equals(b.elementAt(i))){return false;}
		}
		return true;
	    }
	}
    }

}
