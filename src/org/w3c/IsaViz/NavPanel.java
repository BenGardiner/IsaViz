/*   FILE: NavPanel.java
 *   DATE OF CREATION:   05/08/2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu May 08 11:33:31 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/*Navigation Panel (directional arrows, plus zoom)*/

public class NavPanel extends JFrame implements ActionListener,KeyListener {

    Editor application;

    JButton mvNBt,mvNEBt,mvEBt,mvSEBt,mvSBt,mvSWBt,mvWBt,mvNWBt,mvHBt,zmIBt,zmOBt;

    public NavPanel(Editor app,int x,int y){
	this.application=app;
	Container cpane=this.getContentPane();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.NONE;
	constraints.anchor=GridBagConstraints.CENTER;
	cpane.setLayout(gridBag);
	//translation buttons in a 3x3 grid
	JPanel p1=new JPanel();
	p1.setLayout(new GridLayout(3,3));
// 	p1.setBackground(Color.white);
	mvNWBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_nw.gif")));
	mvNWBt.setBorder(BorderFactory.createEmptyBorder());
	mvNWBt.setContentAreaFilled(false);
	mvNWBt.setBorderPainted(false);
	mvNWBt.setFocusPainted(false);
	mvNWBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_nw_h.gif")));
 	mvNWBt.addActionListener(this);
	p1.add(mvNWBt);
	mvNBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_n.gif")));
	mvNBt.setBorder(BorderFactory.createEmptyBorder());
	mvNBt.setContentAreaFilled(false);
	mvNBt.setBorderPainted(false);
	mvNBt.setFocusPainted(false);
	mvNBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_n_h.gif")));
 	mvNBt.addActionListener(this);
	p1.add(mvNBt);
	mvNEBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_ne.gif")));
	mvNEBt.setBorder(BorderFactory.createEmptyBorder());
	mvNEBt.setContentAreaFilled(false);
	mvNEBt.setBorderPainted(false);
	mvNEBt.setFocusPainted(false);
	mvNEBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_ne_h.gif")));
 	mvNEBt.addActionListener(this);
	p1.add(mvNEBt);
	mvWBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_w.gif")));
	mvWBt.setBorder(BorderFactory.createEmptyBorder());
	mvWBt.setContentAreaFilled(false);
	mvWBt.setBorderPainted(false);
	mvWBt.setFocusPainted(false);
	mvWBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_w_h.gif")));
 	mvWBt.addActionListener(this);
	p1.add(mvWBt);
	mvHBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_home.gif")));
	mvHBt.setBorder(BorderFactory.createEmptyBorder());
	mvHBt.setContentAreaFilled(false);
	mvHBt.setBorderPainted(false);
	mvHBt.setFocusPainted(false);
	mvHBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_home_h.gif")));
 	mvHBt.addActionListener(this);
	p1.add(mvHBt);
	mvEBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_e.gif")));
	mvEBt.setBorder(BorderFactory.createEmptyBorder());
	mvEBt.setContentAreaFilled(false);
	mvEBt.setBorderPainted(false);
	mvEBt.setFocusPainted(false);
	mvEBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_e_h.gif")));
 	mvEBt.addActionListener(this);
	p1.add(mvEBt);
	mvSWBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_sw.gif")));
	mvSWBt.setBorder(BorderFactory.createEmptyBorder());
	mvSWBt.setContentAreaFilled(false);
	mvSWBt.setBorderPainted(false);
	mvSWBt.setFocusPainted(false);
	mvSWBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_sw_h.gif")));
 	mvSWBt.addActionListener(this);
	p1.add(mvSWBt);
	mvSBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_s.gif")));
	mvSBt.setBorder(BorderFactory.createEmptyBorder());
	mvSBt.setContentAreaFilled(false);
	mvSBt.setBorderPainted(false);
	mvSBt.setFocusPainted(false);
	mvSBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_s_h.gif")));
 	mvSBt.addActionListener(this);
	p1.add(mvSBt);
	mvSEBt=new JButton(new ImageIcon(this.getClass().getResource("/images/m_se.gif")));
	mvSEBt.setBorder(BorderFactory.createEmptyBorder());
	mvSEBt.setContentAreaFilled(false);
	mvSEBt.setBorderPainted(false);
	mvSEBt.setFocusPainted(false);
	mvSEBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/m_se_h.gif")));
 	mvSEBt.addActionListener(this);
	p1.add(mvSEBt);
	buildConstraints(constraints,0,0,1,1,60,100);
	gridBag.setConstraints(p1,constraints);
	cpane.add(p1);
	//zoom buttons
	JPanel p2=new JPanel();
	p2.setLayout(new GridLayout(2,1));
	zmIBt=new JButton(new ImageIcon(this.getClass().getResource("/images/zm_i.gif")));
	zmIBt.setBorder(BorderFactory.createEmptyBorder());
	zmIBt.setContentAreaFilled(false);
	zmIBt.setBorderPainted(false);
	zmIBt.setFocusPainted(false);
	zmIBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/zm_i_h.gif")));
 	zmIBt.addActionListener(this);
	p2.add(zmIBt);
	zmOBt=new JButton(new ImageIcon(this.getClass().getResource("/images/zm_o.gif")));
	zmOBt.setBorder(BorderFactory.createEmptyBorder());
	zmOBt.setContentAreaFilled(false);
	zmOBt.setBorderPainted(false);
	zmOBt.setFocusPainted(false);
	zmOBt.setRolloverIcon(new ImageIcon(this.getClass().getResource("/images/zm_o_h.gif")));
 	zmOBt.addActionListener(this);
	p2.add(zmOBt);
	buildConstraints(constraints,1,0,1,1,40,0);
	gridBag.setConstraints(p2,constraints);
	cpane.add(p2);
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){application.cmp.showNavMn.setSelected(false);}
	    };
	this.addWindowListener(w0);
	this.setTitle("Navigation");
	this.pack();
	this.setLocation(x,y);
	this.setResizable(false);
// 	this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
	Object o=e.getSource();
	if (o==zmIBt){application.getLowerView();}
	else if (o==zmOBt){application.getHigherView();}
	else if (o==mvHBt){application.getGlobalView();}
	else if (o==mvNBt){application.translateView(Editor.MOVE_UP);}
	else if (o==mvSBt){application.translateView(Editor.MOVE_DOWN);}
	else if (o==mvEBt){application.translateView(Editor.MOVE_RIGHT);}
	else if (o==mvWBt){application.translateView(Editor.MOVE_LEFT);}
	else if (o==mvNWBt){application.translateView(Editor.MOVE_UP_LEFT);}
	else if (o==mvNEBt){application.translateView(Editor.MOVE_UP_RIGHT);}
	else if (o==mvSWBt){application.translateView(Editor.MOVE_DOWN_LEFT);}
	else if (o==mvSEBt){application.translateView(Editor.MOVE_DOWN_RIGHT);}
    }

    public void keyPressed(KeyEvent e){
	int code=e.getKeyCode();
	if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code==KeyEvent.VK_HOME || (code==KeyEvent.VK_G && e.isControlDown())){application.getGlobalView();}
	else if (code==KeyEvent.VK_UP){application.translateView(Editor.MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(Editor.MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(Editor.MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(Editor.MOVE_RIGHT);}
	else if (code==KeyEvent.VK_B && e.isControlDown()){application.moveBack();}
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
