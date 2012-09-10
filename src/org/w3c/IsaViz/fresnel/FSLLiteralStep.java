/*   FILE: FSLLiteralStep.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLLiteralStep.java,v 1.9 2006/06/03 19:34:48 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

/**Node step: literal*/

public class FSLLiteralStep extends FSLNodeStep {
    
    /**constraint on the literal's value*/
    public String literalText = null;
    /**constraint on the literal's datatype (full datatype URI)*/
    public String datatypeURI = null;
    /*used to remember where the split between namespace URI and local name occurs*/
    private int dtNSLNsplitIndex = -1;
    /**constraint on the literal's language*/
    public String lang = null;

    public FSLLiteralStep(){
	type = L_STEP;
    }

    public FSLLiteralStep(String text){
	type = L_STEP;
	if (text != null){
	    int endingQuoteIndex = -1 ;
	    if (text.startsWith("'")){
		endingQuoteIndex = text.lastIndexOf("'");
	    }
	    else if (text.startsWith("\"")){
		endingQuoteIndex = text.lastIndexOf("\"");
	    }
	    if (endingQuoteIndex != -1){
		// get the constraint on literal value
		literalText = text.substring(1, endingQuoteIndex);
		if (endingQuoteIndex + 2 < text.length()){
		    // get the constraint on literal datatype
		    if (text.substring(endingQuoteIndex+1, endingQuoteIndex+3).equals("^^")){
			setDatatype(text.substring(endingQuoteIndex+3));
		    }
		    // or its constraint on lang attrib
		    else if (text.substring(endingQuoteIndex+1, endingQuoteIndex+2).equals("@")){
			setLanguage(text.substring(endingQuoteIndex+2));
		    }
		}
	    }
	    else {
		literalText = text;
	    }
	}
    }

    public void setLanguage(String l){
	lang = l;	
    }

    public void setDatatype(String dt){
	String[] splittedQName = FSLPath.splitQName(dt);
	datatypeURI = FSLPath.NS_RESOLVER.getNamespaceURI(splittedQName[0]) + splittedQName[1];
	dtNSLNsplitIndex = datatypeURI.length() - splittedQName[1].length();
    }

    public String serialize(){
	String res = (literalText != null && literalText.length() > 0) ? "\""+literalText+"\"" : "text()";
	if (datatypeURI != null){
	    res += "^^" + FSLPath.NS_RESOLVER.getPrefix(datatypeURI.substring(0, dtNSLNsplitIndex)) +
		":" + datatypeURI.substring(dtNSLNsplitIndex);
	}
	else if (lang != null){
	    res += "@" + lang;
	}
	return res;
    }
    
}
