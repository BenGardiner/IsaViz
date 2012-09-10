/*   FILE: ImportPlugin.java
 *   DATE OF CREATION:   Wed Apr 16 14:46:40 2003
 *   AUTHOR :            Arjohn Kampman
 *   MODIF:              Mon Aug 11 08:31:19 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 

package org.w3c.IsaViz;

import java.io.*;
import javax.swing.*;

/**
 * An interface for plugins that can import RDF into IsaViz.
 **/
public interface ImportPlugin {

    /**
     * Gets the label for the import menu for this plugin.
     **/
    public String getImportMenuLabel();
    
    /**
     * Imports RDF from somewhere. This method should return an InputStream
     * on the RDF/XML-encoded data to import.
     *
     * @param parent a JFrame that can be used as the parent for any
     * dialog windows that will be shown.
     **/
    public InputStream importRDF(JFrame parent)
	throws IOException;
    
    /**
     * Gets author information about this plug-in
     **/
    public String getAuthor();
    
    /**
     * Gets information about this plug-in
     **/
    public String getName();

    /**
     * Gets version information about this plug-in
     **/
    public String getVersion();

    /**
     * Gets a URL pointing to more information about this plug-in (e.g. Web site)
     **/
    public java.net.URL getURL();

}

