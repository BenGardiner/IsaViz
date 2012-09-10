/*   FILE: Messages.java
 *   DATE OF CREATION:   12/16/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2005.
 *  Please first read the full copyright statement in file copyright.html
 *
 *  $Id: Messages.java,v 1.7 2005/01/06 13:13:01 epietrig Exp $
 */

package org.w3c.IsaViz;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;

public class Messages {
    
    /*warning, error, help and other messages*/
    static final String reLayoutWarning="This will call GraphViz/DOT to compute a new layout for the model.\nThe new layout might be completely different from the current one, and you will not be able to undo this operation.\nDo you want to proceed?";

    static final String antialiasingWarning="Antialiasing requires additional computing resources.\nSetting it ON will noticeably reduce the refresh rate.\nIt is primarily aimed at producing higher quality images when exporting to PNG.";

    static final String incGSSstylingWarning="Applying stylesheets incrementally during editing operations might\nsignificantly slow down the responsiveness of IsaViz.";

    static final String webBrowserHelpText="--------------------------------------\nAUTOMATIC DETECTION\n--------------------------------------\nIsaViz can try to automatically detect your default web browser.\nThis feature is currently supported under Windows and some POSIX environments.\n\n--------------------------------------\nMANUAL CONFIGURATION\n--------------------------------------\nThe Path value should be the full command line path to your browser's main executable file. It can also be just this file's name if its parent directory is in your PATH environment variable.\n\nExamples:\nnetscape\n/usr/bin/netscape\nC:\\Program Files\\Internet Explorer\\IEXPLORE.EXE\n\nThe Command Line Options value is an optional field where you can put command line switches, like -remote for the UNIX version of Netscape that will open URLs in an already existing Netscape process (if it exists).";

    static final String proxyHelpText="If you are behind a firewall, you can manually set the proxy server to access remote resources.\n\nHostname should be the full name of the proxy server.\n\nPort should be the port number used to access external resources. This is a number (default value is 80).";

    static final String pngOnlyIn140FirstPart="This functionality is only available when running IsaViz using a JVM version 1.4.0 or later (it requires the ImageIO API).\nIsaViz detected JVM version ";

    static final String pngOnlyIn140SecondPart="\nDo you want to proceed anyway (this will probably cause an error)?";

    static final String incompleteParsing="The parsing might not be complete (The file is probably not well-formed XML).\n Some nodes and edges might be missing from the graph because of an error (check error log) in file ";

    static final String serializationError="An error occured while trying to serialize XML file ";

    static final String resetWarning="You are about to reset your project.\nAre you sure you want to continue?";

    static final String provideURI="You must provide an identifier.\nIf you want to make the resource anonymous, use the above checkbox.";

    static final String removePropType="At least one property in the model is of this type.\n Are you sure you want to remove the type from the list of types?\n(This will not remove the properties from the current model, but just the entry in this list).";

    static final String restart="You have to save your preferences and restart IsaViz for this change to take effect.";

    static final String commands="Misc. Commands\n"
	+"* Ctrl+N = new project (reset)\n"
	+"* Ctrl+O = open project from file\n"
	+"* Ctrl+S = save current project\n"
	+"* Ctrl+Z = undo last command (multiple levels)\n"
	+"* Ctrl+X = cut selected entitites and store them into clipboard\n"
	+"* Ctrl+C = copy selected entitites to clipboard\n"
	+"* Ctrl+V = paste clipboard content\n"
	+"* Del = delete selected entitites\n"
	+"* Ctrl+A = select all nodes\n"
	+"* Ctrl+E = pop up error log\n"
	+"* Ctrl+P = print current view\n"
	+"* Ctrl+Q = exit IsaViz\n"
	+"\n"
	+"--------------------------------------------------\n"
	+"Navigation\n"
	+"* The left mouse button action depends on which tool is selected in the icon palette\n"
	+"* At any time, press the right mouse button and drag to move in the graph\n"
	+"* At any time, hold Shift, press the right mouse button and drag vertically to zoom/unzoom\n"
	+"* At any time, click the right mouse button on a node or arc to center the view on it\n"
	+"* At any time, Mac OS X users who have a mouse featuring only one button can hold the command key down to simulate the right button\n"
	+"* Home (or Ctrl+G) = get a global view of the graph\n"
	+"* Page Down = Zoom In\n"
	+"* Page Up = Zoom Out\n"
	+"* Arrow Keys = Translation\n"
	+"* Ctrl+B = Back to previous location\n"
	+"* Ctrl+R = get a radar window displaying the whole graph\n"
	+"\n"
	+"--------------------------------------------------\n"
	+"Selection\n"
	+"* Hold Ctrl and press left mouse button (selection tools) to select multiple entities \n"
	+"* Hold Shift and press left mouse button (selection tools) to select an entity and its dependants \n"
	;

    static final String notAFile = "The specified path does not point to a file:\n";

    static final String notADirectory = "The specified path does not point to a directory:\n";

    static final String fileDoesNotExist = "This path does not point to any existing file or directory:\n";

}
