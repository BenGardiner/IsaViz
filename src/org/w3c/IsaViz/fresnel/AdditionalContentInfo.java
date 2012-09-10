/*   FILE: ArcInfo.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: AdditionalContentInfo.java,v 1.1 2006/10/29 11:09:03 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.util.Vector;

import com.xerox.VTM.glyphs.VText;

public class AdditionalContentInfo {
    
    Vector labels;

    AdditionalContentInfo(){
	labels = new Vector();
    }
    
    void add(VText t){
	labels.add(t);
    }

    void destroy(FresnelManager fm){
	for (int i=0;i<labels.size();i++){
	    fm.hideAdditionalContent((VText)labels.elementAt(i));
	}
	labels.clear();
    }
    
}