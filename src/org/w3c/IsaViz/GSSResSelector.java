/*   FILE: GSSResSelector.java
 *   DATE OF CREATION:   Thu Mar 27 11:44:57 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Aug 01 08:42:52 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import java.util.Vector;

/*models contraints on the selection of resources*/

public class GSSResSelector extends GSSSelector {

    /*constraint on the resource's URI - must be equal to - null if none*/
    String resourceURIequals;
    /*constraint on the resource's URI - must start with - null if none - ignored if resourceURIequals is not null*/
    String resourceURIstartsWith;
    /*constraints on outgoing statements (predicate and object) - may be more than one - conjunction of constraints - null if none*/
    GSSPOStatement[] subjectOfStatements;
    /*constraints on incoming statements (subject and predicate) - may be more than one - conjunction of constraints - null if none*/
    GSSSPStatement[] objectOfStatements;

    GSSResSelector(String urie,String urisw,Vector sos,Vector oos){
	this.resourceURIequals=urie;
	if (this.resourceURIequals!=null && this.resourceURIequals.length()==0){this.resourceURIequals=null;}
	this.resourceURIstartsWith=urisw;
	if (this.resourceURIstartsWith!=null && this.resourceURIstartsWith.length()==0){this.resourceURIstartsWith=null;}
	if (sos!=null && sos.size()>0){
	    subjectOfStatements=new GSSPOStatement[sos.size()];
	    for (int i=0;i<sos.size();i++){//should check that each one actually contains at least one constraint and not put it if it is not the case
		subjectOfStatements[i]=(GSSPOStatement)sos.elementAt(i);
	    }
	}
	if (oos!=null && oos.size()>0){
	    objectOfStatements=new GSSSPStatement[oos.size()];
	    for (int i=0;i<oos.size();i++){//should check that each one actually contains at least one constraint and not put it if it is not the case
		objectOfStatements[i]=(GSSSPStatement)oos.elementAt(i);
	    }
	}
	computeWeight();
    }

    protected void computeWeight(){
	weight=0;
	if (resourceURIequals!=null){weight+=GSSSelector.rSelURIeq;}
	if (resourceURIstartsWith!=null){weight+=GSSSelector.rSelURIsw;}
	if (subjectOfStatements!=null){
	    for (int i=0;i<subjectOfStatements.length;i++){
		if (subjectOfStatements[i].getPredicateURI()!=null){weight+=GSSSelector.rSelPredicateURI;}
		if (subjectOfStatements[i].getObjectValueOrURI()!=null){weight+=GSSSelector.rSelObjectValueURI;}
		if (subjectOfStatements[i].getObjectType()!=null){weight+=GSSSelector.rSelObjectType;}
	    }
	}
	if (objectOfStatements!=null){
	    for (int i=0;i<objectOfStatements.length;i++){
		if (objectOfStatements[i].getPredicateURI()!=null){weight+=GSSSelector.rSelPredicateURI;}
		if (objectOfStatements[i].getSubjectURI()!=null){weight+=GSSSelector.rSelSubjectURI;}
		if (objectOfStatements[i].getSubjectType()!=null){weight+=GSSSelector.rSelSubjectType;}
	    }
	}
    }

    public int getWeight(){
	return weight;
    }

    boolean selects(IResource r){
	boolean res=true;
	String ruri=r.getIdentity();
	/*this was implementing disjunction between uriEquals and uriStartsWith*/
// 	if (resourceURIequals!=null && resourceURIstartsWith!=null){
// 	    if (!(ruri.equals(resourceURIequals) || ruri.startsWith(resourceURIstartsWith))){return false;}
// 	}
// 	else {
// 	    if (resourceURIequals!=null){
// 		if (!ruri.equals(resourceURIequals)){return false;}
// 	    }
// 	    else if (resourceURIstartsWith!=null){
// 		if (!ruri.startsWith(resourceURIstartsWith)){return false;}
// 	    }
// 	}
	/*the new version implements conjunction (which is not a real conjunction as equals implies startsWith*/
	if (resourceURIequals!=null && !ruri.equals(resourceURIequals)){return false;}
	if (resourceURIstartsWith!=null && !ruri.startsWith(resourceURIstartsWith)){return false;}
	if (subjectOfStatements!=null){//if there are constraints on the predicates and objects of statements for which this resource is the subject
	    for (int i=0;i<subjectOfStatements.length;i++){
		if (!subjectOfStatements[i].selects(r)){return false;}
	    }
	}
	if (objectOfStatements!=null){//if there are constraints on the subjects and predicates of statements for which this resource is the object
	    for (int i=0;i<objectOfStatements.length;i++){
		if (!objectOfStatements[i].selects(r)){return false;}
	    }
	}
	/*Note: the selection of the resource is based on the conjunction of all subjectOfStatements and objectOfStatements constraints, plus the disjunction of resourceURIequals and resourceURIstartsWith*/
	return res;
    }

    public String toString(){
	String res="";
	res+="\t selector weight="+weight+"\n";
	res+="\t uri must be equal to="+resourceURIequals+"\n";
	res+="\t uri must start with="+resourceURIstartsWith+"\n";
	if (subjectOfStatements!=null){
	    for (int i=0;i<subjectOfStatements.length;i++){
		res+="\t subjectOfStatement [predicate URI,object value or URI,object (data)type] "+i+" =["+subjectOfStatements[i].getPredicateURI()+","+subjectOfStatements[i].getObjectValueOrURI()+","+subjectOfStatements[i].getObjectType()+"]\n";
	    }
	}
	if (objectOfStatements!=null){
	    for (int i=0;i<objectOfStatements.length;i++){
		res+="\t objectOfStatement [subject URI,subject type,predicate URI] "+i+" =["+objectOfStatements[i].getSubjectURI()+","+objectOfStatements[i].getSubjectType()+","+objectOfStatements[i].getPredicateURI()+"]\n";
	    }
	}
	return res;
    }

}
