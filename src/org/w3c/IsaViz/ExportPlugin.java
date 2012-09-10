/*   FILE: ExportPlugin.java
 *   DATE OF CREATION:   Wed Apr 16 14:45:08 2003
 *   AUTHOR :            Arjohn Kampman
 *   MODIF:              Mon Aug 11 08:31:23 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
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
 * An interface for plugins that can export RDF from IsaViz.
 **/
public interface ExportPlugin {

    /**
     * Gets the label for the export menu for this plugin.
     **/
    public String getExportMenuLabel();
    
    /**
     * Exports RDF to somewhere. This method should return an OutputStream
     * to which IsaViz can write the RDF/XML-encoded data.
     *
     * @param parent a JFrame that can be used as the parent for any
     * dialog windows that will be shown.
     **/
    public OutputStream exportRDF(JFrame parent)
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
