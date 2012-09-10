/*   FILE: PrefWindow.java
 *   DATE OF CREATION:   10/22/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Oct 15 08:54:32 2004 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: PrefWindow.java,v 1.12 2004/10/18 12:24:33 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import net.claribole.zvtm.fonts.FontDialog;

class PrefWindow extends JFrame implements ActionListener,KeyListener,MouseListener {

    JTabbedPane tabbedPane;

    JButton okPrefs,savePrefs;

    //directory panel
    JButton brw1,brw2,brw3,brw4,brw5;
    JTextField tf1,tf2,tf3,tf4,tf5;
    JCheckBox cb1;

    //web browser panel
    JRadioButton detectBrowserBt,specifyBrowserBt;
    JTextField browserPathTf,browserOptsTf;
    JButton brw6,webHelpBt;
    JLabel pathLb,optLb;

    //proxy/firewall
    JCheckBox useProxyCb;
    JLabel proxyHostLb,proxyPortLb;
    JTextField proxyHostTf,proxyPortTf;
    JButton proxyHelpBt/*,memCacheBt*/;
    
    //Misc prefs
    JTextField tf1a,tf1c,tf2a;
    //JSpinner spinner;  //for number of characters displayed in literals
    JTextField spinner;  //use this instead (much more primitive) since JSpinner is only available since jdk 1.4 (and we want to be compatible with 1.3.x for now)
    JCheckBox cb1a,cb1b,cb1c,dispAsLabelCb,allowPfxCb;
    JRadioButton parseStrictBt,parseDefaultBt,parseLaxBt;
    
    //rendering panel
    ColorIndicator colInd;
    JButton fontBt;
    JLabel fontInd;
    JButton sfontBt;
    JLabel sfontInd;
    JRadioButton b1a,b2a;
    JCheckBox antialiascb,saveWindowLayoutCb,incGSSCb;

    Editor application;

    PrefWindow(Editor e){
	application=e;

	tabbedPane = new JTabbedPane();

	//misc panel
	JPanel miscPane=new JPanel();
	GridBagLayout gridBag0=new GridBagLayout();
	GridBagConstraints constraints0=new GridBagConstraints();
	constraints0.fill=GridBagConstraints.HORIZONTAL;
	constraints0.anchor=GridBagConstraints.WEST;
	miscPane.setLayout(gridBag0);

	JPanel mp0=new JPanel();
	mp0.setBorder(BorderFactory.createEmptyBorder());
	mp0.setLayout(new GridLayout(1,2));
	JLabel lb0=new JLabel("  Default Base URI:");
	mp0.add(lb0);
	constraints0.fill=GridBagConstraints.HORIZONTAL;
	tf1a=new JTextField(Editor.DEFAULT_BASE_URI);
	mp0.add(tf1a);
	buildConstraints(constraints0,0,0,2,1,100,10);
	gridBag0.setConstraints(mp0,constraints0);
	miscPane.add(mp0);

	JPanel mp1=new JPanel();
	mp1.setBorder(BorderFactory.createEmptyBorder());
	mp1.setLayout(new GridLayout(1,2));
	JLabel lb1=new JLabel("  Anonymous Node Prefix (without ':')");
	mp1.add(lb1);
	tf2a=new JTextField(Editor.ANON_NODE.substring(0,Editor.ANON_NODE.length()-1)); //do not display ':' in the textfield (appended automatically)
	mp1.add(tf2a);
	buildConstraints(constraints0,0,1,2,1,100,10);
	gridBag0.setConstraints(mp1,constraints0);
	miscPane.add(mp1);

	constraints0.fill=GridBagConstraints.NONE;
	cb1c=new JCheckBox("Always Include xml:lang in Literals - Default:");
	cb1c.setSelected(Editor.ALWAYS_INCLUDE_LANG_IN_LITERALS);
	buildConstraints(constraints0,0,2,1,1,70,10);
	gridBag0.setConstraints(cb1c,constraints0);
	miscPane.add(cb1c);
	constraints0.fill=GridBagConstraints.HORIZONTAL;
	tf1c=new JTextField(Editor.DEFAULT_LANGUAGE_IN_LITERALS);
	buildConstraints(constraints0,1,2,1,1,30,0);
	gridBag0.setConstraints(tf1c,constraints0);
	miscPane.add(tf1c);
	constraints0.fill=GridBagConstraints.NONE;
	cb1a=new JCheckBox("Use Abbreviated RDF Syntax");
	cb1a.setSelected(Editor.ABBREV_SYNTAX);
	buildConstraints(constraints0,0,3,2,1,100,10);
	gridBag0.setConstraints(cb1a,constraints0);
	miscPane.add(cb1a);
	cb1b=new JCheckBox("Show Anonymous IDs");
	cb1b.setSelected(ConfigManager.SHOW_ANON_ID);
	buildConstraints(constraints0,0,4,2,1,100,10);
	gridBag0.setConstraints(cb1b,constraints0);
	miscPane.add(cb1b);
	dispAsLabelCb=new JCheckBox("Display Label as Resource Text When Available",Editor.DISP_AS_LABEL);
	buildConstraints(constraints0,0,5,2,1,100,10);
	gridBag0.setConstraints(dispAsLabelCb,constraints0);
	miscPane.add(dispAsLabelCb);
	allowPfxCb=new JCheckBox("Allow Namespace Reference by Bound Prefix in Input Textfields",ConfigManager.ALLOW_PFX_IN_TXTFIELDS);
	buildConstraints(constraints0,0,6,2,1,100,10);
	gridBag0.setConstraints(allowPfxCb,constraints0);
	miscPane.add(allowPfxCb);
	JLabel l47=new JLabel("  Max. Nb. of Chars. Displayed in Literals:");
	buildConstraints(constraints0,0,7,1,1,70,10);
	gridBag0.setConstraints(l47,constraints0);
	miscPane.add(l47);
	constraints0.fill=GridBagConstraints.HORIZONTAL;
// 	spinner=new JSpinner(new SpinnerNumberModel(Editor.MAX_LIT_CHAR_COUNT,0,80,1));
	spinner=new JTextField(String.valueOf(Editor.MAX_LIT_CHAR_COUNT)); //use this instead (much more primitive) since JSpinner is only available since jdk 1.4 (and we want to be compatible with 1.3.x for now)
	spinner.addKeyListener(this);
	buildConstraints(constraints0,1,7,1,1,30,0);
	gridBag0.setConstraints(spinner,constraints0);
	miscPane.add(spinner);
	JPanel parsePanel=new JPanel();
	parseDefaultBt=new JRadioButton("Default");
	parseStrictBt=new JRadioButton("Strict");
	parseLaxBt=new JRadioButton("Laxist");
	ButtonGroup bg86=new ButtonGroup();
	bg86.add(parseStrictBt);
	bg86.add(parseDefaultBt);
	bg86.add(parseLaxBt);
	parsePanel.setLayout(new FlowLayout());
	parsePanel.add(new JLabel("Parsing Mode: "));
	parsePanel.add(parseDefaultBt);
	parsePanel.add(parseStrictBt);
	parsePanel.add(parseLaxBt);
	if (ConfigManager.PARSING_MODE==ConfigManager.STRICT_PARSING){parseStrictBt.setSelected(true);} 
	else if (ConfigManager.PARSING_MODE==ConfigManager.LAX_PARSING){parseLaxBt.setSelected(true);} 
	else {parseDefaultBt.setSelected(true);}
	constraints0.fill=GridBagConstraints.NONE;
	constraints0.anchor=GridBagConstraints.WEST;
	buildConstraints(constraints0,0,8,2,1,100,10);
	gridBag0.setConstraints(parsePanel,constraints0);
	miscPane.add(parsePanel);
	tabbedPane.addTab("Misc.",miscPane);

	//directories panel
	FocusListener fl0=new FocusListener(){
		public void focusGained(FocusEvent e){}
		public void focusLost(FocusEvent e){
		    Object src = e.getSource();
		    if (src == tf1){
			File fl = new File(tf1.getText().trim());
			if (fl.exists()){
			    if (fl.isDirectory()){
				Editor.m_TmpDir = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notADirectory + tf1.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf1.getText());
			}
		    }
		    else if (src == tf2){
			File fl = new File(tf2.getText().trim());
			if (fl.exists()){
			    if (fl.isDirectory()){
				Editor.projectDir = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notADirectory + tf2.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf2.getText());
			}
		    }
		    else if (src == tf3){
			File fl = new File(tf3.getText().trim());
			if (fl.exists()){
			    if (fl.isDirectory()){
				Editor.rdfDir = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notADirectory + tf3.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf3.getText());
			}
		    }
		    else if (src == tf4){
			File fl = new File(tf4.getText().trim());
			if (fl.exists()){
			    if (fl.isFile()){
				Editor.m_GraphVizPath = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notAFile + tf4.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf4.getText());
			}
		    }
		    else if (src == tf5){
			File fl = new File(tf5.getText().trim());
			if (fl.exists()){
			    if (fl.isDirectory()){
				Editor.m_GraphVizFontDir = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notADirectory + tf5.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf5.getText());
			}
		    }
		}
	    };

	JPanel dirPane=new JPanel();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.WEST;
	dirPane.setLayout(gridBag);
	JLabel l1=new JLabel("Temporary directory");
	buildConstraints(constraints,0,0,1,1,60,10);
	gridBag.setConstraints(l1,constraints);
	dirPane.add(l1);
	cb1=new JCheckBox("Delete temp files on exit");
	buildConstraints(constraints,1,0,1,1,30,0);
	gridBag.setConstraints(cb1,constraints);
	if (Editor.dltOnExit){cb1.setSelected(true);} else {cb1.setSelected(false);}
	cb1.addActionListener(this);
	dirPane.add(cb1);
	brw1=new JButton("Browse...");
	buildConstraints(constraints,2,0,1,1,10,0);
	gridBag.setConstraints(brw1,constraints);
	brw1.addActionListener(this);
	dirPane.add(brw1);
	tf1=new JTextField(Editor.m_TmpDir.toString());
	buildConstraints(constraints,0,1,3,1,100,10);
	gridBag.setConstraints(tf1,constraints);
	dirPane.add(tf1);
	tf1.addFocusListener(fl0);
	JLabel l2=new JLabel("Project directory");
	buildConstraints(constraints,0,2,2,1,90,10);
	gridBag.setConstraints(l2,constraints);
	dirPane.add(l2);
	brw2=new JButton("Browse...");
	buildConstraints(constraints,2,2,1,1,10,0);
	gridBag.setConstraints(brw2,constraints);
	brw2.addActionListener(this);
	dirPane.add(brw2);
	tf2=new JTextField(Editor.projectDir.toString());
	buildConstraints(constraints,0,3,3,1,100,10);
	gridBag.setConstraints(tf2,constraints);
	dirPane.add(tf2);
	tf2.addFocusListener(fl0);
	JLabel l3=new JLabel("RDF directory");
	buildConstraints(constraints,0,4,2,1,90,10);
	gridBag.setConstraints(l3,constraints);
	dirPane.add(l3);
	brw3=new JButton("Browse...");
	buildConstraints(constraints,2,4,1,1,10,0);
	gridBag.setConstraints(brw3,constraints);
	brw3.addActionListener(this);
	dirPane.add(brw3);
	tf3=new JTextField(Editor.rdfDir.toString());
	buildConstraints(constraints,0,5,3,1,100,10);
	gridBag.setConstraints(tf3,constraints);
	dirPane.add(tf3);
	tf3.addFocusListener(fl0);
	JLabel l4=new JLabel("GraphViz DOT executable (version 1.8.9 or later required)");
	buildConstraints(constraints,0,6,2,1,90,10);
	gridBag.setConstraints(l4,constraints);
	dirPane.add(l4);
	brw4=new JButton("Browse...");
	buildConstraints(constraints,2,6,1,1,10,0);
	gridBag.setConstraints(brw4,constraints);
	brw4.addActionListener(this);
	dirPane.add(brw4);
	tf4=new JTextField(Editor.m_GraphVizPath.toString());
	buildConstraints(constraints,0,7,3,1,100,10);
	gridBag.setConstraints(tf4,constraints);
	dirPane.add(tf4);
	tf4.addFocusListener(fl0);
	JLabel l5=new JLabel("GraphViz font directory");
	buildConstraints(constraints,0,8,2,1,90,10);
	gridBag.setConstraints(l5,constraints);
	dirPane.add(l5);
	brw5=new JButton("Browse...");
	buildConstraints(constraints,2,8,1,1,10,0);
	gridBag.setConstraints(brw5,constraints);
	brw5.addActionListener(this);
	dirPane.add(brw5);
	tf5=new JTextField(Editor.m_GraphVizFontDir.toString());
	buildConstraints(constraints,0,9,3,1,100,10);
	gridBag.setConstraints(tf5,constraints);
	dirPane.add(tf5);
	tf5.addFocusListener(fl0);
	tabbedPane.addTab("Directories",dirPane);

	//web browser panel
	JPanel webPane=new JPanel();
	GridBagLayout gridBag2=new GridBagLayout();
	GridBagConstraints constraints2=new GridBagConstraints();
	constraints2.fill=GridBagConstraints.HORIZONTAL;
	constraints2.anchor=GridBagConstraints.WEST;
	webPane.setLayout(gridBag2);
	ButtonGroup bg2=new ButtonGroup();
	detectBrowserBt=new JRadioButton("Automatically Detect Default Browser");
	buildConstraints(constraints2,0,0,3,1,100,1);
	gridBag2.setConstraints(detectBrowserBt,constraints2);
	detectBrowserBt.addActionListener(this);
	bg2.add(detectBrowserBt);
	webPane.add(detectBrowserBt);
	specifyBrowserBt=new JRadioButton("Specify Browser:");
	buildConstraints(constraints2,0,1,3,1,100,1);
	gridBag2.setConstraints(specifyBrowserBt,constraints2);
	specifyBrowserBt.addActionListener(this);
	bg2.add(specifyBrowserBt);
	webPane.add(specifyBrowserBt);
	JPanel p7=new JPanel();
	buildConstraints(constraints2,0,2,1,1,10,1);
	gridBag2.setConstraints(p7,constraints2);
	webPane.add(p7);
	pathLb=new JLabel("Path");
	buildConstraints(constraints2,1,2,1,1,80,0);
	gridBag2.setConstraints(pathLb,constraints2);
	webPane.add(pathLb);
	brw6=new JButton("Browse...");
	buildConstraints(constraints2,2,2,1,1,10,0);
	gridBag2.setConstraints(brw6,constraints2);
	brw6.addActionListener(this);
	webPane.add(brw6);
	browserPathTf=new JTextField(Editor.browserPath.toString());
	buildConstraints(constraints2,1,3,2,1,90,1);
	gridBag2.setConstraints(browserPathTf,constraints2);
	webPane.add(browserPathTf);
	optLb=new JLabel("Command Line Options");
	buildConstraints(constraints2,1,4,2,1,90,1);
	gridBag2.setConstraints(optLb,constraints2);
	webPane.add(optLb);
	browserOptsTf=new JTextField(Editor.browserOptions);
	buildConstraints(constraints2,1,5,2,1,90,1);
	gridBag2.setConstraints(browserOptsTf,constraints2);
	webPane.add(browserOptsTf);
	//fill out empty space
	JPanel p8=new JPanel();
	buildConstraints(constraints2,0,6,3,1,100,92);
	gridBag2.setConstraints(p8,constraints2);
	webPane.add(p8);
	webHelpBt=new JButton("Help");
	buildConstraints(constraints2,2,7,1,1,10,1);
	gridBag2.setConstraints(webHelpBt,constraints2);
	webHelpBt.addActionListener(this);
	webPane.add(webHelpBt);
	if (Editor.autoDetectBrowser){detectBrowserBt.doClick();} //select and fire event
	else {specifyBrowserBt.doClick();} //so that fields get enabled/disabled as is approriate
	tabbedPane.addTab("Web Browser",webPane);

	//proxy panel
	JPanel proxyPane=new JPanel();
	GridBagLayout gridBag5=new GridBagLayout();
	GridBagConstraints constraints5=new GridBagConstraints();
	constraints5.fill=GridBagConstraints.HORIZONTAL;
	constraints5.anchor=GridBagConstraints.WEST;
	proxyPane.setLayout(gridBag5);
	useProxyCb=new JCheckBox("Use Proxy Server");
	buildConstraints(constraints5,0,0,2,1,100,1);
	gridBag5.setConstraints(useProxyCb,constraints5);
	useProxyCb.setSelected(Editor.useProxy);
	useProxyCb.addActionListener(this);
	proxyPane.add(useProxyCb);
	proxyHostLb=new JLabel("Hostname:");
	proxyHostLb.setEnabled(Editor.useProxy);
	buildConstraints(constraints5,0,1,1,1,80,1);
	gridBag5.setConstraints(proxyHostLb,constraints5);
	proxyPane.add(proxyHostLb);
	proxyPortLb=new JLabel("Port:");
	proxyPortLb.setEnabled(Editor.useProxy);
	buildConstraints(constraints5,1,1,1,1,20,1);
	gridBag5.setConstraints(proxyPortLb,constraints5);
	proxyPane.add(proxyPortLb);
	proxyHostTf=new JTextField(Editor.proxyHost);
	proxyHostTf.setEnabled(Editor.useProxy);
	buildConstraints(constraints5,0,2,1,1,80,1);
	gridBag5.setConstraints(proxyHostTf,constraints5);
	proxyPane.add(proxyHostTf);
	proxyPortTf=new JTextField(Editor.proxyPort);
	proxyPortTf.setEnabled(Editor.useProxy);
	buildConstraints(constraints5,1,2,1,1,20,1);
	gridBag5.setConstraints(proxyPortTf,constraints5);
	proxyPane.add(proxyPortTf);
// 	HSepPanel hsp89=new HSepPanel(0,false,Color.black);
// 	buildConstraints(constraints5,0,3,2,1,100,20);
// 	gridBag5.setConstraints(hsp89,constraints5);
// 	proxyPane.add(hsp89);
// 	constraints5.fill=GridBagConstraints.NONE;
// 	memCacheBt=new JButton("Clear Memory Bitmap Cache");
// 	buildConstraints(constraints5,0,4,2,1,100,1);
// 	gridBag5.setConstraints(memCacheBt,constraints5);
// 	proxyPane.add(memCacheBt);
// 	memCacheBt.addActionListener(this);
	constraints5.fill=GridBagConstraints.BOTH;
	constraints5.anchor=GridBagConstraints.CENTER;
	//fill out empty space
	JPanel p1000=new JPanel();
	buildConstraints(constraints5,0,5,2,1,100,90);
	gridBag5.setConstraints(p1000,constraints5);
	proxyPane.add(p1000);
	constraints5.fill=GridBagConstraints.NONE;
	constraints5.anchor=GridBagConstraints.EAST;
	proxyHelpBt=new JButton("Help");
	buildConstraints(constraints5,1,6,1,1,20,1);
	gridBag5.setConstraints(proxyHelpBt,constraints5);
	proxyHelpBt.addActionListener(this);
	proxyPane.add(proxyHelpBt);
	tabbedPane.addTab("Proxy/Cache",proxyPane);

	//rendering panel
	JPanel renderPane=new JPanel();
	GridBagLayout gridBag4=new GridBagLayout();
	GridBagConstraints constraints4=new GridBagConstraints();
	constraints4.fill=GridBagConstraints.HORIZONTAL;
	constraints4.anchor=GridBagConstraints.WEST;
	renderPane.setLayout(gridBag4);
// 	JLabel lb3=new JLabel("Color scheme:");
// 	buildConstraints(constraints4,0,0,1,1,33,10);
// 	gridBag4.setConstraints(lb3,constraints4);
// 	renderPane.add(lb3);
// 	java.util.Vector colorSchemes=new java.util.Vector();
// 	colorSchemes.add("default");colorSchemes.add("b&w");
// 	cbb=new JComboBox(colorSchemes);
// 	cbb.setMaximumRowCount(2);
// 	cbb.setSelectedItem(ConfigManager.COLOR_SCHEME);
// 	buildConstraints(constraints4,1,0,2,1,66,0);
// 	gridBag4.setConstraints(cbb,constraints4);
// 	renderPane.add(cbb);

	JLabel bkgColLb=new JLabel("Background Color:");
	buildConstraints(constraints4,0,0,1,1,33,10);
	gridBag4.setConstraints(bkgColLb,constraints4);
	renderPane.add(bkgColLb);
	constraints4.anchor=GridBagConstraints.CENTER;
	colInd=new ColorIndicator(ConfigManager.bckgColor);
	buildConstraints(constraints4,1,0,2,1,66,0);
	gridBag4.setConstraints(colInd,constraints4);
	renderPane.add(colInd);
	colInd.addMouseListener(this);

	constraints4.fill=GridBagConstraints.HORIZONTAL;
	constraints4.anchor=GridBagConstraints.WEST;
	JLabel fontLb=new JLabel("Graph/VTM Font:");
	buildConstraints(constraints4,0,1,1,1,33,10);
	gridBag4.setConstraints(fontLb,constraints4);
	renderPane.add(fontLb);
	fontInd=new JLabel(Editor.vtmFont.getFamily()+","+FontDialog.getFontStyleName(Editor.vtmFont.getStyle())+","+Editor.vtmFont.getSize());
	buildConstraints(constraints4,1,1,1,1,33,0);
	gridBag4.setConstraints(fontInd,constraints4);
	renderPane.add(fontInd);
	constraints4.fill=GridBagConstraints.NONE;
	constraints4.anchor=GridBagConstraints.EAST;
	fontBt=new JButton("Change...");
	buildConstraints(constraints4,2,1,1,1,33,0);
	gridBag4.setConstraints(fontBt,constraints4);
	renderPane.add(fontBt);
	fontBt.addActionListener(this);
	constraints4.fill=GridBagConstraints.HORIZONTAL;
	constraints4.anchor=GridBagConstraints.WEST;
	JLabel sfontLb=new JLabel("Swing Font:");
	buildConstraints(constraints4,0,2,1,1,33,10);
	gridBag4.setConstraints(sfontLb,constraints4);
	renderPane.add(sfontLb);
	sfontInd=new JLabel(Editor.swingFont.getFamily()+","+FontDialog.getFontStyleName(Editor.swingFont.getStyle())+","+Editor.swingFont.getSize());
	buildConstraints(constraints4,1,2,1,1,33,0);
	gridBag4.setConstraints(sfontInd,constraints4);
	renderPane.add(sfontInd);
	constraints4.fill=GridBagConstraints.NONE;
	constraints4.anchor=GridBagConstraints.EAST;
	sfontBt=new JButton("Change...");
	buildConstraints(constraints4,2,2,1,1,33,0);
	gridBag4.setConstraints(sfontBt,constraints4);
	renderPane.add(sfontBt);
	sfontBt.addActionListener(this);

	constraints4.fill=GridBagConstraints.HORIZONTAL;
	constraints4.anchor=GridBagConstraints.CENTER;
	JLabel lb2=new JLabel("Graph Orientation:");
	buildConstraints(constraints4,0,3,1,1,33,10);
	gridBag4.setConstraints(lb2,constraints4);
	renderPane.add(lb2);
	ButtonGroup bg1=new ButtonGroup();
	b1a=new JRadioButton("Horizontal");
	b2a=new JRadioButton("Vertical");
	bg1.add(b1a);
	bg1.add(b2a);
	if (Editor.GRAPH_ORIENTATION.equals("LR")){b1a.setSelected(true);} else {b2a.setSelected(true);}
	buildConstraints(constraints4,1,3,1,1,33,0);
	gridBag4.setConstraints(b1a,constraints4);
	renderPane.add(b1a);
	buildConstraints(constraints4,2,3,1,1,33,0);
	gridBag4.setConstraints(b2a,constraints4);
	renderPane.add(b2a);

	constraints4.anchor=GridBagConstraints.WEST;
	antialiascb=new JCheckBox("Antialiasing",Editor.ANTIALIASING);
	antialiascb.addActionListener(this);
	buildConstraints(constraints4,0,4,3,1,100,10);
	gridBag4.setConstraints(antialiascb,constraints4);
	renderPane.add(antialiascb);
	saveWindowLayoutCb=new JCheckBox("Save/Restore Window Layout at Startup",Editor.SAVE_WINDOW_LAYOUT);
	buildConstraints(constraints4,0,5,3,1,100,10);
	gridBag4.setConstraints(saveWindowLayoutCb,constraints4);
	renderPane.add(saveWindowLayoutCb);
	incGSSCb=new JCheckBox("Automatically Apply GSS Styling Rules After Graph Modifications",GSSManager.ALLOW_INCREMENTAL_STYLING);
	incGSSCb.addActionListener(this);
	buildConstraints(constraints4,0,6,3,1,100,10);
	gridBag4.setConstraints(incGSSCb,constraints4);
	renderPane.add(incGSSCb);
// 	JPanel p51=new JPanel();
// 	buildConstraints(constraints4,0,7,3,1,100,20);
// 	gridBag4.setConstraints(p51,constraints4);
// 	renderPane.add(p51);
	tabbedPane.addTab("Rendering/GSS",renderPane);

	//main panel (tabbed panes + OK/Save buttons)
	Container cpane=this.getContentPane();
	GridBagLayout gridBag3=new GridBagLayout();
	GridBagConstraints constraints3=new GridBagConstraints();
	constraints3.fill=GridBagConstraints.BOTH;
	constraints3.anchor=GridBagConstraints.WEST;
	cpane.setLayout(gridBag3);
	buildConstraints(constraints3,0,0,3,1,100,90);
	gridBag3.setConstraints(tabbedPane,constraints3);
	cpane.add(tabbedPane);
	JPanel tmp=new JPanel();
	buildConstraints(constraints3,0,1,1,1,70,10);
	gridBag3.setConstraints(tmp,constraints3);
	cpane.add(tmp);
	constraints3.fill=GridBagConstraints.HORIZONTAL;
	constraints3.anchor=GridBagConstraints.CENTER;
	okPrefs=new JButton("Apply & Close");
	//okPrefs.setPreferredSize(new Dimension(60,25));
	buildConstraints(constraints3,1,1,1,1,15,10);
	gridBag3.setConstraints(okPrefs,constraints3);
	okPrefs.addActionListener(this);
	cpane.add(okPrefs);
	constraints3.fill=GridBagConstraints.HORIZONTAL;
	constraints3.anchor=GridBagConstraints.CENTER;
	savePrefs=new JButton("Save");
	//savePrefs.setPreferredSize(new Dimension(60,35));
	buildConstraints(constraints3,2,1,1,1,15,10);
	gridBag3.setConstraints(savePrefs,constraints3);
	savePrefs.addActionListener(this);
	cpane.add(savePrefs);

	tabbedPane.setSelectedIndex(0);
	//window
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){}
	    };
	this.addWindowListener(w0);
	this.setTitle("Preferences");
	this.pack();
	this.setLocation(0,0);
	this.setSize(400,300);
	this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
	JFileChooser fc;
	int returnVal;
	Object o=e.getSource();
	if (o==brw1){//tmp directory browse button
	    fc=new JFileChooser(Editor.m_TmpDir);
 	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //does not work well with JVM 1.3.x (works fine in 1.4)
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		Editor.m_TmpDir=fc.getSelectedFile();
		tf1.setText(Editor.m_TmpDir.toString());
	    }
	}
	else if (o==brw2){
	    fc=new JFileChooser(Editor.projectDir);
 	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //does not work well with JVM 1.3.x (works fine in 1.4)
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		Editor.projectDir=fc.getSelectedFile();
		tf2.setText(Editor.projectDir.toString());
	    }
	}
	else if (o==brw3){
	    fc=new JFileChooser(Editor.rdfDir);
 	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //does not work well with JVM 1.3.x (works fine in 1.4)
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		Editor.rdfDir=fc.getSelectedFile();
		tf3.setText(Editor.rdfDir.toString());
	    }
	}
	else if (o==brw4){
	    fc=new JFileChooser(Editor.m_GraphVizPath);
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		Editor.m_GraphVizPath=fc.getSelectedFile();
		tf4.setText(Editor.m_GraphVizPath.toString());
	    }
	}
	else if (o==brw5){
	    fc=new JFileChooser(Editor.m_GraphVizFontDir);
 	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //does not work well with JVM 1.3.x (works fine in 1.4)
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		Editor.m_GraphVizFontDir=fc.getSelectedFile();
		tf5.setText(Editor.m_GraphVizFontDir.toString());
	    }
	}
	else if (o==cb1){
	    if (cb1.isSelected()){Editor.dltOnExit=true;}
	    else {Editor.dltOnExit=false;}
	}
	else if (o==detectBrowserBt){
	    if (detectBrowserBt.isSelected()){//automatically detect browser
		Editor.autoDetectBrowser=true;
		browserPathTf.setEnabled(false);
		brw6.setEnabled(false);
		browserOptsTf.setEnabled(false);
		pathLb.setEnabled(false);
		optLb.setEnabled(false);
	    }
	}
	else if (o==specifyBrowserBt){
	    if (specifyBrowserBt.isSelected()){//specify browser
		Editor.autoDetectBrowser=false;
		browserPathTf.setEnabled(true);
		brw6.setEnabled(true);
		browserOptsTf.setEnabled(true);
		pathLb.setEnabled(true);
		optLb.setEnabled(true);
	    }
	}
	else if (o==brw6){
	    fc=new JFileChooser(Editor.browserPath);
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		Editor.browserPath=fc.getSelectedFile();
		browserPathTf.setText(Editor.browserPath.toString());
	    }
	}
	else if (o==webHelpBt){
	    Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	    TextViewer help=new TextViewer(new StringBuffer(Messages.webBrowserHelpText),"Web Browser Configuration",0,(screenSize.width-400)/2,(screenSize.height-300)/2,400,300,false);
	}
	else if (o==useProxyCb){
	    proxyHostLb.setEnabled(useProxyCb.isSelected());
	    proxyPortLb.setEnabled(useProxyCb.isSelected());
	    proxyHostTf.setEnabled(useProxyCb.isSelected());
	    proxyPortTf.setEnabled(useProxyCb.isSelected());
	}
// 	else if (o==memCacheBt){
// 	    application.clearBitmapCache();
// 	}
	else if (o==proxyHelpBt){
	    Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	    TextViewer help=new TextViewer(new StringBuffer(Messages.proxyHelpText),"Proxy Configuration",0,(screenSize.width-400)/2,(screenSize.height-300)/2,400,300,false);
	}
	else if (o==okPrefs){updateVars();this.dispose();}
	else if (o==savePrefs){updateVars();application.saveConfig();}
	else if (o==antialiascb){
	    if (antialiascb.isSelected()){javax.swing.JOptionPane.showMessageDialog(this,Messages.antialiasingWarning);}
	    application.setAntialiasing(antialiascb.isSelected());
	}
	else if (o==incGSSCb){
	    if (incGSSCb.isSelected()){javax.swing.JOptionPane.showMessageDialog(this,Messages.incGSSstylingWarning);}
	    GSSManager.ALLOW_INCREMENTAL_STYLING=incGSSCb.isSelected();
	}
	else if (o==fontBt){
	    ConfigManager.assignFontToGraph(this);
	    fontInd.setText(Editor.vtmFont.getFamily()+","+FontDialog.getFontStyleName(Editor.vtmFont.getStyle())+","+Editor.vtmFont.getSize());
	}
	else if (o==sfontBt){
	    ConfigManager.assignFontToSwing(this);
	    sfontInd.setText(Editor.swingFont.getFamily()+","+FontDialog.getFontStyleName(Editor.swingFont.getStyle())+","+Editor.swingFont.getSize());
	}
    }

    public void mouseClicked(MouseEvent e){
	Object o=e.getSource();
	if (o==colInd){
	    Color newCol=JColorChooser.showDialog(this,"Background Color",colInd.getColor());
	    if (newCol!=null){
		ConfigManager.updateBckgColor(newCol);
		colInd.setColor(ConfigManager.bckgColor);
	    }
	}
    }

    public void mousePressed(MouseEvent e){}

    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}

    public void keyPressed(KeyEvent e){//only need this because we could not implement JSpinner ()
	if (e.getKeyCode()==KeyEvent.VK_ENTER && e.getSource()==spinner){
	    if (!Utils.isPositiveInteger(spinner.getText())){javax.swing.JOptionPane.showMessageDialog(this,spinner.getText()+" is not a valid number.");spinner.setText(String.valueOf(Editor.MAX_LIT_CHAR_COUNT));}
	}
    }
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    void updateVars(){
// 	if (gr2.isSelected()){Editor.GRAPHVIZ_VERSION=1;} //means GraphViz 1.7.11 or later is used
// 	else {Editor.GRAPHVIZ_VERSION=0;} //means GraphViz 1.7.6 is used
	String base=tf1a.getText();
	Editor.DEFAULT_BASE_URI=Utils.isWhiteSpaceCharsOnly(base) ? "" : base;
	Editor.ANON_NODE=tf2a.getText()+":";//since it is the separator between prefix and ID
	Editor.ALWAYS_INCLUDE_LANG_IN_LITERALS=cb1c.isSelected();
	Editor.DEFAULT_LANGUAGE_IN_LITERALS=tf1c.getText();
	application.setAbbrevSyntax(cb1a.isSelected());
	Editor.SAVE_WINDOW_LAYOUT=saveWindowLayoutCb.isSelected();
	GSSManager.ALLOW_INCREMENTAL_STYLING=incGSSCb.isSelected();
	if (ConfigManager.SHOW_ANON_ID!=cb1b.isSelected()){application.showAnonIds(cb1b.isSelected());}
	if (Editor.DISP_AS_LABEL!=dispAsLabelCb.isSelected()){application.displayLabels(dispAsLabelCb.isSelected());}
	ConfigManager.ALLOW_PFX_IN_TXTFIELDS=allowPfxCb.isSelected();
// 	application.setMaxLiteralCharCount(((Integer)spinner.getValue()).intValue());
	try {
	    application.setMaxLiteralCharCount((new Integer(spinner.getText())).intValue());//use this instead (much more primitive) since JSpinner is only available since jdk 1.4 (and we want to be compatible with 1.3.x for now)
	}
	catch (NumberFormatException ex){javax.swing.JOptionPane.showMessageDialog(this,spinner.getText()+" is not a valid number.");}//if there is an error, signal it and keep old value (should have been signaled to user anyway)
	if (b1a.isSelected()){Editor.GRAPH_ORIENTATION="LR";} else {Editor.GRAPH_ORIENTATION="TB";}
	if (parseStrictBt.isSelected()){ConfigManager.PARSING_MODE=ConfigManager.STRICT_PARSING;}
	else if (parseLaxBt.isSelected()){ConfigManager.PARSING_MODE=ConfigManager.LAX_PARSING;}
	else {ConfigManager.PARSING_MODE=ConfigManager.DEFAULT_PARSING;}
	//ConfigManager.assignColorsToGraph((String)cbb.getSelectedItem());
	Editor.browserPath=new File(browserPathTf.getText());
	Editor.browserOptions=browserOptsTf.getText();
	ConfigManager.updateProxy(useProxyCb.isSelected(),proxyHostTf.getText(),proxyPortTf.getText());
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

class ColorIndicator extends JPanel {

    Color color;
    //JPanel p;

    ColorIndicator(Color c){
	super();
	color=c;
	setBorder(BorderFactory.createLineBorder(Color.black));
	//p=new JPanel();
	this.setBackground(color);
	//add(p);
    }

    void setColor(Color c){
	color=c;
	this.setBackground(color);
	repaint();
    }

    Color getColor(){
	return color;
    }

}
