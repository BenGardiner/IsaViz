/*   FILE: FSLResourceStep.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLResourceStep.java,v 1.6 2005/11/18 08:28:42 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**Node step: resource*/

public class FSLResourceStep extends FSLNodeStep {
    
    public FSLResourceStep(String qname, FSLNSResolver nsr){
	type = R_STEP;
	String[] splittedQName = FSLPath.splitQName(qname);
	nsURI = nsr.getNamespaceURI(splittedQName[0]);
	localName = splittedQName[1];
    }

}
