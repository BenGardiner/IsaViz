/*   FILE: GraphStylesheet.java
 *   DATE OF CREATION:   Fri Feb 28 10:09:59 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Aug 07 13:26:49 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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

import org.w3c.IsaViz.fresnel.FSLPath;
import org.w3c.IsaViz.fresnel.FSLNSResolver;

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

public class GraphStylesheet implements RDFErrorHandler {

    public static String _gssNS="http://www.w3.org/2001/11/IsaViz/graphstylesheets#";
    public static String GSS_NAMESPACE_PREFIX="gss";
    protected static String GSS_BASE_URI="";

    //properties defined in the GSS namespace
    //  misc.
    public static String _gssStyle=_gssNS+"style";
    public static String _gssVisibility=_gssNS+"visibility";
    public static String _gssDisplay=_gssNS+"display";
    public static String _gssLayout=_gssNS+"layout";
    public static String _gssSort=_gssNS+"sortPropertiesBy";
    //properties defined in the rdf namespace
    public static String _rdfType=Editor.RDFMS_NAMESPACE_URI+"type";
    public static String _rdfSeq=Editor.RDFMS_NAMESPACE_URI+"Seq";
    //longest commong substring for rdf:_1, rdf:_2, etc.
    public static String _rdfItems=Editor.RDFMS_NAMESPACE_URI+"_";
    
    //selector types
    public static String _gssProperty=_gssNS+"Property";
    public static String _gssResource=_gssNS+"Resource";
    public static String _gssLiteral=_gssNS+"Literal";
    //subject/predicate/object conditions (for selectors)
    public static String _gssSOS=_gssNS+"subjectOfStatement";
    public static String _gssPOS=_gssNS+"predicateOfStatement";
    public static String _gssOOS=_gssNS+"objectOfStatement";
    //disambiguation (for selectors)
    public static String _gssSubject=_gssNS+"subject";
    public static String _gssObject=_gssNS+"object";
    public static String _gssPredicate=_gssNS+"predicate";
    //selector constraints
    public static String _gssURIsw=_gssNS+"uriStartsWith";
    public static String _gssURIeq=_gssNS+"uriEquals";
    public static String _gssValue=_gssNS+"value";
    public static String _gssClass=_gssNS+"class";
    public static String _gssDatatype=_gssNS+"datatype";

    // FSL selector types
    public static String _gssMatchNode = _gssNS + "matchNode";
    public static String _gssMatchArc = _gssNS + "matchArc";

    //  style attributes
    public static String _gssFill=_gssNS+"fill";
    public static String _gssStroke=_gssNS+"stroke";
    public static String _gssStrokeWidth=_gssNS+"stroke-width";
    public static String _gssStrokeDashArray=_gssNS+"stroke-dasharray";
    public static String _gssFontFamily=_gssNS+"font-family";
    public static String _gssFontSize=_gssNS+"font-size";
    public static String _gssFontWeight=_gssNS+"font-weight";
    public static String _gssFontStyle=_gssNS+"font-style";
    public static String _gssShape=_gssNS+"shape";
    public static String _gssTextAlign=_gssNS+"text-align";
    public static String _gssIcon=_gssNS+"icon";  //rectangular bitmap image (mutually exclusive with gss:shape)
    public static String _gssSize = _gssNS + "size";

    //predefined resources in the GSS namespace
    //  visibility and layout predefined values
    public static String _gssShow=_gssNS+"Visible";
    public static String _gssHide=_gssNS+"Hidden";
    public static String _gssNone=_gssNS+"None";
    public static String _gssTable=_gssNS+"Table";
    public static String _gssNodeAndArc=_gssNS+"NodeAndArc";
    public static String _gssSortN=_gssNS+"Name";
    public static String _gssSortNR=_gssNS+"NameReversed";
    public static String _gssSortNS=_gssNS+"Namespace";
    public static String _gssSortNSR=_gssNS+"NamespaceReversed";

    //  wildcards
    public static String _gssPlainLiterals=_gssNS+"PlainLiterals";
    public static String _gssAllDatatypes=_gssNS+"TypedLiterals";

    // predefined shapes
    public static String _gssEllipse=_gssNS+"Ellipse";
    public static String _gssRectangle=_gssNS+"Rectangle";
    public static String _gssRoundRect=_gssNS+"RoundRectangle";
    public static String _gssCircle=_gssNS+"Circle";
    public static String _gssDiamond=_gssNS+"Diamond";
    public static String _gssOctagon=_gssNS+"Octagon";
    public static String _gssTriangleN=_gssNS+"TriangleNorth";
    public static String _gssTriangleS=_gssNS+"TriangleSouth";
    public static String _gssTriangleW=_gssNS+"TriangleWest";
    public static String _gssTriangleE=_gssNS+"TriangleEast";

    // predefined text alignement
    public static String _gssTAAbove=_gssNS+"Above";
    public static String _gssTABelow=_gssNS+"Below";
    public static String _gssTACenter=_gssNS+"Center";
    public static String _gssTALeft=_gssNS+"Left";
    public static String _gssTARight=_gssNS+"Right";

    //predefined dash arrays
    public static String _gssDASolid=_gssNS+"Solid";
    public static String _gssDADashed=_gssNS+"Dashed";
    public static String _gssDADotted=_gssNS+"Dotted";

    // dynamic image specification function
    public static String _gssFetch=_gssNS+"Fetch";

    static final String PERCENT_SIGN = "%";

    public static Integer DISPLAY_NONE=new Integer(0);
    public static Integer VISIBILITY_HIDDEN=new Integer(1);
    public static Integer VISIBILITY_VISIBLE=new Integer(2);

    public static Integer TABLE_FORM=new Integer(0);
    public static Integer NODE_EDGE=new Integer(1);

    public static Integer SORT_BY_NAME=new Integer(0);
    public static Integer SORT_BY_NAME_REV=new Integer(1);
    public static Integer SORT_BY_NAMESPACE=new Integer(2);
    public static Integer SORT_BY_NAMESPACE_REV=new Integer(3);

    public static Integer RES_SEL=new Integer(0);
    public static Integer LIT_SEL=new Integer(1);
    public static Integer PRP_SEL=new Integer(2);


    /*default values*/
    public static Integer DEFAULT_RESOURCE_LAYOUT=NODE_EDGE;
    public static Integer DEFAULT_LITERAL_LAYOUT=NODE_EDGE;
    public static Integer DEFAULT_PROPERTY_LAYOUT=NODE_EDGE;

    public static Integer DEFAULT_RESOURCE_VISIBILITY=VISIBILITY_VISIBLE;
    public static Integer DEFAULT_LITERAL_VISIBILITY=VISIBILITY_VISIBLE;
    public static Integer DEFAULT_PROPERTY_VISIBILITY=VISIBILITY_VISIBLE;

    public static Color DEFAULT_RESOURCE_STROKE=ConfigManager.resourceColorTB;
    public static Color DEFAULT_LITERAL_STROKE=ConfigManager.literalColorTB;
    public static Color DEFAULT_PROPERTY_STROKE=ConfigManager.propertyColorB;

    public static Color DEFAULT_RESOURCE_FILL=ConfigManager.resourceColorF;
    public static Color DEFAULT_LITERAL_FILL=ConfigManager.literalColorF;

    public static Float DEFAULT_RESOURCE_STROKE_WIDTH=new Float(1.0f);
    public static Float DEFAULT_LITERAL_STROKE_WIDTH=new Float(1.0f);
    public static Float DEFAULT_PROPERTY_STROKE_WIDTH=new Float(1.0f);

    public static String DEFAULT_RESOURCE_FONT_FAMILY=Editor.vtmFontName;
    public static String DEFAULT_LITERAL_FONT_FAMILY=Editor.vtmFontName;
    public static String DEFAULT_PROPERTY_FONT_FAMILY=Editor.vtmFontName;

    public static Integer DEFAULT_RESOURCE_FONT_SIZE=new Integer(Editor.vtmFontSize);
    public static Integer DEFAULT_LITERAL_FONT_SIZE=new Integer(Editor.vtmFontSize);
    public static Integer DEFAULT_PROPERTY_FONT_SIZE=new Integer(Editor.vtmFontSize);

    public static Short DEFAULT_RESOURCE_FONT_WEIGHT=new Short(Style.CSS_FONT_WEIGHT_NORMAL);
    public static Short DEFAULT_LITERAL_FONT_WEIGHT=new Short(Style.CSS_FONT_WEIGHT_NORMAL);
    public static Short DEFAULT_PROPERTY_FONT_WEIGHT=new Short(Style.CSS_FONT_WEIGHT_NORMAL);

    public static Short DEFAULT_RESOURCE_FONT_STYLE=new Short(Style.CSS_FONT_STYLE_NORMAL);
    public static Short DEFAULT_LITERAL_FONT_STYLE=new Short(Style.CSS_FONT_STYLE_NORMAL);
    public static Short DEFAULT_PROPERTY_FONT_STYLE=new Short(Style.CSS_FONT_STYLE_NORMAL);

    public static Integer DEFAULT_RESOURCE_SHAPE=Style.ELLIPSE;
    public static Integer DEFAULT_LITERAL_SHAPE=Style.RECTANGLE;

    public static Integer DEFAULT_RESOURCE_TEXT_ALIGN=Style.TA_CENTER;
    public static Integer DEFAULT_LITERAL_TEXT_ALIGN=Style.TA_CENTER;

    public static String _dotCircle="circle";
    public static String _dotEllipse="ellipse";
    public static String _dotRectangle="rectangle";
    public static String _dotDiamond="diamond";
    public static String _dotOctagon="octagon";
    public static String _dotTriangle="triangle";
    public static String _dotInvTriangle="invtriangle";

    public static String gss2dotShape(Object o){
	if (o instanceof Integer){
	    Integer i=(Integer)o;
	    if (i.equals(Style.ELLIPSE)){return _dotEllipse;}
	    else if (i.equals(Style.RECTANGLE)){return _dotRectangle;}
	    else if (i.equals(Style.ROUND_RECTANGLE)){return _dotRectangle;}
	    else if (i.equals(Style.CIRCLE)){return _dotCircle;}
	    else if (i.equals(Style.DIAMOND)){return _dotCircle;}  //not _dotDiamond because it is easier to access the SVG circle size than to compute
	    else if (i.equals(Style.OCTAGON)){return _dotCircle;}  //it for a polygon (same for _dotTriangle, _dot Octagon)
	    else if (i.equals(Style.TRIANGLEN)){return _dotCircle;}
	    else if (i.equals(Style.TRIANGLES)){return _dotCircle;}
	    else if (i.equals(Style.TRIANGLEE)){return _dotCircle;}
	    else if (i.equals(Style.TRIANGLEW)){return _dotCircle;}
	    else if (i.equals(Style.CUSTOM_SHAPE)){return _dotCircle;}  //should not happen
	    else if (i.equals(Style.CUSTOM_POLYGON)){return _dotCircle;}  //should not happen
	    else {return _dotEllipse;} //this is arbitrary (just based on the fact that it is the conventional shape of resources)
	}
	else if (o instanceof CustomShape){return _dotCircle;} //circle is the best general approximation of a VShape
	else if (o instanceof CustomPolygon){return _dotCircle;} //circle is the best general approximation of a VPolygon
	else {return _dotEllipse;} //this is arbitrary (just based on the fact that it is the conventional shape of resources)
    }

    /*print debug information about GSS selectors and styles*/
    protected static boolean DEBUG_GSS=false;
    protected static StringBuffer debugBuffer1=new StringBuffer();  //for rule description
    protected static StringBuffer debugBuffer2=new StringBuffer();  //for rule evaluation

    Editor application;

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

    // FSL namespace prefix binding resolver
    FSLNSResolver nsr;

    private URL stylesheetURL; 

    public GraphStylesheet(){
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

    void load(File f,Editor app,int whichReader){
	application=app;
	try {stylesheetURL=f.toURL();}
	catch (MalformedURLException mue){
	    application.errorMessages.append("GraphStylesheet.load():Error: malformed stylesheet URL for local file: "+f.toString()+"\n");
	    application.reportError=true;
	}
	try {
	    FileInputStream fis=new FileInputStream(f);
	    Model model=ModelFactory.createDefaultModel();
	    Editor.tblp.setSTPBValue(5);
	    RDFReader parser;
	    if (whichReader==RDFLoader.N3_READER){
		parser=model.getReader(RDFLoader.N3);
		parser.setProperty(RDFLoader.errorModePropertyName,"lax");
	    }
	    else if (whichReader==RDFLoader.NTRIPLE_READER){
		parser=model.getReader(RDFLoader.NTRIPLE);
		//no error mode decl as it throws an exception
	    }
	    else {
		parser=model.getReader(RDFLoader.RDFXMLAB);
		parser.setProperty(RDFLoader.errorModePropertyName,"lax");
	    }
	    parser.setErrorHandler(this);
	    Editor.tblp.setSTPBValue(10);
	    parser.read(model,fis,GraphStylesheet.GSS_BASE_URI);
	    application.declareNSBindings(model.getNsPrefixMap(),model.listNameSpaces());
	    Editor.tblp.setSTPBValue(40);
	    processStatements(model.listStatements());
	}
	catch (Exception ex){
	    String message="RDFErrorHandler:Warning:GraphStylehseet "+format(ex);
	    application.errorMessages.append(message+"\n");
	    application.reportError=true;
	}
    }

    void load(java.net.URL url,Editor app,int whichReader){
	application=app;
	stylesheetURL=url;
	try {
	    Model model=ModelFactory.createDefaultModel();
	    Editor.tblp.setSTPBValue(5);
	    RDFReader parser;
	    if (whichReader==RDFLoader.N3_READER){
		parser=model.getReader(RDFLoader.N3);
		parser.setProperty(RDFLoader.errorModePropertyName,"lax");
	    }
	    else if (whichReader==RDFLoader.NTRIPLE_READER){
		parser=model.getReader(RDFLoader.NTRIPLE);
		//no error mode decl as it throws an exception
	    }
	    else {
		parser=model.getReader(RDFLoader.RDFXMLAB);
		parser.setProperty(RDFLoader.errorModePropertyName,"lax");
	    }
	    parser.setErrorHandler(this);
	    Editor.tblp.setSTPBValue(10);
	    parser.read(model,url.toString());
	    application.declareNSBindings(model.getNsPrefixMap(),model.listNameSpaces());
	    Editor.tblp.setSTPBValue(40);
	    processStatements(model.listStatements());
	}
	catch (Exception ex){
	    String message="RDFErrorHandler:Warning:GraphStylehseet "+format(ex);
	    application.errorMessages.append(message+"\n");
	    application.reportError=true;
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
	// build an FSLNSResolver from the namespace prefix bindings declared in IsaViz's main list
	// (any new binding will have been added through the call to application.declareNSBindings in the load() methods)
	nsr = new FSLNSResolver();
	for (int i=0;i<application.tblp.nsTableModel.getRowCount();i++){
	    nsr.addPrefixBinding((String)application.tblp.nsTableModel.getValueAt(i,0),
				 (String)application.tblp.nsTableModel.getValueAt(i,1));
	}
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
	catch (Exception ex){System.err.println("GraphStylesheet.processStatements: Error: ");ex.printStackTrace();}
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
	if (DEBUG_GSS){debug();TextViewer tv=new TextViewer(debugBuffer1,"GSS Debugger - Stylesheet Rules",0,true);}
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
	if (pURI.equals(_gssStyle)){rememberStyleRule(sURI,oURI);}
	else if (pURI.equals(_gssVisibility)){rememberVisRule(sURI,oURI);}
	else if (pURI.equals(_gssDisplay)){rememberVisRule(sURI,oURI);}
	else if (pURI.equals(_gssLayout)){rememberLayoutRule(sURI,oURI);}
	else if (pURI.equals(_gssURIeq)){declareURIeqConstraint(sURI,oURI);}
	else if (pURI.equals(_gssURIsw)){declareURIswConstraint(sURI,oURI);}
	else if (pURI.equals(_gssSOS)){declareSOS(sURI,oURI);}
	else if (pURI.equals(_gssPOS)){declarePOS(sURI,oURI);}
	else if (pURI.equals(_gssOOS)){declareOOS(sURI,oURI);}
	else if (pURI.equals(_rdfType)){declareSelectorTypeOrCustomOrdering(sURI,oURI);}
	else if (pURI.equals(_gssSubject)){declareXOSSubject(sURI,oURI);}
	else if (pURI.equals(_gssPredicate)){declareXOSPredicate(sURI,oURI);}
	else if (pURI.equals(_gssObject)){declareXOSObject(sURI,oURI);}
	else if (pURI.equals(_gssValue)){declareValueConstraint(sURI,oURI);}
	else if (pURI.equals(_gssClass)){declareClassConstraint(sURI,oURI);}
	else if (pURI.equals(_gssDatatype)){declareDatatypeConstraint(sURI,oURI);}
	else if (pURI.equals(_gssShape)){addPredefShapeAttributeToStyle(sURI,oURI);}  //a predefined shape
	else if (pURI.equals(_gssIcon)){addIconAttributeToStyle(sURI,oURI);}
	else if (pURI.equals(_gssTextAlign)){addTextAlignAttributeToStyle(sURI,oURI);}
	else if (pURI.equals(_gssSort)){rememberSortRule(sURI,oURI);}
	else if (pURI.equals(_gssStrokeDashArray)){addStrokeDashArrayToStyle(sURI,oURI);}
	else if (pURI.startsWith(_rdfItems)){addItemToCustomOrdering(sURI,pURI,oURI);}
    }

    protected void processStatement(Resource s,Resource p,Literal o){
	//s could be a b-node or named resource (URI)
	String sURI=(s.isAnon()) ? s.getId().toString() : s.toString();
	String pURI=p.getURI();
        StringBuffer oValue=new StringBuffer(o.getLexicalForm());
        Utils.delLeadingAndTrailingSpaces(oValue);
	if (pURI.equals(_gssMatchNode)){createFSLNodeSelector(sURI, oValue.toString());}
	else if (pURI.equals(_gssMatchArc)){createFSLArcSelector(sURI, oValue.toString());}
	else if (pURI.equals(_gssFill)){addFillAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(_gssStroke)){addStrokeAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(_gssStrokeWidth)){addStrokeWAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(_gssValue)){declareValueConstraint(sURI,oValue.toString());}
	//a custom shape or polygon, following the VShape model or VPolygon
	else if (pURI.equals(_gssShape)){addShapeOrPolygonAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(_gssSize)){addSizeToStyle(sURI, oValue.toString());}
	else if (pURI.equals(_gssStrokeDashArray)){addStrokeDashArrayToStyle(sURI,oValue.toString());}
	else if (pURI.equals(_gssFontWeight)){addFontWAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(_gssFontStyle)){addFontStAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(_gssFontFamily)){addFontFAttributeToStyle(sURI,oValue.toString());}
	else if (pURI.equals(_gssFontSize)){addFontSzAttributeToStyle(sURI,oValue.toString());}
	//present here too as the value of property uriStarstWith can be a literal
	else if (pURI.equals(_gssURIsw)){declareURIswConstraint(sURI,oValue.toString());}
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
	if (visibility.equals(_gssHide)){
	    visibilityStatements.put(selector,VISIBILITY_HIDDEN);
	}
	else if (visibility.equals(_gssNone)){
	    visibilityStatements.put(selector,DISPLAY_NONE);
	}
	else if (visibility.equals(_gssShow)){
	    visibilityStatements.put(selector,VISIBILITY_VISIBLE);
	}
	else {//default - should add an error message too (illegal value, as range of property gss:visibility is {gss:Show,gss:Hide})
	    visibilityStatements.put(selector,VISIBILITY_VISIBLE);
	}
    }

    protected void rememberLayoutRule(String selector,String layout){//layout=gss:TableForm || gss:EdgeAndNode
	if (layout.equals(_gssTable)){
	    layoutStatements.put(selector,TABLE_FORM);
	}
	else if (layout.equals(_gssNodeAndArc)){
	    layoutStatements.put(selector,NODE_EDGE);
	}
	else {//default - should add an error message too (illegal value, as range of property gss:layout is {gss:TableForm,gss:EdgeAndNode})
	    layoutStatements.put(selector,NODE_EDGE);
	}
    }

    protected void rememberSortRule(String selector,String sortNode){//selector should be an anon ID, sortNode either a Custom Sort ID or one of {gss:Name, gss:NameReversed, gss:Namespace, gss:NamespaceReversed}
	if (sortNode.equals(_gssSortN)){
	    sortStatements.put(selector,SORT_BY_NAME);
	}
	else if (sortNode.equals(_gssSortNR)){
	    sortStatements.put(selector,SORT_BY_NAME_REV);
	}
	else if (sortNode.equals(_gssSortNS)){
	    sortStatements.put(selector,SORT_BY_NAMESPACE);
	}
	else if (sortNode.equals(_gssSortNSR)){
	    sortStatements.put(selector,SORT_BY_NAMESPACE_REV);
	}
	else {//Custom ordering
	    sortStatements.put(selector,sortNode);
	}
    }

    protected void createFSLNodeSelector(String rule, String fslExpr){
	FSLPath p = FSLPath.pathFactory(fslExpr, nsr, FSLPath.NODE_STEP);
    }

    protected void createFSLArcSelector(String rule, String fslExpr){
	FSLPath p = FSLPath.pathFactory(fslExpr, nsr, FSLPath.ARC_STEP);
    }

    protected void declareSelectorTypeOrCustomOrdering(String selector,String type){
	if (type.equals(_gssResource)){//declaring a resource selector
	    selectorTypes.put(selector,RES_SEL);
	}
	else if (type.equals(_gssProperty)){//declaring a property selector
	    selectorTypes.put(selector,PRP_SEL);
	}
	else if (type.equals(_gssLiteral)){//declaring a literal selector
	    selectorTypes.put(selector,LIT_SEL);
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
	    if (DEBUG_GSS){debugBuffer1.append("Error: Style "+styleID+" declares both gss:shape and gss:icon properties which are mutually exclusive. The gss:icon property will be ignored.\n");}
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
	else {debugBuffer1.append("GraphStylesheet.addShapeOrPolygonAttributeToStyle:error: syntax error in shape or polygon definition "+s+"\n");}
	if (st.getIcon()!=null){
	    if (DEBUG_GSS){debugBuffer1.append("Error: Style "+styleID+" declares both gss:shape and gss:icon properties which are mutually exclusive. The gss:icon property will be ignored.\n");}
	}
    }

    protected void addIconAttributeToStyle(String styleID,String iconURL){
	Style st=createAndGetStyle(styleID);
	if (st.getShape()==null){
	    if (iconURL.equals(_gssFetch)){//dynamic image
		try {st.setIcon(new URL(_gssFetch));}//this is statically defined, so it should never throw the exception unless its value is changed
		catch (MalformedURLException ex){debugBuffer1.append("Error: Style: Malformed URL "+iconURL+"\n");ex.printStackTrace();}
	    }
	    else {
		URL absoluteURL=Utils.getAbsoluteURL(iconURL,stylesheetURL);
		if (absoluteURL!=null){
		    //storeIcon() returns true only if the ImageIcon could be retrieved, instantiated and stored
		    if (application.gssMngr.storeIcon(absoluteURL)){st.setIcon(absoluteURL);}
		}
		else {debugBuffer1.append("Error: Style "+styleID+" declares a (possibly malformed) URL value for property gss:icon:\n"+iconURL+"\nwhich could not be interpreted against the stylesheet document base URL:\n"+stylesheetURL+"\n");}
	    }
	}
	else {//no need to set the icon as it will be ignored when applying the style
	    if (DEBUG_GSS){debugBuffer1.append("Error: Style "+styleID+" declares both gss:shape and gss:icon properties which are mutually exclusive. The gss:icon property will be ignored.\n");}
	}
    }

    protected void addSizeToStyle(String styleID, String size){
	Style st = createAndGetStyle(styleID);
	if (size.endsWith(PERCENT_SIGN)){
	    st.setRelativeSize(size.substring(0, size.length()-1));
	}
	else {
	    if (DEBUG_GSS){debugBuffer1.append("Error: Style "+styleID+" declares a size property value not expressed as a percentage:'"+size+"'. The gss:size property will be ignored.\n");}
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
	    if (selType.equals(RES_SEL)){
		buildResourceSelector(selID);
	    }
	    else if (selType.equals(LIT_SEL)){
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
		    else {System.err.println("GraphStylesheet.buildResourceSelector(): Error: resource class and literal datatype constraints on the same resource selector's statement's object cannot coexist :"+id+" "+objectClassType+" "+objectDataType);}
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
		else {System.err.println("GraphStylesheet.buildPropertySelector(): Error: resource class and literal datatype constraints on the same property selector's statement's object cannot coexist :"+id+" "+objectClassType+" "+objectDataType);}
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
		if (ordering!=null && (ordering.equals(SORT_BY_NAMESPACE) || ordering.equals(SORT_BY_NAME) || ordering.equals(SORT_BY_NAME_REV) || ordering.equals(SORT_BY_NAMESPACE_REV) || (ordering instanceof String && orderings.containsKey(ordering)))){//check that the rule actually defines a valid ordering declaration (either predefined or enumerated) before adding the rule
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

    /*returns a list of all GSSResSelectors that match this resource, sorted by weight (biggest one first)*/
    Vector evaluateRules(IResource r){
	Vector res=new Vector();
	Vector tmpRes=new Vector();
	GSSResSelector grs;
	//evaluate all resource selectors against this resource and keep only the ones matching it
	if (resourceSelectors!=null){
	    for (int i=0;i<resourceSelectors.size();i++){
		grs=(GSSResSelector)resourceSelectors.elementAt(i);
		if (grs.selects(r)){tmpRes.add(grs);}
	    }
	}
	//order the matching selectors by weight (biggest one first)
	Object[] tmpRes2=new Object[tmpRes.size()];
	for (int i=0;i<tmpRes.size();i++){
	    tmpRes2[i]=tmpRes.elementAt(i);
	}
	java.util.Arrays.sort(tmpRes2,new GSSSelectorComparator());
	for (int i=0;i<tmpRes2.length;i++){
	    res.add(tmpRes2[i]);
	}
	if (DEBUG_GSS){debugWeight(r,res);}
	return res;
    }

    /*returns a list of all GSSPrpSelectors that match this property, sorted by weight (biggest one first)*/
    Vector evaluateRules(IProperty p){
	Vector res=new Vector();
	Vector tmpRes=new Vector();
	GSSPrpSelector gps;
	//evaluate all property selectors against this resource and keep only the ones matching it
	if (propertySelectors!=null){
	    for (int i=0;i<propertySelectors.size();i++){
		gps=(GSSPrpSelector)propertySelectors.elementAt(i);
		if (gps.selects(p)){tmpRes.add(gps);}
	    }
	}
	//order the matching selectors by weight (biggest one first)
	Object[] tmpRes2=new Object[tmpRes.size()];
	for (int i=0;i<tmpRes.size();i++){
	    tmpRes2[i]=tmpRes.elementAt(i);
	}
	java.util.Arrays.sort(tmpRes2,new GSSSelectorComparator());
	for (int i=0;i<tmpRes2.length;i++){
	    res.add(tmpRes2[i]);
	}
	if (DEBUG_GSS){debugWeight(p,res);}
	return res;
    }

    /*returns a list of all GSSLitSelectors that match this literal, sorted by weight (biggest one first)*/
    Vector evaluateRules(ILiteral l){
	Vector res=new Vector();
	Vector tmpRes=new Vector();
	GSSLitSelector gls;
	//evaluate all literal selectors against this resource and keep only the ones matching it
	if (literalSelectors!=null){
	    for (int i=0;i<literalSelectors.size();i++){
		gls=(GSSLitSelector)literalSelectors.elementAt(i);
		if (gls.selects(l)){tmpRes.add(gls);}
	    }
	}
	//order the matching selectors by weight (biggest one first)
	Object[] tmpRes2=new Object[tmpRes.size()];
	for (int i=0;i<tmpRes.size();i++){
	    tmpRes2[i]=tmpRes.elementAt(i);
	}
	java.util.Arrays.sort(tmpRes2,new GSSSelectorComparator());
	for (int i=0;i<tmpRes2.length;i++){
	    res.add(tmpRes2[i]);
	}
	if (DEBUG_GSS){debugWeight(l,res);}
	return res;
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
	String message="RDFErrorHandler:Error:GraphStylehseet "+format(e);
	application.errorMessages.append(message+"\n");
	application.reportError=true;
    }
    
    public void fatalError(Exception e){
	String message="RDFErrorHandler:Fatal Error:GraphStylehseet "+format(e);
	application.errorMessages.append(message+"\n");
	application.reportError=true;
    }

    public void warning(Exception e){
	String message="RDFErrorHandler:Warning:GraphStylehseet "+format(e);
	application.errorMessages.append(message+"\n");
	application.reportError=true;
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

    public void debug(){
	Object key;
	debugBuffer1.append("------------------------- Styles -------------------------\n");
	for (Enumeration e=styles.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append(styles.get(key).toString()+"\n");
	}
	debugBuffer1.append("-------------------- Sorting Enumerations ----------------\n");
	for (Enumeration e=orderings.keys();e.hasMoreElements();){
	    key=orderings.get(e.nextElement());
	    if (key instanceof CustomOrdering){debugBuffer1.append(key.toString());}
	}
	debugBuffer1.append("--------------------- Resource Styling -------------------\n");
	for (Enumeration e=rStyleRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t "+Utils.vectorOfStringAsCSStrings((Vector)rStyleRules.get(key))+"\n");
	}
	debugBuffer1.append("--------------------- Property Styling -------------------\n");
	for (Enumeration e=pStyleRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t "+Utils.vectorOfStringAsCSStrings((Vector)pStyleRules.get(key))+"\n");
	}
	debugBuffer1.append("--------------------- Literal Styling  -------------------\n");
	for (Enumeration e=lStyleRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t "+Utils.vectorOfStringAsCSStrings((Vector)lStyleRules.get(key))+"\n");
	}
	debugBuffer1.append("------------------- Resource Visibility ------------------\n");
	for (Enumeration e=rVisRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t (0=display_none,1=visibility_hidden,2=visibility_visible) = "+rVisRules.get(key).toString()+"\n");
	}
	debugBuffer1.append("------------------- Property Visibility ------------------\n");
	for (Enumeration e=pVisRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t (0=display_none,1=visibility_hidden,2=visibility_visible) = "+pVisRules.get(key).toString()+"\n");
	}
	debugBuffer1.append("------------------- Literal Visibility  ------------------\n");
	for (Enumeration e=lVisRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t (0=display_none,1=visibility_hidden,2=visibility_visible) = "+lVisRules.get(key).toString()+"\n");
	}
	debugBuffer1.append("--------------------- Resource Layout --------------------\n");
	for (Enumeration e=rLayoutRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t (0=table_form,1=node_edge) = "+rLayoutRules.get(key).toString()+"\n");
	}
	debugBuffer1.append("--------------------- Property Layout --------------------\n");
	for (Enumeration e=pLayoutRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t (0=table_form,1=node_edge) = "+pLayoutRules.get(key).toString()+"\n");
	}
	debugBuffer1.append("--------------------- Literal Layout  --------------------\n");
	for (Enumeration e=lLayoutRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t (0=table_form,1=node_edge) = "+lLayoutRules.get(key).toString()+"\n");
	}
	debugBuffer1.append("----- Property sorting (for resource selectors) ----------\n0=by name,1=by name (reversed),2=by namespace,3=by namespace (reversed), or a sortID refering to an explicit enumeration of properties\n");
	Object value;
	for (Enumeration e=sortRules.keys();e.hasMoreElements();){
	    key=e.nextElement();
	    debugBuffer1.append("-- "+key.getClass().getName()+"@"+Integer.toHexString(key.hashCode())+"\n");
	    debugBuffer1.append(key.toString());
	    debugBuffer1.append("\t"+sortRules.get(key).toString()+"\n");
	}
    }

    void debugWeight(INode n,Vector v){
	if (v.size()>0){
	    Object o;
	    debugBuffer2.append("Selector list (selector object[weight]) for "+n.toString()+"\n");
	    for (int i=0;i<v.size()-1;i++){
		o=v.elementAt(i);
		debugBuffer2.append(o.getClass().getName()+"@"+Integer.toHexString(o.hashCode())+"["+((GSSSelector)o).getWeight()+"],");
	    }
	    o=v.lastElement();
	    debugBuffer2.append(o.getClass().getName()+"@"+Integer.toHexString(o.hashCode())+"["+((GSSSelector)o).getWeight()+"]\n");
	}
	else debugBuffer2.append("No selector for "+n.toString()+"\n");
    }

}
