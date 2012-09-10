/*   FILE: Group.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: Group.java,v 1.2 2006/05/17 14:41:37 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

public class Group {

    String uri;

    Lens[] lenses;
    Format[] formats;

    public Group(String uri){
	this.uri = uri;
    }
    
    void addLens(Lens l){
	if (lenses == null){
	    lenses = new Lens[1];
	    lenses[0] = (Lens)l;
	    }
	else {
	    Lens[] tmpA = new Lens[lenses.length+1];
	    System.arraycopy(lenses, 0, tmpA, 0, lenses.length);
	    tmpA[lenses.length] = (Lens)l;
	    lenses = tmpA;
	}
    }

    void addFormat(Format f){
	if (formats == null){
	    formats = new Format[1];
	    formats[0] = (Format)f;
	    }
	else {
	    Format[] tmpA = new Format[formats.length+1];
	    System.arraycopy(formats, 0, tmpA, 0, formats.length);
	    tmpA[formats.length] = (Format)f;
	    formats = tmpA;
	}	
    }

    public String toString(){
	return uri;
    }

    /* debugging */
    
//     void printItems(){
// 	System.out.println("---------------------\nGROUP " + uri);
// 	if (lenses != null){
// 	    System.out.println("LENSES");
// 	    for (int i=0;i<lenses.length;i++){
// 		System.out.println(lenses[i]);
// 	    }
// 	}
// 	if (lenses != null){
// 	    System.out.println("FORMATS");
// 	    for (int i=0;i<lenses.length;i++){
// 		System.out.println(lenses[i]);
// 	    }
// 	}	
//     }

}