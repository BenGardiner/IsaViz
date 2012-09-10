/*   FILE: NSTableModel.java
 *   DATE OF CREATION:   12/10/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:53:29 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import javax.swing.table.*;

/*a custom table model for namespace bindings in which column 1 is not editable
also checks that prefixes do not end with ':' - get rid of it if that is the case*/

public class NSTableModel extends DefaultTableModel {

    public NSTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    public boolean isCellEditable(int row,int column){
	if (column==1){return false;}
	else {return true;}
    }

    public void setValueAt(Object aValue,int row,int column){
	if (column==0){
	    String prefix=(String)aValue;
	    if (prefix.endsWith(":")){prefix=prefix.substring(0,prefix.length()-1);}
	    super.setValueAt(prefix,row,column);
	}
	else {super.setValueAt(aValue,row,column);}
    }

}
