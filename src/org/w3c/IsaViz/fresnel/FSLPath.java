/*   FILE: FSLPath.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLPath.java,v 1.25 2006/06/03 19:34:48 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.io.StringReader;
import java.util.Vector;
import antlr.CommonAST;
import antlr.collections.AST;

/**FSL Path expression*/

public class FSLPath extends FSLExpression {

    /**Location step type: node*/
    public static short NODE_STEP = 0;
    /**Location step type: arc*/
    public static short ARC_STEP = 1;

    /**Table of namespace prefix bindings*/
    static FSLNSResolver NS_RESOLVER;
    /**Sequence of location steps that constitute this path*/
    public FSLLocationStep[] steps = null;

    /**
     * Instantiates a Java representation of the FSL path given as a String
     *@param fslPath the FSL path, represented as a String
     *@param nsr an FSLNSResolver containing namespace prefix binding declarations for all prefixes used in the path expression
     *@param firstStepType one of FSLPath.NODE_STEP or FSLPath.ARC_STEP, depending on whether the first location step of the path expression should be interpreted as a node or arc location step
     */
    public static FSLPath pathFactory(String fslPath, FSLNSResolver nsr, short firstStepType){
	FSLLexer fslLexer = new FSLLexer(new StringReader(fslPath));
	FSLParser fslParser = new FSLParser(fslLexer);
	try {
	    fslParser.locationpath();
	    CommonAST parseTree = (CommonAST)fslParser.getAST();
	    AST c = parseTree;
// 	    while (c != null){
// 		printNode(c, 0);
// 		c = c.getNextSibling();
// 	    }
	    setNSResolver(nsr);
	    return processRootNode(parseTree, firstStepType);
	}
	catch (Exception ex){
	    System.err.println("ERROR");
	    ex.printStackTrace();
	    return null;
	}
    }

    /**Set the namespace prefix binding table*/
    public static void setNSResolver(FSLNSResolver nsr){
	NS_RESOLVER = nsr;
    }

    static FSLPath processRootNode(AST rootNode, short stepType){
	Object res = processNodeAndPredicates(rootNode, getSiblingPredicates(rootNode), stepType, true);
	if (res instanceof FSLLocationStep){// !(res instanceof FSLPath)
	    return locationStep2Path((FSLLocationStep)res);
	}
	return (FSLPath)res;
    }

    /**convert a single location step into a path (one-step path)*/
    static FSLPath locationStep2Path(FSLLocationStep s){
	FSLLocationStep[] singleStep = {s};
	return new FSLPath(singleStep);
    }

    static AST[] getSiblingPredicates(AST node){
	Vector predicates = new Vector();
	node = node.getNextSibling();
	while (node != null){// add the single child of each LSQBR ("[") node
	    if (node.getType() == FSLParserTokenTypes.LSQBR){
		predicates.add(node.getFirstChild());
	    }
	    node = node.getNextSibling();
	}
	if (predicates.size() > 0){
	    AST[] res = new AST[predicates.size()];
	    for (int i=0;i<predicates.size();i++){
		res[i] = (AST)predicates.elementAt(i);
	    }
	    return res;
	}
	else {
	    return null;
	}
    }

    // predicates is null if none
    static Object processNodeAndPredicates(AST node, AST[] predicates, short stepType, boolean step){
	Object res;
	switch (node.getType()){
	case FSLParserTokenTypes.SLASHOP: {
	    res = processSLASHOP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.QNAME:{
	    res = processQNAME(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.ANYNAME:{
	    res = processANYNAME(stepType);
	    break;
	}
	case FSLParserTokenTypes.AXIS:{
	    res = processAXIS(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.MSUBOP:{
	    res = processMSUBOP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.TEXT:{
	    res = processTEXT(node);
	    break;
	}
	case FSLParserTokenTypes.TEXTLANG:{
	    res = processTEXT(node);
	    break;
	}
	case FSLParserTokenTypes.TEXTDT:{
	    res = processTEXT(node);
	    break;
	}
	case FSLParserTokenTypes.LITERAL:{
	    if (step){
		res = processLITERAL(node);
	    }
	    else {
		res = processSTRING(node);
	    }
	    break;
	}
	case FSLParserTokenTypes.SELFABBR:{
	    res = processSELFABBR(stepType);
	    break;
	}
	case FSLParserTokenTypes.OROP:{
	    res = processOROP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.ANDOP:{
	    res = processANDOP(node, stepType);
	    break;
	}
 	case FSLParserTokenTypes.NUMBER:{
	    res = processNUMBER(node);
	    break;
	}
	case FSLParserTokenTypes.EQUALOP:{
	    res = processEQUALOP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.DIFFOP:{
	    res = processDIFFOP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.INFOP:{
	    res = processINFOP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.SUPOP:{
	    res = processSUPOP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.INFEQOP:{
	    res = processINFEQOP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.SUPEQOP:{
	    res = processSUPEQOP(node, stepType);
	    break;
	}
	case FSLParserTokenTypes.FUNCTIONNAME:{
	    res = processFUNCTIONNAME(node, stepType);
	    break;
	}
	default:{
	    System.err.println("ERROR: Unknown node type");
	    res = null;
	    break;
	}
	}
	if (predicates != null){
	    // predicates[] is the list of children of AST nodes of type LSQBR ("[")
	    FSLLocationStep cres = (FSLLocationStep)res;
	    for (int i=0;i<predicates.length;i++){
		cres.addPredicate((FSLExpression)processNodeAndPredicates(predicates[i], getSiblingPredicates(predicates[i]),
									  (stepType == NODE_STEP) ? ARC_STEP : NODE_STEP,
									  false));
	    }
	}
	return res;
    }

    static FSLPath processSLASHOP(AST node, short stepType){
	int nbChildren = node.getNumberOfChildren();
	AST[] children = new AST[nbChildren];
	int i = 0;
	AST c = node.getFirstChild();
	while (i < nbChildren){
	    children[i++] = c;
	    c = c.getNextSibling();
	}
	int[] lsIndexes = getChildLocationStepIndexes(children);
	FSLPath path = null;
	// process the child location steps
	if (children[lsIndexes[0]].getType() == FSLParserTokenTypes.SLASHOP){
	    path = (FSLPath)processNodeAndPredicates(children[lsIndexes[0]], null, stepType, true);
	    // predicates associated with 2nd child (always a locationstep)
	    AST[] preds = null;
	    if (children.length - lsIndexes[1] - 1 > 0){
		preds = new AST[children.length - lsIndexes[1] - 1];
		System.arraycopy(children, lsIndexes[1] + 1, preds, 0, children.length - lsIndexes[1] - 1);
		for (int j=0;j<preds.length;j++){
		    preds[j] = preds[j].getFirstChild();
		}
	    }
	    Object c2 = processNodeAndPredicates(children[lsIndexes[1]], preds,
						 (path.steps[path.steps.length-1] instanceof FSLNodeStep) ? ARC_STEP : NODE_STEP,
						 true);
	    path.appendLocationStep((FSLLocationStep)c2);
	}
	else {// c1 instanceof FSLLocationStep
	    AST[] preds1 = null;
	    AST[] preds2 = null;
	    if (lsIndexes[1] - 1 > 0){
		preds1 = new AST[lsIndexes[1] - 1];
		System.arraycopy(children, 1, preds1, 0, lsIndexes[1] - 1);
		for (int j=0;j<preds1.length;j++){
		    preds1[j] = preds1[j].getFirstChild();
		}
	    }
	    if (children.length - lsIndexes[1] - 1 > 0){
		preds2 = new AST[children.length - lsIndexes[1] - 1];
		System.arraycopy(children, lsIndexes[1] + 1, preds2, 0, children.length - lsIndexes[1] - 1);
		for (int j=0;j<preds2.length;j++){
		    preds2[j] = preds2[j].getFirstChild();
		}
	    }
	    Object c1 = processNodeAndPredicates(children[lsIndexes[0]], preds1,
						 stepType,
						 true);
	    Object c2 = processNodeAndPredicates(children[lsIndexes[1]], preds2,
						 (stepType == NODE_STEP) ? ARC_STEP : NODE_STEP,
						 true);
	    FSLLocationStep[] ils = {(FSLLocationStep)c1, (FSLLocationStep)c2};
	    path = new FSLPath(ils);
	}
	return path;
    }

    static int[] getChildLocationStepIndexes(AST[] nodes){
	// the first children node is necessarily the first location step
	// it can be followed by 0..n predicates
	int i = 1;
	while (nodes[i].getType() == FSLParserTokenTypes.LSQBR){i++;}
	// the next node after the last predicate is necessarily the second location step
	// it can be followed by 0..n predicates, but we don't care at this point
	int[] res = {0, i};
	return res;
    }

    static FSLLocationStep processQNAME(AST node, short stepType){
	if (stepType == NODE_STEP){
	    FSLResourceStep res = new FSLResourceStep(node.getText(), NS_RESOLVER);
	    return res;
	}
	else {// ARC_STEP
	    FSLArcStep res = new FSLArcStep(node.getText(), NS_RESOLVER);
	    return res;
	}
    }

    static FSLLocationStep processANYNAME(short stepType){
	if (stepType == NODE_STEP){
	    FSLResourceStep res = new FSLResourceStep(null, NS_RESOLVER);
	    return res;
	}
	else {// ARC_STEP
	    FSLArcStep res = new FSLArcStep(null, NS_RESOLVER);
	    return res;
	}
    }

    static FSLLocationStep processSELFABBR(short stepType){
	if (stepType == NODE_STEP){
	    FSLSelfNodeStep res = new FSLSelfNodeStep();
	    return res;
	}
	else {// ARC_STEP
	    FSLSelfArcStep res = new FSLSelfArcStep();
	    return res;
	}
    }

    static FSLLocationStep processAXIS(AST node, short stepType){
	AST lsNode = node.getFirstChild();
	FSLLocationStep res = (FSLLocationStep)processNodeAndPredicates(lsNode, getSiblingPredicates(lsNode), stepType, true);
	if (node.getText().equals(FSLLocationStep.AXIS_IN_TEXT)){
	    res.setAxis(FSLLocationStep.AXIS_IN, true);
	}
	else {
	    res.setAxis(FSLLocationStep.AXIS_OUT, true);
	}
	return res;
    }

    static FSLLocationStep processMSUBOP(AST node, short stepType){
	AST lsNode = node.getFirstChild();
	FSLLocationStep res = (FSLLocationStep)processNodeAndPredicates(lsNode, getSiblingPredicates(lsNode), stepType, true);
	if (node.getText().equals(FSLLocationStep.MATCHES_SUB)){
	    res.setMatchesSubClassSubProp(true);
	}
	else {
	    res.setMatchesSubClassSubProp(false);
	}
	return res;
    }

    static FSLNodeStep processTEXT(AST node){
	FSLLiteralStep res = new FSLLiteralStep();
	String s = node.getText();
	if (s.length() > 6){// there is an @ lang tag or a ^^ datatype URI
	    s = s.substring(6);
	    if (s.startsWith("@")){
		res.setLanguage(s.substring(1));
	    }
	    else if (s.startsWith("^^")){
		res.setDatatype(s.substring(2));
	    }
	}
	return res;
    }

    static FSLNumber processNUMBER(AST node){
	return new FSLNumber(node.getText());
    }

    static FSLString processSTRING(AST node){
	return new FSLString(node.getText());
    }

    static FSLNodeStep processLITERAL(AST node){
	return new FSLLiteralStep(node.getText());
    }

    static FSLExpression processOROP(AST node, short stepType){
	FSLExpression ex1 = (FSLExpression)processNodeAndPredicates(node.getFirstChild(), null, stepType, false);
	FSLExpression ex2 = (FSLExpression)processNodeAndPredicates(node.getFirstChild().getNextSibling(), null, stepType, false);
	return new FSLOrExpr((ex1 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex1) : ex1,
			     (ex2 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex2) : ex2);
    }

    static FSLExpression processANDOP(AST node, short stepType){
	FSLExpression ex1 = (FSLExpression)processNodeAndPredicates(node.getFirstChild(), null, stepType, false);
	FSLExpression ex2 = (FSLExpression)processNodeAndPredicates(node.getFirstChild().getNextSibling(), null, stepType, false);
	return new FSLAndExpr((ex1 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex1) : ex1,
			      (ex2 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex2) : ex2);
    }

    static FSLExpression processEQUALOP(AST node, short stepType){
	FSLExpression ex1 = (FSLExpression)processNodeAndPredicates(node.getFirstChild(), null, stepType, false);
	FSLExpression ex2 = (FSLExpression)processNodeAndPredicates(node.getFirstChild().getNextSibling(), null, stepType, false);
	return new FSLEqExpr((ex1 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex1) : ex1,
			     (ex2 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex2) : ex2);
    }

    static FSLExpression processDIFFOP(AST node, short stepType){
	FSLExpression ex1 = (FSLExpression)processNodeAndPredicates(node.getFirstChild(), null, stepType, false);
	FSLExpression ex2 = (FSLExpression)processNodeAndPredicates(node.getFirstChild().getNextSibling(), null, stepType, false);
	return new FSLDiffExpr((ex1 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex1) : ex1,
			       (ex2 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex2) : ex2);
    }

    static FSLExpression processINFOP(AST node, short stepType){
	FSLExpression ex1 = (FSLExpression)processNodeAndPredicates(node.getFirstChild(), null, stepType, false);
	FSLExpression ex2 = (FSLExpression)processNodeAndPredicates(node.getFirstChild().getNextSibling(), null, stepType, false);
	return new FSLInfExpr((ex1 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex1) : ex1,
			      (ex2 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex2) : ex2);
    }

    static FSLExpression processSUPOP(AST node, short stepType){
	FSLExpression ex1 = (FSLExpression)processNodeAndPredicates(node.getFirstChild(), null, stepType, false);
	FSLExpression ex2 = (FSLExpression)processNodeAndPredicates(node.getFirstChild().getNextSibling(), null, stepType, false);
	return new FSLSupExpr((ex1 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex1) : ex1,
			      (ex2 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex2) : ex2);
    }

    static FSLExpression processINFEQOP(AST node, short stepType){
	FSLExpression ex1 = (FSLExpression)processNodeAndPredicates(node.getFirstChild(), null, stepType, false);
	FSLExpression ex2 = (FSLExpression)processNodeAndPredicates(node.getFirstChild().getNextSibling(), null, stepType, false);
	return new FSLInfEqExpr((ex1 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex1) : ex1,
				(ex2 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex2) : ex2);
    }

    static FSLExpression processSUPEQOP(AST node, short stepType){
	FSLExpression ex1 = (FSLExpression)processNodeAndPredicates(node.getFirstChild(), null, stepType, false);
	FSLExpression ex2 = (FSLExpression)processNodeAndPredicates(node.getFirstChild().getNextSibling(), null, stepType, false);
	return new FSLSupEqExpr((ex1 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex1) : ex1,
				(ex2 instanceof FSLLocationStep) ? FSLPath.locationStep2Path((FSLLocationStep)ex2) : ex2);
    }

    static FSLExpression processFUNCTIONNAME(AST node, short stepType){
	String functionName = node.getText();
	FSLFunctionCall res = new FSLFunctionCall(functionName);
	FSLExpression ex0;
	node = node.getFirstChild();
	while (node != null){
	    ex0 = (FSLExpression)processNodeAndPredicates(node,
							  getSiblingPredicates(node),
							  stepType,
							  false);
	    res.addParameter((ex0 instanceof FSLLocationStep) ? locationStep2Path((FSLLocationStep)ex0) : ex0);
	    node = node.getNextSibling();
	}
	return res;
    }

    /**
     *Split a qualified name at the colon (e.g. dc:title -> {dc, title})
     *@param qname the qualified name to split
     *@returns the prefix binding and the local name
     */
    public static String[] splitQName(String qname){
	String[] res = new String[2];
	if (qname == null){
	    res[0] = null;
	    res[1] = null;
	}
	else {
	    int colonIndex = qname.indexOf(":");
	    res[0] = qname.substring(0, colonIndex);
	    res[1] = qname.substring(colonIndex + 1, qname.length());
	    if (res[1].equals(FSLLocationStep.ANY_TYPE)){res[1] = null;}
	}
	return res;
    }

    /**Constructs an empty FSL path*/
    public FSLPath(){
	this.type = PATH_EXPR;
    }

    /**Constructs an FSL path made of the location steps given as parameters*/
    public FSLPath(FSLLocationStep[] initialSteps){
	steps = initialSteps;
	this.type = PATH_EXPR;
    }

    /**append a location step to the path (the ordering of node vs. arc steps is not checked)*/
    public void appendLocationStep(FSLLocationStep ls){
	if (steps == null){
	    steps = new FSLLocationStep[1];
	    steps[0] = ls;
	}
	else {
	    FSLLocationStep[] t = new FSLLocationStep[steps.length + 1];
	    System.arraycopy(steps, 0, t, 0, steps.length);
	    steps = t;
	    steps[steps.length - 1] = ls;
	    // if the current step comes after an arc location step whose axis is in::,
	    // then the default axis for the current step is in::, not out::
	    if (!ls.explicitAxis && steps[steps.length - 2] instanceof FSLArcStep
		&& steps[steps.length - 2].axis == FSLLocationStep.AXIS_IN){
		ls.setAxis(FSLLocationStep.AXIS_IN, false);
	    }
	}
    }

    /**remove last location step of the path*/
    public void removeLastLocationStep(){
	if (steps != null){
	    FSLLocationStep[] t = new FSLLocationStep[steps.length - 1];
	    System.arraycopy(steps, 0, t, 0, steps.length - 1);
	    steps = t;
	}
    }

    /**Serialize the path - get a String representation of this FSL path*/
    public String serialize(){
	String res = "";
	for (int i=0;i<steps.length - 1;i++){
	    res += steps[i].serialize() + "/";
	}
	res += steps[steps.length - 1].serialize();
	return res;
    }

    /**Serialize the path - get a String representation of this FSL path*/
    public String toString(){
	return this.serialize();
    }

    /*debug*/
    private static void printNode(AST node, int tab){
	StringBuffer sbtab = new StringBuffer();
	for (int i=0;i<2*tab;i++){
	    sbtab.append(' ');
	}
	System.out.println(new String(sbtab) + node.getText());
	AST child = node.getFirstChild();
	while (child != null){
	    printNode(child, tab + 1);
	    child = child.getNextSibling();
	}
    }

}
