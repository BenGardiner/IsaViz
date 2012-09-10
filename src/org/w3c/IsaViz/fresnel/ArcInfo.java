/*   FILE: ArcInfo.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: ArcInfo.java,v 1.3 2006/10/26 09:49:06 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import org.w3c.IsaViz.IProperty;

import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.VSegment;

public class ArcInfo extends ItemInfo {

    public IProperty owner;
    public LongPoint tl;

    public VSegment replacementArc;

    public String originalLabel;

    ArcInfo(IProperty o){
	this.owner = o;
    }
    

}