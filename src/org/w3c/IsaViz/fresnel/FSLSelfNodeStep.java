/*   FILE: FSLSelfNodeStep.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLSelfNodeStep.java,v 1.5 2005/11/18 08:28:42 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**location step: current node*/

public class FSLSelfNodeStep extends FSLLocationStep {
    
    public FSLSelfNodeStep(){
	type = R_STEP;
    }

    public String serialize(){
	return ".";
    }

}
