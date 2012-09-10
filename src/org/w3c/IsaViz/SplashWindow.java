/*   FILE: SplashWindow.java
 *   DATE OF CREATION:   12/21/2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Apr 18 13:52:03 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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

  /**
   * A splash screen constructor (can also be used as About window)
   * @author Emmanuel Pietriga
   **/

public class SplashWindow extends JWindow implements Runnable,MouseListener {

    Thread runView;

    boolean interesting;
    String imagePath="";
    int duration=4000;
    boolean periodBringToFront;  //true will periodically bring it to front so that it always stay on top (might behave strangely on some systems)

    JProgressBar jpb;
    JScrollPane sp;
    JTextArea txtInfo;
    JLabel msg;

    /**
     *@param d display splash screen for at least d milliseconds (0 means infinite - until user clicks on the screen) - 'at least' because it will anyway wait until the JProgressBar goes to 100% (you can set it at 100% from the beginning if you want)
     *@param ip image path (from exec directory)
     *@param toFront true will periodically bring it to front so that it always stay on top (might behave strangely on some systems)
     */
    public SplashWindow(int d,String ip,boolean toFront,Frame owner){
	super(owner!=null ? owner : null);
	if (d==0){interesting=true;}
	else {interesting=false;duration=d;}
	imagePath=ip;
	periodBringToFront=toFront;
	Color color1=(Color)UIManager.get("ProgressBar.foreground");  //remember default colors 
	Color color2=(Color)UIManager.get("ProgressBar.background");
	UIManager.put("ProgressBar.foreground",Color.black);
	UIManager.put("ProgressBar.background",Color.white);
	jpb=new JProgressBar();
	UIManager.put("ProgressBar.foreground",color1);               //restore them
	UIManager.put("ProgressBar.background",color2);
	jpb.setMinimum(0);
	jpb.setMaximum(100);
	jpb.setStringPainted(false);
	Container cpane=this.getContentPane();
	Toolkit toolkit=Toolkit.getDefaultToolkit();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane.setBackground(Color.white);
	cpane.setLayout(gridBag);
	//load image (in a jar compatible way)
 	ImageIcon splashImage=new ImageIcon(this.getClass().getResource("/"+imagePath));
	JPanel canvas=new JPanel();
	canvas.addMouseListener(this);
	canvas.setBackground(Color.white);
	canvas.add(new JLabel(splashImage));
	canvas.setPreferredSize(new Dimension(320,240));
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,0,1,1,100,65);
	gridBag.setConstraints(canvas,constraints);
	cpane.add(canvas);
	//jpb.setPreferredSize(new Dimension(320,10));
	buildConstraints(constraints,0,1,1,1,100,3);
	gridBag.setConstraints(jpb,constraints);
	cpane.add(jpb);
	//message area
	msg=new JLabel(".");
	msg.setForeground(Color.black);
	msg.setBackground(Color.white);
	msg.setFont(Editor.tinyFont);
	buildConstraints(constraints,0,2,1,1,100,3);
	gridBag.setConstraints(msg,constraints);
	cpane.add(msg);
	//system properties area
	txtInfo=new JTextArea("");
	sp=new JScrollPane(txtInfo);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,3,1,1,100,25);
	gridBag.setConstraints(sp,constraints);
	cpane.add(sp);
	cpane.doLayout();
	Dimension screenSize=toolkit.getScreenSize();
	int splashWidth=splashImage.getIconWidth();
	int splashHeight=splashImage.getIconHeight();
	this.setLocation((screenSize.width-splashWidth)/2,(screenSize.height-splashHeight)/2);
	this.setSize(splashWidth,splashHeight+70);
	txtInfo.setFont(Editor.tinyFont);
	txtInfo.setBackground(Color.white);
	txtInfo.setLineWrap(true);
	txtInfo.setWrapStyleWord(true);
	txtInfo.setEditable(false);
	this.setVisible(true);
	this.start();
    }

    public void destroy(){//to kill it from another class
	if (this.isShowing()){
	    this.setVisible(false);
	    this.dispose();
	}
	this.stop();
    }

    public void start(){
	runView = new Thread(this);
	//runView.setPriority(Thread.MIN_PRIORITY);
	runView.start();
    }

    public synchronized void stop() {
	runView = null;
	notify();
    }

    public void run() {
	txtInfo.append("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
	txtInfo.append("\nOS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
	txtInfo.append("\n-----------------");
	txtInfo.append("\nDirectory information");
	txtInfo.append("\nJava Classpath: "+System.getProperty("java.class.path"));	
	txtInfo.append("\nJava directory: "+System.getProperty("java.home"));
	txtInfo.append("\nLaunching from: "+System.getProperty("user.dir"));
	txtInfo.append("\n-----------------");
	txtInfo.append("\nUser informations");
	txtInfo.append("\nUser name: "+System.getProperty("user.name"));
	txtInfo.append("\nUser home directory: "+System.getProperty("user.home"));
	while (duration>0){//this loop should last about -duration- ms
	    if (periodBringToFront){this.toFront();} //we have no way of telling that a window should always be drawn on top of the others, so we bring it to front regularly
	    try {runView.sleep(50);}
	    catch (Exception e){e.printStackTrace();}
	    duration=duration-50;
	}
	while (jpb.getValue()<100){//then, wait until progress bar has reached 100%
	    if (periodBringToFront){this.toFront();}  //we have no way of telling that a window should always be drawn on top of the others, so we bring it to front regularly
	    try {runView.sleep(50);}
	    catch (Exception e){e.printStackTrace();}
	}
	if (!interesting){
	    if (this.isShowing()){
	    this.setVisible(false);
	    this.dispose();
	    }
	    this.stop();
	}
    }

    public void mousePressed(MouseEvent e){
	if (!interesting){interesting=true;}
	else{	
	    if (this.isShowing()){
		this.setVisible(false);
		this.dispose();
	    }
	    this.stop();
	}
    }
    public void mouseReleased(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}

    public void setProgressBarValue(int v){
	jpb.setValue(v);
    }

    public void setMessage(String s){
	msg.setText(s);
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
