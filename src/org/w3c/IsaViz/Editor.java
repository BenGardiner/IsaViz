/*   FILE: Editor.java
 *   DATE OF CREATION:   10/18/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Nov 23 09:58:56 2005 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: Editor.java,v 1.64 2006/10/29 11:09:03 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import java.awt.Font;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.*;
import javax.swing.JOptionPane;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

// import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;

//www.xrce.xerox.com/
import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.VRectangleST;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VEllipse;
import com.xerox.VTM.glyphs.VTriangleOr;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VQdCurve;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.svg.SVGWriter;
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.engine.AnimationListener;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.glyphs.GlyphUtils;

import org.w3c.IsaViz.fresnel.FSLHierarchyStore;
import org.w3c.IsaViz.fresnel.FSLJenaHierarchyStore;
import org.w3c.IsaViz.fresnel.FresnelManager;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.NsIterator;

/**This is the main IsaViz class - usd to launch the application. You can pass a file as argument. If its extension is .isv, isaViz will attempt to load it as an IsaViz project. Otherwise, it will try to import it through Jena+GraphViz/Dot. <br> It contains the main definitions, references to all managers and GUI components + the internal model and methods to modify it.*/

public class Editor implements AnimationListener {

    /*namespaces and default prefixes*/
    public static final String isavizURI="http://www.w3.org/2001/10/IsaViz";     /*isaviz namespace*/
    public static String RDFMS_NAMESPACE_PREFIX="rdf";                     /*RDF model and syntax namespace*/
    public static final String RDFMS_NAMESPACE_URI="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static String RDFS_NAMESPACE_PREFIX="rdfs"; /*RDF Schema namespace*/
    public static final String RDFS_NAMESPACE_URI="http://www.w3.org/2000/01/rdf-schema#";
    public static String XSD_NAMESPACE_PREFIX="xsd"; /*XML Schema datatypes*/
    public static final String XSD_NAMESPACE_URI="http://www.w3.org/2001/XMLSchema#";

    /*The string to use as the model's base URI when none is available - e.g. for the RDF that is directly entered into the input form, or coming from a plug-in*/
    public static String DEFAULT_BASE_URI="";
    
    /*the actual base URI of the document*/
    public static String BASE_URI=DEFAULT_BASE_URI;

    /*The string to use for to prefix anonymous nodes*/
    public static String ANON_NODE="genid:";  

    /*Misc. constants*/
    /*value displayed in the property type table for the auto-numbering membership property constructor (should begin with a string identifying it uniquely based on its first 3 chars like '_??' since a test depends on this in createNewProperty())*/
    public static String MEMBERSHIP_PROP_CONSTRUCTOR="_??   (Membership property auto-numbering: _1, _2, ...)";
    /*default language used in literals*/
    public static String DEFAULT_LANGUAGE_IN_LITERALS="en"; 
    /*if true, xml:lang is added for each literal, even when lang is default*/
    public static boolean ALWAYS_INCLUDE_LANG_IN_LITERALS=false;
    /*tells whether RDFWriter should output standard or abbreviated syntax*/
    public static boolean ABBREV_SYNTAX=true;
    /*tells whether we should display the URI (false) or the label (true) of a resource in the ellipse*/
    public static boolean DISP_AS_LABEL=true;
    /*max number of chars displayed in the graph for literals*/ 
    public static int MAX_LIT_CHAR_COUNT=40;
    /*orientation of the graph (when computed by GraphViz) - can be "LR" or "TB"*/
    public static String GRAPH_ORIENTATION="LR";
    /*GraphViz version 1.7.6 is no longer supported in IsaViz 2.0 (we are at version 1.9.0 available for all platforms, so don't bother)*/
//     /*which version of graphviz (changes the way we parse the SVG file) 0=GraphViz 1.7.6 ; 1=GraphViz 1.7.11 or later*/
//     static int GRAPHVIZ_VERSION=1;

    /*directories and files*/
    JFileChooser fc;
    /*location of the configuration file - at init time, we look for it in the user's home dir.
     If it is not there, we take the one in IsaViz dir.*/
    static File cfgFile;
    /*rdf/isv file passed as argument from the command line (if any)*/
    static String argFile;
    /*gss file passed as argument from the command line (if any)*/
    static String gssFile;
    /*file for the current project - set by openProject(), used by saveProject()*/
    static File projectFile=null;
    /*last RDF file/URL imported*/
    static String lastRDF=null;
    /*temp xml-serialization of the current model used to display model as RDF/XML*/
    static String tmpRdfFile="tmp/serial.rdf";
    /*path to GraphViz/DOT executable*/
    static File m_GraphVizPath=new File("C:\\ATT\\Graphviz\\bin\\dot.exe");
    /*path to Graphviz font dir (did not seem to matter, at least under Win32 and Linux)*/
    static File m_GraphVizFontDir=new File("C:\\ATT\\Graphviz");
    /*temporary directory (temp .rdf, .dot and .svg files)*/
    public static File m_TmpDir=new File("tmp");
    /*IsaViz project files (.isv)*/
    public static File projectDir=new File("projects");
    public static File lastOpenPrjDir=null; //remember these 2 so that next file
    public static File lastSavePrjDir=null;  //dialog gets open in the same place
    /*Import/Export directory*/
    public static File rdfDir=new File("export");
    public static File lastImportRDFDir=null; //remember these 2 so that next file
    public static File lastExportRDFDir=null; //dialog gets open in the same place

    /*delete temporary files on exit*/
    static boolean dltOnExit=true;
    /*maximum number of resources remembered (for back button) when navigating in the property browser tab (TablePanel)*/
    static int MAX_BRW_LIST_SIZE=10;
    /*maximum number of operations remembered (for Undo)*/
    static int UNDO_SIZE=5;
    /*should the window positions and sizes be saved and restored next time IsaViz is started*/
    static boolean SAVE_WINDOW_LAYOUT=false;

    /*VTM data*/
    public static VirtualSpaceManager vsm;
    public static final String mainVirtualSpace="rdfSpace"; /*name of the main VTM virtual space*/
    public static VirtualSpace mSpace;                /*the main (rdf graph) space itself*/
    static final String rdRegionVirtualSpace="radarSpace"; /*name of the VTM virtual space holding the rectangle delimiting the region seen by main view in radar view*/
    static VirtualSpace rSpace;                /*the radar (region rect) space itself*/
    public static final String mainView="Graph";            /*name of the main VTM view*/
    public static View mView;                               /*main view itself*/
    static final String radarView="Overview";        /*name of radar VTM view*/
    static View rView;                               /*radar view itself*/

    static VirtualSpace fpSpace;
    static final String floatingPaletteSpace = "fpSpace";

    static final String resShapeType="resG";       //VTM glyph types associated with 
    static final String resTextType="resT";          //the entities of the graph (resources, 
    static final String propPathType="prdG";         //properties, literals). Actions fired 
    static final String propHeadType="prdH";         //in the VTM event handler (EditorEvtHdlr) 
    static final String propTextType="prdT";         //depend on these (or part of these, like {G,T,H}).
    static final String propCellType="prdC";         //depend on these (or part of these, like {G,T,H}).
    static final String litShapeType="litG";         //Modify at your own risks
    static final String litTextType="litT";

    /*L&F data*/
    static Font smallFont=new Font("Dialog",0,10);
    static Font tinyFont=new Font("Dialog",0,9);

    /*Font used in VTM view - info also used when generating the DOT file for GraphViz*/
    static String vtmFontName="Dialog";
    static int vtmFontSize=10;
    public static Font vtmFont=new Font(vtmFontName,0,vtmFontSize);
    /*Font used in Swing components that need to be able to receive i18n content*/
    static String swingFontName="Dialog";
    static int swingFontSize=10;
    static Font swingFont=new Font(swingFontName,0,swingFontSize);
    static int tinySwingFontSize=9;
    static Font tinySwingFont=new Font(swingFontName,0,tinySwingFontSize);

    static boolean ANTIALIASING=false;  //sets antialiasing in VTM views

    /*class that receives the events sent from VTM main view (include mouse click, entering object,...)*/
    EditorEvtHdlr eeh;
    /*class that receives the events sent from VTM radar view (include mouse click, entering object,...)*/
    RadarEvtHdlr reh;
    /*in charge of loading, parsing  and serializing RDF files (using Jena/ARP)*/
    RDFLoader rdfLdr;
    /*in charge of loading and parsing misc. XML files (for instance SVG and ISV project files)*/
    XMLManager xmlMngr;
    /*in charge of building and analysing ISV DOM Trees*/
    ISVManager isvMngr;
    /*configuration (user prefs) manager*/
    public ConfigManager cfgMngr;
    /*graph stylesheet manager*/
    public static GSSManager gssMngr;
    /*Fresnel manager*/
    public static FresnelManager fresnelMngr;
    /*methods to adjust path start/end points, text inside ellipses, etc...*/
    public GeometryManager geomMngr;
    /*rdfs/owl manager*/
    public SchemaManager schemaMngr;

    /*methods to manage contextual menus associated with nodes and edges*/
    //ContMenuManager ctmnMngr;
    /*represents the region seen by main view in the radar view*/
    static VRectangle observedRegion;

    /*error log window*/
    TextViewer errorLog;

    /*store last UNDO_SIZE commands so that they can be undone (not all operations are supported)*/
    ISVCommand[] undoStack;
    /*index of the last command in the undo stack*/
    int undoIndex;

    /*remember previous camera locations so that we can get back*/
    static final int MAX_PREV_LOC=10;
    static Vector previousLocations;

    /*Swing panels*/
    public static MainCmdPanel cmp;   //main swing command panel (menus,...)
    public static TablePanel tblp;    //swing panel with tables for namespaces, properties, resource types...
    public static PropsPanel propsp;  //swing panel showing the attributes of the last selected node/edge (can be edited through this panel)
    public static NavPanel navp;      //swing panel showing directional arrows and zoom buttons for navigation in main zvtm view
    public static BookmarkPanel bkp;  //swing panel showing geographical bookmarks

    /*External (platform-dependant) browser*/
    //a class to access a platform-specific web browser (not initialized at startup, but only on demand)
    static WebBrowser webBrowser;
    //try to automatically detect browser (do not take browser path into account)
    static boolean autoDetectBrowser=true;
    //path to the browser's exec file
    static File browserPath=new File("");
    //browser command line options
    static String browserOptions="";

    /*proxy/firewall configuration*/
    static boolean useProxy=false;
    static String proxyHost="";    //proxy hostname
    static String proxyPort="80";    //default value for the JVM proxyPort system property

    /*In memory data structures used to store the model*/

    /*a dictionary containing all resources in the model*/
    public Hashtable resourcesByURI; //key is a String whose value is a resource's URI or ID (obtained by IResource.getIdent()) ; value is the corresponding IResource
    /*a dictionary containing all property instances (predicates) in the model*/
    public Hashtable propertiesByURI; //key is a string representing a property URI ; value is a vector containing all IProperty whose URI is equal to key
    /*a list of all literals in the model (literals with the same value are different ILiteral objects)*/
    public Vector literals; //vector of ILiteral

    /*next unique anonymous ID to be assigned to an anonymous resource - DOES NOT CONTAIN THE ANON_NODE PREFIX*/
    StringBuffer nextAnonID=new StringBuffer("0");

    /*selected graph entities*/
    Vector selectedResources=new Vector();
    Vector selectedLiterals=new Vector();
    Vector selectedPredicates=new Vector();
    static INode lastSelectedItem;   //last node/edge selected by user

    /*copied graph entities (isaviz clipboard, for cut/copy/paste)*/
    Vector copiedResources=new Vector();
    Vector copiedLiterals=new Vector();
    Vector copiedPredicates=new Vector();

    /*Jena rdf model (instantiated when importing/exporting RDF/XML or NTriples)*/
    Model rdfModel;

    /*selected row in the table of Properties (one such row needs to be selected when creating a new property instance in the graph)*/
    String selectedPropertyConstructorNS;
    String selectedPropertyConstructorLN;
    
    /*quick search variables*/
    int searchIndex=0;
    String lastSearchedString="";
    Vector matchingList=new Vector();
    INode lastMatchingEntity=null;  //remember it so that its color can be reset after the search ends

    /**RDFS/OWL class and property hierarchy store*/
    public FSLHierarchyStore fhs = new FSLJenaHierarchyStore();

    /*error management*/
    public StringBuffer errorMessages;
    public boolean reportError;

    /*translation constants*/
    static short MOVE_UP=0;
    static short MOVE_DOWN=1;
    static short MOVE_LEFT=2;
    static short MOVE_RIGHT=3;
    static short MOVE_UP_LEFT=4;
    static short MOVE_UP_RIGHT=5;
    static short MOVE_DOWN_LEFT=6;
    static short MOVE_DOWN_RIGHT=7;

    /*main constructor - called from main()*/
    public Editor(){
	SplashWindow sp=new SplashWindow(2000,"images/IsavizSplash.gif",false,null); //displays a splash screen
	File f=new File(System.getProperty("user.home")+"/isaviz.cfg");
	if (f.exists()){cfgFile=f;}
	else {cfgFile=new File("isaviz.cfg");}
	sp.setMessage("Loading Preferences from "+cfgFile.getAbsolutePath());
// 	System.out.println("Loading config file from "+cfgFile);
	vsm=new VirtualSpaceManager();//VTM main class
// 	System.out.println("DEVEL Version - switch DEBUG off in public release -----");
//  	vsm.setDebug(true);   //COMMENT OUT IN PUBLIC RELEASES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	vsm.setZoomLimit(-90);
	vsm.animator.setAnimationListener(this);
	cfgMngr=new ConfigManager(this);
	geomMngr=new GeometryManager(this);
	isvMngr=new ISVManager(this);
	gssMngr=new GSSManager(this);
	schemaMngr = new SchemaManager(this, false);
	sp.setProgressBarValue(10);
	cfgMngr.initLookAndFeel();    //fonts, Swing colors
	sp.setProgressBarValue(20);
	sp.setMessage("Looking for plug-ins");
	cfgMngr.initWindows();                //Swing panels and VTM views (default layout) - plus plug-in initialisation
	sp.setProgressBarValue(30);
	sp.setMessage("Initializing XML parser");
	xmlMngr=new XMLManager(this); //must happen before initConfig(), initHistory() and any project opening/file import
	sp.setProgressBarValue(40);
	sp.setMessage("Initializing Internal Data Structures");
	resourcesByURI=new Hashtable();
	sp.setProgressBarValue(50);
	propertiesByURI=new Hashtable();
	sp.setProgressBarValue(60);
	literals=new Vector();
	errorMessages=new StringBuffer();
	reportError=false;
	undoStack=new ISVCommand[UNDO_SIZE];
	undoIndex=-1;
	previousLocations=new Vector();
	sp.setProgressBarValue(70);
	sp.setMessage("Initializing Look & Feel");
	cfgMngr.assignColorsToGraph();
	sp.setProgressBarValue(80);
	cfgMngr.initConfig();
	vsm.setMainFont(vtmFont);
	sp.setProgressBarValue(90);
	cfgMngr.layoutWindows();
	sp.setProgressBarValue(100);
	cfgFile=new File(System.getProperty("user.home")+"/isaviz.cfg"); //the user's prefs will be saved in his home dir, no matter whether there was a cfg file there or not
	if (m_TmpDir.exists()){
	    if (gssFile!=null){
		File f2=new File(gssFile);
		if (f2.exists()){
		    if (argFile.endsWith(".nt")){gssMngr.loadStylesheet(f2,RDFLoader.NTRIPLE_READER);}  //1 is NTriples reader
		    else if (argFile.endsWith(".n3")){gssMngr.loadStylesheet(f2,RDFLoader.N3_READER);}  //2 is Notation3 reader
		    else {gssMngr.loadStylesheet(f2,RDFLoader.RDF_XML_READER);}  //0 is for RDF/XML reader (default)
		}
	    }
	    if (argFile!=null){//load/import file passed as command line argument
		if (argFile.endsWith(".isv")){isvMngr.openProject(new File(argFile));}
		else if (argFile.endsWith(".nt")){loadRDF(new File(argFile),RDFLoader.NTRIPLE_READER,true);}  //1 is NTriples reader
		else if (argFile.endsWith(".n3")){loadRDF(new File(argFile),RDFLoader.N3_READER,true);}  //2 is Notation3 reader
		else {loadRDF(new File(argFile),RDFLoader.RDF_XML_READER,true);}  //0 is for RDF/XML reader (default)
	    }
	    else {
		/*vsm.getGlobalView(vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0),100);*/
		vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0).setAltitude(0);
	    }
	}
	else {
	    JOptionPane.showMessageDialog(cmp,"You need to select a temporary directory for IsaViz\nin the Directories tab of the Preferences Panel, or some functions will not work properly.\nThe current directory ("+m_TmpDir+") does not exist.");
	}
    }

    /*GUI warning before reset*/
    void promptReset(){
	Object[] options={"Yes","No"};
	int option=JOptionPane.showOptionDialog(null,Messages.resetWarning,"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
	if (option==JOptionPane.OK_OPTION){
	    Editor.vsm.getView(Editor.mainView).setStatusBarText("New project");
	    this.reset(true);
	    this.resetGraphStylesheets();
	}
    }

    /*reset project*/
    public void reset(boolean resetNSBindings){
	if (rdfLdr!=null){rdfLdr.reset();}
	projectFile=null;
	propsp.reset();
	matchingList=new Vector();
	resourcesByURI.clear();
	propertiesByURI.clear();
	literals.removeAllElements();
	if (resetNSBindings){resetNamespaceBindings();}
	resetPropertyConstructors();
	resetPropertyBrowser();
	reportError=false;
	rdfModel=null;
	nextAnonID=new StringBuffer("0");
	resetSelected();
	resetCopied();
	lastSelectedItem=null;
	Utils.resetArray(undoStack);  //undo stack 
	undoIndex=-1;
	cmp.enableUndo(false);
	previousLocations.removeAllElements();
	geomMngr.resetLastResizer();
	vsm.destroyGlyphsInSpace(mainVirtualSpace);  //erase source document representation
	SVGReader.setPositionOffset(0,0);  /*the offset might have been changed by RDFLoader - and it might affect
					     project loaded from ISV (which uses SVGReader.createPath())*/
	vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0).setAltitude(0);
	vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0).moveTo(0,0);
    }

    /*called by reset()*/
    void resetSelected(){
	selectedResources.removeAllElements();   //selected nodes/edges
	selectedLiterals.removeAllElements();
	selectedPredicates.removeAllElements();	
    }

    /*called by reset() and each time we do a new copy*/
    void resetCopied(){
	copiedResources.removeAllElements();   //reset clipboard
	copiedLiterals.removeAllElements();
	copiedPredicates.removeAllElements();
	cmp.enablePaste(false);
    }

    /*called by reset()*/
    void resetNamespaceBindings(){
	tblp.resetNamespaceTable();
	addNamespaceBinding(RDFMS_NAMESPACE_PREFIX,RDFMS_NAMESPACE_URI,new Boolean(true),true,false);
	addNamespaceBinding(RDFS_NAMESPACE_PREFIX,RDFS_NAMESPACE_URI,new Boolean(true),true,false);
	addNamespaceBinding(XSD_NAMESPACE_PREFIX,XSD_NAMESPACE_URI,new Boolean(true),true,false);
	addNamespaceBinding(GraphStylesheet.GSS_NAMESPACE_PREFIX,GraphStylesheet._gssNS,new Boolean(true),true,false);
	schemaMngr.updateNamespaces();
    }

    /*called by reset()*/
    void resetPropertyConstructors(){
	tblp.resetPropertyTable();
	initRDFMSProperties();
	initRDFSProperties();
    }

    /*reset the tab in which is displayed the property browser*/
    void resetPropertyBrowser(){
	tblp.resetBrowser();
    }

    /*reset the tab in which stylesheets are displayed and the GSSManager data structures
      not called from general reset() as many methods calling reset() do not want to loose graph stylesheet information
    */
    void resetGraphStylesheets(){
	tblp.resetStylesheets();
	gssMngr.reset();
    }

    void openProject(){
	fc = new JFileChooser(Editor.lastOpenPrjDir!=null ? Editor.lastOpenPrjDir : Editor.projectDir);
	fc.setDialogTitle("Open ISV Project");
	int returnVal= fc.showOpenDialog(cmp);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    errorMessages.append("-----Loading ISV project-----\n");
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			isvMngr.openProject(fc.getSelectedFile());
			return null; 
		    }
		};
	    worker.start();
	}
	cmp.repaint();  //some time the icon palette is not painted properly after the jfilechooser disappears
    }

    void saveProject(){
	if (Editor.projectFile!=null){
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			isvMngr.saveProject(Editor.projectFile);
			return null; 
		    }
		};
	    worker.start();
	}
	else {
	    saveProjectAs();
	}
    }

    void saveProjectAs(){
	fc = new JFileChooser(Editor.lastSavePrjDir!=null ? Editor.lastSavePrjDir : Editor.projectDir);
	fc.setDialogTitle("Save ISV Project As");
	int returnVal= fc.showSaveDialog(cmp);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			isvMngr.saveProject(fc.getSelectedFile());
			return null;
		    }
		};
	    worker.start();
	}
	cmp.repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
    }

    //assign a unique anonymous ID to a node (checks for potential conflicts with existing IDs)
    //returns an anon id WITH prefix ANON_NODE
    public String nextAnonymousID(){
	incAnonID();
	while (resourcesByURI.containsKey(ANON_NODE+nextAnonID)){//to prevent possible conflicts
	    incAnonID();//with anon IDs generated by Jena or loaded from an ISV file
	}
	return ANON_NODE+nextAnonID;
    }

    //called by nextAnonymousID - do not use it anywhere else
    private void incAnonID(){
	boolean done=false;
	for (int i=0;i<nextAnonID.length();i++){
	    byte b=(byte)nextAnonID.charAt(i);
	    if (b<0x7a){
		nextAnonID.setCharAt(i,(char)Utils.incByte(b));
		done=true;
		for (int j=0;j<i;j++){nextAnonID.setCharAt(j,'0');}
		break;
	    }
	}
	if (!done){
	    for (int i=0;i<nextAnonID.length();i++){nextAnonID.setCharAt(i,'0');}
	    nextAnonID.append('0');
	}
    }

    /*import local RDF file*/
    public void loadRDF(final File f,final int whichReader,boolean updateLastDir){
	if (m_GraphVizPath.exists()){
	    reset(false);
	    errorMessages.append("-----Importing RDF-----\n");
	    if (updateLastDir){lastImportRDFDir=f.getParentFile();}
	    if (rdfLdr==null){rdfLdr=new RDFLoader(this);}  //not initialized at launch time since we might not need it
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
			if (gssMngr.getStylesheetList().size()>0){rdfLdr.loadAndStyle(f,whichReader);}//if a least one stylesheet is declared,
			else {rdfLdr.load(f,whichReader);}//apply it automatically, else load the RDF the old way (as in IsaViz 1.x)
			updatePrefixBindingsInGraph();
			Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
			Editor.lastRDF=f.getAbsolutePath();
			return null; 
		    }
		};
	    worker.start();
	}
	else {
	    JOptionPane.showMessageDialog(cmp,"The current location of GraphViz/dot ("+m_GraphVizPath+") is not valid.\nGo to the Directories tab in Preferences and browse\nto the location of your dot executable file (dot or dot.exe)");
	}
    }

    /*import remote RDF file*/
    public void loadRDF(final java.net.URL u,final int whichReader,boolean asGSS){//asGSS=as a graph stylesheet
	if (asGSS){
	    gssMngr.loadStylesheet(u,whichReader);
	}
	else {
	    if (m_GraphVizPath.exists()){
		reset(false);
		errorMessages.append("-----Importing RDF-----\n");
		if (rdfLdr==null){rdfLdr=new RDFLoader(this);}  //not initialized at launch time since we might not need it
		final SwingWorker worker=new SwingWorker(){
			public Object construct(){
			    Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
			    if (gssMngr.getStylesheetList().size()>0){rdfLdr.loadAndStyle(u,whichReader);}//if a least one stylesheet is declared,
			    else {rdfLdr.load(u,whichReader);}//apply it automatically, else load the RDF the old way (as in IsaViz 1.x)
			    updatePrefixBindingsInGraph();
			    Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
			    Editor.lastRDF=u.toString();
			    return null;
			}
		    };
		worker.start();
	    }
	    else {
		JOptionPane.showMessageDialog(cmp,"The current location of GraphViz/dot ("+m_GraphVizPath+") is not valid.\nGo to the Directories tab in Preferences and browse\nto the location of your dot executable file (dot or dot.exe)");
	    }
	}
    }

    /*import local RDF file*/
    public void loadRDF(final InputStream is,final int whichReader){
	if (m_GraphVizPath.exists()){
	    reset(false);
	    errorMessages.append("-----Importing RDF-----\n");
	    if (rdfLdr==null){rdfLdr=new RDFLoader(this);}  //not initialized at launch time since we might not need it
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
			if (gssMngr.getStylesheetList().size()>0){rdfLdr.loadAndStyle(is,whichReader);}//if a least one stylesheet is declared,
			else {rdfLdr.load(is,whichReader);}//apply it automatically, else load the RDF the old way (as in IsaViz 1.x)
			updatePrefixBindingsInGraph();
			Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
			return null; 
		    }
		};
	    worker.start();
	}
	else {
	    JOptionPane.showMessageDialog(cmp,"The current location of GraphViz/dot ("+m_GraphVizPath+") is not valid.\nGo to the Directories tab in Preferences and browse\nto the location of your dot executable file (dot or dot.exe)");
	}
    }

    /*merge local RDF file with current model*/
    public void mergeRDF(final File f,final int whichReader,boolean updateLastDir){
	if (m_GraphVizPath.exists()){
	    if (updateLastDir){lastImportRDFDir=f.getParentFile();}
	    errorMessages.append("-----Merging-----\n");
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
			//generate Jena model for current model
			generateJenaModel();
			//load second model only as a Jena model and merge it with first one
			try {
			    rdfModel.add(rdfLdr.merge(f,whichReader));
			}
			catch (RDFException ex){errorMessages.append("Editor.mergeRDF() "+ex+"\n");reportError=true;}
			//serialize this model in a temporary file (RDF/XML)
			File tmpF=Utils.createTempFile(Editor.m_TmpDir.toString(),"mrg",".rdf");
			boolean wasAbbrevSyntax=Editor.ABBREV_SYNTAX; //serialize using
			Editor.ABBREV_SYNTAX=true;                    //abbreviated syntax
			rdfLdr.save(rdfModel,tmpF);                   //but restore user prefs after
			if (!wasAbbrevSyntax){Editor.ABBREV_SYNTAX=false;}//we are done
			//import this file
			reset(false);
			//tmp file is generated as RDF/XML
			//if a least one stylesheet is declared,
			//apply it automatically, else load the RDF the old way (as in IsaViz 1.x)
			if (gssMngr.getStylesheetList().size()>0){rdfLdr.loadAndStyle(tmpF,RDFLoader.RDF_XML_READER);}
			else {rdfLdr.load(tmpF,RDFLoader.RDF_XML_READER);}
			if (Editor.dltOnExit && tmpF!=null){tmpF.deleteOnExit();}
			updatePrefixBindingsInGraph();
			Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
			Editor.lastRDF=f.getAbsolutePath();
			return null; 
		    }
		};
	    worker.start();
	}
	else {
	    JOptionPane.showMessageDialog(cmp,"The current location of GraphViz/dot ("+m_GraphVizPath+") is not valid.\nGo to the Directories tab in Preferences and browse\nto the location of your dot executable file (dot or dot.exe)");
	}
    }

    /*merge remote RDF file with current model*/
    public void mergeRDF(final java.net.URL u,final int whichReader){
	if (m_GraphVizPath.exists()){
	    errorMessages.append("-----Merging-----\n");
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
			//generate Jena model for current model
			generateJenaModel();
			//load second model only as a Jena model and merge it with first one 
			try {rdfModel.add(rdfLdr.merge(u,whichReader));}
			catch (RDFException ex){errorMessages.append("Editor.mergeRDF() "+ex+"\n");reportError=true;}
			//serialize this model in a temporary file (RDF/XML)
			File tmpF=Utils.createTempFile(Editor.m_TmpDir.toString(),"mrg",".rdf");
			boolean wasAbbrevSyntax=Editor.ABBREV_SYNTAX; //serialize using
			Editor.ABBREV_SYNTAX=true;                    //abbreviated syntax
			rdfLdr.save(rdfModel,tmpF);                   //but restore user prefs after
			if (!wasAbbrevSyntax){Editor.ABBREV_SYNTAX=false;}//we are done
			//import this file
			reset(false);
			//tmp file is generated as RDF/XML
			//if a least one stylesheet is declared,
			//apply it automatically, else load the RDF the old way (as in IsaViz 1.x)
			if (gssMngr.getStylesheetList().size()>0){rdfLdr.loadAndStyle(tmpF,RDFLoader.RDF_XML_READER);}
			else {rdfLdr.load(tmpF,RDFLoader.RDF_XML_READER);}
			updatePrefixBindingsInGraph();
			if (Editor.dltOnExit){tmpF.deleteOnExit();}
			Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
			Editor.lastRDF=u.toString();
			return null; 
		    }
		};
	    worker.start();
	}
	else {
	    JOptionPane.showMessageDialog(cmp,"The current location of GraphViz/dot ("+m_GraphVizPath+") is not valid.\nGo to the Directories tab in Preferences and browse\nto the location of your dot executable file (dot or dot.exe)");
	}
    }

    /*merge local RDF file with current model*/
    public void mergeRDF(final InputStream is,final int whichReader){
	if (m_GraphVizPath.exists()){
	    errorMessages.append("-----Merging-----\n");
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
			//generate Jena model for current model
			generateJenaModel();
			//load second model only as a Jena model and merge it with first one
			try {
			    rdfModel.add(rdfLdr.merge(is,whichReader));
			}
			catch (RDFException ex){errorMessages.append("Editor.mergeRDF() "+ex+"\n");reportError=true;}
			//serialize this model in a temporary file (RDF/XML)
			File tmpF=Utils.createTempFile(Editor.m_TmpDir.toString(),"mrg",".rdf");
			boolean wasAbbrevSyntax=Editor.ABBREV_SYNTAX; //serialize using
			Editor.ABBREV_SYNTAX=true;                    //abbreviated syntax
			rdfLdr.save(rdfModel,tmpF);                   //but restore user prefs after
			if (!wasAbbrevSyntax){Editor.ABBREV_SYNTAX=false;}//we are done
			//import this file
			reset(false);
			//tmp file is generated as RDF/XML
			//if a least one stylesheet is declared,
			//apply it automatically, else load the RDF the old way (as in IsaViz 1.x)
			if (gssMngr.getStylesheetList().size()>0){rdfLdr.loadAndStyle(tmpF,RDFLoader.RDF_XML_READER);}
			else {rdfLdr.load(tmpF,RDFLoader.RDF_XML_READER);}
			if (Editor.dltOnExit && tmpF!=null){tmpF.deleteOnExit();}
			updatePrefixBindingsInGraph();
			Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
			return null; 
		    }
		};
	    worker.start();
	}
	else {
	    JOptionPane.showMessageDialog(cmp,"The current location of GraphViz/dot ("+m_GraphVizPath+") is not valid.\nGo to the Directories tab in Preferences and browse\nto the location of your dot executable file (dot or dot.exe)");
	}
    }

    /*parse local RDF/XML file and add all property types to tblp.prTable (property constructors)*/
    public void loadPropertyTypes(final File f){
	if (rdfLdr==null){rdfLdr=new RDFLoader(this);}  //not initialized at launch time since we might not need it
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    rdfLdr.loadProperties(f);
		    return null; 
		}
	    };
	worker.start();
    }

    /*generate the Jena model equivalent to our internal model prior to export*/
    void generateJenaModel(){
	if (rdfLdr==null){rdfLdr=new RDFLoader(this);}
	rdfLdr.generateJenaModel();
    }

    /*export RDF/XML file locally*/
    public void exportRDF(File f,boolean updateLastDir){
	Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	if (updateLastDir){lastExportRDFDir=f.getParentFile();}
	if (rdfLdr==null){rdfLdr=new RDFLoader(this);}
	rdfLdr.generateJenaModel(); //this actually builds the Jena model from our internal representation
	rdfLdr.save(rdfModel,f);
	Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
    }

    /*export RDF/XML to an outputstream*/
    public void exportRDF(OutputStream os){
	Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	if (rdfLdr==null){rdfLdr=new RDFLoader(this);}
	rdfLdr.generateJenaModel(); //this actually builds the Jena model from our internal representation
	rdfLdr.save(rdfModel,os);
	Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
    }

    /*export as Notation3 locally*/
    public void exportN3(File f){
	Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	lastExportRDFDir=f.getParentFile();
	if (rdfLdr==null){rdfLdr=new RDFLoader(this);}
	this.generateJenaModel(); //this actually builds the Jena model from our internal representation
	rdfLdr.saveAsN3(rdfModel,f);
	Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
    }

    /*export as N-Triples locally*/
    public void exportNTriples(File f){
	Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	lastExportRDFDir=f.getParentFile();
	if (rdfLdr==null){rdfLdr=new RDFLoader(this);}
	this.generateJenaModel(); //this actually builds the Jena model from our internal representation
	rdfLdr.saveAsTriples(rdfModel,f);
	Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
    }

    /*export as PNG (bitmap image) locally (only the current view displayed by VTM, not the entire virtual space)*/
    public void exportPNG(File f){//should only be called if JVM is 1.4.0-beta or later (uses package javax.imageio)
	//comment out this method if trying to compile using a JDK 1.3.x
	boolean proceed=true;
	if (!Utils.javaVersionIs140OrLater()){
	    Object[] options={"Yes","No"};
	    int option=JOptionPane.showOptionDialog(null,Messages.pngOnlyIn140FirstPart+System.getProperty("java.vm.version")+Messages.pngOnlyIn140SecondPart,"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
	    if (option!=JOptionPane.OK_OPTION){
		proceed=false;
	    }
	}
	if (proceed){
	    Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	    lastExportRDFDir=f.getParentFile();
	    vsm.getView(mainView).setStatusBarText("Exporting to PNG "+f.toString()+" ... (This operation can take some time)");
	    ImageWriter writer=(ImageWriter)ImageIO.getImageWritersByFormatName("png").next();
	    try {
		writer.setOutput(ImageIO.createImageOutputStream(f));
		java.awt.image.BufferedImage bi=vsm.getView(mainView).getImage();
		if (bi!=null){
		    writer.write(bi);
		    writer.dispose();
		    vsm.getView(mainView).setStatusBarText("Exporting to PNG "+f.toString()+" ...done");
		}
		else {JOptionPane.showMessageDialog(cmp,"An error occured when retrieving the image.\n Please try again.");}
	    }
	    catch (java.io.IOException ex){JOptionPane.showMessageDialog(cmp,"Error while exporting to PNG:\n"+ex);}
	    Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
	}
    }

    /*export the entire RDF graph as SVG locally*/
    public void exportSVG(File f){
	if (f!=null){
	    Editor.mView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	    lastExportRDFDir=f.getParentFile();
	    Editor.mView.setStatusBarText("Exporting to SVG "+f.toString()+" ... (This operation can take some time if the model contains bitmap icons)");
	    if (f.exists()){f.delete();}
	    SVGWriter svgw=new SVGWriter();
	    Document d=svgw.exportVirtualSpace(vsm.getVirtualSpace(mainVirtualSpace),new DOMImplementationImpl(),f,new SVGXLinkAdder());
	    xmlMngr.serialize(d,f);
	    Editor.mView.setStatusBarText("Exporting to SVG "+f.toString()+" ...done");
	    Editor.mView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
	}
    }

    /*enable/disable use of abbreviated syntax when exporting to RDF/XML*/
    public void setAbbrevSyntax(boolean b){
	ABBREV_SYNTAX=b;
    }

    public void displayLabels(boolean b){//finish it--------------------------------------------------
	//have to scan all resources, see if they have a rdfs:label property and assign new VText
	DISP_AS_LABEL=b;
	showResourceLabels(DISP_AS_LABEL);
    }

    /*display anonymous IDs in the graph, or just display blank ellipses*/
    public void showAnonIds(boolean b){
	IResource r;
	VirtualSpace vs=vsm.getVirtualSpace(mainVirtualSpace);
	for (Enumeration e=resourcesByURI.elements();e.hasMoreElements();){
	    r=(IResource)e.nextElement();
	    if (r.isAnon()){//for all anonymous resources
		if (b){vs.show(r.getGlyphText());}
		else {vs.hide(r.getGlyphText());}
	    }
	}
	ConfigManager.SHOW_ANON_ID=b;
    }

    /*if true, display resource URI or rdfs:label if defined; if false, always display resource URI*/
    void showResourceLabels(boolean b){//in any case, the rdfs:label property is still shown as a statement in
	Vector v=getProperties(RDFS_NAMESPACE_URI+"label");//the graph
	if (b){//display labels
	    try {
		IProperty p;
		for (int i=0;i<v.size();i++){
		    p=(IProperty)v.elementAt(i);
		    geomMngr.adjustResourceTextAndShape(p.subject,p.object.getText());
		}
	    }//v might be null if there is no such property in the graph
	    catch (NullPointerException ex){}
	}
	else {//display URIs
	    try {
		IProperty p;
		for (int i=0;i<v.size();i++){
		    p=(IProperty)v.elementAt(i);
		    geomMngr.adjustResourceTextAndShape(p.subject,p.subject.getGraphLabel());
		}
	    }//v might be null if there is no such property in the graph
	    catch (NullPointerException ex){}
	}
    }

    /*create a new IResource from ISV or user-entered data, and add it to the internal model*/
    public IResource addResource(String uri){
	IResource res=new IResource();
	res.setURI(uri);
	if (uri.startsWith(Editor.BASE_URI)){res.setURIFragment(true);}
	String id=res.getIdentity();
	if (!resourcesByURI.containsKey(id)){
	    resourcesByURI.put(id,res);
	    return res;
	}
	else {return (IResource)resourcesByURI.get(id);}
    }

    //if ID is null, a new ID is generated for this resource
    public IResource addAnonymousResource(String id){
	IResource res=new IResource();
	res.setAnon(true);
	res.setURIFragment(false);
	if (id!=null){res.setAnonymousID(id);} //if not already defined
	else {res.setAnonymousID(nextAnonymousID());} //generate a new one
	String id2=res.getIdentity();
	if (!resourcesByURI.containsKey(id2)){
	    resourcesByURI.put(id2,res);
	    return res;
	}
	else {return (IResource)resourcesByURI.get(id2);}
    }

    //create a new IProperty and add it to the internal model (from ISV or user-entered data)
    public IProperty addProperty(String namespace,String localname){
	IProperty res=new IProperty();
	res.setNamespace(namespace);
	res.setLocalname(localname);
	if (propertiesByURI.containsKey(res.getIdent())){
	    Vector v=(Vector)propertiesByURI.get(res.getIdent());
	    v.add(res);
	}
	else {
	    Vector v=new Vector();
	    v.add(res);
	    propertiesByURI.put(res.getIdent(),v);
	}
	addPropertyType(res.getNamespace(),res.getLocalname(),true);  //add to the table of property constructors silently (a property might be used multiple times in existing graphs)
	return res;
    }

    //create a new ILiteral and add it to the internal model (from ISV or user-entered data)
    public ILiteral addLiteral(String value,String lang,boolean wellFormed){
	ILiteral res=new ILiteral();
	res.setValue(value);
	if (lang!=null){res.setLanguage(lang);}
	res.setEscapeXMLChars(wellFormed);
	literals.add(res);
	return res;
    }

    //get an IResource knowing its URI
    public IResource getResource(String uri){
	IResource res=(IResource)resourcesByURI.get(uri);
	return res;
    }

    //get IPropert(-ies) having this URI (null if none)
    public Vector getProperties(String uri){
	Vector res=(Vector)propertiesByURI.get(uri);
	return res;
    }

    /*when the user creates a new resource from scratch in the environment*/
    void createNewResource(long x,long y){
	IResource r=new IResource();
	VEllipse g=new VEllipse(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.resourceColorF);
	r.setGlyph(g);
	vsm.addGlyph(g,mainVirtualSpace);
	g.setHSVbColor(ConfigManager.resTBh,ConfigManager.resTBs,ConfigManager.resTBv);
	new NewResPanel(this,r);
    }

    /*the resource will be stored only after the user gives enough information through NewResPanel (which calls this method) ; uriORid=true if resource defined by a URI, false if defined by an ID*/
    void storeResource(IResource r,String about,boolean uriORid){
	String displayedURI;
	if (about.length()==0){//considered as an anonymous resource - if URI is added later, will change its status
	    r.setAnon(true);
	    r.setAnonymousID(this.nextAnonymousID());
	    displayedURI=r.getGraphLabel();
	}
	else {
	    if (uriORid){
		r.setURI(about);
		r.setURIFragment(false);
		String qname=r.getGraphLabel();
		String[] pref=getNSBindingFromFullURI(qname);
		if (pref!=null && pref[2].equals("T")){
		    qname=pref[0]+":"+qname.substring(pref[1].length(),qname.length());
		}
		displayedURI=qname;
	    }
	    else {
		//insert a hash (#) between base URI and fragment if it is not yet either in the base URI or the fragment input by the user
		if (BASE_URI.endsWith("#") || about.startsWith("#")){
		    r.setURI(BASE_URI+about);
		}
		else {r.setURI(BASE_URI+"#"+about);}
		r.setURIFragment(true);
		displayedURI=r.getGraphLabel();
	    }
	}
	VEllipse el=(VEllipse)r.getGlyph();
	VText g=new VText(el.vx,el.vy,0,ConfigManager.resourceColorTB,displayedURI);
	vsm.addGlyph(g,mainVirtualSpace);
	r.setGlyphText(g);
	//here we use an ugly hack to compute the position of text and size of ellipse because VText.getBounds() is not yet available (computed in another thread at an unknown time) - so we access the VTM view's Graphics object to manually compute the bounds of the text. Very ugly. Shame on me. But right now there is no other way.
	Rectangle2D r2d=vsm.getView(mainView).getGraphicsContext().getFontMetrics().getStringBounds(g.getText(),vsm.getView(mainView).getGraphicsContext());
	
	el.setWidth(Math.round(0.6*r2d.getWidth()));  //0.6 comes from 1.2*Math.round(r2d.getWidth())/2
	//ellipse should always have width > height  (just for aesthetics)
	if (el.getWidth()<(1.5*el.getHeight())){el.setWidth(Math.round(1.5*el.getHeight()));}
	//center VText in ellipse
	g.moveTo(el.vx-(long)r2d.getWidth()/2,el.vy-(long)r2d.getHeight()/4);
	if (r.isAnon() && !ConfigManager.SHOW_ANON_ID){vsm.getVirtualSpace(mainVirtualSpace).hide(g);}
	resourcesByURI.put(r.getIdentity(),r); //we have already checked through resourceAlreadyExists that there is no conflict
	gssMngr.incStyling(r);
	centerRadarView();
    }

    //have to destroy the recently added node+glyph because user canceled is operation
    void cancelNewNode(INode n){
	//just have to remove it form virtual space ; should then be garbage-collected
	vsm.getVirtualSpace(mainVirtualSpace).destroyGlyph(n.getGlyph());
    }

    //called when editing an existing resource
    void makeAnonymous(IResource r){
	resourcesByURI.remove(r.getIdentity());
	r.setAnon(true);
	r.setAnonymousID(this.nextAnonymousID());
	resourcesByURI.put(r.getIdentity(),r);
	r.getGlyphText().setText(r.getIdentity());
	if (!ConfigManager.SHOW_ANON_ID){vsm.getVirtualSpace(mainVirtualSpace).hide(r.getGlyphText());}
	gssMngr.incStyling(r);
    }

    //called when editing an existing resource
    void changeResourceURI(IResource r,String uri,boolean uriORid){
	if (uriORid){//a full URI
	    if (!uri.equals(r.getIdentity())){//trying to change the URI to the same value has no effect
		if (!resourceAlreadyExists(uri)){
		    resourcesByURI.remove(r.getIdentity());
		    if (r.isAnon()){
			r.setAnon(false);
			if (!ConfigManager.SHOW_ANON_ID){vsm.getVirtualSpace(mainVirtualSpace).show(r.getGlyphText());}
		    }
		    r.setURI(uri);
		    r.setURIFragment(false);
		    resourcesByURI.put(r.getIdentity(),r);
		}
		else {JOptionPane.showMessageDialog(propsp,"A resource with URI "+uri+" already exists");}
	    }
	}
	else {//a local ID
	    String id=uri.startsWith(BASE_URI) ? uri.substring(BASE_URI.length(),uri.length()) : uri ;
	    if (!(r.getIdentity().equals(BASE_URI+"#"+id) || r.getIdentity().equals(BASE_URI+id))){//if URI has changed
		//there are 2 teste because at this point we have not yet normalized IDs with '#' (still value entered by user in text field)
		if (!resourceAlreadyExists(BASE_URI+"#"+id)){//if new value does not already exist
		    resourcesByURI.remove(r.getIdentity());
		    if (r.isAnon()){
			r.setAnon(false);
			if (!ConfigManager.SHOW_ANON_ID){vsm.getVirtualSpace(mainVirtualSpace).show(r.getGlyphText());}
		    }
		    if (BASE_URI.endsWith("#") || uri.startsWith("#")){
			r.setURI(BASE_URI+uri);
		    }
		    else {r.setURI(BASE_URI+"#"+uri);}
		    r.setURIFragment(true);
		    resourcesByURI.put(r.getIdentity(),r);
		}
		else {JOptionPane.showMessageDialog(propsp,"A resource with ID "+uri+" already exists");}
	    }
	}
	if (DISP_AS_LABEL && r.getLabel() != null){
	    geomMngr.adjustResourceTextAndShape(r, r.getLabel());
	}
	else {
	    String qname=r.getGraphLabel();
	    String[] pref=getNSBindingFromFullURI(qname);
	    if (pref!=null && pref[2].equals("T")){
		qname=pref[0]+":"+qname.substring(pref[1].length());
	    }
	    geomMngr.adjustResourceTextAndShape(r, qname);
	}
	VText g=r.getGlyphText();
	if (!g.isVisible()){vsm.getVirtualSpace(mainVirtualSpace).show(g);}
	gssMngr.incStyling(r);
    }

    /*returns true if a resource with this URI already exists in the internal model*/
    boolean resourceAlreadyExists(String uri){
	if (resourcesByURI.containsKey(uri)){return true;}
	else {return false;}
    }

    /*when the user creates a new resource from scratch in the environment*/
    void createNewLiteral(long x,long y){
	ILiteral l=new ILiteral();
	VRectangle g=new VRectangle(x,y,0,GeometryManager.DEFAULT_NODE_WIDTH,GeometryManager.DEFAULT_NODE_HEIGHT,ConfigManager.literalColorF);
	vsm.addGlyph(g,mainVirtualSpace);
	g.setHSVbColor(ConfigManager.litTBh,ConfigManager.litTBs,ConfigManager.litTBv);
	l.setGlyph(g);
	new NewLitPanel(this,l);
    }

    /*the resource will be stored only after the user gives enough information through NewResPanel (which calls this method)*/
    void storeLiteral(ILiteral l,String value,boolean typed,String lang,String dturi){
	if (lang.length()>0){l.setLanguage(lang);}
	if (typed){l.setDatatype(dturi);}
	l.setEscapeXMLChars(true);
	setLiteralValue(l,value);
	literals.add(l);
	centerRadarView();
    }

    /*set the value of the ILiteral and updates its VText in the graph view*/
    public void setLiteralValue(ILiteral l,String value){
	l.setValue(value);
	if (value.length()>0){
	    String truncText=((l.getValue().length()>=Editor.MAX_LIT_CHAR_COUNT) ? l.getValue().substring(0,Editor.MAX_LIT_CHAR_COUNT)+" ..." :l.getValue());
	    if (l.getGlyphText()!=null){l.getGlyphText().setText(truncText);}
	    else {//literal is empty, so it does not have an associated VText - have to create it
		Glyph rt=l.getGlyph();
		VText g=new VText(rt.vx,rt.vy,0,l.isSelected() ? ConfigManager.selectionColorTB : ConfigManager.literalColorTB,truncText);
		vsm.addGlyph(g,mainVirtualSpace);
		l.setGlyphText(g);
// 		VRectangle rt=(VRectangle)l.getGlyph();
// 		//here we use an ugly hack to compute the position of text and size of rectangle because VText.getBounds() is not yet available (computed in another thread at an unknown time) - so we access the VTM view's Graphics object to manually compute the bounds of the text. Very ugly. Shame on me. But right now there is no other way.
// 		Rectangle2D r2d=vsm.getView(mainView).getGraphicsContext().getFontMetrics().getStringBounds(truncText,vsm.getView(mainView).getGraphicsContext());
// 		rt.setWidth(Math.round(0.6*r2d.getWidth()));  //0.6 comes from 1.2*Math.round(r2d.getWidth())/2
// 		//rectangle should always have width > height  (just for aesthetics)
// 		if (rt.getWidth()<(1.5*rt.getHeight())){rt.setWidth(Math.round(1.5*rt.getHeight()));}
// 		//center VText in rectangle
// 		g.moveTo(rt.vx-(long)r2d.getWidth()/2,rt.vy-(long)r2d.getHeight()/4);
		geomMngr.correctLiteralTextAndShape(l);
	    }
	}
	else {//get rid of the VText if value is set to empty
	    if (l.getGlyphText()!=null){
		vsm.getVirtualSpace(mainVirtualSpace).destroyGlyph(l.getGlyphText());
		l.setGlyphText(null);
	    }
	}
	IProperty p=l.getIncomingPredicate();
	if (p!=null && p.getNamespace().equals(RDFS_NAMESPACE_URI) && p.getLocalname().equals("label")){//in case this literal is the object of an rdfs:label statement, update 
	    p.subject.setLabel(l.getValue());
	    if (DISP_AS_LABEL){geomMngr.adjustResourceTextAndShape(p.subject,p.subject.getLabel());}
	}
	gssMngr.incStyling(l);
    }

    //displays a dialog containing a list of all datatypes available through the Jena TypeMapper, and returns the one selected by the user
    static com.hp.hpl.jena.datatypes.RDFDatatype displayAvailableDataTypes(String oldType){
	return DatatypeChooser.getDatatypeChooser(((propsp==null) ? (java.awt.Frame)cmp : (java.awt.Frame)propsp),oldType);
    }

    //displays a dialog containing a list of all datatypes available through the Jena TypeMapper, and returns the one selected by the user
    static com.hp.hpl.jena.datatypes.RDFDatatype displayAvailableDataTypes(java.awt.Dialog owner,String oldType){
	return DatatypeChooser.getDatatypeChooser(owner,oldType);
    }

    /*when the user creates a new property from scratch in the environment - points is a Vector of LongPoint*/
    void createNewProperty(IResource subject,INode object,Vector points){
	createNewProperty(subject,object,points,selectedPropertyConstructorNS,selectedPropertyConstructorLN);
    }

    /*when the user creates a new property from scratch in the environment - points is a Vector of LongPoint*/
    void createNewProperty(IResource subject,INode object,Vector points,String propNS,String propLN){
	boolean error=false;
	if ((object instanceof ILiteral) && (((ILiteral)object).getIncomingPredicate()!=null)){error=true;JOptionPane.showMessageDialog(vsm.getActiveView().getFrame(),"This literal is already the object of a statement.");}
	if (!error){
	    IProperty res;
	    if (propNS.equals(RDFMS_NAMESPACE_URI) && propLN.startsWith(MEMBERSHIP_PROP_CONSTRUCTOR.substring(0,3))){//user selected the membership property auto-numbering constructor
		res=addProperty(RDFMS_NAMESPACE_URI,IContainer.nextContainerIndex(subject));
	    }
	    else {//any other property type
		res=addProperty(propNS,propLN);
	    }
	    res.setSubject(subject);
	    subject.addOutgoingPredicate(res);
	    if (object instanceof IResource){
		IResource object2=(IResource)object;
		res.setObject(object2);
		object2.addIncomingPredicate(res);
	    }
	    else {//object is an ILiteral (or we have an error)
		ILiteral object2=(ILiteral)object;
		res.setObject(object2);
		object2.setIncomingPredicate(res);
		if (res.getIdent().equals(Editor.RDFS_NAMESPACE_URI+"label")){
		    //if property is rdfs:label, set label for the resource
		    subject.setLabel(object2.getValue());
		    subject.getGlyphText().setText(subject.getLabel());
		}
	    }
	    LongPoint lp1=(LongPoint)points.elementAt(0);
	    LongPoint lp2=(LongPoint)points.elementAt(1);
	    //have to modify first and last point so that they begin at the node's boundary, not its center
	    //modification for start point
	    //compute the direction from center of glyph to point 2 on curve
	    Point2D newPoint=new Point2D.Double(lp1.x,lp1.y);
	    Point2D delta=GeometryManager.computeStepValue(lp1,lp2);
	    //we then walk in this direction until we get out of the subject
// 	    VEllipse el1=subject.getGlyph();
	    Glyph el1=subject.getGlyph();
// 	    Ellipse2D el2=new Ellipse2D.Double(el1.vx-el1.getWidth(),el1.vy-el1.getHeight(),el1.getWidth()*2,el1.getHeight()*2);
	    Shape el2=GlyphUtils.getJava2DShape(el1);
	    while (el2.contains(newPoint)){
		newPoint.setLocation(newPoint.getX()+delta.getX(),newPoint.getY()+delta.getY());
	    }
	    //when we find the point on the boundary of the ellipse, still in the direction of the second point on the path, we assign its coordinates to the first point on path
	    lp1.setLocation(Math.round(newPoint.getX()),Math.round(newPoint.getY()));
	    VPath pt=new VPath(lp1.x,lp1.y,0,ConfigManager.propertyColorB);
	    //then add following points
// 	    pt.addSegment((lp1.x+lp2.x)/2,(lp1.y+lp2.y)/2,true);
// 	    for (int i=1;i<points.size()-1;i++){
// 		lp1=(LongPoint)points.elementAt(i);
// 		lp2=(LongPoint)points.elementAt(i+1);
// 		pt.addQdCurve((lp2.x+lp1.x)/2,(lp2.y+lp1.y)/2,lp1.x,lp1.y,true);
// 	    }
	    for (int i=1;i<points.size()-2;i++){//old version started by a segment - was the source of ericP's elbows
		lp1=(LongPoint)points.elementAt(i);//now we begin directly with a Quad curve
		lp2=(LongPoint)points.elementAt(i+1);
		pt.addQdCurve((lp2.x+lp1.x)/2,(lp2.y+lp1.y)/2,lp1.x,lp1.y,true);
	    }
	    //finally, for last point, do something similar to what's been done for first point (place it on the edge of the object's shape)
	    lp2=(LongPoint)points.elementAt(points.size()-2);
	    lp1=(LongPoint)points.lastElement();
	    //modification for end point
	    //compute the direction from center of glyph to one before last point on curve
	    Point2D newPoint2=new Point2D.Double(lp1.x,lp1.y);
	    Point2D delta2=GeometryManager.computeStepValue(lp1,lp2);
	    Glyph el3=object.getGlyph();
	    Shape el4=GlyphUtils.getJava2DShape(el3);
	    while (el4.contains(newPoint2)){
		newPoint2.setLocation(newPoint2.getX()+delta2.getX(),newPoint2.getY()+delta2.getY());
	    }
// 	    if (object instanceof IResource){//we have a VEllipse
// 		//we then walk in this direction until we get out of the object (which is an ellipse in this case)
// 		VEllipse el3=(VEllipse)object.getGlyph();
// 		Ellipse2D el4=new Ellipse2D.Double(el3.vx-el3.getWidth(),el3.vy-el3.getHeight(),el3.getWidth()*2,el3.getHeight()*2);
// 		while (el4.contains(newPoint2)){
// 		    newPoint2.setLocation(newPoint2.getX()+delta2.getX(),newPoint2.getY()+delta2.getY());
// 		}
// 	    }
// 	    else {//instanceof ILiteral - we have a VRectangle
// 		//we then walk in this direction until we get out of the object (which is an ellipse in this case)
// 		VRectangle rl3=(VRectangle)object.getGlyph();
// 		Rectangle2D rl4=new Rectangle2D.Double(rl3.vx-rl3.getWidth(),rl3.vy-rl3.getHeight(),rl3.getWidth()*2,rl3.getHeight()*2);
// 		while (rl4.contains(newPoint2)){
// 		    newPoint2.setLocation(newPoint2.getX()+delta2.getX(),newPoint2.getY()+delta2.getY());
// 		}
// 	    }
	    //when we find the point on the boundary of the shape, 
	    //we assign its coordinates to the first point on path
	    lp1.setLocation(Math.round(newPoint2.getX()),Math.round(newPoint2.getY()));
	    //then add the last curve/segment to the path using the newly computed point
	    if (points.size()>2){//old version created a segment - now we finish by a quad curve
		pt.addQdCurve(lp1.x,lp1.y,lp2.x,lp2.y,true);
	    }
	    else {//unless the user did not specify any intermediate point, in which case the path is just made of one segment (straight)
		pt.addSegment(lp1.x,lp1.y,true);
	    }
// 	    pt.addSegment(lp1.x,lp1.y,true);
	    vsm.addGlyph(pt,mainVirtualSpace);
	    //ARROW HEAD
	    //at this point lp1 holds the coordinates of the path's end point, and lp2 the coordinates of the one point just before lp1 in the path
	    VTriangleOr tr=GeometryManager.createPathArrowHead(lp2,lp1,null);
	    vsm.addGlyph(tr,Editor.mainVirtualSpace);
	    //TEXT - display namespace as prefix or URI depending on user's prefs
	    String uri=""; //this will be used later to create the IProperty's VText in the graph
	    boolean bindingDefined=false;
	    for (int i=0;i<tblp.nsTableModel.getRowCount();i++){//retrieve NS binding if defined
		if (((String)tblp.nsTableModel.getValueAt(i,1)).equals(propNS)){
		    if (((Boolean)tblp.nsTableModel.getValueAt(i,2)).booleanValue()){
			uri=((String)tblp.nsTableModel.getValueAt(i,0))+":"+res.getLocalname();
		    }
		    else {uri=propNS+res.getLocalname();}
		    bindingDefined=true;
		    break;
		}
	    }
	    if (!bindingDefined){uri=propNS+res.getLocalname();}
	    long posx,posy;
	    if (points.size()%2!=0){//try to position the text to the best location possible
		posx=((LongPoint)points.elementAt(points.size()/2)).x;
		posy=((LongPoint)points.elementAt(points.size()/2)).y;
	    }
	    else {
		posx=(((LongPoint)points.elementAt(points.size()/2)).x+((LongPoint)points.elementAt(points.size()/2-1)).x)/2;
		posy=(((LongPoint)points.elementAt(points.size()/2)).y+((LongPoint)points.elementAt(points.size()/2-1)).y)/2;
	    }
	    VText tx=new VText(posx,posy,0,ConfigManager.propertyColorT,uri);
	    vsm.addGlyph(tx,mainVirtualSpace);
	    res.setGlyph(pt,tr);
	    res.setGlyphText(tx);
	    //subject and object styling might change as a result of adding this new property to them
	    gssMngr.incStyling(res);
	    if (!res.isLaidOutInTableForm() && tr!=null){//in std node-edge layout,
		//adapt arrow head size to edge's thickness (otherwise it might be too small, or even not visible)
		if (pt.getStrokeWidth()>2.0f){
		    tr.reSize(pt.getStrokeWidth()/2.0f);
		}
	    }
	    centerRadarView();
	}
    }

    /*change the type of a statement's predicate*/
    void changePropertyURI(IProperty p,String ns,String ln){
	String uri;
	if ((uri=getNSURIfromPrefix(ns))==null){uri=ns;} //replace prefix by uri if necessary
	if (!p.getIdent().equals(uri+ln)){//if the property has really changed, we have to update propertiesByURI
	    if (p.getNamespace().equals(RDFS_NAMESPACE_URI) && p.getLocalname().equals("label")){
		//property WAS rdfs:label, but we are changing - update subject's label
		p.subject.setLabel("");
		if (Editor.DISP_AS_LABEL){geomMngr.adjustResourceTextAndShape(p.subject,p.subject.getGraphLabel());}
	    }
	    if (uri.equals(RDFMS_NAMESPACE_URI) && ln.startsWith(MEMBERSHIP_PROP_CONSTRUCTOR.substring(0,3))){
		ln=IContainer.nextContainerIndex(p.subject); //replace _??.... by the first real available index
	    }
	    else {
		if (uri.equals(RDFS_NAMESPACE_URI) && ln.equals("label")){
		    //property was anything but we are changing to rdfs:label - display the statement's object value
		    p.subject.setLabel(p.object.getText());
		    if (Editor.DISP_AS_LABEL){geomMngr.adjustResourceTextAndShape(p.subject,p.subject.getLabel());}
		}
	    }
	    Vector v=(Vector)propertiesByURI.get(p.getIdent());
	    v.remove(p);
	    p.setNamespace(uri);
	    p.setLocalname(ln);
	    if (propertiesByURI.containsKey(p.getIdent())){
		Vector v2=(Vector)propertiesByURI.get(p.getIdent());
		v2.add(p);
	    }
	    else {
		Vector v2=new Vector();
		v2.add(p);
		propertiesByURI.put(p.getIdent(),v2);
	    }
	    if (showThisNSAsPrefix(p.getNamespace(),true)){
		geomMngr.updateAPropertyText(p,ns+":"+p.getLocalname());  //ns still holds the prefix at this time (but not uri)
	    }
	    else {
		geomMngr.updateAPropertyText(p,p.getIdent());  //here should display prefix if appropriate
	    }
	    gssMngr.incStyling(p);
	}
    }

    /*change the subject of a statement*/
    static void changePropertySubject(IProperty p,IResource newSubject){
	//first remove the property from the list of outgoing predicates in old subject
	if (p.getSubject()!=null && p.getSubject()!=newSubject){
	    IResource oldSubject=p.getSubject();oldSubject.removeOutgoingPredicate(p);
	}
	//then attach it as an outgoing predicate to the new subject
	p.setSubject(newSubject);
	newSubject.addOutgoingPredicate(p);
	gssMngr.incStyling(p);
    }

    /*change the object of a statement*/
    static void changePropertyObject(IProperty p,INode newObject){
	//first remove the property from the list of incoming predicates in old object
	if (p.getObject()!=null && p.getObject()!=newObject){
	    INode oldObject=p.getObject();
	    if (oldObject instanceof ILiteral){((ILiteral)oldObject).setIncomingPredicate(null);}
	    else {((IResource)oldObject).removeIncomingPredicate(p);}
	}
	//then attach it as an incoming predicate to the new object
	if (newObject instanceof ILiteral){
	    p.setObject((ILiteral)newObject);
	    ((ILiteral)newObject).setIncomingPredicate(p);
	}
	else {//instanceof IResource
	    p.setObject((IResource)newObject);
	    ((IResource)newObject).addIncomingPredicate(p);
	}
	gssMngr.incStyling(p);
    }

    /*selects all resources whose URI contains uriFragment*/
    void selectResourcesMatching(String uriFragment){
	if (uriFragment.length()>0){
	    String key;
	    for (Enumeration e=resourcesByURI.keys();e.hasMoreElements();){
		key=(String)e.nextElement();
	    if (key.indexOf(uriFragment)!=-1){selectResource((IResource)resourcesByURI.get(key),true);}
	    }
	    if (lastSelectedItem!=null){propsp.updateDisplay(lastSelectedItem);}
	}
    }

    /*selects all property instances whose namespace contains nsFragment and local name contains lnFragment
      for both parameters, empty string acts as wildcard ("*")
      nsFragment can be a FULL (no fragment) namespace prefix ending with a colon (":")
    */
    void selectPropertiesMatching(String nsFragment,String lnFragment){
	if (nsFragment.length()>0 || lnFragment.length()>0){
	    String trueNS=nsFragment;
	    if (nsFragment.endsWith(":")){
		trueNS=getNSURIfromPrefix(nsFragment.substring(0,nsFragment.length()-1));
		if (trueNS==null){trueNS="";} //getNSURIformPrefix returns null if no binding uses this prefix
	    }
	    IProperty p;
	    for (Enumeration e=propertiesByURI.elements();e.hasMoreElements();){
		for (Enumeration e2=((Vector)e.nextElement()).elements();e2.hasMoreElements();){
		    p=(IProperty)e2.nextElement();
		    if (trueNS.length()>0){
			if (lnFragment.length()>0){
			    if ((p.getNamespace().indexOf(trueNS)!=-1) && (p.getLocalname().indexOf(lnFragment)!=-1)){selectPredicate(p,true);}
			}
			else {
			    if (p.getNamespace().indexOf(trueNS)!=-1){selectPredicate(p,true);}
			}
		    }
		    else {
			if (lnFragment.length()>0){
			    if (p.getLocalname().indexOf(lnFragment)!=-1){selectPredicate(p,true);}
			}
			//else nether occurs (caught by root test)
		    }
		}
	    }
	}
	if (lastSelectedItem!=null){propsp.updateDisplay(lastSelectedItem);}
    }

    /*selects all literals whose value contains fragment*/
    void selectLiteralsMatching(String fragment){
	if (fragment.length()>0){
	    ILiteral literal;
	    for (Enumeration e=literals.elements();e.hasMoreElements();){
		if ((literal=(ILiteral)e.nextElement()).getValue().indexOf(fragment)!=-1){
		    selectLiteral(literal,true);
		}
	    }
	    if (lastSelectedItem!=null){propsp.updateDisplay(lastSelectedItem);}
	}
    }

    /*(un)select a single resource*/
    public void selectResource(IResource r,boolean b){
	if (b!=r.isSelected()){
	    r.setSelected(b);
	    if (b){if (!selectedResources.contains(r)){selectedResources.add(r);lastSelectedItem=r;}}
	    else {selectedResources.remove(r);}
	}
    }

    /*select all properties associated with a resource*/
    void selectPropertiesOfResource(IResource r){
	Vector v;
	IProperty p;
	if ((v=r.getIncomingPredicates())!=null){
	    for (int i=0;i<v.size();i++){
		p=(IProperty)v.elementAt(i);
		selectPredicate(p,true);
	    }
	}
	if ((v=r.getOutgoingPredicates())!=null){
	    for (int i=0;i<v.size();i++){
		p=(IProperty)v.elementAt(i);
		selectPredicate(p,true);
	    }
	}
    }

    /*(un)select a single literal*/
    public void selectLiteral(ILiteral l,boolean b){
	if (b!=l.isSelected()){
	    l.setSelected(b);
	    if (b){if (!selectedLiterals.contains(l)){selectedLiterals.add(l);lastSelectedItem=l;}}
	    else {selectedLiterals.remove(l);}
	}
    }

    /*select the property that might be associated with this literal*/
    void selectPropertiesOfLiteral(ILiteral l){
	IProperty p;
	if ((p=l.getIncomingPredicate())!=null){
	    selectPredicate(p,true);
	}
    }
    
    /*(un)select a single property instance*/
    public void selectPredicate(IProperty p,boolean select){
	selectPredicate(p,select,false);
    }

    /*(un)select a single property instance*/
    void selectPredicate(IProperty p,boolean select,boolean selectedByEdgeClick){
	if (select!=p.isSelected()){
	    if (selectedByEdgeClick && p.isLaidOutInTableForm()){
		if (select){lastSelectedItem=p;}
		if (p.getSubject()!=null){
		    Vector v=p.getSubject().getOutgoingPredicates();
		    IProperty tmpP;
		    try {//v should always be non-null
			for (int i=0;i<v.size();i++){
			    tmpP=(IProperty)v.elementAt(i);
			    if (tmpP.getGlyph()==p.getGlyph()){//for all properties in the table whose edge has been selected
				tmpP.setSelected(select,true);
				if (select){if (!selectedPredicates.contains(tmpP)){selectedPredicates.add(tmpP);}}
				else {selectedPredicates.remove(tmpP);}
			    }
			}
		    }
		    catch (NullPointerException ex){System.err.println("Error:Editor.selectPredicate(): unable to select property "+p);ex.printStackTrace();}
		}
	    }
	    else {
		p.setSelected(select,!selectedByEdgeClick);
		if (select){if (!selectedPredicates.contains(p)){selectedPredicates.add(p);lastSelectedItem=p;}}
		else {selectedPredicates.remove(p);}
	    }
	}
    }

    /*select the property that might be associated with this literal*/
    void selectNodesOfProperty(IProperty p){
	selectResource(p.getSubject(),true);  //properties necessarily have a subject and object, they cannot exist on their own
	INode n=p.getObject();
	if (n instanceof IResource){selectResource((IResource)n,true);}
	else {selectLiteral((ILiteral)n,true);}//instanceof ILiteral
    }

    /*select all resources and literals*/
    void selectAllNodes(){
// 	for (Enumeration en=resourcesByURI.elements();en.hasMoreElements();){
// 	    selectResource((IResource)en.nextElement(),true);
// 	}
// 	for (Enumeration en=literals.elements();en.hasMoreElements();){
// 	    selectLiteral((ILiteral)en.nextElement(),true);
// 	}
	/*the IsaViz 1.x method consisted in taking every resource and literal from the internal tables
	  this no longer works in IsaViz 2.x, as these tables also contain invisible nodes, which should not be selected
	  therefore, the new method relies on the virtual space's content, from which we retrieve glyphs, and then INodes	
	*/
	//resources
	Vector v=mSpace.getGlyphsOfType(Editor.resShapeType);
	for (int i=0;i<v.size();i++){
	    selectResource((IResource)((Glyph)v.elementAt(i)).getOwner(),true);
	}
	v=mSpace.getGlyphsOfType(Editor.litShapeType);
	for (int i=0;i<v.size();i++){
	    selectLiteral((ILiteral)((Glyph)v.elementAt(i)).getOwner(),true);
	}
    }

    /*select all property instances*/
    void selectAllEdges(){
// 	for (Enumeration en1=propertiesByURI.elements();en1.hasMoreElements();){
// 	    for (Enumeration en2=((Vector)en1.nextElement()).elements();en2.hasMoreElements();){
// 		selectPredicate((IProperty)en2.nextElement(),true);
// 	    }
// 	}
	/*the IsaViz 1.x method consisted in taking every resource and literal from the internal tables
	  this no longer works in IsaViz 2.x, as these tables also contain invisible nodes, which should not be selected
	  therefore, the new method relies on the virtual space's content, from which we retrieve glyphs, and then INodes	
	*/
	Vector v=mSpace.getGlyphsOfType(Editor.propPathType);
	for (int i=0;i<v.size();i++){
	    selectPredicate((IProperty)((Glyph)v.elementAt(i)).getOwner(),true);
	}
    }

    //unselect last selected item (does not matter whether it was a property instance, resource or literal)
    void unselectLastSelection(){
	propsp.reset();
	tblp.updatePropertyBrowser(null,false);
	unselectAll();
	lastSelectedItem=null;
    }

    /*unselect all nodes and edges*/
    public void unselectAll(){
	propsp.reset();
	for (int i=selectedResources.size()-1;i>=0;i--){
	    selectResource((IResource)selectedResources.elementAt(i),false);
	}
	for (int i=selectedLiterals.size()-1;i>=0;i--){
	    selectLiteral((ILiteral)selectedLiterals.elementAt(i),false);
	}
	for (int i=selectedPredicates.size()-1;i>=0;i--){
	    selectPredicate((IProperty)selectedPredicates.elementAt(i),false);
	}
	if (!selectedResources.isEmpty()){System.err.println("Debug: Editor.unselectAll(): selectedResources should be empty");}
	if (!selectedLiterals.isEmpty()){System.err.println("Debug: Editor.unselectAll(): selectedLiterals should be empty ; size="+selectedLiterals.size());}
	if (!selectedPredicates.isEmpty()){System.err.println("Debug: Editor.unselectAll(): selectedPredicates should be empty");}
	lastSelectedItem=null;
    }

    //remove a resource (called by GUI)
    public void deleteResource(IResource r){
	//remove all incoming and outgoing predicates
	Vector v;
	if ((v=r.getIncomingPredicates())!=null){
	    for (int i=v.size()-1;i>=0;i--){deleteProperty((IProperty)v.elementAt(i));}
	}
	if ((v=r.getOutgoingPredicates())!=null){
	    for (int i=v.size()-1;i>=0;i--){deleteProperty((IProperty)v.elementAt(i));}
	}
	if (r.isVisuallyRepresented()){
	    //destroy glyphs
	    VirtualSpace vs=vsm.getVirtualSpace(mainVirtualSpace);
	    vs.destroyGlyph(r.getGlyph());
	    if (r.getGlyphText()!=null){vs.destroyGlyph(r.getGlyphText());}
	}
	//remove from resourcesByURI
	removeResource(r);
    }

    //remove a resource from internal model, and from list of selected resources if present
    void removeResource(IResource r){
	if (resourcesByURI.containsKey(r.getIdentity())){
	    resourcesByURI.remove(r.getIdentity());
	}
	selectResource(r,false);
    }

    //remove a literal (called by GUI)
    public void deleteLiteral(ILiteral l){
	//remove incoming property if exists
	if (l.getIncomingPredicate()!=null){deleteProperty(l.getIncomingPredicate());}
	if (l.isVisuallyRepresented()){
	    //destroy glyphs
	    VirtualSpace vs=vsm.getVirtualSpace(mainVirtualSpace);
	    vs.destroyGlyph(l.getGlyph());
	    if (l.getGlyphText()!=null){vs.destroyGlyph(l.getGlyphText());}
	}
	//remove from literals
	removeLiteral(l);
    }

    //remove an ILiteral from internal model, and from list of selected literals if present
    void removeLiteral(ILiteral l){
	literals.remove(l);
	selectLiteral(l,false);
    }

    //remove a property, called by GUI or by deleteResource/deleteLiteral (pending edges are not allowed)
    public void deleteProperty(IProperty p){
	IResource subj=null;
	INode obj=null;
	//remove links to subject and object
	if (p.getSubject()!=null){
	    subj=p.getSubject();
	    subj.removeOutgoingPredicate(p);
	    if (p.getNamespace().equals(RDFS_NAMESPACE_URI) && p.getLocalname().equals("label")){
		//subject's text gets back to resource URI since we are deleting the rdfs:label property
		geomMngr.adjustResourceTextAndShape(p.subject,p.subject.getGraphLabel());
	    }
	}
	if (p.getObject()!=null){
	    obj=p.getObject();
	    if (obj instanceof IResource){
		((IResource)obj).removeIncomingPredicate(p);
	    }
	    else {//instanceof ILiteral
		((ILiteral)obj).setIncomingPredicate(null);
	    }
	    if (obj.isLaidOutInTableForm()){obj.setTableFormLayout(false);}
	}
	if (p.isVisuallyRepresented()){
	    //destroy glyphs
	    VirtualSpace vs=vsm.getVirtualSpace(mainVirtualSpace);
	    if (p.isLaidOutInTableForm() && IProperty.sharedPropertyArc(p)){
		//we should assign the edge and arrow head an owner corresponding to an existing IProperty 
		//in the table if the current owner is the IProperty being destroyed
		if (p.getGlyph().getOwner()==p || (p.getGlyphHead()!=null && p.getGlyphHead().getOwner()==p)){
		    Vector v=IProperty.getAllPropertiesInSameTableAs(p);
		    if (v.size()>0){//should always be the case
			//assigning the first iproperty we find that is part of the same table
			p.getGlyph().setOwner(v.firstElement());
			if (p.getGlyphHead()!=null){p.getGlyphHead().setOwner(v.firstElement());}
		    }
		}
	    }
	    else {
		vs.destroyGlyph(p.getGlyph());
		if (p.getGlyphHead()!=null){vs.destroyGlyph(p.getGlyphHead());}
	    }
	    if (p.getGlyphText()!=null){vs.destroyGlyph(p.getGlyphText());}
	    if (p.getTableCellGlyph()!=null){vs.destroyGlyph(p.getTableCellGlyph());}
	}
	//remove from propertiesByURI
	removeProperty(p);

	/*no incremental styling here, as it takes forever to delete stuff if we do incremental styling each time. we should find another way of applying style incrementally, only once, after all things have been deleted*/
// 	if (subj!=null){gssMngr.incStyling(subj);}
// 	if (obj!=null){
// 	    if (obj instanceof IResource){gssMngr.incStyling((IResource)obj);}
// 	    else {gssMngr.incStyling((ILiteral)obj);}
// 	}
    }

    //remove an IProperty from internal model, remove entry for this URI if empty - erase from list of selected properties if present
    void removeProperty(IProperty p){
	if (propertiesByURI.containsKey(p.getIdent())){
	    Vector v=(Vector)propertiesByURI.get(p.getIdent());
	    v.remove(p);
	    if (v.isEmpty()){propertiesByURI.remove(p.getIdent());}
	}
	selectPredicate(p,false);
    }

    public void commentAll(boolean b){
	for (Enumeration e=resourcesByURI.elements();e.hasMoreElements();){
	    commentNode((IResource)e.nextElement(), b, false);
	}
	for (int i=0;i<literals.size();i++){
	    commentNode((ILiteral)literals.elementAt(i), b, false);
	}
	Vector v;
	for (Enumeration e=propertiesByURI.elements();e.hasMoreElements();){
	    v = (Vector)e.nextElement();
	    for (int i=0;i<v.size();i++){
		commentNode((IProperty)v.elementAt(i), b, false);
	    }
	}
    }

    //(un)comment resource or literal - will (un)comment out properties when appropriate
    public void commentNode(INode n, boolean b, boolean propagate){
	if (b && (!n.isCommented())){
	    n.comment(b, this, propagate);
	}
	else if ((!b) && (n.isCommented())){
	    n.comment(b, this, propagate);
	}
	/*no incremental styling here, as it takes forever to comment stuff if we
	  do incremental styling each time. we should find another way of applying
	  style incrementally, only once, after all things have been commented*/
    }

    //(un)comment property - does not modify resource/literal linked to it
    public void commentPredicate(IProperty p, boolean b, boolean propagate){
	if (b && (!p.isCommented())){
	    p.comment(b, this, propagate);
	}
	else if ((!b) && (p.isCommented())){
	    p.comment(b, this, propagate);
	}
	/*no incremental styling here, as it takes forever to comment stuff if we
	  do incremental styling each time. we should find another way of applying
	  style incrementally, only once, after all things have been commented*/
    }

    //this methods takes all the namespace information from a Jena model and declares as many bindings as possible, leaving other namespaces without bindings but still declaring them
    void declareNSBindings(Map bindings,NsIterator nsit){
	Hashtable finalBindings=new Hashtable();
	Iterator it=bindings.keySet().iterator();
	String prefix;
	String namespace;
	while (it.hasNext()){
	    prefix=(String)it.next();
	    namespace=(String)bindings.get(prefix);
	    finalBindings.put(namespace,prefix);  //in theory, there cannot be any conflict as all xmlns decls come from the same RDF/XML file
	}//in case there is one, we just take the last binding declaration
	Vector namespacesWithoutAPrefix=new Vector();
	while (nsit.hasNext()){
	    namespace=nsit.nextNs();
	    if (!finalBindings.containsKey(namespace)){namespacesWithoutAPrefix.add(namespace);}
	}
	for (Enumeration e=finalBindings.keys();e.hasMoreElements();){
	    namespace=(String)e.nextElement();
	    prefix=(String)finalBindings.get(namespace);
	    //System.err.println(prefix+"-$-"+namespace);
	    addNamespaceBinding(prefix,namespace,new Boolean(true),true,true);//do it silently, don't override display state, override prefix for existing bindings
	}
	for (Enumeration e=namespacesWithoutAPrefix.elements();e.hasMoreElements();){
	    //System.err.println(prefix+"-#-"+namespace);
	    addNamespaceBinding("",(String)e.nextElement(),new Boolean(false),true,false);//do it silently, don't override display state, don't override prefix for existing bindings (might be a prefix defined in the default namespaces)
	}
	finalBindings.clear();
	namespacesWithoutAPrefix.removeAllElements();
	schemaMngr.updateNamespaces();
    }

    //this method is called both when the user adds manually a binding and when we load/import ISV/RDF. In the second case we do not want dialogs to appear if bindings like rdfms have already been defined, so we set silent to true in that case. When loading an ISV file, bindings from this file override any binding already present (like rdf and rdfs).
    boolean addNamespaceBinding(String prefix,String uri,Boolean display,boolean silent,boolean override){
	boolean prefAlreadyInUse=false;
	boolean uriAlreadyInUse=false;
	for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
	    if (prefix.length()>0 && prefix.equals((String)tblp.nsTableModel.getValueAt(i,0))){//prefix can be "" if no binding is assigned
		prefAlreadyInUse=true;
		if (!silent){JOptionPane.showMessageDialog(tblp,"Prefix "+prefix+" is already assigned to namespace "+(String)tblp.nsTableModel.getValueAt(i,1));}
		if (!override){return false;} //don't return if overrid (occurs in the next test)
	    }
	    if (uri.equals((String)tblp.nsTableModel.getValueAt(i,1))){
		uriAlreadyInUse=true;
		if (!silent){JOptionPane.showMessageDialog(tblp,"Namespace URI "+uri+" is already binded to prefix "+(String)tblp.nsTableModel.getValueAt(i,0));}
		if (!override){return false;}
		else {//conflict detected - override existing prefix and display_as_prefix in the table with new param values
		    tblp.nsTableModel.setValueAt(prefix,i,0);
		    tblp.nsTableModel.setValueAt(display,i,2);
		    return true;
		}
	    }
	}
// 	if (prefix.equals(ANON_NODE.substring(0,ANON_NODE.length()-1)) || prefix.equals(BASE_URI.substring(0,BASE_URI.length()))){
	if (prefix.equals(ANON_NODE.substring(0,ANON_NODE.length()-1))){
	    prefAlreadyInUse=true;
	    if (!silent){JOptionPane.showMessageDialog(tblp,"Prefix '"+prefix+"' is already used as the anonymous node prefix");}
// 	    if (!silent){JOptionPane.showMessageDialog(tblp,"Prefix '"+prefix+"' is either used as the anonymous node prefix or the base prefix");}
	}
	if (!(prefAlreadyInUse || uriAlreadyInUse)){//no conflict
	    //the update of namespaceBindings will be handled by updateNamespaceBinding
	    //since addRow fires a tableChanged event
	    Vector v=new Vector();v.add(prefix);v.add(uri);v.add(display);
	    String aURI;
	    int i;
	    for (i=0;i<tblp.nsTableModel.getRowCount();i++){//find where to insert the new binding in the table 
		aURI=(String)tblp.nsTableModel.getValueAt(i,1);//(sorted lexicographically)
		if (aURI.compareTo(uri)>0){break;}
	    }
	    tblp.nsTableModel.insertRow(i,v);
	    updatePropertyTabPrefix(uri,prefix);
	    return true;
	}
	else return false;
    }

    //remove namespace definition @ row n ONLY REMOVES the binding - keeping it with a prefix="" would be the same from the internal model point of view
    void removeNamespaceBinding(int n){
	String ns=(String)tblp.nsTableModel.getValueAt(n,1);
	tblp.nsTableModel.removeRow(n);
	if (tblp.nsTable.getRowCount()>n){tblp.nsTable.setRowSelectionInterval(n,n);}
	else if (tblp.nsTable.getRowCount()>0){
	    int i=tblp.nsTable.getRowCount()-1;
	    tblp.nsTable.setRowSelectionInterval(i,i);
	}
	String key;//display resources and properties using URI instead of prefix since the binding is being deleted
	IProperty p;
	for (Enumeration e=propertiesByURI.keys();e.hasMoreElements();){
	    key=(String)e.nextElement();
	    if (key.startsWith(ns)){
		for (Enumeration e2=((Vector)propertiesByURI.get(key)).elements();e2.hasMoreElements();){
		    p=(IProperty)e2.nextElement();
		    geomMngr.updateAPropertyText(p,p.getIdent());
		}
	    }
	}
	IResource r;
	for (Enumeration e=resourcesByURI.keys();e.hasMoreElements();){
	    key=(String)e.nextElement();
	    if (key.startsWith(ns)){
		r=(IResource)resourcesByURI.get(key);
		geomMngr.adjustResourceTextAndShape(r,r.getGraphLabel());
	    }
	}
	updatePropertyTabPrefix(ns,"");
    }

    /*given a URI, tries to replace the substring that's before the first ':' in the param string
      if the substring is a declared prefix, it will replace this substring (plus the ':' sign) with the corresponding namespace URI
      if the substring is not a declared prefix, it will return the URI as it was given
    */
    String tryToSolveBinding(String uri){
	if (uri.startsWith("http:") || uri.startsWith("ftp:") || uri.startsWith("mailto:")){
	    //if URI starts with one of the above strings, there is a very good chance that this is not
	    //a prefix binding, so do not even bother to try and find a binding involving this
	    return uri;
	}
	else {
	    int colonIndex=uri.indexOf(":");
	    if (colonIndex>0){
		String prefix=uri.substring(0,colonIndex);
		String namespace=getNSURIfromPrefix(prefix);
		if (namespace!=null && namespace.length()>0){
		    return namespace+uri.substring(prefix.length()+1,uri.length());  //+1 beause of the colon (:)
		}
		else return uri;		
	    }
	    else return uri;
	}
    }

    /*replaces full NS URIs by prefix in the Graph window when user has checked the corresponding box 
      in the NS definition table. This method should only be called after an RDFLoader.load() as it 
      makes the assumption that all URIs are full namespace URIs (i.e. no prefix)*/
    void updatePrefixBindingsInGraph(){
	if (tblp!=null){
	    String namespaceURI,prefix,key,s;
	    IProperty p;
	    IResource r;
	    for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
		if (((Boolean)tblp.nsTableModel.getValueAt(i,2)).booleanValue()){//if prefix should be displayed
		    namespaceURI=(String)tblp.nsTableModel.getValueAt(i,1);
		    prefix=(String)tblp.nsTableModel.getValueAt(i,0);
		    if (prefix!=null && prefix.length()>0){
			for (Enumeration e=propertiesByURI.keys();e.hasMoreElements();){
			    key=(String)e.nextElement();
			    if (key.startsWith(namespaceURI)){
				for (Enumeration e2=((Vector)propertiesByURI.get(key)).elements();e2.hasMoreElements();){
				    p=(IProperty)e2.nextElement();
				    s=p.getIdent();
				    geomMngr.updateAPropertyText(p,prefix+":"+s.substring(namespaceURI.length(),s.length()));
				}
			    }
			}
			for (Enumeration e=resourcesByURI.keys();e.hasMoreElements();){
			    key=(String)e.nextElement();
			    if (key.startsWith(namespaceURI)){
				r=(IResource)resourcesByURI.get(key);
				// display rdfs:label if exist ; else display resource identity
				if (DISP_AS_LABEL && r.getLabel() != null){
				    geomMngr.adjustResourceTextAndShape(r, r.getLabel());
				}
				else {
				    s = r.getGraphLabel();
				    geomMngr.adjustResourceTextAndShape(r,prefix+":"+s.substring(namespaceURI.length(),s.length()));
				}
			    }
			}
		    }
		}
		//else (i.e. if full namespace URI should be displayed) nothing to do as the graph is already in this state
	    }
	}
    }

    /*update the prefix or display status of a given namespace binding*/
    /*the URI of an NS binding cannot be edited in the table - users have to remove it
      from the list and add a new one
      addORupd tells whether this is a new entry or the update of an existing one
    */
    void updateNamespaceBinding(int nb,int whatCell,String prefix,String uri,Boolean display,int addORupd){
	if (whatCell==2){//if the display_as_prefix cell has changed w.r.t what is stored, update the graph
	    if (display.booleanValue() && prefix.length()>0){//show namespace as prefix
		String key;
		String s;
		IProperty p;
		for (Enumeration e=propertiesByURI.keys();e.hasMoreElements();){
		    key=(String)e.nextElement();
		    if (key.startsWith(uri)){
			for (Enumeration e2=((Vector)propertiesByURI.get(key)).elements();e2.hasMoreElements();){
			    p=(IProperty)e2.nextElement();
			    s=p.getIdent();
			    geomMngr.updateAPropertyText(p,prefix+":"+s.substring(uri.length()));
			}
		    }
		}
		IResource r;
		for (Enumeration e=resourcesByURI.keys();e.hasMoreElements();){
		    key=(String)e.nextElement();
		    if (key.startsWith(uri)){
			r=(IResource)resourcesByURI.get(key);
			s=r.getGraphLabel();
			if (s.length()>uri.length()){geomMngr.adjustResourceTextAndShape(r,prefix+":"+s.substring(uri.length(),s.length()));}
		    }
		}
	    }
	    else {//show namespace as URI
		String key;
		IProperty p;
		for (Enumeration e=propertiesByURI.keys();e.hasMoreElements();){
		    key=(String)e.nextElement();
		    if (key.startsWith(uri)){
			for (Enumeration e2=((Vector)propertiesByURI.get(key)).elements();e2.hasMoreElements();){
			    p=(IProperty)e2.nextElement();
			    geomMngr.updateAPropertyText(p,p.getIdent());
			}
		    }
		}
		IResource r;
		for (Enumeration e=resourcesByURI.keys();e.hasMoreElements();){
		    key=(String)e.nextElement();
		    if (key.startsWith(uri)){
			r=(IResource)resourcesByURI.get(key);
			geomMngr.adjustResourceTextAndShape(r,r.getGraphLabel());
		    }
		}
	    }
	}
	else if (whatCell==0){//the prefix field has been edited
	    if (prefix.length()>0){//assign a new prefix for this namespace
		if (display.booleanValue()){//if displaying as prefix and if prefix is not null, update the graph
		    IProperty p;
		    String key;
		    for (Enumeration e=propertiesByURI.keys();e.hasMoreElements();){
			key=(String)e.nextElement();
			if (key.startsWith(uri)){
			    for (Enumeration e2=((Vector)propertiesByURI.get(key)).elements();e2.hasMoreElements();){
				p=(IProperty)e2.nextElement();
				geomMngr.updateAPropertyText(p,prefix+":"+p.getLocalname());
			    }
			}
		    }
		    IResource r;
		    String s;
		    for (Enumeration e=resourcesByURI.keys();e.hasMoreElements();){
			key=(String)e.nextElement();
			if (key.startsWith(uri)){
			    r=(IResource)resourcesByURI.get(key);
			    s=r.getGraphLabel();
			    if (s.length()>uri.length()){geomMngr.adjustResourceTextAndShape(r,prefix+":"+s.substring(uri.length(),s.length()));}
			}
		    }
		}
		updatePropertyTabPrefix(uri,prefix);
	    }
	    else {// if prefix has become null, revert back to namespace URI
		String key;
		IProperty p;
		for (Enumeration e=propertiesByURI.keys();e.hasMoreElements();){
		    key=(String)e.nextElement();
		    if (key.startsWith(uri)){
			for (Enumeration e2=((Vector)propertiesByURI.get(key)).elements();e2.hasMoreElements();){
			    p=(IProperty)e2.nextElement();
			    geomMngr.updateAPropertyText(p,p.getIdent());
			}
		    }
		}
		IResource r;
		for (Enumeration e=resourcesByURI.keys();e.hasMoreElements();){
		    key=(String)e.nextElement();
		    if (key.startsWith(uri)){
			r=(IResource)resourcesByURI.get(key);
			geomMngr.adjustResourceTextAndShape(r,r.getGraphLabel());
		    }
		}
		updatePropertyTabPrefix(uri,prefix);
	    }
	    schemaMngr.updateNamespaces();
	}
	//whatCell can also be equal to -1 when the entire row has changed - e.g. when adding a new NS - do not need to deal with it here
	//whatCell can also be equal to 1 when modifying the namespace URI - this should be prevented by the fact that column 1 is not editable in NSTableModel
    }


    //providingURI=true if s is the URI, false if it is the prefix
    //returns false if no binding is defined
    boolean showThisNSAsPrefix(String s,boolean providingURI){
	for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
	    if ((((String)tblp.nsTableModel.getValueAt(i,1)).equals(s) && (providingURI)) || (((String)tblp.nsTableModel.getValueAt(i,0)).equals(s) && (!providingURI))){
		return ((Boolean)tblp.nsTableModel.getValueAt(i,2)).booleanValue();
	    }
	}
	return false;
    }

    //returns the prefix binded to this uri if defined (null otherwise)
    public static String getNSBinding(String uri){
	for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
	    if (((String)tblp.nsTableModel.getValueAt(i,1)).equals(uri) && ((String)tblp.nsTableModel.getValueAt(i,0)).length()>0){
		return (String)tblp.nsTableModel.getValueAt(i,0);
	    }
	}
	return null;
    }

    //returns the prefix binded to the namespace part of this uri if defined (null otherwise)
    public static String[] getNSBindingFromFullURI(String uri){
	String[] res=null;
	String tmpURI=null;
	String tmpPrefix=null;
	String longestnsURI="";
	String prefix=null;
	String dispPrefix="F";   //F or T
	for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
	    tmpPrefix=(String)tblp.nsTableModel.getValueAt(i,0);
	    tmpURI=(String)tblp.nsTableModel.getValueAt(i,1);
	    if (uri.startsWith(tmpURI)){
		//weird attempt at solving potential conflicts by selecting the longest namespace URI in case several of them 
		if (tmpURI.length()>longestnsURI.length()){
		    prefix=tmpPrefix;
		    longestnsURI=tmpURI;
		    dispPrefix=(((Boolean)tblp.nsTableModel.getValueAt(i,2)).booleanValue()) ? "T" : "F" ;
		}
	    }
	}
	if (prefix!=null && prefix.length()>0){
	    res=new String[3];
	    res[0]=prefix;
	    res[1]=longestnsURI;
	    res[2]=dispPrefix;
	    return res;
	}
	else return null;
    }


    //returns the URI binded to this prefix if defined (null otherwise)
    String getNSURIfromPrefix(String prefix){
	for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
	    if (((String)tblp.nsTableModel.getValueAt(i,0)).equals(prefix) && ((String)tblp.nsTableModel.getValueAt(i,1)).length()>0){
		return (String)tblp.nsTableModel.getValueAt(i,1);
	    }
	}
	return null;
    }

    //returns true if pr is already binded to a namespace
    boolean prefixAlreadyInUse(String pr){
	if (pr.length()>0){
	    for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
		if (((String)tblp.nsTableModel.getValueAt(i,0)).equals(pr)){return true;}
	    }
// 	    if (pr.equals(ANON_NODE.substring(0,ANON_NODE.length()-1)) || pr.equals(BASE_URI.substring(0,BASE_URI.length()))){return true;}
	    if (pr.equals(ANON_NODE.substring(0,ANON_NODE.length()-1))){return true;}
	    return false;
	}
	else return false;
    }

    //reconfigure text of some nodes (typed literals for now)
    String processNodeTextForSB(INode n){
	String res=n.getText();
	if (n instanceof ILiteral){
	    ILiteral l=(ILiteral)n;
	    if (l.getDatatype()!=null){
		String dturi=l.getDatatype().getURI();
		String[] pref=Editor.getNSBindingFromFullURI(dturi);
		if (pref!=null && pref[2].equals("T")){
		    dturi=pref[0]+":"+dturi.substring(pref[1].length(),dturi.length());
		}
		res="["+dturi+"]   "+res;
	    }
	}
	return res;
    }

    /*add a new property type constructor to the table, that can be selected to add a new property instance 
      of this type to the graph
    */
    boolean addPropertyType(String ns,String ln,boolean silent){
	boolean propertyAlreadyExists=false;
	String namespace="";
	if (ns.length()>0 && ln.length()>0){//only add complete properties
	    if (ns.charAt(ns.length()-1)==':'){//if cell ends with ':', the user has entered 
		//the prefix instead of the full URI, check that it is an existing prefix, 
		//get its URI, and then add it to the list
		String prefix=ns.substring(0,ns.length()-1);
		for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
		    if (((String)tblp.nsTableModel.getValueAt(i,0)).equals(prefix)){
			namespace=(String)tblp.nsTableModel.getValueAt(i,1);
			break;
		    }
		}
		if (namespace.length()==0){
		    if (!silent){
			Editor.vsm.getView(Editor.mainView).setStatusBarText("Prefix "+ns+" is not binded to any namespace. Treating input as a full namespace URI.");
		    }
		    namespace=ns;
		}
	    }
	    else {
		namespace=ns;
	    }
	    DefaultTableModel tm=(DefaultTableModel)tblp.prTable.getModel();
	    for (int i=0;i<tm.getRowCount();i++){//check that this property is not already defined
		if (((String)tm.getValueAt(i,2)).equals(ln) && ((String)tm.getValueAt(i,0)).equals(namespace)){
		    propertyAlreadyExists=true;
		    if (!silent){JOptionPane.showMessageDialog(tblp,"Property "+namespace+ln+" is already defined.");}
		    return false;
		}
	    }
	    if (!propertyAlreadyExists){
		String prefix=getNSBinding(namespace);
		Vector data=new Vector();data.add(namespace);data.add(prefix==null?"":prefix);data.add(ln);//null as 2nd element
		String aProperty;              //is for the prefix column
		int i;
		if (Utils.isMembershipProperty(ln)){//adding a membership property type to the table
		    //(e.g. _1, _2 ,...). Adding it will automatically select it. We do not want that in this specific case
		    //Furthermore, the special properties should be inserted at the end of the table (not very interesting)
		    //but the way isaviz works for now, they need to be added to the table since the table is the main 
		    //and only in-memory internal representation of available property types
		    //We also do that for another reason: so that _X do not appear first in combo boxes of propspanel (when changing the property type of an iproperty). First because they are not very interesting, second because this has a nasty side effect: it automatically assigned _1 to the iproperty, which altered the automatic numbering if then selected as the type of this property (since the property was _1, the generator thought that the next one was _2 whereas there was actually no real _1 for this resource)
		    int lastMembershipPropIndex=tm.getRowCount();
		    String aLN;
		    for (int j=tm.getRowCount()-1;j>0;j--){//find the first table row containing a membership property type
			aLN=(String)tm.getValueAt(j,2);
			if (!(((String)tm.getValueAt(j,0)).equals(RDFMS_NAMESPACE_URI) && Utils.isMembershipProperty(aLN))){lastMembershipPropIndex=j+1;break;}
		    }
		    for (i=lastMembershipPropIndex;i<tm.getRowCount();i++){//find where to insert the new entry in the table, beginning sorting at the first row containing a membership property (end of table if none)
			aProperty=((String)tm.getValueAt(i,0)).concat((String)tm.getValueAt(i,2));
			if (aProperty.compareTo(namespace+ln)>0){break;}//(sorted lexicographically)
		    }
		    tblp.prTable.clearSelection();
		    selectedPropertyConstructorNS=RDFMS_NAMESPACE_URI;
		    selectedPropertyConstructorLN=MEMBERSHIP_PROP_CONSTRUCTOR;
		}
		else {
		    for (i=0;i<tm.getRowCount();i++){//find where to insert the new entry in the table
			aProperty=((String)tm.getValueAt(i,0)).concat((String)tm.getValueAt(i,2));
			//(sorted lexicographically, but before the membership properties (_X))
			if (aProperty.compareTo(namespace+ln)>0 || Utils.isMembershipProperty((String)tm.getValueAt(i,2))){break;}
		    }
		}
		tm.insertRow(i,data);
		return true;
	    }
	    else return false;
	}
	else return false;
    }

    //remove property constructor @ row n ONLY REMOVES the constructor from the table - does not delete any predicate pr definition in propertiesByURI
    void removePropertyConstructor(int n){
	boolean used=false;  //if the property type ised used at least once in the model,
	DefaultTableModel tm=(DefaultTableModel)tblp.prTable.getModel();
	String uri=(String)tm.getValueAt(n,0)+(String)tm.getValueAt(n,2);
	for (Enumeration e=propertiesByURI.keys();e.hasMoreElements();){
	    if (((String)e.nextElement()).equals(uri)){used=true;break;}
	}
	if (used){//prompt a warning dialog before removing it
	    Object[] options={"Yes","No"};
	    int option=JOptionPane.showOptionDialog(null,Messages.removePropType,"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
	    if (option==JOptionPane.OK_OPTION){
		tm.removeRow(n);
		if (tblp.prTable.getRowCount()>n){tblp.prTable.setRowSelectionInterval(n,n);}
		else if (tblp.prTable.getRowCount()>0){
		    int i=tblp.prTable.getRowCount()-1;
		    tblp.prTable.setRowSelectionInterval(i,i);
		}
	    }
	}
	else {
	    tm.removeRow(n);
	    if (tblp.prTable.getRowCount()>n){tblp.prTable.setRowSelectionInterval(n,n);}
	    else if (tblp.prTable.getRowCount()>0){
		int i=tblp.prTable.getRowCount()-1;
		tblp.prTable.setRowSelectionInterval(i,i);
	    }
	}
    }

    //called when something changes in the NS binding table - updates the property type table's prefix column
    void updatePropertyTabPrefix(String ns,String prefix){
	DefaultTableModel tm=(DefaultTableModel)tblp.prTable.getModel();
	for (int i=0;i<tm.getRowCount();i++){
	    if (((String)tm.getValueAt(i,0)).equals(ns)){tm.setValueAt(prefix,i,1);}
	}
    }

    //add the properties defined in RDF Model and Syntax Spec to the table of property constructors (they might be used often, so we offer them by default)
    void initRDFMSProperties(){
	//addPropertyType(Editor.RDFMS_NAMESPACE_URI,"li",true);  //remove rdf:li as it seems to no longer exist in RDF/XML
	addPropertyType(Editor.RDFMS_NAMESPACE_URI,MEMBERSHIP_PROP_CONSTRUCTOR,true);
	addPropertyType(Editor.RDFMS_NAMESPACE_URI,"object",true);
	addPropertyType(Editor.RDFMS_NAMESPACE_URI,"predicate",true);
	addPropertyType(Editor.RDFMS_NAMESPACE_URI,"subject",true);
	addPropertyType(Editor.RDFMS_NAMESPACE_URI,"type",true);
	addPropertyType(Editor.RDFMS_NAMESPACE_URI,"value",true);
    }

    //add the properties defined in RDF Schema Spec to the table of property constructors (they might be used often, so we offer them by default)
    void initRDFSProperties(){
	addPropertyType(Editor.RDFS_NAMESPACE_URI,"comment",true);
	addPropertyType(Editor.RDFS_NAMESPACE_URI,"domain",true);
	addPropertyType(Editor.RDFS_NAMESPACE_URI,"isDefinedBy",true);
	addPropertyType(Editor.RDFS_NAMESPACE_URI,"label",true);
	addPropertyType(Editor.RDFS_NAMESPACE_URI,"range",true);
	addPropertyType(Editor.RDFS_NAMESPACE_URI,"seeAlso",true);
	addPropertyType(Editor.RDFS_NAMESPACE_URI,"subClassOf",true);
	addPropertyType(Editor.RDFS_NAMESPACE_URI,"subPropertyOf",true);
    }

    //holds the type of property to create when the user creates a new predicate
    void setSelectedPropertyConstructor(String namespace,String localname){
	selectedPropertyConstructorNS=namespace;
	selectedPropertyConstructorLN=localname;
    }

    //returns a list of strings representing all namespaces used in properties (one copy for each)
    Vector getAllPropertyNS(){// (used by the combo boxes in PropsPanel)
	Vector res=new Vector();
	javax.swing.table.TableModel tm=tblp.prTable.getModel();
	String ns;
	String prefix;
	for (int i=0;i<tm.getRowCount();i++){
	    ns=(String)tm.getValueAt(i,0);
	    if ((prefix=getNSBinding(ns))!=null){ns=prefix;} //replace namespace URI by prefix if defined
	    if (!res.contains(ns)){res.add(ns);}
	}
	return res;
    }

    //returns all property names defined in a given namespace (used by the combo boxes in PropsPanel)
    Vector getProperties4NS(String ns){
	Vector res=new Vector();
	javax.swing.table.TableModel tm=tblp.prTable.getModel();
	String prefix;
	for (int i=0;i<tm.getRowCount();i++){
	    if (((String)tm.getValueAt(i,0)).equals(ns) || (((prefix=getNSBinding((String)tm.getValueAt(i,0)))!=null) &&  prefix.equals(ns))){//ns might be the namespace's uri or the prefix binded to this namespace (depending on whether a binding has been defined or not)
		res.add(tm.getValueAt(i,2));
	    }
	}
	return res;
    }

    /**set prefix used in anonymous node (default is "genid:")*/
    public void setAnonymousNodePrefix(String s){ANON_NODE=s;}

    /**get prefix used in anonymous node (default is "genid:")*/
    public String getAnonymousNodePrefix(){return ANON_NODE;}

    /*show/hide the window containing the NS binding and Property types tables*/
    void showTablePanel(boolean b){
	ConfigManager.showNSWindow=b;
	if (ConfigManager.showNSWindow){tblp.setVisible(true);}
	else {tblp.setVisible(false);}
    }

    /*show/hide the window displaying a node/edge attributes (which can be edited)*/
    void showPropsPanel(boolean b){
	ConfigManager.showEditWindow=b;
	if (ConfigManager.showEditWindow){propsp.setVisible(true);}
	else {propsp.setVisible(false);}
    }

    /*show/hide the window displaying navigation buttons (zoom and translation)*/
    void showNavPanel(boolean b){
	ConfigManager.showNavWindow=b;
	if (ConfigManager.showNavWindow){navp.setVisible(true);}
	else {navp.setVisible(false);}
    }

    /*global view*/
    public void getGlobalView(){
	rememberLocation(Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0).getLocation());
	vsm.getGlobalView(Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0),ConfigManager.ANIM_DURATION);
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    public void getHigherView(){
	Camera c=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_DURATION,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    public void getLowerView(){
	Camera c=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_DURATION,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*direction should be one of Editor.MOVE_* */
    public void translateView(short direction){
	Camera c=Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0);
	rememberLocation(c.getLocation());
	LongPoint trans;
	long qt1,qt2;
	long[] rb=mView.getVisibleRegion(c);
	if (direction==MOVE_UP){
	    qt2=Math.round((rb[1]-rb[3])/2.4);
	    trans=new LongPoint(0,qt2);
	}
	else if (direction==MOVE_DOWN){
	    qt2=Math.round((rb[3]-rb[1])/2.4);
	    trans=new LongPoint(0,qt2);
	}
	else if (direction==MOVE_RIGHT){
	    qt1=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt1,0);
	}
	else if (direction==MOVE_LEFT){
	    qt1=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt1,0);
	}
	else if (direction==MOVE_UP_LEFT){
	    qt1=Math.round((rb[0]-rb[2])/2.4);
	    qt2=Math.round((rb[1]-rb[3])/2.4);
	    trans=new LongPoint(qt1,qt2);
	}
	else if (direction==MOVE_UP_RIGHT){
	    qt1=Math.round((rb[2]-rb[0])/2.4);
	    qt2=Math.round((rb[1]-rb[3])/2.4);
	    trans=new LongPoint(qt1,qt2);
	}
	else if (direction==MOVE_DOWN_RIGHT){
	    qt1=Math.round((rb[2]-rb[0])/2.4);
	    qt2=Math.round((rb[3]-rb[1])/2.4);
	    trans=new LongPoint(qt1,qt2);
	}
	else {//DOWN_LEFT
	    qt1=Math.round((rb[0]-rb[2])/2.4);
	    qt2=Math.round((rb[3]-rb[1])/2.4);
	    trans=new LongPoint(qt1,qt2);
	}
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_DURATION,AnimManager.CA_TRANS_SIG,trans,c.getID());
    }

    /*show/hide radar view*/
    void showRadarView(boolean b){
	if (b){
	    if (Editor.rView==null){
		Vector cameras=new Vector();
		cameras.add(mSpace.getCamera(1));
		cameras.add(rSpace.getCamera(0));
		vsm.addExternalView(cameras, Editor.radarView, View.STD_VIEW, ConfigManager.rdW, ConfigManager.rdH, false, true);
		reh=new RadarEvtHdlr(this);
		Editor.rView=vsm.getView(Editor.radarView);
		Editor.rView.setEventHandler(reh);
		Editor.rView.setResizable(false);
		Editor.rView.setActiveLayer(1);
		Editor.rView.setCursorIcon(java.awt.Cursor.MOVE_CURSOR);
		Editor.rView.setBackgroundColor(ConfigManager.bckgColor);
		Editor.rView.mouse.setColor(ConfigManager.cursorColor);
		vsm.getGlobalView(mSpace.getCamera(1),100);
		cameraMoved();
	    }
	    else {
		Editor.rView.toFront();
	    }
	}
    }

    /*implementation of the 'back' button (remember the current camera location before moving)*/
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

    /*implementation of the 'back' button (go back to previous camera location)*/
    void moveBack(){
	if (previousLocations.size()>0){
	    Location newlc=(Location)previousLocations.lastElement();
	    Location currentlc=vsm.getActiveCamera().getLocation();
	    Vector animParams=Location.getDifference(currentlc,newlc);
	    vsm.animator.createCameraAnimation(ConfigManager.ANIM_DURATION,AnimManager.CA_ALT_TRANS_SIG,animParams,vsm.getActiveCamera().getID());
	    previousLocations.removeElementAt(previousLocations.size()-1);
	}
    }

    public void cameraMoved(){//interface AnimationListener (com.xerox.VTM.engine)
	if (Editor.rView!=null){
	    Camera c0=mSpace.getCamera(1);
	    Camera c1=rSpace.getCamera(0);
	    c1.posx=c0.posx;
	    c1.posy=c0.posy;
	    c1.focal=c0.focal;
	    c1.altitude=c0.altitude;
	    long[] wnes=Editor.mView.getVisibleRegion(mSpace.getCamera(0));
	    observedRegion.moveTo((wnes[0]+wnes[2])/2,(wnes[3]+wnes[1])/2);
	    //observedRegion.vy=;
	    observedRegion.setWidth((wnes[2]-wnes[0])/2);
	    observedRegion.setHeight((wnes[1]-wnes[3])/2);
	    //c1.repaintNow();
	    vsm.repaintNow();
	}
    }

    void updateMainViewFromRadar(){
	Camera c0=mSpace.getCamera(0);
	c0.posx=observedRegion.vx;
	c0.posy=observedRegion.vy;
	vsm.repaintNow();
    }

    public void centerRadarView(){
	if (Editor.rView!=null){
	    vsm.getGlobalView(Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(1),ConfigManager.ANIM_DURATION);
	    cameraMoved();
	}
    }

    /*set the maximum number of chars of a literal's value displayed (updates the graph with the new value)*/
    void setMaxLiteralCharCount(int max){
	if (max!=MAX_LIT_CHAR_COUNT){
	    MAX_LIT_CHAR_COUNT=max;
	    ILiteral l;
	    String value;
	    for (int i=0;i<literals.size();i++){
		l=(ILiteral)literals.elementAt(i);
		if (l.getGlyphText() != null){
		    String displayedValue=((l.getValue().length()>=MAX_LIT_CHAR_COUNT) ? l.getValue().substring(0,MAX_LIT_CHAR_COUNT)+" ..." : l.getValue());
		    l.getGlyphText().setText(displayedValue);
		    geomMngr.correctLiteralTextAndShape(l);
		}
	    }
	    if (DISP_AS_LABEL){
		IResource r;
		for (Enumeration e=resourcesByURI.elements();e.hasMoreElements();){
		    r = (IResource)e.nextElement();
		    value = r.getRDFSLabel();
		    if (value != null){
			r.setLabel((value.length()>=MAX_LIT_CHAR_COUNT) ? value.substring(0,MAX_LIT_CHAR_COUNT)+" ..." : value);
			if (r.getGlyphText() != null){
			    geomMngr.adjustResourceTextAndShape(r, r.getLabel());
			}
		    }
		}
	    }
	}
    }

    /*changing tool in icon palette*/
    void setMode(int i){
	if (i!=EditorEvtHdlr.MOVE_RESIZE_MODE){geomMngr.destroyLastResizer();}  //get rid of resizer object when changing mode
	eeh.mode=i;
    }

    void updatePropertyBrowser(INode n){
	if (ConfigManager.showNSWindow && tblp.tabbedPane.getSelectedIndex()==2){//only update if visible (increases performances)
	    tblp.updatePropertyBrowser(n,true);
	}
    }

    //given a string, centers on a VText with this string in it
    void quickSearch(String s){//if firstTime=true, the list of VText is reinitialized ; if false, go to the next one in the list
	if (s.length()>0){
	    if (!s.toLowerCase().equals(lastSearchedString)){//searching a new string - reinitialize everything
		lastSearchedString=s.toLowerCase();
		searchIndex=-1;
		matchingList.removeAllElements();
		Glyph[] gl=vsm.getVirtualSpace(mainVirtualSpace).getVisibleGlyphList();
		for (int i=0;i<gl.length;i++){
		    if (gl[i] instanceof VText && (((VText)gl[i]).getText()!=null) && (((VText)gl[i]).getText().toLowerCase().indexOf(lastSearchedString)!=-1)){matchingList.add(gl[i]);}
		}
	    }
	    if (matchingList.size()>0){
		if (searchIndex<matchingList.size()-1){//get next entry in the list of matching elements
		    searchIndex++;
		}
		else {//go back to first one if necessary (loop)
		    vsm.getActiveView().setStatusBarText("Reached end of list, going back to the beginning");
		    searchIndex=0;
		}
		//center on the entity
		Glyph g=(Glyph)matchingList.elementAt(searchIndex);
		if (lastMatchingEntity!=null){resetINodeColors(lastMatchingEntity);}
		lastMatchingEntity=(INode)g.getOwner();
		g.setHSVColor(ConfigManager.srhTh,ConfigManager.srhTs,ConfigManager.srhTv);
		if (lastMatchingEntity instanceof IResource || lastMatchingEntity instanceof ILiteral){
		    /*when text belongs to a node, center on the node itself rather than the text
		     in part to workaround a bug in ZVTM centerOnGlyph when applied to VText (altitude can be wrong)*/
		    vsm.centerOnGlyph(lastMatchingEntity.getGlyph(),vsm.getVirtualSpace(mainVirtualSpace).getCamera(0),400);
		}
		else {
		    vsm.centerOnGlyph(g,vsm.getVirtualSpace(mainVirtualSpace).getCamera(0),400);
		}
	    }
	}
    }

    //reset the search variables after it is finished
    void resetQuickSearch(){
	searchIndex=-1;
	lastSearchedString="";
	matchingList.removeAllElements();
	if (lastMatchingEntity!=null){
	    resetINodeColors(lastMatchingEntity);
	    lastMatchingEntity=null;
	}
    }

    //reset the colors of an INode, taking into accout its state (selected, commented, normal)
    void resetINodeColors(INode n){
	if (lastMatchingEntity.isSelected()){
	    lastMatchingEntity.getGlyphText().setHSVColor(ConfigManager.selTh,ConfigManager.selTs,ConfigManager.selTv);
	}
	else if (lastMatchingEntity.isCommented()){
	    lastMatchingEntity.getGlyphText().setHSVColor(ConfigManager.comTh,ConfigManager.comTs,ConfigManager.comTv);
	}
	else {
	    if (lastMatchingEntity instanceof IResource){
		lastMatchingEntity.getGlyphText().setHSVColor(ConfigManager.resTBh,ConfigManager.resTBs,ConfigManager.resTBv);
	    }
	    else if (lastMatchingEntity instanceof IProperty){
		    lastMatchingEntity.getGlyphText().setHSVColor(ConfigManager.prpTh,ConfigManager.prpTs,ConfigManager.prpTv);
	    }
	    else {//necessarily instanceof ILiteral
		lastMatchingEntity.getGlyphText().setHSVColor(ConfigManager.litTBh,ConfigManager.litTBs,ConfigManager.litTBv);
	    }
	}
    }

    /*cut selected node(s)*/
    void cutSelection(){
	if (lastSelectedItem!=null){
	    propsp.reset();
	    resetCopied(); //clean clipboard
	    ISVCut cmd=new ISVCut(this,selectedPredicates,selectedResources,selectedLiterals);
	    cmd._do();
	    addCmdToUndoStack(cmd);
	    cmp.enablePaste(true);
	}
    }

    /*copy selected node(s)*/
    void copySelection(){
	if (lastSelectedItem!=null){
	    resetCopied(); //clean clipboard
	    ISVCopy cmd=new ISVCopy(this,selectedPredicates,selectedResources,selectedLiterals);
	    cmd._do();
// 	    addCmdToUndoStack(cmd); //no undo for copy, so it is not added to the stack
	    cmp.enablePaste(true);
	}
    }

    /*paste selected node(s)*/
    void pasteSelection(long x,long y){//x,y are the coords (of set's geom center) where the copies should be created
	ISVPaste cmd=new ISVPaste(this,copiedPredicates,copiedResources,copiedLiterals,x,y);
	cmd._do();
	addCmdToUndoStack(cmd);
	centerRadarView();
    }

    //delete everything that is selected, beginning by edges and then nodes
    //this is the only gate to deletion from the GUI - (so that UNDO works correctly)
    void deleteSelectedEntities(){
	if (lastSelectedItem!=null){
	    propsp.reset();
	    /*also select predicates that would anyway get deleted
	      by the deletion of their subject/object so that they 
	      get are properly restored if the operation is undone*/
	    Vector v;
	    IProperty p;
	    Enumeration e;
	    for (int i=0;i<selectedResources.size();i++){
		if ((v=((IResource)selectedResources.elementAt(i)).getIncomingPredicates())!=null){
		    for (e=v.elements();e.hasMoreElements();){
			p=(IProperty)e.nextElement();
			if (!p.isSelected()){selectPredicate(p,true);}
		    }
		}
		if ((v=((IResource)selectedResources.elementAt(i)).getOutgoingPredicates())!=null){
		    for (e=v.elements();e.hasMoreElements();){
			p=(IProperty)e.nextElement();
			if (!p.isSelected()){selectPredicate(p,true);}
		    }
		}
	    }
	    for (int i=0;i<selectedLiterals.size();i++){
		if ((p=((ILiteral)selectedLiterals.elementAt(i)).getIncomingPredicate())!=null && (!p.isSelected())){
		    selectPredicate(p,true);
		}
	    }
 	    ISVDelete cmd=new ISVDelete(this,selectedPredicates,selectedResources,selectedLiterals);
	    cmd._do();
	    addCmdToUndoStack(cmd);
	    centerRadarView();
	}
    }

    /*undo last operation and update stack*/
    void undo(){
	if (undoIndex>=0){
	    undoStack[undoIndex]._undo();
	    undoStack[undoIndex]=null;
	    undoIndex--;
	    if (undoIndex<0){undoIndex=-1;cmp.enableUndo(false);}
	}
    }

    /*add an undoable command to undo stack*/
    void addCmdToUndoStack(ISVCommand c){
	int index=Utils.getFirstEmptyIndex(undoStack);
	if (index==-1){
	    Utils.eraseFirstAddNewElem(undoStack,c);
	    undoIndex=undoStack.length-1;
	}
	else {
	    undoStack[index]=c;
	    undoIndex=index;
	}
	cmp.enableUndo(true);
    }


    /*serializes the model to a stringbuffer and displays it in TextViewer*/
    void displayRawRDFXMLFile(){
	try {
	    if (rdfLdr==null){rdfLdr=new RDFLoader(this);}
	    rdfLdr.generateJenaModel(); //this actually builds the Jena model from our internal representation
	    StringBuffer sb=rdfLdr.serialize(rdfModel);
	    TextViewer v=new TextViewer(sb,"Raw RDF/XML Viewer",0,false);
	}
	catch (Exception ex){System.err.println("Error: Editor.displayRawFile: "+ex);ex.printStackTrace();}
    }

    /*call graphviz to relayout current model*/
    /*right now this is a very dumb version that goes through the whole process of export/import.
      In the future, I will write a version that bypasses the RDF export/import phase. It will 
      generate directly the DOT file with unique IDs, call graphviz and get the SVG, linking
      it back to existing entities (don;t go through the whole process of generating the model bla bla bla...)
    */
    /*since we are performing a reset, this also means that UNDO/PASTE from earlier operations are not available any longer - this will be fixed when we write the above mentioned code*/
    void reLayoutGraph(){
	Object[] options={"Yes","No"};
	int option=JOptionPane.showOptionDialog(null,Messages.reLayoutWarning,"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
	if (option==JOptionPane.OK_OPTION){
	    File tmpRdf=Utils.createTempFile(m_TmpDir.toString(),"tmp",".rdf");
	    exportRDF(tmpRdf,false);
	    //no need to reset prior to load as loadRDF() does a reset
	    loadRDF(tmpRdf,RDFLoader.RDF_XML_READER,false);
	    if (dltOnExit){tmpRdf.deleteOnExit();}
	}
    }

    /*open a window and display all properties for which r is the subject*/
    void displayResOutgoingPredicates(IResource r){
	new PropertySummary(r,this);
    }

    /*opens a window and displays error messages*/
    void showErrorMessages(){
	if (errorLog!=null){
	    if (errorLog.isShowing()){
		errorLog.toFront();
	    }
	    else {//if not, it has probably been closed and disposed - errorLog still points to it, but it is no longer on screen
		errorLog=new TextViewer(errorMessages,"Error log",1000,true);
		vsm.getView(mainView).setStatusBarText("");
	    }
	}
	else {
	    errorLog=new TextViewer(errorMessages,"Error log",1000,true);
	    vsm.getView(mainView).setStatusBarText("");
	}
    }
    
    /*opens a print dialog box*/
    void printRequest(){
	java.awt.image.BufferedImage bi=vsm.getView(mainView).getImage();
	if (bi!=null){
	    PrintUtilities pu=new PrintUtilities(bi);
	    pu.print();
// 	    java.awt.print.PrinterJob pj=java.awt.print.PrinterJob.getPrinterJob();
// 	    pj.setPrintable(new PrintableImage(bi));
// 	    if (pj.printDialog()){
// 		try {pj.print();}
// 		catch (Exception ex){ex.printStackTrace();}
// 	    }
	}
    }

    /*opens a window and dislays information about the project (file name, number of resources, etc)*/
    void showPrjSummary(){
	int nbProps=0;
	for (Enumeration e1=propertiesByURI.elements();e1.hasMoreElements();){
	    for (Enumeration e2=((Vector)e1.nextElement()).elements();e2.hasMoreElements();){
		e2.nextElement();
		nbProps++;
	    }
	}
	String prjF=projectFile==null ? "" : projectFile.toString();
	InfoPanel pi=new InfoPanel(this,prjF,lastRDF,resourcesByURI.size(),literals.size(),nbProps);
// 	JOptionPane.showMessageDialog(cmp,
// 						  "Project File: "+prjF+"\n"+
// 						  "Number of resources: "+resourcesByURI.size()+"\n"+
// 						  "Number of literals: "+literals.size()+"\n"+
// 						  "Number of statements: "+nbProps);
    }

    //open up the default or user-specified browser (netscape, ie,...) and try to display the content at the resource's URI
    void displayURLinBrowser(IResource r){
	if (!r.isAnon()){
	    displayURLinBrowser(r.getIdentity());
	}
	else vsm.getActiveView().setStatusBarText("Anonymous resources do not have a URI");
    }

    //open up the default or user-specified browser (netscape, ie,...) and try to display the content uri
    void displayURLinBrowser(String uri){
	if (webBrowser==null){webBrowser=new WebBrowser();}
	webBrowser.show(uri);
    }

    /*tells whether all views should be repainted, even if not active*/
    void alwaysUpdateViews(boolean b){
	vsm.setRepaintPolicy(b);
    }

    /*antialias ON/OFF for views*/
    void setAntialiasing(boolean b){
	ANTIALIASING=b;
	vsm.getView(mainView).setAntialiasing(ANTIALIASING);
    }

//     void clearBitmapCache(){
// 	if (gssMngr!=null){gssMngr.clearBitmapCache();}
//     }

    /*save user preferences*/
    void saveConfig(){cfgMngr.saveConfig();}

    static void collectGarbage(){
	System.gc();
    }

    /*exit from IsaViz, save bookmarks*/
    public void exit(){
	cfgMngr.saveURLs();
	System.exit(0);
    }

    //debug
    void summary(){ 
	System.out.println("Resources "+resourcesByURI.size());
	System.out.println("Literals "+literals.size());
	int i=0;
	for (Enumeration e1=propertiesByURI.elements();e1.hasMoreElements();){
	    for (Enumeration e2=((Vector)e1.nextElement()).elements();e2.hasMoreElements();){
		e2.nextElement();
		i++;
	    }
	}
	System.out.println("Properties "+i);
    }

    //debug
    void nsBindings(){
	System.out.println("Namespace bindings");
	for (int i=0;i<tblp.nsTableModel.getRowCount();i++){
	    System.out.println("p="+tblp.nsTableModel.getValueAt(i,0)+" uri="+tblp.nsTableModel.getValueAt(i,1)+" disp="+tblp.nsTableModel.getValueAt(i,2));
	}
    }

    //debug
    void printClipboard(){
	System.out.println("Clipboard");
	System.out.print("[");
	if (!copiedResources.isEmpty()){
	    for (int i=0;i<copiedResources.size()-1;i++){System.out.print(copiedResources.elementAt(i).toString()+",");}
	    System.out.println(copiedResources.lastElement().toString()+"]");
	}
	if (!copiedLiterals.isEmpty()){
	    System.out.print("[");
	    for (int i=0;i<copiedLiterals.size()-1;i++){System.out.print(copiedLiterals.elementAt(i).toString()+",");}
	    System.out.println(copiedLiterals.lastElement().toString()+"]");
	}
	if (!copiedPredicates.isEmpty()){
	    System.out.print("[");
	    for (int i=0;i<copiedPredicates.size()-1;i++){System.out.print(copiedPredicates.elementAt(i).toString()+",");}
	    System.out.println(copiedPredicates.lastElement().toString()+"]");
	}
    }

    public static void commandLineHelp(){
	System.out.println("Usage : ");
	System.out.println("  java org.w3c.IsaView [options] [file_name.isv|file_name.rdf|file_name.nt|file_name.n3]");
	System.out.println("  Options:");
	System.out.println("          -gss  file_name.gss   loads a GSS stylesheet");
	System.exit(0);	
    }

//     private static void debugCharset(){
// 	java.util.Map availcs=java.nio.charset.Charset.availableCharsets();
// 	java.util.Set keys=availcs.keySet();
// 	for (java.util.Iterator iter=keys.iterator();iter.hasNext();){
// 	    System.out.println(iter.next());
// 	}
//     }

    //MAIN - update from the Sesame plug-in version
    public static void main(String[] args){
	if (args.length>3) {
	    commandLineHelp();
	}
	else if (args.length==3){//both a gss file and a file to parse specified
	    if (args[0].equals("-gss")){
		gssFile=args[1];
		argFile=args[2];
	    }
	    else if (args[1].equals("-gss")){
		gssFile=args[2];
		argFile=args[0];	
	    }
	    else {
		commandLineHelp();
	    }
	}
	else if (args.length==2){//just a gss file specified
	    if (args[0].equals("-gss")){
		gssFile=args[1];
	    }
	    else {
		commandLineHelp();
	    }
	}
	else if (args.length==1){//just a file to parse specified
	    argFile=args[0];
	}
// 	if (Utilities.osIsMacOS()){
// 	    System.setProperty("apple.laf.useScreenMenuBar", "true");
// 	}
	Editor appli=new Editor();
    }

}
