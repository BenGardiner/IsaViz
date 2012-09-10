/*   FILE: WebBrowser.java
 *   DATE OF CREATION:   12/11/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Mon Dec 08 15:06:18 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;

import java.io.IOException;

class WebBrowser {

    WebBrowser(){}

    public void show(String url){
	if (url!=null && url.length()>0){   //perhaps we should try to convert it to a URL, or make the param a URL
	    String command=null;            //instead of a string
	    if (Editor.autoDetectBrowser){  //try to autodetect browser
		try {
		    if (Utils.osIsWindows()){//running under Win32
			command="rundll32 url.dll,FileProtocolHandler "+url;
			Process proc=Runtime.getRuntime().exec(command);
		    }
		    else {//UNIX and perhaps Linux - not tested yet  (no support for Mac right now)
			command="firefox -remote openURL("+url+")";
			Process proc=Runtime.getRuntime().exec(command);
			int exitCode;
			try {
			    if ((exitCode=proc.waitFor())!=0){
				command="netscape "+url;
				proc=Runtime.getRuntime().exec(command);
			    }
			}
			catch (InterruptedException ex1){javax.swing.JOptionPane.showMessageDialog(Editor.vsm.getActiveView().getFrame(),"Browser invokation failed "+command+"\n"+ex1);}
		    }
		    
		}
		catch (IOException ex2){javax.swing.JOptionPane.showMessageDialog(Editor.vsm.getActiveView().getFrame(),"Browser invokation failed "+command+"\n"+ex2);}
	    }
	    else {
		try {
		    command=Editor.browserPath+" "+Editor.browserOptions+" "+url;
		    Process proc=Runtime.getRuntime().exec(command);
		}
		catch (Exception ex3){javax.swing.JOptionPane.showMessageDialog(Editor.vsm.getActiveView().getFrame(),"Browser invokation failed "+command+"\n"+ex3);}
	    }
	}
    }

}
