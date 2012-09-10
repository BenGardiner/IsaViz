/*   FILE: FSLLocationStep.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLLocationStep.java,v 1.13 2005/12/01 14:18:01 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**Location step on a path*/

public abstract class FSLLocationStep extends FSLExpression {

    public static final String ANY_TYPE = "*";
    public static final String SELF = ".";
    public static final String AXIS_IN_TEXT = "in::";
    public static final String AXIS_OUT_TEXT = "out::";
    public static final String MATCHES_SUB = "^";

    /**Graph traversal axis: inward*/
    public static final short AXIS_IN = 0;
    /**Graph traversal axis: outward*/
    public static final short AXIS_OUT = 1;

    /**Property (arc) step*/
    public static short P_STEP = 0;
    /**Resource (node) step*/
    public static short R_STEP = 1;
    /**Literal (node) step*/
    public static short L_STEP = 2;

    /**one of FSLLocationStep.{R_STEP,L_STEP,P_STEP}. Tells whether the step is a node step or an arc step.*/
    public short type;

    /**Graph traversal axis for this location step*/
    public short axis = AXIS_OUT;
    /**tells whether axis was explicitly specified in the expression or not*/
    boolean explicitAxis = false;

    /**tells whether the type test matches instances of subclasses of the declared class/subproperties of the declared property*/
    boolean matchesSub = false;

    /**Type constraint is split as namespace URI and local name ; either can be null*/
    public String nsURI;
    /**Type constraint is split as namespace URI and local name ; either can be null*/
    public String localName;
    
    /**Predicates associated with step <br>
       (each array item contains the entire expression associated with one [...]) <br>
       there can be several of them as in a:a[count(b:b) = 2][c:c] */
    public FSLExpression[] predicates = null;

    public void addPredicate(FSLExpression e){
	// make sure that one-step paths are instances of FSLPath
	if (e instanceof FSLLocationStep){
	    e = FSLPath.locationStep2Path((FSLLocationStep)e);
	}
	if (predicates == null){
	    predicates = new FSLExpression[1];
	    predicates[0] = e;
	}
	else {
	    FSLExpression[] t = new FSLExpression[predicates.length + 1];
	    System.arraycopy(predicates, 0, t, 0, predicates.length);
	    predicates = t;
	    predicates[predicates.length - 1] = e;
	}
    }

    public void setAxis(short a, boolean ea){
	axis = a;
	explicitAxis = ea;
    }
    
    /**Set whether the type test matches instances of subclasses of the declared class/subproperties of the declared property or not*/
    public void setMatchesSubClassSubProp(boolean b){
	matchesSub = b;
    }

    /**Set whether the type test matches instances of subclasses of the declared class/subproperties of the declared property or not*/
    public boolean isSubClassSubPropMatching(){
	return matchesSub;
    }

    public String serialize(){
	String res = "";
	if (explicitAxis){
	    if (axis == AXIS_IN){res += AXIS_IN_TEXT;}
	    else {res += AXIS_OUT_TEXT;}
	}
	if (matchesSub){
	    res += "^";
	}
	if (localName == null){
	    if (nsURI == null) {res += "*";}
	    else {res += FSLPath.NS_RESOLVER.getPrefix(nsURI) + ":*";}
	}
	else {
	    res += FSLPath.NS_RESOLVER.getPrefix(nsURI) + ":" + localName;
	}
	if (predicates != null){
	    for (int i=0;i<predicates.length;i++){
		res += "[" + predicates[i].serialize() + "]";
	    }
	}
	return res;
    }
    
}
