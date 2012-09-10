/*   FILE: Resizer.java
 *   DATE OF CREATION:   12/05/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:56:16 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 




package org.w3c.IsaViz;

import com.xerox.VTM.glyphs.Glyph;

/*Parent of LitResizer, ResResizer, PropResizer. Contains resizing handles (small black boxes) that are used to modify the geometry of a glyph*/

abstract class Resizer {

    abstract void updateMainGlyph(Glyph g);

    abstract void updateHandles();

    abstract void destroy();

    abstract Glyph getMainGlyph();
    
}
