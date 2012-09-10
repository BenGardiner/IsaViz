/*   FILE: INode.java
 *   DATE OF CREATION:   10/18/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 09 11:06:26 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.engine.LongPoint;
import net.claribole.zvtm.engine.PostAnimationAction;

import org.w3c.IsaViz.fresnel.*;

/*Parent of IResource, IProperty, ILiteral*/

public abstract class INode {

    /*is the entity selected in the GUI*/
    boolean selected=false;
    /*is the node/property deactivated*/
    boolean commented=false;
    
    /*is the node in a table form (for resources, this is from the point of view of resource as objects)*/
    boolean table=false;

    private int align=Style.TA_CENTER.intValue();

    /*color*/
    int strokeIndex;
    int fillIndex;

    public void setSelected(boolean b){selected=b;}

    public boolean isSelected(){return selected;}

    public abstract Glyph getGlyph();

    public abstract VText getGlyphText();

    public abstract String getText(); //a meaningful string depending on the node's type (uri, etc)

    public abstract void gray();
    public abstract void colorize();
    public abstract void grayAnimated(long d);
    public abstract void colorizeAnimated(long d);
    public abstract void translate(ItemInfo ii, long d, long nx, long ny, PostAnimationAction paa);

    public abstract void comment(boolean b, Editor e, boolean propagate);

    public abstract void displayOnTop();

    public boolean isCommented(){return commented;}

    public boolean isVisuallyRepresented(){//note IProperty defines its own method
	//the entity might not be present in the graph (visual) if it has a visibility attribute set to display=none or visibility=hidden
	//the test for isVisible() is necessary because INodes for which visibility=hidden do have glyphs associated with them, they are just not visible
	if (this.getGlyph()!=null && this.getGlyph().isVisible()){return true;}
	else return false;
    }

    public abstract void setVisible(boolean b);

    public void setTableFormLayout(boolean b){
	table=b;
    }

    public boolean isLaidOutInTableForm(){
	return table;
    }

    public void setTextAlign(int al){
	align=al;
    }

    public int getTextAlign(){
	return align;
    }


}
