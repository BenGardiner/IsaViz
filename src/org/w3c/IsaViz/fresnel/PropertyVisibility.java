/*   FILE: PropertyVisibility.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: PropertyVisibility.java,v 1.4 2006/05/16 06:15:14 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import org.w3c.IsaViz.IResource;

import java.util.Vector;

public abstract class PropertyVisibility {

    abstract void getPropertiesToShow(IResource r, Vector propertiesShown, Vector incomingPredicates, Vector outgoingPredicates);
    
}