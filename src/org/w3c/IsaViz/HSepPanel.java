/*   FILE: HSepPanel.java
 *   DATE OF CREATION:   03/30/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:47:29 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.awt.Dimension;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.Graphics;

/**A simple swing component that draws a horizontal line*/

public class HSepPanel extends JPanel {

    int h=1;
    boolean fill=true;
    Color color=Color.black;

    /**
     *@param thick thickness of line
     *@param filled fill the interior of the line when thickness>2
     *@param c color of the line (can be null, default is black)
     */
    public HSepPanel(int thick,boolean filled,Color c){
	this.h=thick;
	this.fill=filled;
	if (c!=null){color=c;}
    }

    public void setHeight(int h){this.h=h;}

    public void setFilled(boolean b){this.fill=b;}

    public void paint(Graphics g) {
	super.paint(g);
	Dimension d=this.getSize();
	g.setColor(color);
	if (fill){g.fillRect(1,(d.height-h)/2,d.width-2,h);}else{g.drawRect(1,(d.height-h)/2,d.width-2,h);}
    }
    

}
