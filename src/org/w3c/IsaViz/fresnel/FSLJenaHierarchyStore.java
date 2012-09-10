/*   FILE: FSLJenaHierarchyStore.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLJenaHierarchyStore.java,v 1.2 2005/12/01 14:22:15 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;

/**Contains all class and property hierarchies (retrieved from available RDFS/OWL document)*/

public class FSLJenaHierarchyStore extends FSLHierarchyStore {

    Hashtable tmpHierarchy;

    public FSLJenaHierarchyStore(){
	super();
	tmpHierarchy = new Hashtable();
    }

    /** add the classes and properties of an ontology into the store
     *@param docURI the ontology's URI
     *@param locationURL an alternative (possibly local) URL where the ontology can be found (null if none); local URLs should be given with the file:// protocol
     */
    public void addOntology(String docURI, String locationURL){
	OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
	if (locationURL != null){
	    if (DEBUG){
		System.out.println("Retrieving ontology "+docURI);
	    }
	    m.getDocumentManager().addAltEntry(docURI, locationURL);
	}
        m.read(docURI);
	processOntology(m);
    }
    
    void processOntology(OntModel m){
	for (Iterator i=rootClasses(m);i.hasNext();){
            processClass((OntClass)i.next(), new ArrayList());
        }
	gatherAncestorClasses();
	for (Iterator i=rootProperties(m);i.hasNext();){
            processProperty((OntProperty)i.next(), new ArrayList());
        }
	gatherAncestorProperties();
    }

    void processClass(OntClass cls, List occurs){
	addClass(cls);
	// recurse to the next level down
        if (cls.canAs(OntClass.class) && !occurs.contains(cls)){
            for (Iterator i=cls.listSubClasses(true);i.hasNext();){
                OntClass sub = (OntClass)i.next();
                // we push this expression on the occurs list before we recurse
                occurs.add(cls);
                processClass(sub, occurs);
                occurs.remove(cls);
            }
        }
    }

    void addClass(OntClass c){
	if (!c.isRestriction() && !c.isAnon()){
	    String classURI = c.getURI();
	    String pURI;
	    Vector parentClasses = new Vector();
	    for (Iterator i=c.listSuperClasses(true);i.hasNext();){
		pURI = ((OntClass)i.next()).getURI();
		if (pURI != null && pURI.length() >0){
		    parentClasses.add(pURI);
		}
	    }
	    if (parentClasses.size() > 0){
		tmpHierarchy.put(classURI, parentClasses);
	    }
	}
    }

    protected Iterator rootClasses(OntModel m){
        List roots = new ArrayList();
        for (Iterator i=m.listClasses();i.hasNext();){
            OntClass c = (OntClass)i.next();
            // too confusing to list all the restrictions as root classes 
            if (c.isAnon()){
                continue;
            }
            if (c.hasSuperClass(m.getProfile().THING(), true)){
                // this class is directly descended from Thing
                roots.add(c);
            }
            else if (c.getCardinality(m.getProfile().SUB_CLASS_OF()) == 0){
                // this class has no super-classes (can occur if we're not using the reasoner)
                roots.add(c);
            }
        }
        return roots.iterator();
    }

    void processProperty(OntProperty prp, List occurs){
	addProperty(prp);
	// recurse to the next level down
        if (prp.canAs(OntProperty.class) && !occurs.contains(prp)){
            for (Iterator i=prp.listSubProperties(true);i.hasNext();){
                OntProperty sub = (OntProperty)i.next();
                // we push this expression on the occurs list before we recurse
                occurs.add(prp);
                processProperty(sub, occurs);
                occurs.remove(prp);
            }
        }
    }

    void addProperty(OntProperty p){
	if (!p.isAnon()){// properties with no URI reference wouldn't be of any use (though they cannot exist)
	    String propertyURI = p.getURI();
	    String pURI;
	    Vector parentProperties = new Vector();
	    for (Iterator i=p.listSuperProperties(true);i.hasNext();){
		pURI = ((OntProperty)i.next()).getURI();
		if (pURI != null && pURI.length() >0){
		    parentProperties.add(pURI);
		}
	    }
	    if (parentProperties.size() > 0){
		tmpHierarchy.put(propertyURI, parentProperties);
	    }
	}
    }

    protected Iterator rootProperties(OntModel m){
        List roots = new ArrayList();
        for (Iterator i=m.listOntProperties();i.hasNext();){
            OntProperty p = (OntProperty)i.next();
            if (p.isAnon()){
                continue;
            }
	    if (p.getCardinality(m.getProfile().SUB_PROPERTY_OF()) == 0){
                // this property has no super-properties
                roots.add(p);
            }
        }
        return roots.iterator();
    }

    void gatherAncestorClasses(){
	Vector v;
	Object classURI;
	Vector ancestors;
	for (Enumeration e=tmpHierarchy.keys();e.hasMoreElements();){
	    classURI = e.nextElement();
	    // for each class add all ancestors to the list of parents
	    v = (Vector)tmpHierarchy.get(classURI);
	    if (v != null){
		ancestors = new Vector();
		for (int i=0;i<v.size();i++){
		    populateAncestor(v.elementAt(i), ancestors, tmpHierarchy);
		}
		String[] res = new String[ancestors.size()];
		for (int i=0;i<res.length;i++){
		    res[i] = (String)ancestors.elementAt(i);
		}
		ancestors.removeAllElements();
		classHierarchy.put(classURI, res);
	    }
	}
	tmpHierarchy.clear();
    }

    void gatherAncestorProperties(){
	Vector v;
	Object propertyURI;
	Vector ancestors;
	for (Enumeration e=tmpHierarchy.keys();e.hasMoreElements();){
	    propertyURI = e.nextElement();
	    // for each property add all ancestors to the list of parents
	    v = (Vector)tmpHierarchy.get(propertyURI);
	    if (v != null){
		ancestors = new Vector();
		for (int i=0;i<v.size();i++){
		    populateAncestor(v.elementAt(i), ancestors, tmpHierarchy);
		}
		String[] res = new String[ancestors.size()];
		for (int i=0;i<res.length;i++){
		    res[i] = (String)ancestors.elementAt(i);
		}
		ancestors.removeAllElements();
		propertyHierarchy.put(propertyURI, res);
	    }
	}
	tmpHierarchy.clear();
    }

    void populateAncestor(Object uri, Vector ancestors, Hashtable hierarchy){
	if (!ancestors.contains(uri)){ancestors.add(uri);}
	Vector ownAncestors = (Vector)hierarchy.get(uri);
	if (ownAncestors != null){
	    for (int i=0;i<ownAncestors.size();i++){
		populateAncestor(ownAncestors.elementAt(i), ancestors, hierarchy);
	    }
	}
    }

    public static void main(String[] args){
	FSLJenaHierarchyStore fhs = new FSLJenaHierarchyStore();
	fhs.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs",
			"file:///Users/epietrig/projects/WWW/2001/10/IsaViz/tests/fsl-hierarchy-test-model.rdfs");
	fhs.printClassHierarchy();
	fhs.printPropertyHierarchy();
    }

}
