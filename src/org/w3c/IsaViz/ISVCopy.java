/*   FILE: ISVCopy.java
 *   DATE OF CREATION:   12/20/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:49:36 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.util.Vector;

/*ISV command: copy*/

class ISVCopy extends ISVCommand {

    Editor application;
    IResource[] rl;  //list of IResource
    ILiteral[] ll;   //list of ILiteral
    IProperty[] pl;  //list of IProperty

    ISVCopy(Editor e,Vector props,Vector ress,Vector lits){
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
	application.copiedResources=new Vector(java.util.Arrays.asList(rl));
	application.copiedLiterals=new Vector(java.util.Arrays.asList(ll));
	application.copiedPredicates=new Vector(java.util.Arrays.asList(pl));
    }

    void _undo(){
	//does not do anything
    }


}
