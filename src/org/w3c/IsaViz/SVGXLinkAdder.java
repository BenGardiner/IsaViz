/*   FILE: SVGXLinkAdder.java
 *   DATE OF CREATION:   Fri Oct 15 14:15:07 2004
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Fri Oct 15 14:21:34 2004 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.svg.SVGReader;
import com.xerox.VTM.svg.SVGWriter;
import com.xerox.VTM.svg.SVGWriterPostProcessor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

class SVGXLinkAdder implements SVGWriterPostProcessor {
    
    public void newElementCreated(Element el, Glyph gl, Document d){
	Object owner = gl.getOwner();
	if (owner != null && owner instanceof IResource){
	    Element a = d.createElementNS(SVGWriter.svgURI, SVGReader._a);
	    a.setAttributeNS(SVGWriter.xlinkURI, "xlink:href", ((IResource)owner).getIdentity());
	    Node parent = el.getParentNode();
	    parent.replaceChild(a, el);
	    a.appendChild(el);
	}
    }
    
}
