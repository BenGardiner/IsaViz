/*   FILE: PRTableModel.java
 *   DATE OF CREATION:   12/10/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:55:32 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import javax.swing.table.*;

/*a custom table model in which no cell is editable*/

public class PRTableModel extends DefaultTableModel {

    public PRTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    public boolean isCellEditable(int row,int column){
	return false;
    }

}
