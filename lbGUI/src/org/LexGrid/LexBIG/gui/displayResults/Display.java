/*
 * Copyright: (c) 2004-2010 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.LexGrid.LexBIG.gui.displayResults;

import prefuse.Visualization;

/**
 * Hack class to fix bad behavior.
 * 
 * @author <A HREF="mailto:armbrust.daniel@mayo.edu">Dan Armbrust</A>
 * @version subversion $Revision: $ checked in on $Date: $
 */
public class Display extends prefuse.Display {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	public Display(Visualization visualization) {
		super(visualization);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see prefuse.Display#registerDefaultCommands()
	 */
	@Override
	protected void registerDefaultCommands() {
		// make this method do nothing. Its trying to register key stroke
		// methods
		// for swing, which don't work, and its trying to create a JFile chooser
		// which is really freaking slow.
	}

}