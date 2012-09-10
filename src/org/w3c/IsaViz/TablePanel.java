/*   FILE: TablePanel.java
 *   DATE OF CREATION:   11/27/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Oct  6 11:07:32 2005 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: TablePanel.java,v 1.23 2006/05/11 09:05:38 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2006.
 *  Please first read the full copyright statement in file copyright.html
 */ 

package org.w3c.IsaViz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.IOException;

import com.xerox.VTM.engine.SwingWorker;

import org.w3c.IsaViz.fresnel.*;

/*a panel displaying namespace bindings and property constructors (property types that can be selected to instantiate a property), in 2 different tabbed panes.*/

public class TablePanel extends JFrame implements ActionListener,KeyListener,MouseListener,ChangeListener {

    /*tells whether isolated nodes (when applying graph stylesheets) should be shown or hidden (default is show)*/
    static boolean SHOW_ISOLATED_NODES=true;

    static String _rx="   RDF/XML...",_n3="   Notation 3...",_nt="   N-Triples...";

    static String[] ldStyleFileFormats={"Load from File...",_rx,_n3,_nt};
    static String[] ldStyleURLFormats={"Load from URL...",_rx,_n3,_nt};

    Editor application;

    JTabbedPane tabbedPane;
    JScrollPane sp1,sp2,sp4;  //scrollpanes for nsTable, prTable, gssTable

    //namespace bindings table
    JTable nsTable;
    public DefaultTableModel nsTableModel;
    JButton addNSBt,remNSBt;
    JTextField nsPrefTf,nsURITf;

    //property constructors table
    JTable prTable;
    JButton addPRBt,remPRBt,loadPRBt;
    JTextField nsPrpTf,lnPrpTf;
    
    //resource outgoing predicates browser
    JPanel rsPane,outerRsPane;
    JLabel resourceLb,bckBt;

    IResource[] browserList=new IResource[Editor.MAX_BRW_LIST_SIZE];
    int brwIndex=0;

    //graph stylesheet table
    JTable gssTable;
    DefaultTableModel gssTableModel;
    JComboBox ldStyleFromFileCb,ldStyleFromURLCb;
    JButton editStyleBt,removeStyleBt,applyStyleBt,promoteSelStyleBt,demoteSelStyleBt;
    JCheckBox shIsolRsCb,debugGssCb; //show isolated resources,debug mode
    ProgressPanel stpb;

    //quick input model table
    JTextArea rdfInputArea;
    JButton loadQIBt,mergeQIBt,clearQIBt;
    JComboBox qiFormatList;

    //FSL table
    JTextField fslInputArea;
    JLabel fslResultLb;
    JButton evalFSLBt, clearFSLBt, cfgRDFSBt;
    JRadioButton nodeStepBt, arcStepBt;
    JCheckBox loadRDFSCb;

    // other tabs instantiated by plugins
    TabPlugin[] tabPlugins;


    TablePanel(Editor e,int x,int y,int width,int height){
	application=e;

	tabbedPane = new JTabbedPane();
	tabbedPane.addChangeListener(this);

	//panel for namespace definitions
	JPanel nsPane=new JPanel();
	GridBagLayout gridBag1=new GridBagLayout();
	GridBagConstraints constraints1=new GridBagConstraints();
	nsPane.setLayout(gridBag1);
	nsTableModel=new NSTableModel(0,3);
	nsTableModel.addTableModelListener(l1);
	nsTable=new JTable(nsTableModel);
	nsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
// 	nsTable.setCellSelectionEnabled(true);
	TableColumn tc=nsTable.getColumnModel().getColumn(0);
	tc.setPreferredWidth(width/100*20);tc.setHeaderValue("Prefix");
	tc=nsTable.getColumnModel().getColumn(1);
	tc.setPreferredWidth(width/100*70);tc.setHeaderValue("URI");
	tc=nsTable.getColumnModel().getColumn(2);
	tc.setPreferredWidth(width/100*10);tc.setHeaderValue("Display Prefix");
	//display 3rd column as checkbox (it is a boolean)
	TableCellRenderer tcr=nsTable.getDefaultRenderer(Boolean.class);
	tc.setCellRenderer(tcr);
	TableCellEditor tce=nsTable.getDefaultEditor(Boolean.class);
	tc.setCellEditor(tce);
	sp1=new JScrollPane(nsTable);
	sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	sp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	constraints1.fill=GridBagConstraints.BOTH;
	constraints1.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints1,0,0,5,1,100,99);
	gridBag1.setConstraints(sp1,constraints1);
	nsPane.add(sp1);
	constraints1.fill=GridBagConstraints.HORIZONTAL;
	constraints1.anchor=GridBagConstraints.WEST;
	nsPrefTf=new JTextField();
	nsPrefTf.addKeyListener(this);
	buildConstraints(constraints1,0,1,1,1,25,1);
	gridBag1.setConstraints(nsPrefTf,constraints1);
	nsPane.add(nsPrefTf);
	nsURITf=new JTextField();
	nsURITf.addKeyListener(this);
	buildConstraints(constraints1,1,1,1,1,75,0);
	gridBag1.setConstraints(nsURITf,constraints1);
	nsPane.add(nsURITf);
	constraints1.fill=GridBagConstraints.NONE;
	addNSBt=new JButton("Add NS Binding");addNSBt.setFont(Editor.tinyFont);
	addNSBt.addActionListener(this);addNSBt.addKeyListener(this);
	buildConstraints(constraints1,2,1,1,1,7,0);
	gridBag1.setConstraints(addNSBt,constraints1);
	nsPane.add(addNSBt);
	constraints1.anchor=GridBagConstraints.CENTER;
	remNSBt=new JButton("Remove Selected");remNSBt.setFont(Editor.tinyFont);
	remNSBt.addActionListener(this);remNSBt.addKeyListener(this);
	buildConstraints(constraints1,3,1,1,1,6,0);
	gridBag1.setConstraints(remNSBt,constraints1);
	nsPane.add(remNSBt);
	tabbedPane.addTab("Namespaces",nsPane);


	//panel for properties
	JPanel prPane=new JPanel();
	GridBagLayout gridBag2=new GridBagLayout();
	GridBagConstraints constraints2=new GridBagConstraints();
	prPane.setLayout(gridBag2);
	DefaultTableModel prTbModel=new PRTableModel(0,3);
	prTbModel.addTableModelListener(l2);
	prTable=new JTable(prTbModel);
	prTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
// 	prTable.setCellSelectionEnabled(true);
	TableColumn tc2=prTable.getColumnModel().getColumn(0);
	tc2.setPreferredWidth(width/100*50);tc2.setHeaderValue("Namespace");
	tc2=prTable.getColumnModel().getColumn(1);
	tc2.setPreferredWidth(width/100*15);tc2.setHeaderValue("Prefix");
	tc2=prTable.getColumnModel().getColumn(2);
	tc2.setPreferredWidth(width/100*35);tc2.setHeaderValue("Property name");
	sp2=new JScrollPane(prTable);
	sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	sp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	constraints2.fill=GridBagConstraints.BOTH;
	constraints2.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints2,0,0,5,1,100,99);
	gridBag2.setConstraints(sp2,constraints2);
	prPane.add(sp2);
	constraints2.fill=GridBagConstraints.HORIZONTAL;
	constraints2.anchor=GridBagConstraints.WEST;
	nsPrpTf=new JTextField();
	nsPrpTf.addKeyListener(this);
	buildConstraints(constraints2,0,1,1,1,40,1);
	gridBag2.setConstraints(nsPrpTf,constraints2);
	prPane.add(nsPrpTf);
	lnPrpTf=new JTextField();
	lnPrpTf.addKeyListener(this);
	buildConstraints(constraints2,1,1,1,1,40,0);
	gridBag2.setConstraints(lnPrpTf,constraints2);
	prPane.add(lnPrpTf);
	constraints2.fill=GridBagConstraints.NONE;
	addPRBt=new JButton("Add Property");addPRBt.setFont(Editor.tinyFont);
	addPRBt.addActionListener(this);addPRBt.addKeyListener(this);
	buildConstraints(constraints2,2,1,1,1,20,0);
	gridBag2.setConstraints(addPRBt,constraints2);
	prPane.add(addPRBt);
	constraints2.anchor=GridBagConstraints.WEST;
	remPRBt=new JButton("Remove Selected");remPRBt.setFont(Editor.tinyFont);
	remPRBt.addActionListener(this);remPRBt.addKeyListener(this);
	buildConstraints(constraints2,3,1,1,1,10,0);
	gridBag2.setConstraints(remPRBt,constraints2);
	prPane.add(remPRBt);
	constraints2.anchor=GridBagConstraints.EAST;
	loadPRBt=new JButton("Load Properties...");loadPRBt.setFont(Editor.tinyFont);
	loadPRBt.addActionListener(this);loadPRBt.addKeyListener(this);
	buildConstraints(constraints2,4,1,1,1,10,0);
	gridBag2.setConstraints(loadPRBt,constraints2);
	prPane.add(loadPRBt);
	tabbedPane.addTab("Property Types",prPane);

	ListSelectionModel rowSM=prTable.getSelectionModel();
	rowSM.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    if (e.getValueIsAdjusting()) return;
		    ListSelectionModel lsm=(ListSelectionModel)e.getSource();
		    if (!lsm.isSelectionEmpty()){
			int selectedRow = lsm.getMinSelectionIndex();
			application.setSelectedPropertyConstructor((String)prTable.getModel().getValueAt(selectedRow,0),(String)prTable.getModel().getValueAt(selectedRow,2));
		    }
		}
	    });

	//property browser panel
	outerRsPane=new JPanel();
	GridBagLayout gridBag3=new GridBagLayout();
	GridBagConstraints constraints3=new GridBagConstraints();
	outerRsPane.setLayout(gridBag3);

	constraints3.fill=GridBagConstraints.HORIZONTAL;
	constraints3.anchor=GridBagConstraints.WEST;
	resourceLb=new JLabel();
	resourceLb.setFont(Editor.swingFont);
	buildConstraints(constraints3,0,0,1,1,50,1);
	gridBag3.setConstraints(resourceLb,constraints3);
	outerRsPane.add(resourceLb);
	constraints3.fill=GridBagConstraints.NONE;
	constraints3.anchor=GridBagConstraints.EAST;
	bckBt=new JLabel("Back");
	buildConstraints(constraints3,1,0,1,1,50,1);
	gridBag3.setConstraints(bckBt,constraints3);
	bckBt.addMouseListener(this);
	outerRsPane.add(bckBt);
	rsPane=new JPanel();
	JScrollPane sp3=new JScrollPane(rsPane);
	sp3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	sp3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	sp3.getVerticalScrollBar().setUnitIncrement(5);
	constraints3.fill=GridBagConstraints.BOTH;
	constraints3.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints3,0,1,2,1,100,99);
	gridBag3.setConstraints(sp3,constraints3);
	outerRsPane.add(sp3);
	tabbedPane.addTab("Property Browser",outerRsPane);

	//graph stylsheet panel
	JPanel gssPane=new JPanel();
	GridBagLayout gridBag4=new GridBagLayout();
	GridBagConstraints constraints4=new GridBagConstraints();
	gssPane.setLayout(gridBag4);
	JPanel gssPrioPane=new JPanel();
	gssPrioPane.setBorder(BorderFactory.createEmptyBorder());
	GridBagLayout gridBag4a=new GridBagLayout();
	GridBagConstraints constraints4a=new GridBagConstraints();
	gssPrioPane.setLayout(gridBag4a);
	promoteSelStyleBt=new JButton(new ImageIcon(this.getClass().getResource("/images/down24.gif")));
	demoteSelStyleBt=new JButton(new ImageIcon(this.getClass().getResource("/images/up24.gif")));
	promoteSelStyleBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/down24b.gif")));
	demoteSelStyleBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/up24b.gif")));
	promoteSelStyleBt.setBorder(BorderFactory.createEmptyBorder());
	demoteSelStyleBt.setBorder(BorderFactory.createEmptyBorder());
	promoteSelStyleBt.addActionListener(this);
	demoteSelStyleBt.addActionListener(this);
	constraints4a.fill=GridBagConstraints.NONE;
	constraints4a.anchor=GridBagConstraints.SOUTH;
	buildConstraints(constraints4a,0,0,1,1,100,50);
	gridBag4a.setConstraints(demoteSelStyleBt,constraints4a);
	gssPrioPane.add(demoteSelStyleBt);
	constraints4a.anchor=GridBagConstraints.NORTH;
	buildConstraints(constraints4a,0,1,1,1,100,50);
	gridBag4a.setConstraints(promoteSelStyleBt,constraints4a);
	gssPrioPane.add(promoteSelStyleBt);
	buildConstraints(constraints4,0,0,1,1,1,99);
	gridBag4.setConstraints(gssPrioPane,constraints4);
	gssPane.add(gssPrioPane);
	gssTableModel=new GSSTableModel(0,1);
	gssTable=new JTable(gssTableModel);
	gssTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	tc=gssTable.getColumnModel().getColumn(0);
	tc.setPreferredWidth(width);tc.setHeaderValue("Location");
	sp4=new JScrollPane(gssTable);
	sp4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	sp4.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	constraints4.fill=GridBagConstraints.BOTH;
	constraints4.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints4,1,0,1,1,99,0);
	gridBag4.setConstraints(sp4,constraints4);
	gssPane.add(sp4);
	JPanel gssCmdPane=new JPanel();
	gssCmdPane.setBorder(BorderFactory.createEmptyBorder());
	gssCmdPane.setLayout(new GridLayout(1,8));
	ldStyleFromFileCb=new JComboBox(ldStyleFileFormats);
	ldStyleFromURLCb=new JComboBox(ldStyleURLFormats);
	removeStyleBt=new JButton("Remove");
	editStyleBt=new JButton("Edit Stylesheet...");
	shIsolRsCb=new JCheckBox("Show Isolated Resources",SHOW_ISOLATED_NODES);
	applyStyleBt=new JButton("Apply Stylesheets");
	debugGssCb=new JCheckBox("Debug",GraphStylesheet.DEBUG_GSS);
	stpb=new ProgressPanel();
	stpb.setForegroundColor(ConfigManager.pastelBlue);
	ldStyleFromFileCb.addActionListener(this);
	ldStyleFromURLCb.addActionListener(this);
	editStyleBt.addActionListener(this);
	removeStyleBt.addActionListener(this);
	applyStyleBt.addActionListener(this);
	shIsolRsCb.addActionListener(this);
	debugGssCb.addActionListener(this);
	gssCmdPane.add(ldStyleFromFileCb);
	gssCmdPane.add(ldStyleFromURLCb);
	gssCmdPane.add(removeStyleBt);
	JPanel p89=new JPanel();
	p89.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	GridBagLayout gridBag89=new GridBagLayout();
	GridBagConstraints constraints89=new GridBagConstraints();
	p89.setLayout(gridBag89);
	constraints89.fill=GridBagConstraints.HORIZONTAL;
	constraints89.anchor=GridBagConstraints.CENTER;
 	buildConstraints(constraints89,0,0,1,1,100,100);
	gridBag89.setConstraints(stpb,constraints89);
 	p89.add(stpb);
	gssCmdPane.add(p89);
	gssCmdPane.add(editStyleBt);
	//gssCmdPane.add(new JPanel());
	gssCmdPane.add(applyStyleBt);
	gssCmdPane.add(shIsolRsCb);
	gssCmdPane.add(debugGssCb);
	buildConstraints(constraints4,0,1,2,1,100,1);
	gridBag4.setConstraints(gssCmdPane,constraints4);
	gssPane.add(gssCmdPane);
	tabbedPane.addTab("Stylesheets",gssPane);

	//quick rdf input panel
	JPanel outerQIPane=new JPanel();
	GridBagLayout gridBag5=new GridBagLayout();
	GridBagConstraints constraints5=new GridBagConstraints();
	outerQIPane.setLayout(gridBag5);
	rdfInputArea=new JTextArea();
	JScrollPane sp5=new JScrollPane(rdfInputArea);
	sp5.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	sp5.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	constraints5.fill=GridBagConstraints.BOTH;
	constraints5.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints5,0,0,5,1,100,99);
	gridBag5.setConstraints(sp5,constraints5);
	outerQIPane.add(sp5);
	qiFormatList=new JComboBox(RDFLoader.formatList);
	qiFormatList.setBorder(BorderFactory.createEmptyBorder());
	buildConstraints(constraints5,0,1,1,1,20,1);
	gridBag5.setConstraints(qiFormatList,constraints5);
	qiFormatList.addActionListener(this);
	outerQIPane.add(qiFormatList);
	loadQIBt=new JButton("Replace");
	loadQIBt.setBorder(BorderFactory.createEtchedBorder());
	buildConstraints(constraints5,1,1,1,1,20,0);
	gridBag5.setConstraints(loadQIBt,constraints5);
	loadQIBt.addActionListener(this);
	outerQIPane.add(loadQIBt);
	mergeQIBt=new JButton("Merge");
	mergeQIBt.setBorder(BorderFactory.createEtchedBorder());
	buildConstraints(constraints5,2,1,1,1,20,0);
	gridBag5.setConstraints(mergeQIBt,constraints5);
	mergeQIBt.addActionListener(this);
	outerQIPane.add(mergeQIBt);
	clearQIBt=new JButton("Clear");
	clearQIBt.setBorder(BorderFactory.createEtchedBorder());
	buildConstraints(constraints5,4,1,1,1,20,0);
	gridBag5.setConstraints(clearQIBt,constraints5);
	clearQIBt.addActionListener(this);
	outerQIPane.add(clearQIBt);
	tabbedPane.addTab("Quick Input",outerQIPane);

	// Fresnel input panel
	JPanel outerFresnelPane = new FresnelPanel(application);
	tabbedPane.addTab("Fresnel", outerFresnelPane);
	

	//FSL input panel
	JPanel outerFSLPane = new JPanel();
	GridBagLayout gridBag6 = new GridBagLayout();
	GridBagConstraints constraints6=new GridBagConstraints();
	outerFSLPane.setLayout(gridBag6);
	fslInputArea = new JTextField();
	constraints6.fill = GridBagConstraints.HORIZONTAL;
	constraints6.anchor = GridBagConstraints.SOUTH;
	buildConstraints(constraints6, 0, 0, 7, 1, 100, 50);
	gridBag6.setConstraints(fslInputArea, constraints6);
	outerFSLPane.add(fslInputArea);
	fslInputArea.addKeyListener(this);
	constraints6.anchor = GridBagConstraints.NORTH;
	fslResultLb = new JLabel("Enter an FSL expression");
	buildConstraints(constraints6, 0, 1, 7, 1, 100, 49);
	gridBag6.setConstraints(fslResultLb, constraints6);
	outerFSLPane.add(fslResultLb);
	constraints6.anchor = GridBagConstraints.CENTER;
	evalFSLBt = new JButton("Evaluate Expression");
	evalFSLBt.setBorder(BorderFactory.createEtchedBorder());
	buildConstraints(constraints6, 0, 2, 1, 1, 30, 1);
	gridBag6.setConstraints(evalFSLBt, constraints6);
	evalFSLBt.addActionListener(this);
	outerFSLPane.add(evalFSLBt);
	clearFSLBt = new JButton("Clear");
	clearFSLBt.setBorder(BorderFactory.createEtchedBorder());
	buildConstraints(constraints6, 1, 2, 1, 1, 30, 0);
	gridBag6.setConstraints(clearFSLBt, constraints6);
	clearFSLBt.addActionListener(this);
	outerFSLPane.add(clearFSLBt);
	constraints6.fill = GridBagConstraints.NONE;
	constraints6.anchor = GridBagConstraints.EAST;
	JLabel firstStepLb = new JLabel("First step:");
	buildConstraints(constraints6, 2, 2, 1, 1, 10, 0);
	gridBag6.setConstraints(firstStepLb, constraints6);
	outerFSLPane.add(firstStepLb);
	nodeStepBt = new JRadioButton("Node");
	buildConstraints(constraints6, 3, 2, 1, 1, 10, 0);
	gridBag6.setConstraints(nodeStepBt, constraints6);
	outerFSLPane.add(nodeStepBt);
	constraints6.anchor = GridBagConstraints.WEST;
	arcStepBt = new JRadioButton("Arc");
	buildConstraints(constraints6, 4, 2, 1, 1, 10, 0);
	gridBag6.setConstraints(arcStepBt, constraints6);
	outerFSLPane.add(arcStepBt);
	ButtonGroup bg89 = new ButtonGroup();
	bg89.add(nodeStepBt);
	bg89.add(arcStepBt);
	nodeStepBt.setSelected(true);
	loadRDFSCb = new JCheckBox("Load RDFS/OWL", false);
	buildConstraints(constraints6, 5, 2, 1, 1, 5, 0);
	gridBag6.setConstraints(loadRDFSCb, constraints6);
	outerFSLPane.add(loadRDFSCb);
	cfgRDFSBt = new JButton("RDFS/OWL settings...");
	buildConstraints(constraints6, 6, 2, 1, 1, 5, 0);
	gridBag6.setConstraints(cfgRDFSBt, constraints6);
	outerFSLPane.add(cfgRDFSBt);
	cfgRDFSBt.addActionListener(this);
	tabbedPane.addTab("FSL", outerFSLPane);
	
	// plugins appearing as tabs in this window
	initPlugins();
	//window
	Container cpane=this.getContentPane();
	cpane.add(tabbedPane);	
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){application.cmp.showTablesMn.setSelected(false);}
	    };
	this.addWindowListener(w0);
	this.setTitle("Definitions");
	this.pack();
	this.setLocation(x,y);
	this.setSize(width,height);
    }





    /**
     * Initializes all available plugins.
     **/
    void initPlugins(){
	//plugin classes
	Vector plgs=new Vector();
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
				    //find out if it implements TabPlugin
				    for (int j=0;j<interfaces.length;j++){
					if (interfaces[j].getName().equals("org.w3c.IsaViz.TabPlugin")){
					    plgInstance=c.newInstance();
					    plgs.add(plgInstance);
					}
				    }
				    plgInstance=null;
				}
				catch (InstantiationException ie) {
				    System.err.println("Unable to create plug-in object for class "+
						       s + ": " + ie.getMessage());
				    ie.printStackTrace();
				}
				catch (IllegalAccessException ie) {
				    System.err.println("Unable to create plug-in object for class "+
						       s + ": " + ie.getMessage());
				    ie.printStackTrace();
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
	//store the plugins in an array instead of a vector
	tabPlugins = new TabPlugin[plgs.size()];
	for (int i=0;i<plgs.size();i++){
	    tabPlugins[i]=(TabPlugin)plgs.elementAt(i);
	    tabPlugins[i].setIsaViz(application);
	    tabbedPane.addTab(tabPlugins[i].getTabLabel(), tabPlugins[i].getPluginGUI());
	}
    }

    void updatePropertyBrowser(INode n,boolean insert){
	rsPane.removeAll();
	if (n!=null && n instanceof IResource){
	    IResource r=(IResource)n;
	    if (insert){
		int index=Utils.getFirstEmptyIndex(browserList);
		if (index==-1){
		    Utils.eraseFirstAddNewElem(browserList,r);
		    brwIndex=browserList.length-1;
		}
		else {
		    browserList[index]=r;
		    brwIndex=index;
		}
	    }
	    String subjectLabel=r.getIdentity();
	    if (r.getLabel()!=null){subjectLabel+=" ("+r.getLabel()+")";}
	    resourceLb.setText(subjectLabel);
	    resourceLb.setForeground(ConfigManager.darkerPastelBlue);
	    Vector v;
	    if ((v=r.getOutgoingPredicates())!=null){
		GridBagLayout gridBag=new GridBagLayout();
		GridBagConstraints constraints=new GridBagConstraints();
		constraints.fill=GridBagConstraints.HORIZONTAL;
		constraints.anchor=GridBagConstraints.WEST;
		rsPane.setLayout(gridBag);
		int gridIndexH=0;
		int gridIndexV=0;
		int spanH=1;
		int spanV=1;
		int ratioH=50;
		int ratioV=100/v.size();
		IProperty p;
		String prefix;
		JLabel propertyLabel;
		Component objectComp;
		for (int j=0;j<v.size();j++){
		    p=(IProperty)v.elementAt(j);
		    prefix=application.getNSBinding(p.getNamespace());
		    propertyLabel=new JLabel(prefix!=null ? prefix+":"+p.getLocalname() : p.getIdent());
		    propertyLabel.setFont(Editor.swingFont);
		    buildConstraints(constraints,gridIndexH,gridIndexV,spanH,spanV,ratioH,1);
		    gridBag.setConstraints(propertyLabel,constraints);
		    rsPane.add(propertyLabel);
		    gridIndexH++;
		    objectComp=this.getSwingRepresentation(p.object);
		    objectComp.setFont(Editor.swingFont);
		    buildConstraints(constraints,gridIndexH,gridIndexV,spanH,spanV,ratioH,0);
		    gridBag.setConstraints(objectComp,constraints);
		    rsPane.add(objectComp);
		    gridIndexH=0;
		    gridIndexV++;
		}
		JLabel lb=new JLabel();//add a blank label to push all entries upward in the window (this label is not displayed
		buildConstraints(constraints,gridIndexH,gridIndexV,2,1,100,99);//even if we use a vertical scrollbar)
		gridBag.setConstraints(lb,constraints);
		rsPane.add(lb);
	    }
	    else {
		rsPane.add(new JLabel("No property is associated to this resource."));
	    }
	}
	else {resourceLb.setText("");}
	outerRsPane.paintAll(outerRsPane.getGraphics());
    }

    public void actionPerformed(ActionEvent e){
	Object src=e.getSource();
	if (src==addNSBt){
	    checkAndAddNS(nsPrefTf.getText(),nsURITf.getText());
	}
	else if (src==addPRBt){
	    checkAndAddProperty(nsPrpTf.getText(),lnPrpTf.getText());
	}
	else if (src==remNSBt){
	    int row;
	    if ((row=nsTable.getSelectedRow())!=-1){application.removeNamespaceBinding(row);}
	}
	else if (src==remPRBt){
	    int row;
	    if ((row=prTable.getSelectedRow())!=-1){application.removePropertyConstructor(row);}
	}
	else if (src==loadPRBt){
	    JFileChooser fc = new JFileChooser(Editor.lastImportRDFDir!=null ? Editor.lastImportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Load Properties from RDF/XML File");
	    int returnVal=fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		application.loadPropertyTypes(fc.getSelectedFile());
	    }
	}
	else if (src==ldStyleFromFileCb){
	    JFileChooser fc = new JFileChooser(GSSManager.lastStyleDir!=null ? GSSManager.lastStyleDir : Editor.rdfDir);
	    String format=(String)ldStyleFromFileCb.getSelectedItem();
	    if (format.equals(_rx)){
		fc.setDialogTitle("Load GSS Stylesheet (RDF/XML)");
		int returnVal=fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    application.gssMngr.loadStylesheet(fc.getSelectedFile(),RDFLoader.RDF_XML_READER);
		    ldStyleFromFileCb.setSelectedIndex(0);  //reset the combobox to display the main title ("Load Stylesheet...")
		}
	    }
	    else if (format.equals(_n3)){
		fc.setDialogTitle("Load GSS Stylesheet (Notation 3)");
		int returnVal=fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    application.gssMngr.loadStylesheet(fc.getSelectedFile(),RDFLoader.N3_READER);
		    ldStyleFromFileCb.setSelectedIndex(0);  //reset the combobox to display the main title ("Load Stylesheet...")
		}
	    }
	    else if (format.equals(_nt)){
		fc.setDialogTitle("Load GSS Stylesheet (N-Triples)");
		int returnVal=fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    application.gssMngr.loadStylesheet(fc.getSelectedFile(),RDFLoader.NTRIPLE_READER);
		    ldStyleFromFileCb.setSelectedIndex(0);  //reset the combobox to display the main title ("Load Stylesheet...")
		}
	    }
	    //else do nothing as the last option is the list's main title (no associated action)
	}
	else if (src==ldStyleFromURLCb){
	    String format=(String)ldStyleFromURLCb.getSelectedItem();
	    if (format.equals(_rx)){
		new URLPanel(application,"Specify Graph Stylesheet URL (RDF/XML):",RDFLoader.RDF_XML_READER,false,true);
		ldStyleFromURLCb.setSelectedIndex(0);  //reset the combobox to display the main title ("Load Stylesheet...")
	    }
	    else if (format.equals(_n3)){
		new URLPanel(application,"Specify Graph Stylesheet URL (Notation 3):",RDFLoader.N3_READER,false,true);
		ldStyleFromURLCb.setSelectedIndex(0);  //reset the combobox to display the main title ("Load Stylesheet...")
	    }
	    else if (format.equals(_nt)){
		new URLPanel(application,"Specify Graph Stylesheet URL (N-Triples):",RDFLoader.NTRIPLE_READER,false,true);
		ldStyleFromURLCb.setSelectedIndex(0);  //reset the combobox to display the main title ("Load Stylesheet...")
	    }
	    //else do nothing as the last option is the list's main title (no associated action)
	}
	else if (src==removeStyleBt){
	    application.gssMngr.removeSelectedStylesheet();
	}
	else if (src==editStyleBt){
	    int index=gssTable.getSelectedRow();
	    if (index>-1){
		application.gssMngr.editSelectedStylesheet(gssTable.getValueAt(index,0));
	    }
	}
	else if (src==applyStyleBt){
	    application.gssMngr.applyStylesheets();
	}
	else if (src==promoteSelStyleBt){
	    promoteSelectedStyle();
	}
	else if (src==demoteSelStyleBt){
	    demoteSelectedStyle();
	}
	else if (src==shIsolRsCb){
	    setShowIsolatedNodes(shIsolRsCb.isSelected());
	}
	else if (src==debugGssCb){
	    GraphStylesheet.DEBUG_GSS=debugGssCb.isSelected();
	}
	else if (src==loadQIBt){
	    this.loadRDFFromInputArea();
	}
	else if (src==mergeQIBt){
	    this.mergeRDFFromInputArea();
	}
	else if (src==clearQIBt){
	    this.clearRDFInputArea();
	}
	else if (src == evalFSLBt){
	    this.evaluateFSLExpression();
	}
	else if (src == cfgRDFSBt){
	    application.schemaMngr.setVisible(true);
	    application.schemaMngr.toFront();
	}
	else if (src == clearFSLBt){
	    this.clearFSLInputArea();
	}
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    Object src=e.getSource();
	    if ((src==addNSBt) || (src==nsPrefTf) || (src==nsURITf)){
		checkAndAddNS(nsPrefTf.getText(),nsURITf.getText());
	    }
	    else if ((src==addPRBt) || (src==nsPrpTf) || (src==lnPrpTf)){
		checkAndAddProperty(nsPrpTf.getText(),lnPrpTf.getText());
	    }
	    else if (src==remNSBt){
		int row;
		if ((row=nsTable.getSelectedRow())!=-1){application.removeNamespaceBinding(row);}
	    }
	    else if (src==remPRBt){
		int row;
		if ((row=prTable.getSelectedRow())!=-1){application.removePropertyConstructor(row);}
	    }
	    else if (src == fslInputArea){
		this.evaluateFSLExpression();
	    }
	}
    }

    public Component getSwingRepresentation(INode n){
	if (n instanceof IResource){
	    final IResource r=(IResource)n;
	    String s;
	    if (r.isAnon()){
		s="(AR) ";
		if (ConfigManager.SHOW_ANON_ID){s+=r.getIdentity();}
	    }
	    else {s="(R) "+r.getIdentity();}
	    final JLabel res=new JLabel(s);
	    MouseListener m1=new MouseAdapter(){
		    public void mousePressed(MouseEvent e){
			int whichBt=e.getModifiers();
			if ((whichBt & InputEvent.BUTTON1_MASK)==InputEvent.BUTTON1_MASK){
			    updatePropertyBrowser(r,true);
			}
			else if ((whichBt & InputEvent.BUTTON3_MASK)==InputEvent.BUTTON3_MASK){
			    Editor.vsm.centerOnGlyph(r.getGlyph(),Editor.vsm.getActiveCamera(),500);
			}
		    }
		    public void mouseReleased(MouseEvent e){}
		    public void mouseClicked(MouseEvent e){}
		    public void mouseEntered(MouseEvent e){res.setForeground(ConfigManager.darkerPastelBlue);}
		    public void mouseExited(MouseEvent e){res.setForeground(Color.black);}
		};
	    res.addMouseListener(m1);
	    return res;
	}
	else if (n instanceof ILiteral){
	    final ILiteral l=(ILiteral)n;
	    final JLabel res=new JLabel("(L) "+l.getText()+"...");
	    MouseListener m2=new MouseAdapter(){
		    public void mousePressed(MouseEvent e){
			int whichBt=e.getModifiers();
			if ((whichBt & InputEvent.BUTTON3_MASK)==InputEvent.BUTTON3_MASK){
			    Editor.vsm.centerOnGlyph(l.getGlyph(),Editor.vsm.getActiveCamera(),500);
			}
		    }
		    public void mouseReleased(MouseEvent e){}
		    public void mouseClicked(MouseEvent e){}
		    public void mouseEntered(MouseEvent e){res.setForeground(ConfigManager.darkerPastelBlue);}
		    public void mouseExited(MouseEvent e){res.setForeground(Color.black);}
		};
	    res.addMouseListener(m2);
	    return res;
	}
	else {
	    return new JLabel("Unknown kind of object - unable to display "+n.toString());
	}
    }

    public void mousePressed(MouseEvent e){
	int whichBt=e.getModifiers();
	if (e.getSource()==bckBt){//pressing Back button in property browser
	    if ((whichBt & InputEvent.BUTTON1_MASK)==InputEvent.BUTTON1_MASK){
		if (brwIndex>0){browserList[brwIndex]=null;brwIndex--;updatePropertyBrowser(browserList[brwIndex],false);}
	    }
	}
    }
    public void mouseReleased(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    //only JLabel serving as buttons are registered to this mouse listener - if this changes in the future, the automatic casting will not work
    public void mouseEntered(MouseEvent e){((JLabel)e.getSource()).setForeground(ConfigManager.darkerPastelBlue);}
    public void mouseExited(MouseEvent e){((JLabel)e.getSource()).setForeground(Color.black);}

    public void stateChanged(ChangeEvent e) {
	if (tabbedPane.getSelectedIndex()==2){
	    updatePropertyBrowser(Editor.lastSelectedItem,true);
	}
    }
    
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    void resetNamespaceTable(){
	for (int i=nsTableModel.getRowCount()-1;i>=0;i--){
	    nsTableModel.removeRow(i);
	}
    }

    void resetPropertyTable(){
	for (int i=((DefaultTableModel)prTable.getModel()).getRowCount()-1;i>=0;i--){
	    ((DefaultTableModel)prTable.getModel()).removeRow(i);
	}
    }
    
    void resetBrowser(){
	Utils.resetArray(browserList);
	updatePropertyBrowser(null,false);
    }

    void resetStylesheets(){
	for (int i=gssTableModel.getRowCount()-1;i>=0;i--){
	    gssTableModel.removeRow(i);
	}
    }

    void checkAndAddNS(String prefix,String uri){//prefix can be "" (no binding assigned), but URI has to be non-null
	if (uri.length()>0){
	    String prefix2="";
	    if (prefix.length()>0){//get rid of ':' if entered by user in the text field
		prefix2=prefix.endsWith(":") ? prefix.substring(0,prefix.length()-1) : prefix;
	    }
	    boolean success=application.addNamespaceBinding(prefix2,uri,new Boolean(false),false,false);
	    if (success){//empty fields only if the addition succeeded
		nsPrefTf.setText("");nsURITf.setText("");
		nsPrefTf.requestFocus();
		application.schemaMngr.updateNamespaces();
	    }
	}
    }

    void checkAndAddProperty(String ns,String ln){
	if (application.addPropertyType(ns,ln,false)){//empty fields only if the addition succeeded
	    nsPrpTf.setText("");lnPrpTf.setText("");
	    nsPrpTf.requestFocus();
	}
    }

    void addStylesheet(Object o){//either a java.io.File or a java.net.URL
	Vector nr=new Vector();
	nr.add(o);
	gssTableModel.addRow(nr);
    }

    void insertStylesheet(Object o, int index){//can be either a File or a URL
	Vector nr=new Vector();                //but in practive always a File for now
	nr.add(o);
	gssTableModel.insertRow(index, nr);
    }

    /*returns the java.io.File or a java.net.URL that has been deleted (null if nothing deleted)*/
    Object removeSelectedStylesheet(){
	Object res = null;
	int i = gssTable.getSelectedRow();
	if (i != -1){
	    removeStylesheet(i);
	}
	return res;
    }

    /*remove stylesheet at row i in the table*/
    void removeStylesheet(int i){
	gssTableModel.removeRow(i);
	if (gssTable.getRowCount() > i){gssTable.setRowSelectionInterval(i, i);}
	else if (gssTable.getRowCount() > 0){
	    int j = gssTable.getRowCount() - 1;
	    gssTable.setRowSelectionInterval(j, j);
	}
    }

    /*returns the list of stylesheet files (java.io.File or java.net.URL) in their order of application (empty vector if none)*/
    Vector getStylesheetList(){
	Vector res=new Vector();
	for (int i=0;i<gssTableModel.getRowCount();i++){
	    res.addElement(gssTableModel.getValueAt(i,0));
	}
	return res;
    }

    void promoteSelectedStyle(){//style will be applied one position upstream (later stylesheets have
	int i=gssTable.getSelectedRow();//a higher priority as they override previous rules in case of conflict)
	if (i!=-1 && i<gssTableModel.getRowCount()-1){//do not allow promote for last row
	    gssTableModel.moveRow(i,i,i+1);
	    gssTable.setRowSelectionInterval(i+1,i+1);
	}
    }

    void demoteSelectedStyle(){//style will be applied one position downstream (later stylesheets have
	int i=gssTable.getSelectedRow();//a higher priority as they override previous rules in case of conflict)
	if (i>0){//do not allow demote for first row
	    gssTableModel.moveRow(i,i,i-1);
	    gssTable.setRowSelectionInterval(i-1,i-1);
	}
    }

    void setShowIsolatedNodes(boolean b){
	SHOW_ISOLATED_NODES=b;
    }

    void loadRDFFromInputArea(){
	String s=rdfInputArea.getText();
	if (s!=null && s.length()>0){
	    try {
		InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
		String format=(String)qiFormatList.getSelectedItem();
		if (format.equals(RDFLoader.formatRDFXML)){
		    application.loadRDF(is,RDFLoader.RDF_XML_READER);
		}
		else if (format.equals(RDFLoader.formatNTRIPLES)){
		    application.loadRDF(is,RDFLoader.NTRIPLE_READER);
		}
		else if (format.equals(RDFLoader.formatN3)){
		    application.loadRDF(is,RDFLoader.N3_READER);
		}
	    }
	    catch (java.io.UnsupportedEncodingException ex){System.err.println("TablePanel.loadRDFFromInputArea:Error "+ex);ex.printStackTrace();}
	}
    }

    void mergeRDFFromInputArea(){
	String s=rdfInputArea.getText();
	if (s!=null && s.length()>0){
	    try {
		InputStream is=new ByteArrayInputStream(s.getBytes("UTF-8"));
		String format=(String)qiFormatList.getSelectedItem();
		if (format.equals(RDFLoader.formatRDFXML)){
		    application.mergeRDF(is,RDFLoader.RDF_XML_READER);
		}
		else if (format.equals(RDFLoader.formatNTRIPLES)){
		    application.mergeRDF(is,RDFLoader.NTRIPLE_READER);
		}
		else if (format.equals(RDFLoader.formatN3)){
		    application.mergeRDF(is,RDFLoader.N3_READER);
		}
	    }
	    catch (java.io.UnsupportedEncodingException ex){System.err.println("TablePanel.loadRDFFromInputArea:Error "+ex);ex.printStackTrace();}
	}
    }
    
    void clearRDFInputArea(){
	rdfInputArea.setText(null);
    }

    void evaluateFSLExpression(){
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    String expr = fslInputArea.getText();
		    if (expr != null && expr.length() > 0) {
			FSLNSResolver nsr = new FSLNSResolver();
			for (int i=0;i<nsTableModel.getRowCount();i++){
			    nsr.addPrefixBinding((String)nsTableModel.getValueAt(i,0),
						 (String)nsTableModel.getValueAt(i,1));
			}
			String schemaURI;
			if (TablePanel.this.loadRDFSCb.isSelected()){
			    for (int i=0;i<application.schemaMngr.schTableModel.getRowCount();i++){
				if (((Boolean)application.schemaMngr.schTableModel.getValueAt(i, 3)).booleanValue()){
				    schemaURI = (String)application.schemaMngr.schTableModel.getValueAt(i, 2);
				    if (schemaURI != null && schemaURI.length() > 0){
					if (schemaURI.endsWith("#")){schemaURI = schemaURI.substring(0, schemaURI.length()-1);}
					setFSLResultLabel("Attempting to retrieve RDFS/OWL information from "+schemaURI+"  ...");
					try {
					    application.fhs.addOntology(schemaURI, null);
					}
					catch (Exception ex){
					    System.err.println("Warning: failed to load RDFS/OWL class and property hierarchy data for namespace URI\n"+schemaURI);
					}
				    }
				}
			    }
			}
			FSLISVEvaluator fie = new FSLISVEvaluator(application, nsr, application.fhs);
			Vector pathInstances = fie.evaluate(expr, (arcStepBt.isSelected()) ? FSLPath.ARC_STEP : FSLPath.NODE_STEP);
			fslResultLb.setText("Found " + pathInstances.size() + " path(s)");
			//fie.printPaths(pathInstances);
			application.unselectAll();
			Vector path;
			Object step;
			for (int i=0;i<pathInstances.size();i++){
			    path = (Vector)pathInstances.elementAt(i);
			    for (int j=0;j<path.size();j++){
				step = path.elementAt(j);
				if (step instanceof IResource){
				    application.selectResource((IResource)step, true);
				}
				else if (step instanceof IProperty){
				    application.selectPredicate((IProperty)step, true);
				}
				else {
				    application.selectLiteral((ILiteral)step, true);
				}
			    }
			}
		    }
		    return null; 
		}
		};
	worker.start();
    }

    void clearFSLInputArea(){
	fslInputArea.setText(null);
    }
    
    void updateSwingFont(){
	resourceLb.setFont(Editor.swingFont);
	rdfInputArea.setFont(Editor.swingFont);
    }
    
    void setSTPBValue(int i){
	stpb.setPBValue(i);
    }

    void setFSLResultLabel(String s){
	fslResultLb.setText(s);
    }

    public static void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }


    TableModelListener l1=new TableModelListener(){//listener for the namespace table
	    public void tableChanged(TableModelEvent e){
		if (e.getType()!=TableModelEvent.DELETE){//can be INSERT or UPDATE
		    int row=e.getFirstRow();
		    int column=e.getColumn();
		    application.updateNamespaceBinding(row,column,(String)nsTableModel.getValueAt(row,0),(String)nsTableModel.getValueAt(row,1),(Boolean)nsTableModel.getValueAt(row,2),e.getType());
		}
		//do not do anything if DELETE (there's nothing to update - we are taking care of it in resetNamespaceDefinition() or removeNamespaceDefinition())
	    }
	};

    TableModelListener l2=new TableModelListener(){//listener for the property table
	    public void tableChanged(TableModelEvent e){
		if (e.getType()!=TableModelEvent.DELETE){//could also be INSERT or UPDATE
// 		    int row=e.getFirstRow();
// 		    int column=e.getColumn();
		    
		}
		//do not do anything is DELETE (there's nothing to update - we are taking care of it in resetNamespaceDefinition() or removeNamespaceDefinition())
	    }
	};

}
