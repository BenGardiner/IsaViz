/*   FILE: SchemaManager.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: SchemaManager.java,v 1.2 2005/10/06 09:10:45 epietrig Exp $
 */ 

package org.w3c.IsaViz;

import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.DefaultTableModel;

class SchemaManager extends JFrame {

    int FRAME_WIDTH = 800;
    int FRAME_HEIGHT = 400;

    Editor application;
    JTable ns2schemaURI;
    SchTableModel schTableModel;

    SchemaManager(Editor app, boolean show){
	super();
	this.application = app;
	this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
	initGUI();
	if (show){
	    this.setVisible(true);
	}
    }

    void initGUI(){
	schTableModel = new SchTableModel(0, 4);
	ns2schemaURI = new JTable(schTableModel);
	ns2schemaURI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	TableColumn tc = ns2schemaURI.getColumnModel().getColumn(0);
	tc.setPreferredWidth(FRAME_WIDTH/100*20);
	tc.setHeaderValue("Prefix");
	tc = ns2schemaURI.getColumnModel().getColumn(1);
	tc.setPreferredWidth(FRAME_WIDTH/100*35);
	tc.setHeaderValue("Namespace URI");
	tc = ns2schemaURI.getColumnModel().getColumn(2);
	tc.setPreferredWidth(FRAME_WIDTH/100*35);
	tc.setHeaderValue("Schema URI");
	tc = ns2schemaURI.getColumnModel().getColumn(3);
	tc.setPreferredWidth(FRAME_WIDTH/100*10);
	tc.setHeaderValue("Retrieve and Use");
	//display 4th column as checkbox (it is a boolean)
	TableCellRenderer tcr = ns2schemaURI.getDefaultRenderer(Boolean.class);
	tc.setCellRenderer(tcr);
	TableCellEditor tce = ns2schemaURI.getDefaultEditor(Boolean.class);
	tc.setCellEditor(tce);
	Container cpane = this.getContentPane();
	JScrollPane sp1 = new JScrollPane(ns2schemaURI);
	cpane.add(sp1);
    }

    void updateNamespaces(){
	TableModel nsTableModel = application.tblp.nsTableModel;
	String nsURI;
	int index;
	for (int i=0;i<nsTableModel.getRowCount();i++){
	    nsURI = (String)nsTableModel.getValueAt(i, 1);
	    if ((index=getNamespaceIndex(nsURI)) != -1){
		updateNamespace(index, (String)nsTableModel.getValueAt(i, 0));
	    }
	    else {
		addNamespace((String)nsTableModel.getValueAt(i, 0), nsURI);
	    }
	}
    }

    void updateNamespace(int index, String prefix){
	if (!schTableModel.getValueAt(index, 0).equals(prefix)){
	    schTableModel.setValueAt(prefix, index, 0);
	}
    }

    void addNamespace(String prefix, String nsURI){
	Object[] data = new Object[4];
	data[0] = prefix;
	data[1] = nsURI;
	// duplicate the Namespace URI as the default schema URI
	// users will be able to change it afterwards if incorrect
	data[2] = nsURI;
	data[3] = Boolean.FALSE;
	schTableModel.addRow(data);
    }

    /*returns the namespace's index in the schema table*/
    int getNamespaceIndex(String nsURI){
	for (int i=0;i<schTableModel.getRowCount();i++){
	    if (schTableModel.getValueAt(i, 1).equals(nsURI)){
		return i;
	    }
	}
	return -1;
    }

}