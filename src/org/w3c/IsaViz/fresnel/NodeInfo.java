/*   FILE: NodeInfo.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: NodeInfo.java,v 1.2 2006/05/18 14:16:12 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import org.w3c.IsaViz.INode;

import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.VImage;

public class NodeInfo extends ItemInfo {

    public INode owner;
    public LongPoint sl;
    public LongPoint tl;

    VImage replacementImage;

    NodeInfo(INode o){
	this.owner = o;
    }

}