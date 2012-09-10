/*   FILE: GSSSPStatement.java
 *   DATE OF CREATION:   Thu Mar 27 11:44:57 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 09 10:59:47 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import java.util.Vector;

/*models statements selectors where object is free*/

public class GSSSPStatement extends GSSStatement {

    /*constraint on the statement's subject class (refered to by its class URI) - null if none*/
    String subjectType;
    /*constraint on the statement's subject URI - null if none*/
    String subjectURI;
    /*constraint on the statement's property type (refered to by its URI) - null if none*/
    String predicateURI;

    GSSSPStatement(String st,String su,String pu){
	this.subjectType=st;
	if (this.subjectType!=null && this.subjectType.length()==0){this.subjectType=null;}
	this.subjectURI=su;
	if (this.subjectURI!=null && this.subjectURI.length()==0){this.subjectURI=null;}
	this.predicateURI=pu;
	if (this.predicateURI!=null && this.predicateURI.length()==0){this.predicateURI=null;}
    }

    /*get constraint on the statement's subject class (refered to by its class URI) - returns null if none*/
    public String getSubjectType(){
	return subjectType;
    }

    /*get constraint on the statement's subject URI - returns null if none*/
    public String getSubjectURI(){
	return subjectURI;
    }

    /*get constraint on the statement's property type (refered to by its URI) - returns null if none*/
    public String getPredicateURI(){
	return predicateURI;
    }

    public boolean selects(ILiteral l){
	boolean res=true;
	IProperty p=l.getIncomingPredicate();
	IResource r=(p!=null) ? p.getSubject() : null ;
	if (predicateURI!=null){//if there is a constraint on the predicate's URI
	    if (!(p!=null && p.getIdent().equals(predicateURI))){//but the predicate does not match, do not select
		return false;
	    }
	}
	if (subjectType!=null){//if there is a constraint on the subject's type
	    if (!(r!=null && r.hasRDFType(subjectType))){//but the resource does not declare an rdf:type property of type subjectType
		return false;//do not select
	    }
	}
	if (subjectURI!=null){//if there is a constraint on the subject's URI
	    if (!(r!=null && r.getIdentity().equals(subjectURI))){//but the resource does not match, do not select
		return false;
	    }
	}
	return res;
    }

    public boolean selects(IResource o){
	boolean res=false;
	Vector ip=o.getIncomingPredicates();
	IResource s;
	IProperty p;
	if (ip!=null){
	    boolean statementMeetsAllConstraints=true;
	    for (int i=0;i<ip.size();i++){
		statementMeetsAllConstraints=true;
		p=(IProperty)ip.elementAt(i);
		s=p.getSubject();
		if (predicateURI!=null){//if there is a constraint on the predicate's URI
		    if (!(p!=null && p.getIdent().equals(predicateURI))){//but the predicate does not match, do not select
			statementMeetsAllConstraints=false;
			continue;
		    }
		}
		if (subjectType!=null){//if there is a constraint on the subject's type
		    if (!(s!=null && s.hasRDFType(subjectType))){//but the resource does not declare an rdf:type property of type subjectType
			statementMeetsAllConstraints=false;//do not select
			continue;
		    }
		}
		if (subjectURI!=null){//if there is a constraint on the subject's URI
		    if (!(s!=null && s.getIdentity().equals(subjectURI))){//but the resource does not match, do not select
			statementMeetsAllConstraints=false;
			continue;
		    }
		}
		if (statementMeetsAllConstraints){return true;}
	    }
	}
	return res;
    }

}
