/*   FILE: PropsPanel.java
 *   DATE OF CREATION:   12/05/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Oct 15 09:30:07 2004 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: PropsPanel.java,v 1.5 2005/07/21 14:31:40 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.Vector;

/*Display (and edit) an INode's attributes (URI, anonymous, value...) + appropriate commands, like delete*/

class PropsPanel extends JFrame {

    Editor application;
    JPanel mainPanel;

    JTextComponent tc;

    PropsPanel(Editor e,int x,int y,int width,int height){
	this.application=e;
	Container cpane=this.getContentPane();
	mainPanel=new JPanel();
	cpane.add(mainPanel);
	//window
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){application.cmp.showPropsMn.setSelected(false);}
		public void windowActivated(WindowEvent e){application.alwaysUpdateViews(true);}
	    };
	this.addWindowListener(w0);
	this.setTitle("Attributes");
	this.pack();
	this.setLocation(x,y);
	this.setSize(width,height);
    }

    void updateDisplay(INode o){
	if (o instanceof IResource){this.showResourceProps((IResource)o);}
	else if (o instanceof ILiteral){this.showLiteralProps((ILiteral)o);}
	else if (o instanceof IProperty){this.showPropertyProps((IProperty)o);}
    }

    void reset(){
	mainPanel.removeAll();
	this.paintAll(this.getGraphics());
    }

    void showResourceProps(final IResource r){
	if (r!=null){
	    final JRadioButton uriBt=new JRadioButton("URI");
	    final JRadioButton idBt=new JRadioButton("ID");
	    final JCheckBox anonBt=new JCheckBox("bnode",r.isAnon());
	    String id=r.getIdentity();
	    tc=new JTextField(id.startsWith(Editor.BASE_URI) ? id.substring(Editor.BASE_URI.length(),id.length()) : id);
	    tc.setFont(Editor.swingFont);
	    final JButton delete=new JButton("Delete");
	    final JButton showProperties=new JButton("Show Properties");
	    final JLabel caption=new JLabel(" ");

	    ActionListener a1=new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			if (e.getSource()==delete){
			    //application.deleteResource(r);
			    application.deleteSelectedEntities();
			    reset();
			}
			else if (e.getSource()==uriBt){
			    caption.setText(" ");
			    this.checkAndUpdateResource();
			}
			else if (e.getSource()==idBt){
			    caption.setText("Base URI="+Editor.BASE_URI);
			    this.checkAndUpdateResource();
			}
			else if (e.getSource()==showProperties){
			    application.displayResOutgoingPredicates(r);
			}
			else if (e.getSource()==anonBt){
			    if (anonBt.isSelected()){
				caption.setText(" ");
				tc.setEnabled(false);
				uriBt.setEnabled(false);
				idBt.setEnabled(false);
				application.makeAnonymous(r);
				tc.setText(r.getIdentity());
			    }
			    else {
				tc.setText("");
				tc.setEnabled(true);
				uriBt.setEnabled(true);
				uriBt.setSelected(true);
				idBt.setEnabled(true);
			    }
			}
		    }

		    void checkAndUpdateResource(){
			if (tc.getText().length()>0){
			    if (!tc.getText().startsWith(Editor.ANON_NODE)){
				application.changeResourceURI(r,tc.getText(),uriBt.isSelected()); //if uriBt not selected, we have an ID
				String id=r.getIdentity();//update since it may have been changed by changeResourceURI
				tc.setText(id.startsWith(Editor.BASE_URI) ? id.substring(Editor.BASE_URI.length(),id.length()) : id); //don't display default namespace if we have an ID 
			    }//(appended automatically in the resource itself)
			    else {javax.swing.JOptionPane.showMessageDialog(application.propsp,Editor.ANON_NODE+" is reserved for anonymous nodes.");}
			}
			else {javax.swing.JOptionPane.showMessageDialog(application.propsp,Messages.provideURI);}
		    }
		};
	    
	    KeyListener k1=new KeyListener(){
		    public void keyPressed(KeyEvent e){
			if (e.getKeyCode()==KeyEvent.VK_ENTER){
			    if (e.getSource()==tc){
				if (tc.getText().length()>0){
				    if (!tc.getText().startsWith(Editor.ANON_NODE)){
					if (uriBt.isSelected()){
					    String uri=tc.getText();
					    if (ConfigManager.ALLOW_PFX_IN_TXTFIELDS){uri=application.tryToSolveBinding(uri);}
					    application.changeResourceURI(r,uri,true); //if uriBt not selected, we have an ID
					    tc.setText(r.getIdentity());			    
					}
					else {
					    application.changeResourceURI(r,tc.getText(),false); //if uriBt not selected, we have an ID
					    String id=r.getIdentity();//update since it may have been changed by changeResourceURI
					    tc.setText(id.startsWith(Editor.BASE_URI) ? id.substring(Editor.BASE_URI.length()) : id);
					    //don't display default namespace if we have an ID
					    //(appended automatically in the resource itself)
					}
				    }
				    else {javax.swing.JOptionPane.showMessageDialog(application.propsp,Editor.ANON_NODE+" is reserved for anonymous nodes.");}
				}
				else {javax.swing.JOptionPane.showMessageDialog(application.propsp,Messages.provideURI);}
			    }
			}
		    }
		    
		    public void keyReleased(KeyEvent e){}
		    public void keyTyped(KeyEvent e){}
		};

	    mainPanel.removeAll();
	    GridBagLayout gridBag=new GridBagLayout();
	    GridBagConstraints constraints=new GridBagConstraints();
	    mainPanel.setLayout(gridBag);
	    constraints.fill=GridBagConstraints.NONE;
	    constraints.anchor=GridBagConstraints.WEST;

	    ButtonGroup bg=new ButtonGroup();
	    buildConstraints(constraints,0,0,1,1,34,3);
	    gridBag.setConstraints(uriBt,constraints);
	    mainPanel.add(uriBt);bg.add(uriBt);
	    buildConstraints(constraints,1,0,1,1,33,0);
	    gridBag.setConstraints(idBt,constraints);
	    mainPanel.add(idBt);bg.add(idBt);
	    if (!Utils.isWhiteSpaceCharsOnly(Editor.BASE_URI)){
		if (r.getIdentity().startsWith(Editor.BASE_URI)){
		    idBt.setSelected(true);
		    caption.setText("Base URI="+Editor.BASE_URI);
		}
		else {uriBt.setSelected(true);}
	    }
	    else {
		if (r.getIdentity().startsWith("#")){
		    idBt.setSelected(true);
		    caption.setText("Base URI="+Editor.BASE_URI);
		}
		else {uriBt.setSelected(true);}
	    }
	    uriBt.addActionListener(a1);
	    idBt.addActionListener(a1);

	    constraints.anchor=GridBagConstraints.EAST;
	    anonBt.addActionListener(a1);
	    buildConstraints(constraints,2,0,1,1,33,0);
	    gridBag.setConstraints(anonBt,constraints);
	    mainPanel.add(anonBt);

	    constraints.anchor=GridBagConstraints.CENTER;
	    constraints.fill=GridBagConstraints.BOTH;
	    buildConstraints(constraints,0,1,3,1,100,3);
	    gridBag.setConstraints(caption,constraints);
	    mainPanel.add(caption);

	    constraints.fill=GridBagConstraints.HORIZONTAL;
	    tc.addKeyListener(k1);
	    buildConstraints(constraints,0,2,3,1,100,3);
	    gridBag.setConstraints(tc,constraints);
	    mainPanel.add(tc);

	    if (r.isAnon()){//make input fields not editable if anonymous resource
		tc.setEnabled(false);
		uriBt.setEnabled(false);
		idBt.setEnabled(false);
	    }

	    delete.addActionListener(a1);
	    buildConstraints(constraints,0,3,3,1,100,3);
	    gridBag.setConstraints(delete,constraints);
	    mainPanel.add(delete);

	    showProperties.addActionListener(a1);
	    buildConstraints(constraints,0,4,3,1,100,3);
	    gridBag.setConstraints(showProperties,constraints);
	    mainPanel.add(showProperties);

	    //fill remaining space
	    constraints.fill=GridBagConstraints.BOTH;
	    JPanel p0=new JPanel();
	    buildConstraints(constraints,0,5,3,1,100,88);
	    gridBag.setConstraints(p0,constraints);
	    mainPanel.add(p0);
	    this.validate();
	    //this.paintAll(this.getGraphics());
	}
    }

    void showLiteralProps(final ILiteral l){
	if (l!=null){

 	    final JCheckBox typedBt=new JCheckBox("Typed Literal",(l.getDatatype()!=null) ? true : false);
	    final JButton dturiBt=new JButton("Datatype URI: ");
	    final JTextField tfType=new JTextField((l.getDatatype()!=null) ? l.getDatatype().getURI() : "");
	    final JLabel lang=new JLabel("lang: ");
	    final JTextField tfLang=new JTextField((l.getLang()!=null) ? l.getLang() : "");
	    if (l.getDatatype()==null){tfType.setEnabled(false);dturiBt.setEnabled(false);}
	    else {tfLang.setEnabled(false);lang.setEnabled(false);}
	    tc=new JTextArea((l.getValue()!=null) ? l.getValue() : "");
	    tc.setFont(Editor.swingFont);
	    final JButton delete=new JButton("Delete");

	    ActionListener a2=new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			if (e.getSource()==delete){
			    //application.deleteLiteral(l);
			    application.deleteSelectedEntities();
			    reset();
			}
			else if (e.getSource()==typedBt){
			    if (typedBt.isSelected()){
				tfType.setEnabled(true);
				dturiBt.setEnabled(true);
				tfLang.setEnabled(false);
				lang.setEnabled(false);
			    }
			    else {
				tfType.setText("");
				tfType.setEnabled(false);
				dturiBt.setEnabled(false);
				l.setDatatype((String)null);
				tfLang.setEnabled(true);
				lang.setEnabled(true);
				application.gssMngr.incStyling(l);
			    }
			}
			else if (e.getSource()==dturiBt){
			    com.hp.hpl.jena.datatypes.RDFDatatype dt=Editor.displayAvailableDataTypes(tfType.getText());
			    if (dt!=null){
				l.setDatatype(dt);
				tfType.setText(l.getDatatype().getURI());
				application.gssMngr.incStyling(l);
			    }
			}
		    }
		};

	    KeyListener k2=new KeyListener(){
		    public void keyPressed(KeyEvent e){
			if (e.getKeyCode()==KeyEvent.VK_ENTER){
			    if (e.getSource()==tfLang){
				l.setLanguage((tfLang.getText().length()>0) ? tfLang.getText() : null);
			    }
			    else if (e.getSource()==tfType){
				String uri=tfType.getText();
				if (ConfigManager.ALLOW_PFX_IN_TXTFIELDS && typedBt.isSelected() && uri.length()>0 && !Utils.isWhiteSpaceCharsOnly(uri)){
				    uri=application.tryToSolveBinding(uri);
				    tfType.setText(uri);
				}
				l.setDatatype((uri.length()>0) ? uri : null);
				application.gssMngr.incStyling(l);
			    }
			}
		    }
		    
		    public void keyReleased(KeyEvent e){}
		    public void keyTyped(KeyEvent e){}
		};

	    FocusListener f2=new FocusListener(){
		    public void focusGained(FocusEvent e){}
		    public void focusLost(FocusEvent e){
			if (e.getSource()==tfLang){
			    l.setLanguage((tfLang.getText().length()>0) ? tfLang.getText() : null);
			}
			else if (e.getSource()==tfType){
			    String uri=tfType.getText();
			    if (ConfigManager.ALLOW_PFX_IN_TXTFIELDS && typedBt.isSelected() && uri.length()>0 && !Utils.isWhiteSpaceCharsOnly(uri)){
				uri=application.tryToSolveBinding(uri);
				tfType.setText(uri);
			    }
			    l.setDatatype((uri.length()>0) ? uri : null);
			    application.gssMngr.incStyling(l);
			}
			else if (e.getSource()==tc){
			    application.setLiteralValue(l,tc.getText());
			}
		    }
		};


	    mainPanel.removeAll();
	    GridBagLayout gridBag=new GridBagLayout();
	    GridBagConstraints constraints=new GridBagConstraints();
	    mainPanel.setLayout(gridBag);

	    constraints.fill=GridBagConstraints.NONE;
	    constraints.anchor=GridBagConstraints.WEST;
	    buildConstraints(constraints,0,0,1,1,10,3);
	    gridBag.setConstraints(lang,constraints);
	    mainPanel.add(lang);
	    constraints.fill=GridBagConstraints.HORIZONTAL;
	    tfLang.addKeyListener(k2);
	    tfLang.addFocusListener(f2);
	    buildConstraints(constraints,1,0,1,1,30,0);
	    gridBag.setConstraints(tfLang,constraints);
	    mainPanel.add(tfLang);

	    constraints.fill=GridBagConstraints.NONE;
	    constraints.anchor=GridBagConstraints.EAST;
	    typedBt.addActionListener(a2);
	    buildConstraints(constraints,2,0,1,1,80,0);
	    gridBag.setConstraints(typedBt,constraints);
	    mainPanel.add(typedBt);

	    constraints.anchor=GridBagConstraints.WEST;
	    constraints.fill=GridBagConstraints.NONE;
	    dturiBt.setBorder(BorderFactory.createEtchedBorder());
	    buildConstraints(constraints,0,1,1,1,10,3);
	    gridBag.setConstraints(dturiBt,constraints);
	    mainPanel.add(dturiBt);
	    dturiBt.addActionListener(a2);
	    constraints.fill=GridBagConstraints.HORIZONTAL;
	    tfType.addKeyListener(k2);
	    tfType.addFocusListener(f2);
	    buildConstraints(constraints,1,1,2,1,90,0);
	    gridBag.setConstraints(tfType,constraints);
	    mainPanel.add(tfType);

	    constraints.anchor=GridBagConstraints.CENTER;
	    delete.addActionListener(a2);
	    buildConstraints(constraints,0,2,3,1,100,3);
	    gridBag.setConstraints(delete,constraints);
	    mainPanel.add(delete);

	    constraints.fill=GridBagConstraints.BOTH;
 	    tc.addFocusListener(f2);
	    JScrollPane sp=new JScrollPane(tc);
	    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    buildConstraints(constraints,0,3,3,1,100,94);
	    gridBag.setConstraints(sp,constraints);
	    mainPanel.add(sp);
	    this.validate();
	    //this.paintAll(this.getGraphics());
	}
    }

    void showPropertyProps(final IProperty p){
	if (p!=null){
	    final JButton delete=new JButton("Delete");

	    Vector v=application.getAllPropertyNS(); //add an additional element to
	    final JComboBox nscbb=new JComboBox(v);
	    String prefix=application.getNSBinding(p.getNamespace());
	    nscbb.setSelectedItem((prefix!=null) ? prefix : p.getNamespace());
	    final JComboBox lncbb=new JComboBox(application.getProperties4NS(p.getNamespace()));
	    lncbb.setSelectedItem(p.getLocalname());
	    nscbb.setMaximumRowCount(10);
	    lncbb.setMaximumRowCount(10);

	    ActionListener a3=new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			if (e.getSource()==delete){
			    application.deleteSelectedEntities();
			    reset();
			}
		    }
		};

	    ItemListener i3=new ItemListener(){
		    public void itemStateChanged(ItemEvent e){
			if (e.getSource()==nscbb){//update name list to display only props
			    if (e.getStateChange()==ItemEvent.SELECTED){//in this namespace
				lncbb.removeAllItems();
				Vector v=application.getProperties4NS((String)e.getItem());
				for (int i=0;i<v.size();i++){
				    lncbb.addItem(v.elementAt(i));
				}
			    }
			}
			else {//e.getSource()==lncbb
			    if (e.getStateChange()==ItemEvent.SELECTED){
				application.changePropertyURI(p,(String)nscbb.getSelectedItem(),(String)lncbb.getSelectedItem());
			    }
			}
		    }
		};

	    mainPanel.removeAll();
	    GridBagLayout gridBag=new GridBagLayout();
	    GridBagConstraints constraints=new GridBagConstraints();
	    mainPanel.setLayout(gridBag);
	    constraints.fill=GridBagConstraints.HORIZONTAL;
	    constraints.anchor=GridBagConstraints.CENTER;

	    delete.addActionListener(a3);
	    buildConstraints(constraints,0,0,1,1,100,5);
	    gridBag.setConstraints(delete,constraints);
	    mainPanel.add(delete);

	    JLabel l0=new JLabel("Namespace");
	    buildConstraints(constraints,0,1,1,1,100,5);
	    gridBag.setConstraints(l0,constraints);
	    mainPanel.add(l0);

	    nscbb.addItemListener(i3);
	    buildConstraints(constraints,0,2,1,1,100,5);
	    gridBag.setConstraints(nscbb,constraints);
	    mainPanel.add(nscbb);

	    JLabel l1=new JLabel("Property");
	    buildConstraints(constraints,0,3,1,1,100,5);
	    gridBag.setConstraints(l1,constraints);
	    mainPanel.add(l1);

	    lncbb.addItemListener(i3);
	    buildConstraints(constraints,0,4,1,1,100,5);
	    gridBag.setConstraints(lncbb,constraints);
	    mainPanel.add(lncbb);

	    //fill remaining space
	    constraints.fill=GridBagConstraints.BOTH;
	    JPanel p0=new JPanel();
	    buildConstraints(constraints,0,5,1,1,100,75);
	    gridBag.setConstraints(p0,constraints);
	    mainPanel.add(p0);
	    this.validate();
	    //this.paintAll(this.getGraphics());
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

    void updateSwingFont(){
	if (tc!=null){tc.setFont(Editor.swingFont);}
    }

}



























