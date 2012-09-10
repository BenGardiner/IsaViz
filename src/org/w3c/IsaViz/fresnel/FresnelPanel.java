/*   FILE: FresnelPanel.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FresnelPanel.java,v 1.5 2006/05/17 14:41:37 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.w3c.IsaViz.*;

public class FresnelPanel extends JPanel implements ActionListener {

    FresnelManager fm;

    JTable lensTable;
    LensTableModel ltm;

    static final String LOAD_LENSES_BT_TXT = "Add lenses...";
    static final String USE_LENS_BT_TXT = "Use lens";
    JButton loadLensesBt, useLensBt;

    public FresnelPanel(Editor app){
	super();
	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	this.setLayout(gridBag);
	ltm = new LensTableModel(0, 1);
	lensTable = new JTable(ltm);
	lensTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	JScrollPane sp1 = new JScrollPane(lensTable);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	TablePanel.buildConstraints(constraints,0,0,2,1,100,99);
	gridBag.setConstraints(sp1, constraints);
	add(sp1);
	lensTable.getColumnModel().getColumn(0).setHeaderValue("Lenses");
	loadLensesBt = new JButton(LOAD_LENSES_BT_TXT);
	constraints.anchor = GridBagConstraints.CENTER;
	TablePanel.buildConstraints(constraints,0,1,1,1,100,1);
	gridBag.setConstraints(loadLensesBt, constraints);
	add(loadLensesBt);
	loadLensesBt.addActionListener(this);
	useLensBt = new JButton(USE_LENS_BT_TXT);
	TablePanel.buildConstraints(constraints,1,1,1,1,100,1);
	gridBag.setConstraints(useLensBt, constraints);
	add(useLensBt);
	useLensBt.addActionListener(this);
	fm = new FresnelManager(this, app);
    }
    
    public void actionPerformed(ActionEvent e){
	if (e.getSource() == loadLensesBt){
	    JFileChooser fc = new JFileChooser(Editor.lastImportRDFDir != null ? Editor.lastImportRDFDir : Editor.rdfDir);
	    fc.setDialogTitle("Load additional lenses from");
	    int returnVal = fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		fm.loadLenses(fc.getSelectedFile(), RDFLoader.N3_READER);
	    }
	}
	else if (e.getSource() == useLensBt){
	    int lensIndex = lensTable.getSelectedRow();
	    if (lensIndex > 0){// lens at index 0 is "No lens"
		fm.useLens((Lens)lensTable.getValueAt(lensIndex, 0));
	    }
	    else {
		fm.useLens(null);
	    }
	}
    }

}