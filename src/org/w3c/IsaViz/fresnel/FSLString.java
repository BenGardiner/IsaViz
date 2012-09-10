/*   FILE: FSLString.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLString.java,v 1.4 2005/06/22 13:06:04 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**FSL expression: a string of characters*/

public class FSLString extends FSLValue {

    String value;

    public FSLString(String s){
	value = removeQuotes(s);
	this.type = STR_EXPR;
    }

    public String serialize(){
	return "'" + value + "'";
    }

    public static String removeQuotes(String s){
	if (s.startsWith("\"") && s.endsWith("\"") ||
	    s.startsWith("'") && s.endsWith("'")){
	    s = s.substring(1, s.length() - 1);
	}
	return s;
    }
    
}
