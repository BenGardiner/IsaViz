/*   FILE: FSLSesameHierarchyStore.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLSesameHierarchyStore.java,v 1.5 2006/04/25 06:39:09 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.net.URL;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ParseException;
// import org.openrdf.model.Literal;
// import org.openrdf.model.Statement;
// import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
// import org.openrdf.sesame.repository.ResourceVertex;
// import org.openrdf.sesame.repository.URIVertex;
// import org.openrdf.sesame.repository.ValueVertex;
import org.openrdf.sesame.repository.Repository;
import org.openrdf.sesame.sailimpl.memory.MemoryStore;
import org.openrdf.sesame.sail.SailInitializationException;
import org.openrdf.model.vocabulary.RDF;

import org.openrdf.sesame.query.QueryLanguage;
// import org.openrdf.sesame.query.Tuple;
import org.openrdf.sesame.query.TupleSet;
import org.openrdf.sesame.query.TupleHandler;
import org.openrdf.sesame.query.TupleHandlerException;
import org.openrdf.sesame.query.MalformedQueryException;

/**Contains all class and property hierarchies (retrieved from available RDFS/OWL document)*/

public class FSLSesameHierarchyStore extends FSLHierarchyStore implements TupleHandler {

    Hashtable tmpHierarchy;

    public FSLSesameHierarchyStore(){
	super();
	tmpHierarchy = new Hashtable();
    }

    /** add the classes and properties of an ontology into the store
     *@param docURI the ontology's URI
     *@param locationPath an alternative local path where the ontology can be found (null if none); cam be given as a relative path
     */
    public void addOntology(String docURI, String locationPath){
	Repository r = new Repository(new MemoryStore());
	try {
	    r.initialize();
	    if (DEBUG){
		System.out.println("Retrieving ontology "+docURI);
	    }
	    try {
		r.add(new URL(docURI), docURI, RDFFormat.RDFXML);
	    }
	    catch (Exception ex1){
		System.err.println("FSLSesameEvaluator: Error: Failed to load RDF data from " + docURI);
		if (locationPath != null){
		    try {
			r.add(new File(locationPath), locationPath, RDFFormat.RDFXML);
		    }
		    catch (Exception ex2){
			System.err.println("FSLSesameEvaluator: Error: Failed to load RDF data from " + locationPath);
			if (DEBUG){ex2.printStackTrace();}
		    }
		}
	    }
	    processOntology(r);
	}
	catch (SailInitializationException ex0){
	    System.err.println("FSLSesameHierarchyStore: Error while initializing Sesame repository:");
	    if (DEBUG){ex0.printStackTrace();}
	}
    }
    
    void processOntology(Repository r){
	String query = "SELECT * FROM {x} <http://www.w3.org/2000/01/rdf-schema#subClassOf> {y}";
	try {
	    r.evaluateTupleQuery(QueryLanguage.SERQL,
				 query,
				 this);
	    processHierarchy(classHierarchy);
	}
	catch (MalformedQueryException ex){
	    System.err.println("Error: malformed SeRQL query \n" + query);
	    if (DEBUG){ex.printStackTrace();}
	}
	catch (TupleHandlerException ex){
	    System.err.println("Error handling result tuple \n during query " + query);
	    if (DEBUG){ex.printStackTrace();}
	}
	query = "SELECT * FROM {x} <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> {y}";
	try {
	    r.evaluateTupleQuery(QueryLanguage.SERQL,
				 query,
				 this);
	    processHierarchy(propertyHierarchy);
	}
	catch (MalformedQueryException ex){
	    System.err.println("Error: malformed SeRQL query \n" + query);
	    if (DEBUG){ex.printStackTrace();}
	}
	catch (TupleHandlerException ex){
	    System.err.println("Error handling result tuple \n during query " + query);
	    if (DEBUG){ex.printStackTrace();}
	}
    }

    void processHierarchy(Hashtable hierarchy){
	Object uri;
	Vector ancestors;
	for (Enumeration e=tmpHierarchy.keys();e.hasMoreElements();){
	    uri = e.nextElement();
	    ancestors = new Vector();
	    getAncestors(uri, ancestors);
	    if (ancestors.size() > 0){
		String[] ancestorList = new String[ancestors.size()];
		for (int i=0;i<ancestorList.length;i++){
		    ancestorList[i] = (String)ancestors.elementAt(i);
		}
		hierarchy.put(uri, ancestorList);
	    }
	}
	tmpHierarchy.clear();
    }

    void getAncestors(Object uri, Vector ancestors){
	Vector ancestorsToBeProcessed = (Vector)tmpHierarchy.get(uri);
	if (ancestorsToBeProcessed != null){
	    Object ancestorURI;
	    for (int i=0;i<ancestorsToBeProcessed.size();i++){
		ancestorURI = ancestorsToBeProcessed.elementAt(i);
		if (!ancestors.contains(ancestorURI)){
		    ancestors.add(ancestorURI);
		    getAncestors(ancestorURI, ancestors);
		}
	    }
	}
    }

    /* methods of TupleHandler */

    public void startTupleSet(String[] columnHeaders){}
    
    public void handleTuple(List<? extends Value> tuple){
	Value aSubType = tuple.get(0);
	Value aType = tuple.get(1);
	if (aSubType instanceof URI && aType instanceof URI){
	    String classURI = aType.toString();
	    String subTypeURI = aSubType.toString();
	    Vector v;
	    if (tmpHierarchy.containsKey(subTypeURI)){
		v = (Vector)tmpHierarchy.get(subTypeURI);
		if (!v.contains(classURI)){v.add(classURI);}
	    }
	    else {
		v = new Vector();
		v.add(classURI);
		tmpHierarchy.put(subTypeURI, v);
	    }
	}
    }

    public void endTupleSet(){}

    /* main */

    public static void main(String[] args){
	FSLSesameHierarchyStore fhs = new FSLSesameHierarchyStore();
	fhs.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs",
			"tests/fsl-hierarchy-test-model.rdfs");
// 	fhs.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl_hie_p_test.rdfs",
// 			"file:///Users/epietrig/projects/WWW/2001/10/IsaViz/test/fsl_hie_p_test.rdfs");
	fhs.printClassHierarchy();
	fhs.printPropertyHierarchy();
    }

}
