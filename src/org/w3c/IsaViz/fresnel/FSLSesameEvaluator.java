/*   FILE: FSLSesameEvaluator.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLSesameEvaluator.java,v 1.9 2006/10/26 14:24:11 epietrig Exp $
 */ 


package org.w3c.IsaViz.fresnel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ParseException;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.sesame.repository.RResource;
import org.openrdf.sesame.repository.RURI;
import org.openrdf.sesame.repository.RValue;
import org.openrdf.sesame.repository.Repository;
import org.openrdf.sesame.sailimpl.memory.MemoryStore;
import org.openrdf.sesame.sail.SailInitializationException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.util.iterator.CloseableIterator;

/**<strong>Main class for evaluating an FSL expression on a Sesame repository (requires Sesame 2-alpha3)</strong>*/

public class FSLSesameEvaluator extends FSLEvaluator {

    private static boolean DEBUG = false;

    Repository sesameRepository;

    /** Construct an FSL Path evaluator for expression path
     *@param nsr namespace prefix bindings used in the FSL expression (this has to be instantiated by the client)
     *@param fhs class / type hierarchy store for RDFS/OWL awareness (this has to be instantiated by the client)
     */
    public FSLSesameEvaluator(FSLNSResolver nsr, FSLHierarchyStore fhs){
	this.nsr = nsr;
	this.fhs = fhs;
    }

    /** Evaluate this FSL expression on an RDF/XML file
     *@param path the FSL path expression as a String
     *@param firstStepType one of {FSLPath.NODE_STEP, FSLPath.ARC_STEP} - specifies how the first location step should be interpreted (as a node step or arc step)
     *@param loc the file's URL (either a local file or an http URL)
     *@param printPaths should the result paths be printed on System.out or not
     *@return the set of paths in the model that instantiate the FSL expression
     */
    public Vector evaluate(String path, short firstStepType, String loc, boolean printPaths){
	if (loc.startsWith("http://")){
	    try {
		return evaluate(FSLPath.pathFactory(path, this.nsr, firstStepType), new URL(loc), printPaths);
	    }
	    catch (MalformedURLException ex){
		System.err.println("FSLSesameEvaluator: ill-formed URL "+loc);
		return new Vector();
	    }
	}
	else {
	    return evaluate(FSLPath.pathFactory(path, this.nsr, firstStepType), new File(loc), printPaths);
	}
    }

    /** Evaluate this FSL expression on an RDF/XML file 
     *@param p the FSL path expression (use FSLPath.pathFactory() to build it from its String representation)
     *@param file the file's URL
     *@param printPaths should the result paths be printed on System.out or not
     *@return the set of paths in the model that instantiate the FSL expression
     */
    public Vector evaluate(FSLPath p, File file, boolean printPaths){
	initRepository();
	try {
	    storeRDFInRepository(file, file.toURL().toString());
	    Vector res = evaluatePath(p);
	    if (printPaths){
		printPaths(res);
	    }
	    return res;
	}
	catch (Exception ex){
	    System.err.println("FSLSesameEvaluator: Error: failed to load file " + file.toString());
	    if (DEBUG){ex.printStackTrace();}
	    return new Vector();
	}
    }

    /** Evaluate this FSL expression on an RDF/XML file.
     *@param p the FSL path expression (use FSLPath.pathFactory() to build it from its String representation)
     *@param url the file's URL
     *@param printPaths should the result paths be printed on System.out or not
     *@return the set of paths in the model that instantiate the FSL expression
     */
    public Vector evaluate(FSLPath p, URL url, boolean printPaths){
	initRepository();
	try {
	    storeRDFInRepository(url);
	    Vector res = evaluatePath(p);
	    if (printPaths){
		printPaths(res);
	    }
	    return res;
	}
	catch (Exception ex){
	    System.err.println("FSLSesameEvaluator: Error: failed to load file " + url.toString());
	    if (DEBUG){ex.printStackTrace();}
	    return new Vector();
	}
    }

    /** Set the Sesame repository on which the path expression will be evaluated<br>
     * This method should be used when the repository is created, initialized and populated somewhere else.<br>
     * When just evaluating the path expression on a file or URL, it is simpler to use methods evaluate(...)
     *@param r the repository on which to evaluate the expression
     */
    public void setRepository(Repository r){
	sesameRepository = r;
    }

    /** Get the Sesame repository on which the path expression is evaluated
     *@return the Sesame repository 
     */
    public Repository getRepository(){
	return sesameRepository;
    }

    void initRepository(){
	sesameRepository = new Repository(new MemoryStore());
	try {
	    sesameRepository.initialize();
	}
	catch (SailInitializationException ex){
	    System.err.println("Error while initializing Sesame repository:");
	    if (DEBUG){ex.printStackTrace();}
	}
    }

    void storeRDFInRepository(File rdfFile, String baseURI){
	try {
 	    sesameRepository.add(rdfFile, baseURI, RDFFormat.RDFXML);
	}
	catch (Exception ex){
	    System.err.println("FSLSesameEvaluator: Error: Failed to load RDF data from " + rdfFile.toString());
	    if (DEBUG){ex.printStackTrace();}
	}
    }

    void storeRDFInRepository(URL rdfURL){
	try {
	    sesameRepository.add(rdfURL, rdfURL.toString(), RDFFormat.RDFXML);
	}
	catch (Exception ex){
	    System.err.println("FSLSesameEvaluator: Error: Failed to load RDF data from " + rdfURL.toString());
	    if (DEBUG){ex.printStackTrace();}
	}
    }

    /** Evaluate a path expression on the repository set by setRepository(Repository r).<br>
     * The initial set of nodes/arcs (i.e., the set of nodes or arcs that are going to be considered as potential starting points for matching paths) is:<br>1) all resource nodes in the graph if firstStepType = NODE_STEP and the first location step does not test for a literal node (i.e. it is not "text()");<br>2) all literal nodes in the graph if firstStepType = NODE_STEP and the first location step tests for a literal node (i.e. it is "text()");<br>3) all property arcs in the graph if firstStepType = ARC_STEP
     *@param p the FSL path expression (use FSLPath.pathFactory() to build it from its String representation)
     *@return a Vector containing Vectors that represent sequences of nodes/arcs that are instances of actual paths in the graph that match the FSL expression. These vectors are made of Sesame org.openrdf.model.Resource or org.openrdf.model.Literal objects for node steps and org.openrdf.model.Statement objects for arc steps. Naturally, one every two steps is a Resource or a Literal, the other being a Statement.
     *@see #evaluatePath(FSLPath p, Vector startSet)
     */
    public Vector evaluatePath(FSLPath p){
	if (DEBUG){
	    System.out.println("Evaluating FSL path expression  " + p.serialize());
	}
	Vector pathInstances = new Vector();
	CloseableIterator si = sesameRepository.extractStatements();
	if (p.steps[0].type == FSLLocationStep.P_STEP){// step is an arc step
	    evaluateArcPath(p, si, pathInstances);
	}
	else {// step is a node step
	    Statement s;
 	    Resource r;
 	    Value o;
	    Vector startSet = new Vector();
	    if (p.steps[0].type == FSLLocationStep.L_STEP){
		while (si.hasNext()){
		    s = (Statement)si.next();
		    o = s.getObject();
		    if (o instanceof Literal){startSet.add(o);}
		}
		evaluateNodePathL(p, startSet, pathInstances);
	    }
	    else {
		while (si.hasNext()){
		    s = (Statement)si.next();
		    r = s.getSubject();
		    if (!startSet.contains(r)){startSet.add(r);}
		    o = s.getObject();
		    if (!(o instanceof Literal || startSet.contains(o))){startSet.add(o);}
		}
		evaluateNodePathR(p, startSet, pathInstances);		
	    }
	}
	si.close();
	return pathInstances;
    }

    /** Evaluate a path expression on the repository set by setRepository(Repository r), only considering specific nodes/arcs in the graph as potential starting points for paths.
     *@param p the FSL path expression (use FSLPath.pathFactory() to build it from its String representation)
     *@param startSet The initial set of nodes/arcs (i.e., the set of nodes or arcs that are going to be considered as potential starting points for matching paths).<br>1) if firstStepType = NODE_STEP and the first location step does not test for a literal node (i.e. it is not "text()"), startSet should contain objects that implement org.openrdf.model.Resource;<br>2) if firstStepType = NODE_STEP and the first location step tests for a literal node (i.e. it is "text()"), startSet should contain objects that implement org.openrdf.model.Literal;<br>3) if firstStepType = ARC_STEP, startsSet should contain objects that implement org.openrdf.model.Statement
     *@return a Vector containing Vectors that represent sequences of nodes/arcs that are instances of actual paths in the graph that match the FSL expression. These vectors are made of Sesame org.openrdf.model.Resource or org.openrdf.model.Literal objects for node steps and org.openrdf.model.Statement objects for arc steps. Naturally, one every two steps is a Resource or a Literal, the other being a Statement.
     *@see #evaluatePath(FSLPath p)
     */
    public Vector evaluatePath(FSLPath p, Vector startSet){
	if (DEBUG){
	    System.out.println("Evaluating FSL path expression  " + p.serialize());
	}
	Vector pathInstances = new Vector();
	if (p.steps[0].type == FSLLocationStep.P_STEP){// step is an arc step
	    evaluateArcPath(p, startSet.iterator(), pathInstances);
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

    protected void evaluateArcPath(FSLPath p, Iterator startSet, Vector pathInstances){
	Vector selectedElements;
	while (startSet.hasNext()){
	    selectedElements = new Vector();
	    selectProperty(p, 0, (Statement)startSet.next(), selectedElements, pathInstances);
	}
    }

    protected void selectResource(FSLPath path, int stepIndex, Resource node,
				  Vector selectedElements, Vector pathInstances){
	if (testResource((FSLResourceStep)path.steps[stepIndex], (RResource)node)){
	    Vector v = (Vector)selectedElements.clone();
	    v.add(node);
	    if (stepIndex + 1 == path.steps.length){
		pathInstances.add(v);
	    }
	    else {
		Statement[] properties = getPropertyArcs((RResource)node, path.steps[stepIndex+1].axis);
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
		Statement[] properties = getLPropertyArcs((RValue)node, path.steps[stepIndex+1].axis);
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
		else {// AXIS_IN (and necessarily an FSLResourceStep)
		    if (path.steps[stepIndex+1].type == FSLLocationStep.R_STEP){
			if (arc.getObject() instanceof Resource){
			    selectResource(path, stepIndex+1, (Resource)arc.getObject(), v, pathInstances);
			}
		    }
		    else {// FSLLiteralStep
			if (arc.getObject() instanceof Literal){
			    selectLiteral(path, stepIndex+1, (Literal)arc.getObject(), v, pathInstances);
			}
		    }
		}
	    }
	}
    }

    protected Statement[] getPropertyArcs(RResource node, short axis){
	if (axis == FSLLocationStep.AXIS_OUT){
	    return getStatementArray(node.getSubjectStatements());
	}
	else {// AXIS_IN
	    return getStatementArray(node.getObjectStatements());
	}
    }

    protected Statement[] getLPropertyArcs(RValue node, short axis){
	if (axis == FSLLocationStep.AXIS_IN){
	    return getStatementArray(node.getObjectStatements());
	}
	else {// AXIS_IN
	    System.err.println("Warning : selectLiteral: path models a literal with an outgoing arc: " + node.toString());
	    return null;
	}
    }

    protected boolean testResource(FSLResourceStep step, RResource node){
	boolean typeTest = false;
	if (step.nsURI != null){
	    CloseableIterator si = sesameRepository.getStatements(node, RDF.TYPE, null);
	    if (step.localName != null){// rdf:type constraint = class must have this URI
		String uri = step.nsURI + step.localName;
		String typeURI;
		while (si.hasNext()){
		    typeURI = ((Statement)si.next()).getObject().toString();
		    if (typeURI.equals(uri) || (step.isSubClassSubPropMatching() && fhs.isSubclassOf(typeURI, uri))){
			// test passes if resource's type is the one given in the type test or if it is a subclass of it
			typeTest = true;
			break;
		    }
		}
	    }
	    else {// rdf:type contraint = class must be in the given namespace
		while (si.hasNext()){
		    if (((Statement)si.next()).getObject().toString().startsWith(step.nsURI)){
			typeTest = true;
			break;
		    }
		}
	    }
	    si.close();
	}
	else {
	    if (step.localName != null){// rdf:type constraint = class must have this local name
		CloseableIterator si = sesameRepository.getStatements(node, RDF.TYPE, null);
		while (si.hasNext()){
		    if (((Statement)si.next()).getObject().toString().endsWith(step.localName)){
			typeTest = true;
			break;
		    }
		}
		si.close();
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
	    if (node.getLabel().equals(step.literalText)){
		valueTest = true;
	    }
	}
	else {
	    valueTest = true;
	}
	if (valueTest){
	    if (step.datatypeURI != null){
		if (node.getDatatype().toString() != null &&
		    node.getDatatype().toString().equals(step.datatypeURI)){return true;}
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
	    Statement[] properties = getPropertyArcs((RResource)nodeOrArc, expr.steps[0].axis);
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
		    selectResource(expr, startStepIndex, (Resource)arc.getObject(), selectedElements, pathInstances);
		}
		else if (expr.steps[0].type == FSLLocationStep.L_STEP && arc.getObject() instanceof Literal){// Literal
		    selectLiteral(expr, startStepIndex, (Literal)arc.getObject(), selectedElements, pathInstances);
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
		    floatValues.add(new Float(((Literal)o).getLabel()));
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
		stringValues.add(((Literal)o).getLabel());
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
		return ((Literal)o).getLabel();
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
		    return Float.parseFloat(((Literal)o).getLabel());
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
		return (((RURI)o).getLocalName() != null) ? ((RURI)o).getLocalName() : "";
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
	    if (o instanceof RURI){
		return (((RURI)o).getNamespace() != null) ? ((RURI)o).getNamespace() : "";
	    }
	    else if (o instanceof Statement){
		return ((Statement)o).getPredicate().getNamespace();
	    }
	}
	return "";
    }

    /*uri*/
    public String fcURI(FSLPath expr, Object nodeOrArc){
	Vector paths = evaluatePathExpr(expr, nodeOrArc);
	if (paths.size() > 0){
	    Object o = ((Vector)paths.firstElement()).lastElement();
	    if (o instanceof RURI){
		String ns = ((RURI)o).getNamespace();
		String ln = ((RURI)o).getLocalName();
		if (ns == null){ns = "";}
		if (ln == null){ln = "";}
		return ns + ln;
	    }
	    else if (o instanceof Statement){//XXX: check that we get that in this case
		return ((Statement)o).getPredicate().toString();
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
		return ((Literal)o).getLabel();
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
		URI dtURI = ((Literal)o).getDatatype();
		return (dtURI != null) ? dtURI.toString() : "";
	    }
	}
 	return "";
    }

    /*Misc. utility methods*/

    protected static Statement[] getStatementArray(CloseableIterator si){
	Vector v = new Vector();
	while (si.hasNext()){
	    v.add(si.next());
	}
	si.close();
	Statement[] res = new Statement[v.size()];
	for (int i=0;i<v.size();i++){
	    res[i] = (Statement)v.elementAt(i);
	}
	return res;
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

    /*{0,1} <fsl_expr_in_a_file> <file_or_url_with_model_in_rdfxml>*/
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
 	    FSLHierarchyStore h = new FSLSesameHierarchyStore();
// 	    h.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl_hie_c_test.rdfs",
// 			  "file:///Users/epietrig/projects/WWW/2001/10/IsaViz/test/fsl_hie_c_test.rdfs");
// 	    h.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl_hie_p_test.rdfs",
// 			  "file:///Users/epietrig/projects/WWW/2001/10/IsaViz/test/fsl_hie_p_test.rdfs");
	    /*example of how to build the FSL expression evaluator and then evaluate it on a file that is stored in a Sesame InMemory repository*/
	    FSLSesameEvaluator fse = new FSLSesameEvaluator(n, h);
	    fse.evaluate(path, firstStepType, args[2], true);
	}
	catch (Exception ex){ex.printStackTrace();}
    }

}
