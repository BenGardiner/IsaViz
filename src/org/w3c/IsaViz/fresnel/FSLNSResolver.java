/*   FILE: FSLNSResolver.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLNSResolver.java,v 1.3 2006/05/11 15:31:41 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.util.Hashtable;

/**FSL Namespace Prefix Binding table*/

public class FSLNSResolver {

    Hashtable PREFIX2NS;
    Hashtable NS2PREFIX;

    public FSLNSResolver(){
	PREFIX2NS = new Hashtable();
	NS2PREFIX = new Hashtable();
    }
    
    /**
     * declare a new prefix binding
     *@param prefix the prefix to bind to the namespace URI
     *@param nsURI the namespace URI
     */
    public void addPrefixBinding(String prefix, String nsURI){
	PREFIX2NS.put(prefix, nsURI);
	NS2PREFIX.put(nsURI, prefix);
    }

    /**
     * remove an existing prefix binding
     *@param prefix which prefix binding to remove
     */
    public void removePrefixBinding(String prefix){
	String nsURI = (String)PREFIX2NS.get(prefix);
	PREFIX2NS.remove(prefix);
	NS2PREFIX.remove(nsURI);
    }

    /**
     * get the namespace URI associated with a prefix
     */
    public String getNamespaceURI(String prefix){
	if (prefix != null){
	    return (String)PREFIX2NS.get(prefix);
	}
	else {
	    return null;
	}
    }

    /**
     * get the prefix associated with a namespace URI
     */
    public String getPrefix(String nsURI){
	if (nsURI != null){
	    return (String)NS2PREFIX.get(nsURI);
	}
	else {
	    System.err.println("FSLNSResolver: Error: no namespace URI found for "+nsURI);
	    return null;
	}
    }

    /**
     * clear all prefix bindings
     */
    public void clear(){
	PREFIX2NS.clear();
	NS2PREFIX.clear();
    }

}
