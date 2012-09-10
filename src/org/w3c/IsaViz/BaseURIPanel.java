/*   FILE: BaseURIPanel.java
 *   DATE OF CREATION:   Wed Jul 09 09:30:21 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Mon Jul 28 17:26:39 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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

/*A Simple Window for entering the base URI*/

class BaseURIPanel extends JFrame implements ActionListener, KeyListener {

    JButton ok,cancel;

    JTextField uriTf;

    public BaseURIPanel(){
	Container cpane=this.getContentPane();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.WEST;
	cpane.setLayout(gridBag);
	uriTf=new JTextField(Editor.BASE_URI);
	buildConstraints(constraints,0,0,1,1,90,100);
	gridBag.setConstraints(uriTf,constraints);
	cpane.add(uriTf);
	uriTf.addKeyListener(this);
	constraints.anchor=GridBagConstraints.EAST;
	ok=new JButton("OK");
	ok.addActionListener(this);
	ok.addKeyListener(this);
	buildConstraints(constraints,1,0,1,1,5,0);
	gridBag.setConstraints(ok,constraints);
	cpane.add(ok);
	cancel=new JButton("Cancel");
	cancel.addActionListener(this);
	cancel.addKeyListener(this);
	buildConstraints(constraints,2,0,1,1,5,0);
	gridBag.setConstraints(cancel,constraints);
	cpane.add(cancel);
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){dispose();}
	    };
	this.addWindowListener(w0);
	this.setTitle("Set Base URI");
	this.pack();
	Dimension screen=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation(screen.width/2-300,screen.height/2-25);
	this.setSize(600,50);
	this.setVisible(true);
	uriTf.requestFocus();
    }

    public void actionPerformed(ActionEvent e){
	if (e.getSource()==ok){
	    Editor.BASE_URI=uriTf.getText();
	}
	//if cancel do nothing
	this.dispose();
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    if (e.getSource()==ok || e.getSource()==uriTf){
		Editor.BASE_URI=uriTf.getText();
	    }
	    //if cancel do nothing
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
