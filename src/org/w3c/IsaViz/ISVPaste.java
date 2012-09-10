/*   FILE: ISVPaste.java
 *   DATE OF CREATION:   12/20/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 23 15:23:58 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

import java.util.Vector;
import java.util.Hashtable;
import java.awt.geom.Rectangle2D;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VEllipse;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VTriangleOr;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.RectangularShape;


/*ISV command: paste*/

class ISVPaste extends ISVCommand {

    Editor application;
    IResource[] srl;  //list of source IResource
    ILiteral[] sll;   //list of source ILiteral
    IProperty[] spl;  //list of source IProperty

    IResource[] rl;  //list of IResource
    ILiteral[] ll;   //list of ILiteral
    IProperty[] pl;  //list of IProperty

    long mx,my;  //coords where new selection should be placed

    ISVPaste(Editor e,Vector props,Vector ress,Vector lits,long x,long y){
	application=e;
	srl=(IResource[])ress.toArray(new IResource[ress.size()]);
	sll=(ILiteral[])lits.toArray(new ILiteral[lits.size()]);
	spl=(IProperty[])props.toArray(new IProperty[props.size()]);
	rl=new IResource[ress.size()];
	ll=new ILiteral[lits.size()];
	pl=new IProperty[props.size()];
	this.mx=x;
	this.my=y;
    }

    void _do(){
	//compute the geom center of the set of elements in clipboard
	Glyph[] gList=new Glyph[srl.length+sll.length];
	int i=0;
	int j=0;
	while (i<srl.length){gList[j]=srl[i].getGlyph();i++;j++;}
	i=0;
	while (i<sll.length){gList[j]=sll[i].getGlyph();i++;j++;}
	LongPoint gc=VirtualSpace.getGlyphSetGeometricalCenter(gList);
	//duplicate and paste nodes
	pasteResources(gc);
	pasteLiterals(gc);
	//duplicate and paste edges
	pasteProperties(gc);
	//don't need these any longer
	srl=null;
	sll=null;
	spl=null;
    }

    void pasteResources(LongPoint lp){//have to generate new unqiue URIs or IDs for resources
	for (int i=0;i<srl.length;i++){
	    IResource r=new IResource();
	    r.setTextAlign(srl[i].getTextAlign());
	    if (srl[i].isLaidOutInTableForm()){r.setTableFormLayout(true);}
	    Glyph sel=srl[i].getGlyph();
	    Glyph el=net.claribole.zvtm.glyphs.GlyphUtils.basicClone(sel);
	    el.moveTo(mx+sel.vx-lp.x,my+sel.vy-lp.y);
	    if (el instanceof RectangularShape){
		((RectangularShape)el).setWidth(((RectangularShape)sel).getWidth());
		((RectangularShape)el).setHeight(((RectangularShape)sel).getHeight());
	    }
	    else {
		el.sizeTo(sel.getSize());
	    }
	    r.setGlyph(el);
	    Editor.vsm.addGlyph(el,Editor.mainVirtualSpace);
	    if (srl[i].isAnon()){//if source resource was anonymous, create a new anon resource
		r.setAnon(true);
		r.setAnonymousID(application.nextAnonymousID());
		application.resourcesByURI.put(r.getIdentity(),r);
	    }
	    else {//append '--copy-X' to the end of the URI to prevent conflict with existing resources
		//but first test if the source still exist (this will not be the case if we did a CUT or
		//if the source was deleted prior to paste after a COPY). If it does not, there will
		//not be any conflict, in which case we do not append --copy-X
		r.setURI(srl[i].getIdentity());
		int j=1;
		while (application.resourcesByURI.containsKey(r.getIdentity())){
		    r.setURI(srl[i].getIdentity()+"--copy-"+String.valueOf(j));
		    j++;
		}
		application.resourcesByURI.put(r.getIdentity(),r);
	    }
	    VText g=new VText(el.vx,el.vy,0,ConfigManager.resourceColorTB,r.getGraphLabel());
	    Editor.vsm.addGlyph(g,Editor.mainVirtualSpace);
	    r.setGlyphText(g);
	    if (r.isLaidOutInTableForm()){application.geomMngr.correctResourceTextAndShape(r);}
	    else {application.geomMngr.correctResourceTextAndShape(r);}
	    r.setFillColor(srl[i].getFillIndex());
	    r.setStrokeColor(srl[i].getStrokeIndex());
	    if (r.isAnon() && !ConfigManager.SHOW_ANON_ID){Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).hide(g);}
	    if (srl[i].isCommented()){application.commentNode(r,true,true);}
	    rl[i]=r;
	}
    }

    void pasteLiterals(LongPoint lp){//pretty straightforward since there cannot be any conflict
	for (int i=0;i<sll.length;i++){
	    ILiteral l=new ILiteral();
	    l.setTextAlign(sll[i].getTextAlign());
	    if (sll[i].isLaidOutInTableForm()){l.setTableFormLayout(true);}
	    Glyph sg=sll[i].getGlyph();
	    Glyph g=net.claribole.zvtm.glyphs.GlyphUtils.basicClone(sg);
	    g.moveTo(mx+sg.vx-lp.x,my+sg.vy-lp.y);
	    if (g instanceof RectangularShape){
		((RectangularShape)g).setWidth(((RectangularShape)sg).getWidth());
		((RectangularShape)g).setHeight(((RectangularShape)sg).getHeight());
	    }
	    else {
		g.sizeTo(g.getSize());
	    }
	    Editor.vsm.addGlyph(g,Editor.mainVirtualSpace);
	    l.setGlyph(g);
	    l.setLanguage(sll[i].getLang());
	    l.setEscapeXMLChars(sll[i].escapesXMLChars());
	    l.setDatatype(sll[i].getDatatype());
	    application.setLiteralValue(l,sll[i].getValue());
	    application.literals.add(l);
	    if (l.isLaidOutInTableForm()){application.geomMngr.correctLiteralTextAndShape(l);}
	    else {application.geomMngr.correctLiteralTextAndShape(l);}
	    l.setFillColor(sll[i].getFillIndex());
	    l.setStrokeColor(sll[i].getStrokeIndex());
	    if (sll[i].isCommented()){application.commentNode(l,true, true);}
	    ll[i]=l;  //replace object to be copied by its copy
	}
    }

    void pasteProperties(LongPoint lp){//builds the property and creates all dependencies w.r.t subject and object
	Hashtable stahate=new Hashtable(); //stahate=SubjectsThatAlreadyHaveATableEdge (used to only create table edges once, based on the fact that all properties laid out in a table for a specific subject are grouped in a single table)  key=IResource, value=VPath
	for (int i=0;i<spl.length;i++){
	    IProperty p=application.addProperty(spl[i].getNamespace(),spl[i].getLocalname());
	    p.setTextAlign(spl[i].getTextAlign());
	    //identify subject and object linked to the copied predicate and assign dependencies
	    IResource subject=this.getCopy(spl[i].getSubject());
	    INode object=this.getCopy(spl[i].getObject());
	    p.setSubject(subject);
	    subject.addOutgoingPredicate(p);
	    if (object instanceof IResource){
		p.setObject((IResource)object);
		((IResource)object).addIncomingPredicate(p);
	    }
	    else {//instanceof ILiteral (or we have an error)
		p.setObject((ILiteral)object);
		((ILiteral)object).setIncomingPredicate(p);
		if (p.getIdent().equals(Editor.RDFS_NAMESPACE_URI+"label")){
		    //if property is rdfs:label, set label for the resource
		    subject.setLabel(((ILiteral)object).getValue());
		    subject.getGlyphText().setText(subject.getLabel());
		}
	    }
	    //clone glyphs
	    if (spl[i].isLaidOutInTableForm()){
		p.setTableFormLayout(true);
		p.getObject().setTableFormLayout(true); //good chance it has been set to false if pasting from a cut (because of Editor.deleteProperty())
		if (stahate.containsKey(p.getSubject())){//the table edge already exists, get it and assign it to this prop
		    VPath pt=(VPath)stahate.get(p.getSubject());
// 		    Editor.vsm.addGlyph(pt,Editor.mainVirtualSpace);
		    p.setGlyph(pt,null);
// 		    System.err.println("adding pt "+pt);
		}
		else {//the table edge does not yet exist, create it, store it and asssign it to this prop
		    //path
		    VPath pt=VPath.duplicateVPath((VPath)spl[i].getGlyph(),mx-lp.x,my-lp.y);
		    Editor.vsm.addGlyph(pt,Editor.mainVirtualSpace);
		    p.setGlyph(pt,null);
		    stahate.put(p.getSubject(),pt);
		} 
	    }
	    else {
		//path
		VPath pt=VPath.duplicateVPath((VPath)spl[i].getGlyph(),mx-lp.x,my-lp.y);
		Editor.vsm.addGlyph(pt,Editor.mainVirtualSpace);
		//arrow - if not in table layout
		VTriangleOr str=spl[i].getGlyphHead();
		if (str!=null){
		    VTriangleOr tr=new VTriangleOr(mx+str.vx-lp.x,my+str.vy-lp.y,0,GeometryManager.ARROW_HEAD_SIZE,ConfigManager.propertyColorB,str.getOrient());
		    Editor.vsm.addGlyph(tr,Editor.mainVirtualSpace);
		    p.setGlyph(pt,tr);
		}
		else {//should not happen, for robustness
		    p.setGlyph(pt,null);
		}
	    }
	    //table cell (table layout)
	    VRectangle stcl=spl[i].getTableCellGlyph();
	    VText stx=spl[i].getGlyphText();
	    VText tx;
	    if (p.isLaidOutInTableForm() && stcl!=null){
		VRectangle cell=new VRectangle(mx+stcl.vx-lp.x,my+stcl.vy-lp.y,0,stcl.getWidth(),stcl.getHeight(),(p.getObject() instanceof IResource) ? ConfigManager.resourceColorF : ConfigManager.literalColorF);
		Editor.vsm.addGlyph(cell,Editor.mainVirtualSpace);
		p.setTableCellGlyph(cell);
		tx=new VText(mx+stx.vx-lp.x,my+stx.vy-lp.y,0,(p.getObject() instanceof IResource) ? ConfigManager.resourceColorTB : ConfigManager.literalColorTB,stx.getText());
		//application.geomMngr.adjustTablePath(p);
	    }
	    else {
		tx=new VText(mx+stx.vx-lp.x,my+stx.vy-lp.y,0,ConfigManager.propertyColorT,stx.getText());
		application.geomMngr.adjustPaths(p.getSubject());
		application.geomMngr.adjustPaths(p.getObject());
	    }
	    Editor.vsm.addGlyph(tx,Editor.mainVirtualSpace);
	    p.setGlyphText(tx);
	    p.setCellFillColor(spl[i].getCellFillIndex());
	    p.setStrokeColor(spl[i].getStrokeIndex());
	    p.setTextColor(spl[i].getTextIndex());
	    if (spl[i].isCommented()){application.commentPredicate(p,true, false);}
	    pl[i]=p;
	}
    }

    //given a source resource, returns its copy 
    IResource getCopy(IResource r){
	for (int i=0;i<srl.length;i++){
	    if (r==srl[i]){return rl[i];}
	}
	return null;
    }

    //given a source node (resource or literal), returns its copy (not for IProperty)
    INode getCopy(INode n){
	for (int i=0;i<srl.length;i++){
	    if (n==srl[i]){return rl[i];}
	}
	for (int i=0;i<sll.length;i++){
	    if (n==sll[i]){return ll[i];}
	}
	return null;
    }

    void _undo(){//when calling undo, rl,ll and pl contain the clones
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

}
