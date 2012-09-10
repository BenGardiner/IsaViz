/*   FILE: FSLAndExpr.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLAndExpr.java,v 1.5 2005/06/22 13:06:04 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**AND expression (in predicates)*/

public class FSLAndExpr extends FSLBinExpr {

    public FSLAndExpr(FSLExpression le, FSLExpression re){
	leftOp = le;
	rightOp = re;
	this.type = AND_EXPR;
    }
    
    public String serialize(){
	return leftOp.serialize() + " and " + rightOp.serialize();
    }

}
