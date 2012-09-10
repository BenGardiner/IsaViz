/*   FILE: FresnelManager.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FresnelManager.java,v 1.21 2007/04/17 12:48:56 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.ImageIcon;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.glyphs.RectangleNR;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VSegmentST;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.PostAnimationAction;

import org.w3c.IsaViz.*;

public class FresnelManager implements Java2DPainter {

    /*color preferences*/
    public static Color grayedColorF = new Color(235, 235, 235);   //fill color of commented objects
    public static Color grayedColorT = new Color(200, 200, 200);  //text and border color of commented objects
    public static float grayFh,grayFs,grayFv,grayTh,grayTs,grayTv; //HSV coords

    private void prepareColors(){
	float[] hsv = Color.RGBtoHSB(grayedColorF.getRed(),
				     grayedColorF.getGreen(),
				     grayedColorF.getBlue(),
				     new float[3]);
	grayFh = hsv[0];
	grayFs = hsv[1];
	grayFv = hsv[2];
	hsv = Color.RGBtoHSB(grayedColorT.getRed(),
			     grayedColorT.getGreen(),
			     grayedColorT.getBlue(),
			     new float[3]);
	grayTh = hsv[0];
	grayTs = hsv[1];
	grayTv = hsv[2];
    }


    Editor application;
    FresnelPanel fresnelp;

    Lens currentLens;     // null if none
    Vector matchingNodes; // vector of INodes

    static final Lens NO_LENS = new Lens("No lens", "");
    
    /* lens geometry (on screen) */
    int lensX, lensY;
    int lensR = 100;
    int dlensR = 2 * lensR;
    int hlensR = lensR / 2;
    static final Color LENS_BOUNDARY_COLOR = new Color(240, 240, 240);

    /* animation of changes made to elements rendered by the lens */
    static final long ANIM_DURATION = 400;

    // lenses are stored in the JTable (no need to duplicate their list in a separate structure)
    Format[] formats = new Format[0];
    Group[] groups = new Group[0];

    static Hashtable url2icon = new Hashtable();

    FSLISVEvaluator fie;
    
    FresnelManager(FresnelPanel fp, Editor app){
	this.fresnelp = fp;
	this.application = app;
	this.application.fresnelMngr = this;
	addLens(NO_LENS);
	matchingNodes = new Vector();
	prepareColors();
    }
    
    void loadLenses(final File f, final int whichReader){
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    application.tblp.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
		    load(f, whichReader);
		    application.tblp.setCursor(java.awt.Cursor.getDefaultCursor());
		    return null; 
		}
	    };
	worker.start();
    }

    void load(File f, int whichReader){
	// then create Fresnel parser
	FresnelParser parser = new FresnelParser(this);
	parser.parse(f, whichReader);
	addLenses(parser.getLensDefinitions());
	addFormats(parser.getFormatDefinitions());
	addGroups(parser.getGroupDefinitions());
    }

    void addLenses(Lens[] lensList){
	for (int i=0;i<lensList.length;i++){
	    addLens(lensList[i]);
	}
    }

    void addFormats(Format[] formatList){
	formats = formatList;
	System.out.println("Found "+formats.length+" formats");
    }

    void addGroups(Group[] groupList){
	groups = groupList;
	for (int i=0;i<groups.length;i++){
	    for (int j=0;j<groups[i].lenses.length;j++){
		groups[i].lenses[j].addAssociatedFormats(groups[i].formats);
	    }
	}
	System.out.println("Found "+groups.length+" groups");
    }

    void addLens(Lens lens){
	Lens[] l = {lens};
	fresnelp.ltm.addRow(l);
    }

    void useLens(Lens l){
	if (l != null){
	    Editor.mView.setJava2DPainter(this, Java2DPainter.BACKGROUND);
	    l.printVisibility();
	    if (l == currentLens){return;}
	    // render all nodes and arcs with shades of gray
	    clearMatchingNodes();
	    currentLens = l;
	    fie = new FSLISVEvaluator(application, buildNSResolver(), application.fhs);
	    identifyMatchingNodes();
 	    grayAll(matchingNodes);
	}
	else {// don't use any lens
	    // render all nodes and arcs with shades of gray
	    Editor.mView.setJava2DPainter(null, Java2DPainter.BACKGROUND);
	    currentLens = null;
	    colorizeAll(matchingNodes);
	    clearMatchingNodes();
	}
    }

    void identifyMatchingNodes(){
	IResource r;
	Vector matchedResources = new Vector();
	Vector resourcesNotYetMatched = new Vector();
	// basic selectors
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    r = (IResource)e.nextElement();
	    if (currentLens.selectsByBIS(r) || currentLens.selectsByBCS(r)){
		matchedResources.add(r);
	    }
	    else {// this will be the start set for FSL path expression evaluations
		// (we do not care about matching again resources already selected by a basic selector)
		resourcesNotYetMatched.add(r);
	    }
	}
	// fsl selectors
	if (currentLens.fslInstanceDomains != null && resourcesNotYetMatched.size() > 0){
	    Vector pathInstances = new Vector();
	    Object match; // an IResource, actually
	    for (int i=0;i<currentLens.fslInstanceDomains.length;i++){
		pathInstances.clear();
		fie.evaluateNodePathR(currentLens.fslInstanceDomains[i], resourcesNotYetMatched, pathInstances);
		for (int j=0;j<pathInstances.size();j++){
		    match = ((Vector)pathInstances.elementAt(j)).elementAt(0);
		    matchedResources.add(match);
		    resourcesNotYetMatched.removeElement(match);
		}
		if (resourcesNotYetMatched.isEmpty()){
		    // not necessary to evaluate remaining domains as there
		    // is not resource left that is not already selected
		    break;
		}
	    }
	}
	for (int i=0;i<matchedResources.size();i++){
	    matchingNodes.add(matchedResources.elementAt(i));
	}

	//XXX: TBW: handle literals
    }

    void clearMatchingNodes(){
	matchingNodes.clear();
    }

    Format getAssociatedPropertyFormat(Lens l, IProperty p){
	Format res = null;
	Format[] formats = l.getAssociatedFormats();
	for (int i=0;i<formats.length;i++){
	    //XXX: we take the first format that matches
	    //     we should take the one with the biggest weight/specificity
	    //     we also have to check against FSL selectors (not implemented yet)
	    if (formats[i].selectsByBPS(p)){
		return formats[i];
	    }
	}
	return res;
    }

    public void enteringNode(INode n){
	if (currentLens == null){return;}
	if (matchingNodes.contains(n)){
	    currentLens.render((IResource)n, this);
	}
    }

    public void exitingNode(INode n){
	if (currentLens == null){return;}
	if (matchingNodes.contains(n)){
	    currentLens.unrender((IResource)n, this);
	}
    }

    void grayAll(Vector exceptions){
	IResource r;
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    r = (IResource)e.nextElement();
	    if (!exceptions.contains(r)){
		gray(r);
	    }
	}
	for (Enumeration e=application.propertiesByURI.elements();e.hasMoreElements();){
	    for (Enumeration e2=((Vector)e.nextElement()).elements();e2.hasMoreElements();){
		gray((IProperty)e2.nextElement(), true, false);
	    }
	}
	for (Enumeration e=application.literals.elements();e.hasMoreElements();){
	    gray((ILiteral)e.nextElement());
	}
    }

    void colorizeAll(Vector exceptions){
	IResource r;
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    r = (IResource)e.nextElement();
	    if (!exceptions.contains(r)){
		colorize(r);
	    }
	}
	for (Enumeration e=application.propertiesByURI.elements();e.hasMoreElements();){
	    for (Enumeration e2=((Vector)e.nextElement()).elements();e2.hasMoreElements();){
		colorize((IProperty)e2.nextElement(), true, false);
	    }
	}
	for (Enumeration e=application.literals.elements();e.hasMoreElements();){
	    colorize((ILiteral)e.nextElement());
	}
    }

    void gray(INode in){
	in.grayAnimated(ANIM_DURATION);
    }

    void gray(IProperty ip, boolean grayArcItself, boolean makeArcAppear){
	ip.grayAnimated(ANIM_DURATION, grayArcItself, makeArcAppear);
    }

    void colorize(INode in){
	in.colorizeAnimated(ANIM_DURATION);
    }

    void colorize(IProperty ip, boolean colorizeArcItself, boolean makeArcDisappear){
	ip.colorizeAnimated(ANIM_DURATION, colorizeArcItself, makeArcDisappear);
    }

    void bringCloser(INode in, NodeInfo ni, long nx, long ny, PostAnimationAction paa){
	in.translate(ni, ANIM_DURATION, nx, ny, paa);
    }

    void bringCloser(IProperty ip, ArcInfo ai, long nx, long ny, PostAnimationAction paa){
	ip.translate(ai, ANIM_DURATION, nx, ny, paa);
    }

    void changeLabel(IProperty ip, ArcInfo ai, String newLabel){
	if (newLabel != null){
	    ip.showFresnelLabel(ai, newLabel);
	}
    }

    void showAdditionalContent(VText v){
	Editor.vsm.addGlyph(v, Editor.mSpace);
    }

    void hideAdditionalContent(VText v){
	Editor.mSpace.destroyGlyph(v);
    }

    void putAway(NodeInfo ni){
	Editor.vsm.animator.createGlyphAnimation(ANIM_DURATION, 0 , AnimManager.GL_TRANS_SIG, ni.sl, ni.owner.getGlyph().getID(), null);
	Editor.vsm.animator.createGlyphAnimation(ANIM_DURATION, 0 , AnimManager.GL_TRANS_SIG, ni.tl, ni.owner.getGlyphText().getID(), null);
    }

    void putAway(ArcInfo ai){
	Editor.vsm.animator.createGlyphAnimation(ANIM_DURATION, 0 , AnimManager.GL_TRANS_SIG, ai.tl, ai.owner.getGlyphText().getID(), null);
    }

    static final float[] TEMP_ARC_ANIM_PARAMS = {0, 0, 0, 0, 0, 0, 1.0f};

    void createTemporaryArc(long x1, long y1, long x2, long y2, ArcInfo ai){
	VSegmentST s = new VSegmentST(x1, y1, 0, ConfigManager.propertyColorB, x2, y2, 0.0f);
	Editor.vsm.addGlyph(s, Editor.mSpace);
	ai.replacementArc = s;
	Editor.vsm.animator.createGlyphAnimation(ANIM_DURATION, AnimManager.GL_COLOR_LIN, TEMP_ARC_ANIM_PARAMS, s.getID());
    }

    void destroyTemporaryArc(ArcInfo ai){
	if (ai.replacementArc != null){
	    Editor.mSpace.destroyGlyph(ai.replacementArc);
	}
    }

    FSLNSResolver buildNSResolver(){
	FSLNSResolver res = new FSLNSResolver();
	// update FSL namespace resolver (in case there are FSL expressions)
	//XXX: prefix binding declarations from the Fresnel stylesheet sheet itself
	//     should also be added
	for (int i=0;i<application.tblp.nsTableModel.getRowCount();i++){
	    res.addPrefixBinding((String)application.tblp.nsTableModel.getValueAt(i,0),
				 (String)application.tblp.nsTableModel.getValueAt(i,1));
	}
	return res;
    }

    public void movedLens(int jpx, int jpy){
	if (currentLens != null){
	    lensX = jpx - lensR;
	    lensY = jpy - hlensR;
	    Editor.vsm.repaintNow();
	    //XXX: should test whether selectable nodes are within lens radius node or not
	    //     and (un)render them accordingly
	}
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(LENS_BOUNDARY_COLOR);
	g2d.fillOval(lensX, lensY, dlensR, lensR);
    }


    /* property value radius */
    static final double PVR = 300;
    static final double PLR = 150;

    /* returns absolute position of property values (nodes and arcs) when displayed through
       the lens, taking into account number of items and available screen real-estate*/
    static void computeTranslations(INode centralNode, LongPoint[] nodeP, LongPoint[] arcP){
	int nbItems = nodeP.length;
	long cx = centralNode.getGlyph().vx;
	long cy = centralNode.getGlyph().vy;
	double angle;
	double angleOffset;
	if (nbItems < 6){
	    angle = Math.PI / 4.0;
	    angleOffset = -Math.PI / 2.0 / ((double)(nbItems-1));
	}
	else {
	    angle = Math.PI / 2.0;
	    angleOffset = -Math.PI / ((double)(nbItems-1));
	}
	for (int i=0;i<nodeP.length;i++){
	    nodeP[i] = new LongPoint(Math.round(PVR*Math.cos(angle)+cx), Math.round(PVR*Math.sin(angle)+cy));
	    arcP[i] = new LongPoint(Math.round(PLR*Math.cos(angle)+cx), Math.round(PLR*Math.sin(angle)+cy));
	    angle += angleOffset;
	}
    }

    /*retrieve icon at iconURL and store it in memory*/
    static boolean storeIcon(URL iconURL){
	if (!url2icon.containsKey(iconURL)){
	    ImageIcon ii=new ImageIcon(iconURL);
	    if (ii!=null && ii.getIconWidth()>0 && ii.getIconHeight()>0){
		url2icon.put(iconURL,ii);
		return true;
	    }  //return false if retrieving the icon failed or the content is not an icon
	    else return false;
	}
	else {
	    // the ImageIcon was already stored only if it could be
	    // retrieved and the resource did contain an icon
	    return true;
	}
    }

    /*get the in-memory ImageIcon of icon at iconURL*/
    static ImageIcon getIcon(URL iconURL){
 	if (url2icon.containsKey(iconURL)){
	    return (ImageIcon)url2icon.get(iconURL);
 	}
 	else {
 	    if (storeIcon(iconURL)){return (ImageIcon)url2icon.get(iconURL);}
 	    else {return null;}
 	}
    }


}
