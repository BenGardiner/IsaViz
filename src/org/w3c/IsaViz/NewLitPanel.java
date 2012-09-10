/*   FILE: NewLitPanel.java
 *   DATE OF CREATION:   11/26/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Oct 15 09:19:11 2004 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: NewLitPanel.java,v 1.3 2004/10/15 07:31:24 epietrig Exp $
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

/*This dialog is made modal so that users cannot create several resources/literals simultaneously*/

class NewLitPanel extends JDialog implements KeyListener,ActionListener {

    Editor application;

    ILiteral node;

    JCheckBox tpBt;
    JLabel l2;
    JTextArea ta;
    JTextField tf,tf2;
    JButton ok,cancel,dturiBt;
    
    NewLitPanel(Editor app,ILiteral n){
	super((JFrame)Editor.vsm.getActiveView().getFrame(),"New Literal...",true); //getFrame() sends a Container
	application=app;                     //because it is the first common swing ancestor of EView and IView
	node=n;                              //if in the future we switch to internal views, will have to cast
	Container cpane=this.getContentPane();//as JInternalFrame
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	cpane.setLayout(gridBag);
	//1st row (lang + typed literal chkbox)
	GridBagLayout gridBag0=new GridBagLayout();
	GridBagConstraints constraints0=new GridBagConstraints();
	constraints0.fill=GridBagConstraints.NONE;
	constraints0.anchor=GridBagConstraints.EAST;
	JPanel p0=new JPanel();
	p0.setLayout(gridBag0);
	l2=new JLabel("lang: ");
	buildConstraints(constraints0,0,0,1,1,20,10);
	gridBag0.setConstraints(l2,constraints0);
	p0.add(l2);
	
	tf=new JTextField();
	tf.addKeyListener(this);
	constraints0.fill=GridBagConstraints.HORIZONTAL;
	constraints0.anchor=GridBagConstraints.WEST;
	buildConstraints(constraints0,1,0,1,1,20,0);
	gridBag0.setConstraints(tf,constraints0);
	p0.add(tf);
	
	tpBt=new JCheckBox("Typed Literal");
	constraints0.fill=GridBagConstraints.NONE;
	constraints0.anchor=GridBagConstraints.EAST;
	buildConstraints(constraints0,2,0,1,1,60,0);
	gridBag0.setConstraints(tpBt,constraints0);
	p0.add(tpBt);
	tpBt.setSelected(false);
	tpBt.addActionListener(this);
	
	buildConstraints(constraints,0,0,1,1,100,5);
	gridBag.setConstraints(p0,constraints);
	cpane.add(p0);

	//2nd row (datatype URI)
	GridBagLayout gridBag1=new GridBagLayout();
	GridBagConstraints constraints1=new GridBagConstraints();
	constraints1.fill=GridBagConstraints.HORIZONTAL;
	constraints1.anchor=GridBagConstraints.WEST;
	JPanel p1=new JPanel();
	p1.setLayout(gridBag1);
	dturiBt=new JButton("Datatype URI: ");
	dturiBt.setBorder(BorderFactory.createEtchedBorder());
	buildConstraints(constraints1,0,0,1,1,10,0);
	gridBag1.setConstraints(dturiBt,constraints1);
	p1.add(dturiBt);
	dturiBt.addActionListener(this);
	tf2=new JTextField();
	buildConstraints(constraints1,1,0,1,1,90,0);
	gridBag1.setConstraints(tf2,constraints1);
	p1.add(tf2);
	tf2.addKeyListener(this);
	enableType(false);
	buildConstraints(constraints,0,1,1,1,100,5);
	gridBag.setConstraints(p1,constraints);
	cpane.add(p1);

	//3rd row (literal value)
	ta=new JTextArea("");
	ta.setFont(Editor.swingFont);
	JScrollPane sp=new JScrollPane(ta);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,2,1,1,100,85);
	gridBag.setConstraints(sp,constraints);
	cpane.add(sp);

	//4th row (ok and cancel buttons)
	JPanel p2=new JPanel();
	p2.setLayout(new GridLayout(1,2));
	ok=new JButton("OK");
	ok.addActionListener(this);
	ok.addKeyListener(this);
	p2.add(ok);
	cancel=new JButton("Cancel");
	cancel.addActionListener(this);
	cancel.addKeyListener(this);
	p2.add(cancel);
	constraints.fill=GridBagConstraints.HORIZONTAL;
	buildConstraints(constraints,0,3,1,1,100,5);
	gridBag.setConstraints(p2,constraints);
	cpane.add(p2);
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){}
		public void windowActivated(WindowEvent e){ta.requestFocus();}
	    };
	this.addWindowListener(w0);
	this.pack();
	Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation((screenSize.width-400)/2,(screenSize.height-240)/2);
	this.setSize(400,240);
	this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
	if (e.getSource()==ok){
	    String dturi=tf2.getText();
	    if (ConfigManager.ALLOW_PFX_IN_TXTFIELDS && tpBt.isSelected() && dturi.length()>0 && !Utils.isWhiteSpaceCharsOnly(dturi)){
		dturi=application.tryToSolveBinding(dturi);
	    }
	    application.storeLiteral(node,ta.getText(),tpBt.isSelected(),tf.getText(),dturi);
	    this.dispose();
	}
	else if (e.getSource()==cancel){application.cancelNewNode(node);this.dispose();}
	else if (e.getSource()==tpBt){
	    if (tpBt.isSelected()){enableType(true);}
	    else {enableType(false);}
	}
	else if (e.getSource()==dturiBt){
	    com.hp.hpl.jena.datatypes.RDFDatatype dt=Editor.displayAvailableDataTypes(this,tf2.getText());
	    if (dt!=null){
		tf2.setText(dt.getURI());
	    }
	}
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    if (e.getSource()==tf || e.getSource()==ok){
		application.storeLiteral(node,ta.getText(),tpBt.isSelected(),tf.getText(),tf2.getText());
		this.dispose();
	    }
	    else if (e.getSource()==cancel){application.cancelNewNode(node);this.dispose();}
	    else if (e.getSource()==tf2){
		String dturi=tf2.getText();
		if (ConfigManager.ALLOW_PFX_IN_TXTFIELDS && dturi.length()>0 && !Utils.isWhiteSpaceCharsOnly(dturi)){
		    tf2.setText(application.tryToSolveBinding(dturi));
		}
	    }
	}
    }

    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    private void enableType(boolean b){
	tf2.setEnabled(b);
	dturiBt.setEnabled(b);
	//lang attribute is not available for typed literals
	l2.setEnabled(!b);
	tf.setEnabled(!b);
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
