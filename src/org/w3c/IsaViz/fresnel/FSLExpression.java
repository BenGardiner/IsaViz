/*   FILE: FSLExpression.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLExpression.java,v 1.10 2005/07/11 13:04:54 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**An FSL expression*/

public abstract class FSLExpression {

    public static Short TYPE_BOOLEAN = new Short((short)0);
    public static Short TYPE_NUMBER = new Short((short)1);
    public static Short TYPE_STRING = new Short((short)2);
//     public static Short TYPE_ARC_SET = new Short((short)3);
//     public static Short TYPE_NODE_SET = new Short((short)4);
    public static Short TYPE_NODE_ARC_SET = new Short((short)5);

    public static Short getExpressionType(FSLExpression expr){
	switch(expr.type){
	case EQ_EXPR:{return TYPE_BOOLEAN;}
	case FC_EXPR:{
	    return FSLFunctionCall.getReturnType(((FSLFunctionCall)expr).function);
	}
	case STR_EXPR:{return TYPE_STRING;}
	case NUM_EXPR:{return TYPE_NUMBER;}
	case PATH_EXPR:{return TYPE_NODE_ARC_SET;}
	case DIFF_EXPR:{return TYPE_BOOLEAN;}
	case INFEQ_EXPR:{return TYPE_BOOLEAN;}
	case INF_EXPR:{return TYPE_BOOLEAN;}
	case SUPEQ_EXPR:{return TYPE_BOOLEAN;}
	case SUP_EXPR:{return TYPE_BOOLEAN;}
	case AND_EXPR:{return TYPE_BOOLEAN;}
	case OR_EXPR:{return TYPE_BOOLEAN;}
	default:{
	    System.err.println("Error: getExpressionType: unknown expression type: " + expr.type);
	    return null;
	}
	}
    }

    public static final short AND_EXPR = 0;
    public static final short DIFF_EXPR = 1;
    public static final short EQ_EXPR = 2;
    public static final short FC_EXPR = 3;
    public static final short INFEQ_EXPR = 4;
    public static final short INF_EXPR = 5;
    public static final short NUM_EXPR = 6;
    public static final short OR_EXPR = 7;
    public static final short PATH_EXPR = 8;
    public static final short STR_EXPR = 9;
    public static final short SUPEQ_EXPR = 10;
    public static final short SUP_EXPR = 11;

    public short type = -1;

    public abstract String serialize();

    public String toString(){
	return serialize();
    }
    
}
