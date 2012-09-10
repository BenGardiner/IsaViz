/*   FILE: GSSSelectorComparator.java
 *   DATE OF CREATION:   Thu Mar 27 11:44:57 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Tue Apr 01 16:39:16 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;


public class GSSSelectorComparator implements java.util.Comparator {

    GSSSelectorComparator(){

    }
    
    public int compare(Object o1, Object o2){
	if ((o1 instanceof GSSSelector) && (o2 instanceof GSSSelector)){
	    int w1=((GSSSelector)o1).getWeight();
	    int w2=((GSSSelector)o2).getWeight();
	    return w2-w1;
	}
	else {return 0;}
    }
    
    public boolean equals(Object obj){
	if (obj instanceof GSSSelectorComparator){
	    return true;
	}
	else return false;
    }

}
