/*   FILE: GSSLitSelector.java
 *   DATE OF CREATION:   Thu Mar 27 11:44:57 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jun 25 17:47:06 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;

import com.hp.hpl.jena.datatypes.RDFDatatype;

/*models constraints on the selection of literals (typed and untyped)*/

public class GSSLitSelector extends GSSSelector {
    
    /*constraint on the literal's datatype (refered to by its URI) - null if none (in which case the selector is supposed to apply to all literals, typed or untyped)
      can also be gss:AllDatatypes or gss:PlainLiterals
      note: gss:AllDatatypes does not include plain literals
    */
    String datatype;
    /*constraint on the literal's value (must be exactly equal, except for leading and trailing white space chars which are ignored) - null if none*/
    String value;
    /*constraint on the literal's incoming statement (can model constraints on the statement's subject and predicate) - null if none*/
    GSSSPStatement objectOfStatement;

    GSSLitSelector(String dt,String val,GSSSPStatement oos){
	this.datatype=dt;
	if (this.datatype!=null && this.datatype.length()==0){this.datatype=null;}
	this.value=val;
	if (this.value!=null){
	    if (this.value.length()>0){//trim leading and trailing white spaces
		this.value=Utils.delLeadingAndTrailingSpaces(this.value);
	    }
	    if (this.value.length()==0){this.value=null;}
	}
	this.objectOfStatement=oos;
	if ((this.objectOfStatement!=null) && (this.objectOfStatement.getSubjectType()==null) && (this.objectOfStatement.getSubjectURI()==null) && (this.objectOfStatement.getPredicateURI()==null)){
	    this.objectOfStatement=null;
	}
	computeWeight();
    }

    protected void computeWeight(){
	weight=0;
	if (value!=null){weight+=GSSSelector.lSelValue;}
	if (datatype!=null){weight+=GSSSelector.lSelDatatype;}
	if (objectOfStatement!=null){
	    if (objectOfStatement.getPredicateURI()!=null){weight+=GSSSelector.lSelPredicateURI;}
	    if (objectOfStatement.getSubjectURI()!=null){weight+=GSSSelector.lSelSubjectURI;}
	    if (objectOfStatement.getSubjectType()!=null){weight+=GSSSelector.lSelSubjectType;}
	}
    }

    boolean selects(ILiteral l){
	boolean res=true;
	if (datatype!=null){//if there is a constraint on the datatype of the literal
	    RDFDatatype dt=l.getDatatype();
	    if (datatype.equals(GraphStylesheet._gssPlainLiterals)){//if the selector selects only plain literals
		if (dt!=null){return false;}                         //and the literal is typed, do not select
	    }
	    else if (datatype.equals(GraphStylesheet._gssAllDatatypes)){//if the selector selects only (any) typed literals
		if (dt==null){return false;}                             //and the literal is not typed, do not select
	    }
	    else {//if the selector selects a specific datatype
		if (dt==null){return false;}//and the literal is not typed, do not select
		else {
		    if (!datatype.equals(dt.getURI())){return false;}//and the literal is typed but does not match the specified type, do not select
		}
	    }
	}
	if (value!=null){//if there is a contraint on the value of the literal (we already checked that the constraint is not empty in the constructor)
	    String val=l.getValue();
	    if (val!=null){
		val=Utils.delLeadingAndTrailingSpaces(val);
		if (val.length()>0){
		    if (!value.equals(val)){return false;} //if the literal's value is not equal to the selector's constraint
		}//even after having got rid of leading and trailig white space chars, do not select
		else return false; //if the literal has no value except white space chars (i.e. empty), do not select
	    }
	    else return false; //if the literal has no value (i.e. empty), do not select
	}
	if (objectOfStatement!=null){//if there is a constraint on the incoming statement of this literal 
	    res=objectOfStatement.selects(l);//(i.e. on the subject and predicateof which this literal is the object)
	}//select only if the subject and predicate meet the constraints expressed in objectOfStatement
	return res;
    }

    public String toString(){
	String res="";
	res+="\t selector weight="+weight+"\n";
	res+="\t datatype="+datatype+"\n";
	res+="\t value="+value+"\n";
	if (objectOfStatement!=null){res+="\t objectOfStatement [subject URI,subject type,predicate URI] = ["+objectOfStatement.getSubjectURI()+","+objectOfStatement.getSubjectType()+","+objectOfStatement.getPredicateURI()+"]\n";}
	return res;
    }

}
