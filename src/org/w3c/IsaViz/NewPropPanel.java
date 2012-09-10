/*   FILE: NewPropPanel.java
 *   DATE OF CREATION:   Wed Jul 02 14:22:50 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 09 10:42:55 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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
import java.awt.*;
import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;

/*This dialog is NOT made modal as the user must hbe able to point and click to designate the subject and object in the graph*/

class NewPropPanel extends JFrame implements KeyListener,ActionListener,FocusListener {

    Editor application;

    JButton ok,cancel;

    IResource subject;
    INode object;

    JComboBox pnscbb,plncbb;
    JTextField subjTf,objTf;

    Object lastFocusedTf=null;
    
    NewPropPanel(Editor app){
	super("New Statement...");
	application=app;
	Container cpane=this.getContentPane();
	GridBagLayout gridBag0=new GridBagLayout();
	GridBagConstraints constraints0=new GridBagConstraints();
	cpane.setLayout(gridBag0);
	constraints0.fill=GridBagConstraints.BOTH;
	constraints0.anchor=GridBagConstraints.CENTER;
	JPanel spoPanel=new JPanel();
	buildConstraints(constraints0,0,0,1,1,100,85);
	gridBag0.setConstraints(spoPanel,constraints0);
	cpane.add(spoPanel);
	GridBagLayout gridBag1=new GridBagLayout();
	GridBagConstraints constraints1=new GridBagConstraints();
	spoPanel.setLayout(gridBag1);
	constraints1.fill=GridBagConstraints.HORIZONTAL;
	constraints1.anchor=GridBagConstraints.WEST;
	//subject
	JLabel subjLb=new JLabel("Subject");
	buildConstraints(constraints1,0,0,1,1,20,33);
	gridBag1.setConstraints(subjLb,constraints1);
	spoPanel.add(subjLb);
	subjTf=new JTextField("Select a resource in the Graph window");
	buildConstraints(constraints1,1,0,2,1,80,0);
	gridBag1.setConstraints(subjTf,constraints1);
	spoPanel.add(subjTf);
	subjTf.addFocusListener(this);	
	subjTf.setEditable(false);
	//predicate
	JLabel predLb=new JLabel("Predicate");
	buildConstraints(constraints1,0,1,1,1,20,33);
	gridBag1.setConstraints(predLb,constraints1);
	spoPanel.add(predLb);
	Vector v=application.getAllPropertyNS();
	pnscbb=new JComboBox(v);
	pnscbb.setSelectedItem(v.firstElement());
	plncbb=new JComboBox(application.getProperties4NS((String)v.firstElement()));
	pnscbb.setMaximumRowCount(10);
	plncbb.setMaximumRowCount(10);
	buildConstraints(constraints1,1,1,1,1,40,0);
	gridBag1.setConstraints(pnscbb,constraints1);
	spoPanel.add(pnscbb);
	buildConstraints(constraints1,2,1,1,1,40,0);
	gridBag1.setConstraints(plncbb,constraints1);
	spoPanel.add(plncbb);
	if ((application.selectedPropertyConstructorNS!=null) && (application.selectedPropertyConstructorLN!=null)){
	    String prefix=application.getNSBinding(application.selectedPropertyConstructorNS);
	    pnscbb.setSelectedItem((prefix!=null) ? prefix : application.selectedPropertyConstructorNS);
	    plncbb.setSelectedItem(application.selectedPropertyConstructorLN);
	}
	ItemListener i1=new ItemListener(){
		public void itemStateChanged(ItemEvent e){
		    //update name list to display only props in this namespace
		    if (e.getStateChange()==ItemEvent.SELECTED){
			plncbb.removeAllItems();
			Vector v=application.getProperties4NS((String)e.getItem());
			for (int i=0;i<v.size();i++){
			    plncbb.addItem(v.elementAt(i));
			}
		    }
		}
	    };
	pnscbb.addItemListener(i1);
	//object
	JLabel objLb=new JLabel("Object");
	buildConstraints(constraints1,0,2,1,1,20,33);
	gridBag1.setConstraints(objLb,constraints1);
	spoPanel.add(objLb);
	objTf=new JTextField("Select a resource or a literal in the Graph window");
	buildConstraints(constraints1,1,2,2,1,80,0);
	gridBag1.setConstraints(objTf,constraints1);
	spoPanel.add(objTf);
	objTf.addFocusListener(this);
	objTf.setEditable(false);
	//ok, cancel buttons
	JPanel okclPanel=new JPanel();
	buildConstraints(constraints0,0,1,1,1,0,15);
	gridBag0.setConstraints(okclPanel,constraints0);
	cpane.add(okclPanel);
	okclPanel.setLayout(new GridLayout(1,2));
	ok=new JButton("OK");
	ok.addActionListener(this);
	ok.addKeyListener(this);
	okclPanel.add(ok);
	cancel=new JButton("Cancel");
	cancel.addActionListener(this);
	cancel.addKeyListener(this);
	okclPanel.add(cancel);
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){cancel();}
	    };
	this.addWindowListener(w0);
	this.pack();
	Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation((screenSize.width-500)/2,(screenSize.height-120)/2);
	this.setSize(500,120);
	this.setVisible(true);
    }

    void setSubjectOrObject(Glyph node){
	if (lastFocusedTf==subjTf){
	    setSubject(node);
	}
	else if (lastFocusedTf==objTf){
	    setObject(node);
	}
    }

    void setSubject(Glyph subjectNode){
	try {
	    this.subject=(IResource)subjectNode.getOwner();
	    subjTf.setText(subject.getIdentity());
	}
	catch(NullPointerException ex){}
	catch (ClassCastException cce){JOptionPane.showMessageDialog(this,"A literal cannot be the subject of a statement.");}
    }

    void setObject(Glyph objectNode){
	try {
	    this.object=(INode)objectNode.getOwner();
	    if (object instanceof IResource){
		this.object=(INode)objectNode.getOwner();
		objTf.setText(((IResource)object).getIdentity());
	    }
	    else if (object instanceof ILiteral){
		if (((ILiteral)object).getIncomingPredicate()!=null){
		    this.object=null;
		    JOptionPane.showMessageDialog(this,"This literal is already the object of a statement.");
		}
		else {
		    objTf.setText(((ILiteral)object).getText());
		}
	    }
	}
	catch(NullPointerException ex){}
    }

    void setFocusToSubject(){subjTf.requestFocus();}
    
    void setFocusToObject(){objTf.requestFocus();}

    void ok(){
	if (subject!=null && object!=null){
	    Vector points=new Vector();
	    points.add(subject.getGlyph().getLocation());
	    points.add(object.getGlyph().getLocation());
	    //pnscbb can contain prefixes or full namespace URIs if they are not bounded to a prefix, send the URI, not the prefix
	    String prefixOrURI=(String)pnscbb.getSelectedItem();
	    String nsURI=application.getNSURIfromPrefix(prefixOrURI);
	    application.createNewProperty(subject,object,points,(nsURI!=null) ? nsURI : prefixOrURI,(String)plncbb.getSelectedItem());
	    application.eeh.propertyDialog=null;
	    this.dispose();
	}
	else {
	    javax.swing.JOptionPane.showMessageDialog(this,"You must specify the subject, predicate and object of the new statement");
	}
    }

    void cancel(){
	application.eeh.propertyDialog=null;
	this.dispose();
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource()==ok){
	    ok();
	}
	else if (e.getSource()==cancel){cancel();}
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    if (e.getSource()==ok){
		ok();
	    }
	    else if (e.getSource()==cancel){cancel();}
	}
    }

    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    public void focusGained(FocusEvent e){
	if (e.getSource()==subjTf){lastFocusedTf=subjTf;subjTf.setBackground(ConfigManager.pastelBlue);}
	else if (e.getSource()==objTf){lastFocusedTf=objTf;objTf.setBackground(ConfigManager.pastelBlue);}
    }

    public void focusLost(FocusEvent e){
	if (e.getSource()==subjTf){subjTf.setBackground(this.getBackground());}
	else if (e.getSource()==objTf){objTf.setBackground(this.getBackground());}
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
