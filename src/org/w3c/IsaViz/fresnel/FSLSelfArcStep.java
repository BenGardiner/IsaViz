/*   FILE: FSLSelfArcStep.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLSelfArcStep.java,v 1.5 2005/11/18 08:28:42 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**location step: current arc*/

public class FSLSelfArcStep extends FSLLocationStep {
    
    public FSLSelfArcStep(){
	type = P_STEP;
    }

    public String serialize(){
	return ".";
    }

}
