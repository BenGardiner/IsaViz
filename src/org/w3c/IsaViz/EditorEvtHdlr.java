/*   FILE: EditorEvtHdlr.java
 *   DATE OF CREATION:   10/18/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Sep 16 09:20:28 2005 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 *  $Id: EditorEvtHdlr.java,v 1.32 2007/03/21 13:10:16 epietrig Exp $
 */

package org.w3c.IsaViz;

import java.util.Vector;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.awt.DisplayMode;
import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/*class that receives the events sent from VTM views (include mouse click, entering object,...)*/

public class EditorEvtHdlr implements ViewEventHandler{

    Editor application;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    boolean dragging = false;            //used to get out of camera drag mode under Mac OS X (meta key)
    float tfactor;
    float cfactor=50.0f;
    long x1,y1,x2,y2;                     //remember last mouse coords to display selection rectangle (dragging)
    Camera activeCam;

    int mode=SINGLE_SELECTION_MODE;  //identifies the current interaction mode (selecting, creating,...) value is in MODE below
    static final int SINGLE_SELECTION_MODE=0;
    static final int REGION_SELECTION_MODE=1;
    static final int CREATE_RESOURCE_MODE=2;
    static final int CREATE_PREDICATE_MODE=3;
    static final int CREATE_LITERAL_MODE=4;
    static final int EDIT_PROPS_MODE=5;
    static final int REGION_ZOOM_MODE=6;
    static final int COMMENT_SINGLE_MODE=7;
    static final int COMMENT_REGION_MODE=8;
    static final int UNCOMMENT_SINGLE_MODE=9;
    static final int UNCOMMENT_REGION_MODE=10;
    static final int MOVE_RESIZE_MODE=11;
    static final int PASTE_MODE=12;

//     static final int FP_CREATE_RESOURCE = 0;
//     static final int FP_CREATE_PROPERTY = 2;
//     static final int FP_CREATE_LITERAL = 1;
//     static final int FP_CUT = 4;
//     static final int FP_COPY = 5;
//     static final int FP_PASTE = 6;
//     static final int FP_COMMENT = 3;
//     static final int FP_UNCOMMENT = 7;

    boolean CREATE_PREDICATE_STARTED=false;
    Vector pathForNewProperty; //list of LongPoints to create the VPath
    Vector tempSegments; //store segments temporarily representing the path (before we compute the VPath)
    IResource subjectForNewProperty; //remember first object we clicked on when we began creating the property (will be the subject)
    
    boolean resizing=false;  //true when resizing an object in the graph (so that release1 knows it has to do something)
    boolean moving=false; //true when moving a resource or literal in the graph (so that release1 knows it has to do something)
    boolean movingText=false; //true when moving a property's, resource's or literal's text in the graph (so that release1 knows it has to do something)
    boolean editingPath=false; //true when editing a path (by a moving a handle) (so that release1 knows it has to do something)
    ControlPoint whichHandle=null;  //set when clicking in a handle so that we do not have to retrieve it each time mouseDragged is called
    
    NewPropPanel propertyDialog=null;
    
    int selectWhat=NODES_ONLY;  //when selecting entities in a region, select just NODES (resources+literals) or EDGES (properties)
    static final int NODES_ONLY=0;
    static final int EDGES_ONLY=1;

//     // tells whether floating palette (and thus 2nd layer) is active or not
//     boolean fpIsActive = false;

//     static VImage[] fp_icons;

    EditorEvtHdlr(Editor appli){
	application=appli;
    }

    public synchronized void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (Editor.mView.getActiveLayer() == 0){
	    if (mod == META_MOD || mod == META_SHIFT_MOD){// move camera when command key is pressed (MacOS, single button mouse)
		application.rememberLocation(v.cams[0].getLocation());
		Editor.vsm.getActiveView().setStatusBarText("");
		lastJPX=jpx;
		lastJPY=jpy;
		v.setDrawDrag(true);
		dragging = true;
		Editor.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
		activeCam=Editor.vsm.getActiveCamera();
	    }
	    else {
		Editor.vsm.getActiveView().setStatusBarText("");
		Glyph g=v.lastGlyphEntered();
		switch (mode){
		case SINGLE_SELECTION_MODE:{
		    singleSelect(v.getMouse(), g, v.getMouse().vx, v.getMouse().vy, mod);
		    break;
		}
		case REGION_SELECTION_MODE:{
		    if (mod<2){application.unselectLastSelection();}
		    x1=v.getMouse().vx;
		    y1=v.getMouse().vy;
		    v.setDrawRect(true);
		    break;
		}
		case REGION_ZOOM_MODE:{
		    x1=v.getMouse().vx;
		    y1=v.getMouse().vy;
		    v.setDrawRect(true);
		    break;
		}
		case CREATE_RESOURCE_MODE:{
		    createResource(v.getMouse().vx, v.getMouse().vy, g);
		    break;
		}
		case CREATE_PREDICATE_MODE:{
		    createProperty(v.getMouse().getLocation(), g, mod);
		    break;
		}
		case CREATE_LITERAL_MODE:{
		    createLiteral(v.getMouse().vx, v.getMouse().vy, g);
		    break;
		}
		case MOVE_RESIZE_MODE:{
		    synchronized (EditorEvtHdlr.this){
			Glyph[] gum = v.getGlyphsUnderMouseList(); // give priority to resizing handles
			if (gum.length > 1){                       // in case the mouse is inside more than one glyph 
			    for (int i=gum.length-1;i>=0;i--){     // if mouse inside several resizing handles, take
				if (gum[i].getType().startsWith("rsz")){ // last one entered
				    g = gum[i];
				    break;
				}
			    }
			}
			if (g!=null){
			    String type=g.getType();
			    if (type.startsWith("rsz")){//resizing an object (ellipse, rectangle, table or path)
				Editor.vsm.stickToMouse(g);
				if (type.equals("rszp")){//path
				    whichHandle=(ControlPoint)g.getOwner();editingPath=true;
				    if (mod==2){whichHandle.dragSiblings(true);}
				    //hide the VPath (only display the broken line)
				    Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).hide(whichHandle.getPath());
				}//ellipse or rectangle or table
				else {
				    v.getMouse().setSensitivity(false);
				    resizing=true;
				}
			    }
			    else if (type.charAt(3)=='G'){//editing an INode's main glyph (display little black rectangles that will allow the actual resizing operation)
				if (type.equals(Editor.resShapeType)){
				    if (mod>=2){
					Vector vc=v.getMouse().getIntersectingTexts(Editor.vsm.getActiveCamera());
					if (vc!=null){//there is a text under the mouse
					    Glyph g2=(Glyph)vc.firstElement();
					    if (g2.getType().equals(Editor.resTextType)){Editor.vsm.stickToMouse(g2);movingText=true;} //move VText if Ctrl is down
					}
				    }
				    else {
					application.geomMngr.initResourceResizer((IResource)g.getOwner());
					moving=true;
					Editor.vsm.stickToMouse(g);  //will be unsticked from mouse if we click (do not drag, meaning we want to resize, not move)
				    }
				}
				else if (type.equals(Editor.litShapeType)){
				    if (mod>=2){
					Vector vc=v.getMouse().getIntersectingTexts(Editor.vsm.getActiveCamera());
					if (vc!=null){//there is a text under the mouse
					    Glyph g2=(Glyph)vc.firstElement();
					    if (g2.getType().equals(Editor.litTextType)){Editor.vsm.stickToMouse(g2);movingText=true;} //move VText if Ctrl is down
					}
				    }
				    else {
					ILiteral l=(ILiteral)g.getOwner();
					application.geomMngr.initLiteralResizer(l);
					moving=true;
					Editor.vsm.stickToMouse(g);  //will be unsticked from mouse if we click (do not drag, meaning we want to resize, not move)
				    }
				}
			    }
			    else if (type.equals(Editor.propCellType)){//prdC
				application.geomMngr.initPropCellResizer((IProperty)g.getOwner());
				moving=true;
				Editor.vsm.stickToMouse(g);
			    }
			    else if (type.equals(Editor.propHeadType)){//move/resize the corresponding VPath if clicking on the path's head
				application.geomMngr.initPropertyResizer((IProperty)g.getOwner());
			    }
			}
			else {
			    Vector vc=v.getMouse().getIntersectingTexts(Editor.vsm.getActiveCamera());
			    if (vc!=null){//there is a text under the mouse
				Glyph g2=(Glyph)vc.firstElement();
				if (g2.getType().equals(Editor.propTextType)){Editor.vsm.stickToMouse(g2);movingText=true;Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).onTop(g2);} //move only if it is a Property's VText
				else if (mod>=2 && (g2.getType().equals(Editor.resTextType) || g2.getType().equals(Editor.litTextType))){Editor.vsm.stickToMouse(g2);movingText=true;} //or if it is a resouce's or literal's text and Ctrl is down
			    }
			    else if ((vc=v.getMouse().getIntersectingPaths(Editor.vsm.getActiveCamera()))!=null){
				Glyph g2=(Glyph)vc.firstElement();
				if (g2.getType().equals(Editor.propPathType)){application.geomMngr.initPropertyResizer((IProperty)g2.getOwner());}
			    }
			    else {application.geomMngr.destroyLastResizer();}
			}
			break;
		    }
		}
		case PASTE_MODE:{
		    paste(v.getMouse().vx, v.getMouse().vy);
		    break;
		}
		case COMMENT_REGION_MODE:{
		    x1=v.getMouse().vx;
		    y1=v.getMouse().vy;
		    v.setDrawRect(true);
		    break;
		}
		case UNCOMMENT_REGION_MODE:{
		    x1=v.getMouse().vx;
		    y1=v.getMouse().vy;
		    v.setDrawRect(true);
		    break;
		}
		case COMMENT_SINGLE_MODE:{
		    comment(v.getMouse(), g, v.getMouse().vx, v.getMouse().vy);
		    break;
		}
		case UNCOMMENT_SINGLE_MODE:{
		    uncomment(v.getMouse(), g, v.getMouse().vx, v.getMouse().vy);
		    break;
		}
		}
	    }
	}
// 	else {// active layer is floating palette
// 	    Glyph g = v.lastGlyphEntered();
// 	    if (g != null){
// 		switch (getSelectedFPIcon(g)){
// 		case FP_CREATE_RESOURCE:{
// 		    LongPoint mouseInGraphSpace = v.getMouse().getVSCoordinates(Editor.mSpace.getCamera(0), v);
// 		    Vector glyphsInGraphSpace = v.getMouse().getIntersectingGlyphs(Editor.mSpace.getCamera(0));
// 		    createResource(mouseInGraphSpace.x, mouseInGraphSpace.y,
// 				   (glyphsInGraphSpace != null) ? (Glyph)glyphsInGraphSpace.lastElement() : null);
// 		    break;
// 		}
// 		case FP_CREATE_PROPERTY:{
// 		    Vector glyphsInGraphSpace = v.getMouse().getIntersectingGlyphs(Editor.mSpace.getCamera(0));
// 		    createProperty(v.getMouse().getVSCoordinates(Editor.mSpace.getCamera(0), v), 
// 				   (glyphsInGraphSpace != null) ? (Glyph)glyphsInGraphSpace.lastElement() : null,
// 				   mod);
// 		    break;
// 		}
// 		case FP_CREATE_LITERAL:{
// 		    LongPoint mouseInGraphSpace = v.getMouse().getVSCoordinates(Editor.mSpace.getCamera(0), v);
// 		    Vector glyphsInGraphSpace = v.getMouse().getIntersectingGlyphs(Editor.mSpace.getCamera(0));
// 		    createLiteral(mouseInGraphSpace.x, mouseInGraphSpace.y,
// 				  (glyphsInGraphSpace != null) ? (Glyph)glyphsInGraphSpace.lastElement() : null);
// 		    break;
// 		}
// 		case FP_CUT:{
// 		    LongPoint mouseInGraphSpace = v.getMouse().getVSCoordinates(Editor.mSpace.getCamera(0), v);
// 		    Vector glyphsInGraphSpace = v.getMouse().getIntersectingGlyphs(Editor.mSpace.getCamera(0));
// 		    cut(v.getMouse(),
// 			(glyphsInGraphSpace != null) ? (Glyph)glyphsInGraphSpace.lastElement() : null,
// 			mouseInGraphSpace.x, mouseInGraphSpace.y);
// 		    break;
// 		}
// 		case FP_COPY:{
// 		    LongPoint mouseInGraphSpace = v.getMouse().getVSCoordinates(Editor.mSpace.getCamera(0), v);
// 		    Vector glyphsInGraphSpace = v.getMouse().getIntersectingGlyphs(Editor.mSpace.getCamera(0));
// 		    copy(v.getMouse(),
// 			 (glyphsInGraphSpace != null) ? (Glyph)glyphsInGraphSpace.lastElement() : null,
// 			 mouseInGraphSpace.x, mouseInGraphSpace.y);
// 		    break;
// 		}
// 		case FP_PASTE:{
// 		    LongPoint mouseInGraphSpace = v.getMouse().getVSCoordinates(Editor.mSpace.getCamera(0), v);
// 		    paste(mouseInGraphSpace.x, mouseInGraphSpace.y);
// 		    break;
// 		}
// 		case FP_COMMENT:{
// 		    LongPoint mouseInGraphSpace = v.getMouse().getVSCoordinates(Editor.mSpace.getCamera(0), v);
// 		    Vector glyphsInGraphSpace = v.getMouse().getIntersectingGlyphs(Editor.mSpace.getCamera(0));
// 		    comment(v.getMouse(),
// 			    (glyphsInGraphSpace != null) ? (Glyph)glyphsInGraphSpace.lastElement() : null,
// 			    mouseInGraphSpace.x,
// 			    mouseInGraphSpace.y);
// 		    break;
// 		}
// 		case FP_UNCOMMENT:{
// 		    LongPoint mouseInGraphSpace = v.getMouse().getVSCoordinates(Editor.mSpace.getCamera(0), v);
// 		    Vector glyphsInGraphSpace = v.getMouse().getIntersectingGlyphs(Editor.mSpace.getCamera(0));
// 		    uncomment(v.getMouse(),
// 			      (glyphsInGraphSpace != null) ? (Glyph)glyphsInGraphSpace.lastElement() : null,
// 			      mouseInGraphSpace.x,
// 			      mouseInGraphSpace.y);
// 		    break;
// 		}
// 	    }
// 	    }
// 	}
    }

    public synchronized void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (mod == META_MOD || mod == META_SHIFT_MOD || dragging){
	    Editor.vsm.animator.Xspeed=0;
	    Editor.vsm.animator.Yspeed=0;
	    Editor.vsm.animator.Aspeed=0;
	    v.setDrawDrag(false);
	    dragging = false;
	    Editor.vsm.activeView.mouse.setSensitivity(true);
	}
	else {
	switch (mode){
	case REGION_SELECTION_MODE:{
	    v.setDrawRect(false);
	    x2=v.getMouse().vx;
	    y2=v.getMouse().vy;
	    Vector selectedGlyphs=Editor.vsm.getGlyphsInRegion(x1,y1,x2,y2,Editor.mainVirtualSpace,VirtualSpaceManager.VISIBLE_GLYPHS);
	    if (selectedGlyphs!=null){
		Glyph g; 
		INode n;
		for (int i=0;i<selectedGlyphs.size();i++){
		    g=(Glyph)selectedGlyphs.elementAt(i);
		    //we are only selecting based on the main glyph for each kind of graph entity (identified by G as its 4th char)
		    if (g.getType().charAt(3)=='G'){
			n=(INode)g.getOwner();
			//select only appropriate entities
			if (selectWhat==NODES_ONLY){
			    if (g.getType().equals(Editor.litShapeType)){
				application.selectLiteral((ILiteral)n,true);
				if (mod==1 || mod==3){//SHIFT is down - select nodes/edges associated with the actual selection
				    application.selectPropertiesOfLiteral((ILiteral)n);
				}
			    }
			    else if (g.getType().equals(Editor.resShapeType)){
				application.selectResource((IResource)n,true);
				if (mod==1 || mod==3){//SHIFT is down - select nodes/edges associated with the actual selection
				    application.selectPropertiesOfResource((IResource)n);
				}
			    }
			}
			else if (selectWhat==EDGES_ONLY && g.getType().equals(Editor.propPathType)){
			    application.selectPredicate((IProperty)n,true,true);
			    if (mod==1 || mod==3){//SHIFT is down - select nodes/edges associated with the actual selection
				application.selectNodesOfProperty((IProperty)n);
			    }
			}
		    }
		}
	    }
	    break;
	}
	case REGION_ZOOM_MODE:{
	    v.setDrawRect(false);
	    x2=v.getMouse().vx;
	    y2=v.getMouse().vy;
	    application.rememberLocation(Editor.vsm.getActiveCamera().getLocation());
	    Editor.vsm.centerOnRegion(Editor.vsm.getActiveCamera(),ConfigManager.ANIM_DURATION,x1,y1,x2,y2);
	    break;
	}
	case COMMENT_REGION_MODE:{
	    v.setDrawRect(false);
	    x2=v.getMouse().vx;
	    y2=v.getMouse().vy;
	    Vector selectedGlyphs=Editor.vsm.getGlyphsInRegion(x1,y1,x2,y2,Editor.mainVirtualSpace,VirtualSpaceManager.VISIBLE_GLYPHS);
	    if (selectedGlyphs!=null){
		Glyph g; 
		for (int i=0;i<selectedGlyphs.size();i++){
		    g=(Glyph)selectedGlyphs.elementAt(i);
		    //we are only selecting based on the main glyph for each kind of graph entity (identified by G as its 4th char)
		    if (g.getType().charAt(3)=='G'){
			//select only resources and literals  (properties will be commented out by these if necessary)
			if (g.getType().equals(Editor.litShapeType) || g.getType().equals(Editor.resShapeType)){application.commentNode((INode)g.getOwner(),true,true);}
		    }
		}
	    }
	    break;
	}
	case UNCOMMENT_REGION_MODE:{
	    v.setDrawRect(false);
	    x2=v.getMouse().vx;
	    y2=v.getMouse().vy;
	    Vector selectedGlyphs=Editor.vsm.getGlyphsInRegion(x1,y1,x2,y2,Editor.mainVirtualSpace,VirtualSpaceManager.VISIBLE_GLYPHS);
	    if (selectedGlyphs!=null){
		Glyph g; 
		for (int i=0;i<selectedGlyphs.size();i++){
		    g=(Glyph)selectedGlyphs.elementAt(i);
		    //we are only selecting based on the main glyph for each kind of graph entity (identified by G as its 4th char)
		    if (g.getType().charAt(3)=='G'){
			//select only resources and literals  (properties will be commented out by these if necessary)
			if (g.getType().equals(Editor.litShapeType) || g.getType().equals(Editor.resShapeType)){application.commentNode((INode)g.getOwner(),false,true);}
		    }
		}
	    }
	    break;
	}
	case MOVE_RESIZE_MODE:{
	    if (resizing){
		resizing=false;
		v.getMouse().setSensitivity(true);
		application.geomMngr.endResize();
	    }
	    else if (moving){
		moving=false;
		application.geomMngr.endMove();
	    }
	    else if (movingText){Editor.vsm.unstickFromMouse();movingText=false;}
	    else if (editingPath){
		//first check that we have not changed the subject/object of the statement
		Glyph[] ggum=v.getMouse().getStickedGlyphArray();
		if (ggum.length > 0 && ggum[0].getType().equals("rszp")){
		    ControlPoint cp=(ControlPoint)ggum[0].getOwner();
		    if (cp.type==ControlPoint.START_POINT){
			Glyph[] gum = v.getMouse().getGlyphsUnderMouseList();
			Glyph subj=cp.owner.prop.getSubject().getGlyph();
			IResource r;
			if (!Utilities.containsGlyph(gum, subj) &&
			    ((r=insideAnIResource(gum)) != null)){//mouse is being released in a node that is not the original subject for this predicate
			    Editor.changePropertySubject(cp.owner.prop,r);
			}
		    }
		    else if (cp.type==ControlPoint.END_POINT){
			Glyph[] gum = v.getMouse().getGlyphsUnderMouseList();
			Glyph obj=cp.owner.prop.getObject().getGlyph();
			INode n;
			if (!Utilities.containsGlyph(gum, obj) &&
			    ((n=insideAnINode(gum)) != null)){//mouse is being released in a node that is not the original subject for this predicate
			    Editor.changePropertyObject(cp.owner.prop,n);
			}
		    }
		}
		//then get rid of the resizer (must do it after so that start and end points get adjusted w.r.t the new subject/object if changed)
		Editor.vsm.unstickFromMouse();editingPath=false;
		application.geomMngr.updatePathAfterResize();
		Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).show(whichHandle.getPath());
		whichHandle.dragSiblings(false);
		whichHandle=null;
	    }
	    break;
	}
	}

	}
    }

    public synchronized void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (mod == META_MOD || mod == META_SHIFT_MOD){
	    Glyph g=v.lastGlyphEntered();
	    if (mode==CREATE_PREDICATE_MODE && CREATE_PREDICATE_STARTED){
		cancelStartedPredicate();
	    }
	    else {
		if (g!=null){
		    application.rememberLocation(v.cams[0].getLocation());
		    Editor.vsm.centerOnGlyph(g,v.cams[0],ConfigManager.ANIM_DURATION);
		}
		else {//we might be clicking on a predicate (no enter/exit event is fired when the mouse overlaps a VPath or a VText, test has to be done manually)
		    Vector vc=v.getMouse().getIntersectingTexts(Editor.vsm.getActiveCamera());
		    if (vc!=null){//there is a text under the mouse
			application.rememberLocation(v.cams[0].getLocation());
			Editor.vsm.centerOnGlyph((Glyph)vc.firstElement(),Editor.vsm.getActiveCamera(),ConfigManager.ANIM_DURATION);
		    }
		    else if ((vc=v.getMouse().getIntersectingPaths(Editor.vsm.getActiveCamera()))!=null){
			//no text under mouse, but there might be a path
			application.rememberLocation(v.cams[0].getLocation());
			Editor.vsm.centerOnGlyph((Glyph)vc.firstElement(),Editor.vsm.getActiveCamera(),ConfigManager.ANIM_DURATION);
		    }
		}
	    }
	}
	else {
	    switch (mode){
	    case SINGLE_SELECTION_MODE:{//if double clicking on a resource, try to display its content in a web browser
		if (clickNumber==2){
		    Glyph g=v.lastGlyphEntered();
		    if (g!=null && g.getType().equals(Editor.resShapeType)){
			application.displayURLinBrowser((IResource)g.getOwner());
		    }
		}
		break;
	    }
	    }
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public synchronized void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.rememberLocation(v.cams[0].getLocation());
	Editor.vsm.getActiveView().setStatusBarText("");
	lastJPX=jpx;
	lastJPY=jpy;
	v.setDrawDrag(true);
	dragging = true;
	Editor.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=Editor.vsm.getActiveCamera();
    }

    public synchronized void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	Editor.vsm.animator.Xspeed=0;
	Editor.vsm.animator.Yspeed=0;
	Editor.vsm.animator.Aspeed=0;
	v.setDrawDrag(false);
	dragging = false;
	Editor.vsm.activeView.mouse.setSensitivity(true);
    }

    public synchronized void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	Glyph g=v.lastGlyphEntered();
	if (mode==CREATE_PREDICATE_MODE && CREATE_PREDICATE_STARTED){
	    cancelStartedPredicate();
	}
	else {
	    if (g!=null){
		application.rememberLocation(v.cams[0].getLocation());
		Editor.vsm.centerOnGlyph(g,v.cams[0],ConfigManager.ANIM_DURATION);
	    }
	    else {//we might be clicking on a predicate (no enter/exit event is fired when the mouse overlaps a VPath or a VText, test has to be done manually)
		Vector vc=v.getMouse().getIntersectingTexts(Editor.vsm.getActiveCamera());
		if (vc!=null){//there is a text under the mouse
		    application.rememberLocation(v.cams[0].getLocation());
		    Editor.vsm.centerOnGlyph((Glyph)vc.firstElement(),Editor.vsm.getActiveCamera(),ConfigManager.ANIM_DURATION);
		}
		else if ((vc=v.getMouse().getIntersectingPaths(Editor.vsm.getActiveCamera()))!=null){
		    //no text under mouse, but there might be a path
		    application.rememberLocation(v.cams[0].getLocation());
		    Editor.vsm.centerOnGlyph((Glyph)vc.firstElement(),Editor.vsm.getActiveCamera(),ConfigManager.ANIM_DURATION);
		}
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c=application.vsm.getActiveCamera();
	float a=(c.focal+Math.abs(c.altitude))/c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(a*10);
	    application.cameraMoved();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(-a*10);
	    application.cameraMoved();
	}
	application.vsm.repaintNow();
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	Editor.fresnelMngr.movedLens(jpx, jpy);
    }

    public synchronized void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	synchronized (EditorEvtHdlr.this){
	    if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
		tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
		if (mod == SHIFT_MOD || mod == META_SHIFT_MOD) {
		    application.vsm.animator.Xspeed=0;
		    application.vsm.animator.Yspeed=0;
		    application.vsm.animator.Aspeed=(activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor));
		}
		else {
		    application.vsm.animator.Xspeed=(activeCam.altitude>0) ? (long)((jpx-lastJPX)*(tfactor/cfactor)) : (long)((jpx-lastJPX)/(tfactor*cfactor));
		    application.vsm.animator.Yspeed=(activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor));
		    application.vsm.animator.Aspeed=0;
		}
		//application.updateRadarRegionRect();
	    }
	    else if (buttonNumber==1){//dragging a resizer handle
		if (resizing){application.geomMngr.resize(v.lastGlyphEntered());}  //for both we could store lastGlyphEntered.getowner()
		else if (moving){application.geomMngr.move(v.lastGlyphEntered());} //instead of accessing it each time
		else if (editingPath){whichHandle.update();}
	    }
	    // 	else if (buttonNumber==2){System.err.println(v.getMouse().glyphsUnderMouse[0]);}
	}
    }

    public void enterGlyph(Glyph g){
	// border color
	g.highlight(true, null);
	//if entering a resource or literal, display its value in the status bar text
	if (Editor.mView.getActiveLayer() == 0){
	    try {
		if (g.getType().charAt(3)=='G' && g.getOwner()!=null){
		    INode i = (INode)g.getOwner();
		    Editor.vsm.getActiveView().setStatusBarText(application.processNodeTextForSB(i));
		    Editor.fresnelMngr.enteringNode(i);
		}
	    }
	    catch (StringIndexOutOfBoundsException ex){}
	}
	else {// floating palette
	    Editor.fpSpace.onTop(g);
	}
    }

    public void exitGlyph(Glyph g){
	// border color
	g.highlight(false, ConfigManager.selectionColorTB);
	// unrender a node (if seen through a Fresnel lens)
	try {
	    if (g.getType().charAt(3)=='G' && g.getOwner()!=null){
		Editor.fresnelMngr.exitingNode((INode)g.getOwner());
	    }
	}
	catch (StringIndexOutOfBoundsException ex){}
    }

    int lastKeyPressedCode = KeyEvent.VK_UNDEFINED;

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	lastKeyPressedCode = code;
	if (mod==0){//pressing no modifier
	    if (code == KeyEvent.VK_DELETE){application.deleteSelectedEntities();}
	    else if (code == KeyEvent.VK_PLUS){
		Glyph gl;
		if (mode == MOVE_RESIZE_MODE && v.lastGlyphEntered() != null &&
		    (gl=mouseInsideAPathCP(v.getGlyphsUnderMouseList())) != null){
		    application.geomMngr.insertSegmentInPath(gl);
		}
	    }
	    else if (code == KeyEvent.VK_MINUS){
		Glyph gl;
		if (mode == MOVE_RESIZE_MODE && v.lastGlyphEntered() != null &&
		    (gl=mouseInsideAPathCP(v.getGlyphsUnderMouseList())) != null){
		    application.geomMngr.deleteSegmentInPath(gl);
		}
	    }
	    else if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	    else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	    else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
	    else if (code==KeyEvent.VK_UP){application.translateView(Editor.MOVE_UP);}
	    else if (code==KeyEvent.VK_DOWN){application.translateView(Editor.MOVE_DOWN);}
	    else if (code==KeyEvent.VK_LEFT){application.translateView(Editor.MOVE_LEFT);}
	    else if (code==KeyEvent.VK_RIGHT){application.translateView(Editor.MOVE_RIGHT);}
	}
	else if (mod==2){
	    if (code==KeyEvent.VK_Z){application.undo();}
	    else if (code==KeyEvent.VK_X){application.cutSelection();}
	    else if (code==KeyEvent.VK_C){application.copySelection();}
	    else if (code==KeyEvent.VK_V){application.pasteSelection(v.getMouse().vx,v.getMouse().vy);}
	    else if (code==KeyEvent.VK_A){application.selectAllNodes();}
	    else if (code==KeyEvent.VK_G){application.getGlobalView();}
	    else if (code==KeyEvent.VK_B){application.moveBack();}
	    else if (code==KeyEvent.VK_R){application.showRadarView(true);}
// 	    else if (code==KeyEvent.VK_L){
// 		application.cmp.lensMn.setSelected(!application.cmp.lensMn.isSelected());
// 		application.geomMngr.setLens(application.cmp.lensMn.isSelected());
// 	    }
	    else if (code==KeyEvent.VK_E){application.showErrorMessages();}
	    else if (code==KeyEvent.VK_N){application.promptReset();}
	    else if (code==KeyEvent.VK_O){application.openProject();}
	    else if (code==KeyEvent.VK_S){application.saveProject();}
	    else if (code==KeyEvent.VK_P){application.printRequest();}
	}
	else if (mod==1){
	    if (c=='+'){
		Glyph gl;
		if (mode == MOVE_RESIZE_MODE && v.lastGlyphEntered() != null &&
		    (gl=mouseInsideAPathCP(v.getGlyphsUnderMouseList())) != null){
		    application.geomMngr.insertSegmentInPath(gl);
		}
	    }
	    else if (c=='-'){
		Glyph gl;
		if (mode == MOVE_RESIZE_MODE && v.lastGlyphEntered() != null &&
		    (gl=mouseInsideAPathCP(v.getGlyphsUnderMouseList())) != null){
		    application.geomMngr.deleteSegmentInPath(gl);
		}
	    }
	}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){application.exit();}

    //doing a single select/unselect operation on a node/edge
    //if shift is down, means we also want to select:
    //     -all edges attached to the node we are actually selecting
    //     -both nodes attached to the edge we are actually selecting
    void select(Glyph g,boolean isShiftDown){
	INode n=(INode)g.getOwner();
	if (g.getType().startsWith("res")){
	    IResource r=(IResource)n;
	    application.selectResource(r,!r.isSelected());
	    if (isShiftDown && r.isSelected()){//select associated properties only if selecting (not unselecting)
		application.selectPropertiesOfResource(r);//and if SHIFT is pressed
	    }
	    if (r.isSelected()){//show node attributes in PropsPanel
		application.propsp.updateDisplay(r);
		application.updatePropertyBrowser(r);
	    }
	}
	else if (g.getType().startsWith("lit")){
	    ILiteral l=(ILiteral)n;
	    application.selectLiteral(l,!l.isSelected());
	    if (isShiftDown && l.isSelected()){//select associated property only if selecting (not unselecting)
		application.selectPropertiesOfLiteral(l);//and if SHIFT is pressed
	    }
	    if (l.isSelected()){application.propsp.updateDisplay(l);}//show node attributes in PropsPanel
	}
	else if (g.getType().startsWith("prd")){
	    IProperty p=(IProperty)n;
	    application.selectPredicate(p,!n.isSelected(),g.getType().equals(Editor.propPathType) || g.getType().equals(Editor.propHeadType));
	    if (isShiftDown && p.isSelected()){//select associated nodes only if selecting (not unselecting)
		application.selectNodesOfProperty(p);//and if SHIFT is pressed
	    }
	    if (p.isSelected()){application.propsp.updateDisplay(p);}//show edge attributes in PropsPanel
	}
    }

    void singleSelect(VCursor mouse, Glyph g, long vx, long vy, int mod){
	if (g != null){
	    if (mod<2){application.unselectLastSelection();}//CTRL not pressed
	    if (mod==0 || mod==2){select(g,false);}//SHIFT not pressed
	    else {select(g,true);}
	}
	else {
	    Vector vc = mouse.getIntersectingTexts(Editor.mSpace.getCamera(0), vx, vy);
	    if (vc!=null){//there is a text under the mouse
		if (mod<2){application.unselectLastSelection();}
		//we might accidentally have selected several texts - just take the first one in the list
		if (mod==0 || mod==2){select((Glyph)vc.firstElement(),false);}//SHIFT not pressed
		else {select((Glyph)vc.firstElement(),true);}
	    }
	    else if ((vc=mouse.getIntersectingPaths(Editor.mSpace.getCamera(0), 5, vx, vy))!=null){//no text under mouse, but there might be a path
		if (mod<2){application.unselectLastSelection();}
		if (mod==0 || mod==2){select((Glyph)vc.firstElement(),false);}//SHIFT not pressed
		else {select((Glyph)vc.firstElement(),true);}
	    }
	    else {//unselect everything if user clicks in empty region without Ctrl
		if (mod<2){application.unselectLastSelection();}
	    }
	}
    }

    void cancelStartedPredicate(){
	pathForNewProperty=null;
	VirtualSpace vs=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace);
	for (int i=0;i<tempSegments.size();i++){//get rid of temporary segments representing the path
	    vs.destroyGlyph((Glyph)tempSegments.elementAt(i));
	}
	tempSegments=null;
	subjectForNewProperty=null;
	CREATE_PREDICATE_STARTED=false;
    }

    void showPropertyDialog(){
	propertyDialog=new NewPropPanel(application);
    }

    private IResource insideAnIResource(Glyph[] glyphs){
	Object o;
	for (int i=glyphs.length-1;i>=0;i--){
	    if ((o=glyphs[i].getOwner()) != null && o instanceof IResource){return (IResource)o;}
	}
	return null;
    }
    
    private INode insideAnINode(Glyph[] glyphs){
	Object o;
	for (int i=glyphs.length-1;i>=0;i--){
	    if ((o=glyphs[i].getOwner()) != null && (o instanceof IResource || o instanceof ILiteral)){return (INode)o;}
	}
	return null;
    }

    private Glyph mouseInsideAPathCP(Glyph[] glyphs){
	for (int i=0;i<glyphs.length;i++){
	    if (glyphs[i].getType().equals("rszp")){return glyphs[i];}
	}
	return null;
    }

//     int getSelectedFPIcon(Glyph g){
// 	for (int i=0;i<fp_icons.length;i++){
// 	    if (g == fp_icons[i]){
// 		return i;
// 	    }
// 	}
// 	return -1;
//     }

//     void activateFloatingPalette(boolean activate){
// 	fpIsActive = activate;
// 	if (activate){
// 	    showFloatingPalette(true);
// 	    Editor.mView.setActiveLayer(1);
// 	}
// 	else {
// 	    Editor.mView.setActiveLayer(0);
// 	    showFloatingPalette(false);	    
// 	}
//     }

//     void showFloatingPalette(boolean b){
// 	LongPoint offset;
// 	Camera c = Editor.fpSpace.getCamera(0);
// 	if (b){
// 	    offset = new LongPoint(-c.posx+34, -c.posy+11);
// 	}
// 	else {
// 	    offset = new LongPoint(0, 500);
// 	}
// 	Editor.vsm.animator.createCameraAnimation(ConfigManager.ANIM_DURATION, AnimManager.CA_TRANS_SIG,
// 						  offset, c.getID());
//     }

    void createResource(long vx, long vy, Glyph g){
	if (g == null){//we do not want to create a new resource when the user clicks inside something else
	    application.createNewResource(vx, vy);
	}
	else {
	    if (g.getType() != null && g.getType().equals(Editor.resShapeType)){
		application.propsp.updateDisplay((INode)g.getOwner());
	    }
	}
    }

    void createProperty(LongPoint mouseLocation, Glyph g, int mod){
	if (propertyDialog != null){//a property constructor dialog is opened, we are selecting the subject or object
	    propertyDialog.toFront();
	    if (g != null && (g.getType().startsWith("res") || g.getType().startsWith("lit"))){
		propertyDialog.setSubjectOrObject(g);
	    }
	}
	else {//constructing the property by selecting the subject, then drawing intermediate points, then selecting object
	    //(the property type must have been selected in the def table)
	    if (g != null){//we are either beginning or finishing the creation of a property
		Object o = g.getOwner();
		if (mod == ViewEventHandler.CTRL_MOD){
		    showPropertyDialog();
		    if (g != null){
			if (g.getType().startsWith("res")){propertyDialog.setSubject(g);propertyDialog.setFocusToObject();}
			else if (g.getType().startsWith("lit")){propertyDialog.setObject(g);propertyDialog.setFocusToSubject();}
		    }
		}
		else {
		    if (CREATE_PREDICATE_STARTED){//already started - means we are clicking on the object for this property
			if ((o instanceof IResource) || (o instanceof ILiteral)){//object can be a literal or a resource
			    CREATE_PREDICATE_STARTED = false;
			    INode n = (INode)o;    //object of the statement
			    pathForNewProperty.add(new LongPoint(n.getGlyph().vx,n.getGlyph().vy));
			    application.createNewProperty(subjectForNewProperty,n,pathForNewProperty);
			    subjectForNewProperty = null;
			    VirtualSpace vs = Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace);
			    for (int i = 0;i<tempSegments.size();i++){//get rid of temporary segments representing the path
				vs.destroyGlyph((Glyph)tempSegments.elementAt(i));
			    }
			    tempSegments = null;
			}
			else {Editor.vsm.getActiveView().setStatusBarText("Object must be a resource or a literal");}
		    }
		    else {//not started yet - means we are clicking on the subject for this property
			if ((application.selectedPropertyConstructorNS != null) && (application.selectedPropertyConstructorLN != null)){
			    if (o instanceof IResource){//subject must be a resource
				Editor.vsm.getActiveView().setStatusBarText("Specify intermediate path points (click in empty regions) or select this statement's object (click in a node)");
				CREATE_PREDICATE_STARTED = true;
				subjectForNewProperty = (IResource)o;
				pathForNewProperty = new Vector();
				tempSegments = new Vector();
				pathForNewProperty.add(new LongPoint(subjectForNewProperty.getGlyph().vx,subjectForNewProperty.getGlyph().vy));
			    }
			    else {Editor.vsm.getActiveView().setStatusBarText("Subject must be a resource");}
			}
			else {Editor.vsm.getActiveView().setStatusBarText("Select a property from the list in the Property tab");}
		    }
		}
	    }
	    else {//clicked in an empty region
		if (CREATE_PREDICATE_STARTED){//we are drawing the property's edge using
		    //a broken line that will be converted in a VPath when it is finished
		    Editor.vsm.getActiveView().setStatusBarText("Specify intermediate path points (click in empty regions) or select this statement's object (click in a node)");
		    LongPoint lp = (LongPoint)pathForNewProperty.lastElement();
// 		    LongPoint mlp  =  mouse.getLocation();
		    pathForNewProperty.add(mouseLocation);
		    long x = (lp.x+mouseLocation.x)/2;
		    long y = (lp.y+mouseLocation.y)/2;
		    long w = (mouseLocation.x-lp.x)/2;
		    long h = (-mouseLocation.y+lp.y)/2;
		    VSegment s = new VSegment(x,y,0,w,h,ConfigManager.propertyColorB);
		    Editor.vsm.addGlyph(s,Editor.mainVirtualSpace);
		    tempSegments.add(s);
		}
		else {
		    /*we want to create a property using a dialog which will
		      allow to select subject and object by point&click*/
		    showPropertyDialog();
		}
	    }
	}
    }

    void createLiteral(long vx, long vy, Glyph g){
	if (g == null){//we do not want to create a new resource when the user clicks inside something else
	    application.createNewLiteral(vx, vy);
	}
	else {
	    if (g.getType() != null && g.getType().equals(Editor.litShapeType)){
		application.propsp.updateDisplay((INode)g.getOwner());
	    }
	}
    }

    void cut(VCursor mouse, Glyph g, long vx, long vy){
	if (g != null){
	    singleSelect(mouse, g, vx, vy, 0);// do not take modifiers into account
	    application.cutSelection();
	}
    }

    void copy(VCursor mouse, Glyph g, long vx, long vy){
	if (g != null){
	    singleSelect(mouse, g, vx, vy, 0);// do not take modifiers into account
	    application.copySelection();
	}
    }

    void paste(long vx, long vy){
	application.pasteSelection(vx, vy);
    }

    void comment(VCursor mouse, Glyph g, long vx, long vy){
	if (g != null){
	    if (g.getType().equals(Editor.litShapeType) || g.getType().equals(Editor.resShapeType)){
		application.commentNode((INode)g.getOwner(), true, true);
	    }
	    else if (g.getType().equals(Editor.propHeadType)){application.commentPredicate((IProperty)g.getOwner(), true, true);}
	}
	else {
	    Vector vc = mouse.getIntersectingTexts(Editor.mSpace.getCamera(0), vx, vy);
	    if (vc != null){//there is a text under the mouse
		g = (Glyph)vc.firstElement();
		if (g.getType().startsWith("prd")){application.commentPredicate((IProperty)g.getOwner(), true, true);}
	    }
	    else if ((vc=mouse.getIntersectingPaths(Editor.mSpace.getCamera(0), 5, vx, vy)) != null){//no text under mouse, but there might be a path
		g = (Glyph)vc.firstElement();
		if (g.getType().startsWith("prd")){application.commentPredicate((IProperty)g.getOwner(), true, true);}
	    }
	}
    }

    void uncomment(VCursor mouse, Glyph g, long vx, long vy){
	if (g != null){
	    if (g.getType().equals(Editor.litShapeType) || g.getType().equals(Editor.resShapeType)){
		application.commentNode((INode)g.getOwner(), false, true);
	    }
	    else if (g.getType().equals(Editor.propHeadType)){application.commentPredicate((IProperty)g.getOwner(), false, true);}
	}
	else {
	    Vector vc = mouse.getIntersectingTexts(Editor.mSpace.getCamera(0), vx, vy);
	    if (vc != null){//there is a text under the mouse
		g = (Glyph)vc.firstElement();
		if (g.getType().startsWith("prd")){application.commentPredicate((IProperty)g.getOwner(), false, true);}
	    }
	    else if ((vc=mouse.getIntersectingPaths(Editor.mSpace.getCamera(0), 5, vx, vy)) != null){//no text under mouse, but there might be a path
		g = (Glyph)vc.firstElement();
		if (g.getType().startsWith("prd")){application.commentPredicate((IProperty)g.getOwner(), false, true);}
	    }
	}
    }

}
