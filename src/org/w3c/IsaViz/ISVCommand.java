/*   FILE: ISVCommand.java
 *   DATE OF CREATION:   12/20/2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Jan 22 17:49:25 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 */

/*
 *
 *  (c) COPYRIGHT World Wide Web Consortium, 1994-2001.
 *  Please first read the full copyright statement in file copyright.html
 *
 */ 



package org.w3c.IsaViz;


/*Parent of all ISV commands (delete, copy, cut, paste, create, comment)*/

abstract class ISVCommand {

    abstract void _undo();

    abstract void _do();

}
