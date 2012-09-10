/*   FILE: LensTableModel.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: LensTableModel.java,v 1.1 2006/05/11 09:06:01 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import javax.swing.table.*;

/*a custom table model for Fresnel lens selection in which columns are not editable*/

public class LensTableModel extends DefaultTableModel {

    public LensTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    public boolean isCellEditable(int row,int column){
	return false;
    }

}
