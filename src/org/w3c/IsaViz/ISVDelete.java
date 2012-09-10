/*   FILE: ISVDelete.java
 *   DATE OF CREATION:   12/20/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 23 16:14:36 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

import java.util.Vector;
import java.util.Enumeration;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.glyphs.VTriangleOr;
import com.xerox.VTM.glyphs.VPath;

/*ISV command: delete*/

class ISVDelete extends ISVCommand {

    Editor application;
    IResource[] rl;  //list of IResource
    ILiteral[] ll;   //list of ILiteral
    IProperty[] pl;  //list of IProperty

    ISVDelete(Editor e,Vector props,Vector ress,Vector lits){
	application=e;
	rl=(IResource[])ress.toArray(new IResource[ress.size()]);
	ll=(ILiteral[])lits.toArray(new ILiteral[lits.size()]);
	pl=(IProperty[])props.toArray(new IProperty[props.size()]);
    }

    void _do(){
	for (int i=pl.length-1;i>=0;i--){
	    application.deleteProperty(pl[i]);
	}
	for (int i=ll.length-1;i>=0;i--){
	    application.deleteLiteral(ll[i]);
	}
	for (int i=rl.length-1;i>=0;i--){
	    application.deleteResource(rl[i]);
	}
    }

    void _undo(){
	String vs=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getName();
	for (int i=0;i<rl.length;i++){//restore IResources
	    if (rl[i].isVisuallyRepresented()){
		Editor.vsm.addGlyph(rl[i].getGlyph(),vs);
		Editor.vsm.addGlyph(rl[i].getGlyphText(),vs);
	    }
	    if (!application.resourcesByURI.containsKey(rl[i].getIdentity())){
		application.resourcesByURI.put(rl[i].getIdentity(),rl[i]);
	    }
	    else {
		application.errorMessages.append("Undo: A conflict occured when trying to restore resource '"+rl[i].getIdentity()+"'.\nThe model probably contains two nodes with this URI.\n");application.reportError=true;
	    }
	}
	for (int i=0;i<ll.length;i++){//restore ILiterals
	    if (ll[i].isVisuallyRepresented()){
		Editor.vsm.addGlyph(ll[i].getGlyph(),vs);
		Editor.vsm.addGlyph(ll[i].getGlyphText(),vs);
	    }
	    application.literals.add(ll[i]);
	}
	INode n;
	Vector edge;
	for (int i=0;i<pl.length;i++){//restore IProperties and link them back to resources and literals
	    if (pl[i].isVisuallyRepresented()){
		if (pl[i].isLaidOutInTableForm()){//if laid out in table, also restore the cell, and be carefull not to add the edge incoming to the 
		    Editor.vsm.addGlyph(pl[i].getTableCellGlyph(),vs);//table if it already exists
		    Editor.vsm.addGlyph(pl[i].getGlyphText(),vs);
		    if ((edge=IProperty.getTableIncomingEdge(pl[i]))==null){
			Editor.vsm.addGlyph(pl[i].getGlyph(),vs);
// 			Editor.vsm.addGlyph(pl[i].getGlyphHead(),vs);
		    }
		    else {
			pl[i].setGlyph((VPath)edge.elementAt(0),null);/*(VTriangleOr)edge.elementAt(1)*/
		    }
		}
		else {
		    Editor.vsm.addGlyph(pl[i].getGlyphText(),vs);
		    Editor.vsm.addGlyph(pl[i].getGlyph(),vs);
		    Editor.vsm.addGlyph(pl[i].getGlyphHead(),vs);
		}
	    }
	    if (application.propertiesByURI.containsKey(pl[i].getIdent())){
		Vector v=(Vector)application.propertiesByURI.get(pl[i].getIdent());
		v.add(pl[i]);
	    }
	    else {
		Vector v=new Vector();
		v.add(pl[i]);
		application.propertiesByURI.put(pl[i].getIdent(),v);
	    }
	    pl[i].getSubject().addOutgoingPredicate(pl[i]);
	    n=pl[i].getObject();
	    if (n instanceof IResource){((IResource)n).addIncomingPredicate(pl[i]);}
	    else {((ILiteral)n).setIncomingPredicate(pl[i]);}
	}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
    }


}
