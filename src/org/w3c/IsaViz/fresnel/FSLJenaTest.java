/*   FILE: FSLJenaTest.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLJenaTest.java,v 1.9 2006/06/03 19:45:56 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.io.*;
import java.util.Vector;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;

/**A series of tests for FSL engines implemened by IsaViz (Visual Debugger, Jena, Sesame)*/

public class FSLJenaTest extends TestCase implements RDFErrorHandler {
    
    protected static String MODEL_FILE_PATH = "tests/fsl-test-model.rdf";
    protected static String RDFXMLAB = "RDF/XML-ABBREV";

    protected FSLJenaEvaluator fje;
    protected Model jenaModel;
    protected FSLNSResolver nsr;
    protected FSLHierarchyStore fhs;

    public FSLJenaTest(String name){
	super(name);
    }
    
    /* init */
    protected void setUp(){
	/*initializing the namespace prefix mappings used in the tests*/
	nsr = new FSLNSResolver();
	nsr.addPrefixBinding("dc", "http://purl.org/dc/elements/1.1/");
	nsr.addPrefixBinding("xsd", "http://www.w3.org/2001/XMLSchema#");
	nsr.addPrefixBinding("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	nsr.addPrefixBinding("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	nsr.addPrefixBinding("foaf", "http://xmlns.com/foaf/0.1/");
	nsr.addPrefixBinding("pim", "http://www.w3.org/2000/10/swap/pim/contact#");
	nsr.addPrefixBinding("air", "http://www.daml.org/2001/10/html/airport-ont#");
	nsr.addPrefixBinding("ex", "http://example.org#");
	/*initializing the RDFS hierarchy*/
	fhs = new FSLJenaHierarchyStore();
	/*loading the RDF data from test file in the Jena model*/
	jenaModel = ModelFactory.createDefaultModel();
	RDFReader parser = jenaModel.getReader(RDFXMLAB);
	parser.setErrorHandler(this);
	try {
	    File f = new File(MODEL_FILE_PATH);
	    parser.read(jenaModel, new FileInputStream(f), f.toURL().toString());
	}
	catch (Exception ex){
	    System.err.println("Error during test setup:");
	    ex.printStackTrace();
	}
	fje = new FSLJenaEvaluator(nsr, fhs);
	fje.setModel(jenaModel);
    }

    /* tests */

    public void testNumberOfResources(){
	String path = "*";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void testNumberOfLiterals(){
	String path = "text()";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 39);
    }

    public void testNumberOfStatements(){
	String path = "*";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 80);
    }

    public void testNumberOfPersons(){
	String path = "foaf:Person";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 17);
    }

    public void testNumberOfFOAFknows(){
	String path = "foaf:knows";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 16);
    }

    public void testCount1(){
	String path = "foaf:Person[count(foaf:knows) > 5]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testCount2(){
	String path = "foaf:Person[count(foaf:knows) < 5]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 16);
    }

    public void testLocalName(){
	String path = "*[local-name(.) = \"mbox_sha1sum\"]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 18);
    }

    public void testNamespaceURI1(){
	String path = "*[namespace-uri(.) != 'http://xmlns.com/foaf/0.1/']";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 23);
    }

    public void testNamespaceURI2(){
	String path = "*[namespace-uri(.) != 'http://xmlns.com/foaf/0.1/' and namespace-uri(.) != 'http://www.w3.org/1999/02/22-rdf-syntax-ns#']";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 5);
    }

    public void testURI(){
	String path = "*[uri(.) = \"http://www.daml.org/cgi-bin/airport?CDG\"]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testExp(){
	String path = "*[uri(.) = exp('foaf:Person')]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testLiteralValue(){
	String path = "*[literal-value(foaf:name) = 'Emmanuel Pietriga']";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testStartsWith(){
	String path = "*[starts-with(foaf:name/text(), 'Eric')]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 2);
    }

    public void testNestedFunctionCalls(){
	String path = "*[starts-with(literal-value(foaf:name), 'Eric')]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 2);
    }

    public void testConcat(){
	String path = "*[foaf:name/text() = concat(\"Emmanuel\", \" Pietriga\")]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testSubstringBefore(){
	String path = "*[substring-before(foaf:name/text(), \" \") = \"Emmanuel\"]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testSubstringAfter(){
	String path = "*[substring-after(foaf:name/text(), \" \") = \"Pietriga\"]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testSubstring1(){
	String path = "*[substring(foaf:name/text(), 10) = \"Pietriga\"]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testSubstring2(){
	String path = "*[substring(foaf:name/text(), 10, 2) = \"Pi\"]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testStringLength(){
	String path = "*[string-length(foaf:name/text()) = 11]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 3);
    }

    public void testNumber1(){
	String path = "*[number(\"11\") = 11]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void testNumber2(){
	String path = "*[number(\"0.789\") = 0.789]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void testNumber3(){
	String path = "*[number(\"-100.789\") = -100.789]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void testBoolean(){
	String path = "*[boolean(foaf:name) or boolean(rdf:type)]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 18);
    }

    public void testTrueFalse(){
	String path = "*[true() != false()]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void test5stepPathEndingWithLiteral(){
	String path = "*/*/*/*/text()";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 34);
    }

    public void test5stepPathEndingWithResource(){
	String path = "*/*/*/*/*";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 17);
    }
    
    public void testArcEndingAtResource1(){
	String path = "*/*";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 41);
    }

    public void testArcEndingAtResource2(){
	String path = "*/in::*";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 41);
    }

    public void testArcEndingAtLiteral1(){
	String path = "*/text()";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 39);
    }

    public void testArcEndingAtLiteralWithLang(){
	String path = "dc:title/text()@fr";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testArcEndingAtLiteralWithDatatype(){
	String path = "ex:age/text()^^xsd:nonNegativeInteger";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testArcEndingAtSpecificLiteralWithLang(){
	String path = "dc:title/\"Institut National de Recherche en Informatique et en Automatique\"@fr";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testArcEndingAtSpecificLiteralWithDatatype(){
	String path = "ex:age/\"29\"^^xsd:nonNegativeInteger";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testArcEndingAtLiteral2(){
	String path = "text()/in::*";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 39);
    }

    public void testOneStepPathWithTwoPredicates(){
	String path = "foaf:Person[foaf:knows/foaf:Person and foaf:name/text() = \"Emmanuel Pietriga\"]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testComplexPath1(){
	String path = "foaf:Person[foaf:knows/foaf:Person and foaf:name/text() = \"Emmanuel Pietriga\"]/air:nearestAirport/air:Airport[air:iataCode/text() = 'CDG']";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testComplexPath2(){
	String path = "foaf:Person[foaf:knows/foaf:Person and foaf:name/text() = \"Emmanuel Pietriga\"]/air:nearestAirport/air:Airport[air:iataCode/text() = 'CDG']/airport:iataCode/text()";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testComplexPathWithManyInstances(){
	String path = "foaf:Person/foaf:knows/*/rdf:type/*[uri(.) = exp(\"foaf:Person\")]/in::rdf:type";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 272);
    }

    public void testEvenMoreComplexPathWithNotSoManyInstances(){
	String path = "foaf:Person/foaf:knows/*/rdf:type/*[uri(.) = exp(\"foaf:Person\")]/in::rdf:type/*/foaf:name[starts-with(text(), \"Eric\")]";
	Vector pathInstances = fje.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 32);
    }

    /*Jena's RDFErrorHandler methods*/
    public void error(java.lang.Exception ex){
	System.err.println("An error occured while parsing test model "+MODEL_FILE_PATH+": "+ex+"\n");
    }

    public void fatalError(java.lang.Exception ex){
	System.err.println("A fatal error occured while parsing test model "+MODEL_FILE_PATH+": "+ex+"\n");
    }

    public void warning(java.lang.Exception ex){
	System.err.println("Warning while parsing test model "+MODEL_FILE_PATH+": "+ex+"\n");
    }

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTestSuite(FSLJenaTest.class);
	return suite;
    }

}