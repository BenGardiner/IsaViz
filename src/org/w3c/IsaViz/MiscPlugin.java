/*   FILE: ImportPlugin.java
 *   DATE OF CREATION:   02/01/2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   $Id: MiscPlugin.java,v 1.1 2006/02/01 14:21:37 epietrig Exp $
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2003.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2006.
 *  Please first read the full copyright statement in file copyright.html
 */ 

package org.w3c.IsaViz;

import java.awt.Component;

/**
 * An interface for plugins that can import RDF into IsaViz.
 **/
public interface MiscPlugin {

    /**
     * Gets the label for the associated tab in the definition window 
     **/
    public String getTabLabel();

    /**
     * Method called once at init time ; provides the plug-in with a reference
     * to IsaViz' main object (Editor)
     **/
    public void setIsaViz(Editor app);
    
    /**
     * Method called once at init time ; should return the plugin's
     * Swing-based GUI embedded in a Component (e.g. a JPanel)
     **/
    public Component getPluginGUI();
    
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

