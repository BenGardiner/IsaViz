/*   FILE: CustomOrdering.java
 *   DATE OF CREATION:   Fri May 30 11:25:29 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jun 11 14:47:42 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */ 

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

import java.util.Vector;

/*store the ordering of properties for sorting */

public class CustomOrdering {

    String sortID;

    Vector items;

    /*Constructor*/
    public CustomOrdering(String id){
	sortID=id;
	items=new Vector();
    }

    void addItem(String property,String rdfLi){
	//rdfLi is of the form rdf:_X where X is a positive integer
	try {
	    //extract position X in the sequence from the rdfLi URI
	    int index=Integer.parseInt(rdfLi.substring(rdfLi.lastIndexOf("_")+1));
	    if (index>=0 && !Utils.containsString(items,property)){
		if (index>=items.size()){
		    items.setSize(index+1);
		}
		items.setElementAt(property,index);
	    }
	    else {if (GraphStylesheet.DEBUG_GSS){GraphStylesheet.debugBuffer1.append("Error:CustomOrdering: "+sortID+" either index "+rdfLi+" or property "+property+" has already been declared in custom ordering "+sortID+"\n");}}
	}
	//catch any exception like an arrayindexoutofbounds or an illegalnumberformat as we want ot be robust here (error-prone)
	catch (Exception ex){if (GraphStylesheet.DEBUG_GSS){GraphStylesheet.debugBuffer1.append("Error:CustomOrdering: "+sortID+" declares an illegal item or position number : "+property+" at index "+rdfLi+"\n");}ex.printStackTrace();}
    }

    /*items might have one or more null elements if the rdf:_X sequence in the input is skipping/omitting one or more index (e.g. going from _2 to _4 without any _3). We still want to build the order, so we provided for that in addItem() ; the following method is called to tidy things a bit (remove all null elements from items and make its size equal to what is required, not more)*/
    void buildFinalSequence(){
	Vector v=new Vector();
	for (int i=0;i<items.size();i++){
	    if (items.elementAt(i)!=null){
		v.addElement(items.elementAt(i));
	    }
	}
	items.removeAllElements();
	items=v;
    }

    Vector getEnumeration(){
	return items;
    }

//     String getItemAtIndex(int i){
// 	if (i<items.size()){
// 	    return (String)items.elementAt(i);
// 	}
// 	else return null;
//     }

    public String toString(){
	String res="Enumeration ID"+sortID+"\n";
	for (int i=0;i<items.size();i++){
	   res+="\t"+(i+1)+". "+(String)items.elementAt(i)+"\n";
	}
	return res;
    }

}
