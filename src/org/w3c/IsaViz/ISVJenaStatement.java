/*   FILE: ISVJenaStatement.java
 *   DATE OF CREATION:   Wed Mar 19 10:03:12 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Tue May 13 17:30:40 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;

/*temporarily store statements (during styling process)*/

class ISVJenaStatement {

    IResource isubject;
    IProperty ipredicate;
    IResource iobjectr; //either iobjectr or iobjectl is set (the other one is null)
    ILiteral iobjectl;

    Resource jsubject;
    Property jpredicate;
    Resource jobjectr;  //either jobjectr or jobjectl is set (the other one is null)
    Literal jobjectl;

    /*sShapeType and oShapeType are (each) one of Style.{ELLIPSE,RECTANGLE,CIRCLE,DIAMOND,OCTAGON,TRIANGLEN,TRIANGLES,TRIANGLEE,TRIANGLEW} 
      or a CustomShape or a CustomPolygon*/
    Object subjectShapeType; 
    Object objectShapeType;
    

    ISVJenaStatement(IResource is,IProperty ip,IResource io,Resource s,Property p,Resource o,Object sShapeType,Object oShapeType){
	isubject=is;
	ipredicate=ip;
	iobjectr=io;
	iobjectl=null;
	jsubject=s;
	jpredicate=p;
	jobjectr=o;
	jobjectl=null;
	subjectShapeType=sShapeType;
	objectShapeType=oShapeType;
    }

    ISVJenaStatement(IResource is,IProperty ip,ILiteral io,Resource s,Property p,Literal o,Object sShapeType,Object oShapeType){
	isubject=is;
	ipredicate=ip;
	iobjectr=null;
	iobjectl=io;
	jsubject=s;
	jpredicate=p;
	jobjectr=null;
	jobjectl=o;
	subjectShapeType=sShapeType;
	objectShapeType=oShapeType;
    }

    /*returns true if the object of the statement is a resource, false if it is a literal (typed or untyped)*/
    boolean objectIsResource(){
	if (iobjectr==null){return false;}  //assumes iobjectl is not null
	else {return true;} //assumes iobjectl is null
    }

//     RDFNode getJenaObject(){
// 	if (jobjectr!=null){return jobjectr;}
// 	else {return jobjectl;}
//     }

    INode getISVObject(){
	if (iobjectr!=null){return iobjectr;}
	else {return iobjectl;}
    }

    /*one of Style.{ELLIPSE,RECTANGLE,CIRCLE,DIAMOND,OCTAGON,TRIANGLEN,TRIANGLES,TRIANGLEE,TRIANGLEW} 
      or a CustomShape or a CustomPolygon*/
    Object getSubjectShapeType(){
	return subjectShapeType;
    }

    /*one of Style.{ELLIPSE,RECTANGLE,CIRCLE,DIAMOND,OCTAGON,TRIANGLEN,TRIANGLES,TRIANGLEE,TRIANGLEW} 
      or a CustomShape or a CustomPolygon */
    Object getObjectShapeType(){
	return objectShapeType;
    }

}
