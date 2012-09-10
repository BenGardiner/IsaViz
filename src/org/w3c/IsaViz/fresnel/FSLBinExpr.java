/*   FILE: FSLBinExpr.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLBinExpr.java,v 1.2 2005/06/13 08:34:01 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**Binary expression (taking two operands)*/

public abstract class FSLBinExpr extends FSLExpression {

    FSLExpression leftOp;
    FSLExpression rightOp;

}
