/*   FILE: SchTableModel.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: SchTableModel.java,v 1.1 2005/10/06 09:11:33 epietrig Exp $
 */ 

package org.w3c.IsaViz;

import javax.swing.table.DefaultTableModel;

/*a custom table model for RDFS/OWL schemas*/

public class SchTableModel extends DefaultTableModel {

    public SchTableModel(int nbRow, int nbCol){
	super(nbRow, nbCol);
    }

    public boolean isCellEditable(int row, int column){
	if (column<2){return false;}
	else {return true;}
    }

}
