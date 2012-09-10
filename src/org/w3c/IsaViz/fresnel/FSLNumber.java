/*   FILE: FSLNumber.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLNumber.java,v 1.4 2005/06/22 13:06:04 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**FSL expression: a number*/

public class FSLNumber extends FSLValue {

    float value;

    public FSLNumber(float f){
	value = f;
	this.type = NUM_EXPR;
    }

    public FSLNumber(String s){
	try {
	    value = Float.parseFloat(s);
	}
	catch(NumberFormatException ex){System.err.println("FSL Parser ERROR: bad number format "+s);}
	this.type = NUM_EXPR;
    }

    public String serialize(){
	// get rid of trailing ".0" for integers
	if (value - Math.floor(value) > 0){
	    return Float.toString(value);
	}
	else {
	    return Integer.toString((int)value);
	}
    }
    
}
