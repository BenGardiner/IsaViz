/*   FILE: ISVCut.java
 *   DATE OF CREATION:   12/20/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Jul 11 11:28:15 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.util.Vector;

/*ISV command: cut*/

class ISVCut extends ISVCommand {

    Editor application;
    IResource[] rl;  //list of IResource
    ILiteral[]  ll;  //list of ILiteral
    IProperty[] pl;  //list of IProperty

    ISVCut(Editor e,Vector props,Vector ress,Vector lits){
	application=e;
	rl=(IResource[])ress.toArray(new IResource[ress.size()]);
	ll=(ILiteral[])lits.toArray(new ILiteral[lits.size()]);
	Vector v=new Vector();
	IProperty p;
	for (int i=0;i<props.size();i++){//copy only predicates for which both subject and
	    p=(IProperty)props.elementAt(i);// object have been selected for the copy, even
	    if (p.getSubject().isSelected() && p.getObject().isSelected()){v.add(p);} //if
	    //the edge itself is selected. Copying the others would create pending edges
	}
	pl=(IProperty[])v.toArray(new IProperty[v.size()]);
    }

    void _do(){
	//remember the entities about to be cut
	application.copiedResources=new Vector(java.util.Arrays.asList(rl));
	application.copiedLiterals=new Vector(java.util.Arrays.asList(ll));
	application.copiedPredicates=new Vector(java.util.Arrays.asList(pl));
	//we have to remember who was laid out in table, as this information is lost when doing application.deleteProperty()
	Vector resLaidOutInTableForm=new Vector();
	for (int i=0;i<rl.length;i++){
	    if (rl[i].isLaidOutInTableForm()){resLaidOutInTableForm.add(rl[i]);}
	}
	Vector litLaidOutInTableForm=new Vector();
	for (int i=0;i<ll.length;i++){
	    if (ll[i].isLaidOutInTableForm()){litLaidOutInTableForm.add(ll[i]);}
	}
	//delete the entities
	for (int i=0;i<pl.length;i++){
	    application.deleteProperty(pl[i]);
	}
	for (int i=0;i<rl.length;i++){
	    application.deleteResource(rl[i]);
	}
	for (int i=0;i<ll.length;i++){
	    application.deleteLiteral(ll[i]);
	}
	//then restore the table layout property
	for (int i=0;i<resLaidOutInTableForm.size();i++){
	    ((IResource)resLaidOutInTableForm.elementAt(i)).setTableFormLayout(true);
	}
	resLaidOutInTableForm.removeAllElements();
	for (int i=0;i<litLaidOutInTableForm.size();i++){
	    ((ILiteral)litLaidOutInTableForm.elementAt(i)).setTableFormLayout(true);
	}
	litLaidOutInTableForm.removeAllElements();
    }

    void _undo(){
	String vs=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getName();
	for (int i=0;i<rl.length;i++){//put back IResources
	    Editor.vsm.addGlyph(rl[i].getGlyph(),vs);
	    Editor.vsm.addGlyph(rl[i].getGlyphText(),vs);
	    if (!application.resourcesByURI.containsKey(rl[i].getIdentity())){
		application.resourcesByURI.put(rl[i].getIdentity(),rl[i]);
	    }
	    else {
		application.errorMessages.append("Undo: A conflict occured when trying to restore resource '"+rl[i].getIdentity()+"'.\nThe model probably contains two nodes with this URI.\n");application.reportError=true;
	    }
	    if (!ConfigManager.SHOW_ANON_ID && rl[i].isAnon()){Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).hide(rl[i].getGlyphText());}
	}
	for (int i=0;i<ll.length;i++){//put back ILiterals
	    Editor.vsm.addGlyph(ll[i].getGlyph(),vs);
	    Editor.vsm.addGlyph(ll[i].getGlyphText(),vs);
	    application.literals.add(ll[i]);
	}
	INode n;
	Vector alreadyAddedEdges=new Vector();
	for (int i=0;i<pl.length;i++){//put back IProperties and link them back to resources and literals
	    if (pl[i].getTableCellGlyph()!=null){Editor.vsm.addGlyph(pl[i].getTableCellGlyph(),vs);}
	    if (!alreadyAddedEdges.contains(pl[i].getGlyph())){//only add an edge once (would be added several times for tables without this test)
		Editor.vsm.addGlyph(pl[i].getGlyph(),vs);
		alreadyAddedEdges.add(pl[i].getGlyph());
	    }//not necessary to do the same for arrow head as it no longer exists for edges pointing to tables
	    if (pl[i].getGlyphHead()!=null){Editor.vsm.addGlyph(pl[i].getGlyphHead(),vs);}
	    Editor.vsm.addGlyph(pl[i].getGlyphText(),vs);
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
