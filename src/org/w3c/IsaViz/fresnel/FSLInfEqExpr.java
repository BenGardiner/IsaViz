/*   FILE: FSLInfEqExpr.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLInfEqExpr.java,v 1.3 2005/06/22 13:06:04 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**Comparison expression: le inferior or equal to re*/

public class FSLInfEqExpr extends FSLBinExpr {

    public FSLInfEqExpr(FSLExpression le, FSLExpression re){
	leftOp = le;
	rightOp = re;
	this.type = INFEQ_EXPR;
    }
    
    public String serialize(){
	return leftOp.serialize() + " <= " + rightOp.serialize();
    }

}
