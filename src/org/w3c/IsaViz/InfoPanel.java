/*   FILE: InfoPanel.java
 *   DATE OF CREATION:   12/17/2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:48:21 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 




package org.w3c.IsaViz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;

//implements a JFrame with system and ISV project information

class InfoPanel extends JFrame implements ActionListener {

    Editor application;
    
    int nbProcs=1;
    long totalMemKb=0;
    long maxMemKb=0;
    long freeMemKb=0;
    long usedMemKb=0;
    
    JButton okBt,gcBt,refreshBt;
    JLabel l2,l4,l6,l8,l24;
    JLabel l12,l14,l16,l18;

    InfoPanel(Editor app,String projectFile,String rdfImport,int nbResources,int nbLiterals,int nbProperties){
	super();
	application=app;
	retrieveSystemInfo();
	Container cpane=this.getContentPane();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.WEST;
	cpane.setLayout(gridBag);


	JLabel l1=new JLabel("Project File");
	buildConstraints(constraints,0,0,1,1,50,10);
	gridBag.setConstraints(l1,constraints);
	cpane.add(l1);
	l2=new JLabel(projectFile);
	l2.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,0,1,1,50,0);
	gridBag.setConstraints(l2,constraints);
	cpane.add(l2);

	JLabel l23=new JLabel("Last RDF import");
	buildConstraints(constraints,0,1,1,1,50,10);
	gridBag.setConstraints(l23,constraints);
	cpane.add(l23);
	l24=new JLabel(rdfImport);
	l24.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,1,1,1,50,0);
	gridBag.setConstraints(l24,constraints);
	cpane.add(l24);

	HSepPanel p0=new HSepPanel(1,true,Color.black);
	buildConstraints(constraints,0,2,2,1,100,10);
	gridBag.setConstraints(p0,constraints);
	cpane.add(p0);

	JLabel l3=new JLabel("Number of resources");
	buildConstraints(constraints,0,3,1,1,50,10);
	gridBag.setConstraints(l3,constraints);
	cpane.add(l3);
	l4=new JLabel((new Integer(nbResources)).toString());
	l4.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,3,1,1,50,0);
	gridBag.setConstraints(l4,constraints);
	cpane.add(l4);

	JLabel l5=new JLabel("Number of literals");
	buildConstraints(constraints,0,4,1,1,50,10);
	gridBag.setConstraints(l5,constraints);
	cpane.add(l5);
	l6=new JLabel((new Integer(nbLiterals)).toString());
	l6.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,4,1,1,50,0);
	gridBag.setConstraints(l6,constraints);
	cpane.add(l6);

	JLabel l7=new JLabel("Number of statements");
	buildConstraints(constraints,0,5,1,1,50,10);
	gridBag.setConstraints(l7,constraints);
	cpane.add(l7);
	l8=new JLabel((new Integer(nbProperties)).toString());
	l8.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,5,1,1,50,0);
	gridBag.setConstraints(l8,constraints);
	cpane.add(l8);

	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	HSepPanel p1=new HSepPanel(1,true,Color.black);
	buildConstraints(constraints,0,6,2,1,100,10);
	gridBag.setConstraints(p1,constraints);
	cpane.add(p1);

	constraints.anchor=GridBagConstraints.WEST;
	JLabel l9=new JLabel("Number of processors available");
	buildConstraints(constraints,0,7,1,1,50,10);
	gridBag.setConstraints(l9,constraints);
	cpane.add(l9);
	JLabel l10=new JLabel((new Integer(nbProcs)).toString());
	l10.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,7,1,1,50,0);
	gridBag.setConstraints(l10,constraints);
	cpane.add(l10);

	HSepPanel p2=new HSepPanel(1,true,Color.black);
	buildConstraints(constraints,0,8,2,1,100,10);
	gridBag.setConstraints(p2,constraints);
	cpane.add(p2);

	JLabel l11=new JLabel("Free Memory");
	buildConstraints(constraints,0,9,1,1,50,10);
	gridBag.setConstraints(l11,constraints);
	cpane.add(l11);
	l12=new JLabel(freeMemKb+"Kb");
	l12.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,9,1,1,50,0);
	gridBag.setConstraints(l12,constraints);
	cpane.add(l12);

	JLabel l13=new JLabel("Used Memory");
	buildConstraints(constraints,0,10,1,1,50,10);
	gridBag.setConstraints(l13,constraints);
	cpane.add(l13);
	l14=new JLabel(usedMemKb+"Kb");
	l14.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,10,1,1,50,0);
	gridBag.setConstraints(l14,constraints);
	cpane.add(l14);

	JLabel l15=new JLabel("Available Memory");
	buildConstraints(constraints,0,11,1,1,50,10);
	gridBag.setConstraints(l15,constraints);
	cpane.add(l15);
	l16=new JLabel(totalMemKb+"Kb");
	l16.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,11,1,1,50,0);
	gridBag.setConstraints(l16,constraints);
	cpane.add(l16);

	JLabel l17=new JLabel("System Memory");
	buildConstraints(constraints,0,12,1,1,50,10);
	gridBag.setConstraints(l17,constraints);
	cpane.add(l17);
	l18=new JLabel(maxMemKb+"Kb");
	l18.setForeground(ConfigManager.darkerPastelBlue);
	buildConstraints(constraints,1,12,1,1,50,0);
	gridBag.setConstraints(l18,constraints);
	cpane.add(l18);

	constraints.fill=GridBagConstraints.NONE;
	constraints.anchor=GridBagConstraints.CENTER;
	gcBt=new JButton("Manual Garbage Collecting");
	buildConstraints(constraints,0,13,1,1,50,10);
	gridBag.setConstraints(gcBt,constraints);
	cpane.add(gcBt);
	gcBt.addActionListener(this);

	constraints.fill=GridBagConstraints.HORIZONTAL;
	HSepPanel p3=new HSepPanel(1,true,Color.black);
	buildConstraints(constraints,0,14,2,1,100,10);
	gridBag.setConstraints(p3,constraints);
	cpane.add(p3);

// 	JLabel jenaLb=new JLabel("Jena Version");
// 	buildConstraints(constraints,0,15,1,1,50,10);
// 	gridBag.setConstraints(jenaLb,constraints);
// 	cpane.add(jenaLb);

// 	JLabel jenaVLb=new JLabel(com.hp.hpl.mesa.rdf.jena.model.Jena.RELEASE+"("+com.hp.hpl.mesa.rdf.jena.model.Jena.DATE+")");
// 	buildConstraints(constraints,1,15,1,1,50,0);
// 	gridBag.setConstraints(jenaVLb,constraints);
// 	cpane.add(jenaVLb);


	okBt=new JButton("OK");
	buildConstraints(constraints,0,15,1,1,50,10);
	gridBag.setConstraints(okBt,constraints);
	cpane.add(okBt);
	okBt.addActionListener(this);
	refreshBt=new JButton("Refresh");
	buildConstraints(constraints,1,15,1,1,50,0);
	gridBag.setConstraints(refreshBt,constraints);
	cpane.add(refreshBt);
	refreshBt.addActionListener(this);
	

// 	Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
// 	this.setLocation((screenSize.width-300)/2,(screenSize.height-100)/2);
	this.setTitle("IsaViz Project Information");
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){destroy();}
		//public void windowActivated(WindowEvent e){application.alwaysUpdateViews(true);}
	    };
	this.addWindowListener(w0);
	this.setSize(500,270);
	//this.setResizable(false);
	this.setLocation(10,10);
	this.setVisible(true);
    }

    void destroy(){
	this.setVisible(false);
	this.dispose();
    }

    void retrieveSystemInfo(){
	nbProcs=Runtime.getRuntime().availableProcessors();
	totalMemKb=Runtime.getRuntime().totalMemory()/1024;
	maxMemKb=Runtime.getRuntime().maxMemory()/1024;
	freeMemKb=Runtime.getRuntime().freeMemory()/1024;
	usedMemKb=totalMemKb-freeMemKb;
    }

    void updateMemStats(){
	l12.setText(freeMemKb+"Kb");
	l14.setText(usedMemKb+"Kb");
	l16.setText(totalMemKb+"Kb");
	l18.setText(maxMemKb+"Kb");
    }

    void updateProjStats(){
	String prjF=application.projectFile==null ? "" : application.projectFile.toString();
	String rdfI=application.lastRDF==null ? "" : application.lastRDF;
	int nbProps=0;
	for (Enumeration e1=application.propertiesByURI.elements();e1.hasMoreElements();){
	    for (Enumeration e2=((Vector)e1.nextElement()).elements();e2.hasMoreElements();){
		e2.nextElement();
		nbProps++;
	    }
	}
	int nbRess=application.resourcesByURI.size();
	int nbLits=application.literals.size();
	l2.setText(prjF);
	l24.setText(rdfI);
	l4.setText((new Integer(nbRess)).toString());
	l6.setText((new Integer(nbLits)).toString());
	l8.setText((new Integer(nbProps)).toString());
    }

    public void actionPerformed(ActionEvent e){
	Object source=e.getSource();
	if (source==okBt){destroy();}
	else if (source==gcBt){
	    Editor.collectGarbage();
	    retrieveSystemInfo();
	    updateMemStats();
	}
	else if (source==refreshBt){
	    retrieveSystemInfo();
	    updateMemStats();
	    updateProjStats();
	}
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
