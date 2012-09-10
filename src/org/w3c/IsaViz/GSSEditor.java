/*   FILE: GSSEditor.java
 *   DATE OF CREATION:   Thu Jul 24 14:17:24 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: GSSEditor.java,v 1.16 2006/05/11 07:45:01 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Enumeration;

import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.glyphs.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
// import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.RDFReader;
// import com.hp.hpl.jena.rdf.model.NsIterator;
// import com.hp.hpl.jena.datatypes.RDFDatatype;

public class GSSEditor extends JFrame implements ActionListener,KeyListener {

    static String GSS_MANUAL_URI="http://www.w3.org/2001/11/IsaViz/gss/gssmanual.html";
    static String GSS_EDITOR_MANUAL_URI="http://www.w3.org/2001/11/IsaViz/gss/gssmanual.html#frend";

    /*selector contraint labels*/
    public static String _uriswc="URI starts with";
    public static String _urieqc="URI equals";

    public static String _vleqc="value equals";
    public static String _dtc="datatype";

    public static String _sosc="subject of a statement";
    public static String _oosc="object of a statement";
    public static String _posc="predicate of a statement";

    public static String _wpc="whose predicate is";

    public static String _wovlc="whose object's value/URI is";
    public static String _wotc="whose object's type is";
    public static String _wodtc="whose object's datatype is";

    public static String _wsvlc="whose subject's URI is";
    public static String _wstc="whose subject's type is";

    public static String _sweight="Selector Weight";

    public static String _visvisible="Visible";
    public static String _vishidden="Hidden";
    public static String _dispnone="Not Displayed";

    public static String _nodearc="Node and Arc";
    public static String _table="Table";

    public static String _sortnm="Name";
    public static String _sortns="Namespace";
    public static String _sortnmr="Name (Reversed)";
    public static String _sortnsr="Namespace (Reversed)";
    public static String _enum="Enumeration...";
    public static String _selectEnum="Select an Enumeration ID";

    public static String _daSolid="Solid";
    public static String _daDashed="Dashed";
    public static String _daDotted="Dotted";
    public static String _daCustomPat="Custom Pattern...";

    public static String _taAbove="Above";
    public static String _taBelow="Below";
    public static String _taCenter="Center";
    public static String _taLeft="Left";
    public static String _taRight="Right";

    public static String _iconFile="File...";
    public static String _iconURI="URI...";
    public static String _iconFetchR="Fetch (Resources only)";
    public static String _iconFetch="Fetch";
//     public static String _iconNone="No Icon";

    public static String _ellipse="Ellipse";
    public static String _rectangle="Rectangle";
    public static String _circle="Circle";
    public static String _diamond="Diamond";
    public static String _octagon="Octagon";
    public static String _trianglen="Triangle North";
    public static String _triangles="Triangle South";
    public static String _trianglee="Triangle East";
    public static String _trianglew="Triangle West";
    public static String _customsh="Custom Shape...";
    public static String _custompg="Custom Polygon...";
    public static String _roundrect="Round Rectangle";

    protected static Vector glyphFactoryShapes;
    protected static double[] defaultShape={1.0,0.5,1.0,0.5,1.0,0.5,1.0,0.5};
    
    static {
	glyphFactoryShapes=new Vector();
	glyphFactoryShapes.add(net.claribole.zvtm.glyphs.GlyphFactory.V_Shape);
    }


    /*false if called from IsaViz, true if called directly from command line*/
    /*standalone=true will cause a System.exit(0) when the user closes the editor*/
    boolean standalone=true;
    Editor isv=null;  //non-null only if GSSEditor called from IsaViz

    JMenuItem resetMn,loadFileMn,loadURLMn,mergeFileMn,mergeURLMn,saveMn,saveAsMn,exitMn,umMn,gssumMn,aboutMn,baseURIMn;

    JLabel statusBar;

    JTabbedPane tabbedPane;

    JPanel rselPanel;
    JPanel lselPanel;
    JPanel pselPanel;
    JPanel stylePanel;
    JPanel sortPanel;

    JTable styleTable;
    JButton styleAddBt,styleRemoveBt;
    JTable sortTable;
    JButton sortAddBt,sortRemoveBt;

    JTable rselTable;
    JButton rselAddSBt,rselRemoveSBt,rselAddCBt,rselRemoveCBt;
    JTable lselTable;
    JButton lselAddSBt,lselRemoveSBt,lselAddCBt,lselRemoveCBt;
    JTable pselTable;
    JButton pselAddSBt,pselRemoveSBt,pselAddCBt,pselRemoveCBt;

    /*IMPORTANT NOTE: in all selector tables, we make the assumption that the first row of each selector contains the string pointed at by _sweight. Many things depend on this, including cell rendering attributes and code that inserts/deletes selectors and conditions. This is not very elegant, but it is quicker to implement and more efficient than having several data structures to keep track of what row contains what. As the whole code makes reference to _swkey, its value can be changed, but refs to _swkey should not be changed (unless you are willing to change the rendering and insert/delte code)*/

    TableCellRenderer str=new STableRenderer();
    TableCellRenderer sytr=new StyleTableRenderer();
    TableCellRenderer sotr=new SortTableRenderer();

    static Border selectedCellBorder=BorderFactory.createLineBorder(Color.black,2);
    static Border selectedRowBorder=BorderFactory.createLineBorder(ConfigManager.pastelBlue,1);

    /*points to the selector's first row for which we are currently selecting an enumeration ID (sortPropertiesBy), -1 if not selecting an ID*/
    int selectingEnumID=-1;

    File lastGSSFile=null;
    boolean reportError=false;

    String BASE_URI="";

    ProgressPanel pp;

    /*standalone=true will cause a System.exit(0) when the user closes the editor*/
    GSSEditor(Editor app){
	this.isv=app;
	if (this.isv!=null){standalone=false;}
	JMenuBar mnb=new JMenuBar();
	this.setJMenuBar(mnb);
	JMenu fileMenu=new JMenu("File");
	fileMenu.setMnemonic(KeyEvent.VK_F);
	mnb.add(fileMenu);
	resetMn=new JMenuItem("Reset");
	resetMn.addActionListener(this);
	resetMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
	fileMenu.add(resetMn);
	JMenu loadMenu=new JMenu("Load");
	JMenu mergeMenu=new JMenu("Merge");
	fileMenu.add(loadMenu);
	fileMenu.add(mergeMenu);
	loadFileMn=new JMenuItem("Stylesheet from File...");
	loadFileMn.addActionListener(this);
	loadFileMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	loadMenu.add(loadFileMn);
	loadURLMn=new JMenuItem("Stylesheet from URL...");
	loadURLMn.addActionListener(this);
	loadURLMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
	loadMenu.add(loadURLMn);
	mergeFileMn=new JMenuItem("Stylesheet from File...");
	mergeFileMn.addActionListener(this);
	mergeMenu.add(mergeFileMn);
	mergeURLMn=new JMenuItem("Stylesheet from URL...");
	mergeURLMn.addActionListener(this);
	mergeMenu.add(mergeURLMn);
	saveMn=new JMenuItem("Save Stylesheet");
	saveMn.addActionListener(this);
	saveMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	fileMenu.add(saveMn);
	saveAsMn=new JMenuItem("Save Stylesheet As...");
	saveAsMn.addActionListener(this);
	fileMenu.add(saveAsMn);
	fileMenu.addSeparator();
	exitMn=new JMenuItem("Close");
	exitMn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
	fileMenu.add(exitMn);
	exitMn.addActionListener(this);
	JMenu editMenu=new JMenu("Edit");
	mnb.add(editMenu);
	baseURIMn=new JMenuItem("Set Stylesheet Base URI...");
	baseURIMn.addActionListener(this);
	editMenu.add(baseURIMn);
	JMenu helpMenu=new JMenu("Help");
	mnb.add(helpMenu);
	gssumMn=new JMenuItem("GSS User Manual...");
	gssumMn.addActionListener(this);
	helpMenu.add(gssumMn);
	umMn=new JMenuItem("GSS Editor User Manual...");
	umMn.addActionListener(this);
	helpMenu.add(umMn);
	helpMenu.addSeparator();
	aboutMn=new JMenuItem("About...");
	aboutMn.addActionListener(this);
	helpMenu.add(aboutMn);
	tabbedPane=new JTabbedPane();
	//resource selectors panel
	rselPanel=new JPanel();
	GridBagLayout gridBag1=new GridBagLayout();
	GridBagConstraints constraints1=new GridBagConstraints();
	constraints1.fill=GridBagConstraints.HORIZONTAL;
	constraints1.anchor=GridBagConstraints.WEST;
	rselPanel.setLayout(gridBag1);
	JLabel rselTitle=new JLabel("Resource Selectors");
	rselTitle.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints1,0,0,1,1,20,5);
	gridBag1.setConstraints(rselTitle,constraints1);
	rselPanel.add(rselTitle);
	constraints1.fill=GridBagConstraints.NONE;
	constraints1.anchor=GridBagConstraints.CENTER;
	rselAddSBt=new JButton("Add Selector");
	buildConstraints(constraints1,1,0,1,1,20,0);
	gridBag1.setConstraints(rselAddSBt,constraints1);
	rselPanel.add(rselAddSBt);
	rselAddSBt.addActionListener(this);
	rselAddSBt.addKeyListener(this);
	rselRemoveSBt=new JButton("Remove Selector");
	buildConstraints(constraints1,2,0,1,1,20,0);
	gridBag1.setConstraints(rselRemoveSBt,constraints1);
	rselPanel.add(rselRemoveSBt);
	rselRemoveSBt.addActionListener(this);
	rselRemoveSBt.addKeyListener(this);
	rselAddCBt=new JButton("Add Condition to Current Selector");
	buildConstraints(constraints1,3,0,1,1,20,0);
	gridBag1.setConstraints(rselAddCBt,constraints1);
	rselPanel.add(rselAddCBt);
	rselAddCBt.addActionListener(this);
	rselAddCBt.addKeyListener(this);
	rselRemoveCBt=new JButton("Remove Condition");
	buildConstraints(constraints1,4,0,1,1,20,0);
	gridBag1.setConstraints(rselRemoveCBt,constraints1);
	rselPanel.add(rselRemoveCBt);
	rselRemoveCBt.addActionListener(this);
	rselRemoveCBt.addKeyListener(this);
	constraints1.fill=GridBagConstraints.BOTH;
	rselTable=initRSelTable();
	JScrollPane sp1=new JScrollPane(rselTable);
	buildConstraints(constraints1,0,1,5,1,100,95);
	gridBag1.setConstraints(sp1,constraints1);
	rselPanel.add(sp1);
	//sort panel
	sortPanel=new JPanel();
	GridBagLayout gridBag2=new GridBagLayout();
	GridBagConstraints constraints2=new GridBagConstraints();
	constraints2.fill=GridBagConstraints.HORIZONTAL;
	constraints2.anchor=GridBagConstraints.WEST;
	sortPanel.setLayout(gridBag2);
	JLabel sortTitle=new JLabel("Custom Enumerations (Sorting Properties)");
	sortTitle.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints2,0,0,1,1,80,5);
	gridBag2.setConstraints(sortTitle,constraints2);
	sortPanel.add(sortTitle);
	constraints2.fill=GridBagConstraints.NONE;
	constraints2.anchor=GridBagConstraints.CENTER;
	sortAddBt=new JButton("Add");
	buildConstraints(constraints2,1,0,1,1,10,0);
	gridBag2.setConstraints(sortAddBt,constraints2);
	sortPanel.add(sortAddBt);
	sortAddBt.addActionListener(this);
	sortAddBt.addKeyListener(this);
	sortRemoveBt=new JButton("Remove");
	buildConstraints(constraints2,2,0,1,1,10,0);
	gridBag2.setConstraints(sortRemoveBt,constraints2);
	sortPanel.add(sortRemoveBt);
	sortRemoveBt.addActionListener(this);
	sortRemoveBt.addKeyListener(this);
	constraints2.fill=GridBagConstraints.BOTH;
	sortTable=initSortTable();
	JScrollPane sp2=new JScrollPane(sortTable);
	buildConstraints(constraints2,0,1,3,1,100,95);
	gridBag2.setConstraints(sp2,constraints2);
	sortPanel.add(sp2);
	JSplitPane rselSortSplit=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,rselPanel,sortPanel);
	rselSortSplit.setOneTouchExpandable(true);
	tabbedPane.add("Resources",rselSortSplit);
	//literal selectors panel
	lselPanel=new JPanel();
	GridBagLayout gridBag3=new GridBagLayout();
	GridBagConstraints constraints3=new GridBagConstraints();
	constraints3.fill=GridBagConstraints.HORIZONTAL;
	constraints3.anchor=GridBagConstraints.WEST;
	lselPanel.setLayout(gridBag3);
	JLabel lselTitle=new JLabel("Literal Selectors");
	lselTitle.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints3,0,0,1,1,20,5);
	gridBag3.setConstraints(lselTitle,constraints3);
	lselPanel.add(lselTitle);
	constraints3.fill=GridBagConstraints.NONE;
	constraints3.anchor=GridBagConstraints.CENTER;
	lselAddSBt=new JButton("Add Selector");
	buildConstraints(constraints3,1,0,1,1,20,0);
	gridBag3.setConstraints(lselAddSBt,constraints3);
	lselPanel.add(lselAddSBt);
	lselAddSBt.addActionListener(this);
	lselAddSBt.addKeyListener(this);
	lselRemoveSBt=new JButton("Remove Selector");
	buildConstraints(constraints3,2,0,1,1,20,0);
	gridBag3.setConstraints(lselRemoveSBt,constraints3);
	lselPanel.add(lselRemoveSBt);
	lselRemoveSBt.addActionListener(this);
	lselRemoveSBt.addKeyListener(this);
	lselAddCBt=new JButton("Add Condition to Current Selector");
	buildConstraints(constraints3,3,0,1,1,20,0);
	gridBag3.setConstraints(lselAddCBt,constraints3);
	lselPanel.add(lselAddCBt);
	lselAddCBt.addActionListener(this);
	lselAddCBt.addKeyListener(this);
	lselRemoveCBt=new JButton("Remove Condition");
	buildConstraints(constraints3,4,0,1,1,20,0);
	gridBag3.setConstraints(lselRemoveCBt,constraints3);
	lselPanel.add(lselRemoveCBt);
	lselRemoveCBt.addActionListener(this);
	lselRemoveCBt.addKeyListener(this);
	constraints3.fill=GridBagConstraints.BOTH;
	lselTable=initLSelTable();
	JScrollPane sp3=new JScrollPane(lselTable);
	buildConstraints(constraints3,0,1,5,1,100,95);
	gridBag3.setConstraints(sp3,constraints3);
	lselPanel.add(sp3);
	tabbedPane.add("Literals",lselPanel);
	//property selectors panel
	pselPanel=new JPanel();
	GridBagLayout gridBag4=new GridBagLayout();
	GridBagConstraints constraints4=new GridBagConstraints();
	constraints4.fill=GridBagConstraints.HORIZONTAL;
	constraints4.anchor=GridBagConstraints.WEST;
	pselPanel.setLayout(gridBag4);
	JLabel pselTitle=new JLabel("Property Selectors");
	pselTitle.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints4,0,0,1,1,20,5);
	gridBag4.setConstraints(pselTitle,constraints4);
	pselPanel.add(pselTitle);
	constraints4.fill=GridBagConstraints.NONE;
	constraints4.anchor=GridBagConstraints.CENTER;
	pselAddSBt=new JButton("Add Selector");
	buildConstraints(constraints4,1,0,1,1,20,0);
	gridBag4.setConstraints(pselAddSBt,constraints4);
	pselPanel.add(pselAddSBt);
	pselAddSBt.addActionListener(this);
	pselAddSBt.addKeyListener(this);
	pselRemoveSBt=new JButton("Remove Selector");
	buildConstraints(constraints4,2,0,1,1,20,0);
	gridBag4.setConstraints(pselRemoveSBt,constraints4);
	pselPanel.add(pselRemoveSBt);
	pselRemoveSBt.addActionListener(this);
	pselRemoveSBt.addKeyListener(this);
	pselAddCBt=new JButton("Add Condition to Current Selector");
	buildConstraints(constraints4,3,0,1,1,20,0);
	gridBag4.setConstraints(pselAddCBt,constraints4);
	pselPanel.add(pselAddCBt);
	pselAddCBt.addActionListener(this);
	pselAddCBt.addKeyListener(this);
	pselRemoveCBt=new JButton("Remove Condition");
	buildConstraints(constraints4,4,0,1,1,20,0);
	gridBag4.setConstraints(pselRemoveCBt,constraints4);
	pselPanel.add(pselRemoveCBt);
	pselRemoveCBt.addActionListener(this);
	pselRemoveCBt.addKeyListener(this);
	constraints4.fill=GridBagConstraints.BOTH;
	pselTable=initPSelTable();
	JScrollPane sp4=new JScrollPane(pselTable);
	buildConstraints(constraints4,0,1,5,1,100,95);
	gridBag4.setConstraints(sp4,constraints4);
	pselPanel.add(sp4);
	tabbedPane.add("Properties",pselPanel);
	//style panel
	stylePanel=new JPanel();
	GridBagLayout gridBag5=new GridBagLayout();
	GridBagConstraints constraints5=new GridBagConstraints();
	constraints5.fill=GridBagConstraints.HORIZONTAL;
	constraints5.anchor=GridBagConstraints.WEST;
	stylePanel.setLayout(gridBag5);
	JLabel styleTitle=new JLabel("Styles");
	styleTitle.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints5,0,0,1,1,80,5);
	gridBag5.setConstraints(styleTitle,constraints5);
	stylePanel.add(styleTitle);
	constraints5.fill=GridBagConstraints.NONE;
	constraints5.anchor=GridBagConstraints.CENTER;
	styleAddBt=new JButton("Add");
	buildConstraints(constraints5,1,0,1,1,10,0);
	gridBag5.setConstraints(styleAddBt,constraints5);
	stylePanel.add(styleAddBt);
	styleAddBt.addActionListener(this);
	styleAddBt.addKeyListener(this);
	styleRemoveBt=new JButton("Remove");
	buildConstraints(constraints5,2,0,1,1,10,0);
	gridBag5.setConstraints(styleRemoveBt,constraints5);
	stylePanel.add(styleRemoveBt);
	styleRemoveBt.addActionListener(this);
	styleRemoveBt.addKeyListener(this);
	constraints5.fill=GridBagConstraints.BOTH;
	styleTable=initStyleTable();
	JScrollPane sp5=new JScrollPane(styleTable);
	buildConstraints(constraints5,0,1,3,1,100,95);
	gridBag5.setConstraints(sp5,constraints5);
	stylePanel.add(sp5);
	JSplitPane selStyleSplit=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,tabbedPane,stylePanel);
	selStyleSplit.setOneTouchExpandable(true);

// 	this.getContentPane().add(selStyleSplit);

	Container mainPanel=this.getContentPane();
	GridBagLayout gridBag0=new GridBagLayout();
	GridBagConstraints constraints0=new GridBagConstraints();
	constraints0.fill=GridBagConstraints.BOTH;
	constraints0.anchor=GridBagConstraints.CENTER;
	mainPanel.setLayout(gridBag0);
	buildConstraints(constraints0,0,0,1,1,100,99);
	gridBag0.setConstraints(selStyleSplit,constraints0);
	mainPanel.add(selStyleSplit);
	constraints0.fill=GridBagConstraints.HORIZONTAL;
	constraints0.anchor=GridBagConstraints.CENTER;
	JPanel statusPanel=new JPanel();
	statusPanel.setLayout(new GridLayout(1,2));
	statusBar=new JLabel(" ");
	statusPanel.add(statusBar);
	pp=new ProgressPanel();
	pp.setForegroundColor(ConfigManager.pastelBlue);
	statusPanel.add(pp);
	buildConstraints(constraints0,0,1,1,1,100,1);
	gridBag0.setConstraints(statusPanel,constraints0);
	mainPanel.add(statusPanel);
	tabbedPane.setSelectedIndex(0);
	//window
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){closeEditor();}
	    };
	this.addWindowListener(w0);
	this.setTitle("GSS Editor");
	this.pack();
	this.setLocation(0,0);
	this.setSize(Toolkit.getDefaultToolkit().getScreenSize().width,600);
	this.setVisible(true);
	guiPostProcessing();
	selStyleSplit.setDividerLocation(0.7);
	rselSortSplit.setDividerLocation(0.8);
	this.repaint(); //necessary to have above split bar positions updated
    }

    private void guiPostProcessing(){
	//adjust sort tabel column widths
	TableColumn tc=sortTable.getColumnModel().getColumn(0);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=sortTable.getColumnModel().getColumn(1);
	tc.setPreferredWidth(Math.round(getWidth()/100*80));
	//adjust resource selector tabel column widths
	tc=rselTable.getColumnModel().getColumn(0);
	tc.setPreferredWidth(Math.round(getWidth()/100*15));
	tc=rselTable.getColumnModel().getColumn(1);
	tc.setPreferredWidth(Math.round(getWidth()/100*15));
	tc=rselTable.getColumnModel().getColumn(2);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=rselTable.getColumnModel().getColumn(3);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=rselTable.getColumnModel().getColumn(4);
	tc.setPreferredWidth(Math.round(getWidth()/100*10));
	tc=rselTable.getColumnModel().getColumn(5);
	tc.setPreferredWidth(Math.round(getWidth()/100*10));
	tc=rselTable.getColumnModel().getColumn(6);
	tc.setPreferredWidth(Math.round(getWidth()/100*10));
	//adjust literal selector tabel column widths
	tc=lselTable.getColumnModel().getColumn(0);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=lselTable.getColumnModel().getColumn(1);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=lselTable.getColumnModel().getColumn(2);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=lselTable.getColumnModel().getColumn(3);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=lselTable.getColumnModel().getColumn(4);
	tc.setPreferredWidth(Math.round(getWidth()/100*10));
	tc=lselTable.getColumnModel().getColumn(5);
	tc.setPreferredWidth(Math.round(getWidth()/100*10));
	//adjust property selector tabel column widths
	tc=pselTable.getColumnModel().getColumn(0);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=pselTable.getColumnModel().getColumn(1);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=pselTable.getColumnModel().getColumn(2);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=pselTable.getColumnModel().getColumn(3);
	tc.setPreferredWidth(Math.round(getWidth()/100*20));
	tc=pselTable.getColumnModel().getColumn(4);
	tc.setPreferredWidth(Math.round(getWidth()/100*10));
	tc=pselTable.getColumnModel().getColumn(5);
	tc.setPreferredWidth(Math.round(getWidth()/100*10));
	rselTable.setDefaultRenderer((new String()).getClass(),str);
	lselTable.setDefaultRenderer((new String()).getClass(),str);
	pselTable.setDefaultRenderer((new String()).getClass(),str);
	styleTable.setDefaultRenderer((new String()).getClass(),sytr);
	sortTable.setDefaultRenderer((new String()).getClass(),sotr);
    }

    private JTable initRSelTable(){
	RSTableModel tm=new RSTableModel(0,7);
	JTable res=new JTable(tm);
	tm.setTable(res);
	res.setCellSelectionEnabled(true);
	TableColumn tc=res.getColumnModel().getColumn(0);
	tc.setHeaderValue("Condition");
	tc.setCellRenderer(str);
        JComboBox cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_urieqc);
	cb.addItem(_uriswc);
	cb.addItem(_sosc);
	cb.addItem(_oosc);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(1);
	tc.setHeaderValue("Subcondition");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(GSSEditor._wpc);
	cb.addItem(GSSEditor._wovlc);
	cb.addItem(GSSEditor._wotc);
	cb.addItem(GSSEditor._wodtc);
	cb.addItem(GSSEditor._wsvlc);
	cb.addItem(GSSEditor._wstc);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(2);
	tc.setHeaderValue("Value");
	tc.setCellRenderer(str);
	tc=res.getColumnModel().getColumn(3);
	tc.setHeaderValue("Styles (Comma-separated)");
	tc.setCellRenderer(str);
	tc=res.getColumnModel().getColumn(4);
	tc.setHeaderValue("Visibility");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_dispnone);
	cb.addItem(_vishidden);
	cb.addItem(_visvisible);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(5);
	tc.setHeaderValue("Layout");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_nodearc);
	cb.addItem(_table);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(6);
	tc.setHeaderValue("Sort Properties By");
	tc.setCellRenderer(str);
	cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_sortnm);
	cb.addItem(_sortns);
	cb.addItem(_sortnmr);
	cb.addItem(_sortnsr);
	cb.addItem(_enum);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tm.addTableModelListener(l1);
	res.addMouseListener(m1);
	return res;
    }

    private JTable initLSelTable(){
	LSTableModel tm=new LSTableModel(0,6);
	JTable res=new JTable(tm);
	tm.setTable(res);
	res.setCellSelectionEnabled(true);
	TableColumn tc=res.getColumnModel().getColumn(0);
	tc.setHeaderValue("Condition");
	tc.setCellRenderer(str);
        JComboBox cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_vleqc);
	cb.addItem(_dtc);
	cb.addItem(_oosc);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(1);
	tc.setHeaderValue("Subcondition");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_wpc);
	cb.addItem(_wsvlc);
	cb.addItem(_wstc);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(2);
	tc.setHeaderValue("Value");
	tc.setCellRenderer(str);
	tc=res.getColumnModel().getColumn(3);
	tc.setHeaderValue("Styles (Comma-separated)");
	tc.setCellRenderer(str);
	tc=res.getColumnModel().getColumn(4);
	tc.setHeaderValue("Visibility");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_dispnone);
	cb.addItem(_vishidden);
	cb.addItem(_visvisible);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(5);
	tc.setHeaderValue("Layout");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_nodearc);
	cb.addItem(_table);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tm.addTableModelListener(l2);
	res.addMouseListener(m1);
	return res;
    }

    private JTable initPSelTable(){
	PSTableModel tm=new PSTableModel(0,6);
	JTable res=new JTable(tm);
	tm.setTable(res);
	res.setCellSelectionEnabled(true);
	TableColumn tc=res.getColumnModel().getColumn(0);
	tc.setHeaderValue("Condition");
	tc.setCellRenderer(str);
        JComboBox cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_urieqc);
	cb.addItem(_uriswc);
	cb.addItem(_posc);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(1);
	tc.setHeaderValue("Subcondition");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_wovlc);
	cb.addItem(_wotc);
	cb.addItem(_wodtc);
	cb.addItem(_wsvlc);
	cb.addItem(_wstc);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(2);
	tc.setHeaderValue("Value");
	tc.setCellRenderer(str);
	tc=res.getColumnModel().getColumn(3);
	tc.setHeaderValue("Styles (Comma-separated)");
	tc.setCellRenderer(str);
	tc=res.getColumnModel().getColumn(4);
	tc.setHeaderValue("Visibility");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_dispnone);
	cb.addItem(_vishidden);
	cb.addItem(_visvisible);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(5);
	tc.setHeaderValue("Layout");
	tc.setCellRenderer(str);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_nodearc);
	cb.addItem(_table);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tm.addTableModelListener(l3);
	res.addMouseListener(m1);
	return res;
    }

    private JTable initStyleTable(){
	TableModel tm=new StyleTableModel(0,12);
	JTable res=new JTable(tm);
	res.setCellSelectionEnabled(true);
	TableColumn tc=res.getColumnModel().getColumn(0);
	tc.setHeaderValue("ID");
	tc.setCellRenderer(sytr);
	tc=res.getColumnModel().getColumn(1);
	tc.setHeaderValue("fill");
	tc.setCellRenderer(sytr);
	tc=res.getColumnModel().getColumn(2);
	tc.setHeaderValue("stroke");
	tc.setCellRenderer(sytr);
	tc=res.getColumnModel().getColumn(3);
	tc.setHeaderValue("stroke-width");
	tc.setCellRenderer(sytr);
	tc=res.getColumnModel().getColumn(4);
	tc.setHeaderValue("stroke-dasharray");
	tc.setCellRenderer(sytr);
        JComboBox cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_daSolid);
	cb.addItem(_daDashed);
	cb.addItem(_daDotted);
	cb.addItem(_daCustomPat);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(5);
	tc.setHeaderValue("shape");
	tc.setCellRenderer(sytr);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_ellipse);
	cb.addItem(_rectangle);
	cb.addItem(_roundrect);
	cb.addItem(_customsh);
	cb.addItem(_custompg);
	cb.addItem(_circle);
	cb.addItem(_diamond);
	cb.addItem(_octagon);
	cb.addItem(_trianglen);
	cb.addItem(_triangles);
	cb.addItem(_trianglee);
	cb.addItem(_trianglew);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(6);
	tc.setHeaderValue("icon");
	tc.setCellRenderer(sytr);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_iconFile);
	cb.addItem(_iconURI);
	cb.addItem(_iconFetchR);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(7);
	tc.setHeaderValue("text-align");
	tc.setCellRenderer(sytr);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem(_taAbove);
	cb.addItem(_taBelow);
	cb.addItem(_taCenter);
	cb.addItem(_taLeft);
	cb.addItem(_taRight);
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(8);
	tc.setHeaderValue("f-family");
	tc.setCellRenderer(sytr);
	tc=res.getColumnModel().getColumn(9);
	tc.setHeaderValue("f-size");
	tc.setCellRenderer(sytr);
	tc=res.getColumnModel().getColumn(10);
	tc.setHeaderValue("f-weight");
	tc.setCellRenderer(sytr);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem("normal");
	cb.addItem("bold");
	cb.addItem("100");
	cb.addItem("200");
	cb.addItem("300");
	cb.addItem("400");
	cb.addItem("500");
	cb.addItem("600");
	cb.addItem("800");
	cb.addItem("900");
	tc.setCellEditor(new DefaultCellEditor(cb));
	tc=res.getColumnModel().getColumn(11);
	tc.setHeaderValue("f-style");
	tc.setCellRenderer(sytr);
        cb=new JComboBox();
	cb.addItem("");
	cb.addItem("normal");
	cb.addItem("italic");
	cb.addItem("oblique");
	tc.setCellEditor(new DefaultCellEditor(cb));
	tm.addTableModelListener(l4);
	res.addMouseListener(m1);
	return res;
    }

    private JTable initSortTable(){
	TableModel tm=new SortTableModel(0,2);
	JTable res=new JTable(tm);
	res.setCellSelectionEnabled(true);
	TableColumn tc=res.getColumnModel().getColumn(0);
	tc.setHeaderValue("ID");
	tc.setCellRenderer(sotr);
	tc=res.getColumnModel().getColumn(1);
	tc.setHeaderValue("Enumeration (Comma-separated list)");
	tc.setCellRenderer(sotr);
	res.addMouseListener(m1);
	return res;
    }

    protected void addSelector(JTable table,boolean addBlankCondition){
	Vector v=new Vector();
	v.add(_sweight);
	((DefaultTableModel)table.getModel()).addRow(v);  //separator row (between selectors)
	if (addBlankCondition){((DefaultTableModel)table.getModel()).addRow(new Vector());}  //a blank row for entering a condition for this new selector
    }

    protected void addConditionToSelector(JTable table){
	addConditionToSelector(table,table.getRowCount());
    }

    protected void addConditionToSelector(JTable table,int row){
	if (isFirstSelectorRow(table,row)){
	    //get the last condition's index for this selector
	    int insertIndex=getSelectorLastRowIndex(table,table.getSelectedRow());
	    //insert a blank row for entering a condition for this new selector just after this index
	    if (insertIndex==table.getRowCount()){((DefaultTableModel)table.getModel()).addRow(new Vector());}
	    else if (insertIndex>-1){((DefaultTableModel)table.getModel()).insertRow(insertIndex+1,new Vector());}
	}
	else if (row==-1){//only add if there is something in the table (otherwise it makes it possible to add conditions on their own, in no selector
	    if (table.getRowCount()>0){((DefaultTableModel)table.getModel()).addRow(new Vector());}
	}
	else {
	    if (row==table.getRowCount()){((DefaultTableModel)table.getModel()).addRow(new Vector());}
	    else if (row>-1){((DefaultTableModel)table.getModel()).insertRow(row+1,new Vector());}
	}
    }

    /*removes an entire selector (selector weight row and all condition rows)*/
    protected void removeSelector(JTable table,int row){
	if (row>-1){
	    int begin=getSelectorFirstRowIndex(table,row);
	    int end=getSelectorLastRowIndex(table,row);
	    DefaultTableModel dtm=(DefaultTableModel)table.getModel();
	    for (int i=end;i>=begin;i--){
		dtm.removeRow(i);
	    }
	    if (table.getRowCount()>row){table.setRowSelectionInterval(row,row);}
	    else if (table.getRowCount()>0 && row>0){table.setRowSelectionInterval(begin-1,begin-1);}
	}
    }

    /*removes the currently selected condition in a selector*/
    protected void removeConditionFromSelector(JTable table,int row){
	if (row>-1 && !isFirstSelectorRow(table,row)){
	    ((DefaultTableModel)table.getModel()).removeRow(row);
	    if (table.getRowCount()>row){table.setRowSelectionInterval(row,row);}
	    else if (row>0){table.setRowSelectionInterval(row-1,row-1);}
	}
    }

    protected void addStyle(){
	((DefaultTableModel)styleTable.getModel()).addRow(new Vector());
    }

    protected void addSort(){
	((DefaultTableModel)sortTable.getModel()).addRow(new Vector());
    }

    protected void removeStyle(int row){
	if (row>-1){
	    ((DefaultTableModel)styleTable.getModel()).removeRow(row);
	    if (styleTable.getRowCount()>row){styleTable.setRowSelectionInterval(row,row);}
	    else if (row>0){styleTable.setRowSelectionInterval(row-1,row-1);}
	}
    }

    protected void removeSort(int row){
	if (row>-1){
	    ((DefaultTableModel)sortTable.getModel()).removeRow(row);
	    if (sortTable.getRowCount()>row){sortTable.setRowSelectionInterval(row,row);}
	    else if (row>0){sortTable.setRowSelectionInterval(row-1,row-1);}
	}
    }

    /*returns the index of the first row of the selector to which the current selected cell belongs to*/
    protected static int getSelectorFirstRowIndex(JTable table,int currentRowIndex){
	int res=-1;
	if (currentRowIndex>-1){
	    int index=currentRowIndex;
	    boolean isSelectorFirstRow=false;
	    while (index>=0){
		isSelectorFirstRow=isFirstSelectorRow(table,index);
		if (isSelectorFirstRow){
		    return index;
		}
		index--;
	    }
	}
	return res;
    }

    /*returns the index of the last row of the selector to which the current selected cell belongs to*/
    protected static int getSelectorLastRowIndex(JTable table,int currentRowIndex){
	if (currentRowIndex>-1){
	    int index=currentRowIndex;
	    boolean isSelectorFirstRow=isFirstSelectorRow(table,currentRowIndex);
	    //if the currently selected row is the selector's first row, we have to ignore it
	    if (isSelectorFirstRow){index++;}
	    while (index<table.getRowCount()){
		isSelectorFirstRow=isFirstSelectorRow(table,index);
		if (isSelectorFirstRow){
		    return index-1;
		}
		index++;
	    }
	}
	//will return -1 is the table is empty
	return table.getRowCount()-1;
    }

    protected static boolean isFirstSelectorRow(JTable table,int row){
	if (row>-1 && table.getRowCount()>row){
	    return ((table.getValueAt(row,0)!=null) ? ((String)table.getValueAt(row,0)).equals(GSSEditor._sweight) : false);
	}
	else return false;
    }

    protected static String getParentCnstrnt(JTable table,int row){
	String res=null;
	int index=row-1;
	String cnstrnt;
	while (index>=0){
	    cnstrnt=(String)table.getValueAt(index,0);
	    if (cnstrnt!=null){
		if (cnstrnt.equals(GSSEditor._sweight)){return null;}
		else {return cnstrnt;}
	    }
	    index--;
	}
	return res;
    }

    static final String resetWarning="You are about to erase all rules in this stylesheet.\nAre you sure you want to continue?";

    /*GUI warning before reset*/
    void promptReset(){
	Object[] options={"Yes","No"};
	int option=JOptionPane.showOptionDialog(null,GSSEditor.resetWarning,"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
	if (option==JOptionPane.OK_OPTION){this.reset();}
    }

    void reset(){
	int i;
	while ((i=sortTable.getRowCount())>0){
	    removeSort(i-1);
	}
	while ((i=styleTable.getRowCount())>0){
	    removeStyle(i-1);
	}
	while ((i=rselTable.getRowCount())>0){
	    removeSelector(rselTable,i-1);
	}
	while ((i=lselTable.getRowCount())>0){
	    removeSelector(lselTable,i-1);
	}
	while ((i=pselTable.getRowCount())>0){
	    removeSelector(pselTable,i-1);
	}
	lastGSSFile=null;
    }

    void loadFile(final boolean merge){
	final JFileChooser fc=new JFileChooser((lastGSSFile!=null) ? lastGSSFile.getParentFile() : new File("."));
	fc.setDialogTitle("Load Local Stylesheet");
	int returnVal=fc.showOpenDialog(this);
	if (returnVal==JFileChooser.APPROVE_OPTION){
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			loadStylesheet(fc.getSelectedFile(),merge);
			return null; 
		    }
		};
	    worker.start();
	}
    }

    void loadURL(final boolean merge){
	String uri=JOptionPane.showInputDialog(this,"Load Stylesheet:","Enter the stylesheet's URL",JOptionPane.PLAIN_MESSAGE);
	final String finaluri=(uri!=null) ? uri.trim() : null;
	if (finaluri!=null){
	    if (finaluri.length()>0){
		final SwingWorker worker=new SwingWorker(){
			public Object construct(){
			    try {
				URL url=new URL(finaluri);
				BASE_URI=finaluri;
				loadStylesheet(url,merge);
			    }
			    catch(MalformedURLException mue){JOptionPane.showMessageDialog(GSSEditor.this,finaluri+"\nis not a well-formed URL","Error",JOptionPane.ERROR_MESSAGE);}
			    return null; 
			}
		};
		worker.start();
	    }
	}
    }

    void loadStylesheet(File f,boolean merge){
	if (f.exists()){
	    if (merge){displayMsg("Merging "+f.toString()+" with current stylesheet (Warning: NO CHECK FOR CONFLICT)...");}
	    else {reset();displayMsg("Loading "+f.toString()+" ...");}
	    pp.setPBValue(0);
	    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
	    EditableStylesheet gss=new EditableStylesheet();
	    pp.setPBValue(5);
	    gss.load(f,this);
	    lastGSSFile=f;
	    pp.setPBValue(40);
	    fillTables(gss);
	    this.setCursor(java.awt.Cursor.getDefaultCursor());
	    if (merge){displayMsg("Merging "+f.toString()+" with current stylesheet (Warning: NO CHECK FOR CONFLICT)... done");}
	    else {displayMsg("Loading "+f.toString()+" ... done");}
	    pp.setPBValue(100);
	    if (reportError){displayError("There were error/warning messages (See Standard Error Output Stream)");reportError=false;}
	}
    }

    void loadStylesheet(URL url,boolean merge){
	if (merge){displayMsg("Merging "+url.toString()+" with current stylesheet (Warning: NO CHECK FOR CONFLICT)...");}
	else {reset();displayMsg("Loading "+url.toString()+" ...");}
	pp.setPBValue(0);
	this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
	EditableStylesheet gss=new EditableStylesheet();
	pp.setPBValue(5);
	gss.load(url,this);
	pp.setPBValue(40);
	fillTables(gss);
	this.setCursor(java.awt.Cursor.getDefaultCursor());
	if (merge){displayMsg("Merging "+url.toString()+" with current stylesheet (Warning: NO CHECK FOR CONFLICT)... done");}
	else {displayMsg("Loading "+url.toString()+" ... done");}
	pp.setPBValue(100);
	if (reportError){displayError("There were error/warning messages (See Standard Error Output Stream)");reportError=false;}
    }

    /*given a fully built Editable Stylesheet, fill editor's tables with selectors, styles and sort enums*/
    void fillTables(EditableStylesheet es){
	//styles
	String id;
	Style s;
	int index=0;
	String tmpS;
	short tmpSH;
	int tmpI;
	URL tmpU;
	pp.setPBValue(45);
	for (Enumeration e=es.styles.keys();e.hasMoreElements();){
	    id=(String)e.nextElement();
	    s=(Style)es.styles.get(id);
	    addStyle();
	    index=styleTable.getRowCount()-1;
	    if (id.startsWith("#")){id=id.substring(1);}
	    else if ((tmpI=id.lastIndexOf("#"))!=-1){id=id.substring(tmpI+1);}
	    styleTable.setValueAt(id,index,0);
	    if (s.getFill()!=null){
		tmpS="rgb("+s.getFill().getRed()+","+s.getFill().getGreen()+","+s.getFill().getBlue()+")";
		styleTable.setValueAt(tmpS,index,1);
	    }
	    if (s.getStroke()!=null){
		tmpS="rgb("+s.getStroke().getRed()+","+s.getStroke().getGreen()+","+s.getStroke().getBlue()+")";
		styleTable.setValueAt(tmpS,index,2);
	    }
	    if (s.getStrokeWidth()!=null){
		styleTable.setValueAt(s.getStrokeWidth().toString(),index,3);
	    }
	    if (s.getStrokeDashArray()!=null){
		if (s.getStrokeDashArray().length!=0){
		    styleTable.setValueAt(Utils.arrayOffloatAsCSStrings(s.getStrokeDashArray()),index,4);
		}
		else {
		    styleTable.setValueAt(_daSolid,index,4);
		}
	    }
	    if (s.getShape()!=null){
		tmpI=s.getShape().intValue();
		if (tmpI==Style.ELLIPSE.intValue()){tmpS=_ellipse;}
		else if (tmpI==Style.RECTANGLE.intValue()){tmpS=_rectangle;}
		else if (tmpI==Style.ROUND_RECTANGLE.intValue()){tmpS=_roundrect;}
		else if (tmpI==Style.CUSTOM_SHAPE.intValue()){
		    tmpS="["+Utils.arrayOffloatAsCSStrings(s.getVertexList())+"]";
		    Float angle=s.getShapeOrient();
		    if (angle!=null && angle.floatValue()!=0.0f){
			tmpS+=" "+angle.toString();
		    }
		}
		else if (tmpI==Style.CUSTOM_POLYGON.intValue()){
		    tmpS="{"+Utils.arrayOffloatCoordsAsCSStrings(s.getVertexList())+"}";
		}
		else if (tmpI==Style.CIRCLE.intValue()){tmpS=_circle;}
		else if (tmpI==Style.DIAMOND.intValue()){tmpS=_diamond;}
		else if (tmpI==Style.OCTAGON.intValue()){tmpS=_octagon;}
		else if (tmpI==Style.TRIANGLEN.intValue()){tmpS=_trianglen;}
		else if (tmpI==Style.TRIANGLES.intValue()){tmpS=_triangles;}
		else if (tmpI==Style.TRIANGLEE.intValue()){tmpS=_trianglee;}
		else if (tmpI==Style.TRIANGLEW.intValue()){tmpS=_trianglew;}
		else tmpS=null;
		styleTable.setValueAt(tmpS,index,5);
	    }
	    if (s.getIcon()!=null){
		tmpU=s.getIcon();
		if (tmpU.toString().equals(GraphStylesheet._gssFetch)){tmpS=_iconFetch;}
		else tmpS=tmpU.toString();
		styleTable.setValueAt(tmpS,index,6);
	    }
	    if (s.getTextAlignment()!=null){
		tmpI=s.getTextAlignment().intValue();
		if (tmpI==Style.TA_ABOVE.intValue()){tmpS=_taAbove;}
		else if (tmpI==Style.TA_BELOW.intValue()){tmpS=_taBelow;}
		else if (tmpI==Style.TA_CENTER.intValue()){tmpS=_taCenter;}
		else if (tmpI==Style.TA_LEFT.intValue()){tmpS=_taLeft;}
		else if (tmpI==Style.TA_RIGHT.intValue()){tmpS=_taRight;}
		else tmpS=null;
		styleTable.setValueAt(tmpS,index,7);
	    }
	    if (s.getFontFamily()!=null){
		styleTable.setValueAt(s.getFontFamily(),index,8);
	    }
	    if (s.getFontSize()!=null){
		styleTable.setValueAt(s.getFontSize().toString(),index,9);
	    }
	    if (s.getFontWeight()!=null){
		tmpSH=s.getFontWeight().shortValue();
		if (tmpSH==Style.CSS_FONT_WEIGHT_NORMAL){tmpS="normal";}
		else if (tmpSH==Style.CSS_FONT_WEIGHT_BOLD){tmpS="bold";}
		else if (tmpSH==Style.CSS_FONT_WEIGHT_100){tmpS="100";}
		else if (tmpSH==Style.CSS_FONT_WEIGHT_200){tmpS="200";}
		else if (tmpSH==Style.CSS_FONT_WEIGHT_300){tmpS="300";}
		else if (tmpSH==Style.CSS_FONT_WEIGHT_500){tmpS="500";}
		else if (tmpSH==Style.CSS_FONT_WEIGHT_600){tmpS="600";}
		else if (tmpSH==Style.CSS_FONT_WEIGHT_800){tmpS="800";}
		else if (tmpSH==Style.CSS_FONT_WEIGHT_900){tmpS="900";}
		else tmpS=null;
		styleTable.setValueAt(tmpS,index,10);
	    }
	    if (s.getFontStyle()!=null){
		tmpSH=s.getFontStyle().shortValue();
		if (tmpSH==Style.CSS_FONT_STYLE_NORMAL){tmpS="normal";}
		else if (tmpSH==Style.CSS_FONT_STYLE_ITALIC){tmpS="italic";}
		else if (tmpSH==Style.CSS_FONT_STYLE_OBLIQUE){tmpS="oblique";}
		else tmpS=null;
		styleTable.setValueAt(tmpS,index,11);
	    }
	}
	pp.setPBValue(60);
	//sort enumerations
	CustomOrdering co;
	String[] binding;
	String tmpS2;
	for (Enumeration e=es.orderings.keys();e.hasMoreElements();){
	    addSort();
	    id=(String)e.nextElement();
	    co=(CustomOrdering)es.orderings.get(id);
	    if (id.startsWith("#")){id=id.substring(1);}
	    else if ((tmpI=id.lastIndexOf("#"))!=-1){id=id.substring(tmpI+1);}
	    index=sortTable.getRowCount()-1;
	    sortTable.setValueAt(id,index,0);
	    if ((tmpS=Utils.vectorOfStringAsCSStrings(co.getEnumeration()))!=null && !Utils.isWhiteSpaceCharsOnly(tmpS)){
		if (isv!=null){//try to replace full namespace URIs by prefix binding, only
		    //if calling from IsaViz, as it is the only place where bindings might be declared
		    for (int i=0;i<co.getEnumeration().size();i++){
			tmpS2=((String)co.getEnumeration().elementAt(i)).trim();
			binding=isv.getNSBindingFromFullURI(tmpS2);
			if (binding!=null){
			    tmpS2=binding[0]+":"+tmpS2.substring(binding[1].length());
			    co.getEnumeration().setElementAt(tmpS2,i);
			}
		    }
		}
		sortTable.setValueAt(Utils.vectorOfStringAsCSStrings(co.getEnumeration()),index,1);
	    }
	}
	pp.setPBValue(70);
	//resource selectors
	GSSResSelector rs;
	int tmpI2;
	Integer tmpI3;
	boolean subCondCreated=false;
	Vector tmpV;
	Object tmpO;
	for (Enumeration e=es.resourceSelectors.elements();e.hasMoreElements();){
	    rs=(GSSResSelector)e.nextElement();
	    addSelector(rselTable,false);
	    tmpI=rselTable.getRowCount()-1;  //index of selector's first row (remebered to put visibility/style/layout/sort info)
	    rselTable.setValueAt(Integer.toString(rs.getWeight()),tmpI,1);
	    if (rs.resourceURIequals!=null){
		addConditionToSelector(rselTable);
		tmpI2=rselTable.getRowCount()-1;
		rselTable.setValueAt(_urieqc,tmpI2,0);
		rselTable.setValueAt(contractURI(rs.resourceURIequals),tmpI2,2);
	    }
	    if (rs.resourceURIstartsWith!=null){
		addConditionToSelector(rselTable);
		tmpI2=rselTable.getRowCount()-1;
		rselTable.setValueAt(_uriswc,tmpI2,0);
		rselTable.setValueAt(rs.resourceURIstartsWith,tmpI2,2);  //do not contract URI here as this is likely to be a namespace
	    }
	    if (rs.subjectOfStatements!=null){
		for (int i=0;i<rs.subjectOfStatements.length;i++){
		    if (rs.subjectOfStatements[i].predicateURI!=null){
			addConditionToSelector(rselTable);
			tmpI2=rselTable.getRowCount()-1;
			rselTable.setValueAt(_sosc,tmpI2,0);
			rselTable.setValueAt(_wpc,tmpI2,1);
			rselTable.setValueAt(contractURI(rs.subjectOfStatements[i].predicateURI),tmpI2,2);
			subCondCreated=true;
		    }
		    if (rs.subjectOfStatements[i].objectValueOrURI!=null){
			addConditionToSelector(rselTable);
			tmpI2=rselTable.getRowCount()-1;
			if (!subCondCreated){rselTable.setValueAt(_sosc,tmpI2,0);subCondCreated=true;}
			rselTable.setValueAt(_wovlc,tmpI2,1);
			rselTable.setValueAt(contractURI(rs.subjectOfStatements[i].objectValueOrURI),tmpI2,2);
		    }
		    if (rs.subjectOfStatements[i].objectType!=null){
			addConditionToSelector(rselTable);
			tmpI2=rselTable.getRowCount()-1;
			if (!subCondCreated){rselTable.setValueAt(_sosc,tmpI2,0);subCondCreated=true;}
			rselTable.setValueAt((rs.subjectOfStatements[i].literalObject.booleanValue()) ? _wodtc : _wotc,tmpI2,1);
			rselTable.setValueAt(contractURI(rs.subjectOfStatements[i].objectType),tmpI2,2);
		    }
		    subCondCreated=false;
		}
	    }
	    if (rs.objectOfStatements!=null){
		for (int i=0;i<rs.objectOfStatements.length;i++){
		    if (rs.objectOfStatements[i].predicateURI!=null){
			addConditionToSelector(rselTable);
			tmpI2=rselTable.getRowCount()-1;
			rselTable.setValueAt(_oosc,tmpI2,0);
			rselTable.setValueAt(_wpc,tmpI2,1);
			rselTable.setValueAt(contractURI(rs.objectOfStatements[i].predicateURI),tmpI2,2);
			subCondCreated=true;
		    }
		    if (rs.objectOfStatements[i].subjectURI!=null){
			addConditionToSelector(rselTable);
			tmpI2=rselTable.getRowCount()-1;
			if (!subCondCreated){rselTable.setValueAt(_oosc,tmpI2,0);subCondCreated=true;}
			rselTable.setValueAt(_wsvlc,tmpI2,1);
			rselTable.setValueAt(contractURI(rs.objectOfStatements[i].subjectURI),tmpI2,2);
		    }
		    if (rs.objectOfStatements[i].subjectType!=null){
			addConditionToSelector(rselTable);
			tmpI2=rselTable.getRowCount()-1;
			if (!subCondCreated){rselTable.setValueAt(_oosc,tmpI2,0);subCondCreated=true;}
			rselTable.setValueAt(_wstc,tmpI2,1);
			rselTable.setValueAt(contractURI(rs.objectOfStatements[i].subjectType),tmpI2,2);
		    }
		    subCondCreated=false;
		}
	    }
	    if (es.rStyleRules.containsKey(rs)){
		tmpV=(Vector)es.rStyleRules.get(rs);
		tmpS="";
		for (int i=0;i<tmpV.size()-1;i++){
		    tmpS2=(String)tmpV.elementAt(i);
		    if (tmpS2.startsWith("#")){tmpS2=tmpS2.substring(1);}
		    else if ((tmpI2=tmpS2.lastIndexOf("#"))!=-1){tmpS2=tmpS2.substring(tmpI2+1);}
		    tmpS+=tmpS2+",";
		}
		tmpS2=(String)tmpV.lastElement();
		if (tmpS2.startsWith("#")){tmpS2=tmpS2.substring(1);}
		else if ((tmpI2=tmpS2.lastIndexOf("#"))!=-1){tmpS2=tmpS2.substring(tmpI2+1);}
		tmpS+=tmpS2;
		rselTable.setValueAt(tmpS,tmpI,3);
	    }
	    if (es.rVisRules.containsKey(rs)){
		tmpI3=(Integer)es.rVisRules.get(rs);
		if (tmpI3.equals(GraphStylesheet.DISPLAY_NONE)){rselTable.setValueAt(_dispnone,tmpI,4);}
		else if (tmpI3.equals(GraphStylesheet.VISIBILITY_HIDDEN)){rselTable.setValueAt(_vishidden,tmpI,4);}
		else if (tmpI3.equals(GraphStylesheet.VISIBILITY_VISIBLE)){rselTable.setValueAt(_visvisible,tmpI,4);}
	    }
	    if (es.rLayoutRules.containsKey(rs)){
		tmpI3=(Integer)es.rLayoutRules.get(rs);
		if (tmpI3.equals(GraphStylesheet.TABLE_FORM)){rselTable.setValueAt(_table,tmpI,5);}
		else if (tmpI3.equals(GraphStylesheet.NODE_EDGE)){rselTable.setValueAt(_nodearc,tmpI,5);}
	    }
	    if (es.sortRules.containsKey(rs)){
		tmpO=es.sortRules.get(rs);
		if (tmpO instanceof Integer){
		    tmpI3=(Integer)tmpO;
		    if (tmpI3.equals(GraphStylesheet.SORT_BY_NAME)){rselTable.setValueAt(_sortnm,tmpI,6);}
		    else if (tmpI3.equals(GraphStylesheet.SORT_BY_NAMESPACE)){rselTable.setValueAt(_sortns,tmpI,6);}
		    else if (tmpI3.equals(GraphStylesheet.SORT_BY_NAME_REV)){rselTable.setValueAt(_sortnmr,tmpI,6);}
		    else if (tmpI3.equals(GraphStylesheet.SORT_BY_NAMESPACE_REV)){rselTable.setValueAt(_sortnsr,tmpI,6);}
		}
		else if (tmpO instanceof String){
		    tmpS2=(String)tmpO;
		    if (tmpS2.startsWith("#")){tmpS2=tmpS2.substring(1);}
		    else if ((tmpI2=tmpS2.lastIndexOf("#"))!=-1){tmpS2=tmpS2.substring(tmpI2+1);}
		    rselTable.setValueAt(tmpS2,tmpI,6);
		}
	    }
	}
	pp.setPBValue(80);
	rs=null;
	//literal selectors
	GSSLitSelector ls;
	for (Enumeration e=es.literalSelectors.elements();e.hasMoreElements();){
	    ls=(GSSLitSelector)e.nextElement();
	    addSelector(lselTable,false);
	    tmpI=lselTable.getRowCount()-1;  //index of selector's first row (remebered to put visibility/style/layout info)
	    lselTable.setValueAt(Integer.toString(ls.getWeight()),tmpI,1);
	    if (ls.value!=null){
		addConditionToSelector(lselTable);
		tmpI2=lselTable.getRowCount()-1;
		lselTable.setValueAt(_vleqc,tmpI2,0);
		lselTable.setValueAt(ls.value,tmpI2,2);
	    }
	    if (ls.datatype!=null){
		addConditionToSelector(lselTable);
		tmpI2=lselTable.getRowCount()-1;
		lselTable.setValueAt(_dtc,tmpI2,0);
		lselTable.setValueAt(contractURI(ls.datatype),tmpI2,2);
	    }
	    if (ls.objectOfStatement!=null){
		if (ls.objectOfStatement.predicateURI!=null){
		    addConditionToSelector(lselTable);
		    tmpI2=lselTable.getRowCount()-1;
		    lselTable.setValueAt(_oosc,tmpI2,0);
		    lselTable.setValueAt(_wpc,tmpI2,1);
		    lselTable.setValueAt(contractURI(ls.objectOfStatement.predicateURI),tmpI2,2);
		    subCondCreated=true;
		}
		if (ls.objectOfStatement.subjectURI!=null){
		    addConditionToSelector(lselTable);
		    tmpI2=lselTable.getRowCount()-1;
		    if (!subCondCreated){lselTable.setValueAt(_oosc,tmpI2,0);subCondCreated=true;}
		    lselTable.setValueAt(_wsvlc,tmpI2,1);
		    lselTable.setValueAt(contractURI(ls.objectOfStatement.subjectURI),tmpI2,2);
		}
		if (ls.objectOfStatement.subjectType!=null){
		    addConditionToSelector(lselTable);
		    tmpI2=lselTable.getRowCount()-1;
		    if (!subCondCreated){lselTable.setValueAt(_oosc,tmpI2,0);subCondCreated=true;}
		    lselTable.setValueAt(_wstc,tmpI2,1);
		    lselTable.setValueAt(contractURI(ls.objectOfStatement.subjectType),tmpI2,2);
		}
		subCondCreated=false;
	    }
	    if (es.lStyleRules.containsKey(ls)){
		tmpV=(Vector)es.lStyleRules.get(ls);
		tmpS="";
		for (int i=0;i<tmpV.size()-1;i++){
		    tmpS2=(String)tmpV.elementAt(i);
		    if (tmpS2.startsWith("#")){tmpS2=tmpS2.substring(1);}
		    else if ((tmpI2=tmpS2.lastIndexOf("#"))!=-1){tmpS2=tmpS2.substring(tmpI2+1);}
		    tmpS+=tmpS2+",";
		}
		tmpS2=(String)tmpV.lastElement();
		if (tmpS2.startsWith("#")){tmpS2=tmpS2.substring(1);}
		else if ((tmpI2=tmpS2.lastIndexOf("#"))!=-1){tmpS2=tmpS2.substring(tmpI2+1);}
		tmpS+=tmpS2;
		lselTable.setValueAt(tmpS,tmpI,3);
	    }
	    if (es.lVisRules.containsKey(ls)){
		tmpI3=(Integer)es.lVisRules.get(ls);
		if (tmpI3.equals(GraphStylesheet.DISPLAY_NONE)){lselTable.setValueAt(_dispnone,tmpI,4);}
		else if (tmpI3.equals(GraphStylesheet.VISIBILITY_HIDDEN)){lselTable.setValueAt(_vishidden,tmpI,4);}
		else if (tmpI3.equals(GraphStylesheet.VISIBILITY_VISIBLE)){lselTable.setValueAt(_visvisible,tmpI,4);}
	    }
	    if (es.lLayoutRules.containsKey(ls)){
		tmpI3=(Integer)es.lLayoutRules.get(ls);
		if (tmpI3.equals(GraphStylesheet.TABLE_FORM)){lselTable.setValueAt(_table,tmpI,5);}
		else if (tmpI3.equals(GraphStylesheet.NODE_EDGE)){lselTable.setValueAt(_nodearc,tmpI,5);}
	    }
	}
	ls=null;
	pp.setPBValue(90);
	//property selectors
	GSSPrpSelector ps;
	for (Enumeration e=es.propertySelectors.elements();e.hasMoreElements();){
	    ps=(GSSPrpSelector)e.nextElement();
	    addSelector(pselTable,false);
	    tmpI=pselTable.getRowCount()-1;  //index of selector's first row (remebered to put visibility/style/layout/sort info)
	    pselTable.setValueAt(Integer.toString(ps.getWeight()),tmpI,1);
	    if (ps.propertyURIequals!=null){
		addConditionToSelector(pselTable);
		tmpI2=pselTable.getRowCount()-1;
		pselTable.setValueAt(_urieqc,tmpI2,0);
		pselTable.setValueAt(contractURI(ps.propertyURIequals),tmpI2,2);
	    }
	    if (ps.propertyURIstartsWith!=null){
		addConditionToSelector(pselTable);
		tmpI2=pselTable.getRowCount()-1;
		pselTable.setValueAt(_uriswc,tmpI2,0);
		pselTable.setValueAt(ps.propertyURIstartsWith,tmpI2,2);  //do not contract URI here as this is likely to be a namespace
	    }
	    if (ps.predicateOfStatement!=null){
		if (ps.predicateOfStatement.objectValueOrURI!=null){
		    addConditionToSelector(pselTable);
		    tmpI2=pselTable.getRowCount()-1;
		    pselTable.setValueAt(_posc,tmpI2,0);
		    pselTable.setValueAt(_wovlc,tmpI2,1);
		    pselTable.setValueAt(contractURI(ps.predicateOfStatement.objectValueOrURI),tmpI2,2);
		    subCondCreated=true;
		}
		if (ps.predicateOfStatement.objectType!=null){
		    addConditionToSelector(pselTable);
		    tmpI2=pselTable.getRowCount()-1;
		    if (!subCondCreated){pselTable.setValueAt(_posc,tmpI2,0);subCondCreated=true;}
		    pselTable.setValueAt((ps.predicateOfStatement.literalObject.booleanValue()) ? _wodtc : _wotc,tmpI2,1);
		    pselTable.setValueAt(contractURI(ps.predicateOfStatement.objectType),tmpI2,2);
		}
		if (ps.predicateOfStatement.subjectURI!=null){
		    addConditionToSelector(pselTable);
		    tmpI2=pselTable.getRowCount()-1;
		    if (!subCondCreated){pselTable.setValueAt(_posc,tmpI2,0);subCondCreated=true;}
		    pselTable.setValueAt(_wsvlc,tmpI2,1);
		    pselTable.setValueAt(contractURI(ps.predicateOfStatement.subjectURI),tmpI2,2);
		}
		if (ps.predicateOfStatement.subjectType!=null){
		    addConditionToSelector(pselTable);
		    tmpI2=pselTable.getRowCount()-1;
		    if (!subCondCreated){pselTable.setValueAt(_posc,tmpI2,0);subCondCreated=true;}
		    pselTable.setValueAt(_wstc,tmpI2,1);
		    pselTable.setValueAt(contractURI(ps.predicateOfStatement.subjectType),tmpI2,2);
		}
		subCondCreated=false;
	    }
	    if (es.pStyleRules.containsKey(ps)){
		tmpV=(Vector)es.pStyleRules.get(ps);
		tmpS="";
		for (int i=0;i<tmpV.size()-1;i++){
		    tmpS2=(String)tmpV.elementAt(i);
		    if (tmpS2.startsWith("#")){tmpS2=tmpS2.substring(1);}
		    else if ((tmpI2=tmpS2.lastIndexOf("#"))!=-1){tmpS2=tmpS2.substring(tmpI2+1);}
		    tmpS+=tmpS2+",";
		}
		tmpS2=(String)tmpV.lastElement();
		if (tmpS2.startsWith("#")){tmpS2=tmpS2.substring(1);}
		else if ((tmpI2=tmpS2.lastIndexOf("#"))!=-1){tmpS2=tmpS2.substring(tmpI2+1);}
		tmpS+=tmpS2;
		pselTable.setValueAt(tmpS,tmpI,3);
	    }
	    if (es.pVisRules.containsKey(ps)){
		tmpI3=(Integer)es.pVisRules.get(ps);
		if (tmpI3.equals(GraphStylesheet.DISPLAY_NONE)){pselTable.setValueAt(_dispnone,tmpI,4);}
		else if (tmpI3.equals(GraphStylesheet.VISIBILITY_HIDDEN)){pselTable.setValueAt(_vishidden,tmpI,4);}
		else if (tmpI3.equals(GraphStylesheet.VISIBILITY_VISIBLE)){pselTable.setValueAt(_visvisible,tmpI,4);}
	    }
	    if (es.pLayoutRules.containsKey(ps)){
		tmpI3=(Integer)es.pLayoutRules.get(ps);
		if (tmpI3.equals(GraphStylesheet.TABLE_FORM)){pselTable.setValueAt(_table,tmpI,5);}
		else if (tmpI3.equals(GraphStylesheet.NODE_EDGE)){pselTable.setValueAt(_nodearc,tmpI,5);}
	    }
	}
    }

    String contractURI(String uri){
	String res=uri;
	if (isv!=null){
	    String[] binding=isv.getNSBindingFromFullURI(uri);
	    if (binding!=null){
		res=binding[0]+":"+uri.substring(binding[1].length());
	    }
	}
	return res;
    }

    void saveFile(){
	if (lastGSSFile!=null){
	    	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			saveStylesheet(lastGSSFile);
			return null; 
		    }
		};
	    worker.start();
	}
	else {saveFileAs();}
    }

    void saveFileAs(){
	final JFileChooser fc=new JFileChooser((lastGSSFile!=null) ? lastGSSFile.getParentFile() : new File("."));
	fc.setDialogTitle("Save GSS Stylesheet");
	int returnVal=fc.showOpenDialog(this);
	if (returnVal==JFileChooser.APPROVE_OPTION){
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			saveStylesheet(fc.getSelectedFile());
			return null; 
		    }
		};
	    worker.start();
	}
    }

    void saveStylesheet(File f){
	displayMsg("Saving "+f.toString()+" ...");
	pp.setPBValue(0);
	this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
	String base="";
	pp.setPBValue(5);
	if (BASE_URI.length()>0 && !Utils.isWhiteSpaceCharsOnly(BASE_URI)){
	    base=BASE_URI;
	}
	else {
	    try {
		base=f.toURL().toString();
	    }
	    catch (MalformedURLException ex){base=BASE_URI;}
	}
	//create mem model from the JTables
	Model m=generateJenaModel(base);
	//then serialize the mem model
	try {
	    RDFWriter rdfw=m.getWriter(RDFLoader.RDFXMLAB);
	    rdfw.setProperty("allowBadURIs","true");  //because Jena2p2 does not allow null (blank) base URIs when checking for bad URIs
	    rdfw.setProperty("showXmlDeclaration","true");
	    rdfw.setErrorHandler(new GSSSerializeErrorHandler(this));
	    //default prefixes
	    m.setNsPrefix(GraphStylesheet.GSS_NAMESPACE_PREFIX,GraphStylesheet._gssNS);
	    m.setNsPrefix(Editor.RDFMS_NAMESPACE_PREFIX,Editor.RDFMS_NAMESPACE_URI);
	    if (isv!=null){
		//bindings declared by IsaViz - only the ones actually needed will appear in the serialization
		for (int i=0;i<isv.tblp.nsTableModel.getRowCount();i++){
		    if (((String)isv.tblp.nsTableModel.getValueAt(i,0)).length()>0){//only add complete bindings
			m.setNsPrefix((String)isv.tblp.nsTableModel.getValueAt(i,0),(String)isv.tblp.nsTableModel.getValueAt(i,1));
		    }
		}
	    }
	    FileOutputStream fos=new FileOutputStream(f);
	    rdfw.setProperty("xmlbase",base);
	    rdfw.write(m,fos,base);
	    lastGSSFile=f;
	    this.setCursor(java.awt.Cursor.getDefaultCursor());
	    pp.setPBValue(100);
	    displayMsg("Saving "+f.toString()+" ... done");
	    if (this.isv != null){
		this.isv.gssMngr.refreshStylesheet(f);
	    }
	}
	catch (RDFException ex){System.err.println("RDF exception in RDFLoader.save() "+ex);ex.printStackTrace();reportError=true;}
	catch (IOException ex){System.err.println("I/O exception in RDFLoader.save() "+ex);ex.printStackTrace();reportError=true;}
	catch (Exception ex){System.err.println("Exception in RDFLoader.save() "+ex);ex.printStackTrace();reportError=true;}
	if (reportError){displayError("There were error/warning messages (See Standard Error Output Stream)");reportError=false;}
    }

    Model generateJenaModel(String base){
	pp.setPBValue(10);
	//Model res=new ModelMem();
	Model res = ModelFactory.createDefaultModel();
	Hashtable addedResources=new Hashtable();  //resource URI/ID 2 jena resource
	Hashtable addedProperties=new Hashtable();  //property URI 2 jena propertiy
	Resource jenaSubject=null;
	Property jenaPredicate=null;
	RDFNode jenaObject=null;
	Statement st=null;
	pp.setPBValue(15);
	//init main property types and GSS resources
	addedProperties.put(GraphStylesheet._gssSOS,res.createProperty(GraphStylesheet._gssNS,"subjectOfStatement"));
	addedProperties.put(GraphStylesheet._gssPOS,res.createProperty(GraphStylesheet._gssNS,"predicateOfStatement"));
	addedProperties.put(GraphStylesheet._gssOOS,res.createProperty(GraphStylesheet._gssNS,"objectOfStatement"));
	addedProperties.put(GraphStylesheet._gssSubject,res.createProperty(GraphStylesheet._gssNS,"subject"));
	addedProperties.put(GraphStylesheet._gssPredicate,res.createProperty(GraphStylesheet._gssNS,"predicate"));
	addedProperties.put(GraphStylesheet._gssObject,res.createProperty(GraphStylesheet._gssNS,"object"));
	addedProperties.put(GraphStylesheet._gssURIsw,res.createProperty(GraphStylesheet._gssNS,"uriStartsWith"));
	addedProperties.put(GraphStylesheet._gssURIeq,res.createProperty(GraphStylesheet._gssNS,"uriEquals"));
	addedProperties.put(GraphStylesheet._gssValue,res.createProperty(GraphStylesheet._gssNS,"value"));
	addedProperties.put(GraphStylesheet._gssClass,res.createProperty(GraphStylesheet._gssNS,"class"));
	addedProperties.put(GraphStylesheet._gssDatatype,res.createProperty(GraphStylesheet._gssNS,"datatype"));
	addedProperties.put(GraphStylesheet._gssStyle,res.createProperty(GraphStylesheet._gssNS,"style"));
	addedProperties.put(GraphStylesheet._gssVisibility,res.createProperty(GraphStylesheet._gssNS,"visibility"));
	addedProperties.put(GraphStylesheet._gssDisplay,res.createProperty(GraphStylesheet._gssNS,"display"));
	addedProperties.put(GraphStylesheet._gssLayout,res.createProperty(GraphStylesheet._gssNS,"layout"));
	addedProperties.put(GraphStylesheet._gssSort,res.createProperty(GraphStylesheet._gssNS,"sortPropertiesBy"));
	addedProperties.put(GraphStylesheet._gssFill,res.createProperty(GraphStylesheet._gssNS,"fill"));
	addedProperties.put(GraphStylesheet._gssStroke,res.createProperty(GraphStylesheet._gssNS,"stroke"));
	addedProperties.put(GraphStylesheet._gssStrokeWidth,res.createProperty(GraphStylesheet._gssNS,"stroke-width"));
	addedProperties.put(GraphStylesheet._gssStrokeDashArray,res.createProperty(GraphStylesheet._gssNS,"stroke-dasharray"));
	addedProperties.put(GraphStylesheet._gssShape,res.createProperty(GraphStylesheet._gssNS,"shape"));
	addedProperties.put(GraphStylesheet._gssIcon,res.createProperty(GraphStylesheet._gssNS,"icon"));
	addedProperties.put(GraphStylesheet._gssTextAlign,res.createProperty(GraphStylesheet._gssNS,"text-align"));
	addedProperties.put(GraphStylesheet._gssFontFamily,res.createProperty(GraphStylesheet._gssNS,"font-family"));
	addedProperties.put(GraphStylesheet._gssFontSize,res.createProperty(GraphStylesheet._gssNS,"font-size"));
	addedProperties.put(GraphStylesheet._gssFontWeight,res.createProperty(GraphStylesheet._gssNS,"font-weight"));
	addedProperties.put(GraphStylesheet._gssFontStyle,res.createProperty(GraphStylesheet._gssNS,"font-style"));
	addedProperties.put(GraphStylesheet._rdfType,res.createProperty(Editor.RDFMS_NAMESPACE_URI,"type"));
	addedResources.put(GraphStylesheet._gssResource,res.createResource(GraphStylesheet._gssResource));
	addedResources.put(GraphStylesheet._gssProperty,res.createResource(GraphStylesheet._gssProperty));
	addedResources.put(GraphStylesheet._gssLiteral,res.createResource(GraphStylesheet._gssLiteral));
	addedResources.put(GraphStylesheet._gssShow,res.createResource(GraphStylesheet._gssShow));
	addedResources.put(GraphStylesheet._gssHide,res.createResource(GraphStylesheet._gssHide));
	addedResources.put(GraphStylesheet._gssNone,res.createResource(GraphStylesheet._gssNone));
	addedResources.put(GraphStylesheet._gssTable,res.createResource(GraphStylesheet._gssTable));
	addedResources.put(GraphStylesheet._gssNodeAndArc,res.createResource(GraphStylesheet._gssNodeAndArc));
	addedResources.put(GraphStylesheet._gssSortN,res.createResource(GraphStylesheet._gssSortN));
	addedResources.put(GraphStylesheet._gssSortNR,res.createResource(GraphStylesheet._gssSortNR));
	addedResources.put(GraphStylesheet._gssSortNS,res.createResource(GraphStylesheet._gssSortNS));
	addedResources.put(GraphStylesheet._gssSortNSR,res.createResource(GraphStylesheet._gssSortNSR));
	addedResources.put(GraphStylesheet._gssPlainLiterals,res.createResource(GraphStylesheet._gssPlainLiterals));
	addedResources.put(GraphStylesheet._gssAllDatatypes,res.createResource(GraphStylesheet._gssAllDatatypes));
	addedResources.put(GraphStylesheet._gssTAAbove,res.createResource(GraphStylesheet._gssTAAbove));
	addedResources.put(GraphStylesheet._gssTABelow,res.createResource(GraphStylesheet._gssTABelow));
	addedResources.put(GraphStylesheet._gssTACenter,res.createResource(GraphStylesheet._gssTACenter));
	addedResources.put(GraphStylesheet._gssTALeft,res.createResource(GraphStylesheet._gssTALeft));
	addedResources.put(GraphStylesheet._gssTARight,res.createResource(GraphStylesheet._gssTARight));
	addedResources.put(GraphStylesheet._gssEllipse,res.createResource(GraphStylesheet._gssEllipse));
	addedResources.put(GraphStylesheet._gssRectangle,res.createResource(GraphStylesheet._gssRectangle));
	addedResources.put(GraphStylesheet._gssRoundRect,res.createResource(GraphStylesheet._gssRoundRect));
	addedResources.put(GraphStylesheet._gssCircle,res.createResource(GraphStylesheet._gssCircle));
	addedResources.put(GraphStylesheet._gssDiamond,res.createResource(GraphStylesheet._gssDiamond));
	addedResources.put(GraphStylesheet._gssOctagon,res.createResource(GraphStylesheet._gssOctagon));
	addedResources.put(GraphStylesheet._gssTriangleN,res.createResource(GraphStylesheet._gssTriangleN));
	addedResources.put(GraphStylesheet._gssTriangleS,res.createResource(GraphStylesheet._gssTriangleS));
	addedResources.put(GraphStylesheet._gssTriangleW,res.createResource(GraphStylesheet._gssTriangleW));
	addedResources.put(GraphStylesheet._gssTriangleE,res.createResource(GraphStylesheet._gssTriangleE));
	addedResources.put(GraphStylesheet._gssDASolid,res.createResource(GraphStylesheet._gssDASolid));
	addedResources.put(GraphStylesheet._gssDADashed,res.createResource(GraphStylesheet._gssDADashed));
	addedResources.put(GraphStylesheet._gssDADotted,res.createResource(GraphStylesheet._gssDADotted));
	addedResources.put(GraphStylesheet._rdfSeq,res.createResource(GraphStylesheet._rdfSeq));
	pp.setPBValue(20);
	//first deal with styles
	String s1,s2;
	for (int i=0;i<styleTable.getRowCount();i++){
	    //style ID
	    s1=(String)styleTable.getValueAt(i,0);
	    if (s1!=null && !Utils.isWhiteSpaceCharsOnly(s1)){//retrieve ID if specified by user
		if (!s1.startsWith("#")){s1="#"+s1;}
		s1=base+s1;
		jenaSubject=res.createResource(s1);
		addedResources.put(s1,jenaSubject);
	    }
	    else {//if not generate a unique ID for this style
		String uniqueID=base+"#style"+Long.toString(Math.round(Math.random()*100000));
		while (addedResources.containsKey(uniqueID)){
		    uniqueID=base+"#style"+Long.toString(Math.round(Math.random()*100000));
		}
		jenaSubject=res.createResource(uniqueID);
		addedResources.put(uniqueID,jenaSubject);
	    }
	    //style statement - fill
	    s2=(String)styleTable.getValueAt(i,1);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssFill);
		jenaObject=res.createLiteral(s2.trim(),"");
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - stroke
	    s2=(String)styleTable.getValueAt(i,2);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssStroke);
		jenaObject=res.createLiteral(s2.trim(),"");
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - stroke width
	    s2=(String)styleTable.getValueAt(i,3);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssStrokeWidth);
		jenaObject=res.createLiteral(s2.trim(),"");
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - stroke dasharray
	    s2=(String)styleTable.getValueAt(i,4);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssStrokeDashArray);
		if (s2.equals(_daSolid)){
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssDASolid);
		}
		else if (s2.equals(_daDashed)){
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssDADashed);
		}
		else if (s2.equals(_daDotted)){
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssDADotted);
		}
		else {//custom pattern
		    jenaObject=res.createLiteral(s2.trim(),"");
		}
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - shape
	    s2=(String)styleTable.getValueAt(i,5);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssShape);
		s2=s2.trim();
		//predefined shapes
		if (s2.equals(_ellipse)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssEllipse);}
		else if (s2.equals(_rectangle)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssRectangle);}
		else if (s2.equals(_roundrect)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssRoundRect);}
		else if (s2.equals(_circle)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssCircle);}
		else if (s2.equals(_diamond)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssDiamond);}
		else if (s2.equals(_octagon)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssOctagon);}
		else if (s2.equals(_trianglen)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssTriangleN);}
		else if (s2.equals(_triangles)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssTriangleS);}
		else if (s2.equals(_trianglee)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssTriangleE);}
		else if (s2.equals(_trianglew)){jenaObject=(Resource)addedResources.get(GraphStylesheet._gssTriangleW);}
		else {//custom shape or custom polygon
		    jenaObject=res.createLiteral(s2.trim(),"");
		}
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - icon
	    s2=(String)styleTable.getValueAt(i,6);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssIcon);
		s2=s2.trim();
		if (s2.equals(_iconFetch)){s2=GraphStylesheet._gssFetch;}
		if (addedResources.containsKey(s2)){
		    jenaObject=(Resource)addedResources.get(s2);
		}
		else {
		    jenaObject=res.createResource(s2);
		    addedResources.put(s2,jenaObject);
		}
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - text alignment
	    s2=(String)styleTable.getValueAt(i,7);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssTextAlign);
		s2=GraphStylesheet._gssNS+s2;
		if (addedResources.containsKey(s2)){
		    jenaObject=(Resource)addedResources.get(s2);
		}
		else {
		    jenaObject=res.createResource(s2);
		    addedResources.put(s2,jenaObject);
		}
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - font family
	    s2=(String)styleTable.getValueAt(i,8);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssFontFamily);
		jenaObject=res.createLiteral(s2.trim(),"");
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - font size
	    s2=(String)styleTable.getValueAt(i,9);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssFontSize);
		jenaObject=res.createLiteral(s2.trim(),"");
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - font weight
	    s2=(String)styleTable.getValueAt(i,10);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssFontWeight);
		jenaObject=res.createLiteral(s2.trim(),"");
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	    //style statement - font style
	    s2=(String)styleTable.getValueAt(i,11);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssFontStyle);
		jenaObject=res.createLiteral(s2.trim(),"");
		st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		res.add(st);
	    }
	}
	pp.setPBValue(30);
	//then with sorting enumerations
	for (int i=0;i<sortTable.getRowCount();i++){
	    //sort ID
	    s1=(String)sortTable.getValueAt(i,0);
	    if (s1!=null && !Utils.isWhiteSpaceCharsOnly(s1)){//retrieve ID entered by user
		if (!s1.startsWith("#")){s1="#"+s1;}
		s1=base+s1;
		jenaSubject=res.createResource(s1);
		addedResources.put(s1,jenaSubject);
	    }
	    else {//if none, generate a unique ID for this sort
		String uniqueID=base+"#sort"+Long.toString(Math.round(Math.random()*100000));
		while (addedResources.containsKey(uniqueID)){
		    uniqueID=base+"#sort"+Long.toString(Math.round(Math.random()*100000));
		}
		jenaSubject=res.createResource(uniqueID);
		addedResources.put(uniqueID,jenaSubject);
	    }
	    s2=(String)sortTable.getValueAt(i,1);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		StringTokenizer tk=new StringTokenizer(s2.trim(),",");
		String s3,s4;
		int index=1;
		while (tk.hasMoreTokens()){
		    s3=expandBinding(tk.nextToken().trim());
		    if (s3.length()>0){
			s4=Editor.RDFMS_NAMESPACE_URI+"_"+Integer.toString(index);
			if (addedProperties.containsKey(s4)){
			    jenaPredicate=(Property)addedProperties.get(s4);
			}
			else {
			    jenaPredicate=res.createProperty(Editor.RDFMS_NAMESPACE_URI,"_"+Integer.toString(index));
			    addedProperties.put(s4,jenaPredicate);
			}
			if (addedResources.containsKey(s3)){
			    jenaObject=(Resource)addedResources.get(s3);
			}
			else {
			    jenaObject=res.createResource(s3);
			    addedResources.put(s3,jenaObject);
			}
			st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			res.add(st);
		    }
		    index++;
		}
	    }
	}
	pp.setPBValue(40);
	//then do resource selectors, literal selectors and property selectors, along with style, visib and layout instructions
	//resource selectors
	int firstRowIndex=0;
	int lastRowIndex=getSelectorLastRowIndex(rselTable,firstRowIndex);
	while (firstRowIndex<rselTable.getRowCount()){//for each selector
	    jenaSubject=res.createResource();
	    //rdf:type selector as gss:Resource
	    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._rdfType);
	    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssResource);
	    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
	    res.add(st);
	    //visibility
	    s2=(String)rselTable.getValueAt(firstRowIndex,4);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		if (s2.equals(_visvisible)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssVisibility);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssShow);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_vishidden)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssVisibility);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssHide);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_dispnone)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssDisplay);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssNone);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
	    }
	    //layout
	    s2=(String)rselTable.getValueAt(firstRowIndex,5);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		if (s2.equals(_nodearc)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssLayout);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssNodeAndArc);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_table)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssLayout);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssTable);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
	    }
	    //sorting
	    s2=(String)rselTable.getValueAt(firstRowIndex,6);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		s2=s2.trim();
		if (s2.equals(_sortnm)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSort);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssSortN);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_sortns)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSort);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssSortNS);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_sortnmr)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSort);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssSortNR);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_sortnsr)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSort);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssSortNSR);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else {
		    if (!s2.startsWith("#")){s2="#"+s2;}
		    s2=base+s2;
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSort);
		    if (addedResources.containsKey(s2)){
			jenaObject=(Resource)addedResources.get(s2);
			st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			res.add(st);//if !addedResources.containsKey(s2), as we already have processed
		    }//sort enumerations, it's better not to do anything, as there is no definition for this ID
		}
	    }
	    //styling
	    s2=(String)rselTable.getValueAt(firstRowIndex,3);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		StringTokenizer tk=new StringTokenizer(s2.trim(),",");
		String s3;
		while (tk.hasMoreTokens()){
		    s3=tk.nextToken().trim();
		    if (!s3.startsWith("#")){s3="#"+s3;}
		    s3=base+s3;
		    if (addedResources.containsKey(s3)){
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssStyle);
			jenaObject=(Resource)addedResources.get(s3);
			st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			res.add(st);
		    }
		}
	    }
	    //conditions (resource selectors)
	    String s3;
	    //at this point jenaSubject is still the anon nodes representing the selector
	    Resource subCondNode=null,subCondNodeNode=null;  //resp. the object of gss:{{su,o}bject}OfStatement statements and the object of gss:{su,o}bject statements
	    for (int i=firstRowIndex+1;i<=lastRowIndex;i++){
		if (GSSEditor.isFirstSelectorRow(rselTable,i)){subCondNode=null;subCondNodeNode=null;}  //reset it just to be sure that subconditions in a selector do not get attached to a condition in another selector (should not happen anyway, but this is safer)
		s1=(String)rselTable.getValueAt(i,0);
		s2=(String)rselTable.getValueAt(i,1);
		s3=(String)rselTable.getValueAt(i,2);if (s3!=null){s3=expandBinding(s3.trim());}
		if (s1!=null && !Utils.isWhiteSpaceCharsOnly(s1)){
		    subCondNodeNode=null;//have to reset it
		    if (s1.equals(_uriswc)){
			if (s3!=null && !Utils.isWhiteSpaceCharsOnly(s3)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssURIsw);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			    res.add(st);
			}
		    }
		    else if (s1.equals(_urieqc)){
			if (s3!=null && !Utils.isWhiteSpaceCharsOnly(s3)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssURIeq);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			    res.add(st);
			}
		    }//A
		    else if (s1.equals(_sosc)){//subcondition for a condition whose first row is this one
			subCondNode=res.createResource();
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSOS);
			st=res.createStatement(jenaSubject,jenaPredicate,subCondNode);
			res.add(st);
			if (s2!=null && s3!=null && !Utils.isWhiteSpaceCharsOnly(s2) && !Utils.isWhiteSpaceCharsOnly(s3)){
			    if (s2.equals(_wpc)){
				jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssPredicate);
				if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				st=res.createStatement(subCondNode,jenaPredicate,jenaObject);
				res.add(st);
			    }
			    else if (s2.equals(_wovlc) || s2.equals(_wotc) || s2.equals(_wodtc)){
				subCondNodeNode=res.createResource();
				jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssObject);
				st=res.createStatement(subCondNode,jenaPredicate,subCondNodeNode);
				res.add(st);
				if (s2.equals(_wovlc)){
				    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssValue);
				    jenaObject=res.createLiteral(s3,"");
				}
				else if (s2.equals(_wotc)){
				    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssClass);
				    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				}
				else if (s2.equals(_wodtc)){
				    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssDatatype);
				    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				}
				st=res.createStatement(subCondNodeNode,jenaPredicate,jenaObject);
				res.add(st);
			    }
			}
		    }//B
		    else if (s1.equals(_oosc)){//subcondition for a condition whose first row is this one
			subCondNode=res.createResource();
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssOOS);
			st=res.createStatement(jenaSubject,jenaPredicate,subCondNode);
			res.add(st);
			if (s2!=null && s3!=null && !Utils.isWhiteSpaceCharsOnly(s2) && !Utils.isWhiteSpaceCharsOnly(s3)){
			    if (s2.equals(_wpc)){
				jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssPredicate);
				if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				st=res.createStatement(subCondNode,jenaPredicate,jenaObject);
				res.add(st);
			    }
			    else if (s2.equals(_wsvlc) || s2.equals(_wstc)){
				subCondNodeNode=res.createResource();
				jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSubject);
				st=res.createStatement(subCondNode,jenaPredicate,subCondNodeNode);
				res.add(st);
				jenaPredicate=(Property)addedProperties.get((s2.equals(_wsvlc)) ? GraphStylesheet._gssValue : GraphStylesheet._gssClass);
				if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				st=res.createStatement(subCondNodeNode,jenaPredicate,jenaObject);
				res.add(st);
			    }
			}
		    }
		}
		else if (s2!=null && s3!=null && !Utils.isWhiteSpaceCharsOnly(s2) && !Utils.isWhiteSpaceCharsOnly(s3)){
		    //subcondition for a condition whose first row is above this one;
		    //subCondNode and subCondNodeNode (if exists) should have been set by one of A or B
		    if (s2.equals(_wpc) && subCondNode!=null){
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssPredicate);
			if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			st=res.createStatement(subCondNode,jenaPredicate,jenaObject);
			res.add(st);
		    }
		    else if ((s2.equals(_wovlc) || s2.equals(_wotc) || s2.equals(_wodtc)) && subCondNode!=null){
			if (subCondNodeNode==null){
			    subCondNodeNode=res.createResource();
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssObject);
			    st=res.createStatement(subCondNode,jenaPredicate,subCondNodeNode);
			    res.add(st);
			}
			if (s2.equals(_wovlc)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssValue);
			    jenaObject=res.createLiteral(s3,"");
			}
			else if (s2.equals(_wotc)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssClass);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			}
			else if (s2.equals(_wodtc)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssDatatype);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			}
			st=res.createStatement(subCondNodeNode,jenaPredicate,jenaObject);
			res.add(st);
		    }
		    else if ((s2.equals(_wsvlc) || s2.equals(_wstc)) && subCondNode!=null){
			if (subCondNodeNode==null){
			    subCondNodeNode=res.createResource();
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSubject);
			    st=res.createStatement(subCondNode,jenaPredicate,subCondNodeNode);
			    res.add(st);
			}
			jenaPredicate=(Property)addedProperties.get((s2.equals(_wsvlc)) ? GraphStylesheet._gssValue : GraphStylesheet._gssClass);
			if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			st=res.createStatement(subCondNodeNode,jenaPredicate,jenaObject);
			res.add(st);
		    }
		}
	    }
	    firstRowIndex=lastRowIndex+1;
	    lastRowIndex=getSelectorLastRowIndex(rselTable,firstRowIndex);
	}
	pp.setPBValue(60);
	//literal selectors
	firstRowIndex=0;
	lastRowIndex=getSelectorLastRowIndex(lselTable,firstRowIndex);
	while (firstRowIndex<lselTable.getRowCount()){//for each selector
	    jenaSubject=res.createResource();
	    //rdf:type selector as gss:Literal
	    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._rdfType);
	    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssLiteral);
	    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
	    res.add(st);
	    //visibility
	    s2=(String)lselTable.getValueAt(firstRowIndex,4);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		if (s2.equals(_visvisible)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssVisibility);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssShow);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_vishidden)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssVisibility);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssHide);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_dispnone)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssDisplay);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssNone);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
	    }
	    //layout
	    s2=(String)lselTable.getValueAt(firstRowIndex,5);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		if (s2.equals(_nodearc)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssLayout);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssNodeAndArc);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_table)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssLayout);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssTable);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
	    }
	    //styling
	    s2=(String)lselTable.getValueAt(firstRowIndex,3);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		StringTokenizer tk=new StringTokenizer(s2.trim(),",");
		String s3;
		while (tk.hasMoreTokens()){
		    s3=tk.nextToken().trim();
		    if (!s3.startsWith("#")){s3="#"+s3;}
		    s3=base+s3;
		    if (addedResources.containsKey(s3)){
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssStyle);
			jenaObject=(Resource)addedResources.get(s3);
			st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			res.add(st);
		    }
		}
	    }
	    //conditions (literal selectors)
	    String s3;
	    //at this point jenaSubject is still the anon nodes representing the selector
	    Resource subCondNode=null,subCondNodeNode=null;  //resp. the object of gss:{predicate,object}OfStatement statements and the object of gss:subject statements
	    for (int i=firstRowIndex+1;i<=lastRowIndex;i++){
		if (GSSEditor.isFirstSelectorRow(lselTable,i)){subCondNode=null;subCondNodeNode=null;}  //reset it just to be sure that subconditions in a selector do not get attached to a condition in another selector (should not happen anyway, but this is safer)
		s1=(String)lselTable.getValueAt(i,0);
		s2=(String)lselTable.getValueAt(i,1);
		s3=(String)lselTable.getValueAt(i,2);if (s3!=null){s3=expandBinding(s3.trim());}
		if (s1!=null && !Utils.isWhiteSpaceCharsOnly(s1)){
		    subCondNodeNode=null;//have to reset it
		    if (s1.equals(_vleqc)){
			if (s3!=null && !Utils.isWhiteSpaceCharsOnly(s3)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssValue);
			    jenaObject=res.createLiteral(s3,"");
			    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			    res.add(st);
			}
		    }
		    else if (s1.equals(_dtc)){
			if (s3!=null && !Utils.isWhiteSpaceCharsOnly(s3)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssDatatype);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			    res.add(st);
			}
		    }//C
		    else if (s1.equals(_oosc)){//subcondition for a condition whose first row is this one
			subCondNode=res.createResource();
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssOOS);
			st=res.createStatement(jenaSubject,jenaPredicate,subCondNode);
			res.add(st);
			if (s2!=null && s3!=null && !Utils.isWhiteSpaceCharsOnly(s2) && !Utils.isWhiteSpaceCharsOnly(s3)){
			    if (s2.equals(_wpc)){
				jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssPredicate);
				if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				st=res.createStatement(subCondNode,jenaPredicate,jenaObject);
				res.add(st);
			    }
			    else if (s2.equals(_wsvlc) || s2.equals(_wstc)){
				subCondNodeNode=res.createResource();
				jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSubject);
				st=res.createStatement(subCondNode,jenaPredicate,subCondNodeNode);
				res.add(st);
				jenaPredicate=(Property)addedProperties.get((s2.equals(_wsvlc)) ? GraphStylesheet._gssValue : GraphStylesheet._gssClass);
				if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				st=res.createStatement(subCondNodeNode,jenaPredicate,jenaObject);
				res.add(st);
			    }
			}
		    }
		}
		else if (s2!=null && s3!=null && !Utils.isWhiteSpaceCharsOnly(s2) && !Utils.isWhiteSpaceCharsOnly(s3)){
		    //subcondition for a condition whose first row is above this one;
		    //subCondNode and subCondNodeNode (if exists) should have been set by C
		    if (s2.equals(_wpc) && subCondNode!=null){
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssPredicate);
			if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			st=res.createStatement(subCondNode,jenaPredicate,jenaObject);
			res.add(st);
		    }
		    else if ((s2.equals(_wsvlc) || s2.equals(_wstc)) && subCondNode!=null){
			if (subCondNodeNode==null){
			    subCondNodeNode=res.createResource();
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSubject);
			    st=res.createStatement(subCondNode,jenaPredicate,subCondNodeNode);
			    res.add(st);
			}
			jenaPredicate=(Property)addedProperties.get((s2.equals(_wsvlc)) ? GraphStylesheet._gssValue : GraphStylesheet._gssClass);
			if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			st=res.createStatement(subCondNodeNode,jenaPredicate,jenaObject);
			res.add(st);
		    }
		}
	    }
	    firstRowIndex=lastRowIndex+1;
	    lastRowIndex=getSelectorLastRowIndex(lselTable,firstRowIndex);
	}
	pp.setPBValue(80);
	//property selectors
	firstRowIndex=0;
	lastRowIndex=getSelectorLastRowIndex(pselTable,firstRowIndex);
	while (firstRowIndex<pselTable.getRowCount()){//for each selector
	    jenaSubject=res.createResource();
	    //rdf:type selector as gss:Property
	    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._rdfType);
	    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssProperty);
	    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
	    res.add(st);
	    //visibility
	    s2=(String)pselTable.getValueAt(firstRowIndex,4);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		if (s2.equals(_visvisible)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssVisibility);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssShow);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_vishidden)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssVisibility);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssHide);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_dispnone)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssDisplay);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssNone);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
	    }
	    //layout
	    s2=(String)pselTable.getValueAt(firstRowIndex,5);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		if (s2.equals(_nodearc)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssLayout);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssNodeAndArc);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
		else if (s2.equals(_table)){
		    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssLayout);
		    jenaObject=(Resource)addedResources.get(GraphStylesheet._gssTable);
		    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
		    res.add(st);
		}
	    }
	    //styling
	    s2=(String)pselTable.getValueAt(firstRowIndex,3);
	    if (s2!=null && !Utils.isWhiteSpaceCharsOnly(s2)){
		StringTokenizer tk=new StringTokenizer(s2.trim(),",");
		String s3;
		while (tk.hasMoreTokens()){
		    s3=tk.nextToken().trim();
		    if (!s3.startsWith("#")){s3="#"+s3;}
		    s3=base+s3;
		    if (addedResources.containsKey(s3)){
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssStyle);
			jenaObject=(Resource)addedResources.get(s3);
			st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			res.add(st);
		    }
		}
	    }

	    //conditions (property selectors)
	    String s3;
	    //at this point jenaSubject is still the anon nodes representing the selector
	    Resource subCondNode=null,subCondNodeSubject=null,subCondNodeObject=null;  //resp. the object of gss:predicateOfStatement statements and the objects of gss:{su,o}bject statements
	    for (int i=firstRowIndex+1;i<=lastRowIndex;i++){
		if (GSSEditor.isFirstSelectorRow(pselTable,i)){subCondNode=null;subCondNodeSubject=null;subCondNodeObject=null;}  //reset it just to be sure that subconditions in a selector do not get attached to a condition in another selector (should not happen anyway, but this is safer)
		s1=(String)pselTable.getValueAt(i,0);
		s2=(String)pselTable.getValueAt(i,1);
		s3=(String)pselTable.getValueAt(i,2);if (s3!=null){s3=expandBinding(s3.trim());}
		if (s1!=null && !Utils.isWhiteSpaceCharsOnly(s1)){
		    subCondNodeSubject=null;//have to reset it
		    subCondNodeObject=null;//have to reset it
		    if (s1.equals(_uriswc)){
			if (s3!=null && !Utils.isWhiteSpaceCharsOnly(s3)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssURIsw);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			    res.add(st);
			}
		    }
		    else if (s1.equals(_urieqc)){
			if (s3!=null && !Utils.isWhiteSpaceCharsOnly(s3)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssURIeq);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			    st=res.createStatement(jenaSubject,jenaPredicate,jenaObject);
			    res.add(st);
			}
		    }//D
		    else if (s1.equals(_posc)){//subcondition for a condition whose first row is this one
			subCondNode=res.createResource();
			jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssPOS);
			st=res.createStatement(jenaSubject,jenaPredicate,subCondNode);
			res.add(st);
			if (s2!=null && s3!=null && !Utils.isWhiteSpaceCharsOnly(s2) && !Utils.isWhiteSpaceCharsOnly(s3)){
			    if (s2.equals(_wovlc) || s2.equals(_wotc) || s2.equals(_wodtc)){
				subCondNodeObject=res.createResource();
				jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssObject);
				st=res.createStatement(subCondNode,jenaPredicate,subCondNodeObject);
				res.add(st);
				if (s2.equals(_wovlc)){
				    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssValue);
				    jenaObject=res.createLiteral(s3,"");
				}
				else if (s2.equals(_wotc)){
				    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssClass);
				    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				}
				else if (s2.equals(_wodtc)){
				    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssDatatype);
				    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				}
				st=res.createStatement(subCondNodeObject,jenaPredicate,jenaObject);
				res.add(st);
			    }
			    else if (s2.equals(_wsvlc) || s2.equals(_wstc)){
				subCondNodeSubject=res.createResource();
				jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSubject);
				st=res.createStatement(subCondNode,jenaPredicate,subCondNodeSubject);
				res.add(st);
				jenaPredicate=(Property)addedProperties.get((s2.equals(_wsvlc)) ? GraphStylesheet._gssValue : GraphStylesheet._gssClass);
				if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
				else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
				st=res.createStatement(subCondNodeSubject,jenaPredicate,jenaObject);
				res.add(st);
			    }
			}
		    }
		}
		else if (s2!=null && s3!=null && !Utils.isWhiteSpaceCharsOnly(s2) && !Utils.isWhiteSpaceCharsOnly(s3)){
		    //subcondition for a condition whose first row is above this one;
		    //subCondNode and subCondNodeNode (if exists) should have been set by D
		    if ((s2.equals(_wovlc) || s2.equals(_wotc) || s2.equals(_wodtc)) && subCondNode!=null){
			if (subCondNodeObject==null){
			    subCondNodeObject=res.createResource();
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssObject);
			    st=res.createStatement(subCondNode,jenaPredicate,subCondNodeObject);
			    res.add(st);
			}
			if (s2.equals(_wovlc)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssValue);
			    jenaObject=res.createLiteral(s3,"");
			}
			else if (s2.equals(_wotc)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssClass);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			}
			else if (s2.equals(_wodtc)){
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssDatatype);
			    if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			    else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			}
			st=res.createStatement(subCondNodeObject,jenaPredicate,jenaObject);
			res.add(st);
		    }
		    else if ((s2.equals(_wsvlc) || s2.equals(_wstc)) && subCondNode!=null){
			if (subCondNodeSubject==null){
			    subCondNodeSubject=res.createResource();
			    jenaPredicate=(Property)addedProperties.get(GraphStylesheet._gssSubject);
			    st=res.createStatement(subCondNode,jenaPredicate,subCondNodeSubject);
			    res.add(st);
			}
			jenaPredicate=(Property)addedProperties.get((s2.equals(_wsvlc)) ? GraphStylesheet._gssValue : GraphStylesheet._gssClass);
			if (addedResources.containsKey(s3)){jenaObject=(Resource)addedResources.get(s3);}
			else {jenaObject=res.createResource(s3);addedResources.put(s3,jenaObject);}
			st=res.createStatement(subCondNodeSubject,jenaPredicate,jenaObject);
			res.add(st);
		    }
		}
	    }
	    firstRowIndex=lastRowIndex+1;
	    lastRowIndex=getSelectorLastRowIndex(pselTable,firstRowIndex);
	}
	return res;
    }

    void closeEditor(){
	if (standalone){System.exit(0);}
	else {this.setVisible(false);this.dispose();}
    }

    public void actionPerformed(ActionEvent e){
	Object source=e.getSource();
	if (source==rselAddSBt){addSelector(rselTable,true);}
	else if (source==lselAddSBt){addSelector(lselTable,true);}
	else if (source==pselAddSBt){addSelector(pselTable,true);}
	else if (source==rselAddCBt){addConditionToSelector(rselTable,rselTable.getSelectedRow());}
	else if (source==lselAddCBt){addConditionToSelector(lselTable,lselTable.getSelectedRow());}
	else if (source==pselAddCBt){addConditionToSelector(pselTable,pselTable.getSelectedRow());}
	else if (source==styleAddBt){addStyle();}
	else if (source==sortAddBt){addSort();}
	else if (source==rselRemoveSBt){removeSelector(rselTable,rselTable.getSelectedRow());}
	else if (source==lselRemoveSBt){removeSelector(lselTable,lselTable.getSelectedRow());}
	else if (source==pselRemoveSBt){removeSelector(pselTable,pselTable.getSelectedRow());}
	else if (source==rselRemoveCBt){removeConditionFromSelector(rselTable,rselTable.getSelectedRow());}
	else if (source==lselRemoveCBt){removeConditionFromSelector(lselTable,lselTable.getSelectedRow());}
	else if (source==pselRemoveCBt){removeConditionFromSelector(pselTable,pselTable.getSelectedRow());}
	else if (source==styleRemoveBt){removeStyle(styleTable.getSelectedRow());}
	else if (source==sortRemoveBt){removeSort(sortTable.getSelectedRow());}
	else if (source==resetMn){promptReset();}
	else if (source==loadFileMn){loadFile(false);}
	else if (source==loadURLMn){loadURL(false);}
	else if (source==mergeFileMn){loadFile(true);}
	else if (source==mergeURLMn){loadURL(true);}
	else if (source==saveMn){saveFile();}
	else if (source==saveAsMn){saveFileAs();}
	else if (source==exitMn){closeEditor();}
	else if (source==baseURIMn){setBaseURI();}
	else if (source==gssumMn){displayURLinBrowser(GSS_MANUAL_URI);}
	else if (source==umMn){displayURLinBrowser(GSS_EDITOR_MANUAL_URI);}
	else if (source==aboutMn){aboutGSSEditor();}
    }

    public void keyPressed(KeyEvent e){
	Object source=e.getSource();
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    if (source==rselAddSBt){addSelector(rselTable,true);}
	    else if (source==lselAddSBt){addSelector(lselTable,true);}
	    else if (source==pselAddSBt){addSelector(pselTable,true);}
	    else if (source==rselAddCBt){addConditionToSelector(rselTable,rselTable.getSelectedRow());}
	    else if (source==lselAddCBt){addConditionToSelector(lselTable,lselTable.getSelectedRow());}
	    else if (source==pselAddCBt){addConditionToSelector(pselTable,pselTable.getSelectedRow());}
	    else if (source==styleAddBt){addStyle();}
	    else if (source==sortAddBt){addSort();}
	    else if (source==rselRemoveSBt){removeSelector(rselTable,rselTable.getSelectedRow());}
	    else if (source==lselRemoveSBt){removeSelector(lselTable,lselTable.getSelectedRow());}
	    else if (source==pselRemoveSBt){removeSelector(pselTable,pselTable.getSelectedRow());}
	    else if (source==rselRemoveCBt){removeConditionFromSelector(rselTable,rselTable.getSelectedRow());}
	    else if (source==lselRemoveCBt){removeConditionFromSelector(lselTable,lselTable.getSelectedRow());}
	    else if (source==pselRemoveCBt){removeConditionFromSelector(pselTable,pselTable.getSelectedRow());}
	    else if (source==styleRemoveBt){removeStyle(styleTable.getSelectedRow());}
	    else if (source==sortRemoveBt){removeSort(sortTable.getSelectedRow());}
	}
    }
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }

    void setBaseURI(){
	String uri=JOptionPane.showInputDialog(this,"Enter the Stylesheet's base URI",BASE_URI);
	if (uri!=null){
	    uri=uri.trim();
	    if (uri!=null && uri.length()>0){BASE_URI=uri;}
	    else {BASE_URI="";}
	}
	else {BASE_URI="";}
    }

    String expandBinding(String uri){
	if (isv!=null){return isv.tryToSolveBinding(uri);}
	else {return uri;}
    }

    void setEnumID(){
	if (sortTable.getSelectedRow()>-1){
	    rselTable.setValueAt(sortTable.getValueAt(sortTable.getSelectedRow(),0),selectingEnumID,6);
	}
	else {
	    displayError("This is not a Custom Enumeration ID");
	}
	selectingEnumID=-1;
    }

    static final String aboutMsg="GSS Editor v 0.2\n\nA Graphical Front-End for Graph Stylesheets\n\nhttp://www.w3.org/2001/11/IsaViz/gss/gssmanual.html\n\nWritten by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)";

    void displayError(String msg){
	statusBar.setForeground(Color.red);
	statusBar.setText(msg);
    }

    void displayMsg(String msg){
	statusBar.setForeground(Color.black);
	statusBar.setText(msg);
    }

    void displayURLinBrowser(String uri){
	(new WebBrowser()).show(uri);
    }

    public void aboutGSSEditor(){
	javax.swing.JOptionPane.showMessageDialog(this,aboutMsg);
    }

    public static void main(String[] args){
	ConfigManager.initLookAndFeel();
	if (args.length==0){
	    new GSSEditor(null);
	}
	else if (args.length==1){
	    URL stylesheet=null;
	    try {
		File f=new File(args[0]);
		if (f.exists()){stylesheet=f.toURL();}
	    }
	    catch (Exception ex){}
	    if (stylesheet==null){
		try {
		    stylesheet=new URL(args[0]);
		}
		catch (Exception ex){}
	    }
	    if (stylesheet!=null){
		(new GSSEditor(null)).loadStylesheet(stylesheet,false);
	    }
	    else {new GSSEditor(null);}
	}
	else {
	    System.err.println("Usage:\njava -cp <classpath> org.w3c.IsaVIz.GSSEditor [<file>|<URL>]\n\tfile: a (relative or absolute) local file path\n\turl: a file:// or http:// URL");
	    System.exit(0);
	}
    }

    /*listening to the updates to prevent bad subconditions to be associated with some conditions*/
    TableModelListener l1=new TableModelListener(){//listener for the resource selector table
	    public void tableChanged(TableModelEvent e){
		if (e.getColumn()==1){
		    if (e.getType()==TableModelEvent.UPDATE){
			int row=e.getFirstRow();
			String value=(String)rselTable.getValueAt(row,1);
			if (value!=null){
			    String primaryCnstrnt=(String)rselTable.getValueAt(row,0);
			    if (primaryCnstrnt!=null){
				if (primaryCnstrnt.equals(GSSEditor._uriswc) || primaryCnstrnt.equals(GSSEditor._urieqc)){
				    rselTable.setValueAt(null,row,1);
				    displayError("No subconstraint allowed here");
				}
				else if (primaryCnstrnt.equals(GSSEditor._sosc) && !(value.equals(GSSEditor._wpc) || value.equals(GSSEditor._wovlc) || value.equals(GSSEditor._wotc) || value.equals(GSSEditor._wodtc))){
				    rselTable.setValueAt(null,row,1);
				    displayError("Only the following subconstraints are allowed here: '"+GSSEditor._wpc+"' , '"+GSSEditor._wovlc+"' , '"+GSSEditor._wotc+"' , '"+GSSEditor._wodtc+"'");
				}
				else if (primaryCnstrnt.equals(GSSEditor._oosc) && !(value.equals(GSSEditor._wpc) || value.equals(GSSEditor._wsvlc) || value.equals(GSSEditor._wstc))){
				    rselTable.setValueAt(null,row,1);
				    displayError("Only the following subconstraints are allowed here: "+GSSEditor._wpc+"' , '"+GSSEditor._wsvlc+"' , '"+GSSEditor._wstc+"'");
				}
			    }
			    else if ((primaryCnstrnt=GSSEditor.getParentCnstrnt(rselTable,row))!=null){
				if (primaryCnstrnt.equals(GSSEditor._uriswc) || primaryCnstrnt.equals(GSSEditor._urieqc)){
				    rselTable.setValueAt(null,row,1);
				    displayError("No subconstraint allowed here");
				}
				else if (primaryCnstrnt.equals(GSSEditor._sosc) && !(value.equals(GSSEditor._wpc) || value.equals(GSSEditor._wovlc) || value.equals(GSSEditor._wotc) || value.equals(GSSEditor._wodtc))){
				    rselTable.setValueAt(null,row,1);
				    displayError("Only the following subconstraints are allowed here: "+GSSEditor._wpc+"' , '"+GSSEditor._wovlc+"' , '"+GSSEditor._wotc+"' , '"+GSSEditor._wodtc+"'");
				}
				else if (primaryCnstrnt.equals(GSSEditor._oosc) && !(value.equals(GSSEditor._wpc) || value.equals(GSSEditor._wsvlc) || value.equals(GSSEditor._wstc))){
				    rselTable.setValueAt(null,row,1);
				    displayError("Only the following subconstraints are allowed here: "+GSSEditor._wpc+"' , '"+GSSEditor._wsvlc+"' , '"+GSSEditor._wstc+"'");
				}
			    }
			    else {
				rselTable.setValueAt(null,row,1);
				displayError("No subconstraint allowed here");
			    }
			}
		    }
		}
		else if (e.getColumn()==6){
		    if (e.getType()==TableModelEvent.UPDATE){
			int row=e.getFirstRow();
			String value=(String)rselTable.getValueAt(row,6);
			if (value!=null && value.equals(_enum)){
			    rselTable.setValueAt(_selectEnum,row,6);
			    selectingEnumID=row;
			    displayMsg("Select a Custom Enumeration ID");
			}
		    }
		}
	    }
	};

    /*listening to the updates to prevent bad subconditions to be associated with some conditions*/
    TableModelListener l2=new TableModelListener(){//listener for the literal selector table
	    public void tableChanged(TableModelEvent e){
		if (e.getType()==TableModelEvent.UPDATE && e.getColumn()==1){
		    int row=e.getFirstRow();
		    String value=(String)lselTable.getValueAt(row,1);
		    if (value!=null){
			String primaryCnstrnt=(String)lselTable.getValueAt(row,0);
			if (primaryCnstrnt!=null){
			    if (primaryCnstrnt.equals(GSSEditor._vleqc) || primaryCnstrnt.equals(GSSEditor._dtc)){
				lselTable.setValueAt(null,row,1);
				displayError("No subconstraint allowed here");
			    }
			}
			else if ((primaryCnstrnt=GSSEditor.getParentCnstrnt(lselTable,row))!=null){
			    if (primaryCnstrnt.equals(GSSEditor._vleqc) || primaryCnstrnt.equals(GSSEditor._dtc)){
				lselTable.setValueAt(null,row,1);
				displayError("No subconstraint allowed here");
			    }
			}
			else {
			    lselTable.setValueAt(null,row,1);
			    displayError("No subconstraint allowed here");
			}
		    }
		}
	    }
	};

    /*listening to the updates to prevent bad subconditions to be associated with some conditions*/
    TableModelListener l3=new TableModelListener(){//listener for the property selector table
	    public void tableChanged(TableModelEvent e){
		if (e.getType()==TableModelEvent.UPDATE && e.getColumn()==1){
		    int row=e.getFirstRow();
		    String value=(String)pselTable.getValueAt(row,1);
		    if (value!=null){
			String primaryCnstrnt=(String)pselTable.getValueAt(row,0);
			if (primaryCnstrnt!=null){
			    if (primaryCnstrnt.equals(GSSEditor._uriswc) || primaryCnstrnt.equals(GSSEditor._urieqc)){
				pselTable.setValueAt(null,row,1);
				displayError("No subconstraint allowed here");
			    }
			}
			else if ((primaryCnstrnt=GSSEditor.getParentCnstrnt(pselTable,row))!=null){
			    if (primaryCnstrnt.equals(GSSEditor._uriswc) || primaryCnstrnt.equals(GSSEditor._urieqc)){
				pselTable.setValueAt(null,row,1);
				displayError("No subconstraint allowed here");
			    }
			}
			else {
			    pselTable.setValueAt(null,row,1);
			    displayError("No subconstraint allowed here");
			}
		    }
		}
	    }
	};

    TableModelListener l4=new TableModelListener(){//listener for the style table
	    public void tableChanged(TableModelEvent e){
		int column=e.getColumn();
		int row=e.getFirstRow();
		if (e.getType()==TableModelEvent.UPDATE){
		    if (column==6){
			String value=(String)styleTable.getValueAt(row,6);
			if (value!=null){
			    if (value.equals(_iconFile)){
				JFileChooser fc=new JFileChooser((lastGSSFile!=null) ? lastGSSFile.getParentFile() : new File("."));
				fc.setDialogTitle("Choose a Local Bitmap Icon File");
				int returnVal=fc.showOpenDialog(GSSEditor.this);
				if (returnVal==JFileChooser.APPROVE_OPTION){
				    try {
					styleTable.setValueAt(fc.getSelectedFile().toURL().toString(),row,6);
				    }
				    catch (MalformedURLException ex){styleTable.setValueAt(null,row,6);}
				}
				else {styleTable.setValueAt(null,row,6);}
			    }
			    else if (value.equals(_iconURI)){
				String uri=JOptionPane.showInputDialog(GSSEditor.this,"Bitmap icon's URI:","Choose a Local/Remote Bitmap Icon File",JOptionPane.PLAIN_MESSAGE);
				if (uri!=null){uri=uri.trim();}
				if (uri!=null && uri.length()>0){
				    uri=expandBinding(uri);
				    styleTable.setValueAt(uri,row,6);
				}
				else {
				    styleTable.setValueAt(null,row,6);
				}
			    }
			    else if (value.equals(_iconFetchR)){
				styleTable.setValueAt(_iconFetch,row,6);
			    }
			    else if (value.equals("")){
				styleTable.setValueAt(null,row,6);
			    }
			}
		    }
		    else if (column==5){
			String value=(String)styleTable.getValueAt(row,5);
			if (value!=null){
			    if (value.equals(_custompg)){
				String coords=JOptionPane.showInputDialog(GSSEditor.this,"Enter a list of coordinates following this syntax: {x1,y1;x2,y2;...}","{}");
				styleTable.setValueAt(coords,row,5);
			    }
			    else if (value.equals(_customsh)){
				String s;
				Color c;
				Color fillColor=Color.white;
				if ((s=(String)styleTable.getValueAt(row,1))!=null && !Utils.isWhiteSpaceCharsOnly(s)){
				    c=com.xerox.VTM.svg.SVGReader.getColor(s);
				    if (c!=null){fillColor=c;}
				}
				Color borderColor=Color.black;
				if ((s=(String)styleTable.getValueAt(row,2))!=null && !Utils.isWhiteSpaceCharsOnly(s)){
				    c=com.xerox.VTM.svg.SVGReader.getColor(s);
				    if (c!=null){borderColor=c;}
				}
				double[] vertices=GSSEditor.defaultShape;
				int vertexCount=GSSEditor.defaultShape.length;
				double angle=0.0;
				//the code below is commented because for now styleTable.getValueAt(row,5) holds "Custom Shape...", not the previous value
// 				float[] verticesf;
// 				if ((s=(String)styleTable.getValueAt(row,5))!=null && !Utils.isWhiteSpaceCharsOnly(s)){
// 				    System.err.println("a");
// 				    verticesf=Style.parseCustomShape(s);
// 				    if (verticesf!=null){
// 					System.err.println("b");
// 					vertexCount=verticesf.length;
// 					vertices=new double[vertexCount];
// 					for (int i=0;i<vertexCount;i++){
// 					    vertices[i]=(double)verticesf[i];
// 					}
// 					System.err.println("c");
// 					String orient=null;
// 					if (s.lastIndexOf("]")<s.length()-1){//there might be an orientation value
// 					    orient=s.substring(s.lastIndexOf("]")+1).trim();
// 					    if (!Utils.isWhiteSpaceCharsOnly(orient)){
// 						try {angle=new Double(orient).doubleValue();}
// 						catch (NumberFormatException ex){}
// 					    }
// 					}
// 					System.err.println("d"+vertices+" "+angle);
// 				    }
// 				    else {vertices=GSSEditor.defaultShape;vertexCount=GSSEditor.defaultShape.length;}
// 				}
				com.xerox.VTM.glyphs.VShape g=(com.xerox.VTM.glyphs.VShape)net.claribole.zvtm.glyphs.GlyphFactory.getGlyphFactoryDialog(GSSEditor.this,GSSEditor.glyphFactoryShapes,net.claribole.zvtm.glyphs.GlyphFactory.V_Shape,false,fillColor,false,borderColor,false,false,false,1.0,false,true,true,angle,true,50,false,vertexCount,true,vertices,true);
				if (g!=null){
				    value="["+Utils.arrayOffloatAsCSStrings(g.getVertices())+"]";
				    angle=g.getOrient();
				    if (angle!=0.0){value+=" "+Double.toString(angle);}
				    styleTable.setValueAt(value,row,5);
				}
				else {styleTable.setValueAt(null,row,5);}
			    }
			}
		    }
		    else if (column==4){
			String value=(String)styleTable.getValueAt(row,4);
			if (value!=null){
			    if (value.equals(_daCustomPat)){
				String coords=JOptionPane.showInputDialog(GSSEditor.this,"Enter a list of floats representing the lengths of the dash segments\nfollowing this syntax: n1,n2,n3,...");
				styleTable.setValueAt(coords,row,4);
			    }
			}
		    }
		}
	    }
	};

    /*used to clear status bar from previous (error message)*/
    MouseAdapter m1=new MouseAdapter(){
	    public void mousePressed(MouseEvent e){
		displayMsg(" ");
		if (selectingEnumID>-1){setEnumID();}
	    }
	};

}

class RSTableModel extends DefaultTableModel {

    JTable table;

    RSTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    protected void setTable(JTable t){
	this.table=t;
    }

    public boolean isCellEditable(int row,int column){
	if ((column==0 || column==1 || column==2)){
	    if (GSSEditor.isFirstSelectorRow(table,row)){return false;}
	    else {return true;}
	}
	else {
	    if (GSSEditor.isFirstSelectorRow(table,row)){return true;}
	    else {return false;}
	}
    }

}

class LSTableModel extends DefaultTableModel {

    JTable table;

    LSTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    protected void setTable(JTable t){
	this.table=t;
    }

    public boolean isCellEditable(int row,int column){
	if ((column==0 || column==1 || column==2)){
	    if (GSSEditor.isFirstSelectorRow(table,row)){return false;}
	    else {return true;}
	}
	else {
	    if (GSSEditor.isFirstSelectorRow(table,row)){return true;}
	    else {return false;}
	}
    }

}

class PSTableModel extends DefaultTableModel {

    JTable table;

    PSTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    protected void setTable(JTable t){
	this.table=t;
    }

    public boolean isCellEditable(int row,int column){
	if ((column==0 || column==1 || column==2)){
	    if (GSSEditor.isFirstSelectorRow(table,row)){return false;}
	    else {return true;}
	}
	else {
	    if (GSSEditor.isFirstSelectorRow(table,row)){return true;}
	    else {return false;}
	}
    }

}

class StyleTableModel extends DefaultTableModel {

    StyleTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

}

class SortTableModel extends DefaultTableModel {

    SortTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

}

class STableRenderer extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
	if (row<table.getRowCount()){
	    Component res=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
	    boolean isSelectorFirstRow=(table.getValueAt(row,0)!=null) ? ((String)table.getValueAt(row,0)).equals(GSSEditor._sweight) : false;
	    if (isSelectorFirstRow){
		((JComponent)res).setOpaque(true);
		res.setForeground(ConfigManager.darkerPastelBlue);
		res.setBackground(Color.lightGray);
	    }
	    else {
		((JComponent)res).setOpaque(true);
		res.setForeground(Color.black);
		res.setBackground(Color.white);
	    }
	    if (isSelected){((JComponent)res).setBorder(GSSEditor.selectedCellBorder);}
	    else if (row==table.getSelectedRow()){((JComponent)res).setBorder(GSSEditor.selectedRowBorder);}
	    return res;
	}
	else return null;
    }

}

class StyleTableRenderer extends DefaultTableCellRenderer {

    JLabel ellipse=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VEllipse(0,0,0,20,10,Color.white),20,20));
    JLabel rectangle=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VRectangle(0,0,0,20,10,Color.white),20,20));
    JLabel roundrect=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VRoundRect(0,0,0,20,10,Color.white,1,1),20,20));
    JLabel circle=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VCircle(0,0,0,15,Color.white),15,15));
    JLabel diamond=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VDiamond(0,0,0,15,Color.white),15,15));
    JLabel octagon=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VOctagon(0,0,0,15,Color.white),15,15));
    JLabel trianglen=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VTriangle(0,0,0,15,Color.white),15,15));
    JLabel triangles=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VTriangleOr(0,0,0,15,Color.white,(float)Math.PI),15,15));
    JLabel trianglee=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VTriangleOr(0,0,0,15,Color.white,(float)-Math.PI/2.0f),15,15));
    JLabel trianglew=new JLabel(net.claribole.zvtm.glyphs.GlyphIcon.getGlyphIcon(new VTriangleOr(0,0,0,15,Color.white,(float)Math.PI/2.0f),15,15));

    public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
	Component res;
	if (row<table.getRowCount()){
	    if (column==5){
		if (value!=null){
		    String s=(String)value;
		    if (value.equals(GSSEditor._ellipse)){res=ellipse;}
		    else if (value.equals(GSSEditor._rectangle)){res=rectangle;}
		    else if (value.equals(GSSEditor._roundrect)){res=roundrect;}
		    else if (value.equals(GSSEditor._circle)){res=circle;}
		    else if (value.equals(GSSEditor._diamond)){res=diamond;}
		    else if (value.equals(GSSEditor._octagon)){res=octagon;}
		    else if (value.equals(GSSEditor._trianglen)){res=trianglen;}
		    else if (value.equals(GSSEditor._triangles)){res=triangles;}
		    else if (value.equals(GSSEditor._trianglee)){res=trianglee;}
		    else if (value.equals(GSSEditor._trianglew)){res=trianglew;}
		    else {res=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);}
		}
		else {res=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);}
		if (isSelected){((JComponent)res).setBorder(GSSEditor.selectedCellBorder);}
		else if (row==table.getSelectedRow()){((JComponent)res).setBorder(GSSEditor.selectedRowBorder);}
		return res;
	    }
	    else {
		res=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		((JComponent)res).setOpaque(true);
		if (column==1){
		    if (table.getValueAt(row,2)!=null){
			res.setForeground(com.xerox.VTM.svg.SVGReader.getColor((String)table.getValueAt(row,2)));
		    }
		    else {res.setForeground(Color.black);}
		    if (value!=null){
			res.setBackground(com.xerox.VTM.svg.SVGReader.getColor((String)value));
		    }
		}
		else if (column==2){
		    if (table.getValueAt(row,1)!=null){
			res.setBackground(com.xerox.VTM.svg.SVGReader.getColor((String)table.getValueAt(row,1)));
		    }
		    else {res.setBackground(Color.white);}
		    if (value!=null){
			res.setForeground(com.xerox.VTM.svg.SVGReader.getColor((String)value));
		    }
		}
		else {
		    res.setForeground(Color.black);
		    res.setBackground(Color.white);
		}
		if (isSelected){((JComponent)res).setBorder(GSSEditor.selectedCellBorder);}
		else if (row==table.getSelectedRow()){((JComponent)res).setBorder(GSSEditor.selectedRowBorder);}
		if (column==6 && value!=null){((JComponent)res).setToolTipText((value.toString()));}
		return res;
	    }
	}
	else return null;
    }
}

class SortTableRenderer extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
	if (row<table.getRowCount()){
	    Component res=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
	    ((JComponent)res).setOpaque(true);
	    res.setForeground(Color.black);
	    res.setBackground(Color.white);
	    if (isSelected){((JComponent)res).setBorder(GSSEditor.selectedCellBorder);}
	    else if (row==table.getSelectedRow()){((JComponent)res).setBorder(GSSEditor.selectedRowBorder);}
	    return res;
	}
	else return null;
    }

}

class GSSSerializeErrorHandler implements RDFErrorHandler {

    GSSEditor editor;
    
    GSSSerializeErrorHandler(GSSEditor ed){
	this.editor=ed;
    }
    
    public void error(java.lang.Exception ex){
	System.err.println("An error occured while saving the GSS Stylesheet "+ex+"\n");
	editor.reportError=true;
    }

    public void fatalError(java.lang.Exception ex){
	System.err.println("A fatal error occured while saving the GSS Stylesheet "+ex+"\n");
	editor.reportError=true;
    }

    public void warning(java.lang.Exception ex){
	System.err.println("Warning (exporting GSS stylesheet) "+ex+"\n");
	editor.reportError=true;
    }
    
}


