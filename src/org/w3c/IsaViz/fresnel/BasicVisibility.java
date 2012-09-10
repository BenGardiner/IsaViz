/*   FILE: BasicVisibility.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: BasicVisibility.java,v 1.3 2006/05/16 06:15:14 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import org.w3c.IsaViz.IResource;
import org.w3c.IsaViz.IProperty;

import java.util.Vector;

public class BasicVisibility extends PropertyVisibility {

    static final String BASIC = "BASIC ";

    String constraint;

    BasicVisibility(String propertyURI){
	constraint = propertyURI;
    }

    void getPropertiesToShow(IResource r, Vector propertiesShown, Vector incomingPredicates, Vector outgoingPredicates){
	IProperty p;
	for (int i=0;i<outgoingPredicates.size();i++){
	    p = (IProperty)outgoingPredicates.elementAt(i);
	    if (p.getIdent().equals(constraint) && !propertiesShown.contains(p)){
		propertiesShown.add(p);
	    }
	}
	// incomingPredicates is not used here as basic selectors only look at
	// outgoing properties (from the current node), but is in FSLVisibility
    }
 
    public String toString(){
	return BASIC + constraint;
    }
   
}