/*   FILE: IContainer.java
 *   DATE OF CREATION:   12/24/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:47:47 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.util.Vector;

/*methods related to containers (bag, sequence, alternative)*/

public class IContainer {
    
    /**
     *returns the lowest available index for a new membership property, for a given resource
     *@param r the resource to which the new membership property is added
     */
    public static String nextContainerIndex(IResource r){
	int maxIndex=0;
	if (r.outgoingPredicates!=null){
	    IProperty p;
	    int index;
	    String name;
	    Vector v=r.getOutgoingPredicates();
	    for (int i=0;i<v.size();i++){
		p=(IProperty)v.elementAt(i);
		if (p.getNamespace().equals(Editor.RDFMS_NAMESPACE_URI) && (name=p.getLocalname()).startsWith("_")){
		    //there is a good chance this is a _X property with X=a number
		    try {
			index=Integer.parseInt(name.substring(1,name.length()),10);  //radix=10  (decimal base)
			if (index>maxIndex){maxIndex=index;}
		    }
		    catch (NumberFormatException ex){}//it was not a number after all, just ignore it and go to the next property
		}
	    }
	}
	String res="_"+String.valueOf(++maxIndex);
	return res;
    }
    

    
}
