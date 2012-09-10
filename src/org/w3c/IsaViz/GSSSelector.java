/*   FILE: GSSSelector.java
 *   DATE OF CREATION:   Thu Mar 27 11:44:57 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Tue Apr 01 16:24:21 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */

package org.w3c.IsaViz;


public abstract class GSSSelector {

    protected static int minWeight=0;

    /*resource selector weights*/
    //weight of "resource URI should be equal to xxx"
    protected static int rSelURIeq=200000;    /*there might be more than 1 objectOfStatement and subjectOfStatement constraints
							   and we always want an equality constraint to have the biggest weight (as there is nothing 
							   more precise), no matter how many other constraints are expressed in the selector*/
    //weight of "resource URI should start with xxx"
    protected static int rSelURIsw=100000;     /*using such large values, there is little chance that subjectOfStatement and objectOfStatement
						constraints will interfere, even if there are many of them*/
    //weight of "predicate of statement for which this resource is the subject (resp object) should have a URI equal to xxx"
    protected static int rSelPredicateURI=1000;
    //weight of "subject (resp object) of statement for which this resource is the object (resp subject) should declare rdf:type xxx"
    protected static int rSelSubjectType=1; //minWeight + 1
    //weight of "subject (resp object) of statement for which this resource is the object (resp subject) should have a URI equal to xxx"
    protected static int rSelSubjectURI=2;
    //weight of "object (resp subject) of statement for which this resource is the subject (resp object) should declare rdf:type xxx"
    protected static int rSelObjectType=1;  //minWeight + 1
    //weight of "object (resp subject) of statement for which this resource is the subject (resp object) should have a URI equal to xxx"
    protected static int rSelObjectValueURI=2;

    /*for properties and literals we do not need to use large numbers as there is
      at most one instance of subjectOfStatement and objectOfStatement (each)*/

    /*property selector weights*/
    //weight of "property URI should be equal to xxx"
    protected static int pSelURIeq=14; //pSelSubjectType+pSelSubjectURI+pSelObjectType+pSelObjectValueURI+pSelURIsw + 1
    //weight of "property URI should start with xxx"
    protected static int pSelURIsw=7;  //pSelSubjectType+pSelSubjectURI+pSelObjectType+pSelObjectValueURI + 1
    //weight of "subject of statement for which this property is the predicate should declare rdf:type xxx"
    protected static int pSelSubjectType=1;  //minWeight + 1
    //weight of "subject of statement for which this property is the predicate should have a URI equal to xxx"
    protected static int pSelSubjectURI=2;  //pSelSubjectType + 1
    //weight of "object of statement for which this property is the predicate should declare rdf:type xxx"
    protected static int pSelObjectType=1;  //minWeight + 1
    //weight of "object of statement for which this property is the predicate should have a URI equal to xxx"
    protected static int pSelObjectValueURI=2;  //pSelObjectType + 1

    /*literal selector weights*/
    //weight of "literal should have value equal to xxx"
    protected static int lSelValue=16; //lSelSubjectType+lSelSubjectURI+lSelPredicateURI+lSelDatatype + 1
    //weight of "literal should have type xxx"
    protected static int lSelDatatype=8; //lSelSubjectType+lSelSubjectURI+lSelPredicateURI + 1
    //weight of "predicate of statement for which this literal is the object should have a URI equal to xxx"
    protected static int lSelPredicateURI=4; //lSelSubjectType+lSelSubjectURI + 1
    //weight of "subject of statement for which this literal is the object should declare rdf:type xxx"
    protected static int lSelSubjectType=1; //minWeight + 1
    //weight of "subject of statement for which this literal is the object should have a URI equal to xxx"
    protected static int lSelSubjectURI=2; //lSelSubjectType + 1


    int weight=0;   //computed weight of this selector (for priority computation)

    public int getWeight(){return weight;}

}
