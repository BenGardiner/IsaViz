/*   FILE: PropertySummary.java
 *   DATE OF CREATION:   12/17/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Jul 10 15:06:11 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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

/*Shows all properties defining a resource (all statements whose subject is this resource), with their value in a table like way, trying to appropriately display things like images*/

class PropertySummary extends JFrame implements ActionListener {

    IResource subject;
    Editor application;

    PropertySummary(IResource r,Editor e){
	this.application=e;
	this.subject=r;
	initProperties();
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){}
	    };
	this.addWindowListener(w0);
	this.pack();
	this.setTitle("Properties of Resource "+r.getGraphLabel());
	Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	this.setSize(Math.round(this.getWidth()*1.2f),Math.round(this.getHeight()*1.2f));
	this.setLocation((screenSize.width-this.getWidth())/2,(screenSize.height-this.getHeight())/2);
	this.setVisible(true);
    }

    private void initProperties(){
	Container cpane=this.getContentPane();
	Vector v;
	if ((v=subject.getOutgoingPredicates())!=null){
	    JPanel p0=new JPanel();
	    JScrollPane sp=new JScrollPane(p0);
	    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    sp.getVerticalScrollBar().setUnitIncrement(5);
	    GridBagLayout gridBag=new GridBagLayout();
	    GridBagConstraints constraints=new GridBagConstraints();
	    constraints.fill=GridBagConstraints.HORIZONTAL;
	    constraints.anchor=GridBagConstraints.WEST;
	    p0.setLayout(gridBag);
	    int gridIndexH=0;
	    int gridIndexV=0;
	    int spanH=1;
	    int spanV=1;
	    int ratioH=50;
	    int ratioV=100/(v.size()+1);  //+1 because insert a first row for the subject's URI
	    String subjectLabel=subject.getIdentity();
	    if (subject.getLabel()!=null){subjectLabel+=" ("+subject.getLabel()+")";}
	    JLabel resourceLb=new JLabel(subjectLabel);
	    resourceLb.setFont(Editor.swingFont);
	    resourceLb.setForeground(ConfigManager.darkerPastelBlue);
	    buildConstraints(constraints,gridIndexH,gridIndexV,100,spanV,ratioH,ratioV);
	    gridBag.setConstraints(resourceLb,constraints);
	    p0.add(resourceLb);
	    gridIndexV++;
	    IProperty p;
	    JLabel propertyLabel;
	    Component objectComp;
	    String prefix;
	    for (int i=0;i<v.size();i++){
		p=(IProperty)v.elementAt(i);
		prefix=application.getNSBinding(p.getNamespace());
		propertyLabel=new JLabel(prefix!=null ? prefix+":"+p.getLocalname() : p.getIdent());
		propertyLabel.setFont(Editor.swingFont);
		buildConstraints(constraints,gridIndexH,gridIndexV,spanH,spanV,ratioH,ratioV);
		gridBag.setConstraints(propertyLabel,constraints);
		p0.add(propertyLabel);
		gridIndexH++;
		objectComp=this.getSwingRepresentation(p.object);
		objectComp.setFont(Editor.swingFont);
		buildConstraints(constraints,gridIndexH,gridIndexV,spanH,spanV,ratioH,0);
		gridBag.setConstraints(objectComp,constraints);
		p0.add(objectComp);
		gridIndexH=0;
		gridIndexV++;
	    }
	    cpane.add(sp);
	}
	else {
	    cpane.add(new JLabel("No property is associated to this resource."));
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
			if ((whichBt & InputEvent.BUTTON1_MASK)==InputEvent.BUTTON1_MASK){new PropertySummary(r,application);}
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
	    final JLabel res=new JLabel("(L) "+l.getValue());
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

    public void actionPerformed(ActionEvent e){
	
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
