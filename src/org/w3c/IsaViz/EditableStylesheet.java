/*   FILE: EditableStylesheet.java
 *   DATE OF CREATION:   Thu Jul 31 09:19:02 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Aug 07 10:27:42 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 


package org.w3c.IsaViz;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.NsIterator;

public class EditableStylesheet implements RDFErrorHandler {

    GSSEditor editor;

    /*all declared styles - key=style ID (String); value=Style instance*/
    Hashtable styles;

    /*all declared sort enumerations = key=sort ID (String); value=CustomOrdering instance*/
    Hashtable orderings;

    /*associates resource selectors to styles - key=GSSResSelector selector; value=vector of Style IDs, in no particular order*/
    Hashtable rStyleRules;
    /*associates literal selectors to styles - key=GSSLitSelector selector; value=vector of Style IDs, in no particular order*/
    Hashtable lStyleRules;
    /*associates property selectors to styles - key=GSSPrpSelector selector; value=vector of Style IDs, in no particular order*/
    Hashtable pStyleRules;

    /*associates resource selectors to visibility rules*/
    Hashtable rVisRules;
    /*associates literal selectors to visibility rules*/
    Hashtable lVisRules;
    /*associates property selectors to visibility rules*/
    Hashtable pVisRules;

    /*associates resource selectors to layout rules*/
    Hashtable rLayoutRules;
    /*associates literal selectors to layout rules*/
    Hashtable lLayoutRules;
    /*associates property selectors to layout rules*/
    Hashtable pLayoutRules;

    /*associates resource selectors to a sort enumeration - key=GSSResSelector selector; value=one of SORT_BY_NAME,SORT_BY_NAME_REV,SORT_BY_NAMESPACE,SORT_BY_NAMESPACE_REV or a sortID (used to retrieve CustomOrderings from orderings)
      there would be no point in having sort enumerations associated with property and literal selectors, as sorting is about all statements having the same subject (which is always a resource)
    */
    Hashtable sortRules;

    Vector resourceSelectors;
    Vector propertySelectors;
    Vector literalSelectors;

    /*temporary data structures used when processing GSS statements (before building the actual styling rules)*/
    Hashtable styleStatements;  /*maps anon res centralizing all selection constraints to styles used by them
				  key=anon res (selector ID)
				  value=vector of style ID (corresponds to hashtable styles keys)
				*/
    Hashtable visibilityStatements; /*maps anon res centralizing all selection constraints to visibility rules
				      key=anon res (selector ID)
				      value=DISPLAY_NONE || VISIBLE || HIDE
				    */
    Hashtable layoutStatements; /*maps anon res centralizing all selection constraints to layout rules used by them
				  key=anon res (selector ID)
				  value=TABLE_FORM || NODE_EDGE
				*/
    Hashtable sortStatements; /*maps anon res centralizing all selection constraints to sort rules used by them
				key=anon res (selector ID)
				value=SORT_BY_NAME || SORT_BY_NAME_REV || SORT_BY_NAMESPACE || SORT_BY_NAMESPACE_REV || a String representing a CustomOrdering ID
			      */

    Hashtable selectorTypes;  /*maps anon res centralizing all selection constraints to the kind of RDF entity they select (resource, property, literal)
				key=anon res
				value=RES_SEL || LIT_SEL || PRP_SEL
			      */

    Hashtable uriEQConstraints;  //selector ID, uri to match
    Hashtable uriSWConstraints;  //selector ID, uri fragment to match

    Hashtable sos;  //key=selector ID, value=vector of (subject of statement IDs)
    Hashtable pos;  //key=selector ID, value=predicate of statement ID
    Hashtable oos;  //key=selector ID, value=vector of (object of statement IDs)

    Hashtable xosSubjects;  //statement anon res ID, subject anon res ID
    Hashtable xosPredicates; //statement anon res ID, property URI
    Hashtable xosObjects; //statement anon res ID, object anon res ID

    Hashtable valueCnstrnts; //subject or object anon res ID, URI or literal value to be matched
    Hashtable classCnstrnts; //subject or object anon res ID, class type to be matched
    Hashtable dtCnstrnts; //object anon res ID, datatype URI to be matched

    Hashtable id2rselector; //anon res ID for a resource selector -> actual GSSResSelector object
    Hashtable id2pselector; //anon res ID for a property selector -> actual GSSPrpSelector object
    Hashtable id2lselector; //anon res ID for a literal selector -> actual GSSLitSelector object

    private URL stylesheetURL; 

    public EditableStylesheet(){
	styles=new Hashtable();
	orderings=new Hashtable();
	rStyleRules=new Hashtable();
	pStyleRules=new Hashtable();
	lStyleRules=new Hashtable();
	rVisRules=new Hashtable();
	pVisRules=new Hashtable();
	lVisRules=new Hashtable();
	rLayoutRules=new Hashtable();
	pLayoutRules=new Hashtable();
	lLayoutRules=new Hashtable();
	sortRules=new Hashtable();
    }

    void load(File f,GSSEditor app){
	editor=app;
	try {stylesheetURL=f.toURL();}
	catch (MalformedURLException mue){
	    System.err.println("EditableStylesheet.load():Error: malformed stylesheet URL for local file: "+f.toString());
	    editor.reportError=true;
	}
	try {
	    FileInputStream fis=new FileInputStream(f);
	    Model model=ModelFactory.createDefaultModel();
	    RDFReader parser=model.getReader(RDFLoader.RDFXMLAB);
	    parser.setErrorHandler(this);
	    parser.setProperty(RDFLoader.errorModePropertyName,"lax");
	    parser.read(model,fis,GraphStylesheet.GSS_BASE_URI);
	    processStatements(model.listStatements());
	}
	catch (Exception ex){
	    System.err.println("RDFErrorHandler:Warning:GraphStylehseet "+format(ex));
	    editor.reportError=true;
	}
    }

    void load(java.net.URL url,GSSEditor app){
	editor=app;
	stylesheetURL=url;
	try {
	    Model model=ModelFactory.createDefaultModel();
	    RDFReader parser=model.getReader(RDFLoader.RDFXMLAB);
	    parser.setErrorHandler(this);
	    parser.setProperty(RDFLoader.errorModePropertyName,"lax");
	    parser.read(model,url.toString());
	    processStatements(model.listStatements());
	}
	catch (Exception ex){
	    System.err.println("RDFErrorHandler:Warning:GraphStylehseet "+format(ex));
	    editor.reportError=true;
	}
    }

    protected void processStatements(StmtIterator it){
	//init temporary data structures (used to remember some info while other statements (necessary to store complete rules) are processed)
 	styleStatements=new Hashtable();
 	visibilityStatements=new Hashtable();
 	layoutStatements=new Hashtable();
	sortStatements=new Hashtable();
 	selectorTypes=new Hashtable();
	uriEQConstraints=new Hashtable();
	uriSWConstraints=new Hashtable();
	sos=new Hashtable();
	pos=new Hashtable();
	oos=new Hashtable();
	xosSubjects=new Hashtable();
	xosPredicates=new Hashtable();
	xosObjects=new Hashtable();
	valueCnstrnts=new Hashtable();
	classCnstrnts=new Hashtable();
	dtCnstrnts=new Hashtable();
	try {
	    //process statements
	    Statement st;
	    while (it.hasNext()){
		st=it.nextStatement();
		if (st.getObject() instanceof Resource){processStatement(st.getSubject(),st.getPredicate(),(Resource)st.getObject());}
		else if (st.getObject() instanceof Literal){processStatement(st.getSubject(),st.getPredicate(),(Literal)st.getObject());}
	    }
	    it.close();
	    //build the final data structures (containging style, visibility and layout rules)
	    buildCustomOrderings();
	    buildSelectors();
	    cleanSelectorTempData();
 	    buildRules();
	}
	catch (Exception ex){System.err.println("EditableStylesheet.processStatements: Error: ");ex.printStackTrace();}
	//destroy temporary data structures
	cleanSelectorMapping();
	styleStatements.clear();
	styleStatements=null;
	visibilityStatements.clear();
	visibilityStatements=null;
	layoutStatements.clear();
	layoutStatements=null;
	sortStatements.clear();
	sortStatements=null;
	selectorTypes.clear();
	selectorTypes=null;
    }

    protected void cleanSelectorTempData(){
	uriEQConstraints.clear();
	uriEQConstraints=null;
	uriSWConstraints.clear();
	uriSWConstraints=null;
	sos.clear();
	sos=null;
	pos.clear();
	pos=null;
	oos.clear();
	oos=null;
	xosSubjects.clear();
	xosSubjects=null;
	xosPredicates.clear();
	xosPredicates=null;
	xosObjects.clear();
	xosObjects=null;
	valueCnstrnts.clear();
	valueCnstrnts=null;
	classCnstrnts.clear();
	classCnstrnts=null;
	dtCnstrnts.clear();
	dtCnstrnts=null;
    }

    protected void cleanSelectorMapping(){
	id2rselector.clear();
	id2rselector=null;
	id2pselector.clear();
	id2pselector=null;
	id2lselector.clear();
	id2lselector=null;
    }

    protected void processStatement(Resource s,Resource p,Resource o){
	//s could be a b-node or named resource (URI)
	String sURI=(s.isAnon()) ? s.getId().toString() : s.toString();
	String pURI=p.getURI();
	//s could be a b-node or named resource (URI)
	String oURI=(o.isAnon()) ? o.getId().toString() : o.toString();
	if (pURI.equals(GraphStylesheet._gssStyle)){rememberStyleRule(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssVisibility)){rememberVisRule(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssDisplay)){rememberVisRule(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssLayout)){rememberLayoutRule(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssURIeq)){declareURIeqConstraint(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssURIsw)){declareURIswConstraint(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssSOS)){declareSOS(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssPOS)){declarePOS(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssOOS)){declareOOS(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._rdfType)){declareSelectorTypeOrCustomOrdering(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssSubject)){declareXOSSubject(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssPredicate)){declareXOSPredicate(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssObject)){declareXOSObject(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssValue)){declareValueConstraint(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssClass)){declareClassConstraint(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssDatatype)){declareDatatypeConstraint(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssShape)){addPredefShapeAttributeToStyle(sURI,oURI);}  //a predefined shape
	else if (pURI.equals(GraphStylesheet._gssIcon)){addIconAttributeToStyle(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssTextAlign)){addTextAlignAttributeToStyle(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssSort)){rememberSortRule(sURI,oURI);}
	else if (pURI.equals(GraphStylesheet._gssStrokeDashArray)){addStrokeDashArrayToStyle(sURI,oURI);}
	else if (pURI.startsWith(GraphStylesheet._rdfItems)){addItemToCustomOrdering(sURI,pURI,oURI);}
    }

    protected void processStatement(Resource s,Resource p,Literal o){
	//s could be a b-node or named resource (URI)
	String sURI=(s.isAnon()) ? s.getId().toString() : s.toString();
	String pURI=p.getURI();
	StringBuffer oValue=new StringBuffer(o.getLexicalForm());
	Utils.delLeadingAndTrailingSpaces(oValue);
	if (pURI.equals(GraphStylesheet._gssFill)){addFillAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssStroke)){addStrokeAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssStrokeWidth)){addStrokeWAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssValue)){declareValueConstraint(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssFontWeight)){addFontWAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssFontStyle)){addFontStAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssFontFamily)){addFontFAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssFontSize)){addFontSzAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssShape)){addShapeOrPolygonAttributeToStyle(sURI,oValue.toString());}   //a custom shape or polygon, following the VShape model or VPolygon
	else if (pURI.equals(GraphStylesheet._gssStrokeDashArray)){addStrokeDashArrayToStyle(sURI,oValue.toString());}
	else if (pURI.equals(GraphStylesheet._gssURIsw)){declareURIswConstraint(sURI,oValue.toString());}  //present here too as the value
	//of property uriStarstWith can be a literal
    }

    protected void rememberStyleRule(String selector,String style){//selector should be an anon ID, style the style ID
	if (styleStatements.containsKey(selector)){
	    Vector v=(Vector)styleStatements.get(selector);
	    if (!Utils.containsString(v,style)){v.add(style);}
	}
	else {
	    Vector v=new Vector();
	    v.add(style);
	    styleStatements.put(selector,v);
	}
    }

    protected void rememberVisRule(String selector,String visibility){//visibility=gss:Show || gss:Hide || gss:None
	if (visibility.equals(GraphStylesheet._gssHide)){
	    visibilityStatements.put(selector,GraphStylesheet.VISIBILITY_HIDDEN);
	}
	else if (visibility.equals(GraphStylesheet._gssNone)){
	    visibilityStatements.put(selector,GraphStylesheet.DISPLAY_NONE);
	}
	else if (visibility.equals(GraphStylesheet._gssShow)){
	    visibilityStatements.put(selector,GraphStylesheet.VISIBILITY_VISIBLE);
	}
	else {//default - should add an error message too (illegal value, as range of property gss:visibility is {gss:Show,gss:Hide})
	    visibilityStatements.put(selector,GraphStylesheet.VISIBILITY_VISIBLE);
	}
    }

    protected void rememberLayoutRule(String selector,String layout){//layout=gss:TableForm || gss:EdgeAndNode
	if (layout.equals(GraphStylesheet._gssTable)){
	    layoutStatements.put(selector,GraphStylesheet.TABLE_FORM);
	}
	else if (layout.equals(GraphStylesheet._gssNodeAndArc)){
	    layoutStatements.put(selector,GraphStylesheet.NODE_EDGE);
	}
	else {//default - should add an error message too (illegal value, as range of property gss:layout is {gss:TableForm,gss:EdgeAndNode})
	    layoutStatements.put(selector,GraphStylesheet.NODE_EDGE);
	}
    }

    protected void rememberSortRule(String selector,String sortNode){//selector should be an anon ID, sortNode either a Custom Sort ID or one of {gss:Name, gss:NameReversed, gss:Namespace, gss:NamespaceReversed}
	if (sortNode.equals(GraphStylesheet._gssSortN)){
	    sortStatements.put(selector,GraphStylesheet.SORT_BY_NAME);
	}
	else if (sortNode.equals(GraphStylesheet._gssSortNR)){
	    sortStatements.put(selector,GraphStylesheet.SORT_BY_NAME_REV);
	}
	else if (sortNode.equals(GraphStylesheet._gssSortNS)){
	    sortStatements.put(selector,GraphStylesheet.SORT_BY_NAMESPACE);
	}
	else if (sortNode.equals(GraphStylesheet._gssSortNSR)){
	    sortStatements.put(selector,GraphStylesheet.SORT_BY_NAMESPACE_REV);
	}
	else {//Custom ordering
	    sortStatements.put(selector,sortNode);
	}
    }

    protected void declareSelectorTypeOrCustomOrdering(String selector,String type){
	if (type.equals(GraphStylesheet._gssResource)){//declaring a resource selector
	    selectorTypes.put(selector,GraphStylesheet.RES_SEL);
	}
	else if (type.equals(GraphStylesheet._gssProperty)){//declaring a property selector
	    selectorTypes.put(selector,GraphStylesheet.PRP_SEL);
	}
	else if (type.equals(GraphStylesheet._gssLiteral)){//declaring a literal selector
	    selectorTypes.put(selector,GraphStylesheet.LIT_SEL);
	}
	/*we don't really need to do anything with this statement as _1, _2, etc. are always interpreted as a sequence for custom orderings*/
// 	else if (type.equals(_rdfSeq)){
// 	    /*declaring a custom sort order specification (this is the only thing using rdf:Seq for now,
// 	      we might have problems if other elements in the language decide to use this*/
// 	}
	//else do nothing as other type declarations are not recognized (might issue an error)
    }

    protected void declareURIeqConstraint(String selector,String uri){
	uriEQConstraints.put(selector,uri);
    }

    protected void declareURIswConstraint(String selector,String uriFrag){
	uriSWConstraints.put(selector,uriFrag);
    }

    protected void declareSOS(String selector,String sosID){
	Vector v;
	if (sos.containsKey(selector)){
	    v=(Vector)sos.get(selector);
	    v.add(sosID);
	}
	else {
	    v=new Vector();
	    v.add(sosID);
	    sos.put(selector,v);   
	}
    }

    protected void declarePOS(String selector,String posID){
	pos.put(selector,posID);
    }

    protected void declareOOS(String selector,String oosID){
	Vector v;
	if (oos.containsKey(selector)){
	    v=(Vector)oos.get(selector);
	    v.add(oosID);
	}
	else {
	    v=new Vector();
	    v.add(oosID);
	    oos.put(selector,v);   
	}
    }

    protected void declareXOSSubject(String xosID,String subjectID){
	xosSubjects.put(xosID,subjectID);
    }

    protected void declareXOSPredicate(String xosID,String propertyURI){
	xosPredicates.put(xosID,propertyURI);
    }

    protected void declareXOSObject(String xosID,String objectID){
	xosObjects.put(xosID,objectID);
    }

    protected void declareValueConstraint(String subjectOrObjectID,String value){
	valueCnstrnts.put(subjectOrObjectID,value);
    }

    protected void declareClassConstraint(String subjectOrObjectID,String classURI){
	classCnstrnts.put(subjectOrObjectID,classURI);
    }

    protected void declareDatatypeConstraint(String objectID,String dtURI){
	dtCnstrnts.put(objectID,dtURI);
    }

    protected Style createAndGetStyle(String styleID){
	Style s;
	if (styles.containsKey(styleID)){
	    s=(Style)styles.get(styleID);
	}
	else {
	    s=new Style(styleID);
	    styles.put(styleID,s);
	}
	return s;
    }

    protected void addFillAttributeToStyle(String styleID,String fillValue){
	createAndGetStyle(styleID).setFill(fillValue);
    }

    protected void addStrokeAttributeToStyle(String styleID,String strokeValue){
	createAndGetStyle(styleID).setStroke(strokeValue);
    }

    protected void addStrokeWAttributeToStyle(String styleID,String strokeWidth){
	createAndGetStyle(styleID).setStrokeWidth(strokeWidth);
    }

    protected void addStrokeDashArrayToStyle(String styleID,String dashArray){
	createAndGetStyle(styleID).setStrokeDashArray(dashArray);
    }

    protected void addFontFAttributeToStyle(String styleID,String fontFamily){
	createAndGetStyle(styleID).setFontFamily(fontFamily);
    }

    protected void addFontStAttributeToStyle(String styleID,String fontStyle){
	createAndGetStyle(styleID).setFontStyle(fontStyle);
    }

    protected void addFontSzAttributeToStyle(String styleID,String fontSize){
	createAndGetStyle(styleID).setFontSize(fontSize);
    }

    protected void addFontWAttributeToStyle(String styleID,String fontWeight){
	createAndGetStyle(styleID).setFontWeight(fontWeight);
    }

    protected void addPredefShapeAttributeToStyle(String styleID,String shape){
	Style st=createAndGetStyle(styleID);
	st.setPredefShape(shape);
	if (st.getIcon()!=null){
	    System.err.println("Error: Style "+styleID+" declares both gss:shape and gss:icon properties which are mutually exclusive. The gss:icon property will be ignored.");
	    editor.reportError=true;
	}
    }

    protected void addShapeOrPolygonAttributeToStyle(String styleID,String shape){
	Style st=createAndGetStyle(styleID);
	String s=shape.trim();
	if (s.startsWith("{")){//custom polygon (VPolygon)
	    st.setCustomPolygon(shape);
	}
	else if (s.startsWith("[")){//custom shape (VShape)
	    st.setCustomShape(shape);
	}
	else {System.err.println("EditableStylesheet.addShapeOrPolygonAttributeToStyle:error: syntax error in shape or polygon definition "+s);editor.reportError=true;}
	if (st.getIcon()!=null){
	    System.err.println("Error: Style "+styleID+" declares both gss:shape and gss:icon properties which are mutually exclusive. The gss:icon property will be ignored.");
	    editor.reportError=true;
	}
    }

    protected void addIconAttributeToStyle(String styleID,String iconURL){
	Style st=createAndGetStyle(styleID);
	if (st.getShape()==null){
	    if (iconURL.equals(GraphStylesheet._gssFetch)){//dynamic image
		try {st.setIcon(new URL(GraphStylesheet._gssFetch));}//this is statically defined, so it should never throw the exception unless its value is changed
		catch (MalformedURLException ex){System.err.println("Error: Style: Malformed URL "+iconURL);ex.printStackTrace();editor.reportError=true;}
	    }
	    else {
		URL absoluteURL=Utils.getAbsoluteURL(iconURL,stylesheetURL);
		if (absoluteURL!=null){
		    //storeIcon() returns true only if the ImageIcon could be retrieved, instantiated and stored
		    st.setIcon(absoluteURL);
		}
		else {System.err.println("Error: Style "+styleID+" declares a (possibly malformed) URL value for property gss:icon:\n"+iconURL+"\nwhich could not be interpreted against the stylesheet document base URL:\n"+stylesheetURL);editor.reportError=true;}
	    }
	}
	else {//no need to set the icon as it will be ignored when applying the style
	    System.err.println("Error: Style "+styleID+" declares both gss:shape and gss:icon properties which are mutually exclusive. The gss:icon property will be ignored.\n");
	    editor.reportError=true;
	}
    }

    protected void addTextAlignAttributeToStyle(String styleID,String alignment){
	createAndGetStyle(styleID).setTextAlignment(alignment);
    }

    protected CustomOrdering createAndGetSortOrdering(String sortID){
	CustomOrdering o;
	if (orderings.containsKey(sortID)){
	    o=(CustomOrdering)orderings.get(sortID);
	}
	else {
	    o=new CustomOrdering(sortID);
	    orderings.put(sortID,o);
	}
	return o;
    }

    protected void addItemToCustomOrdering(String sortID,String rdfLi,String item){
	//rdfLi is of the form rdf:_X where X is a positive integer
	createAndGetSortOrdering(sortID).addItem(item,rdfLi);
    }

    protected void buildCustomOrderings(){
	for (Enumeration e=orderings.elements();e.hasMoreElements();){
	    ((CustomOrdering)e.nextElement()).buildFinalSequence();
	}
    }

    protected void buildSelectors(){
	id2rselector=new Hashtable();
	id2pselector=new Hashtable();
	id2lselector=new Hashtable();
	resourceSelectors=new Vector();
	literalSelectors=new Vector();
	propertySelectors=new Vector();
	String selID; //selector ID (RDF anon res ID)
	Integer selType;
	for (Enumeration e=selectorTypes.keys();e.hasMoreElements();){
	    selID=(String)e.nextElement();
	    selType=(Integer)selectorTypes.get(selID);
	    if (selType.equals(GraphStylesheet.RES_SEL)){
		buildResourceSelector(selID);
	    }
	    else if (selType.equals(GraphStylesheet.LIT_SEL)){
		buildLiteralSelector(selID);
	    }
	    else {//PRP_SEL
		buildPropertySelector(selID);
	    }
	}
    }

    protected void buildResourceSelector(String id){
	Vector sosIDs=(Vector)sos.get(id);
	Vector oosIDs=(Vector)oos.get(id);
	Vector vsos=null;
	Vector voos=null;
	if (sosIDs!=null && sosIDs.size()>0){
	    vsos=new Vector();
	    String sosID=null;
	    String objectID=null;
	    String objectClassType=null;
	    String objectDataType=null;
	    String objectValueOrURI=null;
	    String predicateURI=null;
	    for (int i=0;i<sosIDs.size();i++){
		sosID=(String)sosIDs.elementAt(i);
		objectID=(String)xosObjects.get(sosID);
		if (objectID!=null){
		    objectClassType=(String)classCnstrnts.get(objectID);
		    objectDataType=(String)dtCnstrnts.get(objectID);
		    objectValueOrURI=(String)valueCnstrnts.get(objectID);
		}
		predicateURI=(String)xosPredicates.get(sosID);
		if (predicateURI!=null || objectClassType!=null || objectDataType!=null || objectValueOrURI!=null){
		    if (objectDataType!=null && objectClassType==null){//constraint on literal objects
			vsos.add(new GSSPOStatement(predicateURI,objectDataType,objectValueOrURI,new Boolean(true)));
		    }
		    else if (objectClassType!=null && objectDataType==null){//constraint on resource objects
			vsos.add(new GSSPOStatement(predicateURI,objectClassType,objectValueOrURI,new Boolean(false)));
		    }
		    else if (objectClassType==null && objectDataType==null){//in case no information is given on the object's type, say "unknown"
			vsos.add(new GSSPOStatement(predicateURI,null,objectValueOrURI,null));
		    }
		    else {System.err.println("EditableStylesheet.buildResourceSelector(): Error: resource class and literal datatype constraints on the same resource selector's statement's object cannot coexist :"+id+" "+objectClassType+" "+objectDataType);editor.reportError=true;}
		}
		objectID=null;
		objectClassType=null;
		objectDataType=null;
		objectValueOrURI=null;
		predicateURI=null;
	    }
	}
	if (oosIDs!=null && oosIDs.size()>0){
	    voos=new Vector();
	    String oosID;
	    String subjectID=null;
	    String subjectType=null;
	    String subjectURI=null;
	    String predicateURI=null;
	    for (int i=0;i<oosIDs.size();i++){
		oosID=(String)oosIDs.elementAt(i);
		subjectID=(String)xosSubjects.get(oosID);
		if (subjectID!=null){
		    subjectType=(String)classCnstrnts.get(subjectID);
		    subjectURI=(String)valueCnstrnts.get(subjectID);
		}
		predicateURI=(String)xosPredicates.get(oosID);
		if (subjectType!=null || subjectURI!=null || predicateURI!=null){voos.add(new GSSSPStatement(subjectType,subjectURI,predicateURI));}
		subjectID=null;
		subjectType=null;
		subjectURI=null;
		predicateURI=null;
	    }
	}
	GSSResSelector grs=new GSSResSelector((String)uriEQConstraints.get(id),(String)uriSWConstraints.get(id),vsos,voos);
	id2rselector.put(id,grs);
	resourceSelectors.add(grs);
    }

    protected void buildLiteralSelector(String id){
	GSSSPStatement st=null;
	Vector oosIDs=(Vector)oos.get(id);
	if (oosIDs!=null && oosIDs.size()>0){
	    //there should at most one objectOfStatement property attached to a Literal selector
	    String oosID=(String)oosIDs.firstElement();
	    String subjectID=(String)xosSubjects.get(oosID);
	    String subjectType=null;
	    String subjectURI=null;
	    if (subjectID!=null){
		subjectType=(String)classCnstrnts.get(subjectID);
		subjectURI=(String)valueCnstrnts.get(subjectID);
	    }
	    String predicateURI=(String)xosPredicates.get(oosID);
	    if (subjectType!=null || subjectURI!=null || predicateURI!=null){st=new GSSSPStatement(subjectType,subjectURI,predicateURI);}
	}
	GSSLitSelector gls=new GSSLitSelector((String)dtCnstrnts.get(id),(String)valueCnstrnts.get(id),st);
	id2lselector.put(id,gls);
	literalSelectors.add(gls);
    }

    protected void buildPropertySelector(String id){
	GSSSOStatement st=null;
	String posID=(String)pos.get(id);
	if (posID!=null){
	    String subjectID=(String)xosSubjects.get(posID);
	    String subjectType=null;
	    String subjectURI=null;
	    if (subjectID!=null){
		subjectType=(String)classCnstrnts.get(subjectID);
		subjectURI=(String)valueCnstrnts.get(subjectID);
	    }
	    String objectID=(String)xosObjects.get(posID);
	    String objectClassType=null;
	    String objectDataType=null;
	    String objectValueOrURI=null;
	    if (objectID!=null){
		objectClassType=(String)classCnstrnts.get(objectID);
		objectDataType=(String)dtCnstrnts.get(objectID);
		objectValueOrURI=(String)valueCnstrnts.get(objectID);
	    }
	    if (subjectType!=null || objectClassType!=null || objectDataType!=null || objectValueOrURI!=null || subjectURI!=null){
		if (objectDataType!=null && objectClassType==null){//constraint on literal objects
		    st=new GSSSOStatement(subjectType,subjectURI,objectDataType,objectValueOrURI,new Boolean(true));
		}
		else if (objectClassType!=null && objectDataType==null){//constraint on resource objects
		    st=new GSSSOStatement(subjectType,subjectURI,objectClassType,objectValueOrURI,new Boolean(false));
		}
		else if (objectClassType==null && objectDataType==null){//in case no information is given on the object's type, say "unknown"
		    st=new GSSSOStatement(subjectType,subjectURI,null,objectValueOrURI,null);
		}
		else {System.err.println("EditableStylesheet.buildPropertySelector(): Error: resource class and literal datatype constraints on the same property selector's statement's object cannot coexist :"+id+" "+objectClassType+" "+objectDataType);editor.reportError=true;}
	    }
	}
	GSSPrpSelector gps=new GSSPrpSelector((String)uriEQConstraints.get(id),(String)uriSWConstraints.get(id),st);
	id2pselector.put(id,gps);
	propertySelectors.add(gps);
    }

    protected void buildRules(){
	Object selectorID;
	Object selector;
	Vector styleList;
	Object visibility;
	Object layout;
	Object ordering;
	for (Enumeration e=id2rselector.keys();e.hasMoreElements();){
	    selectorID=e.nextElement();
	    selector=id2rselector.get(selectorID);
	    if (styleStatements.containsKey(selectorID)){
		styleList=(Vector)styleStatements.get(selectorID);
		if (styleList!=null && styleList.size()>0){
		    rStyleRules.put(selector,styleList);
		}
	    }
	    if (visibilityStatements.containsKey(selectorID)){
		visibility=visibilityStatements.get(selectorID);
		if (visibility!=null){
		    rVisRules.put(selector,visibility);
		}
	    }
	    if (layoutStatements.containsKey(selectorID)){
		layout=layoutStatements.get(selectorID);
		if (layout!=null){
		    rLayoutRules.put(selector,layout);
		}
	    }
	    if (sortStatements.containsKey(selectorID)){
		ordering=sortStatements.get(selectorID);
		if (ordering!=null && (ordering.equals(GraphStylesheet.SORT_BY_NAMESPACE) || ordering.equals(GraphStylesheet.SORT_BY_NAME) || ordering.equals(GraphStylesheet.SORT_BY_NAME_REV) || ordering.equals(GraphStylesheet.SORT_BY_NAMESPACE_REV) || (ordering instanceof String && orderings.containsKey(ordering)))){//check that the rule actually defines a valid ordering declaration (either predefined or enumerated) before adding the rule
		    sortRules.put(selector,ordering);
		}
	    }
	}
	for (Enumeration e=id2lselector.keys();e.hasMoreElements();){
	    selectorID=e.nextElement();
	    selector=id2lselector.get(selectorID);
	    if (styleStatements.containsKey(selectorID)){
		styleList=(Vector)styleStatements.get(selectorID);
		if (styleList!=null && styleList.size()>0){
		    lStyleRules.put(selector,styleList);
		}
	    }
	    if (visibilityStatements.containsKey(selectorID)){
		visibility=visibilityStatements.get(selectorID);
		if (visibility!=null){
		    lVisRules.put(selector,visibility);
		}
	    }
	    if (layoutStatements.containsKey(selectorID)){
		layout=layoutStatements.get(selectorID);
		if (layout!=null){
		    lLayoutRules.put(selector,layout);
		}
	    }
	}
	for (Enumeration e=id2pselector.keys();e.hasMoreElements();){
	    selectorID=e.nextElement();
	    selector=id2pselector.get(selectorID);
	    if (styleStatements.containsKey(selectorID)){
		styleList=(Vector)styleStatements.get(selectorID);
		if (styleList!=null && styleList.size()>0){
		    pStyleRules.put(selector,styleList);
		}
	    }
	    if (visibilityStatements.containsKey(selectorID)){
		visibility=visibilityStatements.get(selectorID);
		if (visibility!=null){
		    pVisRules.put(selector,visibility);
		}
	    }
	    if (layoutStatements.containsKey(selectorID)){
		layout=layoutStatements.get(selectorID);
		if (layout!=null){
		    pLayoutRules.put(selector,layout);
		}
	    }
	}
    }

    /*not used as incremental styling needs these*/
//     void cleanSelectors(){
// 	resourceSelectors.removeAllElements();
// 	literalSelectors.removeAllElements();
// 	propertySelectors.removeAllElements();
// 	resourceSelectors=null;
// 	literalSelectors=null;
// 	propertySelectors=null;
//     }

    public void error(Exception e){
	System.err.println("RDFErrorHandler:Error:GraphStylehseet "+format(e));
	editor.reportError=true;
    }
    
    public void fatalError(Exception e){
	System.err.println("RDFErrorHandler:Fatal Error:GraphStylehseet "+format(e));
	editor.reportError=true;
    }

    public void warning(Exception e){
	System.err.println("RDFErrorHandler:Warning:GraphStylehseet "+format(e));
	editor.reportError=true;
    }

    private static String format(Exception e){
	String msg=e.getMessage();
	if (msg==null){msg=e.toString();}
	if (e instanceof org.xml.sax.SAXParseException){
	    org.xml.sax.SAXParseException spe=(org.xml.sax.SAXParseException)e;
	    return msg + "[Line = " + spe.getLineNumber() + ", Column = " + spe.getColumnNumber() + "]";
	}
	else {
	    return e.toString();
	}
    }

}
