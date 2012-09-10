/*   FILE: SelectionPanel.java
 *   DATE OF CREATION:   12/07/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:56:43 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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

class SelectionPanel extends JFrame implements ActionListener,KeyListener {

    Editor application;

    JButton selectBt,unselectBt,closeBt;
    JTextField resURI,prpNS,prpLN,litVal;

    SelectionPanel(Editor e){
	application=e;
	Container cpane=this.getContentPane();

	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	cpane.setLayout(gridBag);

	JLabel l2=new JLabel("Resources");
	buildConstraints(constraints,0,0,1,1,10,25);
	gridBag.setConstraints(l2,constraints);
	cpane.add(l2);
	resURI=new JTextField();
	buildConstraints(constraints,1,0,2,1,80,0);
	gridBag.setConstraints(resURI,constraints);

	selectBt=new JButton("Select");
	buildConstraints(constraints,3,0,1,1,10,0);
	gridBag.setConstraints(selectBt,constraints);
	selectBt.addActionListener(this);
	selectBt.addKeyListener(this);

	JLabel l4=new JLabel("Literals");
	buildConstraints(constraints,0,1,1,1,10,25);
	gridBag.setConstraints(l4,constraints);
	cpane.add(l4);
	litVal=new JTextField();
	buildConstraints(constraints,1,1,2,1,80,0);
	gridBag.setConstraints(litVal,constraints);

	unselectBt=new JButton("Unselect All");
	unselectBt.addActionListener(this);
	unselectBt.addKeyListener(this);
	buildConstraints(constraints,3,1,1,1,10,0);
	gridBag.setConstraints(unselectBt,constraints);


	constraints.fill=GridBagConstraints.NONE;
	constraints.anchor=GridBagConstraints.WEST;
	JLabel l0=new JLabel("Namespace (URI, or prefix ending with ':')");
	buildConstraints(constraints,1,2,1,1,40,25);
	gridBag.setConstraints(l0,constraints);
	cpane.add(l0);
	JLabel l1=new JLabel("Name");
	buildConstraints(constraints,2,2,1,1,40,0);
	gridBag.setConstraints(l1,constraints);
	cpane.add(l1);
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	closeBt=new JButton("Close");
	buildConstraints(constraints,3,2,1,1,10,0);
	gridBag.setConstraints(closeBt,constraints);
	closeBt.addActionListener(this);
	closeBt.addKeyListener(this);

	JLabel l3=new JLabel("Properties");
	buildConstraints(constraints,0,3,1,1,10,25);
	gridBag.setConstraints(l3,constraints);
	cpane.add(l3);
	prpNS=new JTextField();
	buildConstraints(constraints,1,3,1,1,40,0);
	gridBag.setConstraints(prpNS,constraints);
	prpLN=new JTextField();
	buildConstraints(constraints,2,3,1,1,40,0);
	gridBag.setConstraints(prpLN,constraints);

	cpane.add(resURI);
	cpane.add(litVal);
	cpane.add(prpNS);
	cpane.add(prpLN);
	cpane.add(selectBt);
	cpane.add(unselectBt);
	cpane.add(closeBt);

	//window
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){}
	    };
	this.addWindowListener(w0);
	this.setTitle("Advanced Selection");
	Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation((screenSize.width-500)/2,(screenSize.height-100)/2);
	this.pack();
	this.setSize(600,this.getHeight());
	this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
	Object src=e.getSource();
	if (src==selectBt){
	    application.selectResourcesMatching(resURI.getText());
	    application.selectPropertiesMatching(prpNS.getText(),prpLN.getText());
	    application.selectLiteralsMatching(litVal.getText());
	}
	else if (src==unselectBt){
	    application.unselectAll();
	}
	else if (src==closeBt){
	    this.dispose();
	}
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    Object src=e.getSource();
	    if (src==selectBt){
		application.selectResourcesMatching(resURI.getText());
		application.selectPropertiesMatching(prpNS.getText(),prpLN.getText());
		application.selectLiteralsMatching(litVal.getText());	
	    }
	    else if (src==unselectBt){
		application.unselectAll();
	    }
	    else if (src==closeBt){
		this.dispose();
	    }
	}
	else if (e.getKeyCode()==KeyEvent.VK_DELETE){
	    application.deleteSelectedEntities();
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
