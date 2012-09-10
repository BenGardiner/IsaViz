/*   FILE: FSLVisibility.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLVisibility.java,v 1.4 2006/10/26 15:18:47 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import org.w3c.IsaViz.Editor;
import org.w3c.IsaViz.IResource;

import java.util.Vector;

public class FSLVisibility extends PropertyVisibility {
    
    static final String FSL = "FSL   ";
    
    FSLPath constraint;

    FSLVisibility(FSLPath pathToProperty){
	constraint = pathToProperty;
    }

    void getPropertiesToShow(IResource r, Vector propertiesShown, Vector incomingPredicates, Vector outgoingPredicates){
	Vector v = Editor.fresnelMngr.fie.evaluatePathExpr(constraint, r);
	for (int i=0;i<v.size();i++){
	    propertiesShown.add(((Vector)v.elementAt(i)).lastElement()); // take arc corresponding to last step
	}// because in expressions such as fresnel:showProperties "foaf:knows/foaf:Person/foaf:surname"
	//  we are interested in foaf:surname, not foaf:knows
    }
    
    public String toString(){
	return FSL + constraint.toString();
    }

}