/*   FILE: NewResPanel.java
 *   DATE OF CREATION:   11/25/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Feb 20 12:06:29 2003 by Emmanuel Pietriga
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

/*This dialog is made modal so that users cannot crate several resources/literals "in parallel"*/

class NewResPanel extends JDialog implements KeyListener,ActionListener {

    Editor application;

    IResource node;

    JRadioButton uriBt,idBt;
    JTextField tf;
    JButton ok,cancel;
    
    NewResPanel(Editor app,IResource n){
	super((JFrame)Editor.vsm.getActiveView().getFrame(),"New Resource...",true); //getFrame() sends a Container
	application=app;                     //because it is the first common swing ancestor of EView and IView
	node=n;                              //if in the future we switch to internal views, will have to cast
	Container cpane=this.getContentPane();//as JInternalFrame
	cpane.setLayout(new GridLayout(3,1));
	JPanel p0=new JPanel();
	p0.setLayout(new GridLayout(1,3));
	JLabel l1=new JLabel("About:");
	p0.add(l1);
	ButtonGroup bg=new ButtonGroup();
	uriBt=new JRadioButton("URI");
	idBt=new JRadioButton("ID");
	p0.add(uriBt);bg.add(uriBt);
	p0.add(idBt);bg.add(idBt);
	cpane.add(p0);
	uriBt.setSelected(true);
	tf=new JTextField("");
	tf.setFont(Editor.swingFont);
	tf.addKeyListener(this);
	cpane.add(tf);
	JPanel p1=new JPanel();
	p1.setLayout(new GridLayout(1,2));
	ok=new JButton("OK");
	ok.addActionListener(this);
	ok.addKeyListener(this);
	p1.add(ok);
	cancel=new JButton("Cancel");
	cancel.addActionListener(this);
	cancel.addKeyListener(this);
	p1.add(cancel);
	cpane.add(p1);
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){}
		public void windowActivated(WindowEvent e){tf.requestFocus();}
	    };
	this.addWindowListener(w0);
	this.pack();
	Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation((screenSize.width-250)/2,(screenSize.height-100)/2);
	this.setSize(250,100);
	this.setVisible(true);
	this.addNotify();
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource()==ok){
	    ok();
	}
	else if (e.getSource()==cancel){application.cancelNewNode(node);this.dispose();}
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    if (e.getSource()==tf || e.getSource()==ok){
		ok();
	    }
	    else if (e.getSource()==cancel){application.cancelNewNode(node);this.dispose();}
	}
    }

    void ok(){
	String uri;
	if (uriBt.isSelected()){
	    uri=tf.getText();
	    if (ConfigManager.ALLOW_PFX_IN_TXTFIELDS){uri=application.tryToSolveBinding(uri);}
	    if (!application.resourceAlreadyExists(uri)){
		if (!uri.startsWith(Editor.ANON_NODE.substring(0,Editor.ANON_NODE.length()-1))){
		    application.storeResource(node,uri,true);
		    this.dispose();
		}
		else {JOptionPane.showMessageDialog(this,Editor.ANON_NODE+" is reserved for anonymous nodes.");}
	    }
	    else {JOptionPane.showMessageDialog(this,"Resource "+uri+" already exists.");}
	}
	else {//ID
	    String tmp=tf.getText();
	    uri=tmp.startsWith(Editor.BASE_URI) ? tmp.substring(Editor.BASE_URI.length(),tmp.length()) : tmp;
	    //2 tests below because at this point with have not yet normalized IDs with '#' (still value entered by user in text field)
	    if (!(application.resourceAlreadyExists(Editor.BASE_URI+"#"+uri) || application.resourceAlreadyExists(Editor.BASE_URI+uri))){
		if (!uri.startsWith(Editor.ANON_NODE.substring(0,Editor.ANON_NODE.length()-1))){
		    application.storeResource(node,uri,false);
		    this.dispose();
		}
		else {JOptionPane.showMessageDialog(this,Editor.ANON_NODE+" is reserved for anonymous nodes.");}
	    }
	    else {JOptionPane.showMessageDialog(this,"Resource "+uri+" already exists.");}
	}
    }

    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

}
