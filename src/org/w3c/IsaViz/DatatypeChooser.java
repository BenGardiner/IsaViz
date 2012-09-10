/*   FILE: DatatypeChooser.java
 *   DATE OF CREATION:   11/26/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Feb 20 10:39:15 2003 by Emmanuel Pietriga
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;

import com.hp.hpl.jena.datatypes.*;


public class DatatypeChooser extends JDialog implements ActionListener {

    DtTracker dt;

    TypeMapper tm;

    JButton okBt,cancelBt;

    JList types;

    public static RDFDatatype getDatatypeChooser(Frame owner,String initSel){
	DtTracker res=new DtTracker();
	DatatypeChooser dc=new DatatypeChooser(res,owner,initSel);
	dc.addWindowListener(new DatatypeChooser.Closer());
        dc.addComponentListener(new DatatypeChooser.DisposeOnClose());
	dc.setVisible(true);  //blocks until the dialog is closed
	return res.getDatatype();
    }

    public static RDFDatatype getDatatypeChooser(Dialog owner,String initSel){
	DtTracker res=new DtTracker();
	DatatypeChooser dc=new DatatypeChooser(res,owner,initSel);
	dc.addWindowListener(new DatatypeChooser.Closer());
        dc.addComponentListener(new DatatypeChooser.DisposeOnClose());
	dc.setVisible(true);  //blocks until the dialog is closed
	return res.getDatatype();
    }

    DatatypeChooser(DtTracker dtt,Frame owner,String initialSelection){
	super(owner,"IsaViz Datatype Chooser",true);
	setLocation(owner.getLocation());
	dt=dtt;
	tm=TypeMapper.getInstance();
	initUI();
	types.setSelectedValue(initialSelection,true);
    }

    DatatypeChooser(DtTracker dtt,Dialog owner,String initialSelection){
	super(owner,"IsaViz Datatype Chooser",true);
	setLocation(owner.getLocation());
	dt=dtt;
	tm=TypeMapper.getInstance();
	initUI();
	types.setSelectedValue(initialSelection,true);
    }

    void initUI(){//depending on selected item/default shape type
	Container cpane=this.getContentPane();
	try {
	   okBt.removeActionListener(this);
	   cancelBt.removeActionListener(this);
	}
	catch (NullPointerException ex){/*all these might be null (for instance when poping up for the first time)*/}
	cpane.removeAll();

	//dt list
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane.setLayout(gridBag);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	types=new JList(initTypeList());
	JScrollPane sp1=new JScrollPane(types);
	buildConstraints(constraints,0,0,1,1,100,90);
	gridBag.setConstraints(sp1,constraints);
	cpane.add(sp1);
	//ok, cancel, reset buttons
	JPanel btPanel=new JPanel();
	btPanel.setLayout(new GridLayout(1,2));
	okBt=new JButton("OK");	
	okBt.addActionListener(this);
	btPanel.add(okBt);
	cancelBt=new JButton("Cancel");	
	cancelBt.addActionListener(this);
	btPanel.add(cancelBt);
	//main components
	buildConstraints(constraints,0,1,1,1,100,10);
	gridBag.setConstraints(btPanel,constraints);
	cpane.add(btPanel);
	this.pack();
	//this.setSize(400,200);
	this.setResizable(false);
    }

    Vector initTypeList(){
	java.util.Iterator it=tm.listTypes();
	Vector items=new Vector();
	while (it.hasNext()){
	    items.add(((RDFDatatype)it.next()).getURI());
	}
	return items;
    }

    public void actionPerformed(ActionEvent e){
	Object source=e.getSource();
	if (source==okBt){
	    dt.setDatatype(getSelectedDatatype());
	    this.dispose();
	}
	else if (source==cancelBt){
	    dt.setDatatype(null);
	    this.dispose();
	}
    }
    
    RDFDatatype getSelectedDatatype(){
	String selectedType=(String)types.getSelectedValue();
	if (selectedType!=null){
	    return tm.getTypeByName(selectedType);
	}
	else return null;
    }

    static void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }
    
    static class Closer extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            Window w = e.getWindow();
            w.setVisible(false);
        }
    }

    static class DisposeOnClose extends ComponentAdapter {
        public void componentHidden(ComponentEvent e) {
            Window w = (Window)e.getComponent();
            w.dispose();
        }
    }

}

class DtTracker {

    RDFDatatype type;

    public void setDatatype(RDFDatatype t){
	type=t;
    }

    public RDFDatatype getDatatype() {
        return type;
    }
}
