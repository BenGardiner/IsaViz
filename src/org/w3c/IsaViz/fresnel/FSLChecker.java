/*   FILE: FSLChecker.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLChecker.java,v 1.7 2005/06/13 08:36:02 epietrig Exp $
 */ 


package org.w3c.IsaViz.fresnel;

import java.io.*;
import antlr.CommonAST;
import antlr.collections.ASTEnumeration;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

public class FSLChecker {

    public static void main(String[] args){
	System.err.println("Checking FSL path in " + args[0]);
	StringBuffer bf = new StringBuffer();
	try {
	    FileReader fr = new FileReader(args[0]);
	    int c;
	    do {
		c = fr.read();
		bf.append((char)c);
	    } while(c != -1);
	    FSLNSResolver nsr = new FSLNSResolver();
	    nsr.addPrefixBinding("a", "http://a#");
	    nsr.addPrefixBinding("b", "http://b#");
	    nsr.addPrefixBinding("c", "http://c#");
	    nsr.addPrefixBinding("d", "http://d#");
	    nsr.addPrefixBinding("e", "http://e#");
	    nsr.addPrefixBinding("f", "http://f#");
	    nsr.addPrefixBinding("n", "http://n#");
	    nsr.addPrefixBinding("dc", "http://dc#");
	    nsr.addPrefixBinding("xsd", "http://xsd#");
	    nsr.addPrefixBinding("rdf", "http://rdf#");
	    nsr.addPrefixBinding("foaf", "http://foaf#");
	    nsr.addPrefixBinding("r", "http://r#");
	    nsr.addPrefixBinding("", "http://DD#"); //default NS
	    System.out.println("serialization:  " + FSLPath.pathFactory(bf.substring(0, bf.length()-1),
									nsr,
									FSLPath.NODE_STEP).serialize() + "\n");
	}
	catch (Exception ex){ex.printStackTrace();}
    }



}
