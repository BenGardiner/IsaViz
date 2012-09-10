/*   FILE: FSLParserTest.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLParserTest.java,v 1.5 2006/06/03 19:34:48 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import junit.framework.*;

/**A series of tests for FSL engines implemened by IsaViz (Visual Debugger, Jena, Sesame)*/

public class FSLParserTest extends TestCase {
    
    protected FSLNSResolver nsr;

    public FSLParserTest(String name){
	super(name);
    }
    
    protected void setUp(){
	/*initializing the namespace prefix mappings used in the tests*/
	nsr = new FSLNSResolver();
	nsr.addPrefixBinding("dc", "http://purl.org/dc/elements/1.1/");
	nsr.addPrefixBinding("xsd", "http://www.w3.org/2001/XMLSchema#");
	nsr.addPrefixBinding("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	nsr.addPrefixBinding("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	nsr.addPrefixBinding("foaf", "http://xmlns.com/foaf/0.1/");
	nsr.addPrefixBinding("pim", "http://www.w3.org/2000/10/swap/pim/contact#");
	nsr.addPrefixBinding("rss", "http://purl.org/rss/1.0/");
    }

    public void testSingleNodePath(){
	String path = "rdfs:Class";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSingleArcPath(){
	String path = "rdf:type";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSingleNodeWithPredicatesPath1(){
	String path = "foaf:Person[count(foaf:knows) > 5]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSingleNodeWithPredicatesPath2(){
	String path = "*[not(boolean(dc:subject))]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSingleArcWithPredicatesPath1(){
	String path = "dc:title[string-length(literal-value(.)) >= 50]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSingleArcWithPredicatesPath2(){
	String path = "*[string-length(normalize-space(literal-value(.))) <= 50]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSimplePath1(){
	String path = "*/rdf:type/foaf:Person";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSimplePath2(){
	String path = "foaf:Person[foaf:age/text() > 60]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testURIPath1(){
	String path = "*[uri(.) = exp('foo:bar')]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testURIPath2(){
	String path = "foaf:Person[uri(.) = 'http://example.org/people#john']";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSingleStarNodePath(){
	String path = "*";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSingleLiteralNodePath(){
	String path = "text()";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testAllStarNodePath(){
	String path = "*/*/*/*";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testAllStarArcPath(){
	String path = "*/*/*/*";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSimpleAndPredicate(){
	String path = "*/rdf:li/rss:item[rss:title and rss:description]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSimplePredicateNesting(){
	String path = "foaf:knows[*[count(foaf:knows) >= 4]]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testComplexPath1(){
	String path = "*[contains(literal-value(dc:title),'improving')]/dc:creator[in::*[dc:title]]/foaf:Person";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testSimplePathWithExplicitAxis(){
	String path = "in::foaf:knows/in::*/out::foaf:surname";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testComplexPathWithExplicitAxis(){
	String path = "*[contains(literal-value(dc:title),'improving')]/out::dc:creator[in::*[dc:title]]/out::foaf:Person";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testComplexPath2(){
	String path = "foaf:Person[foaf:knows/foaf:Person[literal-value(foaf:surname) = 'Smith']]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testComplexPath3(){
	String path = "foaf:Person[foaf:knows/foaf:Person[foaf:surname/text() = 'Smith']]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testLiteralWithLang1(){
	String path = "*/*/text()@fr";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testLiteralWithLang2(){
	String path = "*/*/\"Hello World !\"@en";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testLiteralWithDatatype1(){
	String path = "*/*/text()^^xsd:int";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testLiteralWithDatatype2(){
	String path = "*/*/\"Hello World !\"^^xsd:string";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public void testRDFSOWLAwarePath(){
	String path = "^foaf:Person[^foaf:knows/^foaf:Person[^foaf:surname/text() = 'Smith']]";
	FSLPath p = FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP);
	assertTrue(path.equals(p.serialize()));
    }

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTestSuite(FSLParserTest.class);
	return suite;
    }

}
