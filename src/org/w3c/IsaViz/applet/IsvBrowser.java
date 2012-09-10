/*   FILE: IsvBrowser.java
 *   DATE OF CREATION:   Wed Apr 23 09:31:11 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz.applet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;
import java.io.IOException;

import java.io.*;
import java.net.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.engine.Location;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.apache.xerces.dom.DOMImplementationImpl;


public class IsvBrowser extends JApplet implements ActionListener,KeyListener {

    static Font swingFont=new Font("Helvetica",Font.BOLD,10);
    static Color resourceColorF=new Color(115,191,115);   //fill color of resources  index=0 in colors
    static Color resourceColorTB=new Color(66,105,66);    //text and border color of resources index=1 in colors
    static Color propertyColorB=new Color(90,89,206);     //border color of predicates index=2 in colors
    static Color propertyColorT=new Color(90,89,206);     //text color of predicates index=3 in colors
    static Color literalColorF=new Color(255,223,123);    //fill color of literals index=4 in colors
    static Color literalColorTB=new Color(132,117,66);    //text and border color of literals index=5 in colors
    float resTBh=0.33333334f;
    float resTBs=0.37142858f;
    float resTBv=0.4117647f;
    float prpTh=0.6680911f;
    float prpTs=0.56796116f;
    float prpTv=0.80784315f;
    float litTBh=0.12878788f;
    float litTBs=0.5f;
    float litTBv=0.5176471f;



    static VirtualSpaceManager vsm;
    static IsvAppletEvtHdlr evt;
    
    static String rdfVS="rdfvs";
    static String vtmView="IsaViz Applet Browser";

    static int viewWidth=640;
    static int viewHeight=480;

    /*remember previous camera locations so that we can get back*/
    static final int MAX_PREV_LOC=10;
    static Vector previousLocations;

    static int ANIM_MOVE_LENGTH=300;

    /*translation constants*/
    static short MOVE_UP=0;
    static short MOVE_DOWN=1;
    static short MOVE_LEFT=2;
    static short MOVE_RIGHT=3;
    static short MOVE_UP_LEFT=4;
    static short MOVE_UP_RIGHT=5;
    static short MOVE_DOWN_LEFT=6;
    static short MOVE_DOWN_RIGHT=7;

    static JLabel statusBar;
    JButton helpBt,aboutBt;
    JButton mvNBt,mvNEBt,mvEBt,mvSEBt,mvSBt,mvSWBt,mvWBt,mvNWBt,mvHBt,zmIBt,zmOBt;

    // This is a hack to avoid an ugly error message in 1.1.
    public IsvBrowser() {
        getRootPane().putClientProperty("defeatSystemEventQueueCheck",Boolean.TRUE);
    }

    public void init() {
	this.addKeyListener(this);
	previousLocations=new Vector();
	try {
	    int w=Integer.parseInt(getParameter("width"));
	    int h=Integer.parseInt(getParameter("height"));
	    if (w>0){viewWidth=w;}
	    if (h>0){viewHeight=h;}
	}
	catch (Exception ex){}
	getContentPane().setBackground(Color.white);
	//getContentPane().setLayout(new BorderLayout());
	getContentPane().setLayout(new FlowLayout());
	vsm=new VirtualSpaceManager(true);
	vsm.setMainFont(swingFont);
	vsm.setMouseInsideGlyphColor(Color.red);
	vsm.setZoomLimit(-90);
	vsm.addVirtualSpace(rdfVS);
	vsm.addCamera(rdfVS);
	Vector cams=new Vector();
	cams.add(vsm.getVirtualSpace(rdfVS).getCamera(0));
	this.setSize(viewWidth-10,viewHeight-10);
	getContentPane().setSize(viewWidth,viewHeight);
	JPanel xvtmV=vsm.addPanelView(cams,vtmView,viewWidth-10,viewHeight-60);
 	xvtmV.setPreferredSize(new Dimension(viewWidth-100-10,viewHeight-60));
	statusBar=new JLabel("Please Wait (this can take several minutes)...");
	JPanel borderPanel=new JPanel();
	borderPanel.setLayout(new BorderLayout());
	borderPanel.add(xvtmV,BorderLayout.CENTER);
	borderPanel.add(statusBar,BorderLayout.SOUTH);
	borderPanel.setBackground(Color.white);
	borderPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black,2)," IsaViz (ZVTM) "));
	evt=new IsvAppletEvtHdlr(this);
	vsm.getView(vtmView).setEventHandler(evt);
	vsm.getView(vtmView).setBackgroundColor(Color.white);
	JPanel mainPanel=new JPanel();
	JPanel cmdPanel=new JPanel();
	cmdPanel.setBackground(Color.white);
	mainPanel.setBackground(Color.white);
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.NORTH;
	cmdPanel.setLayout(gridBag);
	JPanel navPanel=new JPanel();
	navPanel.setLayout(new GridLayout(3,3));
	navPanel.setBackground(Color.white);
	mvNWBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_nw.gif")));
	mvNWBt.setBorder(BorderFactory.createEmptyBorder());
	mvNWBt.setContentAreaFilled(false);
	mvNWBt.setBorderPainted(false);
	mvNWBt.setFocusPainted(false);
	mvNWBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_nw_h.gif")));
 	mvNWBt.addActionListener(this);
	navPanel.add(mvNWBt);
	mvNBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_n.gif")));
	mvNBt.setBorder(BorderFactory.createEmptyBorder());
	mvNBt.setContentAreaFilled(false);
	mvNBt.setBorderPainted(false);
	mvNBt.setFocusPainted(false);
	mvNBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_n_h.gif")));
 	mvNBt.addActionListener(this);
	navPanel.add(mvNBt);
	mvNEBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_ne.gif")));
	mvNEBt.setBorder(BorderFactory.createEmptyBorder());
	mvNEBt.setContentAreaFilled(false);
	mvNEBt.setBorderPainted(false);
	mvNEBt.setFocusPainted(false);
	mvNEBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_ne_h.gif")));
 	mvNEBt.addActionListener(this);
	navPanel.add(mvNEBt);
	mvWBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_w.gif")));
	mvWBt.setBorder(BorderFactory.createEmptyBorder());
	mvWBt.setContentAreaFilled(false);
	mvWBt.setBorderPainted(false);
	mvWBt.setFocusPainted(false);
	mvWBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_w_h.gif")));
 	mvWBt.addActionListener(this);
	navPanel.add(mvWBt);
	mvHBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_home.gif")));
	mvHBt.setBorder(BorderFactory.createEmptyBorder());
	mvHBt.setContentAreaFilled(false);
	mvHBt.setBorderPainted(false);
	mvHBt.setFocusPainted(false);
	mvHBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_home_h.gif")));
 	mvHBt.addActionListener(this);
	navPanel.add(mvHBt);
	mvEBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_e.gif")));
	mvEBt.setBorder(BorderFactory.createEmptyBorder());
	mvEBt.setContentAreaFilled(false);
	mvEBt.setBorderPainted(false);
	mvEBt.setFocusPainted(false);
	mvEBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_e_h.gif")));
 	mvEBt.addActionListener(this);
	navPanel.add(mvEBt);
	mvSWBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_sw.gif")));
	mvSWBt.setBorder(BorderFactory.createEmptyBorder());
	mvSWBt.setContentAreaFilled(false);
	mvSWBt.setBorderPainted(false);
	mvSWBt.setFocusPainted(false);
	mvSWBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_sw_h.gif")));
 	mvSWBt.addActionListener(this);
	navPanel.add(mvSWBt);
	mvSBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_s.gif")));
	mvSBt.setBorder(BorderFactory.createEmptyBorder());
	mvSBt.setContentAreaFilled(false);
	mvSBt.setBorderPainted(false);
	mvSBt.setFocusPainted(false);
	mvSBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_s_h.gif")));
 	mvSBt.addActionListener(this);
	navPanel.add(mvSBt);
	mvSEBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/m_se.gif")));
	mvSEBt.setBorder(BorderFactory.createEmptyBorder());
	mvSEBt.setContentAreaFilled(false);
	mvSEBt.setBorderPainted(false);
	mvSEBt.setFocusPainted(false);
	mvSEBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/m_se_h.gif")));
 	mvSEBt.addActionListener(this);
	navPanel.add(mvSEBt);
	buildConstraints(constraints,0,0,1,1,100,30);
	gridBag.setConstraints(navPanel,constraints);
	cmdPanel.add(navPanel);

	constraints.anchor=GridBagConstraints.CENTER;
	zmIBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/zm_i.gif")));
	zmIBt.setBorder(BorderFactory.createEmptyBorder());
	zmIBt.setContentAreaFilled(false);
	zmIBt.setBorderPainted(false);
	zmIBt.setFocusPainted(false);
	zmIBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/zm_i_h.gif")));
 	zmIBt.addActionListener(this);
	buildConstraints(constraints,0,1,1,1,100,10);
	gridBag.setConstraints(zmIBt,constraints);
	cmdPanel.add(zmIBt);
	zmOBt=new JButton(new ImageIcon(this.getClass().getResource("/appimages/zm_o.gif")));
	zmOBt.setBorder(BorderFactory.createEmptyBorder());
	zmOBt.setContentAreaFilled(false);
	zmOBt.setBorderPainted(false);
	zmOBt.setFocusPainted(false);
	zmOBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/appimages/zm_o_h.gif")));
 	zmOBt.addActionListener(this);
	buildConstraints(constraints,0,2,1,1,100,10);
	gridBag.setConstraints(zmOBt,constraints);
	cmdPanel.add(zmOBt);

	constraints.anchor=GridBagConstraints.SOUTH;
	helpBt=new JButton("Help...");
	aboutBt=new JButton("About...");
	helpBt.setBackground(Color.white);
	helpBt.setForeground(Color.black);
	aboutBt.setBackground(Color.white);
	aboutBt.setForeground(Color.black);
	buildConstraints(constraints,0,3,1,1,100,10);
	gridBag.setConstraints(helpBt,constraints);
	cmdPanel.add(helpBt);
	helpBt.addActionListener(this);
	buildConstraints(constraints,0,4,1,1,100,10);
	gridBag.setConstraints(aboutBt,constraints);
	cmdPanel.add(aboutBt);
	aboutBt.addActionListener(this);
	mainPanel.setLayout(new FlowLayout());
	mainPanel.add(borderPanel);
	mainPanel.add(cmdPanel);
	getContentPane().add(mainPanel);
	setVisible(true);
	validate();
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
// 		    this.sleep(5000);
		    loadSVG();
		    return null; 
		}
	    };
	worker.start();
    }

    void loadSVG(){
	try {
	    String uri=getParameter("svgFile");
	    Document svgDoc=parse(uri,false);
	    if (svgDoc!=null){
		this.load(svgDoc,vsm,rdfVS);
		getGlobalView();
		statusBar.setText(" ");
	    }
	    else {
		statusBar.setText("An error occured while loading file "+uri);
	    }
	}
	catch (Exception ex){ex.printStackTrace();}
    }

    void getGlobalView(){
	Location l=vsm.getGlobalView(vsm.getView(vtmView).getCameraNumber(0),ANIM_MOVE_LENGTH);
	rememberLocation(vsm.getView(vtmView).getCameraNumber(0).getLocation());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
	Camera c=vsm.getView(vtmView).getCameraNumber(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
	Camera c=vsm.getView(vtmView).getCameraNumber(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*direction should be one of Editor.MOVE_* */
    void translateView(short direction){
	Camera c=vsm.getView(vtmView).getCameraNumber(0);
	rememberLocation(c.getLocation());
	LongPoint trans;
	long[] rb=vsm.getView(vtmView).getVisibleRegion(c);
	if (direction==MOVE_UP){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_DOWN){
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_RIGHT){
	    long qt=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,0);
	}
	else if (direction==MOVE_LEFT){
	    long qt=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,0);
	}
	else if (direction==MOVE_UP_LEFT){
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    long qt2=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else if (direction==MOVE_UP_RIGHT){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    long qt2=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else if (direction==MOVE_DOWN_RIGHT){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    long qt2=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else {//direction==DOWN_LEFT
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    long qt2=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_TRANS_SIG,trans,c.getID());
    }

    void rememberLocation(Location l){
	if (previousLocations.size()>=MAX_PREV_LOC){// as a result of release/click being undifferentiated)
	    previousLocations.removeElementAt(0);
	}
	if (previousLocations.size()>0){
	    if (!Location.equals((Location)previousLocations.lastElement(),l)){
		previousLocations.add(l);
	    }
	}
	else {previousLocations.add(l);}
    }

    void moveBack(){
	if (previousLocations.size()>0){
	    Location newlc=(Location)previousLocations.lastElement();
	    Location currentlc=vsm.getView(vtmView).getCameraNumber(0).getLocation();
	    Vector animParams=Location.getDifference(currentlc,newlc);
	    vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_TRANS_SIG,animParams,vsm.getView(vtmView).getCameraNumber(0).getID());
	    previousLocations.removeElementAt(previousLocations.size()-1);
	}
    }

    /**
     *Load a DOM-parsed SVG document d in VirtualSpace vs
     *@param d SVG document as a DOM tree
     *@param vsm VTM virtual space manager owning the virtual space
     *@param vs name of the virtual space
     */
    void load(Document d,VirtualSpaceManager vsm,String vs){
	Element svgRoot=d.getDocumentElement();
	NodeList objects=svgRoot.getChildNodes();
	for (int i=0;i<objects.getLength();i++){
	    Node obj=objects.item(i);
	    if (obj.getNodeType()==Node.ELEMENT_NODE){processNode((Element)obj,vsm,vs);}
	}
    }

    /*e id a DOM element, vs is the name of the virtual space where the new glyph(s) are put*/
    void processNode(Element e,VirtualSpaceManager vsm,String vs){
	String tagName=e.getTagName();
	Glyph g;
	if (tagName.equals(SVGReader._ellipse)){
	    g=SVGReader.createEllipse(e);
	    vsm.addGlyph(g,vs);
	    ((ClosedShape)g).setFilled(true);
	    g.setColor(resourceColorF);
	    g.setBorderColor(resourceColorTB);
	}
	else if (tagName.equals(SVGReader._path)){
	    g=SVGReader.createPath(e,new VPath());
	    vsm.addGlyph(g,vs);
	    g.setColor(propertyColorB);
	}
	else if (tagName.equals(SVGReader._text)){
	    g=SVGReader.createText(e,vsm);
	    ((VText)g).setSpecialFont(swingFont);
	    vsm.addGlyph(g,vs);
	    //g.setColor(Color.black);
	}
	else if (tagName.equals(SVGReader._polygon)){
	    g=SVGReader.createRectangleFromPolygon(e);
	    if (g!=null){
		vsm.addGlyph(g,vs);
		((ClosedShape)g).setFilled(true);
		g.setColor(literalColorF);
		g.setBorderColor(literalColorTB);
	    }//if e does not describe a rectangle
	    else {//create a VPolygon
		g=SVGReader.createPolygon(e);
		g.setSensitivity(false);
		vsm.addGlyph(g,vs);
		((ClosedShape)g).setFilled(true);
		g.setColor(propertyColorB);
		g.setBorderColor(propertyColorB);
	    }  
	}
	else if (tagName.equals(SVGReader._g)){
	    NodeList objects=e.getChildNodes();
	    for (int i=0;i<objects.getLength();i++){
		Node obj=objects.item(i);
		if (obj.getNodeType()==Node.ELEMENT_NODE){processNode((Element)obj,vsm,vs);}
	    }
	}
	else if (tagName.equals(SVGReader._a)){
	    NodeList objects=e.getChildNodes();
	    for (int i=0;i<objects.getLength();i++){
		Node obj=objects.item(i);
		if (obj.getNodeType()==Node.ELEMENT_NODE){processNode((Element)obj,vsm,vs);}
	    }
	}
	else if (tagName.equals(SVGReader._title)){
	    //do nothing for now - we might want to send it back for some unknown reason
	    //but title elements are not supposed to be part of the representation
	}
	else if (tagName.equals(SVGReader._rect)){
	    g=SVGReader.createRectangle(e);
	    vsm.addGlyph(g,vs);
	    ((ClosedShape)g).setFilled(true);
	    g.setColor(literalColorF);
	    g.setBorderColor(literalColorTB);
	}
	else System.err.println("SVGReader: unsupported element: "+tagName);
    }

    public void actionPerformed(ActionEvent e){
	Object o=e.getSource();
	if (o==zmIBt){getLowerView();}
	else if (o==zmOBt){getHigherView();}
	else if (o==mvHBt){getGlobalView();}
	else if (o==mvNBt){translateView(MOVE_UP);}
	else if (o==mvSBt){translateView(MOVE_DOWN);}
	else if (o==mvEBt){translateView(MOVE_RIGHT);}
	else if (o==mvWBt){translateView(MOVE_LEFT);}
	else if (o==mvNWBt){translateView(MOVE_UP_LEFT);}
	else if (o==mvNEBt){translateView(MOVE_UP_RIGHT);}
	else if (o==mvSWBt){translateView(MOVE_DOWN_LEFT);}
	else if (o==mvSEBt){translateView(MOVE_DOWN_RIGHT);}
	else if (o==helpBt){
	    new TextViewer(new StringBuffer(this.commands),"Help",0);
	}
	else if (o==aboutBt){
	    about();
	}
    }

    public void keyPressed(KeyEvent e){
	int code=e.getKeyCode();
	if (code==KeyEvent.VK_PAGE_UP){getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){getLowerView();}
	else if (code==KeyEvent.VK_HOME){getGlobalView();}
	else if (code==KeyEvent.VK_B){moveBack();}
	else if (code==KeyEvent.VK_UP){translateView(MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){translateView(MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){translateView(MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){translateView(MOVE_RIGHT);}
    }
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    void about(){
	javax.swing.JOptionPane.showMessageDialog(this,aboutMsg);
    }

    void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }

    public static Document parse(String uri,boolean validation){ 
	try {
	    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	    factory.setValidating(validation);
	    if (!validation){factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",new Boolean(false));}
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder=factory.newDocumentBuilder();

	    InputStream is=new URL(uri).openStream();

	    if (is!=null){
		Document res=builder.parse(is);
		return res;
	    }
	    else {return null;}
// 	    } catch (IOException e) {
// 		System.err.println("error while creating input stream");
// 	    }
	    

	}
	catch (FactoryConfigurationError e){e.printStackTrace();return null;} 
	catch (ParserConfigurationException e){e.printStackTrace();return null;}
	catch (SAXException e){e.printStackTrace();return null;}
	catch (IOException e){e.printStackTrace();return null;}
    }

    static final String aboutMsg="IsaViz/ZVTM Browser for RDF Validator v 0.2\nhttp://www.w3.org/2001/11/IsaViz/\n\n\nBased on the ZVTM (http://zvtm.sourceforge.net)\n\nWritten by Emmanuel Pietriga (emmanuel@w3.org)";

    static final String commands="Misc. Commands\n"
	+"--------------------------------------------------\n"
	+"Navigation\n"
	+"* Press the left or right mouse button and drag to move in the graph\n"
	+"* Hold Shift, press the left or right mouse button and drag vertically to zoom/unzoom\n"
	+"* Click the left or right mouse button on a node to center the view on it\n"
	+"* Home (or Ctrl+G) = get a global view of the graph\n"
	+"* Page Down = Zoom In\n"
	+"* Page Up = Zoom Out\n"
	+"* Arrows = move in the graph\n"
	+"* Ctrl+B = Back to previous location\n"
	;

}

