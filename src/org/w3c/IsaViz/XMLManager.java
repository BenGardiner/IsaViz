/*   FILE: XMLManager.java
 *   DATE OF CREATION:   10/22/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Jul 25 14:29:00 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.LineSeparator;

/*in charge of loading and parsing misc. XML files (for instance SVG and ISV project files)*/

class XMLManager {

    Editor application;

    XMLManager(Editor e){
	application=e;
    }

    Document parse(File xmlFile,boolean validation) {
	try {
	    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	    factory.setValidating(validation);
	    factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",new Boolean(validation));
	    //parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace",false);
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder=factory.newDocumentBuilder();
	    Document res=builder.parse(xmlFile);
	    return res;
	}
	catch (FactoryConfigurationError e){
	    application.errorMessages.append("XMLManager.parse("+xmlFile.getAbsolutePath()+"): "+e+"\n");
	    javax.swing.JOptionPane.showMessageDialog(application.cmp,Messages.incompleteParsing+xmlFile);
	    application.reportError=true;
	    e.printStackTrace();
	    return null;
	}
	catch (ParserConfigurationException e){
	    application.errorMessages.append("XMLManager.parse("+xmlFile.getAbsolutePath()+"): "+e+"\n");
	    javax.swing.JOptionPane.showMessageDialog(application.cmp,Messages.incompleteParsing+xmlFile);
	    application.reportError=true;
	    e.printStackTrace();
	    return null;
	}
	catch (SAXException e){
	    application.errorMessages.append("XMLManager.parse("+xmlFile.getAbsolutePath()+"): "+e+"\n");
	    javax.swing.JOptionPane.showMessageDialog(application.cmp,Messages.incompleteParsing+xmlFile);
	    application.reportError=true;
	    return null;
	}
	catch (IOException e){
	    application.errorMessages.append("XMLManager.parse("+xmlFile.getAbsolutePath()+"): "+e+"\n");
	    javax.swing.JOptionPane.showMessageDialog(application.cmp,Messages.incompleteParsing+xmlFile);
	    application.reportError=true;
	    return null;
	}
	catch (Exception e){
	    application.errorMessages.append("XMLManager.parse("+xmlFile.getAbsolutePath()+"): "+e+"\n");
	    javax.swing.JOptionPane.showMessageDialog(application.cmp,Messages.incompleteParsing+xmlFile);
	    application.reportError=true;
	    return null;
	}
    }

    void serialize(Document d,File f){
	if (f!=null && d!=null){
	    OutputFormat format=new OutputFormat(d,"UTF-8",true);
	    format.setLineSeparator(LineSeparator.Web);
	    try {
		OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream(f),ConfigManager.ENCODING);
		DOMSerializer serializer=(new XMLSerializer(osw,format)).asDOMSerializer();
		serializer.serialize(d);
	    }
	    catch (IOException e){
		application.errorMessages.append("XMLManager.serialize("+f.getAbsolutePath()+"): "+e+"\n");
		javax.swing.JOptionPane.showMessageDialog(application.cmp,Messages.serializationError+f.getAbsolutePath());
		application.reportError=true;
		e.printStackTrace();
	    }
	}
    }
    
}
