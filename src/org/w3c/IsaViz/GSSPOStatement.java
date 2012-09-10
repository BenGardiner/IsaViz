/*   FILE: GSSPOStatement.java
 *   DATE OF CREATION:   Thu Mar 27 11:44:57 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 09 10:59:26 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import java.util.Vector;

import com.hp.hpl.jena.datatypes.RDFDatatype;

/*models statements selectors where subject is free*/

public class GSSPOStatement extends GSSStatement {

    /*constraint on the statement's object class (refered to by its class URI) or datatype (can be gss:PlainLiterals) - null if none*/
    String objectType;
    /*constraint on the statement's object URI or value depending on whether the object is a resource or a literal - null if none*/
    String objectValueOrURI;
    /*tells whether the contraint expressed here implies that the object is a literal or a resource - if nothing is specified in the stylesheet, defaults to literal*/
    Boolean literalObject;
    /*constraint on the statement's property type (refered to by its URI) - null if none*/
    String predicateURI;

    GSSPOStatement(String pu,String ot,String ovu,Boolean isLit){
	this.predicateURI=pu;
	if (this.predicateURI!=null && this.predicateURI.length()==0){this.predicateURI=null;}
	this.objectType=ot;
	if (this.objectType!=null && this.objectType.length()==0){this.objectType=null;}
	this.objectValueOrURI=ovu;
	if (this.objectValueOrURI!=null){
	    if (this.objectValueOrURI.length()>0){//trim leading and trailing white spaces
		this.objectValueOrURI=Utils.delLeadingAndTrailingSpaces(this.objectValueOrURI);
	    }
	    if (this.objectValueOrURI.length()==0){this.objectValueOrURI=null;}
	}
	this.literalObject=isLit;
    }

    /*get constraint on the statement's property type (refered to by its URI) - returns null if none*/
    public String getPredicateURI(){
	return predicateURI;
    }

    /*constraint on the statement's object class (refered to by its class URI) or datatype (can be gss:PlainLiterals) - return null if none*/
    public String getObjectType(){
	return objectType;
    }

    /*constraint on the statement's object URI or value depending on whether the object is a resource or a literal - null if none*/
    public String getObjectValueOrURI(){
	return objectValueOrURI;
    }

    public boolean selects(IResource s){
	boolean res=false;
	Vector op=s.getOutgoingPredicates();
	INode o;
	IResource or;ILiteral ol;
	IProperty p;
	if (op!=null){
	    boolean statementMeetsAllConstraints=true;
	    for (int i=0;i<op.size();i++){
		statementMeetsAllConstraints=true;
		p=(IProperty)op.elementAt(i);
		if (predicateURI!=null){//if there is a constraint on the predicate's URI
		    if (!(p!=null && p.getIdent().equals(predicateURI))){//but the predicate does not match, do not select
			statementMeetsAllConstraints=false;
			continue;
		    }
		}
		o=p.getObject();
		//cast to exact class for efficiency
		if (o instanceof IResource){
		    if (literalObject!=null && literalObject.booleanValue()){statementMeetsAllConstraints=false;continue;}//the object is supposed to be a literal, do not select
		    or=(IResource)o;
		    if (objectType!=null){//if there is a constraint on the object's type
			if (!or.hasRDFType(objectType)){//but the resource does not declare an rdf:type property of type subjectType
			    statementMeetsAllConstraints=false;continue;//do not select
			}
		    }
		    if (objectValueOrURI!=null){//if there is a constraint on the object's URI
			if (!or.getIdentity().equals(objectValueOrURI)){//but the resource does not match, do not select
			    statementMeetsAllConstraints=false;continue;
			}
		    }
		}
		else {//o instanceof ILiteral
		    if (literalObject!=null && !literalObject.booleanValue()){statementMeetsAllConstraints=false;continue;}//the object is supposed to be a resource, do not select
		    ol=(ILiteral)o;
		    if (objectType!=null){//if there is a constraint on the object's type
			RDFDatatype dt=ol.getDatatype();
			if (objectType.equals(GraphStylesheet._gssPlainLiterals)){//if the selector selects only plain literals
			    if (dt!=null){statementMeetsAllConstraints=false;continue;}//and the literal is typed, do not select
			}
			else if (objectType.equals(GraphStylesheet._gssAllDatatypes)){//if the selector selects only (any) typed literals
			    if (dt==null){statementMeetsAllConstraints=false;continue;}//and the literal is not typed, do not select
			}
			else {//if the selector selects a specific datatype
			    if (dt==null){statementMeetsAllConstraints=false;continue;}//and the literal is not typed, do not select
			    else {
				if (!objectType.equals(dt.getURI())){statementMeetsAllConstraints=false;continue;}//and the literal is typed but does not match the specified type, do not select
			    }
			}
		    }
		    if (objectValueOrURI!=null){//if there is a constraint on the object's URI
			String val=ol.getValue();
			if (val!=null){
			    val=Utils.delLeadingAndTrailingSpaces(val);
			    if (val.length()>0){
				if (!objectValueOrURI.equals(val)){statementMeetsAllConstraints=false;continue;} //if the literal's value is not equal to the selector's constraint
			    }//even after having got rid of leading and trailig white space chars, do not select
			    else {statementMeetsAllConstraints=false;continue;} //if the literal has no value except white space chars (i.e. empty), do not select
			}
			else {statementMeetsAllConstraints=false;continue;} //if the literal has no value (i.e. empty), do not select
		    }
		}
		if (statementMeetsAllConstraints){return true;}
	    }
	}
	return res;
    }

    public String toString(){
	return "["+predicateURI+","+objectType+","+objectValueOrURI+","+literalObject+"]";
    }

}
