/*   FILE: FSLFunctionCall.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLFunctionCall.java,v 1.8 2005/07/18 09:51:44 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.util.Hashtable;

/**FSL function call*/

public class FSLFunctionCall extends FSLExpression {

    public static Short UNDEFINED = new Short((short)0);
    public static Short COUNT = new Short((short)1);
    public static Short LOCALNAME = new Short((short)2);
    public static Short NAMESPACEURI = new Short((short)3);
    public static Short URI = new Short((short)4);
    public static Short LITERALVALUE = new Short((short)5);
    public static Short LITERALDT = new Short((short)6);
    public static Short STARTSWITH = new Short((short)7);
    public static Short CONTAINS = new Short((short)8);
    public static Short CONCAT = new Short((short)9);
    public static Short SUBSTRINGBEFORE = new Short((short)10);
    public static Short SUBSTRINGAFTER = new Short((short)11);
    public static Short SUBSTRING = new Short((short)12);
    public static Short STRINGLENGTH = new Short((short)13);
    public static Short NORMALIZESPACE = new Short((short)14);
    public static Short NUMBER = new Short((short)15);
    public static Short BOOLEAN = new Short((short)16);
    public static Short TRUE = new Short((short)17);
    public static Short FALSE = new Short((short)18);
    public static Short NOT = new Short((short)19);
    public static Short EXPAND = new Short((short)20);

    static Hashtable _NAME2FUNCTION;
    static Hashtable _FUNCTION2NAME;
    static Hashtable _FUNCTION2RETURNTYPE;
    
    static {
	_NAME2FUNCTION = new Hashtable();
	_NAME2FUNCTION.put("count", COUNT);
	_NAME2FUNCTION.put("local-name", LOCALNAME);
	_NAME2FUNCTION.put("namespace-uri", NAMESPACEURI);
	_NAME2FUNCTION.put("uri", URI);
	_NAME2FUNCTION.put("literal-value", LITERALVALUE);
	_NAME2FUNCTION.put("literal-dt", LITERALDT);
	_NAME2FUNCTION.put("starts-with", STARTSWITH);
	_NAME2FUNCTION.put("contains", CONTAINS);
	_NAME2FUNCTION.put("concat", CONCAT);
	_NAME2FUNCTION.put("substring-before", SUBSTRINGBEFORE);
	_NAME2FUNCTION.put("substring-after", SUBSTRINGAFTER);
	_NAME2FUNCTION.put("substring", SUBSTRING);
	_NAME2FUNCTION.put("string-length", STRINGLENGTH);
	_NAME2FUNCTION.put("normalize-space", NORMALIZESPACE);
	_NAME2FUNCTION.put("number", NUMBER);
	_NAME2FUNCTION.put("boolean", BOOLEAN);
	_NAME2FUNCTION.put("true", TRUE);
	_NAME2FUNCTION.put("false", FALSE);
	_NAME2FUNCTION.put("not", NOT);
	_NAME2FUNCTION.put("exp", EXPAND);
	_FUNCTION2NAME = new Hashtable();
	_FUNCTION2NAME.put(COUNT, "count");
	_FUNCTION2NAME.put(LOCALNAME, "local-name");
	_FUNCTION2NAME.put(NAMESPACEURI, "namespace-uri");
	_FUNCTION2NAME.put(URI, "uri");
	_FUNCTION2NAME.put(LITERALVALUE, "literal-value");
	_FUNCTION2NAME.put(LITERALDT, "literal-dt");
	_FUNCTION2NAME.put(STARTSWITH, "starts-with");
	_FUNCTION2NAME.put(CONTAINS, "contains");
	_FUNCTION2NAME.put(CONCAT, "concat");
	_FUNCTION2NAME.put(SUBSTRINGBEFORE, "substring-before");
	_FUNCTION2NAME.put(SUBSTRINGAFTER, "substring-after");
	_FUNCTION2NAME.put(SUBSTRING, "substring");
	_FUNCTION2NAME.put(STRINGLENGTH, "string-length");
	_FUNCTION2NAME.put(NORMALIZESPACE, "normalize-space");
	_FUNCTION2NAME.put(NUMBER, "number");
	_FUNCTION2NAME.put(BOOLEAN, "boolean");
	_FUNCTION2NAME.put(TRUE, "true");
	_FUNCTION2NAME.put(FALSE, "false");
	_FUNCTION2NAME.put(NOT, "not");
	_FUNCTION2NAME.put(EXPAND, "exp");
	_FUNCTION2RETURNTYPE = new Hashtable();
	_FUNCTION2RETURNTYPE.put(COUNT, FSLExpression.TYPE_NUMBER);
	_FUNCTION2RETURNTYPE.put(LOCALNAME, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(NAMESPACEURI, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(URI, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(LITERALVALUE, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(LITERALDT, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(STARTSWITH, FSLExpression.TYPE_BOOLEAN);
	_FUNCTION2RETURNTYPE.put(CONTAINS, FSLExpression.TYPE_BOOLEAN);
	_FUNCTION2RETURNTYPE.put(CONCAT, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(SUBSTRINGBEFORE, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(SUBSTRINGAFTER, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(SUBSTRING, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(STRINGLENGTH, FSLExpression.TYPE_NUMBER);
	_FUNCTION2RETURNTYPE.put(NORMALIZESPACE, FSLExpression.TYPE_STRING);
	_FUNCTION2RETURNTYPE.put(NUMBER, FSLExpression.TYPE_NUMBER);
	_FUNCTION2RETURNTYPE.put(BOOLEAN, FSLExpression.TYPE_BOOLEAN);
	_FUNCTION2RETURNTYPE.put(TRUE, FSLExpression.TYPE_BOOLEAN);
	_FUNCTION2RETURNTYPE.put(FALSE, FSLExpression.TYPE_BOOLEAN);
	_FUNCTION2RETURNTYPE.put(NOT, FSLExpression.TYPE_BOOLEAN);
	_FUNCTION2RETURNTYPE.put(EXPAND, FSLExpression.TYPE_STRING);
    }

    public static Short getReturnType(Short functionType){
	return (Short)_FUNCTION2RETURNTYPE.get(functionType);
    }

    Short function = UNDEFINED;
    FSLExpression[] parameters = null;
    
    public FSLFunctionCall(String functionName){
	function = (Short)_NAME2FUNCTION.get(functionName);
	this.type = FC_EXPR;
    }

    /**add a parameter to the list of parameters passed to the function*/
    public void addParameter(FSLExpression e){
	if (parameters == null){
	    parameters = new FSLExpression[1];
	    parameters[0] = e;
	}
	else {
	    FSLExpression[] t = new FSLExpression[parameters.length + 1];
	    System.arraycopy(parameters, 0, t, 0, parameters.length);
	    parameters = t;
	    parameters[parameters.length - 1] = e;
	}
    }

    public String serialize(){
	String functionName = (String)_FUNCTION2NAME.get(function);
	String params = "";
	if (parameters != null){
	    for (int i=0;i<parameters.length-1;i++){
		params += parameters[i].serialize() + ",";
	    }
	    params += parameters[parameters.length - 1].serialize();
	}
	return functionName + "(" + params + ")";
    }
    
}
