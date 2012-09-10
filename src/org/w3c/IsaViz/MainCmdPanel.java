/*   FILE: MainCmdPanel.java
 *   DATE OF CREATION:   10/18/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org) and Arjohn Kampman (arjohn.kampman@aidministrator.nl)
 *   MODIF:              Fri Sep 16 09:16:12 2005 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
// import java.io.FileReader;
// import java.io.InputStreamReader;
// import java.io.BufferedReader;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;
import java.util.Vector;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.engine.View;
import net.claribole.zvtm.engine.Location;

//Added by Arjohn:
import java.util.ArrayList;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/*The main command panel. Contains the icon palette + menus to load/save/access preferences/etc...*/

class MainCmdPanel extends JFrame implements ActionListener,ItemListener,KeyListener,FocusListener {

    Editor application;

    PrefWindow dp;

    JMenuBar mnb;
    JMenu fileMenu;
    JMenuItem resetMn;
    JMenuItem loadMn,saveMn,saveAsMn;
    JMenuItem ldLocRDFXMLMn,ldRemRDFXMLMn,ldLocNTrMn,ldRemNTrMn,ldLocN3Mn,ldRemN3Mn;
    JMenuItem mgLocRDFXMLMn,mgRemRDFXMLMn,mgLocNTrMn,mgRemNTrMn,mgLocN3Mn,mgRemN3Mn;
    JMenuItem summaryMn;
    JMenuItem exportRDFMn,exportNTriMn,exportN3Mn,exportPNGMn,exportSVGMn;
    JMenuItem printMn;
    JMenuItem exitMn;
 
    JMenu editMenu;
    JMenuItem undoMn,cutMn,copyMn,pasteMn;
    JMenuItem selectAllNMn,selectAllEMn,advSelectMn,unselAllMn;
    JMenuItem deleteMn;
    JMenuItem baseURIMn;
    JMenuItem prefMn;

    JMenu viewMenu;
    JMenuItem rawrdfMn,errorMn,getGlobVMn,zmotVMn,zminVMn,radVMn,layoutMn,backMn;
    JCheckBoxMenuItem showTablesMn,showPropsMn,showNavMn,showBkMn;//,lensMn;

    JMenu helpMenu;
    JMenuItem cmdMn,plgMn,aboutMn,isvumMn,gssumMn,gssedumMn;

    JFileChooser fc;

    JRadioButton singleSelectBt,regionNSelectBt,regionESelectBt,regionZoomBt;
    JButton undoBt,cutBt,copyBt;
    JRadioButton createResBt,createPropBt,createLitBt;
    JRadioButton resizeBt,pasteBt;
    JRadioButton commSingleBt,commRegionBt,uncommSingleBt,uncommRegionBt;
    JTextField searchTf;
    JButton searchBt;

    ImportPlugin[] importPlugins;
    ExportPlugin[] exportPlugins;

    JMenuItem[] pluginLdMenuItems;
    JMenuItem[] pluginMgMenuItems;
    JMenuItem[] pluginExportMenuItems;

    MainCmdPanel(Editor appli,int x,int y,int width, int height){
	initPlugins();
	application=appli;
	//MENUS
	mnb=new JMenuBar();
	this.setJMenuBar(mnb);
	fileMenu=new JMenu("File");
	fileMenu.setMnemonic(KeyEvent.VK_F);
	mnb.add(fileMenu);
	resetMn = new JMenuItem("New Project");
	resetMn.addActionListener(this);
	resetMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
	fileMenu.add(resetMn);
	loadMn = new JMenuItem("Open Project...");
	loadMn.addActionListener(this);
	loadMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	fileMenu.add(loadMn);
	saveMn = new JMenuItem("Save Project");
	saveMn.addActionListener(this);
	saveMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	fileMenu.add(saveMn);
	saveAsMn = new JMenuItem("Save Project As...");
	saveAsMn.addActionListener(this);
	fileMenu.add(saveAsMn);
	fileMenu.addSeparator();
	summaryMn=new JMenuItem("Project info...");
	summaryMn.addActionListener(this);
	fileMenu.add(summaryMn);
	fileMenu.addSeparator();
	JMenu loadMenu=new JMenu("Import");
	JMenu replaceMenu=new JMenu("Replace");
	JMenu mergeMenu=new JMenu("Merge");
	loadMenu.add(replaceMenu);
	loadMenu.add(mergeMenu);
	ldLocRDFXMLMn = new JMenuItem("RDF/XML from file...");
	ldLocRDFXMLMn.addActionListener(this);
	replaceMenu.add(ldLocRDFXMLMn);
	ldRemRDFXMLMn = new JMenuItem("RDF/XML from URL...");
	ldRemRDFXMLMn.addActionListener(this);
	replaceMenu.add(ldRemRDFXMLMn);
	replaceMenu.addSeparator();
	ldLocN3Mn = new JMenuItem("Notation 3 from file...");
	ldLocN3Mn.addActionListener(this);
	replaceMenu.add(ldLocN3Mn);
	ldRemN3Mn = new JMenuItem("Notation 3 from URL...");
	ldRemN3Mn.addActionListener(this);
	replaceMenu.add(ldRemN3Mn);
	replaceMenu.addSeparator();
	ldLocNTrMn = new JMenuItem("N-Triples from file...");
	ldLocNTrMn.addActionListener(this);
	replaceMenu.add(ldLocNTrMn);
	ldRemNTrMn = new JMenuItem("N-Triples from URL...");
	ldRemNTrMn.addActionListener(this);
	replaceMenu.add(ldRemNTrMn);
	//Added by Arjohn:
	// Add menu items for import plugins, if available
	pluginLdMenuItems = new JMenuItem[importPlugins.length];
	if (importPlugins.length > 0) {
	    replaceMenu.addSeparator();
	    for (int i = 0; i < importPlugins.length; i++) {
		String menuLabel = importPlugins[i].getImportMenuLabel();
		pluginLdMenuItems[i] = new JMenuItem(menuLabel);
		pluginLdMenuItems[i].addActionListener(pluginListener);
		replaceMenu.add(pluginLdMenuItems[i]);
	    }
	}
	mgLocRDFXMLMn = new JMenuItem("RDF/XML from file...");
	mgLocRDFXMLMn.addActionListener(this);
	mergeMenu.add(mgLocRDFXMLMn);
	mgRemRDFXMLMn = new JMenuItem("RDF/XML from URL...");
	mgRemRDFXMLMn.addActionListener(this);
        mergeMenu.add(mgRemRDFXMLMn);
	mergeMenu.addSeparator();
	mgLocN3Mn = new JMenuItem("Notation 3 from file...");
	mgLocN3Mn.addActionListener(this);
	mergeMenu.add(mgLocN3Mn);
	mgRemN3Mn = new JMenuItem("Notation 3 from URL...");
	mgRemN3Mn.addActionListener(this);
	mergeMenu.add(mgRemN3Mn);
	mergeMenu.addSeparator();
	mgLocNTrMn = new JMenuItem("N-Triples from file...");
	mgLocNTrMn.addActionListener(this);
	mergeMenu.add(mgLocNTrMn);
	mgRemNTrMn = new JMenuItem("N-Triples from URL...");
	mgRemNTrMn.addActionListener(this);
	mergeMenu.add(mgRemNTrMn);
	//Added by Arjohn:
	// Add menu items for import plugins, if available
	pluginMgMenuItems = new JMenuItem[importPlugins.length];
	if (importPlugins.length > 0) {
	    mergeMenu.addSeparator();
	    
	    for (int i = 0; i < importPlugins.length; i++) {
		String menuLabel = importPlugins[i].getImportMenuLabel();
		pluginMgMenuItems[i] = new JMenuItem(menuLabel);
		pluginMgMenuItems[i].addActionListener(pluginListener);
		mergeMenu.add(pluginMgMenuItems[i]);
	    }
	}
	fileMenu.add(loadMenu);
	JMenu exportMenu=new JMenu("Export");
	exportRDFMn = new JMenuItem("RDF/XML...");
	exportRDFMn.addActionListener(this);
	exportMenu.add(exportRDFMn);
	exportN3Mn = new JMenuItem("Notation 3...");
	exportN3Mn.addActionListener(this);
	exportMenu.add(exportN3Mn);
	exportNTriMn = new JMenuItem("N-Triples...");
	exportNTriMn.addActionListener(this);
	exportMenu.add(exportNTriMn);
	exportMenu.addSeparator();
	exportPNGMn = new JMenuItem("PNG (current view)...");
	exportPNGMn.addActionListener(this);
	exportMenu.add(exportPNGMn);
	exportSVGMn = new JMenuItem("SVG...");
	exportSVGMn.addActionListener(this);
	exportMenu.add(exportSVGMn);
	//Added by Arjohn:
	// Add menu items for export plugins, if available
	pluginExportMenuItems = new JMenuItem[exportPlugins.length];
	if (exportPlugins.length > 0) {
	    exportMenu.addSeparator();
	    
	    for (int i = 0; i < exportPlugins.length; i++) {
		String menuLabel = exportPlugins[i].getExportMenuLabel();
		pluginExportMenuItems[i] = new JMenuItem(menuLabel);
		pluginExportMenuItems[i].addActionListener(pluginListener);
		exportMenu.add(pluginExportMenuItems[i]);
	    }
	}
	fileMenu.add(exportMenu);
	fileMenu.addSeparator();
	printMn=new JMenuItem("Print...");
	printMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
	printMn.addActionListener(this);
	fileMenu.add(printMn);
	fileMenu.addSeparator();
	exitMn = new JMenuItem("Exit");
	exitMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
	fileMenu.add(exitMn);
	exitMn.addActionListener(this);

	editMenu=new JMenu("Edit");
	editMenu.setMnemonic(KeyEvent.VK_E);
	mnb.add(editMenu);


	undoMn=new JMenuItem("Undo");
	undoMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
	undoMn.addActionListener(a2);
	editMenu.add(undoMn);
	undoMn.setEnabled(false);
	editMenu.addSeparator();
	cutMn=new JMenuItem("Cut");
	cutMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
	cutMn.addActionListener(a2);
	editMenu.add(cutMn);
	copyMn=new JMenuItem("Copy");
	copyMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
	copyMn.addActionListener(a2);
	editMenu.add(copyMn);
	pasteMn=new JMenuItem("Paste");
	pasteMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
	pasteMn.addActionListener(a2);
	editMenu.add(pasteMn);
	pasteMn.setEnabled(false);
	deleteMn=new JMenuItem("Delete");
	deleteMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));  //0=no mask
	deleteMn.addActionListener(a2);
	editMenu.add(deleteMn);
	editMenu.addSeparator();
	selectAllNMn=new JMenuItem("Select All Nodes");
	selectAllNMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
	selectAllNMn.addActionListener(a2);
	editMenu.add(selectAllNMn);
	selectAllEMn=new JMenuItem("Select All Edges");
	selectAllEMn.addActionListener(a2);
	editMenu.add(selectAllEMn);
	advSelectMn=new JMenuItem("Advanced Selection...");
	advSelectMn.addActionListener(a2);
	editMenu.add(advSelectMn);
	unselAllMn=new JMenuItem("Unselect All");
	unselAllMn.addActionListener(a2);
	editMenu.add(unselAllMn);
	editMenu.addSeparator();
	baseURIMn = new JMenuItem("Set Base URI...");
	baseURIMn.addActionListener(this);
	editMenu.add(baseURIMn);
	editMenu.addSeparator();
	prefMn = new JMenuItem("Preferences...");
	prefMn.addActionListener(this);
	editMenu.add(prefMn);

	viewMenu=new JMenu("Views");
	viewMenu.setMnemonic(KeyEvent.VK_V);
	mnb.add(viewMenu);
	backMn=new JMenuItem("Back");
	backMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
	backMn.addActionListener(this);
	viewMenu.add(backMn);
	getGlobVMn=new JMenuItem("Global View");
	getGlobVMn.addActionListener(this);
	getGlobVMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));  //0=no mask
	viewMenu.add(getGlobVMn);
	zmotVMn=new JMenuItem("Zoom Out");
	zmotVMn.addActionListener(this);
	zmotVMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));  //0=no mask
	viewMenu.add(zmotVMn);
	zminVMn=new JMenuItem("Zoom In");
	zminVMn.addActionListener(this);
	zminVMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));  //0=no mask
	viewMenu.add(zminVMn);
	radVMn=new JMenuItem("Radar View");
	radVMn.addActionListener(this);
	radVMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
	viewMenu.add(radVMn);
// 	lensMn = new JCheckBoxMenuItem("Distortion Lens");
// 	lensMn.addActionListener(this);
// 	lensMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
// 	viewMenu.add(lensMn);
	viewMenu.addSeparator();
	layoutMn=new JMenuItem("Suggest Layout");
	layoutMn.addActionListener(this);
	viewMenu.add(layoutMn);
	viewMenu.addSeparator();
	rawrdfMn=new JMenuItem("Show RDF/XML");
	rawrdfMn.addActionListener(this);
	viewMenu.add(rawrdfMn);
	errorMn=new JMenuItem("Show Error log");
	errorMn.addActionListener(this);
	errorMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
	viewMenu.add(errorMn);
	viewMenu.addSeparator();
	showTablesMn=new JCheckBoxMenuItem("Show Definitions Window");
	showTablesMn.setSelected(ConfigManager.showNSWindow);
	showTablesMn.addItemListener(this);
	viewMenu.add(showTablesMn);
	showPropsMn=new JCheckBoxMenuItem("Show Attributes Window");
	showPropsMn.setSelected(ConfigManager.showEditWindow);
	showPropsMn.addItemListener(this);
	viewMenu.add(showPropsMn);
	showNavMn=new JCheckBoxMenuItem("Show Navigation Window");
	showNavMn.setSelected(ConfigManager.showNavWindow);
	showNavMn.addItemListener(this);
	viewMenu.add(showNavMn);
	showBkMn = new JCheckBoxMenuItem("Show Bookmarks Window");
	showBkMn.setSelected(ConfigManager.showBkWindow);
	showBkMn.addItemListener(this);
	viewMenu.add(showBkMn);
	helpMenu=new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
	mnb.add(helpMenu);
	cmdMn = new JMenuItem("Commands");
	cmdMn.addActionListener(this);
	helpMenu.add(cmdMn);
	isvumMn=new JMenuItem("IsaViz User Manual...");
	isvumMn.addActionListener(this);
	helpMenu.add(isvumMn);
	gssumMn=new JMenuItem("GSS User Manual...");
	gssumMn.addActionListener(this);
	helpMenu.add(gssumMn);
	gssedumMn=new JMenuItem("GSS Editor User Manual...");
	gssedumMn.addActionListener(this);
	helpMenu.add(gssedumMn);
	helpMenu.addSeparator();
	plgMn = new JMenuItem("About Plug-ins");
	plgMn.addActionListener(this);
	helpMenu.add(plgMn);
	aboutMn = new JMenuItem("About IsaViz");
	aboutMn.addActionListener(this);
	helpMenu.add(aboutMn);

	//ICON PALETTE
	Container cpane=this.getContentPane();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane.setLayout(gridBag);
	ButtonGroup bg=new ButtonGroup();

	//1st row title
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	GridBagLayout gridBag1=new GridBagLayout();
	GridBagConstraints constraints1=new GridBagConstraints();
	JPanel p1=new JPanel();
	JLabel lgd1=new JLabel("Selection/Creation");
	HSepPanel hp1=new HSepPanel(1,true,null);
	p1.setLayout(gridBag1);
	constraints1.fill=GridBagConstraints.NONE;
	constraints1.anchor=GridBagConstraints.WEST;
	buildConstraints(constraints1,0,0,1,1,10,100);
	gridBag1.setConstraints(lgd1,constraints1);
	p1.add(lgd1);
	constraints1.fill=GridBagConstraints.HORIZONTAL;
	constraints1.anchor=GridBagConstraints.EAST;
	buildConstraints(constraints1,1,0,1,1,90,0);
	gridBag1.setConstraints(hp1,constraints1);
	p1.add(hp1);
	buildConstraints(constraints,0,0,6,1,100,8);
	gridBag.setConstraints(p1,constraints);
	cpane.add(p1);
	//1st row of icons
	constraints.fill=GridBagConstraints.NONE;
	constraints.anchor=GridBagConstraints.CENTER;
	singleSelectBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/Select24b.gif")));
	singleSelectBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/Select24g.gif")));
	singleSelectBt.addActionListener(a1);
	singleSelectBt.setToolTipText("Select");
	bg.add(singleSelectBt);
	buildConstraints(constraints,0,1,1,1,16,12);
	gridBag.setConstraints(singleSelectBt,constraints);
	cpane.add(singleSelectBt);
	regionNSelectBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/RegionNode24b.gif")));
	regionNSelectBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/RegionNode24g.gif")));
	regionNSelectBt.addActionListener(a1);
	regionNSelectBt.setToolTipText("Select Nodes in Region");
	bg.add(regionNSelectBt);
	buildConstraints(constraints,1,1,1,1,16,0);
	gridBag.setConstraints(regionNSelectBt,constraints);
	cpane.add(regionNSelectBt);
	regionESelectBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/RegionEdge24b.gif")));
	regionESelectBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/RegionEdge24g.gif")));
	regionESelectBt.addActionListener(a1);
	bg.add(regionESelectBt);
	regionESelectBt.setToolTipText("Select Edges in Region");
	buildConstraints(constraints,2,1,1,1,16,0);
	gridBag.setConstraints(regionESelectBt,constraints);
	cpane.add(regionESelectBt);
	createResBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/Resource24b.gif")));
	createResBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/Resource24g.gif")));
	createResBt.addActionListener(a1);
	createResBt.setToolTipText("Create New Resource");
	bg.add(createResBt);
	buildConstraints(constraints,3,1,1,1,16,0);
	gridBag.setConstraints(createResBt,constraints);
	cpane.add(createResBt);
	createLitBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/Literal24b.gif")));
	createLitBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/Literal24g.gif")));
	createLitBt.addActionListener(a1);
	createLitBt.setToolTipText("Create New Literal");
	bg.add(createLitBt);
	buildConstraints(constraints,4,1,1,1,16,0);
	gridBag.setConstraints(createLitBt,constraints);
	cpane.add(createLitBt);
	createPropBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/Property24b.gif")));
	createPropBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/Property24g.gif")));
	createPropBt.addActionListener(a1);
	createPropBt.setToolTipText("Create New Property");
	bg.add(createPropBt);
	buildConstraints(constraints,5,1,1,1,16,0);
	gridBag.setConstraints(createPropBt,constraints);
	cpane.add(createPropBt);

	//2nd row title
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	GridBagLayout gridBag4=new GridBagLayout();
	GridBagConstraints constraints4=new GridBagConstraints();
	JPanel p4=new JPanel();
	JLabel lgd4=new JLabel("Zoom/(De)activation/Resize");
	HSepPanel hp4=new HSepPanel(1,true,null);
	p4.setLayout(gridBag4);
	constraints4.fill=GridBagConstraints.NONE;
	constraints4.anchor=GridBagConstraints.WEST;
	buildConstraints(constraints4,0,0,1,1,10,100);
	gridBag4.setConstraints(lgd4,constraints4);
	p4.add(lgd4);
	constraints4.fill=GridBagConstraints.HORIZONTAL;
	constraints4.anchor=GridBagConstraints.EAST;
	buildConstraints(constraints4,1,0,1,1,90,0);
	gridBag4.setConstraints(hp4,constraints4);
	p4.add(hp4);
	buildConstraints(constraints,0,2,6,1,100,8);
	gridBag.setConstraints(p4,constraints);
	cpane.add(p4);
	//2nd row of icons
	constraints.fill=GridBagConstraints.NONE;
	constraints.anchor=GridBagConstraints.CENTER;
	regionZoomBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/RegionZoom24b.gif")));
	regionZoomBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/RegionZoom24g.gif")));
	regionZoomBt.addActionListener(a1);
	regionZoomBt.setToolTipText("Zoom in Region");
	bg.add(regionZoomBt);
	buildConstraints(constraints,0,3,1,1,16,0);
	gridBag.setConstraints(regionZoomBt,constraints);
	cpane.add(regionZoomBt);
	commSingleBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/Comment24b.gif")));
	commSingleBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/Comment24g.gif")));
	commSingleBt.addActionListener(a1);
	commSingleBt.setToolTipText("Deactivate Node/Edge");
	bg.add(commSingleBt);
	buildConstraints(constraints,1,3,1,1,16,12);
	gridBag.setConstraints(commSingleBt,constraints);
	cpane.add(commSingleBt);
 	uncommSingleBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/UnComment24b.gif")));
 	uncommSingleBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/UnComment24g.gif")));
	uncommSingleBt.addActionListener(a1);
	uncommSingleBt.setToolTipText("Reactivate Node/Edge");
	bg.add(uncommSingleBt);
	buildConstraints(constraints,2,3,1,1,16,0);
	gridBag.setConstraints(uncommSingleBt,constraints);
	cpane.add(uncommSingleBt);
	commRegionBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/CommentRegion24b.gif")));
	commRegionBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/CommentRegion24g.gif")));
	commRegionBt.addActionListener(a1);
	commRegionBt.setToolTipText("Deactivate Region");
	bg.add(commRegionBt);
	buildConstraints(constraints,3,3,1,1,16,0);
	gridBag.setConstraints(commRegionBt,constraints);
	cpane.add(commRegionBt);
 	uncommRegionBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/UnCommentRegion24b.gif")));
 	uncommRegionBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/UnCommentRegion24g.gif")));
	uncommRegionBt.addActionListener(a1);
	uncommRegionBt.setToolTipText("Reactivate Region");
	bg.add(uncommRegionBt);
	buildConstraints(constraints,4,3,1,1,16,0);
	gridBag.setConstraints(uncommRegionBt,constraints);
	cpane.add(uncommRegionBt);
	resizeBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/Resize24b.gif")));
	resizeBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/Resize24g.gif")));
	resizeBt.addActionListener(a1);
	resizeBt.setToolTipText("Move and Resize Node/Edge");
	bg.add(resizeBt);
	buildConstraints(constraints,5,3,1,1,16,0);
	gridBag.setConstraints(resizeBt,constraints);
	cpane.add(resizeBt);

	//3rd row title
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	GridBagLayout gridBag2=new GridBagLayout();
	GridBagConstraints constraints2=new GridBagConstraints();
	JPanel p2=new JPanel();
	JLabel lgd2=new JLabel("Editing");
	HSepPanel hp2=new HSepPanel(1,true,null);
	p2.setLayout(gridBag2);
	constraints2.fill=GridBagConstraints.NONE;
	constraints2.anchor=GridBagConstraints.WEST;
	buildConstraints(constraints2,0,0,1,1,10,100);
	gridBag2.setConstraints(lgd2,constraints2);
	p2.add(lgd2);
	constraints2.fill=GridBagConstraints.HORIZONTAL;
	constraints2.anchor=GridBagConstraints.EAST;
	buildConstraints(constraints2,1,0,1,1,90,0);
	gridBag2.setConstraints(hp2,constraints2);
	p2.add(hp2);
	buildConstraints(constraints,0,4,6,1,100,8);
	gridBag.setConstraints(p2,constraints);
	cpane.add(p2);
	//3rd row if icons
	constraints.fill=GridBagConstraints.NONE;
	constraints.anchor=GridBagConstraints.CENTER;

	undoBt=new JButton(new ImageIcon(this.getClass().getResource("/images/Undo24b.gif")));
	undoBt.setContentAreaFilled(false);
	undoBt.setBorderPainted(false);
	undoBt.setFocusPainted(false);
	undoBt.setMargin(singleSelectBt.getMargin());
	undoBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/Undo24g.gif")));
	undoBt.addActionListener(a2);
	undoBt.setToolTipText("Undo");
	buildConstraints(constraints,0,5,1,1,16,0);
	gridBag.setConstraints(undoBt,constraints);
	cpane.add(undoBt);
 	undoBt.setEnabled(false);
	cutBt=new JButton(new ImageIcon(this.getClass().getResource("/images/Cut16b.gif")));
	cutBt.setContentAreaFilled(false);
	cutBt.setBorderPainted(false);
	cutBt.setFocusPainted(false);
	cutBt.setMargin(singleSelectBt.getMargin());
	cutBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/Cut16g.gif")));
	cutBt.addActionListener(a2);
	cutBt.setToolTipText("Cut");
	buildConstraints(constraints,1,5,1,1,16,0);
	gridBag.setConstraints(cutBt,constraints);
	cpane.add(cutBt);
	copyBt=new JButton(new ImageIcon(this.getClass().getResource("/images/Copy24b.gif")));
	copyBt.setContentAreaFilled(false);
	copyBt.setBorderPainted(false);
	copyBt.setFocusPainted(false);
	copyBt.setMargin(singleSelectBt.getMargin());
	copyBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/Copy24g.gif")));
	copyBt.addActionListener(a2);
	copyBt.setToolTipText("Copy");
	buildConstraints(constraints,2,5,1,1,16,0);
	gridBag.setConstraints(copyBt,constraints);
	cpane.add(copyBt);
	pasteBt=new JRadioButton(new ImageIcon(this.getClass().getResource("/images/Paste24b.gif")));
// 	pasteBt.setContentAreaFilled(false);
// 	pasteBt.setBorderPainted(false);
// 	pasteBt.setFocusPainted(false);
// 	pasteBt.setMargin(singleSelectBt.getMargin());
	pasteBt.setSelectedIcon(new ImageIcon(this.getClass().getResource("/images/Paste24g.gif")));
	bg.add(pasteBt);
	pasteBt.addActionListener(a1);
	pasteBt.setToolTipText("Paste");
	buildConstraints(constraints,3,5,1,1,16,0);
	gridBag.setConstraints(pasteBt,constraints);
	cpane.add(pasteBt);
	pasteBt.setEnabled(false);

	//4th row title
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	GridBagLayout gridBag5=new GridBagLayout();
	GridBagConstraints constraints5=new GridBagConstraints();
	JPanel p5=new JPanel();
	JLabel lgd5=new JLabel("Quick Search");
	HSepPanel hp5=new HSepPanel(1,true,null);
	p5.setLayout(gridBag5);
	constraints5.fill=GridBagConstraints.NONE;
	constraints5.anchor=GridBagConstraints.WEST;
	buildConstraints(constraints5,0,0,1,1,10,100);
	gridBag5.setConstraints(lgd5,constraints5);
	p5.add(lgd5);
	constraints5.fill=GridBagConstraints.HORIZONTAL;
	constraints5.anchor=GridBagConstraints.EAST;
	buildConstraints(constraints5,1,0,1,1,90,0);
	gridBag5.setConstraints(hp5,constraints5);
	p5.add(hp5);
	buildConstraints(constraints,0,6,6,1,100,8);
	gridBag.setConstraints(p5,constraints);
	cpane.add(p5);
	//5rd row (search components)
	searchTf=new JTextField("");
	searchTf.addKeyListener(this);
	searchTf.addFocusListener(this);
	buildConstraints(constraints,0,7,4,1,60,12);
	gridBag.setConstraints(searchTf,constraints);
	cpane.add(searchTf);
	searchBt=new JButton("Search");
	searchBt.addKeyListener(this);
	searchBt.addActionListener(a1);
	searchBt.addFocusListener(this);
	searchBt.setPreferredSize(new Dimension(20,18));
	buildConstraints(constraints,4,7,2,1,20,0);
	gridBag.setConstraints(searchBt,constraints);
	cpane.add(searchBt);
	singleSelectBt.setSelected(true);
	this.setTitle("IsaViz RDF Editor");
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){application.exit();}
		//public void windowActivated(WindowEvent e){application.alwaysUpdateViews(true);}
	    };
	this.addWindowListener(w0);
	this.pack();
	this.setLocation(x,y);
	this.setSize(width,height);
// 	this.setVisible(true);
// 	this.setResizable(false);
    }

    /**
     * Initializes all available plugins. This method will automatically
     * detect whether a plugin is an ImportPlugin, and ExportPlugin, or
     * both.
     **/
    void initPlugins(){
	//import plugin classes
	Vector iplgs=new Vector();
	//export plugin classes
	Vector eplgs=new Vector();
	//list all files in 'plugins' dir
	File[] jars=ConfigManager.plugInDir.listFiles();
	if (jars!=null && jars.length>0){
	    URL[] urls=new URL[jars.length];
	    //store path to each JAR file in plugins dir as a URL so that they can be added
	    //later dynamically to the classpath (through a new ClassLoader)
	    for (int i=0;i<jars.length;i++){
		try {
		    urls[i]=jars[i].toURL();
		}
		catch(MalformedURLException mue){System.err.println("Failed to instantiate a class loader for plug-ins: "+mue);}
	    }
	    //insiatntiate a new class loader with a classpath containing all JAR files in plugins directory
	    URLClassLoader ucl=new URLClassLoader(urls);
	    JarFile jf;
	    String s;
	    Object plgInstance=null;
	    //for each of these JAR files
	    for (int i=0;i<jars.length;i++){
		try {
		    jf=new JarFile(jars[i]);
		    //get all CLASS entries
		    for (Enumeration e=jf.entries();e.hasMoreElements();){
			s=((JarEntry)e.nextElement()).getName();
			if (s.endsWith(".class")){
			    //replace directory / by package .
			    s=Utils.replaceString(s,"/",".");
			    //get rid of .class at the end of the jar entry
			    s=s.substring(0,s.length()-6);
			    try {
				//for each class entry, get the Class definition
				Class c=ucl.loadClass(s);
				Class[] interfaces=c.getInterfaces();
				try {
				    //find out if it implements ImportPlugin and/or ExportPlugin (if it does, instantiate and store it)
				    for (int j=0;j<interfaces.length;j++){
					if (interfaces[j].getName().equals("org.w3c.IsaViz.ImportPlugin")){
					    plgInstance=c.newInstance();
					    iplgs.add(plgInstance);
					}
					else if (interfaces[j].getName().equals("org.w3c.IsaViz.ExportPlugin")){
					    if (plgInstance==null){plgInstance=c.newInstance();}
					    eplgs.add(plgInstance);
					}
				    }
				    plgInstance=null;
				}
				catch (InstantiationException ie) {
				    System.err.println("Unable to create plug-in object for class "+
						       s + ": " + ie.getMessage());
				    //ie.printStackTrace();
				}
				catch (IllegalAccessException ie) {
				    System.err.println("Unable to create plug-in object for class "+
						       s + ": " + ie.getMessage());
				    //ie.printStackTrace();
				}
			    }
			    catch (ClassNotFoundException ex){System.err.println("Failed to load plug-in class "+s);}
			}
		    }
		}
		catch (IOException ex2){System.err.println("Failed to load plug-in from JAR file "+jars[i].getAbsolutePath());}
		catch (NoClassDefFoundError ex2){System.err.println("One or more plugins might have failed to initialize because of the following error:\nNo Class Definition Found for "+ex2.getMessage());}
		catch (ClassFormatError ex2){System.err.println("One or more plugins might have failed to initialize because of the following error:\nClass Format Error for "+ex2.getMessage());}
	    }
	}
	//store the plugins in arrays instead of vectors
	importPlugins = new ImportPlugin[iplgs.size()];
	for (int i=0;i<iplgs.size();i++){
	    importPlugins[i]=(ImportPlugin)iplgs.elementAt(i);
	}
	exportPlugins = new ExportPlugin[eplgs.size()];
	for (int i=0;i<eplgs.size();i++){
	    exportPlugins[i]=(ExportPlugin)eplgs.elementAt(i);
	}
    }

    public void actionPerformed(ActionEvent e){
	int returnVal;
	Object o=e.getSource();
	if (o==exitMn){application.exit();}
	else if (o==backMn){application.moveBack();}
	else if (o==resetMn){
	    application.promptReset();
	}
	else if (o==loadMn){
	    application.openProject();
	}
	else if (o==saveMn){
	    application.saveProject();
	}
	else if (o==saveAsMn){
	    application.saveProjectAs();
	}
	else if (o==summaryMn){application.showPrjSummary();}
	else if (o==ldLocRDFXMLMn){
	    fc=new JFileChooser(Editor.lastImportRDFDir!=null ? Editor.lastImportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Import Local RDF/XML File (replace)");
	    returnVal=fc.showOpenDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		application.loadRDF(fc.getSelectedFile(),RDFLoader.RDF_XML_READER,true);
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==ldRemRDFXMLMn){
	    new URLPanel(this.application,"Specify URL:",RDFLoader.RDF_XML_READER,false,false);
	}
	else if (o==ldLocN3Mn){
	    fc=new JFileChooser(Editor.lastImportRDFDir!=null ? Editor.lastImportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Import Local N3 File (replace)");
	    returnVal=fc.showOpenDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		application.loadRDF(fc.getSelectedFile(),RDFLoader.N3_READER,true);
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==ldRemN3Mn){
	    new URLPanel(this.application,"Specify URL:",RDFLoader.N3_READER,false,false);
	}
	else if (o==ldLocNTrMn){
	    fc=new JFileChooser(Editor.lastImportRDFDir!=null ? Editor.lastImportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Import Local N-Triples File (replace)");
	    returnVal=fc.showOpenDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		application.loadRDF(fc.getSelectedFile(),RDFLoader.NTRIPLE_READER,true);
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==ldRemNTrMn){
	    new URLPanel(this.application,"Specify URL:",RDFLoader.NTRIPLE_READER,false,false);
	}
	else if (o==mgLocRDFXMLMn){
	    fc=new JFileChooser(Editor.lastImportRDFDir!=null ? Editor.lastImportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Import Local RDF/XML File (merge)");
	    returnVal=fc.showOpenDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		application.mergeRDF(fc.getSelectedFile(),RDFLoader.RDF_XML_READER,true);
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==mgRemRDFXMLMn){
	    new URLPanel(this.application,"Specify URL:",RDFLoader.RDF_XML_READER,true,false);
	}
	else if (o==mgLocN3Mn){
	    fc=new JFileChooser(Editor.lastImportRDFDir!=null ? Editor.lastImportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Import Local N3 File (merge)");
	    returnVal=fc.showOpenDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		application.mergeRDF(fc.getSelectedFile(),RDFLoader.N3_READER,true);
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==mgRemN3Mn){
	    new URLPanel(this.application,"Specify URL:",RDFLoader.N3_READER,true,false);
	}
	else if (o==mgLocNTrMn){
	    fc=new JFileChooser(Editor.lastImportRDFDir!=null ? Editor.lastImportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Import Local N-Triples File (merge)");
	    returnVal=fc.showOpenDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		application.mergeRDF(fc.getSelectedFile(),RDFLoader.NTRIPLE_READER,true);
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==mgRemNTrMn){
	    new URLPanel(this.application,"Specify URL:",RDFLoader.NTRIPLE_READER,true,false);
	}
	else if (o==exportRDFMn){
	    fc=new JFileChooser(Editor.lastExportRDFDir!=null ? Editor.lastExportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Export to RDF/XML");
	    returnVal=fc.showSaveDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		    final SwingWorker worker=new SwingWorker(){
			    public Object construct(){
				application.exportRDF(fc.getSelectedFile(),true);
				return null;
			    }
			};
		    worker.start();
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==exportN3Mn){
	    fc=new JFileChooser(Editor.lastExportRDFDir!=null ? Editor.lastExportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Export to N3");
	    returnVal=fc.showSaveDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		    final SwingWorker worker=new SwingWorker(){
			    public Object construct(){
				application.exportN3(fc.getSelectedFile());
				return null;
			    }
			};
		    worker.start();
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==exportNTriMn){
	    fc=new JFileChooser(Editor.lastExportRDFDir!=null ? Editor.lastExportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Export to N-Triples");
	    returnVal=fc.showSaveDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		    final SwingWorker worker=new SwingWorker(){
			    public Object construct(){
				application.exportNTriples(fc.getSelectedFile());
				return null;
			    }
			};
		    worker.start();
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==exportPNGMn){
	    fc=new JFileChooser(Editor.lastExportRDFDir!=null ? Editor.lastExportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Export to PNG");
	    returnVal=fc.showSaveDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		    final SwingWorker worker=new SwingWorker(){
			    public Object construct(){
				application.exportPNG(fc.getSelectedFile());
				return null;
			    }
			};
		    worker.start();
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==exportSVGMn){
	    fc=new JFileChooser(Editor.lastExportRDFDir!=null ? Editor.lastExportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Export to SVG");
	    returnVal=fc.showSaveDialog(this);
	    if (returnVal==JFileChooser.APPROVE_OPTION) {
		    final SwingWorker worker=new SwingWorker(){
			    public Object construct(){
				application.exportSVG(fc.getSelectedFile());
				return null;
			    }
			};
		    worker.start();
	    }
	    repaint(); //some time the icon palette is not painted properly after the jfilechooser disappears
	}
	else if (o==prefMn){
	    dp=new PrefWindow(application);
	}
	else if (o==baseURIMn){
	    BaseURIPanel bup=new BaseURIPanel();
	}
	else if (o==rawrdfMn){
	    application.displayRawRDFXMLFile();
	}
	else if (o==printMn){application.printRequest();}
	else if (o==errorMn){application.showErrorMessages();}
	else if (o==getGlobVMn){application.getGlobalView();}
	else if (o==zminVMn){application.getLowerView();}
	else if (o==zmotVMn){application.getHigherView();}
	else if (o==radVMn){application.showRadarView(true);}
// 	else if (o == lensMn){application.geomMngr.setLens(lensMn.isSelected());}
	else if (o==layoutMn){application.reLayoutGraph();}
	else if (o==cmdMn){
	    Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	    TextViewer cmds=new TextViewer(new StringBuffer(Messages.commands),"Commands",0,(screenSize.width-400)/2,(screenSize.height-300)/2,400,300,false);
	}
	else if (o==aboutMn){SplashWindow sp=new SplashWindow(0,"images/IsavizSplash.gif",false,this);}
	else if (o==isvumMn){application.displayURLinBrowser(ConfigManager.ISV_MANUAL_URI);}
	else if (o==gssumMn){application.displayURLinBrowser(GSSEditor.GSS_MANUAL_URI);}
	else if (o==gssedumMn){application.displayURLinBrowser(GSSEditor.GSS_EDITOR_MANUAL_URI);}
	else if (o==plgMn){displayPluginInfo();}
    }

    void plugInLoad(ImportPlugin impi){
	try {
	    // Dump input to tmp file
	    InputStream is=impi.importRDF(this);
	    if (is!=null){
// 		File tmpF = Utils.inputStreamToTempFile(is,Editor.m_TmpDir.toString(),"plg",".rdf");
		// Load tmp file
// 		if (tmpF!=null){
// 		    application.loadRDF(tmpF,RDFLoader.RDF_XML_READER);
		application.loadRDF(is,RDFLoader.RDF_XML_READER);
// 		}
	    }
	}
	catch (IOException ioexc) {
	    System.err.println("Unable to import data from plugin: " +
			       ioexc.getMessage());
	}
    }

    void plugInMerge(ImportPlugin impi){
	// Load data from plugin i
	try {
	    // Dump input to tmp file
	    InputStream is=impi.importRDF(this);
	    if (is!=null){
// 		File tmpF = Utils.inputStreamToTempFile(is,Editor.m_TmpDir.toString(),"plg",".rdf");
		// merge tmp file
// 		if (tmpF!=null){
// 		    application.mergeRDF(tmpF, RDFLoader.RDF_XML_READER);
		application.mergeRDF(is,RDFLoader.RDF_XML_READER);
// 		}
	    }
	}
	catch (IOException ioexc) {
	    System.err.println("Unable to import data from plugin: " +
			       ioexc.getMessage());
	}
    }
	
    void plugInExport(ExportPlugin expi){
	try {
	    OutputStream outputS = expi.exportRDF(this);
	    if (outputS != null) {
// 		// Dump data in tmp file
// 		File tmpF = Utils.createTempFile(Editor.m_TmpDir.toString(),"plg",".rdf");
// 		tmpF.deleteOnExit();
// 		application.exportRDF(tmpF);
// 		// Read data from tmp file and write it to the plugin
// 		FileInputStream inputS = new FileInputStream(tmpF);
// 		Utils.writeInputStreamToOutputStream(inputS,outputS);
// 		// Remove the tmp file
// 		tmpF.delete();
		application.exportRDF(outputS);
		outputS.flush();
		outputS.close();
	    }
	}
	catch (IOException ioexc) {
	    System.err.println("Unable to export data to plugin: " +
			       ioexc.getMessage());
	}
    }

    public void itemStateChanged(ItemEvent e){
	Object source=e.getItemSelectable();
        if (source==showTablesMn){
	    if (e.getStateChange()==ItemEvent.DESELECTED){
		application.showTablePanel(false);
	    }
	    else if (e.getStateChange()==ItemEvent.SELECTED){
		application.showTablePanel(true);
	    }
	}
	else if (source==showPropsMn){
	    if (e.getStateChange()==ItemEvent.DESELECTED){
		application.showPropsPanel(false);
	    }
	    else if (e.getStateChange()==ItemEvent.SELECTED){
		application.showPropsPanel(true);
	    }
	}
	else if (source==showNavMn){
	    if (e.getStateChange()==ItemEvent.DESELECTED){
		application.showNavPanel(false);
	    }
	    else if (e.getStateChange()==ItemEvent.SELECTED){
		application.showNavPanel(true);
	    }
	}
	else if (source==showBkMn){
	    if (e.getStateChange()==ItemEvent.DESELECTED){
		this.showBookmarkPanel(false);
	    }
	    else if (e.getStateChange()==ItemEvent.SELECTED){
		this.showBookmarkPanel(true);
	    }
	}
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    Object src=e.getSource();
	    if (src==searchTf || src==searchBt){application.quickSearch(searchTf.getText());}
	}
    }

    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    public void focusGained(FocusEvent e){}
    public void focusLost(FocusEvent e){
	application.resetQuickSearch();
    }

    ActionListener a1=new ActionListener(){
	    public void actionPerformed(ActionEvent e){
		Object src=e.getSource();
		if (src==singleSelectBt){
		    application.setMode(EditorEvtHdlr.SINGLE_SELECTION_MODE);
		    Editor.vsm.getActiveView().setStatusBarText("Hold Ctrl down for multiple selection");
		}
		else if (src==regionNSelectBt){
		    application.setMode(EditorEvtHdlr.REGION_SELECTION_MODE);
		    application.eeh.selectWhat=EditorEvtHdlr.NODES_ONLY;
		}
		else if (src==regionESelectBt){
		    application.setMode(EditorEvtHdlr.REGION_SELECTION_MODE);
		    application.eeh.selectWhat=EditorEvtHdlr.EDGES_ONLY;
		}
		else if (src==regionZoomBt){
		    application.setMode(EditorEvtHdlr.REGION_ZOOM_MODE);
		}
		else if (src==createResBt){
		    application.setMode(EditorEvtHdlr.CREATE_RESOURCE_MODE);
		}
		else if (src==createPropBt){
		    application.setMode(EditorEvtHdlr.CREATE_PREDICATE_MODE);
		    application.tblp.tabbedPane.setSelectedIndex(1);  //display the Property constructor Panel in table panel
		}
		else if (src==createLitBt){
		    application.setMode(EditorEvtHdlr.CREATE_LITERAL_MODE);
		}
		else if (src==resizeBt){
		    application.setMode(EditorEvtHdlr.MOVE_RESIZE_MODE);
		}
		else if (src==pasteBt){
		    application.setMode(EditorEvtHdlr.PASTE_MODE);
		}
		else if (src==commSingleBt){
		    application.setMode(EditorEvtHdlr.COMMENT_SINGLE_MODE);
		}
		else if (src==commRegionBt){
		    application.setMode(EditorEvtHdlr.COMMENT_REGION_MODE);
		}
		else if (src==uncommSingleBt){
		    application.setMode(EditorEvtHdlr.UNCOMMENT_SINGLE_MODE);
		}
		else if (src==uncommRegionBt){
		    application.setMode(EditorEvtHdlr.UNCOMMENT_REGION_MODE);
		}
		else if (src==searchBt){
		    application.quickSearch(searchTf.getText());
		}
	    }
	};

    ActionListener a2=new ActionListener(){
	    public void actionPerformed(ActionEvent e){
		Object src=e.getSource();
		if (src==undoMn || src==undoBt){
		    application.undo();
		}
		else if (src==selectAllNMn){
		    application.selectAllNodes();
		}
		else if (src==selectAllEMn){
		    application.selectAllEdges();
		}
		else if (src==advSelectMn){
		    new SelectionPanel(application);
		}
		else if (src==unselAllMn){
		    application.unselectAll();
		}
		else if (src==deleteMn){
		    application.deleteSelectedEntities();
		}
		else if (src==cutMn || src==cutBt){
		    application.cutSelection();
		}
		else if (src==copyMn || src==copyBt){
		    application.copySelection();
		}
		else if (src==pasteMn){
		    View v=Editor.vsm.getView(Editor.mainView);
		    application.pasteSelection(v.mouse.vx,v.mouse.vy);
		}
	    }
	};

    ActionListener pluginListener =new ActionListener(){//Added by Arjohn  (with modifications by Emmanuel)
	    public void actionPerformed(ActionEvent e){
		Object o=e.getSource();
		// Check if one of the plugin menus have been selected
		// Load menus
		boolean stopInspection=false; //used to avoid looking in merge and export menus if the item is found in load menu (or merge menu)
		for (int i = 0; i < pluginLdMenuItems.length; i++) {
		    if (o == pluginLdMenuItems[i]) {
			// Load data from plugin i
			stopInspection=true;
			plugInLoad(importPlugins[i]);
			break;
		    }
		}
		if (!stopInspection){
		    // Merge menus
		    for (int i = 0; i < pluginMgMenuItems.length; i++) {
			if (o == pluginMgMenuItems[i]) {
			    stopInspection=true;
			    plugInMerge(importPlugins[i]);
			    break;
			}
		    }
		}
		if (!stopInspection){
		    // Export menus
		    for (int i = 0; i < pluginExportMenuItems.length; i++) {
			if (o == pluginExportMenuItems[i]) {
			    // Store data to plugin i
			    plugInExport(exportPlugins[i]);
			    break;
			}
		    }
		}
	    }
	};

//     void displayRawFile(Object o,String frameTitle){
// 	try {
// 	    if (o instanceof File){
// 		File f=(File)o;
// 		FileReader fr=new FileReader(f);
// 		char[] buf=new char[(int)f.length()];
// 		fr.read(buf);
// 		fr.close();
// 		StringBuffer sb=new StringBuffer();
// 		sb.append(buf);
// 		TextViewer v=new TextViewer(sb,frameTitle,0,false);
// 	    }
// 	    else if (o instanceof java.net.URL){
// 		java.net.URL u=(java.net.URL)o;
// 		BufferedReader in=new BufferedReader(new InputStreamReader(u.openStream()));
// 		StringBuffer sb=new StringBuffer();
// 		String s1;
// 		while ((s1=in.readLine())!=null){
// 		    sb.append(s1+"\n");
// 		}
// 		in.close();
// 		TextViewer v=new TextViewer(sb,frameTitle,0,false);
// 	    }
// 	}
// 	catch (Exception ex){System.err.println("Error: commandPanel.displayRawFile: "+ex);}
//     }

    /*enable/disable the icon/menu item associated to undo*/
    void enableUndo(boolean b){
	if (b!=undoBt.isEnabled()){
	    undoMn.setEnabled(b);
	    undoBt.setEnabled(b);
	}
    }

    /*enable/disable the icon/menu item associated to undo*/
    void enablePaste(boolean b){
	if (b!=pasteBt.isEnabled()){
	    pasteMn.setEnabled(b);
	    pasteBt.setEnabled(b);
	}
    }

    /*show/hide the window displaying geographical bookmarks*/
    void showBookmarkPanel(boolean b){
	ConfigManager.showBkWindow=b;
	if (ConfigManager.showBkWindow){application.bkp.setVisible(true);}
	else {application.bkp.setVisible(false);}
    }

    void displayPluginInfo(){
	StringBuffer sb=new StringBuffer();
	sb.append("----------------------------------------\nMain Plug-ins:\n----------------------------------------\n");
	for (int i=0;i<application.tblp.tabPlugins.length;i++){
	    sb.append("-"+application.tblp.tabPlugins[i].getName()+"\n");
	    sb.append("\tVersion: "+application.tblp.tabPlugins[i].getVersion()+"\n");
	    sb.append("\tAuthor: "+application.tblp.tabPlugins[i].getAuthor()+"\n");
	    sb.append("\tMore Information: "+application.tblp.tabPlugins[i].getURL().toString()+"\n\n");
	}
	sb.append("\n");
	sb.append("----------------------------------------\nImport Plug-ins:\n----------------------------------------\n");
	for (int i=0;i<importPlugins.length;i++){
	    sb.append("-"+importPlugins[i].getName()+"\n");
	    sb.append("\tVersion: "+importPlugins[i].getVersion()+"\n");
	    sb.append("\tAuthor: "+importPlugins[i].getAuthor()+"\n");
	    sb.append("\tMore Information: "+importPlugins[i].getURL().toString()+"\n\n");
	}
	sb.append("\n");
	sb.append("----------------------------------------\nExport Plug-ins:\n----------------------------------------\n");
	for (int i=0;i<exportPlugins.length;i++){
	    sb.append("-"+exportPlugins[i].getName()+"\n");
	    sb.append("\tVersion: "+exportPlugins[i].getVersion()+"\n");
	    sb.append("\tAuthor: "+exportPlugins[i].getAuthor()+"\n");
	    sb.append("\tMore Information: "+exportPlugins[i].getURL().toString()+"\n\n");
	}
	new TextViewer(sb,"Installed Plug-ins",0,false);
    }

    void updateSwingFont(){
	searchTf.setFont(Editor.swingFont);
    }

    void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }

}
