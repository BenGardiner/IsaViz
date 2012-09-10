/*   FILE: FresnelManager.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FormatCaller.java,v 1.1 2006/05/18 14:16:39 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import org.w3c.IsaViz.IProperty;
import net.claribole.zvtm.engine.PostAnimationAction;

class FormatCaller implements PostAnimationAction {

    Format f;
    IProperty p;
    ArcInfo ai;
    NodeInfo ni;

    FormatCaller(Format f, IProperty p, ArcInfo ai, NodeInfo ni){
	this.f = f;
	this.p = p;
	this.ai = ai;
	this.ni = ni;	
    }

    public void animationEnded(Object target, short type, String dimension){
	f.render(p, ai, ni);
    }

}