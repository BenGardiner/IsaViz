/*   FILE: FSLJenaEvaluator.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLEvaluator.java,v 1.5 2005/12/01 14:18:01 epietrig Exp $
 */ 


package org.w3c.IsaViz.fresnel;

import java.util.Vector;

public abstract class FSLEvaluator {

    /*FSL Namespace Resolver - contains Namespace prefix bindings.
      Each FSLEvaluator implementation is responsible for offering means of
      setting nsr at the evaluator's instantantiation time
      (see e.g. how this is done in FSLJenaEvaluator).*/
    public FSLNSResolver nsr;

    /*FSL Hierarchy store - contains class and property hierarchies (used for RDF/OWL awareness)*/
    public FSLHierarchyStore fhs;
    
    /*Abstract methods that must be defined by all evaluators extending FSLEvalutor.
      See e.g. FSLJenaEvaluator for an example of what each method does.*/

    /*Evaluate an FSLPath expression against a given node or arc*/
    public abstract Vector evaluatePathExpr(FSLPath expr, Object nodeOrArc);

    /*Get all literals in the list of nodes returned by evaluating expr on the
      given nodeOrArc*/
    public abstract String[] getLiteralsAsStrings(FSLPath expr, Object nodeOrArc);

    /*Get the first literal in the list of nodes returned by evaluating expr on
      the given nodeOrArc (order not guaranteed)*/
    public abstract String getFirstLiteralAsString(FSLPath expr, Object nodeOrArc);

    /*Get all literals in the list of nodes returned by evaluating expr on the
      given nodeOrArc, and cast them as numbers*/
    public abstract float[] getLiteralsAsNumbers(FSLPath expr, Object nodeOrArc);

    /*Get the first literal in the list of nodes returned by evaluating expr on
      the given nodeOrArc, and cast it as a number (order not guaranteed)*/
    public abstract float getFirstLiteralAsNumber(FSLPath expr, Object nodeOrArc);

    /*Get the first resource node/property arc in the list of nodes/arcs
      returned by evaluating expr on the given nodeOrArc, and return the
      local name part of its URI*/
    public abstract String fcLocalName(FSLPath expr, Object nodeOrArc);

    /*Get the first resource node/property arc in the list of nodes/arcs
      returned by evaluating expr on the given nodeOrArc, and return the
      namespace part of its URI*/
    public abstract String fcNamespaceURI(FSLPath expr, Object nodeOrArc);

    /*Get the first resource node/property arc in the list of nodes/arcs
      returned by evaluating expr on the given nodeOrArc, and return its URI*/
    public abstract String fcURI(FSLPath expr, Object nodeOrArc);

    /*Get the first element in the list of nodes returned by evaluating expr
      on the given nodeOrArc, and provided it is a literal return its value*/
    public abstract String fcLiteralValue(FSLPath expr, Object nodeOrArc);

    /*Get the first element in the list of nodes returned by evaluating expr
      on the given nodeOrArc, and provided it is a literal return its datatype*/
    public abstract String fcLiteralDT(FSLPath expr, Object nodeOrArc);

    /*Methods available for each evaluator implementation.*/

    /*FSL: evaluate a boolean expression*/
    public boolean evaluateBooleanExpr(FSLExpression expr, Object nodeOrArc){
	switch (expr.type){
	case FSLExpression.EQ_EXPR:{return testEQExpr(((FSLEqExpr)expr).leftOp,
						      ((FSLEqExpr)expr).rightOp,
						      nodeOrArc);}
	case FSLExpression.DIFF_EXPR:{return testDIFFExpr(((FSLDiffExpr)expr).leftOp,
							  ((FSLDiffExpr)expr).rightOp,
							  nodeOrArc);}
	case FSLExpression.FC_EXPR:{return callBooleanFunction((FSLFunctionCall)expr, nodeOrArc);}
	case FSLExpression.STR_EXPR:{return (((FSLString)expr).value.length() > 0) ? true : false;}
	case FSLExpression.NUM_EXPR:{return (((FSLNumber)expr).value != 0) ? true : false;}
	case FSLExpression.PATH_EXPR:{return (evaluatePathExpr((FSLPath)expr, nodeOrArc).size() > 0) ? true : false;}
	case FSLExpression.INF_EXPR:{return testINFExpr(((FSLInfExpr)expr).leftOp,
							((FSLInfExpr)expr).rightOp,
							nodeOrArc);}
	case FSLExpression.INFEQ_EXPR:{return testINFEQExpr(((FSLInfEqExpr)expr).leftOp,
							    ((FSLInfEqExpr)expr).rightOp,
							    nodeOrArc);}
	case FSLExpression.SUP_EXPR:{return testSUPExpr(((FSLSupExpr)expr).leftOp,
							((FSLSupExpr)expr).rightOp,
							nodeOrArc);}
	case FSLExpression.SUPEQ_EXPR:{return testSUPEQExpr(((FSLSupEqExpr)expr).leftOp,
							    ((FSLSupEqExpr)expr).rightOp,
							    nodeOrArc);}
	case FSLExpression.AND_EXPR:{return (evaluateBooleanExpr(((FSLAndExpr)expr).leftOp, nodeOrArc)
					     && evaluateBooleanExpr(((FSLAndExpr)expr).rightOp, nodeOrArc));}
	case FSLExpression.OR_EXPR:{return (evaluateBooleanExpr(((FSLOrExpr)expr).leftOp, nodeOrArc)
					    || evaluateBooleanExpr(((FSLOrExpr)expr).rightOp, nodeOrArc));}
	default:{
	    System.err.println("Error: evaluateBooleanExpression: unknown expression type: " + expr.type);
	    return false;
	}
	}
    }

    /*FSL: evaluate a number expression*/
    public float evaluateNumberExpr(FSLExpression expr, Object nodeOrArc){
	switch (expr.type){
	case FSLExpression.NUM_EXPR:{return ((FSLNumber)expr).value;}
	case FSLExpression.STR_EXPR:{
	    try {//.trim() not necessary (leading and trailing whitespaces handled by parseFloat)
		return Float.parseFloat(((FSLString)expr).value);
	    }
	    catch (NumberFormatException ex){return Float.NaN;}
	}
	case FSLExpression.FC_EXPR:{return callNumberFunction((FSLFunctionCall)expr, nodeOrArc);}
	default:{
	    System.err.println("Error: evaluateNumberExpression: unknown expression type: " + expr.type);
	    return Float.NaN;
	}
	}
    }

    /*FSL: evaluate a string expression*/
    public String evaluateStringExpr(FSLExpression expr, Object nodeOrArc){
	switch (expr.type){
	case FSLExpression.NUM_EXPR:{return Float.toString(((FSLNumber)expr).value);}
	case FSLExpression.STR_EXPR:{return ((FSLString)expr).value;}
	case FSLExpression.FC_EXPR:{return callStringFunction((FSLFunctionCall)expr, nodeOrArc);}
	default:{
	    System.err.println("Error: evaluateStringExpression: unknown expression type: " + expr.type);
	    return "";
	}
	}
    }

    /*FSL: equal to test*/
    public boolean testEQExpr(FSLExpression leftExpr, FSLExpression rightExpr, Object nodeOrArc){
	Short leftExprType = FSLExpression.getExpressionType(leftExpr);
	Short rightExprType = FSLExpression.getExpressionType(rightExpr);
	if (leftExprType == FSLExpression.TYPE_NODE_ARC_SET ^ rightExprType == FSLExpression.TYPE_NODE_ARC_SET){
	    if (leftExprType == FSLExpression.TYPE_NUMBER){// NUMBER - NODE_ARC_SET
		float leftRes = evaluateNumberExpr(leftExpr, nodeOrArc);
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		for (int i=0;i<rightRes.length;i++){
		    if (leftRes == rightRes[i]){return true;}
		}
		return false;
	    }
	    else if (rightExprType == FSLExpression.TYPE_NUMBER){// NODE_ARC_SET - NUMBER
		float[] leftRes = getLiteralsAsNumbers((FSLPath)leftExpr, nodeOrArc);
		float rightRes = evaluateNumberExpr(leftExpr, nodeOrArc);
		for (int i=0;i<leftRes.length;i++){
		    if (leftRes[i] == rightRes){return true;}
		}
		return false;
	    }
	    else if (leftExprType == FSLExpression.TYPE_STRING){// STRING - NODE_ARC_SET
		String leftRes = evaluateStringExpr(leftExpr, nodeOrArc);
		String[] rightRes = getLiteralsAsStrings((FSLPath)rightExpr, nodeOrArc);
		for (int i=0;i<rightRes.length;i++){
		    if (leftRes.equals(rightRes[i])){return true;}
		}
		return false;
	    }
	    else if (rightExprType == FSLExpression.TYPE_STRING){// NODE_ARC_SET - STRING
		String rightRes = evaluateStringExpr(rightExpr, nodeOrArc);
		String[] leftRes = getLiteralsAsStrings((FSLPath)leftExpr, nodeOrArc);
		for (int i=0;i<leftRes.length;i++){
		    if (rightRes.equals(leftRes[i])){return true;}
		}
		return false;
	    }
	    else if (leftExprType == FSLExpression.TYPE_BOOLEAN){// BOOLEAN - NODE_ARC_SET
		boolean leftRes = evaluateBooleanExpr(leftExpr, nodeOrArc);
		boolean rightRes = (evaluatePathExpr((FSLPath)rightExpr, nodeOrArc).size() > 0) ? true : false;
		return !(leftRes ^ rightRes);
	    }
	    else if (rightExprType == FSLExpression.TYPE_BOOLEAN){// NODE_ARC_SET - BOOLEAN
		boolean leftRes = (evaluatePathExpr((FSLPath)leftExpr, nodeOrArc).size() > 0) ? true : false;
		boolean rightRes = evaluateBooleanExpr(rightExpr, nodeOrArc);
		return !(leftRes ^ rightRes);
	    }
	}
	else if (leftExprType == FSLExpression.TYPE_NODE_ARC_SET
		 && rightExprType == FSLExpression.TYPE_NODE_ARC_SET){// NODE_ARC_SET - NODE_ARC_SET
	    String[] leftRes = getLiteralsAsStrings((FSLPath)leftExpr, nodeOrArc);
	    String[] rightRes = getLiteralsAsStrings((FSLPath)rightExpr, nodeOrArc);
	    for (int i=0;i<leftRes.length;i++){
		for (int j=0;j<rightRes.length;j++){
		    if (leftRes[i].equals(rightRes[j])){return true;}
		}
	    }
	    return false;
	}
	else if (leftExprType == FSLExpression.TYPE_STRING){
	    if (rightExprType == FSLExpression.TYPE_STRING){// STRING - STRING
		return evaluateStringExpr(leftExpr, nodeOrArc).equals(evaluateStringExpr(rightExpr, nodeOrArc));
	    }
	    else if (rightExprType == FSLExpression.TYPE_NUMBER){// STRING - NUMBER
		return evaluateNumberExpr(leftExpr, nodeOrArc) == evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_BOOLEAN){// STRING - BOOLEAN
		return evaluateBooleanExpr(leftExpr, nodeOrArc) == evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	}
	else if (leftExprType == FSLExpression.TYPE_NUMBER){
	    if (rightExprType == FSLExpression.TYPE_NUMBER){// NUMBER - NUMBER
		return evaluateNumberExpr(leftExpr, nodeOrArc) == evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_STRING){// NUMBER - STRING
		return evaluateNumberExpr(leftExpr, nodeOrArc) == evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_BOOLEAN){// NUMBER - BOOLEAN
		return evaluateBooleanExpr(leftExpr, nodeOrArc) == evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	}
	else if (leftExprType == FSLExpression.TYPE_BOOLEAN){
	    if (rightExprType == FSLExpression.TYPE_NUMBER){// BOOLEAN - NUMBER
		return evaluateBooleanExpr(leftExpr, nodeOrArc) == evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_BOOLEAN){// BOOLEAN - BOOLEAN
		return evaluateBooleanExpr(leftExpr, nodeOrArc) == evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_STRING){// BOOLEAN - STRING
		return evaluateBooleanExpr(leftExpr, nodeOrArc) == evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	}
	return false;
    }

    /*FSL: different from test*/
    public boolean testDIFFExpr(FSLExpression leftExpr, FSLExpression rightExpr, Object nodeOrArc){
	Short leftExprType = FSLExpression.getExpressionType(leftExpr);
	Short rightExprType = FSLExpression.getExpressionType(rightExpr);
	if (leftExprType == FSLExpression.TYPE_NODE_ARC_SET ^ rightExprType == FSLExpression.TYPE_NODE_ARC_SET){
	    if (leftExprType == FSLExpression.TYPE_NUMBER){// NUMBER - NODE_ARC_SET
		float leftRes = evaluateNumberExpr(leftExpr, nodeOrArc);
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		for (int i=0;i<rightRes.length;i++){
		    if (leftRes != rightRes[i]){return true;}
		}
		return false;
	    }
	    else if (rightExprType == FSLExpression.TYPE_NUMBER){// NODE_ARC_SET - NUMBER
		float[] leftRes = getLiteralsAsNumbers((FSLPath)leftExpr, nodeOrArc);
		float rightRes = evaluateNumberExpr(leftExpr, nodeOrArc);
		for (int i=0;i<leftRes.length;i++){
		    if (leftRes[i] != rightRes){return true;}
		}
		return false;
	    }
	    else if (leftExprType == FSLExpression.TYPE_STRING){// STRING - NODE_ARC_SET
		String leftRes = evaluateStringExpr(leftExpr, nodeOrArc);
		String[] rightRes = getLiteralsAsStrings((FSLPath)rightExpr, nodeOrArc);
		for (int i=0;i<rightRes.length;i++){
		    if (!leftRes.equals(rightRes[i])){return true;}
		}
		return false;
	    }
	    else if (rightExprType == FSLExpression.TYPE_STRING){// NODE_ARC_SET - STRING
		String rightRes = evaluateStringExpr(rightExpr, nodeOrArc);
		String[] leftRes = getLiteralsAsStrings((FSLPath)leftExpr, nodeOrArc);
		for (int i=0;i<leftRes.length;i++){
		    if (!rightRes.equals(leftRes[i])){return true;}
		}
		return false;
	    }
	    else if (leftExprType == FSLExpression.TYPE_BOOLEAN){// BOOLEAN - NODE_ARC_SET
		boolean leftRes = evaluateBooleanExpr(leftExpr, nodeOrArc);
		boolean rightRes = (evaluatePathExpr((FSLPath)rightExpr, nodeOrArc).size() > 0) ? true : false;
		return leftRes ^ rightRes;
	    }
	    else if (rightExprType == FSLExpression.TYPE_BOOLEAN){// NODE_ARC_SET - BOOLEAN
		boolean leftRes = (evaluatePathExpr((FSLPath)leftExpr, nodeOrArc).size() > 0) ? true : false;
		boolean rightRes = evaluateBooleanExpr(rightExpr, nodeOrArc);
		return leftRes ^ rightRes;
	    }
	}
	else if (leftExprType == FSLExpression.TYPE_NODE_ARC_SET
		 && rightExprType == FSLExpression.TYPE_NODE_ARC_SET){// NODE_ARC_SET - NODE_ARC_SET
	    String[] leftRes = getLiteralsAsStrings((FSLPath)leftExpr, nodeOrArc);
	    String[] rightRes = getLiteralsAsStrings((FSLPath)rightExpr, nodeOrArc);
	    for (int i=0;i<leftRes.length;i++){
		for (int j=0;j<rightRes.length;j++){
		    if (!leftRes[i].equals(rightRes[j])){return true;}
		}
	    }
	}
	else if (leftExprType == FSLExpression.TYPE_STRING){
	    if (rightExprType == FSLExpression.TYPE_STRING){// STRING - STRING
		return !evaluateStringExpr(leftExpr, nodeOrArc).equals(evaluateStringExpr(rightExpr, nodeOrArc));
	    }
	    else if (rightExprType == FSLExpression.TYPE_NUMBER){// STRING - NUMBER
		return evaluateNumberExpr(leftExpr, nodeOrArc) != evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_BOOLEAN){// STRING - BOOLEAN
		return evaluateBooleanExpr(leftExpr, nodeOrArc) != evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	}
	else if (leftExprType == FSLExpression.TYPE_NUMBER){
	    if (rightExprType == FSLExpression.TYPE_NUMBER){// NUMBER - NUMBER
		return evaluateNumberExpr(leftExpr, nodeOrArc) != evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_STRING){// NUMBER - STRING
		return evaluateNumberExpr(leftExpr, nodeOrArc) != evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_BOOLEAN){// NUMBER - BOOLEAN
		return evaluateBooleanExpr(leftExpr, nodeOrArc) != evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	}
	else if (leftExprType == FSLExpression.TYPE_BOOLEAN){
	    if (rightExprType == FSLExpression.TYPE_NUMBER){// BOOLEAN - NUMBER
		return evaluateBooleanExpr(leftExpr, nodeOrArc) != evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_BOOLEAN){// BOOLEAN - BOOLEAN
		return evaluateBooleanExpr(leftExpr, nodeOrArc) != evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	    else if (rightExprType == FSLExpression.TYPE_STRING){// BOOLEAN - STRING
		return evaluateBooleanExpr(leftExpr, nodeOrArc) != evaluateBooleanExpr(rightExpr, nodeOrArc);
	    }
	}
	return false;
    }

    /*FSL: inferior to test*/
    public boolean testINFExpr(FSLExpression leftExpr, FSLExpression rightExpr, Object nodeOrArc){
	if (FSLExpression.getExpressionType(leftExpr) == FSLExpression.TYPE_NODE_ARC_SET){
	    float[] leftRes = getLiteralsAsNumbers((FSLPath)leftExpr, nodeOrArc);
	    if (FSLExpression.getExpressionType(rightExpr) == FSLExpression.TYPE_NODE_ARC_SET){
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		/* Case of two node sets not really defined by spec,
		   return the eval performed on 1st item of each expr.
		   Other option would be to eval all results of left expr
		   against all results of right expr. */
		return (leftRes.length > 0 && rightRes.length > 0) ? leftRes[0] < rightRes[0] : false;
	    }
	    else {
		float rightRes = evaluateNumberExpr(rightExpr, nodeOrArc);
		for (int i=0;i<leftRes.length;i++){
		    if (leftRes[i] < rightRes){return true;}
		}
		return false;
	    }
	}
	else {
	    float leftRes = evaluateNumberExpr(leftExpr, nodeOrArc);
	    if (FSLExpression.getExpressionType(rightExpr) == FSLExpression.TYPE_NODE_ARC_SET){
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		for (int i=0;i<rightRes.length;i++){
		    if (leftRes < rightRes[i]){return true;}
		}
		return false;
	    }
	    else {
		return evaluateNumberExpr(leftExpr, nodeOrArc) < evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	}
    }

    /*FSL: inferior or equal to test*/
    public boolean testINFEQExpr(FSLExpression leftExpr, FSLExpression rightExpr, Object nodeOrArc){
	if (FSLExpression.getExpressionType(leftExpr) == FSLExpression.TYPE_NODE_ARC_SET){
	    float[] leftRes = getLiteralsAsNumbers((FSLPath)leftExpr, nodeOrArc);
	    if (FSLExpression.getExpressionType(rightExpr) == FSLExpression.TYPE_NODE_ARC_SET){
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		/* Case of two node sets not really defined by spec,
		   return the eval performed on 1st item of each expr.
		   Other option would be to eval all results of left expr
		   against all results of right expr. */
		return (leftRes.length > 0 && rightRes.length > 0) ? leftRes[0] <= rightRes[0] : false;
	    }
	    else {
		float rightRes = evaluateNumberExpr(rightExpr, nodeOrArc);
		for (int i=0;i<leftRes.length;i++){
		    if (leftRes[i] <= rightRes){return true;}
		}
		return false;
	    }
	}
	else {
	    float leftRes = evaluateNumberExpr(leftExpr, nodeOrArc);
	    if (FSLExpression.getExpressionType(rightExpr) == FSLExpression.TYPE_NODE_ARC_SET){
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		for (int i=0;i<rightRes.length;i++){
		    if (leftRes <= rightRes[i]){return true;}
		}
		return false;
	    }
	    else {
		return evaluateNumberExpr(leftExpr, nodeOrArc) <= evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	}
    }

    /*FSL: superior to test*/
    public boolean testSUPExpr(FSLExpression leftExpr, FSLExpression rightExpr, Object nodeOrArc){
	if (FSLExpression.getExpressionType(leftExpr) == FSLExpression.TYPE_NODE_ARC_SET){
	    float[] leftRes = getLiteralsAsNumbers((FSLPath)leftExpr, nodeOrArc);
	    if (FSLExpression.getExpressionType(rightExpr) == FSLExpression.TYPE_NODE_ARC_SET){
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		/* Case of two node sets not really defined by spec,
		   return the eval performed on 1st item of each expr.
		   Other option would be to eval all results of left expr
		   against all results of right expr. */
		return (leftRes.length > 0 && rightRes.length > 0) ? leftRes[0] > rightRes[0] : false;
	    }
	    else {
		float rightRes = evaluateNumberExpr(rightExpr, nodeOrArc);
		for (int i=0;i<leftRes.length;i++){
		    if (leftRes[i] > rightRes){return true;}
		}
		return false;
	    }
	}
	else {
	    float leftRes = evaluateNumberExpr(leftExpr, nodeOrArc);
	    if (FSLExpression.getExpressionType(rightExpr) == FSLExpression.TYPE_NODE_ARC_SET){
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		for (int i=0;i<rightRes.length;i++){
		    if (leftRes > rightRes[i]){return true;}
		}
		return false;
	    }
	    else {
		return evaluateNumberExpr(leftExpr, nodeOrArc) > evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	}
    }

    /*FSL: superior or equal to test*/
    public boolean testSUPEQExpr(FSLExpression leftExpr, FSLExpression rightExpr, Object nodeOrArc){
	if (FSLExpression.getExpressionType(leftExpr) == FSLExpression.TYPE_NODE_ARC_SET){
	    float[] leftRes = getLiteralsAsNumbers((FSLPath)leftExpr, nodeOrArc);
	    if (FSLExpression.getExpressionType(rightExpr) == FSLExpression.TYPE_NODE_ARC_SET){
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		/* Case of two node sets not really defined by spec,
		   return the eval performed on 1st item of each expr.
		   Other option would be to eval all results of left expr
		   against all results of right expr. */
		return (leftRes.length > 0 && rightRes.length > 0) ? leftRes[0] >= rightRes[0] : false;
	    }
	    else {
		float rightRes = evaluateNumberExpr(rightExpr, nodeOrArc);
		for (int i=0;i<leftRes.length;i++){
		    if (leftRes[i] >= rightRes){return true;}
		}
		return false;
	    }
	}
	else {
	    float leftRes = evaluateNumberExpr(leftExpr, nodeOrArc);
	    if (FSLExpression.getExpressionType(rightExpr) == FSLExpression.TYPE_NODE_ARC_SET){
		float[] rightRes = getLiteralsAsNumbers((FSLPath)rightExpr, nodeOrArc);
		for (int i=0;i<rightRes.length;i++){
		    if (leftRes >= rightRes[i]){return true;}
		}
		return false;
	    }
	    else {
		return evaluateNumberExpr(leftExpr, nodeOrArc) >= evaluateNumberExpr(rightExpr, nodeOrArc);
	    }
	}
    }

    /*FSL: call to a function that returns a boolean*/
    public boolean callBooleanFunction(FSLFunctionCall expr, Object nodeOrArc){
	if (expr.function == FSLFunctionCall.NOT){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcNot(evaluateBooleanExpr(expr.parameters[0], nodeOrArc));
	    }
	    else {
		printFunctionCallError("not", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return false;
	    }
	}
	else if (expr.function == FSLFunctionCall.STARTSWITH){
	    if (expr.parameters != null && expr.parameters.length == 2){
		Short secondExprType = FSLExpression.getExpressionType(expr.parameters[1]);
		if (FSLExpression.getExpressionType(expr.parameters[0]) == FSLExpression.TYPE_NODE_ARC_SET){
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcStartsWith(getLiteralsAsStrings((FSLPath)expr.parameters[0], nodeOrArc),
					    getLiteralsAsStrings((FSLPath)expr.parameters[1], nodeOrArc));
		    }
		    else {
			return fcStartsWith(getLiteralsAsStrings((FSLPath)expr.parameters[0], nodeOrArc),
					    evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
		else {
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcStartsWith(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					    getLiteralsAsStrings((FSLPath)expr.parameters[1], nodeOrArc));
		    }
		    else {
			return fcStartsWith(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					    evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
	    }
	    else {
		printFunctionCallError("starts-with", "2", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return false;
	    }
	}
	else if (expr.function == FSLFunctionCall.CONTAINS){
	    if (expr.parameters != null && expr.parameters.length == 2){
		Short secondExprType = FSLExpression.getExpressionType(expr.parameters[1]);
		if (FSLExpression.getExpressionType(expr.parameters[0]) == FSLExpression.TYPE_NODE_ARC_SET){
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcContains(getLiteralsAsStrings((FSLPath)expr.parameters[0], nodeOrArc),
					  getLiteralsAsStrings((FSLPath)expr.parameters[1], nodeOrArc));
		    }
		    else {
			return fcContains(getLiteralsAsStrings((FSLPath)expr.parameters[0], nodeOrArc),
					  evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
		else {
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcContains(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					  getLiteralsAsStrings((FSLPath)expr.parameters[1], nodeOrArc));
		    }
		    else {
			return fcContains(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					  evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
	    }
	    else {
		printFunctionCallError("contains", "2", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return false;
	    }
	}
	else if (expr.function == FSLFunctionCall.BOOLEAN){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcBoolean(expr.parameters[0], nodeOrArc);
	    }
	    else {
		printFunctionCallError("boolean", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return false;
	    }
	}
	else if (expr.function == FSLFunctionCall.TRUE){
	    return fcTrue();
	}
	else if (expr.function == FSLFunctionCall.FALSE){
	    return fcFalse();
	}
	else {
	    printUnknownFunctionError("boolean", expr.function);
	    return false;
	}
    }

    /*FSL: call to a function that returns a number*/
    public float callNumberFunction(FSLFunctionCall expr, Object nodeOrArc){
	if (expr.function == FSLFunctionCall.COUNT){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcCount((FSLPath)expr.parameters[0], nodeOrArc);
	    }
	    else {
		printFunctionCallError("count", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return Float.NaN;
	    }
	}
	else if (expr.function == FSLFunctionCall.STRINGLENGTH){
	    if (expr.parameters != null && expr.parameters.length == 1){
		Short exprType = FSLExpression.getExpressionType(expr.parameters[0]);
		if (exprType == FSLExpression.TYPE_NODE_ARC_SET){
		    return fcStringLength((FSLPath)expr.parameters[0], nodeOrArc);
		}
		else {
		    return fcStringLength(expr.parameters[0], nodeOrArc);   
		}
	    }
	    else {
		printFunctionCallError("string-length", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return Float.NaN;
	    }
	}
	else if (expr.function == FSLFunctionCall.NUMBER){
	    if (expr.parameters != null && expr.parameters.length == 1){
		Short exprType = FSLExpression.getExpressionType(expr.parameters[0]);
		if (exprType == FSLExpression.TYPE_NODE_ARC_SET){
		    return fcNumber((FSLPath)expr.parameters[0], nodeOrArc);
		}
		else {
		    return fcNumber(expr.parameters[0], nodeOrArc);   
		}
	    }
	    else {
		printFunctionCallError("number", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return Float.NaN;
	    }
	}
	else {
	    printUnknownFunctionError("number", expr.function);
	    return Float.NaN;
	}
    }

    /*FSL: call to a function that returns a string*/
    public String callStringFunction(FSLFunctionCall expr, Object nodeOrArc){
	if (expr.function == FSLFunctionCall.URI){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcURI((FSLPath)expr.parameters[0], nodeOrArc);
	    }
	    else {
		printFunctionCallError("uri", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.LOCALNAME){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcLocalName((FSLPath)expr.parameters[0], nodeOrArc);
	    }
	    else {
		printFunctionCallError("local-name", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.NAMESPACEURI){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcNamespaceURI((FSLPath)expr.parameters[0], nodeOrArc);
	    }
	    else {
		printFunctionCallError("namespace-uri", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.LITERALVALUE){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcLiteralValue((FSLPath)expr.parameters[0], nodeOrArc);
	    }
	    else {
		printFunctionCallError("literal-value", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.LITERALDT){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcLiteralDT((FSLPath)expr.parameters[0], nodeOrArc);
	    }
	    else {
		printFunctionCallError("literal-dt", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.EXPAND){
	    if (expr.parameters != null && expr.parameters.length == 1){
		return fcExpand(evaluateStringExpr(expr.parameters[0], nodeOrArc));
	    }
	    else {
		printFunctionCallError("exp", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.CONCAT){
	    if (expr.parameters != null && expr.parameters.length == 2){
		Short secondExprType = FSLExpression.getExpressionType(expr.parameters[1]);
		if (FSLExpression.getExpressionType(expr.parameters[0]) == FSLExpression.TYPE_NODE_ARC_SET){
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcConcat(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
					getFirstLiteralAsString((FSLPath)expr.parameters[1], nodeOrArc));
			
		    }
		    else {
			return fcConcat(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
					evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
		else {
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcConcat(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					getFirstLiteralAsString((FSLPath)expr.parameters[1], nodeOrArc));
		    }
		    else {
			return fcConcat(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
	    }
	    else {
		printFunctionCallError("concat", "2", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.SUBSTRINGBEFORE){
	    if (expr.parameters != null && expr.parameters.length == 2){
		Short secondExprType = FSLExpression.getExpressionType(expr.parameters[1]);
		if (FSLExpression.getExpressionType(expr.parameters[0]) == FSLExpression.TYPE_NODE_ARC_SET){
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcSubstringBefore(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
						 getFirstLiteralAsString((FSLPath)expr.parameters[1], nodeOrArc));
			
		    }
		    else {
			return fcSubstringBefore(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
						 evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
		else {
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcSubstringBefore(evaluateStringExpr(expr.parameters[0], nodeOrArc),
						 getFirstLiteralAsString((FSLPath)expr.parameters[1], nodeOrArc));
		    }
		    else {
			return fcSubstringBefore(evaluateStringExpr(expr.parameters[0], nodeOrArc),
						 evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
	    }
	    else {
		printFunctionCallError("substring-before", "2", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.SUBSTRINGAFTER){
	    if (expr.parameters != null && expr.parameters.length == 2){
		Short secondExprType = FSLExpression.getExpressionType(expr.parameters[1]);
		if (FSLExpression.getExpressionType(expr.parameters[0]) == FSLExpression.TYPE_NODE_ARC_SET){
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcSubstringAfter(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
						getFirstLiteralAsString((FSLPath)expr.parameters[1], nodeOrArc));
			
		    }
		    else {
			return fcSubstringAfter(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
						evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
		else {
		    if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			return fcSubstringAfter(evaluateStringExpr(expr.parameters[0], nodeOrArc),
						getFirstLiteralAsString((FSLPath)expr.parameters[1], nodeOrArc));
		    }
		    else {
			return fcSubstringAfter(evaluateStringExpr(expr.parameters[0], nodeOrArc),
						evaluateStringExpr(expr.parameters[1], nodeOrArc));
		    }
		}
	    }
	    else {
		printFunctionCallError("substring-after", "2", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.SUBSTRING){
	    if (expr.parameters != null && (expr.parameters.length == 2 || expr.parameters.length == 3)){
		Short firstExprType = FSLExpression.getExpressionType(expr.parameters[0]);
		Short secondExprType = FSLExpression.getExpressionType(expr.parameters[1]);
		if (expr.parameters.length == 2){
		    if (firstExprType == FSLExpression.TYPE_NODE_ARC_SET){
			if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			    return fcSubstring(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
					       Math.round(getFirstLiteralAsNumber((FSLPath)expr.parameters[1], nodeOrArc)));
			}
			else {
			    return fcSubstring(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
					       Math.round(evaluateNumberExpr(expr.parameters[1], nodeOrArc)));
			}
		    }
		    else {
			if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			    return fcSubstring(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					       Math.round(getFirstLiteralAsNumber((FSLPath)expr.parameters[1], nodeOrArc)));
			}
			else {
			    return fcSubstring(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					       Math.round(evaluateNumberExpr(expr.parameters[1], nodeOrArc)));
			}
		    }
		}
		else {//expr.parameters.length == 3
		    Short thirdExprType = FSLExpression.getExpressionType(expr.parameters[2]);
		    int length;
		    if (thirdExprType == FSLExpression.TYPE_NODE_ARC_SET){
			length = Math.round(getFirstLiteralAsNumber((FSLPath)expr.parameters[2], nodeOrArc));
		    }
		    else {
			length = Math.round(evaluateNumberExpr(expr.parameters[2], nodeOrArc));
		    }
		    if (firstExprType == FSLExpression.TYPE_NODE_ARC_SET){
			if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			    return fcSubstring(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
					       Math.round(getFirstLiteralAsNumber((FSLPath)expr.parameters[1], nodeOrArc)),
					       length);
			}
			else {
			    return fcSubstring(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc),
					       Math.round(evaluateNumberExpr(expr.parameters[1], nodeOrArc)),
					       length);
			}
		    }
		    else {
			if (secondExprType == FSLExpression.TYPE_NODE_ARC_SET){
			    return fcSubstring(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					       Math.round(getFirstLiteralAsNumber((FSLPath)expr.parameters[1], nodeOrArc)),
					       length);
			}
			else {
			    return fcSubstring(evaluateStringExpr(expr.parameters[0], nodeOrArc),
					       Math.round(evaluateNumberExpr(expr.parameters[1], nodeOrArc)),
					       length);
			}
		    }		    
		}
	    }
	    else {
		printFunctionCallError("substring", "2 or 3", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else if (expr.function == FSLFunctionCall.NORMALIZESPACE){
	    if (expr.parameters != null && expr.parameters.length == 1){
		if (FSLExpression.getExpressionType(expr.parameters[0]) == FSLExpression.TYPE_NODE_ARC_SET){
		    return fcNormalizeSpace(getFirstLiteralAsString((FSLPath)expr.parameters[0], nodeOrArc));
		}
		else {
		    return fcNormalizeSpace(evaluateStringExpr(expr.parameters[0], nodeOrArc));
		}
	    }
	    else {
		printFunctionCallError("normalize-space", "1", (expr.parameters != null) ? Integer.toString(expr.parameters.length) : "0");
		return "";
	    }
	}
	else {
	    printUnknownFunctionError("string", expr.function);
	    return "";
	}
    }

    /*Function call implementations*/

    /*FSL: function count()*/
    public int fcCount(FSLPath expr, Object nodeOrArc){
	return evaluatePathExpr(expr, nodeOrArc).size();
    }

    /*FSL: function starts-with()*/
    public static boolean fcStartsWith(String s1, String s2){
	return s1.startsWith(s2);
    }

    public static boolean fcStartsWith(String[] s1, String s2){
	for (int i=0;i<s1.length;i++){
	    if (s1[i].startsWith(s2)){return true;}
	}
	return false;
    }

    public static boolean fcStartsWith(String s1, String[] s2){
	for (int i=0;i<s2.length;i++){
	    if (s1.startsWith(s2[i])){return true;}
	}
	return false;
    }

    public static boolean fcStartsWith(String[] s1, String[] s2){
	for (int i=0;i<s1.length;i++){
	    for (int j=0;j<s2.length;j++){
		if (s1[i].startsWith(s2[j])){return true;}
	    }
	}
	return false;
    }

    /*FSL: function contains()*/
    public static boolean fcContains(String s1, String s2){
	return s1.indexOf(s2) != -1;
    }

    public static boolean fcContains(String[] s1, String s2){
	for (int i=0;i<s1.length;i++){
	    if (s1[i].indexOf(s2) != -1){return true;}
	}
	return false;
    }

    public static boolean fcContains(String s1, String[] s2){
	for (int i=0;i<s2.length;i++){
	    if (s1.indexOf(s2[i]) != -1){return true;}
	}
	return false;
    }

    public static boolean fcContains(String[] s1, String[] s2){
	for (int i=0;i<s1.length;i++){
	    for (int j=0;j<s2.length;j++){
		if (s1[i].indexOf(s2[j]) != -1){return true;}
	    }
	}
	return false;
    }

    /*FSL: function concat()*/
    public static String fcConcat(String s1, String s2){
	return s1.concat(s2);
    }

    /*FSL: function substring-before()
      substring-before("1999/04/01","/") returns 1999
    */
    public static String fcSubstringBefore(String s1, String s2){
	int i = s1.indexOf(s2);
	if (i != -1){
	    return s1.substring(0, i);
	}
	else return "";
    }

    /*FSL: function substring-after()
      substring-after("1999/04/01","/") returns 04/01
      substring-after("1999/04/01","19") returns 99/04/01
    */
    public static String fcSubstringAfter(String s1, String s2){
	int i = s1.indexOf(s2);
	if (i != -1){
	    return s1.substring(i + s2.length());
	}
	else return "";
    }

    /*FSL: function substring()
      (first index position in string is 1, not 0)
      substring("12345",2,3) returns "234"
    */
    public static String fcSubstring(String s, int startIndex, int length){
	if (0 < startIndex && startIndex <= s.length()){
	    if (length == -1){
		return s.substring(startIndex - 1);
	    }
	    else {
		if (startIndex + length -1 > s.length()){
		    length = s.length() - startIndex + 1;
		}
		return s.substring(startIndex - 1, startIndex + length - 1);
	    }
	}
	else return "";
    }

    /*
      substring("12345",2) returns "2345"
    */
    public static String fcSubstring(String s, int startIndex){
	return fcSubstring(s, startIndex, -1);
    }

    /*FSL: function string-length()*/
    public int fcStringLength(FSLPath expr, Object nodeOrArc){
	String[] lits = getLiteralsAsStrings(expr, nodeOrArc);
	if (lits.length > 0){// return length of first literal value
	    return lits[0].length();
	}
	else {
	    return 0;
	}
    }

    public int fcStringLength(FSLExpression expr, Object nodeOrArc){
	return evaluateStringExpr(expr, nodeOrArc).length();
    }

    /*FSL: function normalize-space()*/
    public static String fcNormalizeSpace(String s){
	s = s.trim();
	StringBuffer res = new StringBuffer();
	boolean previousCharIsWS = false;
	for (int i=0;i<s.length();i++){
	    if (Character.isWhitespace(s.charAt(i))){
		if (!previousCharIsWS){
		    res.append(' ');
		    previousCharIsWS = true;
		}
	    }
	    else {
		res.append(s.charAt(i));
		previousCharIsWS = false;
	    }
	}
	return res.toString();
    }

    /*FSL: function number()*/
    public float fcNumber(FSLPath expr, Object nodeOrArc){
	float[] lits = getLiteralsAsNumbers(expr, nodeOrArc);
	if (lits.length > 0){// return first literal's value
	    return lits[0];
	}
	else {
	    return Float.NaN;
	}
    }

    public float fcNumber(FSLExpression expr, Object nodeOrArc){
	return evaluateNumberExpr(expr, nodeOrArc);
    }

    /*FSL: function boolean()*/
    public boolean fcBoolean(FSLExpression expr, Object nodeOrArc){
	return evaluateBooleanExpr(expr, nodeOrArc);
    }

    /*FSL: function not()*/
    public static boolean fcNot(boolean b){
	return !b;
    }

    /*FSL: function true()*/
    public static boolean fcTrue(){
	return true;
    }

    /*FSL: function false()*/
    public static boolean fcFalse(){
	return false;
    }

    /*FSL: function expand - exp()*/
    public String fcExpand(String s){
	int prefixEndIndex = s.indexOf(":");
	if (prefixEndIndex != -1){
	    String prefix =  s.substring(0,prefixEndIndex);
	    String nsURI = nsr.getNamespaceURI(prefix);
	    return nsURI + s.substring(prefixEndIndex + 1);
	}
	return s;
    }

    public static void printFunctionCallError(String fName, String expectedNbArgs, String providedNbArgs){
	System.err.println("Error: function " + fName + "() takes " +
			   expectedNbArgs + " argument; " + providedNbArgs + " provided");
    }

    public static void printUnknownFunctionError(String ft, Short function){
	System.err.println("Error: unknown " + ft + " function (code=" + function + ")");
    }

}
