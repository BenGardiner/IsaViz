/*   FILE: FSLJenaEvaluator.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLISVEvaluator.java,v 1.14 2006/10/26 14:24:11 epietrig Exp $
 */ 


package org.w3c.IsaViz.fresnel;


import org.w3c.IsaViz.Editor;
import org.w3c.IsaViz.IResource;
import org.w3c.IsaViz.IProperty;
import org.w3c.IsaViz.ILiteral;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/**<strong>Main class for evaluating an FSL expression on an IsaViz in-memory model (used by the IsaViz FSL visual debugger)</strong>*/

public class FSLISVEvaluator extends FSLEvaluator {

    static String RDFXMLAB = "RDF/XML-ABBREV";

    Editor isv;

    public FSLISVEvaluator(Editor isv, FSLNSResolver nsr, FSLHierarchyStore fhs){
	this.isv = isv;
	this.nsr = nsr;
	this.fhs = fhs;
    }

    public Vector evaluate(String path, short firstStepType){
	try {
	    FSLPath p = FSLPath.pathFactory(path, this.nsr, firstStepType);
	    return evaluatePath(p);
	}
	catch (Exception ex){
	    System.err.println("FSLISVEvaluator: Error: ");
	    ex.printStackTrace();
	    return new Vector();
	}
    }

    public Vector evaluate(FSLPath path, short firstStepType){
	try {
	    return evaluatePath(path);
	}
	catch (Exception ex){
	    System.err.println("FSLISVEvaluator: Error: ");
	    ex.printStackTrace();
	    return new Vector();
	}
    }

    protected Vector evaluatePath(FSLPath p){
	//System.out.println("Evaluating FSL path expression  " + p.serialize());
	Vector pathInstances = new Vector();
	if (p.steps[0].type == FSLLocationStep.P_STEP){// step is an arc step
	    Vector startSet = new Vector();
	    Vector v;
	    for (Enumeration e = isv.propertiesByURI.elements();e.hasMoreElements();){
		v = (Vector)e.nextElement();
		for (Enumeration e2 = v.elements();e2.hasMoreElements();){
		    startSet.add(e2.nextElement());
		}
	    }
	    evaluateArcPath(p, startSet, pathInstances);
	}
	else {// step is a node step
	    if (p.steps[0].type == FSLLocationStep.L_STEP){
		evaluateNodePathL(p, isv.literals, pathInstances);
	    }
	    else {
		Vector startSet = new Vector();
		for (Enumeration e = isv.resourcesByURI.elements();e.hasMoreElements();){
		    startSet.add(e.nextElement());
		}
		evaluateNodePathR(p, startSet, pathInstances);
	    }
	}
	return pathInstances;
    }

    public void evaluateNodePathR(FSLPath p, Vector startSet, Vector pathInstances){
	IResource node;
	Vector selectedElements;
	for (int i=0;i<startSet.size();i++){
	    node = (IResource)startSet.elementAt(i);
	    selectedElements = new Vector();
	    selectResource(p, 0, node, selectedElements, pathInstances);
	}
    }

    protected void evaluateNodePathL(FSLPath p, Vector startSet, Vector pathInstances){
	ILiteral node;
	Vector selectedElements;
	for (int i=0;i<startSet.size();i++){
	    node = (ILiteral)startSet.elementAt(i);
	    selectedElements = new Vector();
	    selectLiteral(p, 0, node, selectedElements, pathInstances);
	}
    }

    protected void evaluateArcPath(FSLPath p, Vector startSet, Vector pathInstances){
	IProperty arc;
	Vector selectedElements;
	for (int i=0;i<startSet.size();i++){
	    arc = (IProperty)startSet.elementAt(i);
	    selectedElements = new Vector();
	    selectProperty(p, 0, arc, selectedElements, pathInstances);
	}
    }

    protected void selectResource(FSLPath path, int stepIndex, IResource node,
			Vector selectedElements, Vector pathInstances){
	if (testResource((FSLResourceStep)path.steps[stepIndex], node)){
	    Vector v = (Vector)selectedElements.clone();
	    v.add(node);
	    if (stepIndex + 1 == path.steps.length){
		pathInstances.add(v);
	    }
	    else {
		IProperty[] properties = getPropertyArcs(node, path.steps[stepIndex+1].axis);
		for (int i=0;i<properties.length;i++){
		    selectProperty(path, stepIndex+1, properties[i], v, pathInstances);
		}
	    }
	}
    }

    protected void selectLiteral(FSLPath path, int stepIndex, ILiteral node,
		       Vector selectedElements, Vector pathInstances){
	if (testLiteral((FSLLiteralStep)path.steps[stepIndex], node)){
	    Vector v = (Vector)selectedElements.clone();
	    v.add(node);
	    if (stepIndex + 1 == path.steps.length){
		pathInstances.add(v);
	    }
	    else {
		IProperty[] properties = getLPropertyArcs(node, path.steps[stepIndex+1].axis);
		for (int i=0;i<properties.length;i++){
		    selectProperty(path, stepIndex+1, properties[i], v, pathInstances);
		}
	    }
	}
    }

    protected void selectProperty(FSLPath path, int stepIndex, IProperty arc,
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
			if (arc.getObject() instanceof IResource){
			    selectResource(path, stepIndex+1, (IResource)arc.getObject(), v, pathInstances);
			}
		    }
		    else {// FSLLiteralStep
			if (arc.getObject() instanceof ILiteral){
			    selectLiteral(path, stepIndex+1, (ILiteral)arc.getObject(), v, pathInstances);
			}
		    }
		}
	    }
	}
    }

    protected IProperty[] getPropertyArcs(IResource node, short axis){
	if (axis == FSLLocationStep.AXIS_OUT){
	    return getPropertyArray(node.getOutgoingPredicates());
	}
	else {// AXIS_IN
	    return getPropertyArray(node.getIncomingPredicates());
	}
    }

    protected IProperty[] getLPropertyArcs(ILiteral node, short axis){
	if (axis == FSLLocationStep.AXIS_IN){
	    IProperty[] res = {node.getIncomingPredicate()};
	    return res;
	}
	else {// AXIS_IN
	    System.err.println("Warning : selectLiteral: path models a literal with an outgoing arc: "+node.getValue());
	    return null;
	}
    }

    protected boolean testResource(FSLResourceStep step, IResource node){
	boolean typeTest = false;
	if (step.nsURI != null){
	    if (step.localName != null){// rdf:type constraint = class must have this URI
		String uri = step.nsURI + step.localName;
		Vector typeURIs = node.getRDFTypes();
		String typeURI;
		for (int i=0;i<typeURIs.size();i++){
		    typeURI = (String)typeURIs.elementAt(i);
		    if (typeURI.equals(uri) || (step.isSubClassSubPropMatching() && fhs.isSubclassOf(typeURI, uri))){
			// test passes if resource's type is the one given in the type test or if it is a subclass of it
			typeTest = true;
			break;
		    }
		}
	    }
	    else {// rdf:type contraint = class must be in the given namespace
		typeTest = node.hasRDFTypeInNamespace(step.nsURI);
	    }
	}
	else {
	    if (step.localName != null){// rdf:type constraint = class must have this local name
		typeTest = node.hasRDFTypeWithLocalName(step.localName);
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

    protected boolean testLiteral(FSLLiteralStep step, ILiteral node){
	boolean valueTest = false;
	if (step.literalText != null){
	    if (node.getValue().equals(step.literalText)){
		valueTest = true;
	    }
	}
	else {
	    valueTest = true;
	}
	if (valueTest){
	    if (step.datatypeURI != null){
		if (node.getDatatype() != null &&
		    node.getDatatype().getURI().equals(step.datatypeURI)){return true;}
		else {return false;}
	    }
	    else if (step.lang != null){
		if (node.getLang() != null &&
		    node.getLang().equals(step.lang)){return true;}
		else {return false;}
	    }
	    else return true;
	}
	else return false;
    }

    protected boolean testProperty(FSLArcStep step, IProperty arc){
	boolean typeTest = false;
	if (step.nsURI != null){
	    if (step.localName != null){
		String uri = step.nsURI + step.localName;
		String typeURI = arc.getIdent();
		if (typeURI.equals(uri) || (step.isSubClassSubPropMatching() && fhs.isSubpropertyOf(typeURI, uri))){
		    // test passes if property is the one given in the type test or if it is one of its subproperties
		    typeTest = true;
		}
	    }
	    // rdf:type contraint = property must be in the given namespace
	    else if (arc.getNamespace().equals(step.nsURI)){
		typeTest = true;
	    }
	}
	else {
	    if (step.localName != null){// rdf:type constraint = property must have this local name
		if (arc.getLocalname().equals(step.localName)){
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

    protected boolean testNodePredicates(FSLExpression[] predicates, IResource node){
	for (int i=0;i<predicates.length;i++){
	    if (!evaluateBooleanExpr(predicates[i], node)){return false;}
	}
	return true;
    }

    protected boolean testArcPredicates(FSLExpression[] predicates, IProperty arc){
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
	if (nodeOrArc instanceof IResource){// the patch should start with an arc location step
	    // path expr should be evaluated either on incoming or outgoing statements
	    // whether we evaluate it on incoming or outgoing arcs depends on the 1st location step's axis
	    IProperty[] properties = getPropertyArcs((IResource)nodeOrArc, expr.steps[0].axis);
	    for (int i=0;i<properties.length;i++){
		selectProperty(expr, startStepIndex, properties[i], selectedElements, pathInstances);
	    }
	}
	else if (nodeOrArc instanceof IProperty){// the path should start with a node location step
	    // path expr should be evaluated either on subject or object of statement
	    // depending on the 1st location step's axis
	    if (expr.steps[0].axis == FSLLocationStep.AXIS_OUT){
		IProperty arc = (IProperty)nodeOrArc;
		if (expr.steps[0].type == FSLLocationStep.R_STEP && arc.getObject() instanceof IResource){
		    selectResource(expr, startStepIndex, (IResource)arc.getObject(), selectedElements, pathInstances);
		}
		else if (expr.steps[0].type == FSLLocationStep.L_STEP && arc.getObject() instanceof ILiteral){// ILiteral
		    selectLiteral(expr, startStepIndex, (ILiteral)arc.getObject(), selectedElements, pathInstances);
		}
	    }
	    else {// AXIS_IN
		selectResource(expr, startStepIndex, ((IProperty)nodeOrArc).getSubject(), selectedElements, pathInstances);
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
	    if (o instanceof ILiteral){
		try {//.trim() not necessary (leading and trailing whitespaces handled by parseFloat)
		    floatValues.add(new Float(((ILiteral)o).getValue()));
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
	    if (o instanceof ILiteral){
		stringValues.add(((ILiteral)o).getValue());
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
	    if (o instanceof ILiteral){
		return ((ILiteral)o).getValue();
	    }
	}
	return "";
    }

    public float getFirstLiteralAsNumber(FSLPath expr, Object nodeOrArc){
	Vector pathInstances = evaluatePathExpr(expr, nodeOrArc);
	Object o;
	for (int i=0;i<pathInstances.size();i++){
	    o = ((Vector)pathInstances.elementAt(i)).lastElement();
	    if (o instanceof ILiteral){
		try {
		    return Float.parseFloat(((ILiteral)o).getValue());
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
	    if (o instanceof IResource){
		/*TBW - localname vs. namespace not availabel from isaviz, use Jena's splitter*/
		return "";
		//return (((IResource)o).getLocalName() != null) ? ((IResource)o).getLocalName() : "";
	    }
	    else if (o instanceof IProperty){
		return ((IProperty)o).getLocalname();
	    }
	}
	return "";
    }

    /*namespace-uri*/
    public String fcNamespaceURI(FSLPath expr, Object nodeOrArc){
	Vector paths = evaluatePathExpr(expr, nodeOrArc);
	if (paths.size() > 0){
	    Object o = ((Vector)paths.firstElement()).lastElement();
	    if (o instanceof IResource){
		/*TBW - localname vs. namespace not availabel from isaviz, use Jena's splitter*/
		return "";
		//return (((Resource)o).getNameSpace() != null) ? ((Resource)o).getNameSpace() : "";
	    }
	    else if (o instanceof IProperty){
		return ((IProperty)o).getNamespace();
	    }
	}
	return "";
    }

    /*uri*/
    public String fcURI(FSLPath expr, Object nodeOrArc){
	Vector paths = evaluatePathExpr(expr, nodeOrArc);
	if (paths.size() > 0){
	    Object o = ((Vector)paths.firstElement()).lastElement();
	    if (o instanceof IResource){
		return (((IResource)o).getIdentity() != null) ? ((IResource)o).getIdentity() : "";
	    }
	    else if (o instanceof IProperty){
		return ((IProperty)o).getIdent();
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
	    if (o instanceof ILiteral){
		return ((ILiteral)o).getValue();
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
	    if (o instanceof ILiteral){
		String dtURI = ((ILiteral)o).getDatatype().getURI();
		return (dtURI != null) ? dtURI : "";
	    }
	}
	return "";
    }

    /*Misc. utility methods*/

    static IProperty[] getPropertyArray(Vector predicates){
	if (predicates != null){
	    IProperty[] res = new IProperty[predicates.size()];
	    for (int i=0;i<predicates.size();i++){
		res[i] = (IProperty)predicates.elementAt(i);
	    }
	    return res;
	}
	else {
	    return new IProperty[0];
	}
    }

    /*Debug*/
    
    void printPaths(Vector pathInstances){
	System.out.println("Found "+pathInstances.size()+" path(s)");
	for (int i=0;i<pathInstances.size();i++){
	    printPath((Vector)pathInstances.elementAt(i));
	}
    }

    void printPath(Vector v){
	Object o;
	for (int i=0;i<v.size()-1;i++){
	    o = v.elementAt(i);
	    if (o instanceof IResource){
		System.out.print(((IResource)o).getIdentity() + " / ");
	    }
	    else if (o instanceof IProperty){
		System.out.print(((IProperty)o).getIdent() + " / ");
	    }
	    else {//ILiteral
		System.out.print(((ILiteral)o).getValue() + " / ");
	    }
	}
	o = v.lastElement();
	if (o instanceof IResource){
	    System.out.println(((IResource)o).getIdentity());
	}
	else if (o instanceof IProperty){
	    System.out.println(((IProperty)o).getIdent());
	}
	else {//ILiteral
	    System.out.println(((ILiteral)o).getValue());
	}
    }

}
