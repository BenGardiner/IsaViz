/*   FILE: GSSTableModel.java
 *   DATE OF CREATION:   Thu Mar 13 16:55:03 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Mar 13 16:56:18 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import javax.swing.table.*;

/*a custom table model for graph stylesheets in which columns are not editable*/

public class GSSTableModel extends DefaultTableModel {

    public GSSTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    public boolean isCellEditable(int row,int column){
	return false;
    }

}
