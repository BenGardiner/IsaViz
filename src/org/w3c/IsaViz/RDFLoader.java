/*   FILE: RDFLoader.java
 *   DATE OF CREATION:   10/19/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Mon Jun 13 10:49:02 2005 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   $Id: RDFLoader.java,v 1.43 2007/03/21 13:10:16 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Point;
import java.awt.Font;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;

import org.w3c.dom.*;

import com.xerox.VTM.svg.SVGReader;
import com.xerox.VTM.svg.SVGWriter;
import com.xerox.VTM.glyphs.*;
import com.xerox.VTM.engine.LongPoint;

//import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.datatypes.RDFDatatype;

/*in charge of loading, parsing  and serializing RDF files (using Jena/ARP)*/

public class RDFLoader implements RDFErrorHandler {

    // Name for the DOT file title
    private static final String DOT_TITLE = "dotfile";

    public static final int RDF_XML_READER = 0;
    public static final int NTRIPLE_READER = 1;
    public static final int N3_READER = 2;

    public static final String RDFXML = "RDF/XML";
    public static final String RDFXMLAB = "RDF/XML-ABBREV";
    public static final String NTRIPLE = "N-TRIPLE";
    public static final String N3 = "N3";

    public static final Vector formatList;
    public static final String formatRDFXML = "RDF/XML";
    public static final String formatNTRIPLES = "N-Triples";
    public static final String formatN3 = "Notation3";

    static {
	formatList=new Vector();
	formatList.add(formatRDFXML);
	formatList.add(formatNTRIPLES);
	formatList.add(formatN3);
    }

    public static final String errorModePropertyName = "http://jena.hpl.hp.com/arp/properties/error-mode";
    public static String errorModePropertyValue = "default";

    Editor application;

    //RDF parser
    RDFReader parser;

    File rdfF;
    java.net.URL rdfU;
    File dotF;
    File svgF;
    boolean dltOnExit;

    private static final String RESOURCE_MAPID_PREFIX="R_";
    private static final String LITERAL_MAPID_PREFIX="L_";
    private static final String PROPERTY_MAPID_PREFIX="P_";
    private static final String TF_PROPERTY_MAPID_PREFIX="F_";
    private static final String STRUCT_MAPID_PREFIX="S_";
    private static final String STRUCT_PREFIX="struct";

    StringBuffer nextNodeID;
    StringBuffer nextEdgeID;
    StringBuffer nextStructID;  //for table form layout (structID is used for records)
    StringBuffer nextTFEdgeID;  //for table form layout (TFEdgeID is used for the (shared) arrow pointing to records)

    Integer literalLbNum;   //used to count literals and generate tmp literal labels in the DOT file (only in styled import for now)

    Hashtable tfMapID2pvPairs; /*used to keep track of what DOT record is associated to what property/value pairs (temp structure cleaned by cleanMapIDs)
				 the structure is as follows:
				 key=String representing a structMapID (associated with a DOT record=the outer rectangle polygon in SVG)
				 value=a vector containing all property/value pairs laid out in table form and associated with the specific
				 resource which is the subject of statements in this rectangle
				 each pair is itself a Vector with 2 components:
				     -1st one is the subjct (IResource),
				     -2nd one is the statement (IProperty),
				     -3rd one is the object (either a IResource or a ILiteral)
			       */

    /*label=hackReplacementText is used throughout the dot generation functions in combination with fixedsize=true
      *in order to prevent the dot process from issuing warnings that the label is too big on the command line, which 
      *somehow causes the process not to terminate from the point of view of java.lang.Runtime when calling Process.waitFor().
      *The fact that the label is not the actual label at the time of layout computation does not really matter as this is
      *only used in the case of shapes with a fixed size (i.e. whose width is not affected by the label's width). The correct
      *label is anyway assigned later (replace hackReplacementText by the actual node identity/value) when loading the SVG 
      *representation and mapping it to the internal model. This is a hack, and it might no longer be necessary with future 
      *versions of dot (post 1.9.0) as I have requested that they add a "silent" command line option. However, using this hack
      *makes it possible to use versions 1.8.x and 1.9.0 of GraphViz.
      */
    private static String hackReplacementText=".is";

    RDFLoader(Editor e){
	application=e;
	nextNodeID=new StringBuffer("0");
	nextEdgeID=new StringBuffer("0");
	nextTFEdgeID=new StringBuffer("0");
	nextStructID=new StringBuffer("0");
    }

    void reset(){
	rdfF=null;
	rdfU=null;
	dotF=null;
	svgF=null;
	nextNodeID=new StringBuffer("0");
	nextEdgeID=new StringBuffer("0");
	nextTFEdgeID=new StringBuffer("0");
	nextStructID=new StringBuffer("0");
    }

    void initParser(int i,Model model){//i==0 means we are reading RDF/XML, i==1 means we are reading NTriples, 2==Notation3
	try {
	    //property name/value pairs are defined in 
	    //www.hpl.hp.com/semweb/javadoc/com/hp/hpl/jena/rdf/arp/JenaReader.html#setProperty(java.lang.String, java.lang.Object)
	    if (ConfigManager.PARSING_MODE==ConfigManager.STRICT_PARSING){errorModePropertyValue="strict";}
	    else if (ConfigManager.PARSING_MODE==ConfigManager.LAX_PARSING){errorModePropertyValue="lax";}
	    else {errorModePropertyValue="default";}
	    if (i==RDF_XML_READER){
		parser=model.getReader(RDFXMLAB);
		parser.setErrorHandler(this);
		parser.setProperty(errorModePropertyName,errorModePropertyValue);
	    }
	    else if (i==NTRIPLE_READER){
		parser=model.getReader(NTRIPLE);
		parser.setErrorHandler(this);
		//parser.setProperty(errorModePropertyName,errorModePropertyValue);
	    }
	    else if (i==N3_READER){
		parser=model.getReader(N3);
		parser.setErrorHandler(this);
		parser.setProperty(errorModePropertyName,errorModePropertyValue);
	    }
	}
	catch (RDFException ex){System.err.println("Error: RDFLoader.initParser(): ");ex.printStackTrace();}
    }

    void load(Object o,int whichReader){//o can be a java.net.URL, a java.io.File or a java.io.InputStream (plug-ins)
	//whichReader=0 if RDF/XML, 1 if NTriples, 2 if N3
	ProgPanel pp=new ProgPanel("Resetting...","Loading RDF");
	PrintWriter pw=null;
	try {
	    pp.setPBValue(5);
	    //application.rdfModel=new ModelMem();
	    application.rdfModel = ModelFactory.createDefaultModel();
	    if (o instanceof File){
		rdfF=(File)o;
		FileInputStream fis=new FileInputStream(rdfF);
		pp.setLabel("Loading local file "+rdfF.toString()+" ...");
		pw=createDOTFile();
		pp.setPBValue(10);
		pp.setLabel("Initializing ARP(Jena) ...");
		initParser(whichReader,application.rdfModel);
		pp.setPBValue(20);
		pp.setLabel("Parsing RDF ...");
		Editor.BASE_URI=rdfF.toURL().toString();
		parser.read(application.rdfModel,fis,Editor.BASE_URI); //the file's URL will serve as the base URI
	    }
	    else if (o instanceof java.net.URL){
		rdfU=(java.net.URL)o;
		pp.setLabel("Loading remote file "+rdfU.toString()+" ...");
		pw=createDOTFile();
		pp.setPBValue(10);
		pp.setLabel("Initializing ARP(Jena) ...");
		initParser(whichReader,application.rdfModel);
		pp.setPBValue(20);
		pp.setLabel("Parsing RDF ...");
		Editor.BASE_URI=rdfU.toString();
		parser.read(application.rdfModel,rdfU.toString());
	    }
	    else if (o instanceof java.io.InputStream){
		pp.setLabel("Reading stream ...");
		pw=createDOTFile();
		pp.setPBValue(10);
		pp.setLabel("Initializing ARP(Jena) ...");
		initParser(whichReader,application.rdfModel);
		pp.setPBValue(20);
		pp.setLabel("Parsing RDF ...");
		parser.read(application.rdfModel,(InputStream)o,Editor.BASE_URI);
	    }
	    application.declareNSBindings(application.rdfModel.getNsPrefixMap(),application.rdfModel.listNameSpaces());
	    SH sh=new SH(pw,application);
	    StmtIterator it=application.rdfModel.listStatements();
	    Statement st;
	    while (it.hasNext()){
		st=it.nextStatement();
		if (st.getObject() instanceof Resource){sh.statement(st.getSubject(),st.getPredicate(),(Resource)st.getObject());}
		else if (st.getObject() instanceof Literal){sh.statement(st.getSubject(),st.getPredicate(),(Literal)st.getObject());}
		else {System.err.println("Error: RDFLoader.load(): unknown kind of object: "+st.getObject());}
	    }
	    it.close();
	    pp.setPBValue(50);
	    pp.setLabel("Creating temporary SVG file ...");
	    svgF=Utils.createTempFile(Editor.m_TmpDir.toString(),"isv",".svg");
	    pp.setPBValue(60);
	    pp.setLabel("Calling GraphViz (this can take several minutes) ...");
	    callDOT(pw);
	    pp.setPBValue(80);
	    pp.setLabel("Parsing SVG ...");
	    displaySVG(application.xmlMngr.parse(svgF,false));
	    cleanMapIDs();//the mapping between SVG and RDF has been done -  we do not need these any longer
	    application.cfgMngr.assignColorsToGraph();
	    application.showAnonIds(ConfigManager.SHOW_ANON_ID);  //show/hide IDs of anonymous resources
	    application.showResourceLabels(Editor.DISP_AS_LABEL);
	    pp.setPBValue(100);
	    pp.setLabel("Deleting temporary files ...");
	    if (Editor.dltOnExit){deleteFiles();}
	    Editor.vsm.getGlobalView(Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0),ConfigManager.ANIM_DURATION);
	    application.centerRadarView();
	    application.rdfModel=null; //get rid of it at this point - we will generate it only when appropriate (for instance when exporting)
	}
	catch (IOException ex){application.errorMessages.append("RDFLoader.load() "+ex+"\n");application.reportError=true;}
	catch (RDFException ex2){application.errorMessages.append("RDFLoader.load() "+ex2+"\n");application.reportError=true;}
	catch (Exception ex3){application.errorMessages.append("RDFLoader.load() "+ex3+"\nPlease verify your directory preferences (GraphViz/DOT might not be configured properly), your default namespace and anonymous node prefix declarations");application.reportError=true;

	    ex3.printStackTrace();

	}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
	pp.destroy();
    }

    Model merge(Object o,int whichReader){//o can be a java.net.URL, a java.io.File or a java.io.InputStream (plug-ins)
	//whichReader=0 if RDF/XML, 1 if NTriples, 2 if N3
	//Model res = new ModelMem();
	Model res = ModelFactory.createDefaultModel();
	if (o instanceof File){
	    try {
		FileInputStream fis=new FileInputStream((File)o);
		initParser(whichReader,application.rdfModel);
		//not setting Editor.BASE_URI as we do not want to overwrite it (this is a merge operation)
		parser.read(res,fis,((File)o).toURL().toString());
	    }
	    catch (IOException ex){application.errorMessages.append("RDFLoader.merge() (File) "+ex+"\n");application.reportError=true;}
	    catch (RDFException ex2){application.errorMessages.append("RDFLoader.merge() (File) "+ex2+"\n");application.reportError=true;}
	}
	else if (o instanceof java.net.URL){
	    java.net.URL tmpURL=(java.net.URL)o;
	    try {
		parser.read(res,tmpURL.toString());
	    }
	    catch (RDFException ex){application.errorMessages.append("RDFLoader.merge() (URL) "+ex+"\n");application.reportError=true;}
	}
	else if (o instanceof InputStream){
	    try {
		initParser(whichReader,application.rdfModel);
		parser.read(res,(InputStream)o,Editor.BASE_URI);
	    }
	    catch (RDFException ex){application.errorMessages.append("RDFLoader.merge() (InputStream) "+ex+"\n");application.reportError=true;}
	}
	return res;
    }

    void loadProperties(File f){
	try {
	    FileInputStream fis=new FileInputStream(f);
	    //Model tmpModel=new ModelMem();
	    Model tmpModel = ModelFactory.createDefaultModel();
	    initParser(RDF_XML_READER,tmpModel);
	    parser.read(tmpModel,fis,f.toURL().toString());
	    application.declareNSBindings(tmpModel.getNsPrefixMap(),tmpModel.listNameSpaces());
	    StmtIterator it=tmpModel.listStatements();
	    Property prd;
	    while (it.hasNext()){
		prd=it.nextStatement().getPredicate();
		application.addPropertyType(prd.getNameSpace(),prd.getLocalName(),true);
	    }
	    tmpModel=null;
	}
	catch (IOException ex){application.errorMessages.append("RDFLoader.loadProperties() "+ex+"\n");application.reportError=true;}
	catch (RDFException ex){application.errorMessages.append("RDFLoader.loadProperties() "+ex+"\n");application.reportError=true;}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
    }

    void deleteFiles(){
	if (dotF!=null){dotF.delete();}
	if (svgF!=null){svgF.delete();}
    }

    PrintWriter createDOTFile(){
	dotF=initGraphFile();
	PrintWriter pw=null;
	if (dotF==null){return null;} // Assume error has been reported
	// Create a PrintWriter for the DOT handler
	try {
	    OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream(dotF),ConfigManager.ENCODING);
	    if (osw!=null){pw=new PrintWriter(osw);}
	    if (pw!=null){processDOTParameters(pw);}  // Add the graph header
	}
	catch (IOException ex){application.errorMessages.append("RDFLoader.createDOTFile() "+ex+"\n");application.reportError=true;}
	return pw;
    }

    //create a temporary file for DOT input
    File initGraphFile(){
        try {
            // Stop if any of the parameters are missing
	    if (Editor.m_TmpDir==null || Editor.m_GraphVizPath==null){
                // Put the paths in a comment in the returned content
		application.errorMessages.append("Temporary DOT file initialization failed\n");
                application.errorMessages.append("TMP_DIR = " + Editor.m_TmpDir+"\n");
                application.errorMessages.append("GRAPH_VIZ_PATH = " + Editor.m_GraphVizPath+"\n");
		application.reportError=true;
                return null;
            }
	}
	catch (Exception e) {application.errorMessages.append("Unable to create a temporary graph file. A graph cannot be generated.\n");application.reportError=true;return null;}
        File f=null;
	// Must generate a unique file name that the DOT handler will use 
	f=Utils.createTempFile(Editor.m_TmpDir.toString(),"isv",".dot");
	if (f == null){
	    application.errorMessages.append("Failed to create a temporary graph file. A graph cannot be generated.\n");
	    application.reportError=true;
	    return null;
	}
	return f;
    }

    void processDOTParameters(PrintWriter pw){
        pw.println("digraph "+DOT_TITLE+" {");  // Print the graph header
        String nodeColor="black";
        String nodeTextColor="black";
	String nodeFillColor="white";
        String edgeColor="black";
        String edgeTextColor="black";
        // Orientation must be either TB or LR
        String orientation=Editor.GRAPH_ORIENTATION;
        // Add an attribute for all of the graph's nodes
        pw.println("node [fontname=\""+Editor.vtmFontName+"\",fontsize=" +Editor.vtmFontSize+",color="+nodeColor+",fillcolor="+nodeFillColor+",fontcolor="+nodeTextColor+"];");
        // Add an attribute for all of the graph's edges
        pw.println("edge [fontname=\""+Editor.vtmFontName+"\",fontsize=" +Editor.vtmFontSize+",color="+edgeColor+",fontcolor="+edgeTextColor+"];");
        // Add an attribute for the orientation
        pw.println("rankdir="+orientation+";");
    }

    /*
     * Generate a graph of the RDF data model
     *
     *@param out the servlet's output stream
     *@param pw the graph file's PrintWriter
     *@param dotFile the File handle for the graph file
     *@param rdf the RDF text
     */
    private void callDOT(PrintWriter pw){
        try {
            pw.println("}"); // Add the graph footer
            pw.close();  // Close the DOT input file so the GraphViz can open and read it
            // Pass the DOT data file to the GraphViz dot program
            // so it can create a graph image of the data model
            if (!generateSVGFile(dotF.getAbsolutePath(), svgF.getAbsolutePath())) {
                application.errorMessages.append("An attempt to create a graph failed.\n");
		deleteFiles();
                return;
            }
        }
	catch (Exception e){application.errorMessages.append("Exception generating graph: " + e.getMessage()+"\n");application.reportError=true;}
    }


    /*
     * Invokes the GraphViz program to create a graph image from the
     * the given DOT data file
     *
     *@param dotFileName the name of the DOT data file
     *@param outputFileName the name of the output data file 
     *@return true if success; false if any failure occurs
     */
    private boolean generateSVGFile(String dotFileName,String outputFileName){
        //String environment[]={DOTFONTPATH+"="+Editor.m_GraphVizFontDir};
	//-q2 could tell dot to be silent (no warning or other messages), but it requires grpahviz 1.10. so don't use it for now
        //String cmdArray[]={Editor.m_GraphVizPath.toString(),"-Tsvg","-q2","-o",outputFileName,dotFileName};
        String cmdArray[]={Editor.m_GraphVizPath.toString(),"-Tsvg","-o",outputFileName,dotFileName};
        Runtime rt=Runtime.getRuntime();
        try {
            //Process p = rt.exec(cmdArray, environment);
            Process p = rt.exec(cmdArray);
            p.waitFor();
        } 
	catch (Exception e) {application.errorMessages.append("Error: generating OutputFile.\n");application.reportError=true;return false;}
        return true;
    }

    void displaySVG(Document d){
	Element svgRoot=d.getDocumentElement();
	//get the space width and height and set an offset for the SVG interpreter so that all objects
	//do not get created in the same quadrant (south-east)
	if (svgRoot.hasAttribute("width") && svgRoot.hasAttribute("height")){
	    String width=svgRoot.getAttribute("width");
	    String height=svgRoot.getAttribute("height");
	    try {
		long Xoffset = -Utils.getLong(width.substring(0,width.length()-2))/2;
		long Yoffset = -Utils.getLong(height.substring(0,height.length()-2))/2;
		SVGReader.setPositionOffset(Xoffset,Yoffset);
	    }
	    catch (IndexOutOfBoundsException ex){} //if we run into any problem, just forget this
	}
	NodeList objects=svgRoot.getElementsByTagName("g").item(0).getChildNodes();
	for (int i=0;i<objects.getLength();i++){
	    Node obj=(Node)objects.item(i);
	    if (obj.getNodeType()==Node.ELEMENT_NODE){processSVGNode((Element)obj,false,null);}
	}
	/*when running isaviz under Linux (and perhaps other posix systems), nodes are too small 
	  (width) w.r.t the text (don't know if it is a graphviz or java problem - anyway,
	  we correct it by adjusting widths a posteriori*/
	/*also do it under windows as some literals, sometimes have the same problem*/
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    application.geomMngr.correctResourceTextAndShape((IResource)e.nextElement());
	}
	for (Enumeration e=application.literals.elements();e.hasMoreElements();){
	    application.geomMngr.correctLiteralTextAndShape((ILiteral)e.nextElement());
	}
 	SVGReader.setPositionOffset(0,0);  //reset position offset (might affect anything that uses SVGReader methods, like constructors in VPath)
    }

    void processSVGNode(Element e,boolean styling,ProgPanel pp){
	NodeList content;
	if (e.getAttribute("class").equals("node")){//dealing with resource or literal
	    if ((content=e.getElementsByTagName("a")).getLength()>0){//dealing with a resource or a literal, or table form
		Element a=(Element)content.item(0);
		String mapID=a.getAttributeNS("http://www.w3.org/1999/xlink","href");
		if (mapID.startsWith(RESOURCE_MAPID_PREFIX)){//dealing with a resource
		    IResource r=getResourceByMapID(mapID);
		    ClosedShape el=getResourceShape(r,a,styling,pp);
		    el.setFilled(true);
		    Editor.vsm.addGlyph(el,Editor.mainVirtualSpace);
		    r.setGlyph(el);
		    Element text=(Element)a.getElementsByTagName("text").item(0);
		    VText tx;
		    if (text!=null){
			tx=SVGReader.createText(text,Editor.vsm);
		    }
		    else {//looks like a resource can be blank (rdf:about="") - even if it is not the case,
			tx=new VText("");//just be robust here, it is up to the RDF parser to report an error
		    }
		    if (tx.getText().equals(RDFLoader.hackReplacementText)){
			tx.setText(r.getGraphLabel());
			tx.setTextAnchor(VText.TEXT_ANCHOR_START);
		    }
		    else {
			tx.setTextAnchor(VText.TEXT_ANCHOR_START); //latest ZVTM/SVG takes MIDDLE into account, and this disturbs our previous hacks to center text (a future version should use MIDDLE and get rid of the hacks)
		    }
		    Editor.vsm.addGlyph(tx,Editor.mainVirtualSpace);
		    r.setGlyphText(tx);
		}
		else if (mapID.startsWith(LITERAL_MAPID_PREFIX)){//dealing with a literal
		    ILiteral lt=getLiteralByMapID(mapID);
		    if (lt!=null){
			ClosedShape r=getLiteralShape(lt,a,styling,pp);
			r.setFilled(true);
			if (r!=null){
			    Editor.vsm.addGlyph(r,Editor.mainVirtualSpace);
			    lt.setGlyph(r);
			}
			Element txt=(Element)a.getElementsByTagName("text").item(0);
			VText tg=null;
			if (txt!=null){
			    tg=SVGReader.createText(txt,Editor.vsm);
			    tg.setTextAnchor(VText.TEXT_ANCHOR_START); //latest ZVTM/SVG takes MIDDLE into account, and this disturbs our previous hacks to center text (a future version should use MIDDLE and get rid of the hacks)
			    Editor.vsm.addGlyph(tg,Editor.mainVirtualSpace);
			}
			if (tg!=null){
			    lt.setGlyphText(tg);
			}
		    }
		}
		else if (mapID.startsWith(STRUCT_MAPID_PREFIX)){//dealing with a table form (property/value pairs)
		    generateTableForm(a,mapID);
		}
		else {System.err.println("Error: processSVGNode: unable to identify node mapID "+mapID);}
	    }
	    else {System.err.println("Error: processSVGNode: unknown tag in "+e+" (expected <a href=\"...\">)");}
	}
	else if (e.getAttribute("class").equals("edge")){//dealing with property
	    /* The following code extracts the various components of the arc,
	       which can be in separate <a> elements when using GraphViz 2.1x for
	       some unknown reason. */
	    Element pathEl = null;
	    Element polygonEl = null;
	    Element textEl = null;
	    String mapID = null;
	    NodeList as = e.getElementsByTagName("a");
	    NodeList nl;
	    Element a;
	    for (int i=0;i<as.getLength();i++){
		a = (Element)as.item(i);
		if (mapID == null && a.hasAttributeNS("http://www.w3.org/1999/xlink", "href")){
		    mapID = a.getAttributeNS("http://www.w3.org/1999/xlink","href");
		}
		if (pathEl == null){
		    nl = a.getElementsByTagName("path");
		    if (nl.getLength() > 0){
			pathEl = (Element)nl.item(0);
		    }
		}
		if (polygonEl == null){
		    nl = a.getElementsByTagName("polygon");
		    if (nl.getLength() > 0){
			polygonEl = (Element)nl.item(0);
		    }
		}
		if (textEl == null){
		    nl = a.getElementsByTagName("text");
		    if (nl.getLength() > 0){
			textEl = (Element)nl.item(0);
		    }
		}
	    }
	    String pathCoords = pathEl.getAttribute("d");
	    //partially deal with the arrow because we need to know if we have to invert the path or not (if the arrow head coincides
	    //with the path start point instead of the end point
	    Vector coords=new Vector();
	    //get the polygon's vertices and translate them in the VTM's coord syst
	    SVGReader.translateSVGPolygon(polygonEl.getAttribute("points"),coords);
	    //find {left,up,right,down}-most coordinates
	    LongPoint p=(LongPoint)coords.firstElement();
	    long minx=p.x;
	    long maxx=p.x;
	    long miny=p.y;
	    long maxy=p.y;
	    for (int i=1;i<coords.size();i++){
		p=(LongPoint)coords.elementAt(i);
		if (p.x<minx){minx=p.x;}
		if (p.x>maxx){maxx=p.x;}
		if (p.y<miny){miny=p.y;}
		if (p.y>maxy){maxy=p.y;}
	    }//note that max and min are used again later in this block
	    //PATH
	    VPath pt=SVGReader.createPath(pathCoords,new VPath());
	    //invert path if necessary (happens when there are paths going from right to left - graphviz encodes them as going from left to right, so start and end points of the spline in isaviz are inversed and automatically/wrongly reassigned to the corresponding node - this causes truely weird splines as start point is moved to the position of end point and inversely) - the method below tests whether the arrow head is closer to the spline start point or end point (in the graphviz representation) ; if it is closer to the start point, it means that the path has to be inversed
	    pt=GeometryManager.invertPath((minx+maxx)/2,(miny+maxy)/2,pt);
	    Editor.vsm.addGlyph(pt,Editor.mainVirtualSpace);
	    IProperty pr;
	    if (mapID.startsWith(PROPERTY_MAPID_PREFIX)){//standard node-edge property path
		//ARROW - not part of the std SVG generator
		//retrieve last two points defining this path (2nd control point + end point) (GraphViz/DOT generates paths made only of cubic curves)
		PathIterator pi=pt.getJava2DPathIterator();
		float[] cds=new float[6];
		while (!pi.isDone()){pi.currentSegment(cds);pi.next();}
		//compute steep of segment linking the two points and deduce the triangle's orientation from it
		double angle=0;
		java.awt.geom.Point2D delta=GeometryManager.computeStepValue(cds[2],-cds[3],cds[4],-cds[5]);
		if (delta.getX()==0){
		    angle=0;
		    if (delta.getY()<0){angle=Math.PI;}
		}
		else {
		    angle=Math.atan(delta.getY()/delta.getX());
		    //align with VTM's system coordinates (a VTriangle's "head" points to the north when orient=0, not to the east)
		    if (delta.getX()<0){angle+=Math.PI/2;}   //comes from angle+PI-PI/2 (first PI due to the fact that ddx is <0 and the use of the arctan function - otherwise, head points in the opposite direction)
		    else {angle-=Math.PI/2;}
		}
		VTriangleOr c=new VTriangleOr((maxx+minx)/2,-(maxy+miny)/2,0,Math.max(maxx-minx,maxy-miny)/2,Color.black,(float)angle);
		Editor.vsm.addGlyph(c,Editor.mainVirtualSpace);	  
		//TEXT
		VText tx = SVGReader.createText(textEl,Editor.vsm);
		tx.setTextAnchor(VText.TEXT_ANCHOR_START); //latest ZVTM/SVG takes MIDDLE into account, and this disturbs our previous hacks to center text (a future version should use MIDDLE and get rid of the hacks)
		Editor.vsm.addGlyph(tx,Editor.mainVirtualSpace);
		Vector props = application.getProperties(textEl.getFirstChild().getNodeValue());
		pr=getPropertyByMapID(props,mapID);
		if (pr!=null){
		    pr.setGlyph(pt,c);
		    pr.setGlyphText(tx);
		}
	    }
	    else if (mapID.startsWith(TF_PROPERTY_MAPID_PREFIX)){//in that case, there is no text label associated with the edge, and no arrow head
		Vector prs=getPropertiesByMapID(mapID);
		for (int i=0;i<prs.size();i++){//no need to check for null, as the method returning prs returns an empty vector if there is no prop
		    pr=(IProperty)prs.elementAt(i);
		    pr.setGlyph(pt,null);
		}
	    }
	    else {System.err.println("Error: processSVGNode: unable to identify edge mapID "+mapID);}
	}
    }

    void generateTableForm(Element a,String mapID){/*we get an <a href=""> element containing a <polygon> (plus 
						     <polyline>s and <text>s that we are going to ignore) the 
						     <polygon> represents the outer rectangle for the record/table*/
	Vector pvPairs=(Vector)tfMapID2pvPairs.get(mapID);
	if (pvPairs!=null){
	    //get the outer rectangle and find out the table limits
	    VRectangle bounds=null;
	    NodeList content;
	    if ((content=a.getElementsByTagName("polygon")).getLength()>0){
		bounds=SVGReader.createRectangleFromPolygon((Element)content.item(0));
	    }
	    else if ((content=a.getElementsByTagName("rect")).getLength()>0){
		bounds=SVGReader.createRectangleFromPolygon((Element)content.item(0));
	    }
	    if (bounds!=null){
		long westBound=bounds.vx-bounds.getWidth();
		long northBound=bounds.vy+bounds.getHeight();
		long eastBound=bounds.vx+bounds.getWidth();
		long southBound=bounds.vy-bounds.getHeight();
		long halfRowHeight=(northBound-southBound)/pvPairs.size()/2; //half height of rows
		//--------------------------------------------------
		//find out the longest string graphical width for both properties and objects
		double longestProperty=0;
		double longestValue=0;
		IProperty predicate=null;
		Object object=null;
		ILiteral objectl;
		IResource objectr;
		java.awt.Graphics gc=Editor.vsm.getView(Editor.mainView).getGraphicsContext();
		gc.setFont(Editor.vsm.getMainFont());  //this should be changed for the font that is actually going to be assigned to the text, perhaps using the StyleInfo object
		Rectangle2D r2d;
		for (int i=0;i<pvPairs.size();i++){
		    predicate=(IProperty)((Vector)pvPairs.elementAt(i)).elementAt(1);
		    r2d=gc.getFontMetrics().getStringBounds(predicate.getIdent(),gc);
		    if (r2d.getWidth()>longestProperty){longestProperty=r2d.getWidth();}
		    object=((Vector)pvPairs.elementAt(i)).elementAt(2);
		    if (object instanceof ILiteral){
			objectl=(ILiteral)object;
			if (objectl.getText().length()>0){
			    r2d=gc.getFontMetrics().getStringBounds(objectl.getText(),gc);
			    if (r2d.getWidth()>longestValue){longestValue=r2d.getWidth();}
			}
		    }
		    else if (object instanceof IResource){
			objectr=(IResource)object;
			if (objectr.getGraphLabel().length()>0){
			    r2d=gc.getFontMetrics().getStringBounds(objectr.getGraphLabel(),gc);
			    if (r2d.getWidth()>longestValue){longestValue=r2d.getWidth();}
			}
		    }
		}
		//then compute the percentage of the total width that should be assigned to each column
		double sColPercentage=longestProperty/(longestProperty+longestValue);
		double oColPercentage=longestValue/(longestProperty+longestValue);
		//check that each column has a minimum width (worst ratio is set to be 20%/80% or 80%/20%)
		if (oColPercentage<0.2){oColPercentage=0.2;sColPercentage=0.8;}
		else if (sColPercentage<0.2){sColPercentage=0.2;oColPercentage=0.8;}
		long halfSColumnWidth=Math.round((eastBound-westBound)*sColPercentage/2);
		long halfOColumnWidth=Math.round((eastBound-westBound)*oColPercentage/2);
		//then deal with a potentially associated gss:sortPropertiesBy declaration
		Vector sortedPairs=null;
		//get the subject (first element of each triple <- we do not really have a pair, but call it so because predicate and objects are what matters (mainly))
		StyleInfoR sir=application.gssMngr.getStyle((IResource)((Vector)pvPairs.firstElement()).firstElement());
		if (sir!=null && sir.getPropertyOrdering()!=null){
		    sortedPairs=Utils.sortProperties(pvPairs,sir.getPropertyOrdering());
		}
		else {//no gss:sortPropertiesBy declaration, put the pairs in the table in no particular order
		    sortedPairs=pvPairs;
		}
		VText tx=null;
		//finally, build the graphical objects (from the sorted list of pairs)
		for (int i=0;i<sortedPairs.size();i++){
		    //predicate
		    predicate=(IProperty)((Vector)sortedPairs.elementAt(i)).elementAt(1);
		    VRectangle rl=new VRectangle(westBound+halfSColumnWidth,northBound-(2*i+1)*halfRowHeight,0,halfSColumnWidth,halfRowHeight,Color.white);
		    rl.setFilled(true);
		    Editor.vsm.addGlyph(rl,Editor.mainVirtualSpace);
		    predicate.setTableCellGlyph(rl);
		    tx=new VText(westBound+halfSColumnWidth,northBound-(2*i+1)*halfRowHeight,0,Color.black,predicate.getIdent(),VText.TEXT_ANCHOR_START);
		    Editor.vsm.addGlyph(tx,Editor.mainVirtualSpace);
		    predicate.setGlyphText(tx);
		    predicate.setTableFormLayout(true);
		    //object
		    object=((Vector)sortedPairs.elementAt(i)).elementAt(2);
		    rl=new VRectangle(westBound+2*halfSColumnWidth+halfOColumnWidth-1,northBound-(2*i+1)*halfRowHeight,0,halfOColumnWidth,halfRowHeight,Color.white);
		    rl.setFilled(true);
		    Editor.vsm.addGlyph(rl,Editor.mainVirtualSpace);
		    if (object instanceof ILiteral){
			objectl=(ILiteral)object;
			String label=objectl.getText();
			tx=new VText(westBound+2*halfSColumnWidth+halfOColumnWidth-1,northBound-(2*i+1)*halfRowHeight,0,Color.black,label,VText.TEXT_ANCHOR_START);
			Editor.vsm.addGlyph(tx,Editor.mainVirtualSpace);
			objectl.setGlyph(rl);
			objectl.setGlyphText(tx);
			objectl.setTableFormLayout(true);
		    }
		    else if (object instanceof IResource){
			objectr=(IResource)object;
			String label=objectr.getGraphLabel();
			tx=new VText(westBound+2*halfSColumnWidth+halfOColumnWidth-1,northBound-(2*i+1)*halfRowHeight,0,Color.black,label,VText.TEXT_ANCHOR_START);
			Editor.vsm.addGlyph(tx,Editor.mainVirtualSpace);
			objectr.setGlyph(rl);
			objectr.setGlyphText(tx);
			objectr.setTableFormLayout(true);
		    }
		    else {System.err.println("Error: processSVGNode/generateTableForm: bad object for "+mapID+" : "+object.toString());}
		}
		try {//predicate, object, and tx are init to null and may not have true values at this point if something goes wrong in the code above
		    if (sortedPairs.size()==1){//post-processing for one-row-only tables differently as GraphViz/dot
			gc.setFont(tx.getFont());// computes a height way too big - reduce it manually
			r2d=gc.getFontMetrics().getStringBounds(tx.getText(),gc);//have to do it this way because the paint thread might not yet have (and probably has not yet) computed the new String's bounds
			predicate.getTableCellGlyph().setHeight(Math.round(r2d.getHeight()));
			((RectangularShape)((INode)object).getGlyph()).setHeight(Math.round(r2d.getHeight()));
		    }
		}//hence the silent exception 
		catch (Exception ex){}
	    }
	    else {System.err.println("Error: processSVGNode/generateTableForm: failed to identify a table shape for "+mapID);}
	}
	else {System.err.println("Error: processSVGNode/generateTableForm: unable to identify table mapID "+mapID);}
    }

    void cleanMapIDs(){//get rid of the mapID attribute used in properties and literals
	for (Enumeration e=application.literals.elements();e.hasMoreElements();){
	    ((ILiteral)e.nextElement()).setMapID(null);
	}
	Vector v;
	for (Enumeration e=application.propertiesByURI.elements();e.hasMoreElements();){
	    v=(Vector)e.nextElement();
	    for (Enumeration e2=v.elements();e2.hasMoreElements();){
		((IProperty)e2.nextElement()).setMapID(null);
	    }
	}
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    ((IResource)e.nextElement()).setMapID(null);
	}
	if (tfMapID2pvPairs!=null){tfMapID2pvPairs.clear();tfMapID2pvPairs=null;}
    }

    IResource getResourceByMapID(String id){
	IResource res=null;
	IResource tmp;
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    tmp=(IResource)e.nextElement();
	    if (tmp.getMapID()!=null && tmp.getMapID().equals(id)){//mapID can be null if the resource is not visible
		res=tmp;
		break;
	    }
	}
	return res;
    }

    ILiteral getLiteralByMapID(String id){
	ILiteral res=null;
	ILiteral tmp;
	for (int i=0;i<application.literals.size();i++){
	    tmp=(ILiteral)application.literals.elementAt(i);
	    if (tmp.getMapID()!=null && tmp.getMapID().equals(id)){//mapID can be null if the literal is not visible
		res=tmp;
		break;
	    }
	}
	return res;
    }

    IProperty getPropertyByMapID(Vector props,String id){//props contains all properties which have the same URI (i.e. the same type) ; we were able to restrict to this thanks to the text label in the SVG (saves time)
	IProperty res=null;
	IProperty tmp;
	for (int i=0;i<props.size();i++){
	    tmp=(IProperty)props.elementAt(i);
	    if (tmp.getMapID()!=null && tmp.getMapID().equals(id)){
		res=tmp;
		break;//no need to inspect others as properties laid out as node/edge have a unique mapID
	    }
	}
	return res;
    }

    Vector getPropertiesByMapID(String id){
	Vector res=new Vector();
	IProperty tmp;
	Vector v;
	for (Enumeration e=application.propertiesByURI.elements();e.hasMoreElements();){
	    v=(Vector)e.nextElement();
	    for (int i=0;i<v.size();i++){
		tmp=(IProperty)v.elementAt(i);
		if (tmp.getMapID()!=null && tmp.getMapID().equals(id)){
		    res.add(tmp);
		}
	    }
	}
	return res;
    }

    void incID(StringBuffer id){
	boolean done=false;
	for (int i=0;i<id.length();i++){
	    byte b=(byte)id.charAt(i);
	    if (b<0x7a){
		id.setCharAt(i,(char)Utils.incByte(b));
		done=true;
		for (int j=0;j<i;j++){id.setCharAt(j,'0');}
		break;
	    }
	}
	if (!done){
	    for (int i=0;i<id.length();i++){id.setCharAt(i,'0');}
	    id.append('0');
	}
    }

    void incNodeID(){
	incID(nextNodeID);
    }

    void incEdgeID(){
	incID(nextEdgeID);
    }

    void incTFEdgeID(){
	incID(nextTFEdgeID);
    }

    void incStructID(){
	incID(nextStructID);
    }

    //given a statement, adds missing entitites to the internal model (called by RDFLoader when importing RDF/XML)
    Vector processStatement(Resource subj, Property pred, Resource obj){
	//subject
	IResource r1=addResource(subj);
	//predicate
	IProperty p=addProperty(pred);
	//object
	IResource r2=addResource(obj);
	r1.addOutgoingPredicate(p);
	p.setSubject(r1);
	p.setObject(r2);
	r2.addIncomingPredicate(p);
	Vector res=new Vector();
	res.add(r1);res.add(p);res.add(r2);
	return res;
    }

    //given a statement, adds missing entitites to the internal model (called by RDFLoader when importing RDF/XML)
    Vector processStyledStatement(Resource subj, Property pred, Resource obj){
	//subject
	IResource r1=addResource(subj);
	//predicate
	IProperty p=addProperty(pred);
	//object
	IResource r2=addResource(obj);
	r1.addOutgoingPredicate(p);
	p.setSubject(r1);
	p.setObject(r2);
	r2.addIncomingPredicate(p);
	Vector res=new Vector();
	res.add(r1);res.add(p);res.add(r2);
	//also remember Jena entities
	res.add(subj);res.add(pred);res.add(obj);
	return res;
    }

    //given a statement, adds missing entitites to the internal model (called by RDFLoader when importing RDF/XML)
    Vector processStatement(Resource subj, Property pred, Literal lit){
	//subject
	IResource r=addResource(subj);
	//predicate
	IProperty p=addProperty(pred);
	//object
	ILiteral l=addLiteral(lit);
	r.addOutgoingPredicate(p);
	p.setSubject(r);
	p.setObject(l);
	l.setIncomingPredicate(p);
	Vector res=new Vector();
	res.add(r);res.add(p);res.add(l);
	if (p.getIdent().equals(Editor.RDFS_NAMESPACE_URI+"label")){//if property is rdfs:label, set label for the resource
	    r.setLabel(l.getValue());
	}
	return res;
    }

    //given a statement, adds missing entitites to the internal model (called by RDFLoader when importing RDF/XML)
    Vector processStyledStatement(Resource subj, Property pred, Literal lit){
	//subject
	IResource r=addResource(subj);
	//predicate
	IProperty p=addProperty(pred);
	//object
	ILiteral l=addLiteral(lit);
	r.addOutgoingPredicate(p);
	p.setSubject(r);
	p.setObject(l);
	l.setIncomingPredicate(p);
	Vector res=new Vector();
	res.add(r);res.add(p);res.add(l);
	res.add(subj);res.add(pred);res.add(lit);
	if (p.getIdent().equals(Editor.RDFS_NAMESPACE_URI+"label")){//if property is rdfs:label, set label for the resource
	    r.setLabel(l.getValue());
	}
	return res;
    }

    /*create a new IResource from a Jena resource and add it to the internal model*/
    IResource addResource(Resource r){
	IResource res=new IResource(r);
	if (!application.resourcesByURI.containsKey(res.getIdentity())){
	    application.resourcesByURI.put(res.getIdentity(),res);
	    return res;
	}
	else {return (IResource)application.resourcesByURI.get(res.getIdentity());}
    }

    //create a new IProperty and add it to the internal model (from a Jena property)
    IProperty addProperty(Property p){
	IProperty res=new IProperty(p);
	if (application.propertiesByURI.containsKey(res.getIdent())){
	    Vector v=(Vector)application.propertiesByURI.get(res.getIdent());
	    v.add(res);
	}
	else {
	    Vector v=new Vector();
	    v.add(res);
	    application.propertiesByURI.put(res.getIdent(),v);
	}
	application.addPropertyType(res.getNamespace(),res.getLocalname(),true);  //add to the table of property constructors silently (a property might be used multiple times in existing graphs)
	return res;
    }

    //create a new ILiteral and add it to the internal model (from Jena literal)
    ILiteral addLiteral(Literal l){
	ILiteral res=new ILiteral(l);
	application.literals.add(res);
	return res;
    }

    void generateJenaModel(){
	//application.rdfModel=new ModelMem();
	application.rdfModel = ModelFactory.createDefaultModel();
	Hashtable addedResources=new Hashtable();
	Hashtable addedProperties=new Hashtable();
	IProperty p;
	IResource s;  //subject
	Object o;      //object
	for (Enumeration e1=application.propertiesByURI.elements();e1.hasMoreElements();){
	    for (Enumeration e2=((Vector)e1.nextElement()).elements();e2.hasMoreElements();){
		p=(IProperty)e2.nextElement();
		s=p.getSubject();
		o=p.getObject();
		if ((s!=null) && (o!=null) && (!p.isCommented())){//if the subject or the object is commented, the predicate will be commented
		    Resource jenaSubject=null;
		    if (addedResources.containsKey(s)){//keep track of resources already added to the model
			jenaSubject=(Resource)addedResources.get(s); // (can appear in several statements)
		    }
		    else {
			if (s.isAnon()){
			    try {
				jenaSubject=application.rdfModel.createResource();
				addedResources.put(s,jenaSubject);
			    }
			    catch(RDFException ex){javax.swing.JOptionPane.showMessageDialog(application.cmp,"An error occured while creating anonymous resource\n"+s.toString()+"\n"+ex);}
			    
			}
			else {
			    try {
				jenaSubject=application.rdfModel.createResource(s.getIdentity());
				addedResources.put(s,jenaSubject);
			    }
			    catch(RDFException ex){javax.swing.JOptionPane.showMessageDialog(application.cmp,"An error occured while creating resource\n"+s.toString()+"\n"+ex);}
			}			
		    }
		    Property jenaPredicate=null;
		    try {
			if (addedProperties.containsKey(p.getIdent())){//keep track of properties already added to the
			    jenaPredicate=(Property)addedProperties.get(p.getIdent()); //model (can appear in several statements)
			}
			else {
			    jenaPredicate=application.rdfModel.createProperty(p.getNamespace(),p.getLocalname());
			    addedProperties.put(p.getIdent(),jenaPredicate);
			}
		    }
		    catch(RDFException ex){javax.swing.JOptionPane.showMessageDialog(application.cmp,"An error occured while creating property\n"+p.toString()+"\n"+ex);}
		    RDFNode jenaObject=null;
		    if (o instanceof IResource){
			IResource o2=(IResource)o;
			if (addedResources.containsKey(o2)){//keep track of resources already added to the model
			    jenaObject=(Resource)addedResources.get(o2); // (can appear in several statements)
			}
			else {
			    if (o2.isAnon()){
				try {
				    jenaObject=application.rdfModel.createResource();
				    addedResources.put(o2,jenaObject);
				}
				catch(RDFException ex){javax.swing.JOptionPane.showMessageDialog(application.cmp,"An error occured while creating anonymous resource\n"+s.toString()+"\n"+ex);}
			    }
			    else {
				try {
				    jenaObject=application.rdfModel.createResource(o2.getIdentity());
				    addedResources.put(o2,jenaObject);
				}
				catch(RDFException ex){javax.swing.JOptionPane.showMessageDialog(application.cmp,"An error occured while creating resource\n"+s.toString()+"\n"+ex);}
			    }
			    
			}
		    }
		    else {//o instanceof ILiteral
			ILiteral l=(ILiteral)o;
			try {
			    if (l.getDatatype()!=null){
				//not dealing with lang here as it is no longer allowed by the spec (for typed literals)
				jenaObject=application.rdfModel.createTypedLiteral(l.getValue(),l.getDatatype());
			    }
			    else {
				if (l.getLang()!=null){
				    jenaObject=application.rdfModel.createLiteral(l.getValue(),l.getLang());
				}
				else {
				    String lang=Editor.ALWAYS_INCLUDE_LANG_IN_LITERALS ? Editor.DEFAULT_LANGUAGE_IN_LITERALS : "" ;
				    jenaObject=application.rdfModel.createLiteral(l.getValue(),lang);
				}
			    }
			}
			catch(RDFException ex){
			    javax.swing.JOptionPane.showMessageDialog(application.cmp,"An error occured while creating literal\n"+o.toString()+"\n"+ex);
			    application.errorMessages.append(ex.getMessage()+"\n");
			    application.reportError=true;
			}
		    }
		    try {
			Statement st=application.rdfModel.createStatement(jenaSubject,jenaPredicate,jenaObject);
			application.rdfModel.add(st);
		    }
		    catch(Exception ex){
			javax.swing.JOptionPane.showMessageDialog(application.cmp,"An error occured while creating the Jena model:\nadding statement "+p.toString()+"("+s.toString()+","+o.toString()+")\n"+ex);
			application.errorMessages.append(ex.getMessage()+"\n");
			application.reportError=true;
		    }
		}
	    }
	}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
    }
    
    public void save(Model m,File f){
	Editor.vsm.getView(Editor.mainView).setStatusBarText("Exporting to RDF/XML "+f.toString()+" ...");
	try {//should choose between abbrev and std syntax
	    RDFWriter rdfw;
	    if (Editor.ABBREV_SYNTAX){
		rdfw=m.getWriter(RDFXMLAB);
		rdfw.setProperty("allowBadURIs","true");  //because Jena2p2 does not allow null (blank) base URIs when checking for bad URIs
		rdfw.setProperty("showXmlDeclaration","true");
		if (Editor.BASE_URI.length()>0 && !Utils.isWhiteSpaceCharsOnly(Editor.BASE_URI)){rdfw.setProperty("xmlbase",Editor.BASE_URI);}
	    }
	    else {
		rdfw=m.getWriter(RDFXML);
		rdfw.setProperty("allowBadURIs","true");  //because Jena2p2 does not allow null (blank) base URIs when checking for bad URIs
		rdfw.setProperty("showXmlDeclaration","true");
		if (Editor.BASE_URI.length()>0 && !Utils.isWhiteSpaceCharsOnly(Editor.BASE_URI)){rdfw.setProperty("xmlbase",Editor.BASE_URI);}
	    }
	    rdfw.setErrorHandler(new SerializeErrorHandler(application));
	    for (int i=0;i<application.tblp.nsTableModel.getRowCount();i++){
		if (((String)application.tblp.nsTableModel.getValueAt(i,0)).length()>0){//only add complete bindings
		    //as of Jena2p4, setNsPrefix methods are located in PrefixMapping (superinterface of Model)
		    //and cannot be accessed through the RDFWriter any longer
		    m.setNsPrefix((String)application.tblp.nsTableModel.getValueAt(i,0),(String)application.tblp.nsTableModel.getValueAt(i,1));
		}
	    }
	    FileOutputStream fos=new FileOutputStream(f);
	    rdfw.write(m,fos,Editor.BASE_URI);
	    Editor.vsm.getView(Editor.mainView).setStatusBarText("Exporting to RDF/XML "+f.toString()+" ...done");
	}
	catch (RDFException ex){application.errorMessages.append("RDF exception in RDFLoader.save() "+ex+"\nSee command line for details.\n");ex.printStackTrace();application.reportError=true;}
	catch (IOException ex){application.errorMessages.append("I/O exception in RDFLoader.save() "+ex+"\nSee command line for details.\n");ex.printStackTrace();application.reportError=true;}
	catch (Exception ex){application.errorMessages.append("Exception in RDFLoader.save() "+ex+"\nSee command line for details.\n");ex.printStackTrace();application.reportError=true;}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
    }

    /*Same as save(), but the target is a StringBuffer instead of a file*/
    public StringBuffer serialize(Model m){
	try {//should choose between abbrev and std syntax
	    RDFWriter rdfw;
	    if (Editor.ABBREV_SYNTAX){
		rdfw=m.getWriter(RDFXMLAB);
		rdfw.setProperty("allowBadURIs","true");  //because Jena2p2 does not allow null (blank) base URIs when checking for bad URIs
		rdfw.setProperty("showXmlDeclaration","true");
		if (Editor.BASE_URI.length()>0 && !Utils.isWhiteSpaceCharsOnly(Editor.BASE_URI)){rdfw.setProperty("xmlbase",Editor.BASE_URI);}
	    }
	    else {
		rdfw=m.getWriter(RDFXML);
		rdfw.setProperty("allowBadURIs","true");  //because Jena2p2 does not allow null (blank) base URIs when checking for bad URIs
		rdfw.setProperty("showXmlDeclaration","true");
		if (Editor.BASE_URI.length()>0 && !Utils.isWhiteSpaceCharsOnly(Editor.BASE_URI)){rdfw.setProperty("xmlbase",Editor.BASE_URI);}
	    }	    
	    rdfw.setErrorHandler(new SerializeErrorHandler(application));
	    for (int i=0;i<application.tblp.nsTableModel.getRowCount();i++){
		if (((String)application.tblp.nsTableModel.getValueAt(i,0)).length()>0){//only add complete bindings
		    //as of Jena2p4, setNsPrefix methods are located in PrefixMapping (superinterface of Model)
		    //and cannot be accessed through the RDFWriter any longer
		    m.setNsPrefix((String)application.tblp.nsTableModel.getValueAt(i,0),(String)application.tblp.nsTableModel.getValueAt(i,1));
		}
	    }
	    java.io.StringWriter sw=new java.io.StringWriter();
	    rdfw.write(m,sw,Editor.BASE_URI);
	    return sw.getBuffer();
	}
	catch (RDFException ex){application.errorMessages.append("RDF exception in RDFLoader.save() "+ex+"\nSee command line for details.\n");ex.printStackTrace();application.reportError=true;}
	catch (Exception ex){application.errorMessages.append("Exception in RDFLoader.save() "+ex+"\nSee command line for details.\n");ex.printStackTrace();application.reportError=true;}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
	return new StringBuffer();
    }

    public void save(Model m,OutputStream os){
	Editor.vsm.getView(Editor.mainView).setStatusBarText("Writing RDF/XML to stream ...");
	try {//should choose between abbrev and std syntax
	    RDFWriter rdfw;
	    if (Editor.ABBREV_SYNTAX){
		rdfw=m.getWriter(RDFXMLAB);
		rdfw.setProperty("allowBadURIs","true");  //because Jena2p2 does not allow null (blank) base URIs when checking for bad URIs
		rdfw.setProperty("showXmlDeclaration","true");
		if (Editor.BASE_URI.length()>0 && !Utils.isWhiteSpaceCharsOnly(Editor.BASE_URI)){rdfw.setProperty("xmlbase",Editor.BASE_URI);}
	    }
	    else {
		rdfw=m.getWriter(RDFXML);
		rdfw.setProperty("allowBadURIs","true");  //because Jena2p2 does not allow null (blank) base URIs when checking for bad URIs
		rdfw.setProperty("showXmlDeclaration","true");
		if (Editor.BASE_URI.length()>0 && !Utils.isWhiteSpaceCharsOnly(Editor.BASE_URI)){rdfw.setProperty("xmlbase",Editor.BASE_URI);}
	    }
	    rdfw.setErrorHandler(new SerializeErrorHandler(application));
	    for (int i=0;i<application.tblp.nsTableModel.getRowCount();i++){
		if (((String)application.tblp.nsTableModel.getValueAt(i,0)).length()>0){//only add complete bindings
		    //as of Jena2p4, setNsPrefix methods are located in PrefixMapping (superinterface of Model)
		    //and cannot be accessed through the RDFWriter any longer
		    m.setNsPrefix((String)application.tblp.nsTableModel.getValueAt(i,0),(String)application.tblp.nsTableModel.getValueAt(i,1));
		}
	    }
	    rdfw.write(m,os,Editor.BASE_URI);
	    Editor.vsm.getView(Editor.mainView).setStatusBarText("Writing RDF/XML to stream ...done");
	}
	catch (RDFException ex){application.errorMessages.append("RDF exception in RDFLoader.save() "+ex+"\nSee command line for details.\n");ex.printStackTrace();application.reportError=true;}
	catch (Exception ex){application.errorMessages.append("Exception in RDFLoader.save() "+ex+"\nSee command line for details.\n");ex.printStackTrace();application.reportError=true;}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
    }

    public void saveAsN3(Model m,File f){
	Editor.vsm.getView(Editor.mainView).setStatusBarText("Exporting to Notation 3 "+f.toString()+" ...");
	try {
	    //OutputStreamWriter fw=new OutputStreamWriter(new FileOutputStream(f),ConfigManager.ENCODING);
	    FileOutputStream fos=new FileOutputStream(f);
	    //RDFWriter rdfw=new N3JenaWriter();
	    RDFWriter rdfw=m.getWriter(N3);
	    rdfw.setErrorHandler(new SerializeErrorHandler(application));
	    rdfw.write(m,fos,Editor.BASE_URI);
	    Editor.vsm.getView(Editor.mainView).setStatusBarText("Exporting to Notation 3 "+f.toString()+" ...done");
	}
	catch (Exception ex){application.errorMessages.append("RDF exception in RDFLoader.saveAsN3() "+ex+"\n");application.reportError=true;}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
    }

    public void saveAsTriples(Model m,File f){
	Editor.vsm.getView(Editor.mainView).setStatusBarText("Exporting to N-Triples "+f.toString()+" ...");
	try {
	    //OutputStreamWriter fw=new OutputStreamWriter(new FileOutputStream(f),ConfigManager.ENCODING);
	    FileOutputStream fos=new FileOutputStream(f);
	    //RDFWriter rdfw=new NTripleWriter();
	    RDFWriter rdfw=m.getWriter(NTRIPLE);
	    rdfw.setErrorHandler(new SerializeErrorHandler(application));
	    rdfw.write(m,fos,Editor.BASE_URI);
	    Editor.vsm.getView(Editor.mainView).setStatusBarText("Exporting to N-Triples "+f.toString()+" ...done");
	}
	catch (Exception ex){application.errorMessages.append("RDF exception in RDFLoader.saveAsTriples() "+ex+"\n");application.reportError=true;}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
    }

    void loadAndStyle(Object o,int whichReader){//o can be a java.net.URL, a java.io.File or a java.io.InputStream (plug-ins)
	//whichReader=0 if RDF/XML, 1 if NTriples, 2 if N3
	ProgPanel pp=new ProgPanel("Resetting..."," ","Loading RDF and applying stylesheets");
	PrintWriter pw=null;
	try {
	    pp.setPBValue(5);
	    //application.rdfModel=new ModelMem();
	    application.rdfModel = ModelFactory.createDefaultModel();
	    if (o instanceof File){
		rdfF=(File)o;
		//FileReader fr=new FileReader(rdfF);
		//InputStreamReader isr=new InputStreamReader(new FileInputStream(rdfF),ConfigManager.ENCODING);
		FileInputStream fis=new FileInputStream(rdfF);
		pp.setLabel("Loading local file "+rdfF.toString()+" ...");
		pw=createDOTFile();
		pp.setPBValue(10);
		pp.setLabel("Initializing ARP(Jena) ...");
		initParser(whichReader,application.rdfModel);
		pp.setPBValue(20);
		pp.setLabel("Parsing RDF ...");
		Editor.BASE_URI=rdfF.toURL().toString();
		parser.read(application.rdfModel,fis,Editor.BASE_URI);
	    }
	    else if (o instanceof java.net.URL){
		rdfU=(java.net.URL)o;
		pp.setLabel("Loading remote file "+rdfU.toString()+" ...");
		pw=createDOTFile();
		pp.setPBValue(10);
		pp.setLabel("Initializing ARP(Jena) ...");
		initParser(whichReader,application.rdfModel);
		pp.setPBValue(20);
		pp.setLabel("Parsing RDF ...");
		Editor.BASE_URI=rdfU.toString();
		parser.read(application.rdfModel,rdfU.toString());
	    }
	    else if (o instanceof InputStream){
		pp.setLabel("Reading stream ...");
		pw=createDOTFile();
		pp.setPBValue(10);
		pp.setLabel("Initializing ARP(Jena) ...");
		initParser(whichReader,application.rdfModel);
		pp.setPBValue(20);
		pp.setLabel("Parsing RDF ...");
		parser.read(application.rdfModel,(InputStream)o,Editor.BASE_URI);
	    }
 	    application.declareNSBindings(application.rdfModel.getNsPrefixMap(),application.rdfModel.listNameSpaces());
	    //build the temp data structures containing the styling rules
	    pp.setPBValue(40);
	    pp.setLabel("Building styling rules ...");
	    application.gssMngr.initStyleTables();
	    //process statements
	    StyledSH sh=new StyledSH(application,application.rdfModel);
	    StmtIterator it=application.rdfModel.listStatements();
	    Statement st;
	    Vector ijStatements=new Vector();
	    while (it.hasNext()){
		st=it.nextStatement();
		if (st.getObject() instanceof Resource){sh.statement(st.getSubject(),st.getPredicate(),(Resource)st.getObject(),ijStatements);}
		else if (st.getObject() instanceof Literal){sh.statement(st.getSubject(),st.getPredicate(),(Literal)st.getObject(),ijStatements);}
		else {System.err.println("Error: RDFLoader.load(): unknown kind of object: "+st.getObject());}
	    }
	    it.close();
	    Vector ijStatement;
	    for (int i=0;i<ijStatements.size();i++){
		ijStatement=(Vector)ijStatements.elementAt(i);
		if (ijStatement.elementAt(2) instanceof IResource){
		    sh.statementDotResource((IResource)ijStatement.elementAt(0),(Resource)ijStatement.elementAt(3),(IProperty)ijStatement.elementAt(1),(Property)ijStatement.elementAt(4),(IResource)ijStatement.elementAt(2),(Resource)ijStatement.elementAt(5));
		    ijStatement.removeAllElements();
		}
		else {//ijStatement.elementAt(2) instanceof ILiteral
		    sh.statementDotLiteral((IResource)ijStatement.elementAt(0),(Resource)ijStatement.elementAt(3),(IProperty)ijStatement.elementAt(1),(Property)ijStatement.elementAt(4),(ILiteral)ijStatement.elementAt(2),(Literal)ijStatement.elementAt(5));
		    ijStatement.removeAllElements();
		}
	    }
	    ijStatements.removeAllElements();
	    ijStatements=null;
	    pp.setPBValue(50);
	    pp.setLabel("Creating temporary SVG file ...");
	    literalLbNum=new Integer(0);  //used to count literals and generate tmp literal labels in the DOT file
  	    generateStyledDOTFile(sh.getVisibleStatements(),sh.getVisibleResources(),pw);
	    sh.clean();
	    svgF=Utils.createTempFile(Editor.m_TmpDir.toString(),"isv",".svg");
	    pp.setPBValue(60);
	    pp.setLabel("Calling GraphViz (this can take several minutes) ...");
	    callDOT(pw);
	    pp.setPBValue(80);
	    pp.setLabel("Parsing SVG ...");
 	    displaySVGAndStyle(application.xmlMngr.parse(svgF,false),pp);
	    cleanMapIDs();//the mapping between SVG and RDF has been done -  we do not need these any longer
	    pp.setPBValue(90);
	    pp.setLabel("Applying styling rules ...");
 	    assignStyleToGraph();
	    application.gssMngr.cleanStyleTables();
	    application.showAnonIds(ConfigManager.SHOW_ANON_ID);  //show/hide IDs of anonymous resources
	    application.showResourceLabels(Editor.DISP_AS_LABEL);
	    pp.setPBValue(100);
	    pp.setLabel("Deleting temporary files and data structures...");
	    if (Editor.dltOnExit){deleteFiles();}
	    Editor.vsm.getGlobalView(Editor.vsm.getVirtualSpace(Editor.mainVirtualSpace).getCamera(0),ConfigManager.ANIM_DURATION);
	    application.centerRadarView();
	    application.rdfModel=null; //get rid of it at this point - we will generate it only when appropriate (for instance when exporting)
	}
	catch (IOException ex){application.errorMessages.append("RDFLoader.loadAndStyle() "+ex+"\n");application.reportError=true;}
	catch (RDFException ex2){application.errorMessages.append("RDFLoader.loadAndStyle() "+ex2+"\n");application.reportError=true;}
	//catch (Exception ex3){application.errorMessages.append("RDFLoader.load() "+ex3+"\nPlease verify your directory preferences (GraphViz/DOT might not be configured properly), your default namespace and anonymous node prefix declarations");application.reportError=true;}
	if (application.reportError){Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");application.reportError=false;}
	pp.destroy();
    }

    void generateStyledDOTFile(Hashtable visibleStatements,Hashtable visibleResources,PrintWriter pw){
	int numLiterals=0;
	String key;
	Vector[] netfStatements;  //node-edge [0] and table-form [1] statements for a given subject
	ISVJenaStatement aStatement;
	Vector tfStatements=new Vector();
	for (Enumeration e=visibleStatements.keys();e.hasMoreElements();){
	    key=(String)e.nextElement();
	    netfStatements=(Vector[])visibleStatements.get(key);
	    for (int i=0;i<netfStatements[0].size();i++){//statements to be laid out as node-edge
		aStatement=(ISVJenaStatement)netfStatements[0].elementAt(i);
		if (aStatement.objectIsResource()){printDOTStatementNERO(aStatement,pw);}
		else {printDOTStatementNELO(aStatement,pw);}
	    }
	    for (int i=0;i<netfStatements[1].size();i++){//statements to be laid out in a table form
		tfStatements.add(netfStatements[1].elementAt(i));
	    }
	}
	printDOTStatementsTF(tfStatements,pw);
	Vector v;
	for (Enumeration e=visibleResources.elements();e.hasMoreElements();){
	    v=(Vector)e.nextElement();
	    printDOTResource((IResource)v.elementAt(0),(Resource)v.elementAt(1),v.elementAt(2),pw);
	}
    }

    /*statement to be laidout as a node-edge with Resource Object*/
    void printDOTStatementNERO(ISVJenaStatement ijs,PrintWriter pw){
	if (pw == null) return;
	try {
	    //find out if subject is already in the DOT file by checking if its mapID il null or not
	    boolean nodeAlreadyInDOTFile=true;
	    if (ijs.isubject.getMapID()==null){
		String aUniqueRID=RESOURCE_MAPID_PREFIX+nextNodeID.toString();
		ijs.isubject.setMapID(aUniqueRID);
		incNodeID();
		nodeAlreadyInDOTFile=false;
	    }
	    if (ijs.jsubject.isAnon()) {//b-node (subject)
		if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (subject)
		    String shape=GraphStylesheet.gss2dotShape(ijs.getSubjectShapeType());
		    if (shape.equals(GraphStylesheet._dotCircle)){
			pw.println("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId())+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
		    }
		    else {
			pw.println("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId())+"\" [shape=\""+shape+"\",URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
		    }
		}
		//then print the left hand side of the DOT statement
		pw.print("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId()));
	    } 
	    else {//named resources (URI) (subject)
 		String rident=ijs.isubject.getGraphLabel();
 		if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (subject)
		    String shape=GraphStylesheet.gss2dotShape(ijs.getSubjectShapeType());
		    if (shape.equals(GraphStylesheet._dotCircle)){
			pw.println("\""+rident+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
		    }
		    else {
			pw.println("\""+rident+"\" [shape=\""+shape+"\",URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
		    }
		}
		//then print the left hand side of the DOT statement
 		pw.print("\""+rident);
	    }
	    //print the -> statement symbol
	    pw.print("\" -> ");
	    //prepare a new unique ID for the edge representing the property
	    String aUniqueID=PROPERTY_MAPID_PREFIX+nextEdgeID.toString();
	    ijs.ipredicate.setMapID(aUniqueID);
	    incEdgeID();
	    //find out if object is already in the DOT file by checking if its mapID is null or not
	    nodeAlreadyInDOTFile=true;
	    if (ijs.iobjectr.getMapID()==null){
		String aUniqueRID=RESOURCE_MAPID_PREFIX+nextNodeID.toString();
		ijs.iobjectr.setMapID(aUniqueRID);
		incNodeID();
		nodeAlreadyInDOTFile=false;
	    }
	    if (ijs.jobjectr.isAnon()){//b-node (object)
		/* "\l" at the end of the label is here to generate a left-aliggned attribute in the SVG file because 
		   since graphviz 1.8 text objects are centered around the coordinates provided with the text element*/
		//print the right-hand side of the statement
		pw.println("\""+ijs.iobjectr.getGraphLabel()+"\" [label=\""+ijs.jpredicate.getURI()+"\\l\",URL=\""+aUniqueID+"\"];");
		if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (object)
		    String shape=GraphStylesheet.gss2dotShape(ijs.getObjectShapeType());
		    if (shape.equals(GraphStylesheet._dotCircle)){
			pw.println("\""+ijs.iobjectr.getGraphLabel()+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,URL=\""+ijs.iobjectr.getMapID()+"\",style=filled];");
		    }
		    else {
			pw.println("\""+ijs.iobjectr.getGraphLabel()+"\" [shape=\""+shape+"\",URL=\""+ijs.iobjectr.getMapID()+"\",style=filled];");
		    }
		}
	    }
	    else {//named resources (URI) (object)
		/* "\l" at the end of the label is here to generate a left-aliggned attribute in the SVG file because 
		   since graphviz 1.8 text objects are centered around the coordinates provided with the text element*/
		//print the right-hand side of the statement
		pw.println("\""+ijs.iobjectr.getGraphLabel()+"\" [label=\""+ijs.jpredicate.getURI()+"\\l\",URL=\""+aUniqueID+"\"];");
		if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (object)
		    String shape=GraphStylesheet.gss2dotShape(ijs.getObjectShapeType());
		    if (shape.equals(GraphStylesheet._dotCircle)){
			pw.println("\""+ijs.iobjectr.getGraphLabel()+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,URL=\""+ijs.iobjectr.getMapID()+"\",style=filled];");
		    }
		    else {
			pw.println("\""+ijs.iobjectr.getGraphLabel()+"\" [shape=\""+shape+"\",URL=\""+ijs.iobjectr.getMapID()+"\",style=filled];");
		    }
		}
	    }
	}
	catch (RDFException ex){application.errorMessages.append("Error: printDOTStatementNERO(): "+ex+"\n");application.reportError=true;}	
    }

    /*statement to be laidout as a node-edge with Literal Object*/
    void printDOTStatementNELO(ISVJenaStatement ijs,PrintWriter pw){
	if (pw == null) return;
	try {
	    //find out if subject is already in the DOT file by checking if its mapID il null or not
	    boolean nodeAlreadyInDOTFile=true;
	    if (ijs.isubject.getMapID()==null){
		String aUniqueRID=RESOURCE_MAPID_PREFIX+nextNodeID.toString();
		ijs.isubject.setMapID(aUniqueRID);
		incNodeID();
		nodeAlreadyInDOTFile=false;
	    }
	    if (ijs.jsubject.isAnon()) {//b-node (subject)
		if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (subject)
		    String shape=GraphStylesheet.gss2dotShape(ijs.getSubjectShapeType());
		    if (shape.equals(GraphStylesheet._dotCircle)){
			pw.println("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId())+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
		    }
		    else {
			pw.println("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId())+"\" [shape=\""+shape+"\",URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
		    }
		}
		//then print the left hand side of the DOT statement
		pw.print("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId()));
	    } 
	    else {//named resources (URI) (subject)
 		String rident=ijs.isubject.getGraphLabel();
 		if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (subject)
		    String shape=GraphStylesheet.gss2dotShape(ijs.getSubjectShapeType());
		    if (shape.equals(GraphStylesheet._dotCircle)){
			pw.println("\""+rident+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
		    }
		    else {
			pw.println("\""+rident+"\" [shape=\""+shape+"\",URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
		    }
		}
		//then print the left hand side of the DOT statement
 		pw.print("\""+rident);
	    }
	    //prepare a new unique ID for the edge representing the property
	    String aUniquePID=PROPERTY_MAPID_PREFIX+nextEdgeID.toString();
	    ijs.ipredicate.setMapID(aUniquePID);
	    incEdgeID();
	    //prepare the literal label (truncate and escape some chars from the initial value)
	    String s1 = new String(ijs.jobjectl.getString());
	    s1 = s1.replace('\n', ' ');
	    s1 = s1.replace('\f', ' ');
	    s1 = s1.replace('\r', ' ');
	    if (s1.indexOf('"')!= -1){s1=Utils.replaceString(s1,"\"","\\\"");}
	    // Anything beyond MAX_LIT_CHAR_COUNT chars makes the graph too large
	    String tmpObject=((s1.length()>=Editor.MAX_LIT_CHAR_COUNT) ? s1.substring(0,Editor.MAX_LIT_CHAR_COUNT)+" ..." : s1);
	    //prepare a new unique ID for the literal
	    String aUniqueLID=LITERAL_MAPID_PREFIX+nextNodeID.toString();
	    ijs.iobjectl.setMapID(aUniqueLID);
	    incNodeID();
	    String tmpName = "Literal_"+literalLbNum.toString();
	    literalLbNum=new Integer(literalLbNum.intValue()+1);
	    pw.print("\" -> \""+tmpName);
	    pw.println("\" [label=\""+ijs.jpredicate.getURI()+"\\l\",URL=\""+aUniquePID+"\"];");// "\l" at the end of the label is here to generate a left-aliggned attribute in the SVG file because since graphviz 1.8 text objects are centered around the coordinates provided with the text element
	    String shape=GraphStylesheet.gss2dotShape(ijs.getObjectShapeType());
	    if (shape.equals(GraphStylesheet._dotCircle)){
		pw.println("\""+tmpName+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,label=\""+tmpObject+"\\l\",URL=\""+aUniqueLID+"\",style=filled];");
	    }
	    else {
		pw.println("\""+tmpName+"\" [shape=\""+shape+"\",label=\""+tmpObject+"\\l\",URL=\""+aUniqueLID+"\",style=filled];");
	    }
	}
	catch (RDFException ex){application.errorMessages.append("Error: printDOTStatementNELO(): "+ex+"\n");application.reportError=true;}
    }

    /*statements to be laid out in a table form (both Resource and Literal Objects)*/
    void printDOTStatementsTF(Vector ijss,PrintWriter pw){
	if (pw == null) return;
	Hashtable subject2statements=new Hashtable();
	Vector v;//tmp variable used throughout this method for different purposes
	ISVJenaStatement ijs;
	for (int i=0;i<ijss.size();i++){//first, we need to group the statements by subject
	    ijs=(ISVJenaStatement)ijss.elementAt(i);
	    if (subject2statements.containsKey(ijs.isubject)){
		/*we do not actually need to sort the statements depending
		  on a potentially associated gss:sortPropertiesBy declaration
		  as this will be done later when creating the glyphs*/
		v=(Vector)subject2statements.get(ijs.isubject);
		v.add(ijs);
	    }
	    else {
		v=new Vector();
		v.add(ijs);
		subject2statements.put(ijs.isubject,v);
	    }
	}
	//then generate the DOT stuff for each group of statement, using shape=record
	IResource ir;
	boolean nodeAlreadyInDOTFile;
	tfMapID2pvPairs=new Hashtable();
	Vector pvPairs;
	Vector pvPair;
	for (Enumeration e=subject2statements.elements();e.hasMoreElements();){//for each set of statements having the same subject
	    v=(Vector)e.nextElement();
	    if (v!=null){
		ijs=(ISVJenaStatement)v.firstElement(); //get a statement as we need to access information about the subject common to all statements
		/*find out if subject is already in the DOT file by checking if its mapID il null or not
		  Note: for now, we won't need to do this for the objects of statements, as we have restricted
		  table form layout to literals and resources which are not subject of any statement and which
		  are only the object of a single statement (the one we are dealig with now). But this could
		  change in the future (we might allow resources which are subject of one or more statements to
		  actually be laid out in a table form) ; in that case we will need to check that they have not
		  yet been declared elsewhere (actually, we might need to prevent them being declared elsewhere
		  - not sure, take a look at the DOT format spec)*/
		nodeAlreadyInDOTFile=true;
		if (ijs.isubject.getMapID()==null){
		    String aUniqueRID=RESOURCE_MAPID_PREFIX+nextNodeID.toString();
		    ijs.isubject.setMapID(aUniqueRID);
		    incNodeID();
		    nodeAlreadyInDOTFile=false;
		}
		if (ijs.jsubject.isAnon()){//b-node (subject)
		    if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (subject)
			String shape=GraphStylesheet.gss2dotShape(ijs.getSubjectShapeType());
			if (shape.equals(GraphStylesheet._dotCircle)){
			    pw.println("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId())+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
			}
			else {
			    pw.println("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId())+"\" [shape=\""+shape+"\",URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
			}
		    }
		    //then print the left hand side of the DOT statement
		    pw.print("\""+Editor.ANON_NODE+IResource.getJenaAnonId(ijs.jsubject.getId()));
		}
		else {//named resources (URI) (subject)
		    String rident=ijs.isubject.getGraphLabel();
		    if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (subject)
			String shape=GraphStylesheet.gss2dotShape(ijs.getSubjectShapeType());
			if (shape.equals(GraphStylesheet._dotCircle)){
			    pw.println("\""+rident+"\" [label=\""+RDFLoader.hackReplacementText+"\",shape=\""+shape+"\",fixedsize=true,URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
			}
			else {
			    pw.println("\""+rident+"\" [shape=\""+shape+"\",URL=\"" +ijs.isubject.getMapID()+"\",style=filled];");
			}
		    }
		    //then print the left hand side of the DOT statement
		    pw.print("\""+rident);
		}
		//generate unique identifiers for the struct record and the edge linking the subject to it
		String aUniqueStructLabel=STRUCT_PREFIX+nextStructID.toString();
		String aUniqueStructID=STRUCT_MAPID_PREFIX+nextStructID.toString();
		incStructID();
		//need to assign an ID to the edge, which will be shared by all involved IProperty objects
		String aUniqueTFEdgeID=TF_PROPERTY_MAPID_PREFIX+nextTFEdgeID.toString();
		incTFEdgeID();
		//print the -> statement symbol + the record struct identifier + the beginning of the record declaration
		pw.print("\" -> \""+aUniqueStructLabel+"\" [URL=\""+aUniqueTFEdgeID+"\"];\n"+aUniqueStructLabel+" [shape=\"record\",URL=\""+aUniqueStructID+"\",label=\"");  //this edge does not take a label, as the property names are in the record
		/*find the longest property name and the longest literal value/object URI
		  they will be used on every line for labels as the DOT layout does not do what we want
		  (it adapts the x-ccord of the line separating columns for each row) - so here we take
		  the longest string for each column and apply it everywhere (anyway we are only going to retrieve
		  the outmost box from the SVG file and do the layout manually later after the SVG parsing)
		*/
		String longestProperty="";
		String longestLiteralValue="";
		String longestResourceURI="";
		String s;
		pvPairs=new Vector(); //used to store all property/value pairs associated with a current subject
		for (int i=0;i<v.size();i++){//for each statement having the same subject
		    ijs=(ISVJenaStatement)v.elementAt(i);
		    ijs.ipredicate.setMapID(aUniqueTFEdgeID);
		    pvPair=new Vector();  //used to store a property/value pair. These will all be stored in pvPairs, which will be stored in tfMapID2pvPairs 
		    pvPair.add(ijs.isubject);
		    pvPair.add(ijs.ipredicate);
		    try {
			s=ijs.jpredicate.getURI();
			//just relying on the string char length is not an accurate method of doing this as the user has probably
			//not set his ZVTM font to be a fixed width font. We should use the Graphics.getFontMetrics(), but this is 
			//computationally expensive, so we'll do it only if this method rely causes problems
			if (s.length()>longestProperty.length()){longestProperty=s;}
			if (ijs.iobjectr!=null){//object of this statement is a resource (with no outgoing
			    // edge and no other incoming edge except the one we are dealing with now)
			    if (!ijs.jobjectr.isAnon()){//we do not want take b-node IDs into account here
				pvPair.add(ijs.iobjectr);
				s=ijs.iobjectr.getGraphLabel();
				if (s.length()>longestResourceURI.length()){longestResourceURI=s;}
			    }
			}
			else if (ijs.iobjectl!=null){//object of this statement is a literal
			    pvPair.add(ijs.iobjectl);
			    s=ijs.jobjectl.getString();
			    if (s.length()>longestLiteralValue.length()){longestLiteralValue=s;}
			}
		    }
		    catch (RDFException ex){application.errorMessages.append("Error: printDOTStatementsTF(): "+ex+"\n");application.reportError=true;}
		    pvPairs.add(pvPair);
		}
		tfMapID2pvPairs.put(aUniqueStructID,pvPairs);
		//store the final longest object value (from URI or literal text) in longestLiteralValue (instead of creating another variable)
		//and prepare the literal label (truncate and escape some chars from the initial value), including white space which is 
		//interpreted as a separator in records by DOT
		if (longestResourceURI.length()>Editor.MAX_LIT_CHAR_COUNT || longestResourceURI.length()>longestLiteralValue.length()){//the longest thing is a resource URI, keep it as it is
		    longestLiteralValue=longestResourceURI;
		}
		else {//the longest thing is a literal value (even truncated by MAX_LITERAL_CHAR_COUNT
		    longestLiteralValue=Utils.replaceString(longestLiteralValue,"\n","\\ ");
		    longestLiteralValue=Utils.replaceString(longestLiteralValue,"\f","\\ ");
		    longestLiteralValue=Utils.replaceString(longestLiteralValue,"\r","\\ ");
		    longestLiteralValue=Utils.replaceString(longestLiteralValue,"\t","\\ ");
		    longestLiteralValue=Utils.replaceString(longestLiteralValue," ","\\ ");
		    if (longestLiteralValue.indexOf('"')!= -1){longestLiteralValue=Utils.replaceString(longestLiteralValue,"\"","\\\"");}
		    // Anything beyond MAX_LIT_CHAR_COUNT chars makes the graph too large
		    longestLiteralValue=((longestLiteralValue.length()>=Editor.MAX_LIT_CHAR_COUNT) ? longestLiteralValue.substring(0,Editor.MAX_LIT_CHAR_COUNT)+"\\ ..." : longestLiteralValue);
		}
		//now we know the longest propertyURI and the longest object value (and it has been properly escape)
		//put them in every row
		for (int i=0;i<v.size()-1;i++){//for each statement having the same subject (minus 1)
		    pw.print("{"+longestProperty+" | "+longestLiteralValue+"} | ");
		}
		pw.print("{"+longestProperty+" | "+longestLiteralValue+"}");
		pw.print("\"];\n");//close the record declaration
	    }
	}
    }

    void printDOTResource(IResource ir,Resource jr,Object shapeType,PrintWriter pw){//resource without any visible statement
	if (pw == null) return;
	try {
	    /*find out if resource is already in the DOT file by checking if its mapID il null or not
	      this might be the case if a visible statement declaring this resource as its object was
	      processed by the StatementHandler (StyleSH) before an invisible statement involving this
	      resource as subject or object (to be shown) ; to find out if it is already declared in
	      the DOT file, we check whether the mapID is null or not (this is safe as all visible 
	      statements have already been processed)*/
	    boolean nodeAlreadyInDOTFile=true;
	    if (ir.getMapID()==null){
		String aUniqueRID=RESOURCE_MAPID_PREFIX+nextNodeID.toString();
		ir.setMapID(aUniqueRID);
		incNodeID();
		nodeAlreadyInDOTFile=false;
	    }
	    if (ir.isAnon()) {/*b-node (subject) - this is a little weird, showing an unlinked AND anonymous resource... should we really output it?
				there is no mean to identify it (except for its anonID, which has no real value)*/
		if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (subject)
		    pw.println("\""+Editor.ANON_NODE+IResource.getJenaAnonId(jr.getId())+"\" [shape=\""+GraphStylesheet.gss2dotShape(shapeType)+"\",URL=\"" +ir.getMapID()+"\",style=filled];");
		}
	    } 
	    else {//named resources (URI) (subject)
 		String rident=ir.getGraphLabel();
 		if (!nodeAlreadyInDOTFile){//declare the node itself if this hasn't been done yet (subject)
		    pw.println("\""+rident+"\" [shape=\""+GraphStylesheet.gss2dotShape(shapeType)+"\",URL=\"" +ir.getMapID()+"\",style=filled];");
		}
	    }
	}
	catch (RDFException ex){application.errorMessages.append("Error: printDOTResource(): "+ex+"\n");application.reportError=true;}
    }

    void displaySVGAndStyle(Document d,ProgPanel pp){
	Element svgRoot=d.getDocumentElement();
	//get the space width and height and set an offset for the SVG interpreter so that all objects
	//do not get created in the same quadrant (south-east)
	pp.setPBValue(82);
	pp.setSecLabel("Initalizing ZVTM Virtual Space...");
	if (svgRoot.hasAttribute("width") && svgRoot.hasAttribute("height")){
	    String width=svgRoot.getAttribute("width");
	    String height=svgRoot.getAttribute("height");
	    try {
		long Xoffset= -(new Long(width.substring(0,width.length()-2))).longValue()/2;
		long Yoffset= -(new Long(height.substring(0,height.length()-2))).longValue()/2;
		SVGReader.setPositionOffset(Xoffset,Yoffset);
	    }
	    catch (IndexOutOfBoundsException ex){} //if we run into any problem, just forget this
	}
	pp.setPBValue(84);
	pp.setSecLabel("Building ZVTM Glyphs...");
	//dealing with SVG output by GraphViz 1.7.11 or later  (but tested only with graphviz 1.9.0)
	NodeList objects=svgRoot.getElementsByTagName("g").item(0).getChildNodes();
	for (int i=0;i<objects.getLength();i++){
	    Node obj=(Node)objects.item(i);
	    if (obj.getNodeType()==Node.ELEMENT_NODE){processSVGNode((Element)obj,true,pp);}
	}
	/*when running isaviz under Linux (and perhaps other posix systems), nodes are too small 
	  (width) w.r.t the text (don't know if it is a graphviz or java problem - anyway,
	  we correct it by adjusting widths a posteriori*/
	/*also do it under windows as some literals, sometimes have the same problem*/
	pp.setPBValue(88);
	pp.setSecLabel("Adjusting Geometry...");
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    application.geomMngr.correctResourceTextAndShape((IResource)e.nextElement());
	}
	for (Enumeration e=application.literals.elements();e.hasMoreElements();){
	    application.geomMngr.correctLiteralTextAndShape((ILiteral)e.nextElement());
	}
	Vector v;
	for (Enumeration e=application.propertiesByURI.elements();e.hasMoreElements();){
	    v=(Vector)e.nextElement();
	    for (int i=0;i<v.size();i++){
		application.geomMngr.correctPropertyTextAndShape((IProperty)v.elementAt(i));
	    }
	}
 	SVGReader.setPositionOffset(0,0);  //reset position offset (might affect anything that uses SVGReader methods, like constructors in VPath)
	pp.setPBValue(89);
	pp.setSecLabel(" ");
    }

    void assignStyleToGraph(){
// 	//key=font family as String; value=hashtable
// 	//                             key=font size as Integer
// 	//                             value=Vector of 3 elements:
// 	//                               -1st is an Integer representing the size
// 	//                               -2nd is either Short(0) or Short(1) for the weight (normal, bold)
// 	//                               -3rd is either Short(0) or Short(1) for the style (normal, italic)
	int fillind;
	int strokeind;
	Color fill;
	Color stroke;
	float width;
	float[] strokeDashArray;
	String ffamily;
	int fsize;
	short fweight,fstyle;
	boolean hide;
	Glyph g1,g3;
	VText g2;
	IResource r;
	Font font;
	Integer textal;
	float sizeFactor;
	StyleInfoR sir;
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    r=(IResource)e.nextElement();
	    if (r.isVisuallyRepresented()){
		sir=application.gssMngr.getStyle(r);
		hide=(sir.getVisibility().equals(GraphStylesheet.VISIBILITY_HIDDEN)) ? true : false;
		fill=sir.getFillColor();
		stroke=sir.getStrokeColor();
		strokeDashArray=sir.getStrokeDashArray();
		width=sir.getStrokeWidth().floatValue();
		ffamily=sir.getFontFamily();
		fsize=sir.getFontSize().intValue();
		fweight=sir.getFontWeight().shortValue();
		fstyle=sir.getFontStyle().shortValue();
		textal=sir.getTextAlignment();
		sizeFactor = sir.getRelativeSize();
		g1=r.getGlyph();
		g2=r.getGlyphText();
		if (fill!=null){
		    fillind=ConfigManager.addColor(fill);
		    r.setFillColor(fillind);
		}
		else {
		    fillind=ConfigManager.defaultRFIndex;
		    r.setFillColor(fillind);
		}
		if (stroke!=null){
		    strokeind=ConfigManager.addColor(stroke);
		    r.setStrokeColor(strokeind);
		}
		else {
		    strokeind=ConfigManager.defaultRTBIndex;
		    r.setStrokeColor(strokeind);
		}
		ConfigManager.assignStrokeToGlyph(g1,width,strokeDashArray);
		if ((font=ConfigManager.rememberFont(ConfigManager.fonts,ffamily,fsize,fweight,fstyle))!=null && g2!=null){
		    g2.setSpecialFont(font);
		}
		// do resizing before text align so as to prevent shape from overlapping label
		if (sizeFactor != 1.0f){
		    g1.reSize(sizeFactor);
		}
		if ((!textal.equals(Style.TA_CENTER)) && g2!=null){//if label is not centered and label actually exists, align it
		    /*note: this can be done here because the text/shape/spline adjustment has already been done
		      in displaySVGAndStyle() ; if it were to be done somewhere else, it would be necessary to check
		      that it still happens before doing the styled alignment (as it would override (even partially) what is done here).
		    */
		    application.geomMngr.alignText(g1,g2,textal);
		    r.setTextAlign(textal.intValue());
		}
	    }
	}
	ILiteral l;
	StyleInfoL sil;
	for (int i=0;i<application.literals.size();i++){
	    l=(ILiteral)application.literals.elementAt(i);
	    if (l.isVisuallyRepresented()){
		sil=application.gssMngr.getStyle(l);
		hide=(sil.getVisibility().equals(GraphStylesheet.VISIBILITY_HIDDEN)) ? true : false;
		fill=sil.getFillColor();
		stroke=sil.getStrokeColor();
		strokeDashArray=sil.getStrokeDashArray();
		width=sil.getStrokeWidth().floatValue();
		ffamily=sil.getFontFamily();
		fsize=sil.getFontSize().intValue();
		fweight=sil.getFontWeight().shortValue();
		fstyle=sil.getFontStyle().shortValue();
		textal=sil.getTextAlignment();
		g1=l.getGlyph();
		g2=l.getGlyphText();
		if (fill!=null){
		    fillind=ConfigManager.addColor(fill);
		    l.setFillColor(fillind);
		}
		else {
		    fillind=ConfigManager.defaultLFIndex;
		    l.setFillColor(fillind);
		}
		if (stroke!=null){
		    strokeind=ConfigManager.addColor(stroke);
		    l.setStrokeColor(strokeind);
		}
		else {
		    strokeind=ConfigManager.defaultLTBIndex;
		    l.setStrokeColor(strokeind);
		}
		ConfigManager.assignStrokeToGlyph(g1,width,strokeDashArray);
		if ((font=ConfigManager.rememberFont(ConfigManager.fonts,ffamily,fsize,fweight,fstyle))!=null && g2!=null){
		    g2.setSpecialFont(font);
		}
		if ((!textal.equals(Style.TA_CENTER)) && g2!=null){//if label is not centered and label actually exists, align it
		    /*note: this can be done here because the text/shape/spline adjustment has already been done
		      in displaySVGAndStyle() ; if it were to be done somewhere else, it would be necessary to check
		      that it still happens before doing the styled alignment (as it would override (even partially) what is done here).
		    */
		    application.geomMngr.alignText(g1,g2,textal);
		    l.setTextAlign(textal.intValue());
		}
	    }
	}
	IProperty p;
	StyleInfoP sip;
	Vector v;
	for (Enumeration e=application.propertiesByURI.elements();e.hasMoreElements();){
	    v=(Vector)e.nextElement();
	    for (int i=0;i<v.size();i++){
		p=(IProperty)v.elementAt(i);
		if (p.isVisuallyRepresented()){
		    sip=application.gssMngr.getStyle(p);
		    hide=(sip.getVisibility().equals(GraphStylesheet.VISIBILITY_HIDDEN)) ? true : false;
		    fill=sip.getFillColor();
		    stroke=sip.getStrokeColor();
		    strokeDashArray=sip.getStrokeDashArray();
		    width=sip.getStrokeWidth().floatValue();
		    ffamily=sip.getFontFamily();
		    fsize=sip.getFontSize().intValue();
		    fweight=sip.getFontWeight().shortValue();
		    fstyle=sip.getFontStyle().shortValue();
		    g1=p.getGlyph();
		    g2=p.getGlyphText();
		    g3=p.getGlyphHead();
		    if (p.isLaidOutInTableForm()){//table form layout means that we have to take fill into account if it exists
			if (fill!=null){//in this style, for the cell background ; cannot use sip.getLayout() here as the table
			    fillind=ConfigManager.addColor(fill);//form layout could be originating from the object
			    p.setCellFillColor(fillind);
			    if (stroke!=null){
				strokeind=ConfigManager.addColor(stroke);
				p.setStrokeColor(strokeind);
				p.setTextColor(strokeind);
			    }
			    else {
				p.setStrokeColor(ConfigManager.defaultPBIndex);
				if (p.getObject() instanceof ILiteral){
// 				    p.setCellStrokeColor(ConfigManager.defaultLTBIndex);
				    p.setTextColor(ConfigManager.defaultLTBIndex);
				}
				else {//instanceof IResource
// 				    p.setCellStrokeColor(ConfigManager.defaultRTBIndex);
				    p.setTextColor(ConfigManager.defaultRTBIndex);
				}
			    }
			}
			else {//if there is no fill information, the cell should be colored following the same scheme as the associated object
			    if (p.getObject() instanceof ILiteral){
				fillind=ConfigManager.defaultLFIndex;
				p.setCellFillColor(fillind);
				if (stroke!=null){
				    strokeind=ConfigManager.addColor(stroke);
				    p.setStrokeColor(strokeind);
				    p.setTextColor(strokeind);
				}
				else {
				    p.setStrokeColor(ConfigManager.defaultPBIndex);
// 				    p.setCellStrokeColor(ConfigManager.defaultLTBIndex);
				    p.setTextColor(ConfigManager.defaultLTBIndex);
				}
			    }
			    else {//instanceof IResource
				fillind=ConfigManager.defaultRFIndex;
				p.setCellFillColor(fillind);
				if (stroke!=null){
				    strokeind=ConfigManager.addColor(stroke);
				    p.setStrokeColor(strokeind);
				    p.setTextColor(strokeind);
				}
				else {
				    p.setStrokeColor(ConfigManager.defaultPBIndex);
// 				    p.setCellStrokeColor(ConfigManager.defaultRTBIndex);
				    p.setTextColor(ConfigManager.defaultRTBIndex);
				}
			    }
			}
		    }
		    else {//standard node-edge layout, color everything in blue if stroke unspecified (or according to it if specified)
			if (stroke!=null){
			    strokeind=ConfigManager.addColor(stroke);
			    p.setStrokeColor(strokeind);
			    p.setTextColor(strokeind);
			}
			else {
			    p.setStrokeColor(ConfigManager.defaultPBIndex);
			    p.setTextColor(ConfigManager.defaultPTIndex);
			}
		    }
		    ConfigManager.assignStrokeToGlyph(g1,width,strokeDashArray);
		    if (g3!=null){
			//adapt arrow head size to edge's thickness (otherwise it might be too small, or even not visible)
			if (g1.getStrokeWidth()>2.0f){
			    g3.reSize(g1.getStrokeWidth()/2.0f);
			}//selecting an edge temporarily increases its thickness by 2, take that into account
		    }
		    if ((font=ConfigManager.rememberFont(ConfigManager.fonts,ffamily,fsize,fweight,fstyle))!=null && g2!=null){
			g2.setSpecialFont(font);
		    }
		}
	    }
	}
// 	strokes.clear();
// 	fonts.clear();
// 	strokes=null;
// 	fonts=null;
	boolean noVisibleAttachedStatement=true;
	boolean inspectNextProperty=true;
	/*the following code hides entities for which visibility=hidden. 
	  Stuff that had display=none has already been removed when generating the DOT file.
	  The code here could be "optimized" by remembering what's already hidden as a side effect
	  of hiding something else so that it does not get hidden twice (or more)
	  But this means constructing new data structures, and the benefits (in terms of speed) are not
	  obvious, as hiding something already hidden does not take much time at the ZVTM level, so
	  constructing the data structures and looking for stuff in them might actually take longer
	*/
	for (Enumeration e=application.resourcesByURI.elements();e.hasMoreElements();){
	    r=(IResource)e.nextElement();
	    if (r.isVisuallyRepresented()){//this checks that the resource has a representation (not the case if display=none)
		sir=application.gssMngr.getStyle(r);
		hide=(sir.getVisibility().equals(GraphStylesheet.VISIBILITY_HIDDEN)) ? true : false;
		if (hide){//if the resource has gss:visibility=hidden, hide it
		    r.setVisible(false);
		    v=r.getIncomingPredicates();//also hide all incoming predicates (predicates for which the resource is the object)
		    if (v!=null){
			for (int i=0;i<v.size();i++){
			    p=(IProperty)v.elementAt(i);
			    p.setVisible(false);
			}
		    }
		    v=r.getOutgoingPredicates();//and all outgoing predicates (predicates for which the resource is the subject)
		    if (v!=null){
			for (int i=0;i<v.size();i++){
			    p=(IProperty)v.elementAt(i);
			    p.setVisible(false);
			    if (p.getObject() instanceof ILiteral){p.getObject().setVisible(false);}
			}
		    }
		}
		else if (!TablePanel.SHOW_ISOLATED_NODES){//else if the resource has visibility=visible but if orphan resources should be hidden
		    noVisibleAttachedStatement=true;//check whether there is at least one visible property coming to or from this resource
		    inspectNextProperty=true;
		    v=r.getIncomingPredicates();//if it is the case, the resource should not be hidden, if not it should be hidden as it is considered
		    if (v!=null){               //an orpahn resource from the graphical point of view (although there might be resources attached to it)
			for (int i=0;i<v.size();i++){
			    p=(IProperty)v.elementAt(i);
			    if (p.isVisuallyRepresented()){
				if (application.gssMngr.getStyle(p).getVisibility().equals(GraphStylesheet.VISIBILITY_VISIBLE)){
				    noVisibleAttachedStatement=false;
				    inspectNextProperty=false;break;
				}
			    }
			}
		    }
		    if (inspectNextProperty){
			v=r.getOutgoingPredicates();
			if (v!=null){
			    for (int i=0;i<v.size();i++){
				p=(IProperty)v.elementAt(i);
				if (p.isVisuallyRepresented()){
				    if (application.gssMngr.getStyle(p).getVisibility().equals(GraphStylesheet.VISIBILITY_VISIBLE)){
					noVisibleAttachedStatement=false;
					break;
				    }
				}
			    }
			}
		    }
		    if (noVisibleAttachedStatement){r.setVisible(false);}
		}
	    }
	}
	for (int i=0;i<application.literals.size();i++){
	    l=(ILiteral)application.literals.elementAt(i);
	    if (l.isVisuallyRepresented()){
		sil=application.gssMngr.getStyle(l);
		hide=(sil.getVisibility().equals(GraphStylesheet.VISIBILITY_HIDDEN)) ? true : false;
		if (hide){
		    l.setVisible(false);
		    l.getIncomingPredicate().setVisible(false);
		}
	    }
	}
	for (Enumeration e=application.propertiesByURI.elements();e.hasMoreElements();){
	    v=(Vector)e.nextElement();
	    for (int i=0;i<v.size();i++){
		p=(IProperty)v.elementAt(i);
		if (p.isVisuallyRepresented()){
		    sip=application.gssMngr.getStyle(p);
		    hide=(sip.getVisibility().equals(GraphStylesheet.VISIBILITY_HIDDEN)) ? true : false;
		    if (hide){
			p.setVisible(false);
			if (p.getObject() instanceof ILiteral){p.getObject().setVisible(false);}
		    }
		}
	    }
	}
    }


    public void error(Exception e){
	String message="RDFErrorHandler:Error: "+format(e);
	application.errorMessages.append(message+"\n");
	application.reportError=true;
    }
    
    public void fatalError(Exception e){
	String message="RDFErrorHandler:Fatal Error: "+format(e);
	application.errorMessages.append(message+"\n");
	application.reportError=true;
    }

    public void warning(Exception e){
	String message="RDFErrorHandler:Warning: "+format(e);
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

    protected ClosedShape getResourceShape(IResource r,Element a,boolean styling,ProgPanel pp){//we get the surrounding a element (should contain an ellipse/polygon/circle/... and a text)
	if (styling){
	    StyleInfoR sir=application.gssMngr.getStyle(r);
	    Object shape=sir.getShape();
	    if (shape==null){
		if (sir.getIcon()!=null){
		    NodeList content=a.getElementsByTagName("ellipse"); //we put a CIRCLE in the dot file
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    VImage vim=null;
		    if (sir.getIcon().toString().equals(GraphStylesheet._gssFetch)){//dynamic icon
			try {
			    URL iconURL=new URL(r.getIdentity());
			    pp.setSecLabel("Retrieving "+iconURL.toString()+" ...");
			    if (iconURL!=null && application.gssMngr.storeIcon(iconURL)){
				//storeIcon() returns true only if the ImageIcon could be retrieved, instantiated and stored
				vim=new VImage(e.vx,e.vy,0,application.gssMngr.getIcon(iconURL).getImage());
				vim.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
				pp.setSecLabel("Retrieving "+iconURL.toString()+" ...OK");
			    }
			    else {//assign default to shape and go to next test (shape instanceof Integer)
				if (GraphStylesheet.DEBUG_GSS){System.err.println("Error: there does not seem to be any icon at the following URI :"+iconURL);}
				shape=GraphStylesheet.DEFAULT_RESOURCE_SHAPE;
				pp.setSecLabel("Retrieving "+iconURL.toString()+" ...Failed");
			    }
			}
			catch (MalformedURLException mue){if (GraphStylesheet.DEBUG_GSS){System.err.println("Error:RDFLoader.getResourceShape(): malformed icon URI: "+r.getIdentity());mue.printStackTrace();}}
		    }
		    else {
			vim=new VImage(e.vx,e.vy,0,application.gssMngr.getIcon(sir.getIcon()).getImage());
			vim.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
// 			java.awt.Image img=application.gssMngr.getIcon(sir.getIcon()).getImage();
// 			if (img!=null){
// 			    vim=new VImage(e.vx,e.vy,0,img);
// 			    vim.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
// 			}
// 			else {shape=GraphStylesheet.DEFAULT_RESOURCE_SHAPE;}
		    }
		    if (vim!=null){
			if (vim.getWidth()>=vim.getHeight()){//adjust the icon to the bounding circle computed by dot
			    vim.setWidth(e.getWidth());
			}
			else {
			    vim.setHeight(e.getHeight());
			}
			return vim;
		    }
		}
		else {
		    shape=GraphStylesheet.DEFAULT_RESOURCE_SHAPE;
		}//assign default to shape and go to next test (shape instanceof Integer)
	    }
	    if (shape!=null && shape instanceof Integer){
		if (shape.equals(Style.ELLIPSE)){//if the stylesheet asks an ellipse, dot generates an SVG ellipse representing the ellipse
		    NodeList content=a.getElementsByTagName("ellipse");
		    return SVGReader.createEllipse((Element)content.item(0));
		}
		else if (shape.equals(Style.CIRCLE)){//if the stylesheet asks a circle, dot generates an SVG ellipse representing the circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VCircle(e.vx,e.vy,0,e.getHeight(),e.getColor());
		}
		else if (shape.equals(Style.DIAMOND)){//if the stylesheet asks a diamond, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VDiamond(e.vx,e.vy,0,e.getHeight(),e.getColor());
		}
		else if (shape.equals(Style.OCTAGON)){//if the stylesheet asks an octagon, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VOctagon(e.vx,e.vy,0,e.getHeight(),e.getColor());
		}
		else if (shape.equals(Style.ROUND_RECTANGLE)){//if the stylesheet asks a round rectangle, dot generates an SVG polygon representing the rectangle
		    NodeList content=a.getElementsByTagName("polygon");
		    return SVGReader.createRoundRectFromPolygon((Element)content.item(0));
		}
		else if (shape.equals(Style.TRIANGLEN)){//if the stylesheet asks a triangle, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VTriangle(e.vx,e.vy,0,e.getHeight(),e.getColor());
		}
		else if (shape.equals(Style.TRIANGLES)){//if the stylesheet asks a triangle, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VTriangleOr(e.vx,e.vy,0,e.getHeight(),e.getColor(),(float)Math.PI);
		}
		else if (shape.equals(Style.TRIANGLEE)){//if the stylesheet asks a triangle, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VTriangleOr(e.vx,e.vy,0,e.getHeight(),e.getColor(),(float)-Math.PI/2.0f);
		}
		else if (shape.equals(Style.TRIANGLEW)){//if the stylesheet asks a triangle, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VTriangleOr(e.vx,e.vy,0,e.getHeight(),e.getColor(),(float)Math.PI/2.0f);
		}
		else if (shape.equals(Style.RECTANGLE)){//if the stylesheet asks a rectangle, dot generates an SVG polygon representing the rectangle
		    NodeList content=a.getElementsByTagName("polygon");
		    return SVGReader.createRectangleFromPolygon((Element)content.item(0));
		}
		else {//for robustness (should not happen)
		    System.err.println("Error: RDFLoader.getResourceShape(): requested shape type unknown: "+shape.toString());
		    return new VRectangle(0,0,0,1,1,Color.white);
		}
	    }
	    else if (shape!=null && shape instanceof CustomShape){
		NodeList content=a.getElementsByTagName("ellipse");
		VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		float[] vertices=((CustomShape)shape).getVertices();
		Float orient=((CustomShape)shape).getOrientation();
		return new VShape(e.vx,e.vy,0,e.getHeight(),vertices,e.getColor(),(orient!=null) ? orient.floatValue(): 0.0f);
	    }
	    else if (shape!=null && shape instanceof CustomPolygon){
		NodeList content=a.getElementsByTagName("ellipse");
		VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		float[] vertices=((CustomPolygon)shape).getVertices();
		VPolygon res=new VPolygon(GeometryManager.computeVPolygonCoords(e.vx,e.vy,e.getHeight(),vertices),e.getColor());
		//res.sizeTo(e.getHeight());
		return res;
	    }
	    else {//for robustness (should not happen)
		System.err.println("Error: RDFLoader.getResourceShape(): requested shape type unknown: "+shape.toString());
		return new VEllipse(0,0,0,1,1,Color.white);
	    }
	}
	else {
	    NodeList content=a.getElementsByTagName("ellipse");
	    return SVGReader.createEllipse((Element)content.item(0));
	}
    }

    protected ClosedShape getLiteralShape(ILiteral l,Element a,boolean styling,ProgPanel pp){//we get the surrounding a element (should contain an ellipse/polygon/circle/... and a text)
	if (styling){
	    StyleInfoL sil=application.gssMngr.getStyle(l);
	    Object shape=sil.getShape();
	    if (shape==null){
		if (sil.getIcon()!=null){
		    NodeList content=a.getElementsByTagName("ellipse"); //we put a CIRCLE in the dot file
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    VImage vim=new VImage(e.vx,e.vy,0,application.gssMngr.getIcon(sil.getIcon()).getImage());
		    vim.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
		    if (vim.getWidth()>=vim.getHeight()){//adjust the icon to the bounding circle computed by dot
			vim.setWidth(e.getWidth());
		    }
		    else {
			vim.setHeight(e.getHeight());
		    }
		    return vim;
		}
		else {
		    shape=GraphStylesheet.DEFAULT_LITERAL_SHAPE;
		}//assign default to shape and go to next test (shape instanceof Integer)
	    }
	    if (shape!=null && shape instanceof Integer){
		if (shape.equals(Style.RECTANGLE)){//if the stylesheet asks a rectangle, dot generates an SVG polygon representing the rectangle
		    NodeList content=a.getElementsByTagName("polygon");
		    return SVGReader.createRectangleFromPolygon((Element)content.item(0));
		}
		else if (shape.equals(Style.ROUND_RECTANGLE)){//if the stylesheet asks a round rectangle, dot generates an SVG polygon representing the rectangle
		    NodeList content=a.getElementsByTagName("polygon");
		    return SVGReader.createRoundRectFromPolygon((Element)content.item(0));
		}
		else if (shape.equals(Style.CIRCLE)){//if the stylesheet asks a circle, dot generates an SVG ellipse representing the circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VCircle(e.vx,e.vy,0,e.getHeight(),e.getColor());
		}
		else if (shape.equals(Style.DIAMOND)){//if the stylesheet asks a diamond, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VDiamond(e.vx,e.vy,0,e.getHeight(),e.getColor());
		}
		else if (shape.equals(Style.OCTAGON)){//if the stylesheet asks a octagon, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VOctagon(e.vx,e.vy,0,e.getHeight(),e.getColor());
		}
		else if (shape.equals(Style.TRIANGLEN)){//if the stylesheet asks a triangle, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VTriangle(e.vx,e.vy,0,e.getHeight(),e.getColor());
		}
		else if (shape.equals(Style.TRIANGLES)){//if the stylesheet asks a triangle, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VTriangleOr(e.vx,e.vy,0,e.getHeight(),e.getColor(),(float)Math.PI);
		}
		else if (shape.equals(Style.TRIANGLEE)){//if the stylesheet asks a triangle, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VTriangleOr(e.vx,e.vy,0,e.getHeight(),e.getColor(),(float)-Math.PI/2.0f);
		}
		else if (shape.equals(Style.TRIANGLEW)){//if the stylesheet asks a triangle, dot generates an SVG ellipse representing the bounding circle
		    NodeList content=a.getElementsByTagName("ellipse");
		    VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		    return new VTriangleOr(e.vx,e.vy,0,e.getHeight(),e.getColor(),(float)Math.PI/2.0f);
		}
		else if (shape.equals(Style.ELLIPSE)){//if the stylesheet asks a ellipse, dot generates an SVG ellipse representing the ellipse
		    NodeList content=a.getElementsByTagName("ellipse");
		    return SVGReader.createEllipse((Element)content.item(0));
		}
		else {//for robustness (should not happen)
		    System.err.println("Error: RDFLoader.getLiteralShape(): requested shape type unknown: "+shape.toString());
		    return new VRectangle(0,0,0,1,1,Color.white);
		}
	    }
	    else if (shape!=null && shape instanceof CustomShape){
		NodeList content=a.getElementsByTagName("ellipse");
		VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		float[] vertices=((CustomShape)shape).getVertices();
		Float orient=((CustomShape)shape).getOrientation();
		return new VShape(e.vx,e.vy,0,e.getHeight(),vertices,e.getColor(),(orient!=null) ? orient.floatValue(): 0.0f);
	    }
	    else if (shape!=null && shape instanceof CustomPolygon){
		NodeList content=a.getElementsByTagName("ellipse");
		VEllipse e=SVGReader.createEllipse((Element)content.item(0));
		float[] vertices=((CustomPolygon)shape).getVertices();
		VPolygon res=new VPolygon(GeometryManager.computeVPolygonCoords(e.vx,e.vy,e.getHeight(),vertices),e.getColor());
		//res.sizeTo(e.getHeight());
		return res;
	    }
	    else {//for robustness (should not happen)
		System.err.println("Error: RDFLoader.getLiteralShape(): requested shape type unknown: "+shape.toString());
		return new VRectangle(0,0,0,1,1,Color.white);
	    }
	}
	else {
	    NodeList content=a.getElementsByTagName("polygon");
	    return SVGReader.createRectangleFromPolygon((Element)content.item(0));
	}
    }

    //Statement handler inner class--------------------------------------------------
    private class SH {
	PrintWriter pw;
	Editor application;
	int numLiterals=0;

	public SH(PrintWriter p,Editor app){
	    this.pw=p;
	    this.application=app;
	}

        /*
         * Generic handler for a Resource/Resource/Resource triple (S/P/O).
	 *@param subj the subject
	 *@param pred the predicate
	 *@param obj the object (as a Resource)
	 */
        public void statement(Resource subj, Property pred, Resource obj){
	    Vector v=processStatement(subj,pred,obj);
	    statementDotResource((IResource)v.elementAt(0),subj,(IProperty)v.elementAt(1),pred,(IResource)v.elementAt(2),obj);
        }

        /*
         * Generic handler for a Resource/Resource/Resource triple (S/P/O).
	 *@param subj the subject
	 *@param pred the predicate
	 *@param obj the object (as a Literal)
	 */
        public void statement(Resource subj, Property pred, Literal lit){
	    Vector v=processStatement(subj,pred,lit);
            numLiterals++;
	    statementDotLiteral((IResource)v.elementAt(0),subj,(IProperty)v.elementAt(1),pred,(ILiteral)v.elementAt(2),lit);
        }


        /*
         * Handler for a Resource/Resource/Resource triple (S/P/O).
	 * Outputs the given triple using Dot syntax.
	 *
	 * Each triple will be output in three lines of DOT code as
	 * follows (not including the complication of anon nodes 
	 * and the possiblity that the anon nodes may be named 
	 * with an empty string):
	 *
	 *   1. "<subject>" [URL="<subject">];
	 *   2. "<subject>" -> "<object>" [label="<predicate>",URL="<predicate>"];
	 *   3. "<object>"  [URL="<object>"];
         *
	 *@param subj the subject
	 *@param pred the predicate
	 *@param obj the object (as a Resource)
	 */
        public void statementDotResource(IResource subj,Resource s,IProperty pred,Property p,IResource obj,Resource o){
	    if (this.pw == null) return;
	    printFirstPart(s,subj);
            this.pw.print("\" -> ");
	    String aUniqueID=PROPERTY_MAPID_PREFIX+application.rdfLdr.nextEdgeID.toString();
	    pred.setMapID(aUniqueID);
	    application.rdfLdr.incEdgeID();
	    boolean nodeAlreadyInDOTFile=true;
	    if (obj.getMapID()==null){
		String aUniqueRID=RDFLoader.RESOURCE_MAPID_PREFIX+application.rdfLdr.nextNodeID.toString();
		obj.setMapID(aUniqueRID);
		application.rdfLdr.incNodeID();
		nodeAlreadyInDOTFile=false;
	    }
	    if (o.isAnon()){
		this.pw.println("\""+obj.getGraphLabel()+"\" [label=\""+p.getURI()+"\\l\",URL=\""+aUniqueID+"\"];");
		/* "\l" at the end of the label is here to generate a left-aliggned attribute in the SVG file because 
		   since graphviz 1.8 text objects are centered around the coordinates provided with the text element*/
		if (!nodeAlreadyInDOTFile){this.pw.println("\""+obj.getGraphLabel()+"\" [URL=\""+obj.getMapID()+"\"];");}
	    }
	    else {
// 		this.pw.println("\""+o.getURI()+"\" [label=\""+p.getURI()+"\\l\",URL=\""+aUniqueID+"\"];");
// 		if (!nodeAlreadyInDOTFile){this.pw.println("\""+o.getURI()+"\" [URL=\""+obj.getMapID()+"\"];");}
		this.pw.println("\""+obj.getGraphLabel()+"\" [label=\""+p.getURI()+"\\l\",URL=\""+aUniqueID+"\"];");
		if (!nodeAlreadyInDOTFile){this.pw.println("\""+obj.getGraphLabel()+"\" [URL=\""+obj.getMapID()+"\"];");}
	    }
        }

        /*
         * Handler for a Resource/Resource/Literal triple (S/P/O).
	 * Outputs the given triple using Dot syntax.
	 *
	 * Each triple will be output in three lines of DOT code as
	 * follows (not including the complication of anon nodes 
	 * and the possiblity that the anon nodes may be named 
	 * with an empty string):
         *
	 *   1. "<subject>" [URL="<subject">];
	 *   2. "<subject>" -> "<literal>" [label="<predicate>",URL="mapID"];
	 *   3. "aLiteralUniqueID" [shape="box",label="<1st 80 characters of the literal's value>",URL="mapID"];
         *
	 *@param subj the subject
	 *@param pred the predicate
	 *@param obj the object (as a Literal)
	 */
        public void statementDotLiteral(IResource subj,Resource s,IProperty pred,Property p,ILiteral lit,Literal l){
	    if (this.pw == null) return;
	    printFirstPart(s,subj);  // Same as Res/Res/Res
            /*
             * Before outputing the object (Literal) do the following:
             *
             * o GraphViz/DOT cannot handle embedded line terminators characters
             *   so they must be replaced with spaces
             * o Limit the number of chars to make the graph legible
             * o Escape double quotes
             */
	    try {
		String s1 = new String(l.getString());
		s1 = s1.replace('\n', ' ');
		s1 = s1.replace('\f', ' ');
		s1 = s1.replace('\r', ' ');
		if (s1.indexOf('"')!= -1){s1=Utils.replaceString(s1,"\"","\\\"");}
		// Anything beyond 80 chars makes the graph too large
		String tmpObject=((s1.length()>=Editor.MAX_LIT_CHAR_COUNT) ? s1.substring(0,Editor.MAX_LIT_CHAR_COUNT)+" ..." : s1);
		// Create a temporary label for the literal so that if
		// it is duplicated it will be unique in the graph and
		// thus have its own node.
		String aUniquePID=PROPERTY_MAPID_PREFIX+application.rdfLdr.nextEdgeID.toString();
		pred.setMapID(aUniquePID);
		application.rdfLdr.incEdgeID();
		String aUniqueLID=RDFLoader.LITERAL_MAPID_PREFIX+application.rdfLdr.nextNodeID.toString();
		lit.setMapID(aUniqueLID);
		application.rdfLdr.incNodeID();
		String tmpName = "Literal_"+Integer.toString(this.numLiterals);
		this.pw.print("\" -> \""+tmpName);
		this.pw.println("\" [label=\""+p.getURI()+"\\l\",URL=\""+aUniquePID+"\"];");// "\l" at the end of the label is here to generate a left-aliggned attribute in the SVG file because since graphviz 1.8 text objects are centered around the coordinates provided with the text element
		this.pw.println("\""+tmpName+"\" [shape=box,label=\""+tmpObject+"\\l\",URL=\""+aUniqueLID+"\"];");
	    }
	    catch (RDFException ex){application.errorMessages.append("Error: SH.statementDotLiteral: "+ex+"\n");application.reportError=true;}
        }
	
	/* 
	 * Print the first part of a triple's Dot file.  See below for
	 * more info.  This is the same regardless if the triple's
	 * object is a Resource or a Literal
	 *
	 *@param subj the subject
	 */
        public void printFirstPart(Resource subj,IResource ir){
	    try {
		boolean nodeAlreadyInDOTFile=true;
		if (ir.getMapID()==null){
		    String aUniqueRID=RDFLoader.RESOURCE_MAPID_PREFIX+application.rdfLdr.nextNodeID.toString();
		    ir.setMapID(aUniqueRID);
		    application.rdfLdr.incNodeID();
		    nodeAlreadyInDOTFile=false;
		}
		if (subj.isAnon()) {
		    if (!nodeAlreadyInDOTFile){this.pw.println("\""+Editor.ANON_NODE+IResource.getJenaAnonId(subj.getId())+"\" [URL=\"" +ir.getMapID()+"\"];");}
		    this.pw.print("\""+Editor.ANON_NODE+IResource.getJenaAnonId(subj.getId()));
		} 
		else {
// 		    if (!nodeAlreadyInDOTFile){this.pw.println("\""+subj.getURI()+"\" [URL=\"" +ir.getMapID()+"\"];");}
// 		    this.pw.print("\""+subj.getURI());
		    String rident=ir.getGraphLabel();
		    if (!nodeAlreadyInDOTFile){this.pw.println("\""+rident+"\" [URL=\"" +ir.getMapID()+"\"];");}
		    this.pw.print("\""+rident);
		}
	    }
	    catch (RDFException ex){application.errorMessages.append("Error: SH.printFirstPart(): "+ex+"\n");application.reportError=true;}
	}
    }

    //Statement handler inner class  (with support for  stylesheets)---------------------
    private class StyledSH {
	Editor application;
	Model model;
	Hashtable visibleStatements;  /*stores all statements that should be output in the DOT file
					key=subject resource URI
					value=array of 2 vectors
					-1st vector (contains properties to be laid out as node-edge)
					items are instances of ISVJenaStatement
					-2nd vector (contains properties to be laid out in a table form)
					items are instances of ISVJenaStatement*/

	Hashtable visibleResources; /*stores unlinked resources that should nevertheless be output in the DOT file
				      (some may actually be linked to a visible statement, but we will filter them
				      as explained in a comment below)
				      key=resource URI
				      value=vector with 3 elements
				      -1st element is the IResource (IsaViz)
				      -2nd element is the Resource (Jena)
				      -3rd element is the shape type (one of Style.{ELLIPSE,RECTANGLE,CIRCLE,DIAMOND,OCTAGON,TRIANGLEN,TRIANGLES,TRIANGLEE,TRIANGLEW} or a CustomShape or a CustomPolygon)*/

	public StyledSH(Editor app,Model m){
	    this.application=app;
	    this.model=m;
	    visibleStatements=new Hashtable();
	    visibleResources=new Hashtable();
	}

	void addVisibleStatement(IResource subject,IProperty predicate,IResource object,Resource s,Property p,Resource o,boolean inTable,Object sShapeType,Object oShapeType){/*sShapeType and oShapeType are (each) one of Style.{ELLIPSE,RECTANGLE,CIRCLE,DIAMOND,OCTAGON,TRIANGLEN,TRIANGLES,TRIANGLEE,TRIANGLEW} or a CustomShape */
	    ISVJenaStatement pair=new ISVJenaStatement(subject,predicate,object,s,p,o,sShapeType,oShapeType);
	    String sURI=subject.getIdentity();
	    if (visibleStatements.containsKey(sURI)){
		Vector[] ar=(Vector[])visibleStatements.get(sURI);
		if (inTable && GSSManager.objectCanBeDisplayedInTable(object)){
		    ar[1].add(pair);
		}
		else {
		    ar[0].add(pair);
		}
	    }
	    else {
		Vector[] ar=new Vector[2];
		Vector v1=new Vector();
		Vector v2=new Vector();
		if (inTable && GSSManager.objectCanBeDisplayedInTable(object)){v2.add(pair);}
		else {v1.add(pair);}
		ar[0]=v1;
		ar[1]=v2;
		visibleStatements.put(sURI,ar);
	    }
	    String oURI=object.getIdentity();
	    //if the subject and/or object of this statement was previously put in visibleResources, remove it from there
	    if (visibleResources.containsKey(sURI)){
		visibleResources.remove(sURI);
	    }
	    if (visibleResources.containsKey(oURI)){
		visibleResources.remove(oURI);
	    }
	}

	void addVisibleStatement(IResource subject,IProperty predicate,ILiteral object,Resource s,Property p,Literal o,boolean inTable,Object sShapeType,Object oShapeType){/*sShapeType and oShapeType are (each) one of Style.{ELLIPSE,RECTANGLE,CIRCLE,DIAMOND,OCTAGON,TRIANGLEN,TRIANGLES,TRIANGLEE,TRIANGLEW} or a CustomShape or a CustomPolygon */
	    ISVJenaStatement pair=new ISVJenaStatement(subject,predicate,object,s,p,o,sShapeType,oShapeType);
	    String sURI=subject.getIdentity();
	    if (visibleStatements.containsKey(sURI)){
		Vector[] ar=(Vector[])visibleStatements.get(sURI);
		if (inTable){
		    ar[1].add(pair);
		}
		else {
		    ar[0].add(pair);
		}
	    }
	    else {
		Vector[] ar=new Vector[2];
		Vector v1=new Vector();
		Vector v2=new Vector();
		if (inTable){v2.add(pair);}
		else {v1.add(pair);}
		ar[0]=v1;
		ar[1]=v2;
		visibleStatements.put(sURI,ar);
	    }
	    //if the subject of this statement was previously put in visibleResources, remove it from there
	    if (visibleResources.containsKey(sURI)){
		visibleResources.remove(sURI);
	    }
	}

	/*although we remove resources from visibleResources when they are part of a visible statement,
	  there is still the possibility that a resource can be added to visibleResources after it has
	  been processed as an object in a visible statement (since we do not keep an easily accessible
	  record of the objects of visible statements. It does not really matter, as we are going to filter
	  the list of visible resources again when generating the DOT file, including only the ones that do
	  not yet have a mapID (and since we do that after generating all visible statements, there is no
	  ambiguity)
	*/

	void addVisibleResource(IResource ir,Resource jr,Object shapeType){/*shapeType is one of Style.{ELLIPSE,RECTANGLE,CIRCLE,DIAMOND,OCTAGON,TRIANGLEN,TRIANGLES,TRIANGLEE,TRIANGLEW} or a CustomShape or a CustomPolygon */
	    String rURI=ir.getIdentity();
	    if (!visibleStatements.containsKey(rURI)){
		if (!visibleResources.containsKey(rURI)){
		    Vector v=new Vector();
		    v.add(ir);
		    v.add(jr);
		    v.add(shapeType);
		    visibleResources.put(rURI,v);
		}
	    }
	}

        /*
         *handler for a Resource/Property/Resource triple (S/P/O).
	 *@param subj the subject
	 *@param pred the predicate
	 *@param obj the object (as a Resource)
	 */
        public void statement(Resource subj, Property pred, Resource obj,Vector stmts){
	    stmts.add(processStyledStatement(subj,pred,obj));
// 	    statementDotResource((IResource)v.elementAt(0),subj,(IProperty)v.elementAt(1),pred,(IResource)v.elementAt(2),obj);
        }

        /*
         * Generic handler for a Resource/Property/Literal triple (S/P/O).
	 *@param subj the subject
	 *@param pred the predicate
	 *@param obj the object (as a Literal)
	 */
        public void statement(Resource subj, Property pred, Literal lit,Vector stmts){
	    stmts.add(processStyledStatement(subj,pred,lit));
// 	    statementDotLiteral((IResource)v.elementAt(0),subj,(IProperty)v.elementAt(1),pred,(ILiteral)v.elementAt(2),lit);
        }

        public void statementDotResource(IResource subj,Resource s,IProperty pred,Property p,IResource obj,Resource o){
	    StyleInfoR ssi=application.gssMngr.computeAndGetStyle(subj);
	    StyleInfoP psi=application.gssMngr.computeAndGetStyle(pred);
	    StyleInfoR osi=application.gssMngr.computeAndGetStyle(obj);
	    Integer subjectVisibility=ssi.getVisibility();
	    boolean subjectTableForm=(ssi.getLayout().equals(GraphStylesheet.TABLE_FORM)) ? true : false ;
	    Integer predicateVisibility=psi.getVisibility();
	    boolean predicateTableForm=(psi.getLayout().equals(GraphStylesheet.TABLE_FORM)) ? true : false ;
	    Integer objectVisibility=osi.getVisibility();
	    boolean objectTableForm=(osi.getLayout().equals(GraphStylesheet.TABLE_FORM)) ? true : false ;
	    Object sShapeType=ssi.getShape();   //subject shape type
	    if (sShapeType==null){
		if (ssi.getIcon()!=null){sShapeType=Style.CIRCLE;}
		else {sShapeType=GraphStylesheet.DEFAULT_RESOURCE_SHAPE;}
	    }
	    Object oShapeType=osi.getShape();   //object shape type
	    if (oShapeType==null){
		if (osi.getIcon()!=null){oShapeType=Style.CIRCLE;}
		else {oShapeType=GraphStylesheet.DEFAULT_RESOURCE_SHAPE;}
	    }
	    if (predicateVisibility.equals(GraphStylesheet.DISPLAY_NONE)){
		//if the statement itself should be hidden, we still want
		//to show the subject and object resources (provided they want to be visible) even if it does not have
		//any visible resource attached
		if (TablePanel.SHOW_ISOLATED_NODES && !(subjectVisibility.equals(GraphStylesheet.DISPLAY_NONE) || subjectVisibility.equals(GraphStylesheet.VISIBILITY_HIDDEN))){addVisibleResource(subj,s,sShapeType);}
		if (TablePanel.SHOW_ISOLATED_NODES && !(objectVisibility.equals(GraphStylesheet.DISPLAY_NONE) || objectVisibility.equals(GraphStylesheet.VISIBILITY_HIDDEN))){addVisibleResource(obj,o,oShapeType);}
	    }
	    else {
		if (!(subjectVisibility.equals(GraphStylesheet.DISPLAY_NONE) || subjectVisibility.equals(GraphStylesheet.VISIBILITY_HIDDEN))){
		    if (objectVisibility.equals(GraphStylesheet.DISPLAY_NONE)){
			if (TablePanel.SHOW_ISOLATED_NODES){addVisibleResource(subj,s,sShapeType);}
			//if the statement itself should be hidden (because the object is hidden),
			//we still want to show the subject resource even if it does not have any visible resource attached
			//(provided it wants to be visible)
		    }
		    else {//everything (subject/predicate/object) is visible, show the entire statement
			addVisibleStatement(subj,pred,obj,s,p,o,subjectTableForm || predicateTableForm,sShapeType,oShapeType);
			/*subjectTableForm || predicateTableForm means that only one table_form attribute is needed to display a property in a table form - we do not add  || objectTableForm here as tableForm=true for a resource refers to the property/value pairs for which the resource is the subject, not the object*/
		    }
		}
		else {
		    if (!(objectVisibility.equals(GraphStylesheet.DISPLAY_NONE) || objectVisibility.equals(GraphStylesheet.VISIBILITY_HIDDEN))){
			if (TablePanel.SHOW_ISOLATED_NODES){addVisibleResource(obj,o,oShapeType);}
			//if the statement itself should be hidden (because the subject is hidden),
			//we still want to show the object resource even if it does not have any visible resource attached
			//(provided it wants to be visible)
		    }
		}
	    }
        }

        public void statementDotLiteral(IResource subj,Resource s,IProperty pred,Property p,ILiteral lit,Literal l){
	    StyleInfoR ssi=application.gssMngr.computeAndGetStyle(subj);
	    StyleInfoP psi=application.gssMngr.computeAndGetStyle(pred);
	    StyleInfoL osi=application.gssMngr.computeAndGetStyle(lit);
	    Integer subjectVisibility=ssi.getVisibility();
	    boolean subjectTableForm=(ssi.getLayout().equals(GraphStylesheet.TABLE_FORM)) ? true : false ;
	    Integer predicateVisibility=psi.getVisibility();
	    boolean predicateTableForm=(psi.getLayout().equals(GraphStylesheet.TABLE_FORM)) ? true : false ;
	    Integer objectVisibility=osi.getVisibility();
	    boolean objectTableForm=(osi.getLayout().equals(GraphStylesheet.TABLE_FORM)) ? true : false ;
	    Object sShapeType=ssi.getShape();   //subject shape type
	    if (sShapeType==null){
		if (ssi.getIcon()!=null){sShapeType=Style.CIRCLE;}
		else {sShapeType=GraphStylesheet.DEFAULT_RESOURCE_SHAPE;}
	    }
	    Object oShapeType=osi.getShape();   //object shape type
	    if (oShapeType==null){
		if (osi.getIcon()!=null){oShapeType=Style.CIRCLE;}
		else {oShapeType=GraphStylesheet.DEFAULT_LITERAL_SHAPE;}
	    }
	    if (predicateVisibility.equals(GraphStylesheet.DISPLAY_NONE)){
		if (TablePanel.SHOW_ISOLATED_NODES && !(subjectVisibility.equals(GraphStylesheet.DISPLAY_NONE) || subjectVisibility.equals(GraphStylesheet.VISIBILITY_HIDDEN))){addVisibleResource(subj,s,sShapeType);}//if the statement itself should be hidden, we still want
		//to show the subject resource even if it does not have any visible resource attached
		//do not do the same for the literal, as there is no point in showing it if the statement is hidden
		//(since no new or existing statementcan be attached to it)
	    }
	    else {
		if (!(subjectVisibility.equals(GraphStylesheet.DISPLAY_NONE) || subjectVisibility.equals(GraphStylesheet.VISIBILITY_HIDDEN))){
		    if (objectVisibility.equals(GraphStylesheet.DISPLAY_NONE)){
			if (TablePanel.SHOW_ISOLATED_NODES){addVisibleResource(subj,s,sShapeType);}
			//if the statement itself should be hidden (because the object is hidden),
			//we still want to show the subject resource even if it does not have any visible resource attached
		    }
		    else {//everything (subject/predicate/object) is visible, show the entire statement
			addVisibleStatement(subj,pred,lit,s,p,l,subjectTableForm || predicateTableForm || objectTableForm,sShapeType,oShapeType);
			/*subjectTableForm || predicateTableForm || objectTableForm means that only one table_form attribute is needed to display a property in a table form - here we do add  || objectTableForm as tableForm=true for a literal refers to the property/value pair for which the literal is the object*/
		    }
		}
	    }
        }

	void clean(){
	    visibleStatements.clear();
	    visibleStatements=null;
	    visibleResources.clear();
	    visibleResources=null;
	}

	Hashtable getVisibleStatements(){
	    return visibleStatements;
	}

	Hashtable getVisibleResources(){
	    return visibleResources;
	}
	
    }

}

class SerializeErrorHandler implements RDFErrorHandler {

    Editor application;

    SerializeErrorHandler(Editor app){
	this.application=app;
    }
    
    public void error(java.lang.Exception ex){
	application.errorMessages.append("An error occured while exporting "+ex+"\n");application.reportError=true;
    }

    public void fatalError(java.lang.Exception ex){
	application.errorMessages.append("A fatal error occured while exporting "+ex+"\n");application.reportError=true;
    }

    public void warning(java.lang.Exception ex){
	application.errorMessages.append("Warning "+ex+"\n");application.reportError=true;
    }
    
}
