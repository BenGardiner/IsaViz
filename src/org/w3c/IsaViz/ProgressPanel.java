/*   FILE: ProgressPanel.java
 *   DATE OF CREATION:   Wed Jul 30 09:39:13 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 30 10:20:56 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.awt.*;
import javax.swing.*;

public class ProgressPanel extends JPanel {

    int ratio=0;

    Color bkgColor=Color.lightGray;
    Color frgColor=Color.darkGray;

    public ProgressPanel(){
	super();
    }

    void setPBValue(int i){
	if (i<0){ratio=0;}
	else if (i>100){ratio=100;}
	else ratio=i;
	this.repaint();
    }

    void setBackgroundColor(Color c){
	bkgColor=c;
	this.setBackground(bkgColor);
    }

    void setForegroundColor(Color c){
	frgColor=c;
    }

    public void paint(Graphics g){
	g.clearRect(0,0,this.getWidth(),this.getHeight());
	g.setColor(frgColor);
	g.fillRect(0,0,Math.round(this.getWidth()/100f*ratio)-4,this.getHeight()-1);
	g.setColor(Color.black);
	g.drawRect(0,0,this.getWidth()-4,this.getHeight()-1);
    }

}
