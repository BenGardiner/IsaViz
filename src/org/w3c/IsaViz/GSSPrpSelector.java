/*   FILE: GSSPrpSelector.java
 *   DATE OF CREATION:   Thu Mar 27 11:44:57 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Apr 02 08:42:52 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import com.hp.hpl.jena.datatypes.RDFDatatype;

/*models contraints on the selection of properties*/

public class GSSPrpSelector extends GSSSelector {

    /*constraint on the property's URI - must be equal to - null if none*/
    String propertyURIequals;
    /*constraint on the property's URI - must start with - null if none - ignored if propertyURIequals is not null*/
    String propertyURIstartsWith;

    /*constraints on the subject and object of the statements attached to this property - null if none*/
    GSSSOStatement predicateOfStatement;

    GSSPrpSelector(String urie,String urisw,GSSSOStatement sos){
	this.propertyURIequals=urie;
	if (this.propertyURIequals!=null && this.propertyURIequals.length()==0){this.propertyURIequals=null;}
	this.propertyURIstartsWith=urisw;
	if (this.propertyURIstartsWith!=null && this.propertyURIstartsWith.length()==0){this.propertyURIstartsWith=null;}
	this.predicateOfStatement=sos;
	if ((this.predicateOfStatement!=null) && (this.predicateOfStatement.getSubjectType()==null) && (this.predicateOfStatement.getSubjectURI()==null) && (this.predicateOfStatement.getObjectType()==null) && (this.predicateOfStatement.getObjectValueOrURI()==null)){
	    this.predicateOfStatement=null;
	}
	computeWeight();
    }

    protected void computeWeight(){
	weight=0;
	if (propertyURIequals!=null){weight+=GSSSelector.pSelURIeq;}
	if (propertyURIstartsWith!=null){weight+=GSSSelector.pSelURIsw;}
	if (predicateOfStatement!=null){
	    if (predicateOfStatement.getSubjectType()!=null){weight+=GSSSelector.pSelSubjectType;}
	    if (predicateOfStatement.getSubjectURI()!=null){weight+=GSSSelector.pSelSubjectURI;}
	    if (predicateOfStatement.getObjectType()!=null){weight+=GSSSelector.pSelObjectType;}
	    if (predicateOfStatement.getObjectValueOrURI()!=null){weight+=GSSSelector.pSelObjectValueURI;}
	}
    }

    boolean selects(IProperty p){
	boolean res=true;
	String puri=p.getIdent();
	/*this was implementing disjunction between uriEquals and uriStartsWith*/
// 	if (propertyURIequals!=null && propertyURIstartsWith!=null){
// 	    if (!(puri.equals(propertyURIequals) || puri.startsWith(propertyURIstartsWith))){return false;}
// 	}
// 	else {
// 	    if (propertyURIequals!=null){
// 		if (!puri.equals(propertyURIequals)){return false;}
// 	    }
// 	    else if (propertyURIstartsWith!=null){
// 		if (!puri.startsWith(propertyURIstartsWith)){return false;}
// 	    }
// 	}
	/*the new version implements conjunction (which is not a real conjunction as equals implies startsWith*/
	if (propertyURIequals!=null && !puri.equals(propertyURIequals)){return false;}
	if (propertyURIstartsWith!=null && !puri.startsWith(propertyURIstartsWith)){return false;}
	if (predicateOfStatement!=null){//if there is a constraint on the subject and/or object of the statement
	    res=predicateOfStatement.selects(p);//for which this property is the predicate
	}//select only if the subject and object meet the constraints expressed in predicateOfStatement
	return res;
    }

    public String toString(){
	String res="";
	res+="\t selector weight="+weight+"\n";
	res+="\t uri must be equal to="+propertyURIequals+"\n";
	res+="\t uri must start with="+propertyURIstartsWith+"\n";
	if (predicateOfStatement!=null){res+="\t predicateOfStatement [subject URI,subject type,object value or URI,object (data)type] = ["+predicateOfStatement.getSubjectURI()+","+predicateOfStatement.getSubjectType()+","+predicateOfStatement.getObjectValueOrURI()+","+predicateOfStatement.getObjectType()+"]\n";}
	return res;
    }

}
