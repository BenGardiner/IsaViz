/*   FILE: GSSSOStatement.java
 *   DATE OF CREATION:   Thu Mar 27 11:44:57 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jul 09 11:00:30 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import com.hp.hpl.jena.datatypes.RDFDatatype;

/*models statements selectors where predicate is free*/

public class GSSSOStatement extends GSSStatement {

    /*constraint on the statement's subject class (refered to by its class URI) - null if none*/
    String subjectType;
    /*constraint on the statement's subject URI - null if none*/
    String subjectURI;
    /*constraint on the statement's object class (refered to by its class URI) or datatype (can be gss:PlainLiterals) - null if none*/
    String objectType;
    /*constraint on the statement's object URI or value depending on whether the object is a resource or a literal - null if none*/
    String objectValueOrURI;
    /*tells whether the contraint expressed here implies that the object is a literal or a resource - if nothing is specified in the stylesheet, defaults to literal*/
    Boolean literalObject;

    GSSSOStatement(String st,String su,String ot,String ovu,Boolean isLit){
	this.subjectType=st;
	if (this.subjectType!=null && this.subjectType.length()==0){this.subjectType=null;}
	this.subjectURI=su;
	if (this.subjectURI!=null && this.subjectURI.length()==0){this.subjectURI=null;}
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

    /*get constraint on the statement's subject class (refered to by its class URI) - returns null if none*/
    public String getSubjectType(){
	return subjectType;
    }

    /*get constraint on the statement's subject URI - returns null if none*/
    public String getSubjectURI(){
	return subjectURI;
    }

    /*constraint on the statement's object class (refered to by its class URI) or datatype (can be gss:PlainLiterals) - return null if none*/
    public String getObjectType(){
	return objectType;
    }

    /*constraint on the statement's object URI or value depending on whether the object is a resource or a literal - null if none*/
    public String getObjectValueOrURI(){
	return objectValueOrURI;
    }

    public boolean selects(IProperty p){
	boolean res=true;
	IResource s=p.getSubject();
	INode o=p.getObject();
	if (subjectType!=null){//if there is a constraint on the subject's type
	    if (!(s!=null && s.hasRDFType(subjectType))){//but the resource does not declare an rdf:type property of type subjectType
		return false;//do not select
	    }
	}
	if (subjectURI!=null){//if there is a constraint on the subject's URI
	    if (!(s!=null && s.getIdentity().equals(subjectURI))){//but the resource does not match, do not select
		return false;
	    }
	}
	if (o!=null){
	    if (o instanceof IResource){//the object of this statement is a resource
		if (literalObject!=null && literalObject.booleanValue()){return false;}//the object is supposed to be a literal, do not select
		if (objectType!=null){//if there is a constraint on the object's type
		    if (!((IResource)o).hasRDFType(objectType)){//but the resource does not declare an rdf:type property of type subjectType
			return false;//do not select
		    }
		}
		if (objectValueOrURI!=null){//if there is a constraint on the object's URI
		    if (!((IResource)o).getIdentity().equals(objectValueOrURI)){//but the resource does not match, do not select
			return false;
		    }
		}
	    }
	    else {//o instanceof ILiteral - the object of this statement is a literal
		if (literalObject!=null && !literalObject.booleanValue()){return false;}//the object is supposed to be a resource, do not select
		if (objectType!=null){//if there is a constraint on the object's type
		    RDFDatatype dt=((ILiteral)o).getDatatype();
		    if (objectType.equals(GraphStylesheet._gssPlainLiterals)){//if the selector selects only plain literals
			if (dt!=null){return false;}                          //and the literal is typed, do not select
		    }
		    else if (objectType.equals(GraphStylesheet._gssAllDatatypes)){//if the selector selects only (any) typed literals
			if (dt==null){return false;}                              //and the literal is not typed, do not select
		    }
		    else {//if the selector selects a specific datatype
			if (dt==null){return false;}//and the literal is not typed, do not select
			else {
				if (!objectType.equals(dt.getURI())){return false;}//and the literal is typed but does not match the specified type, do not select
			}
		    }
		}
		if (objectValueOrURI!=null){//if there is a constraint on the object's URI
		    String val=((ILiteral)o).getValue();
		    if (val!=null){
			val=Utils.delLeadingAndTrailingSpaces(val);
			if (val.length()>0){
			    if (!objectValueOrURI.equals(val)){return false;} //if the literal's value is not equal to the selector's constraint
			}//even after having got rid of leading and trailig white space chars, do not select
			else {return false;} //if the literal has no value except white space chars (i.e. empty), do not select
		    }
		    else {return false;} //if the literal has no value (i.e. empty), do not select
		}
	    }
	}
	else {res=false;}//if the object does not exist, do not select (should not happen)
	return res;
    }

}
