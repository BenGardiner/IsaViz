/*   FILE: FresnelParser.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FresnelParser.java,v 1.9 2006/10/27 09:43:09 epietrig Exp $
 */ 

package org.w3c.IsaViz.fresnel;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Vector;
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
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.w3c.IsaViz.*;

public class FresnelParser implements RDFErrorHandler {

    public static final String FRESNEL_NAMESPACE_URI = "http://www.w3.org/2004/09/fresnel#";

    /* RDF properties */
    public static final String _type = "type";
    public static final String _first = "first";
    public static final String _rest = "rest";

    /* Fresnel properties */
    public static final String _primaryClasses = "primaryClasses";
    public static final String _classLensDomain = "classLensDomain";
    public static final String _instanceLensDomain = "instanceLensDomain";
    public static final String _classFormatDomain = "classFormatDomain";
    public static final String _instanceFormatDomain = "instanceFormatDomain";
    public static final String _propertyFormatDomain = "propertyFormatDomain";
    public static final String _purpose = "purpose";
    public static final String _showProperties = "showProperties";
    public static final String _hideProperties = "hideProperties";
    public static final String _property = "property";
    public static final String _sublens = "sublens";
    public static final String _depth = "depth";
    public static final String _use = "use";
    public static final String _group = "group";
    public static final String _resourceStyle = "resourceStyle";
    public static final String _propertyStyle = "propertyStyle";
    public static final String _label = "label";
    public static final String _comment = "comment";
    public static final String _labelStyle = "labelStyle";
    public static final String _value = "value";
    public static final String _valueStyle = "valueStyle";
    public static final String _valueFormat = "valueFormat";
    public static final String _labelFormat = "labelFormat";
    public static final String _propertyFormat = "propertyFormat";
    public static final String _resourceFormat = "resourceFormat";
    public static final String _contentBefore = "contentBefore";
    public static final String _contentAfter = "contentAfter";
    public static final String _contentFirst = "contentFirst";
    public static final String _contentLast = "contentLast";
    public static final String _contentNoValue = "contentNoValue";
    public static final String _alternateProperties = "alternateProperties";
    public static final String _mergeProperties = "mergeProperties";

    /* Fresnel default values */
    public static final String _Group = FRESNEL_NAMESPACE_URI + "Group";
    public static final String _Lens = FRESNEL_NAMESPACE_URI + "Lens";
    public static final String _Format = FRESNEL_NAMESPACE_URI + "Format";
    public static final String _labelLens = FRESNEL_NAMESPACE_URI + "labelLens";
    public static final String _defaultLens = FRESNEL_NAMESPACE_URI + "defaultLens";
    public static final String _allProperties = FRESNEL_NAMESPACE_URI + "allProperties";
    public static final String _member = FRESNEL_NAMESPACE_URI + "member";
    public static final String _externalLink = FRESNEL_NAMESPACE_URI + "externalLink";
    public static final String _uri = FRESNEL_NAMESPACE_URI + "uri";
    public static final String _image = FRESNEL_NAMESPACE_URI + "image";
    public static final String _none = FRESNEL_NAMESPACE_URI + "none";
    public static final String _show = FRESNEL_NAMESPACE_URI + "show";

    /* Fresnel selector languages */
    public static final String _fslSelector = FRESNEL_NAMESPACE_URI + "fslSelector";
    public static final String _sparqlSelector = FRESNEL_NAMESPACE_URI + "sparqlSelector";

    static final short _BASIC_SELECTOR = 0;
    static final short _FSL_SELECTOR = 1;
    static final short _SPARQL_SELECTOR = 2;

    Model model;
    Property _restProperty, _firstProperty, _typeProperty;

    FresnelManager fresnelm;
    FSLNSResolver nsr;

    /* data structures holding Fresnel definitions after parsing */
    Lens[] lenses;
    Format[] formats;
    Group[] groups;

    /* base URI of Fresnel stylesheet */
    String baseURL;

    Hashtable group2lenses, group2formats;

    FresnelParser(FresnelManager fm){
	this.fresnelm = fm;
	nsr = this.fresnelm.buildNSResolver();
	group2lenses = new Hashtable();
	group2formats = new Hashtable();
    }

    void parse(File f, int whichReader){
	try {
	    FileInputStream fis = new FileInputStream(f);
	    model = ModelFactory.createDefaultModel();
	    RDFReader parser;
	    if (whichReader == RDFLoader.N3_READER){
		parser = model.getReader(RDFLoader.N3);
		parser.setProperty(RDFLoader.errorModePropertyName, "lax");
	    }
	    else {
		parser = model.getReader(RDFLoader.RDFXMLAB);
		parser.setProperty(RDFLoader.errorModePropertyName, "lax");
	    }
	    parser.setErrorHandler(this);
	    try {
		baseURL = (f.toURL()).toString();
		if (baseURL.startsWith("file:/") && !baseURL.startsWith("file:///")){
		    // ugly hack to address the file:/ vs. file:/// problem
		    baseURL = baseURL.substring(0, 6) + "//" + baseURL.substring(6);
		}
	    }
	    catch (MalformedURLException ex){
		baseURL = "";
	    }
	    parser.read(model, fis, baseURL);
	    _firstProperty = model.getProperty(Editor.RDFMS_NAMESPACE_URI+_first);
	    _restProperty = model.getProperty(Editor.RDFMS_NAMESPACE_URI+_rest);
	    _typeProperty = model.getProperty(Editor.RDFMS_NAMESPACE_URI+_type);
	    buildLenses();
	    buildFormats();
	    buildGroups();
	}
	catch (Exception ex){
	    String message="RDFErrorHandler:Warning:Fresnel "+format(ex);
	    fresnelm.application.errorMessages.append(message+"\n");
	    fresnelm.application.reportError = true;
	}
	if (fresnelm.application.reportError){
	    Editor.vsm.getView(Editor.mainView).setStatusBarText("There were error/warning messages ('Ctrl+E' to display error log)");
	    fresnelm.application.reportError = false;
	}
    }

    void buildLenses(){
	StmtIterator si = model.listStatements(null, _typeProperty, model.getResource(_Lens));
	Statement s;
	Vector v = new Vector();
	while (si.hasNext()){
	    s = si.nextStatement();
	    v.add(buildLens(s.getSubject()));
	}
	si.close();
	lenses = new Lens[v.size()];
	for (int i=0;i<lenses.length;i++){
	    lenses[i] = (Lens)v.elementAt(i);
	}
    }

    void buildFormats(){
	StmtIterator si = model.listStatements(null, _typeProperty, model.getResource(_Format));
	Statement s;
	Vector v = new Vector();
	while (si.hasNext()){
	    s = si.nextStatement();
	    v.add(buildFormat(s.getSubject()));
	}
	si.close();
	formats = new Format[v.size()];
	for (int i=0;i<formats.length;i++){
	    formats[i] = (Format)v.elementAt(i);
	}
    }

    void buildGroups(){
	StmtIterator si = model.listStatements(null, _typeProperty, model.getResource(_Group));
	Statement s;
	Vector v = new Vector();
	Property groupProperty = model.getProperty(FRESNEL_NAMESPACE_URI, _group);
	while (si.hasNext()){
	    s = si.nextStatement();
	    v.add(buildGroup(s.getSubject(), groupProperty));
	}
	si.close();
	groups = new Group[v.size()];
	for (int i=0;i<groups.length;i++){
	    groups[i] = (Group)v.elementAt(i);
	}
	group2lenses.clear();
	group2formats.clear();
    }

    Lens buildLens(Resource lensNode){
	Lens res = new Lens((lensNode.isAnon()) ? lensNode.getId().toString() : lensNode.getURI(), baseURL);
	/* process rdfs label and comment */
	StmtIterator si = lensNode.listProperties(model.getProperty(Editor.RDFS_NAMESPACE_URI, _label));
	if (si.hasNext()){
	    // only take the first one, a lens is not supposed to declare multiple labels
	    res.setLabel(si.nextStatement().getLiteral().getLexicalForm());
	}
	si.close();
	si = lensNode.listProperties(model.getProperty(Editor.RDFS_NAMESPACE_URI, _comment));
	if (si.hasNext()){
	    // only take the first one, a lens is not supposed to declare multiple comments
	    res.setComment(si.nextStatement().getLiteral().getLexicalForm());
	}
	si.close();
	/* process instanceLensDomain properties */
	si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _instanceLensDomain));
	RDFNode n;
	while (si.hasNext()){
	    n = si.nextStatement().getObject();
	    if (n instanceof Resource){
		res.addInstanceDomain(((Resource)n).getURI(), _BASIC_SELECTOR);
	    }
	    else {// instanceof Literal
	        Literal l = (Literal)n;
		String value = Utils.delLeadingAndTrailingSpaces(l.getLexicalForm());
		String dt = l.getDatatypeURI();
		if (dt == null){// basic selector (in theory, should not happen as basic selectors are givenas URIs,
		    //             not literals whose text is a URI, but we support it for robustness
		    res.addInstanceDomain(value, _BASIC_SELECTOR);
		}
		else if (dt.equals(_fslSelector)){
		    res.addInstanceDomain(FSLPath.pathFactory(value, nsr, FSLPath.NODE_STEP), _FSL_SELECTOR);
		}
		else if (dt.equals(_sparqlSelector)){
		    res.addInstanceDomain(value, _SPARQL_SELECTOR);
		}
		else {
		    System.out.println("Fresnel: Unknown selector language: "+dt);
		}
	    }
	}
	si.close();
	/* process classLensDomain properties */
	si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _classLensDomain));
	while (si.hasNext()){
	    res.addClassDomain(si.nextStatement().getResource().getURI());
	}
	si.close();
	Statement s;
	/* process property visibility */
	Vector toShow = new Vector();
	Vector toHide = new Vector();
	si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _showProperties));
	if (si.hasNext()){
	    // only take the first one, a lens is not supposed to declare multiple showProperties
	    s = si.nextStatement();
	    processSelectorList(s.getResource(), toShow);
	}
	si.close();
	// deal with hideProperties only if special value fresnel:allProperties appears in showProperties
	int apIndex = toShow.indexOf(_allProperties);
	if (apIndex != -1){
	    si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _hideProperties));
	    if (si.hasNext()){
		// only take the first one, a lens is not supposed to declare multiple hideProperties
		s = si.nextStatement();
		processSelectorList(s.getResource(), toHide);
	    }
	    si.close();
	}
	res.setPropertiesVisibility(toShow, toHide, apIndex);
	// deal with group declarations (store them temporarily until they get processed by buildGroup())
	Vector lenses;
	Resource group;
	String groupId;
	si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _group));
	while (si.hasNext()){
	    group = si.nextStatement().getResource();
	    groupId = (group.isAnon()) ? group.getId().toString() : group.getURI();
	    lenses = (group2lenses.containsKey(groupId)) ? (Vector)group2lenses.get(groupId) : new Vector();
	    lenses.add(res);
	    group2lenses.put(groupId, lenses);
	}
	si.close();
	return res;
    }

    Format buildFormat(Resource formatNode){
	Format res = new Format((formatNode.isAnon()) ? formatNode.getId().toString() : formatNode.getURI());
	/* process propertyFormatDomain properties */
	StmtIterator si = formatNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _propertyFormatDomain));
	RDFNode n;
	while (si.hasNext()){
	    n = si.nextStatement().getObject();
	    if (n instanceof Resource){
		res.addPropertyDomain(((Resource)n).getURI(), _BASIC_SELECTOR);
	    }
	    else {// instanceof Literal
	        Literal l = (Literal)n;
		String value = Utils.delLeadingAndTrailingSpaces(l.getLexicalForm());
		String dt = l.getDatatypeURI();
		if (dt == null){// basic selector (in theory, should not happen as basic selectors are givenas URIs,
		    //             not literals whose text is a URI, but we support it for robustness
		    res.addPropertyDomain(value, _BASIC_SELECTOR);
		}
		else if (dt.equals(_fslSelector)){
		    res.addPropertyDomain(FSLPath.pathFactory(value, nsr, FSLPath.NODE_STEP), _FSL_SELECTOR);
		}
		else {
		    System.out.println("Fresnel: Unsupported selector language for property format domain: "+dt);
		}
	    }
	}
	si.close();
	si = formatNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _value));
	if (si.hasNext()){
	    // only take the first one, a format is not supposed to declare multiple fresnel:value properties
	    res.setValue(si.nextStatement().getResource().getURI());
	}
	si.close();
	si = formatNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _label));
	if (si.hasNext()){
	    // only take the first one, a format is not supposed to declare multiple fresnel:label properties
	    res.setLabel(si.nextStatement().getObject());
	}
	si.close();
	// valueFormat instructions
	si = formatNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _valueFormat));
	while (si.hasNext()){
	    res.addValueFormattingInstruction(si.nextStatement().getResource());
	}
	si.close();
	// deal with group declarations (store them temporarily until they get processed by buildGroup())
	Vector formats;
	Resource group;
	String groupId;
	si = formatNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _group));
	while (si.hasNext()){
	    group = si.nextStatement().getResource();
	    groupId = (group.isAnon()) ? group.getId().toString() : group.getURI();
	    formats = (group2formats.containsKey(groupId)) ? (Vector)group2formats.get(groupId) : new Vector();
	    formats.add(res);
	    group2formats.put(groupId, formats);
	}
	si.close();
	return res;
    }

    Group buildGroup(Resource groupNode, Property groupProperty){
	Group res = new Group((groupNode.isAnon()) ? groupNode.getId().toString() : groupNode.getURI());
	if (group2lenses.containsKey(res.uri)){
	    Vector v = (Vector)group2lenses.get(res.uri);
	    for (int i=0;i<v.size();i++){
		res.addLens((Lens)v.elementAt(i));
	    }
	}
	if (group2formats.containsKey(res.uri)){
	    Vector v = (Vector)group2formats.get(res.uri);
	    for (int i=0;i<v.size();i++){
		res.addFormat((Format)v.elementAt(i));
	    }
	}
	return res;
    }

    Lens[] getLensDefinitions(){
	return lenses;
    }

    Format[] getFormatDefinitions(){
	return formats;
    }

    Group[] getGroupDefinitions(){
	return groups;
    }

    public void error(Exception e){
	e.printStackTrace();
	String message = "RDFErrorHandler:Error:Fresnel "+format(e);
	fresnelm.application.errorMessages.append(message+"\n");
	fresnelm.application.reportError = true;
    }
    
    public void fatalError(Exception e){
	e.printStackTrace();
	String message = "RDFErrorHandler:Fatal Error:Fresnel "+format(e);
	fresnelm.application.errorMessages.append(message+"\n");
	fresnelm.application.reportError = true;
    }

    public void warning(Exception e){
	e.printStackTrace();
	String message = "RDFErrorHandler:Warning:Fresnel "+format(e);
	fresnelm.application.errorMessages.append(message+"\n");
	fresnelm.application.reportError = true;
    }

    private static String format(Exception e){
	String msg = e.getMessage();
	if (msg == null){msg = e.toString();}
	if (e instanceof org.xml.sax.SAXParseException){
	    org.xml.sax.SAXParseException spe = (org.xml.sax.SAXParseException)e;
	    return msg + "[Line = " + spe.getLineNumber() + ", Column = " + spe.getColumnNumber() + "]";
	}
	else {
	    return e.toString();
	}
    }

    public static final String _nil = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";

    private void processSelectorList(Resource r, Vector values){
	// process item at this level
	if (r.hasProperty(_firstProperty)){
	    RDFNode n = r.getProperty(_firstProperty).getObject();
	    if (n instanceof Resource){
		Resource r2 = (Resource)n;
		if (r2.isAnon()){
		    //XXX: TBW  (complex case where there is info about what sublens to use)
		}
		else {
		    values.add((r2).toString());
		}
	    }
	    else {// n instanceof Literal
		Literal l = (Literal)n;
		String value = Utils.delLeadingAndTrailingSpaces(l.getLexicalForm());
		String dt = l.getDatatypeURI();
		if (dt == null){
		    values.add(value);
		}
		else if (dt.equals(_fslSelector)){
		    values.add(FSLPath.pathFactory(value, nsr, FSLPath.ARC_STEP));
		}
		// SPARQL not supported here yet
	    }
	}
	// recursive call to process next item
	if (r.hasProperty(_restProperty)){
	    Resource o = r.getProperty(_restProperty).getResource();
	    if (o.isAnon() || !o.getURI().equals(_nil)){
		processSelectorList(o, values);
	    }
	}
    }

}
