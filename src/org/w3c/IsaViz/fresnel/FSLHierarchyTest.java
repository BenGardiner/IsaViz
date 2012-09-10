/*   FILE: FSLHierarchyTest.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLHierarchyTest.java,v 1.1 2005/11/23 12:52:48 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.io.*;
import java.util.Vector;

import junit.framework.*;

/**A series of tests for FSL engines implemened by IsaViz (Visual Debugger, Jena, Sesame)*/

public class FSLHierarchyTest extends TestCase {
    
    protected FSLHierarchyStore fjhs;
    protected FSLHierarchyStore fshs;

    public FSLHierarchyTest(String name){
	super(name);
    }
    
    /* init */
    protected void setUp(){
	fjhs = new FSLJenaHierarchyStore();
	fshs = new FSLSesameHierarchyStore();
	fshs.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs",
			 "tests/fsl-hierarchy-test-model.rdfs");
	fjhs.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs",
			 "file:///Users/epietrig/projects/WWW/2001/10/IsaViz/tests/fsl-hierarchy-test-model.rdfs");
    }

    /* tests */

    public void testSesameClassHierarchy1(){
	assertTrue(fshs.isSubclassOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassD",
				     "http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassC"));
    }

    public void testSesameClassHierarchy2(){
	assertTrue(fshs.isSubclassOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassD",
				     "http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testSesameClassHierarchy3(){
	assertTrue(fshs.isSubclassOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassC",
				     "http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testSesameClassHierarchy4(){
	assertTrue(fshs.isSubclassOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassB",
				     "http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testJenaClassHierarchy1(){
	assertTrue(fjhs.isSubclassOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassD",
				     "http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassC"));
    }

    public void testJenaClassHierarchy2(){
	assertTrue(fjhs.isSubclassOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassD",
				     "http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testJenaClassHierarchy3(){
	assertTrue(fjhs.isSubclassOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassC",
				     "http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testJenaClassHierarchy4(){
	assertTrue(fjhs.isSubclassOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassB",
				     "http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testSesamePropHierarchy1(){
	assertTrue(fshs.isSubpropertyOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropD",
					"http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropC"));
    }

    public void testSesamePropHierarchy2(){
	assertTrue(fshs.isSubpropertyOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropD",
					"http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public void testSesamePropHierarchy3(){
	assertTrue(fshs.isSubpropertyOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropC",
					"http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public void testSesamePropHierarchy4(){
	assertTrue(fshs.isSubpropertyOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropB",
					"http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public void testJenaPropHierarchy1(){
	assertTrue(fjhs.isSubpropertyOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropD",
					"http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropC"));
    }

    public void testJenaPropHierarchy2(){
	assertTrue(fjhs.isSubpropertyOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropD",
					"http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public void testJenaPropHierarchy3(){
	assertTrue(fjhs.isSubpropertyOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropC",
					"http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public void testJenaPropHierarchy4(){
	assertTrue(fjhs.isSubpropertyOf("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropB",
					"http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTestSuite(FSLHierarchyTest.class);
	return suite;
    }

}