/*   FILE: FSLDiffExpr.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLDiffExpr.java,v 1.3 2005/06/22 13:06:04 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**Comparison expression: le different from re*/

public class FSLDiffExpr extends FSLBinExpr {

    public FSLDiffExpr(FSLExpression le, FSLExpression re){
	leftOp = le;
	rightOp = re;
	this.type = DIFF_EXPR;
    }
    
    public String serialize(){
	return leftOp.serialize() + " != " + rightOp.serialize();
    }

}
