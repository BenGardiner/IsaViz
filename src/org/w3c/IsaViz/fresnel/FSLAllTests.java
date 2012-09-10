/*   FILE: FSLAllTests.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLAllTests.java,v 1.2 2005/11/23 12:52:48 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import junit.framework.*;

public class FSLAllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(FSLParserTest.suite());
	suite.addTest(FSLJenaTest.suite());
	suite.addTest(FSLSesameTest.suite());
	suite.addTest(FSLHierarchyTest.suite());
	return suite;
    }
    
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}
      