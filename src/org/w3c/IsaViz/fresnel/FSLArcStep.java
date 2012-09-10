/*   FILE: FSLArcStep.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLArcStep.java,v 1.7 2005/11/18 08:28:42 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**Arc location step*/

public class FSLArcStep extends FSLLocationStep {

    public FSLArcStep(String qname, FSLNSResolver nsr){
	type = P_STEP;
	String[] splittedQName = FSLPath.splitQName(qname);
	nsURI = nsr.getNamespaceURI(splittedQName[0]);
	localName = splittedQName[1];
    }

}
