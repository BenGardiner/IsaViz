/*   FILE: FSLJenaEvaluator.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLJenaEvaluator.java,v 1.35 2006/10/26 14:24:11 epietrig Exp $
 */ 


package org.w3c.IsaViz.fresnel;

import java.io.*;
import java.util.Vector;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;

/**<strong>Main class for evaluating an FSL expression on a Jena model (requires Jena 2.3)</strong>*/

public class FSLJenaEvaluator extends FSLEvaluator implements RDFErrorHandler {

    public static boolean DEBUG = false;

    static String RDFXMLAB = "RDF/XML-ABBREV";

    Model jenaModel;
    Property RDF_TYPE;

    /** Construct an FSL Path evaluator for expression path
     *@param nsr namespace prefix bindings used in the FSL expression (this has to be instantiated by the client)
     *@param fhs class / type hierarchy store for RDFS/OWL awareness (this has to be instantiated by the client)
     */
    public FSLJenaEvaluator(FSLNSResolver nsr, FSLHierarchyStore fhs){
	this.nsr = nsr;
	this.fhs = fhs;
    }

    /** Evaluate this FSL expression on an RDF/XML file
     *@param path the FSL path expression as a String
     *@param firstStepType one of {FSLPath.NODE_STEP, FSLPath.ARC_STEP} - specifies how the first location step should be interpreted (as a node step or arc step)
     *@param file the file's URL
     *@param printPaths should the result paths be printed on System.out or not
     *@return the set of paths in the model that instantiate the FSL expression
     */
    public Vector evaluate(String path, short firstStepType, String file, boolean printPaths){
	File f = new File(file);
	try {
	    parseRDF(new FileInputStream(f), f.toURL().toString());
	    Vector res = evaluatePath(FSLPath.pathFactory(path, this.nsr, firstStepType));
	    if (printPaths){
		printPaths(res);
	    }
	    return res;
	}
	catch (Exception ex){
	    System.err.println("FSLJenaEvaluator: Error: failed to load file " + file);
	    return new Vector();
	}
    }

    protected void parseRDF(FileInputStream fis, String baseURI){
	jenaModel = ModelFactory.createDefaultModel();
	RDFReader parser = jenaModel.getReader(RDFXMLAB);
	parser.setErrorHandler(this);
	parser.read(jenaModel, fis, baseURI);
	RDF_TYPE = jenaModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
    }

    /** Set the Jena model on which the path expression will be evaluated<br>
     * This method should be used when the model is created, initialized and populated somewhere else.<br>
     * When just evaluating the path expression on a file or URL, it is simpler to use methods evaluate(...)
     *@param m the model on which to evaluate the expression
     */
    public void setModel(Model m){
	jenaModel = m;
	RDF_TYPE = jenaModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
    }

    /** Get the Jena model on which the path expression is evaluated
     *@return the Jena model
     */
    public Model getModel(){
	return jenaModel;
    }

    /** Evaluate a path expression on the repository set by setRepository(Repository r).<br>
     * The initial set of nodes/arcs (i.e., the set of nodes or arcs that are going to be considered as potential starting points for matching paths) is:<br>1) all resource nodes in the graph if firstStepType = NODE_STEP and the first location step does not test for a literal node (i.e. it is not "text()");<br>2) all literal nodes in the graph if firstStepType = NODE_STEP and the first location step tests for a literal node (i.e. it is "text()");<br>3) all property arcs in the graph if firstStepType = ARC_STEP
     *@param p the FSL path expression (use FSLPath.pathFactory() to build it from its String representation)
     *@return a Vector containing Vectors that represent sequences of nodes/arcs that are instances of actual paths in the graph that match the FSL expression. These vectors are made of Jena com.hp.hpl.jena.rdf.model.Resource or com.hp.hpl.jena.rdf.model.Literal objects for node steps and com.hp.hpl.jena.rdf.model.Statement objects for arc steps. Naturally, one every two steps is a Resource or a Literal, the other being a Statement.
     *@see #evaluatePath(FSLPath p, Vector startSet)
     */
    public Vector evaluatePath(FSLPath p){
	if (DEBUG){
	    System.out.println("Evaluating FSL path expression  " + p.serialize());
	}
	Vector pathInstances = new Vector();
	StmtIterator si = jenaModel.listStatements();
	if (p.steps[0].type == FSLLocationStep.P_STEP){// step is an arc step
	    evaluateArcPath(p, si, pathInstances);
	}
	else {// step is a node step
	    Statement s;
	    Resource r;
	    Object o;
	    Vector startSet = new Vector();
	    if (p.steps[0].type == FSLLocationStep.L_STEP){
		while (si.hasNext()){
		    s = si.nextStatement();
		    o = s.getObject();
		    if (o instanceof Literal){startSet.add(o);}
		}
		evaluateNodePathL(p, startSet, pathInstances);
	    }
	    else {
		while (si.hasNext()){
		    s = si.nextStatement();
		    r = s.getSubject();
		    if (!startSet.contains(r)){startSet.add(r);}
		    o = s.getObject();
		    if (!(o instanceof Literal || startSet.contains(o))){startSet.add(o);}
		}
		evaluateNodePathR(p, startSet, pathInstances);
	    }
	}
	return pathInstances;
    }

    /** Evaluate a path expression on the repository set by setRepository(Repository r), only considering specific nodes/arcs in the graph as potential starting points for paths.
     *@param p the FSL path expression (use FSLPath.pathFactory() to build it from its String representation)
     *@param startSet The initial set of nodes/arcs (i.e., the set of nodes or arcs that are going to be considered as potential starting points for matching paths).<br>1) if firstStepType = NODE_STEP and the first location step does not test for a literal node (i.e. it is not "text()"), startSet should contain objects that implement org.openrdf.model.Resource;<br>2) if firstStepType = NODE_STEP and the first location step tests for a literal node (i.e. it is "text()"), startSet should contain objects that implement org.openrdf.model.Literal;<br>3) if firstStepType = ARC_STEP, startsSet should contain objects that implement org.openrdf.model.Statement
     *@return a Vector containing Vectors that represent sequences of nodes/arcs that are instances of actual paths in the graph that match the FSL expression. These vectors are made of Jena com.hp.hpl.jena.rdf.model.Resource or com.hp.hpl.jena.rdf.model.Literal objects for node steps and com.hp.hpl.jena.rdf.model.Statement objects for arc steps. Naturally, one every two steps is a Resource or a Literal, the other being a Statement.
     *@see #evaluatePath(FSLPath p)
     */
    public Vector evaluatePath(FSLPath p, Vector startSet){
	if (DEBUG){
	    System.out.println("Evaluating FSL path expression  " + p.serialize());
	}
	Vector pathInstances = new Vector();
	if (p.steps[0].type == FSLLocationStep.P_STEP){// step is an arc step
	    evaluateArcPath(p, startSet, pathInstances);
	}
	else {// step is a node step
	    if (p.steps[0].type == FSLLocationStep.L_STEP){
		evaluateNodePathL(p, startSet, pathInstances);
	    }
	    else {
		evaluateNodePathR(p, startSet, pathInstances);
	    }
	}
	return pathInstances;
    }

    protected void evaluateNodePathR(FSLPath p, Vector startSet, Vector pathInstances){
	Resource node;
	Vector selectedElements;
	for (int i=0;i<startSet.size();i++){
	    node = (Resource)startSet.elementAt(i);
	    selectedElements = new Vector();
	    selectResource(p, 0, node, selectedElements, pathInstances);
	}
    }

    protected void evaluateNodePathL(FSLPath p, Vector startSet, Vector pathInstances){
	Literal node;
	Vector selectedElements;
	for (int i=0;i<startSet.size();i++){
	    node = (Literal)startSet.elementAt(i);
	    selectedElements = new Vector();
	    selectLiteral(p, 0, node, selectedElements, pathInstances);
	}
    }

    protected void evaluateArcPath(FSLPath p, StmtIterator startSet, Vector pathInstances){
	Vector selectedElements;
	while (startSet.hasNext()){
	    selectedElements = new Vector();
	    selectProperty(p, 0, startSet.nextStatement(), selectedElements, pathInstances);
	}
    }

    protected void evaluateArcPath(FSLPath p, Vector startSet, Vector pathInstances){
	Vector selectedElements;
	for (int i=0;i<startSet.size();i++){
	    selectedElements = new Vector();
	    selectProperty(p, 0, (Statement)startSet.elementAt(i), selectedElements, pathInstances);
	}
    }

    protected void selectResource(FSLPath path, int stepIndex, Resource node,
				  Vector selectedElements, Vector pathInstances){
	if (testResource((FSLResourceStep)path.steps[stepIndex], node)){
	    Vector v = (Vector)selectedElements.clone();
	    v.add(node);
	    if (stepIndex + 1 == path.steps.length){
		pathInstances.add(v);
	    }
	    else {
		Statement[] properties = getPropertyArcs(node, path.steps[stepIndex+1].axis);
		for (int i=0;i<properties.length;i++){
		    selectProperty(path, stepIndex+1, properties[i], v, pathInstances);
		}
	    }
	}
    }

    protected void selectLiteral(FSLPath path, int stepIndex, Literal node,
				 Vector selectedElements, Vector pathInstances){
	if (testLiteral((FSLLiteralStep)path.steps[stepIndex], node)){
	    Vector v = (Vector)selectedElements.clone();
	    v.add(node);
	    if (stepIndex + 1 == path.steps.length){
		pathInstances.add(v);
	    }
	    else {
		Statement[] properties = getLPropertyArcs(node, path.steps[stepIndex+1].axis);
		for (int i=0;i<properties.length;i++){
		    selectProperty(path, stepIndex+1, properties[i], v, pathInstances);
		}
	    }
	}
    }

    protected void selectProperty(FSLPath path, int stepIndex, Statement arc,
				  Vector selectedElements, Vector pathInstances){
	if (testProperty((FSLArcStep)path.steps[stepIndex], arc)){
	    Vector v = (Vector)selectedElements.clone();
	    v.add(arc);
	    if (stepIndex + 1 == path.steps.length){
		pathInstances.add(v);
	    }
	    else {
		if (path.steps[stepIndex].axis == FSLLocationStep.AXIS_IN || path.steps[stepIndex+1].axis == FSLLocationStep.AXIS_IN){
		    selectResource(path, stepIndex+1, arc.getSubject(), v, pathInstances);
		}
		else {
		    if (path.steps[stepIndex+1].type == FSLLocationStep.R_STEP){
			if (arc.getObject() instanceof Resource){
			    selectResource(path, stepIndex+1, arc.getResource(), v, pathInstances);
			}
		    }
		    else {// FSLLiteralStep
			if (arc.getObject() instanceof Literal){
			    selectLiteral(path, stepIndex+1, arc.getLiteral(), v, pathInstances);
			}
		    }
		}
	    }
	}
    }

    protected Statement[] getPropertyArcs(Resource node, short axis){
	if (axis == FSLLocationStep.AXIS_OUT){
	    StmtIterator si = node.listProperties();
	    return getStatementArray(si);
	}
	else {// AXIS_IN
	    StmtIterator si = node.getModel().listStatements(null, null, node);  // do not use jenaModel.listStatements(null, null, node)
	    return getStatementArray(si);                                        // it does not work (I don't know why)
	}
    }

    protected Statement[] getLPropertyArcs(Literal node, short axis){
	if (axis == FSLLocationStep.AXIS_IN){
	    StmtIterator si = jenaModel.listStatements(null, null, node);
	    return getStatementArray(si);
	}
	else {// AXIS_OUT
	    System.err.println("Warning : selectLiteral: path models a literal with an outgoing arc: "+node.getValue());
	    return null;
	}
    }

    protected boolean testResource(FSLResourceStep step, Resource node){
	boolean typeTest = false;
	if (step.nsURI != null){
	    StmtIterator si = node.listProperties(RDF_TYPE);
	    if (step.localName != null){// rdf:type constraint = class must have this URI
		String uri = step.nsURI + step.localName;
		String typeURI;
		while (si.hasNext()){
		    typeURI = si.nextStatement().getResource().toString();
		    if (typeURI.equals(uri) || (step.isSubClassSubPropMatching() && fhs.isSubclassOf(typeURI, uri))){
			// test passes if resource's type is the one given in the type test or if it is a subclass of it
			typeTest = true;
			break;
		    }
		}
	    }
	    else {// rdf:type contraint = class must be in the given namespace
		while (si.hasNext()){
		    if (si.nextStatement().getResource().toString().startsWith(step.nsURI)){
			typeTest = true;
			break;
		    }
		}
	    }
	}
	else {
	    if (step.localName != null){// rdf:type constraint = class must have this local name
		StmtIterator si = node.listProperties(RDF_TYPE);
		while (si.hasNext()){
		    if (si.nextStatement().getResource().toString().endsWith(step.localName)){
			typeTest = true;
			break;
		    }
		}
	    }
	    else {
		typeTest = true;
	    }
	}
	if (typeTest){
	    return (step.predicates != null) ? testNodePredicates(step.predicates, node) : true;
	}
	else return false;
    }

    protected boolean testLiteral(FSLLiteralStep step, Literal node){
	boolean valueTest = false;
	if (step.literalText != null){
	    if (node.getLexicalForm().equals(step.literalText)){
		valueTest = true;
	    }
	}
	else {
	    valueTest = true;
	}
	if (valueTest){
	    if (step.datatypeURI != null){
		if (node.getDatatypeURI() != null &&
		    node.getDatatypeURI().equals(step.datatypeURI)){return true;}
		else {return false;}
	    }
	    else if (step.lang != null){
		if (node.getLanguage() != null &&
		    node.getLanguage().equals(step.lang)){return true;}
		else {return false;}
	    }
	    else return true;
	}
	else return false;
    }

    protected boolean testProperty(FSLArcStep step, Statement arc){
	boolean typeTest = false;
	if (step.nsURI != null){
	    if (step.localName != null){
		String uri = step.nsURI + step.localName;
		String typeURI = arc.getPredicate().toString();
		if (typeURI.equals(uri) || (step.isSubClassSubPropMatching() && fhs.isSubpropertyOf(typeURI, uri))){
		    // test passes if property is the one given in the type test or if it is one of its subproperties
		    typeTest = true;
		}
	    }
	    // rdf:type contraint = property must be in the given namespace
	    else if (arc.getPredicate().toString().startsWith(step.nsURI)){
		typeTest = true;
	    }
	}
	else {
	    if (step.localName != null){// rdf:type constraint = property must have this local name
		if (arc.getPredicate().toString().endsWith(step.localName)){
		    typeTest = true;
		}
	    }
	    else {
		typeTest = true;
	    }
	}
	if (typeTest){
	    return (step.predicates != null) ? testArcPredicates(step.predicates, arc) : true;
	}
	else return false;
    }

    protected boolean testNodePredicates(FSLExpression[] predicates, Resource node){
	for (int i=0;i<predicates.length;i++){
	    if (!evaluateBooleanExpr(predicates[i], node)){return false;}
	}
	return true;
    }

    protected boolean testArcPredicates(FSLExpression[] predicates, Statement arc){
	for (int i=0;i<predicates.length;i++){
	    if (!evaluateBooleanExpr(predicates[i], arc)){return false;}
	}
	return true;
    }

    public Vector evaluatePathExpr(FSLPath expr, Object nodeOrArc){// nodeOrArc is the context node/arc
	// against which the path expression should be evaluated
	Vector selectedElements = new Vector();
	Vector pathInstances = new Vector();
	int startStepIndex = 0;
	/*deal with the case of location step "." (node or arc makes no difference)*/
	if (expr.steps[0] instanceof FSLSelfNodeStep || expr.steps[0] instanceof FSLSelfArcStep){
	    selectedElements.add(nodeOrArc);
	    pathInstances.add(selectedElements);
	    if (expr.steps.length == 1){// path expression is just "."
		return pathInstances;
	    }
	    else {// path expression is of the form "./???/???/???/..."
		/*XXX: this isn't working, there is an issue with the following steps' types*/
		startStepIndex = 1;
	    }
	}
	if (nodeOrArc instanceof Resource){// the path should start with an arc location step
	    // path expr should be evaluated either on incoming or outgoing statements
	    // whether we evaluate it on incoming or outgoing arcs depends on the 1st location step's axis
	    Statement[] properties = getPropertyArcs((Resource)nodeOrArc, expr.steps[0].axis);
	    for (int i=0;i<properties.length;i++){
		selectProperty(expr, startStepIndex, properties[i], selectedElements, pathInstances);
	    }
	}
	else if (nodeOrArc instanceof Statement){// the path should start with a node location step
	    // path expr should be evaluated either on subject or object of statement
	    // depending on the 1st location step's axis
	    if (expr.steps[0].axis == FSLLocationStep.AXIS_OUT){
		Statement arc = (Statement)nodeOrArc;
		if (expr.steps[0].type == FSLLocationStep.R_STEP && arc.getObject() instanceof Resource){
		    selectResource(expr, startStepIndex, arc.getResource(), selectedElements, pathInstances);
		}
		else if (expr.steps[0].type == FSLLocationStep.L_STEP && arc.getObject() instanceof Literal){// Literal
		    selectLiteral(expr, startStepIndex, arc.getLiteral(), selectedElements, pathInstances);
		}
	    }
	    else {// AXIS_IN
		selectResource(expr, startStepIndex, ((Statement)nodeOrArc).getSubject(), selectedElements, pathInstances);
	    }
	}
	//the case of a literal having predicates is not supposed to happen
// 	else {// the path should start with an arc location step
// 	    selectLiteral(expr, 0, null, selectedElements, pathInstances);
// 	}
	return pathInstances;
    }

    public float[] getLiteralsAsNumbers(FSLPath expr, Object nodeOrArc){
	Vector pathInstances = evaluatePathExpr(expr, nodeOrArc);
	Vector floatValues = new Vector();
	Object o;
	for (int i=0;i<pathInstances.size();i++){
	    o = ((Vector)pathInstances.elementAt(i)).lastElement();
	    if (o instanceof Literal){
		try {//.trim() not necessary (leading and trailing whitespaces handled by parseFloat)
		    floatValues.add(new Float(((Literal)o).getLexicalForm()));
		}
		catch (NumberFormatException ex){}
	    }
	}
	float[] res = new float[floatValues.size()];
	for (int i=0;i<floatValues.size();i++){
	    res[i] = ((Float)floatValues.elementAt(i)).floatValue();
	}
	return res;
    }

    public String[] getLiteralsAsStrings(FSLPath expr, Object nodeOrArc){
	Vector pathInstances = evaluatePathExpr(expr, nodeOrArc);
	Vector stringValues = new Vector();
	Object o;
	for (int i=0;i<pathInstances.size();i++){
	    o = ((Vector)pathInstances.elementAt(i)).lastElement();
	    if (o instanceof Literal){
		stringValues.add(((Literal)o).getLexicalForm());
	    }
	}
	String[] res = new String[stringValues.size()];
	for (int i=0;i<stringValues.size();i++){
	    res[i] = (String)stringValues.elementAt(i);
	}
	return res;
    }

    public String getFirstLiteralAsString(FSLPath expr, Object nodeOrArc){
	Vector pathInstances = evaluatePathExpr(expr, nodeOrArc);
	Object o;
	for (int i=0;i<pathInstances.size();i++){
	    o = ((Vector)pathInstances.elementAt(i)).lastElement();
	    if (o instanceof Literal){
		return ((Literal)o).getLexicalForm();
	    }
	}
	return "";
    }

    public float getFirstLiteralAsNumber(FSLPath expr, Object nodeOrArc){
	Vector pathInstances = evaluatePathExpr(expr, nodeOrArc);
	Object o;
	for (int i=0;i<pathInstances.size();i++){
	    o = ((Vector)pathInstances.elementAt(i)).lastElement();
	    if (o instanceof Literal){
		try {
		    return Float.parseFloat(((Literal)o).getLexicalForm());
		}
		catch(NumberFormatException ex){}
	    }
	}
	return Float.NaN;
    }

    /*Function call implementations*/

    /*local-name*/
    public String fcLocalName(FSLPath expr, Object nodeOrArc){
	Vector paths = evaluatePathExpr(expr, nodeOrArc);
	if (paths.size() > 0){
	    Object o = ((Vector)paths.firstElement()).lastElement();
	    if (o instanceof Resource){
		return (((Resource)o).getLocalName() != null) ? ((Resource)o).getLocalName() : "";
	    }
	    else if (o instanceof Statement){
		return ((Statement)o).getPredicate().getLocalName();
	    }
	}
	return "";
    }

    /*namespace-uri*/
    public String fcNamespaceURI(FSLPath expr, Object nodeOrArc){
	Vector paths = evaluatePathExpr(expr, nodeOrArc);
	if (paths.size() > 0){
	    Object o = ((Vector)paths.firstElement()).lastElement();
	    if (o instanceof Resource){
		return (((Resource)o).getNameSpace() != null) ? ((Resource)o).getNameSpace() : "";
	    }
	    else if (o instanceof Statement){
		return ((Statement)o).getPredicate().getNameSpace();
	    }
	}
	return "";
    }

    /*uri*/
    public String fcURI(FSLPath expr, Object nodeOrArc){
	Vector paths = evaluatePathExpr(expr, nodeOrArc);
	if (paths.size() > 0){
	    Object o = ((Vector)paths.firstElement()).lastElement();
	    if (o instanceof Resource){
		return (((Resource)o).getURI() != null) ? ((Resource)o).getURI() : "";
	    }
	    else if (o instanceof Statement){
		return ((Statement)o).getPredicate().getURI();
	    }
	}
	return "";
    }

    /*literal-value*/
    public String fcLiteralValue(FSLPath expr, Object nodeOrArc){
	expr.appendLocationStep(new FSLLiteralStep());
	Vector pathInstances = evaluatePathExpr(expr, nodeOrArc);
	expr.removeLastLocationStep();
	if (pathInstances.size() > 0){
	    Object o = ((Vector)pathInstances.firstElement()).lastElement();
	    if (o instanceof Literal){
		return ((Literal)o).getLexicalForm();
	    }
	}
	return "";
    }

    /*literal-dt*/
    public String fcLiteralDT(FSLPath expr, Object nodeOrArc){
	expr.appendLocationStep(new FSLLiteralStep());
	Vector pathInstances = evaluatePathExpr(expr, nodeOrArc);
	expr.removeLastLocationStep();
	if (pathInstances.size() > 0){
	    Object o = ((Vector)pathInstances.firstElement()).lastElement();
	    if (o instanceof Literal){
		String dtURI = ((Literal)o).getDatatypeURI();
		return (dtURI != null) ? dtURI : "";
	    }
	}
	return "";
    }

    /*Misc. utility methods*/

    protected static Statement[] getStatementArray(StmtIterator si){
	ArrayList al = new ArrayList();
	while (si.hasNext()){
	    al.add(si.next());
	}
	return (Statement[])al.toArray(new Statement[al.size()]);
    }

    /*Jena's RDFErrorHandler methods*/
    public void error(java.lang.Exception ex){
	System.err.println("An error occured while parsing: "+ex+"\n");
    }

    public void fatalError(java.lang.Exception ex){
	System.err.println("A fatal error occured while parsing: "+ex+"\n");
    }

    public void warning(java.lang.Exception ex){
	System.err.println("Warning while parsing: "+ex+"\n");
    }

    /*Debug*/
    private void printPaths(Vector pathInstances){
	System.out.println("Found "+pathInstances.size()+" path(s)");
	for (int i=0;i<pathInstances.size();i++){
	    printPath((Vector)pathInstances.elementAt(i));
	}
    }

    public void printPath(Vector v){
	Object o;
	for (int i=0;i<v.size()-1;i++){
	    o = v.elementAt(i);
	    if (o instanceof Statement){
		System.out.print(((Statement)o).getPredicate().toString() + " / ");
	    }
	    else {
		System.out.print(o.toString() + " / ");
	    }
	}
	o = v.lastElement();
	if (o instanceof Statement){
	    System.out.println(((Statement)o).getPredicate().toString());
	}
	else {
	    System.out.println(o.toString());
	}
    }

    /*{0,1} <fsl_expr_in_a_file> <file_with_model_in_rdfxml>*/
    public static void main(String[] args){
	//System.out.println("Checking FSL path in " + args[1] + "\non file " + args[2]);
	StringBuffer bf = new StringBuffer();
	try {
	    FileReader fr = new FileReader(args[1]);
	    int c;
	    do {
		c = fr.read();
		bf.append((char)c);
	    } while(c != -1);
	    String path = bf.substring(0, bf.length()-2);
	    System.out.println("Input FSL path expression       " + path);
	    short firstStepType = Short.parseShort(args[0]);
	    System.out.println("First step : " + ((firstStepType == FSLPath.NODE_STEP) ? "node" : "arc"));
	    /*example of how to declare the namespace prefix bindings used in the FSL expression*/
	    FSLNSResolver n = new FSLNSResolver();
	    n.addPrefixBinding("dc", "http://purl.org/dc/elements/1.1/");
	    n.addPrefixBinding("xsd", "http://www.w3.org/2001/XMLSchema#");
	    n.addPrefixBinding("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	    n.addPrefixBinding("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	    n.addPrefixBinding("foaf", "http://xmlns.com/foaf/0.1/");
	    n.addPrefixBinding("pim", "http://www.w3.org/2000/10/swap/pim/contact#");
	    n.addPrefixBinding("ex", "http://example.org#");
	    n.addPrefixBinding("foo", "http://foo#");
	    n.addPrefixBinding("zvtm", "http://zvtm.sourceforge.net/");
	    n.addPrefixBinding("c0", "http://www.lri.fr/~pietriga/IsaViz/test/fsl_hie_c_test.rdfs#");
	    n.addPrefixBinding("p1", "http://www.lri.fr/~pietriga/IsaViz/test/fsl_hie_p_test.rdfs#");
	    n.addPrefixBinding("", "http://bob/"); //default NS
	    FSLHierarchyStore h = new FSLJenaHierarchyStore();
// 	    h.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl_hie_c_test.rdfs",
// 			  "file:///Users/epietrig/projects/WWW/2001/10/IsaViz/test/fsl_hie_c_test.rdfs");
// 	    h.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl_hie_p_test.rdfs",
// 			  "file:///Users/epietrig/projects/WWW/2001/10/IsaViz/test/fsl_hie_p_test.rdfs");
	    /*example of how to build the FSL expression evaluator and then evaluate it on a file that will be parsed by Jena*/
	    FSLJenaEvaluator fje = new FSLJenaEvaluator(n, h);
	    fje.evaluate(path, firstStepType, args[2], true);
	}
	catch (Exception ex){ex.printStackTrace();}
    }

}
