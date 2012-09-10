/*   FILE: URLPanel.java
 *   DATE OF CREATION:   10/25/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Mar 14 09:17:47 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;

import com.xerox.VTM.engine.SwingWorker;

/*A Simple Window with bookmardked URLs to load remote RDF/XML or NTriple files (through http)*/

class URLPanel extends JFrame implements ActionListener, KeyListener {

    JButton load;

    JComboBox cbb;

    int whichReader=0;

    Editor application;

    boolean merge=false;
    boolean asStylesheet=false;

    public URLPanel(Editor app,String frameTitle,int wr,boolean mrg,boolean asGSS){
	this.application=app;
	this.whichReader=wr;
	this.merge=mrg;
	this.asStylesheet=asGSS;
	cbb=new JComboBox(ConfigManager.lastURLs);
	cbb.setEditable(true);
	cbb.setMaximumRowCount(5);
	Container cpane=this.getContentPane();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.WEST;
	cpane.setLayout(gridBag);
	buildConstraints(constraints,0,0,1,1,90,100);
	gridBag.setConstraints(cbb,constraints);
// 	cbb.addActionListener(this);  //not consistent between JDK 1.3 and 1.4, so we use
	cbb.getEditor().getEditorComponent().addKeyListener(this); //this instead
	cpane.add(cbb);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.EAST;
	load=new JButton("Load");
	load.addActionListener(this);
	load.addKeyListener(this);
	buildConstraints(constraints,1,0,1,1,10,0);
	gridBag.setConstraints(load,constraints);
	cpane.add(load);
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){dispose();}
	    };
	this.addWindowListener(w0);
	this.setTitle(frameTitle);
	this.pack();
	Dimension screen=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation(screen.width/2-300,screen.height/2-25);
	this.setSize(600,50);
	this.setVisible(true);
	cbb.getEditor().getEditorComponent().requestFocus();
    }

    public void actionPerformed(ActionEvent e){
	String s=(String)cbb.getSelectedItem();
	application.cfgMngr.addLastURL(s);
	if (e.getSource()==load){
	    try {
		java.net.URL aURL=new java.net.URL(s);
		if (this.merge){
		    application.mergeRDF(aURL,whichReader);
		}
		else {
		    application.loadRDF(aURL,whichReader,asStylesheet);
		}
	    }
	    catch (java.net.MalformedURLException ex){JOptionPane.showMessageDialog(this,ex.toString());}
	    this.dispose();
	}
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    String s;
	    if (e.getSource()==cbb.getEditor().getEditorComponent()){s=(String)cbb.getEditor().getItem();}
	    else {s=(String)cbb.getSelectedItem();}
	    application.cfgMngr.addLastURL(s);
	    try {
		java.net.URL aURL=new java.net.URL(s);
		if (this.merge){
		    application.mergeRDF(aURL,whichReader);
		}
		else {
		    application.loadRDF(aURL,whichReader,asStylesheet);
		}
	    }
	    catch (java.net.MalformedURLException ex){JOptionPane.showMessageDialog(this,ex.toString());}
	    this.dispose();
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

}
