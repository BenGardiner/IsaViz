/*   FILE: BookmarkPanel.java
 *   DATE OF CREATION:  Thu Feb 10 13:54:15 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: BookmarkPanel.java,v 1.2 2005/02/15 14:28:52 epietrig Exp $
 */ 

package org.w3c.IsaViz;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Vector;
import java.util.Hashtable;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.engine.Location;

class BookmarkPanel extends JFrame implements ActionListener, ListSelectionListener {

    Editor application;

    JScrollPane sp;
    JTable bkTable;
    BookmarkTableModel bkTableModel;
    JButton addBt, rmBt;

    /*key=bookmark name (String), value=Location*/
    Hashtable bookmarks;

    //ugly hack to prevent valueChanged events due to bookmark
    //deletion from being processed as bookmark selection events
    boolean removingRow = false;

    public BookmarkPanel(Editor app){
	this.application = app;
	Container cpane = this.getContentPane();
	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.WEST;
	cpane.setLayout(gridBag);
	bkTableModel = new BookmarkTableModel(0,1);
	bkTable = new JTable(bkTableModel);
	bkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	bkTable.getColumnModel().getColumn(0).setHeaderValue("");
	sp = new JScrollPane(bkTable);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	buildConstraints(constraints,0,0,2,1,100,99);
	gridBag.setConstraints(sp, constraints);
	cpane.add(sp);
	bkTable.getSelectionModel().addListSelectionListener(this);
	addBt = new JButton("Add bookmark...");
	buildConstraints(constraints,0,1,1,1,50,1);
	gridBag.setConstraints(addBt, constraints);
	cpane.add(addBt);
	rmBt = new JButton("Remove bookmark");
	buildConstraints(constraints,1,1,1,1,50,0);
	gridBag.setConstraints(rmBt, constraints);
	cpane.add(rmBt);
	addBt.addActionListener(this);
	rmBt.addActionListener(this);
	WindowListener w0 = new WindowAdapter(){
		public void windowClosing(WindowEvent e){application.cmp.showBkMn.setSelected(false);}
	    };
	this.addWindowListener(w0);
	this.setTitle("Bookmarks");
	this.pack();
	this.setSize(300,400);
	bookmarks = new Hashtable();
    }

    public void actionPerformed(ActionEvent e){
	Object o = e.getSource();
	if (o == addBt){
	    addBookmark();
	}
	else if (o == rmBt){
	    removeSelectedBookmark();
	}
    }

    public void addBookmark(){
	Location l = Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0).getLocation();
	String bkName = JOptionPane.showInputDialog("Bookmark name");
	addBookmark(bkName, l);
    }
    
    public void addBookmark(String bkName, Location l){
	if (bkName != null && bkName.length() > 0 && !Utils.isWhiteSpaceCharsOnly(bkName)){
	    bookmarks.put(bkName, l);
	    Vector rows = (Vector)bkTableModel.getDataVector();
	    boolean bkNameExists = false;
	    if (rows.size() > 0){
		rows = (Vector)rows.firstElement();
		for (int i=0;i<rows.size();i++){
		    if (rows.elementAt(i).equals(bkName)){bkNameExists = true;break;}
		}
	    }
	    if (!bkNameExists){
		Vector v = new Vector();
		v.add(bkName);
		bkTableModel.addRow(v);
	    }
	}
    }

    public void removeSelectedBookmark(){
	int selectedIndex = bkTable.getSelectedRow();
	if (selectedIndex != -1){
	    Object selectedBkName = bkTable.getValueAt(bkTable.getSelectedRow(), 0);
	    bookmarks.remove(selectedBkName);
	    removingRow = true;
	    bkTableModel.removeRow(selectedIndex);
	}
    }

    public void valueChanged(ListSelectionEvent e){
	if (!e.getValueIsAdjusting()){
	    if (removingRow){
		removingRow = false;
	    }
	    else {
		Object selectedBkName = bkTable.getValueAt(bkTable.getSelectedRow(), 0);
		Location targetLoc = (Location)bookmarks.get(selectedBkName);
		if (targetLoc != null){
		    Camera c = Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0);
		    Vector animData = Location.getDifference(c.getLocation(), targetLoc);
		    Editor.vsm.animator.createCameraAnimation(500, AnimManager.CA_ALT_TRANS_SIG,
							      animData, c.getID());
		}
	    }
	}
    }

    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
			  int gw, int gh, int wx, int wy){
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }

}

class BookmarkTableModel extends DefaultTableModel {

    public BookmarkTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    public boolean isCellEditable(int row,int column){
	return false;
    }

}
