/*   FILE: FSLHierarchyStore.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLHierarchyStore.java,v 1.8 2005/12/07 08:09:49 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;

/**Contains all class and property hierarchies (retrieved from available RDFS/OWL document)*/

public class FSLHierarchyStore {

    public static boolean DEBUG = false;

    /**maps class URIs to a String[] containing all ancestor class URIs (direct or not)*/
    public Hashtable classHierarchy;
    /**maps property URIs to a String[] containing all ancestor property URIs (direct or not)*/
    public Hashtable propertyHierarchy;

    public FSLHierarchyStore(){
	classHierarchy = new Hashtable();
	propertyHierarchy = new Hashtable();
    }

    /** Does nothing, must be overridden by actual hierarchy store implementations */
    public void addOntology(String docURI, String locationURL){}
    
    /**returns true if uri1 is a subclass of uri2*/
    public boolean isSubclassOf(String uri1, String uri2){
	String[] ancestors = (String[])classHierarchy.get(uri1);
	if (ancestors != null){
	    for (int i=0;i<ancestors.length;i++){
		if (ancestors[i].equals(uri2)){return true;}
	    }
	    return false;
	}
	else {
	    return false;
	}
    }

    /**returns true if uri1 is a subproperty of uri2*/
    public boolean isSubpropertyOf(String uri1, String uri2){
	String[] ancestors = (String[])propertyHierarchy.get(uri1);
	if (ancestors != null){
	    for (int i=0;i<ancestors.length;i++){
		if (ancestors[i].equals(uri2)){return true;}
	    }
	    return false;
	}
	else {
	    return false;
	}	
    }

    /*debug*/
    public void printClassHierarchy(){
	String classURI;
	String[] parents;
	for (Enumeration e=classHierarchy.keys();e.hasMoreElements();){
	    classURI = (String)e.nextElement();
	    parents = (String[])classHierarchy.get(classURI);
	    System.out.println("----------------------\nSuperclasses of class "+classURI);
	    for (int i=0;i<parents.length;i++){
		System.out.println("    "+parents[i]);
	    }
	}
    }

    /*debug*/
    public void printPropertyHierarchy(){
	String propertyURI;
	String[] parents;
	for (Enumeration e=propertyHierarchy.keys();e.hasMoreElements();){
	    propertyURI = (String)e.nextElement();
	    parents = (String[])propertyHierarchy.get(propertyURI);
	    System.out.println("----------------------\nSuperproperties of property "+propertyURI);
	    for (int i=0;i<parents.length;i++){
		System.out.println("    "+parents[i]);
	    }
	}
    }

}
