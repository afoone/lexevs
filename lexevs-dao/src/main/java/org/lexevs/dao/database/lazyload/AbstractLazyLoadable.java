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
package org.lexevs.dao.database.lazyload;

import org.lexevs.dao.database.service.DatabaseServiceManager;

/**
 * The Class AbstractLazyLoadable.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public abstract class AbstractLazyLoadable {

	/** The database service manager. */
	private DatabaseServiceManager databaseServiceManager;
	
	/** The is hydrated. */
	private boolean isHydrated = false;
	
	/**
	 * Hydrate.
	 */
	protected void hydrate() {
		if(!isHydrated) {
			doHydrate();
			isHydrated = true;
		}
	}
	
	/**
	 * Do hydrate.
	 */
	protected abstract void doHydrate();
	
	/**
	 * Gets the database service manager.
	 * 
	 * @return the database service manager
	 */
	public DatabaseServiceManager getDatabaseServiceManager() {
		return databaseServiceManager;
	}
	
	/**
	 * Sets the database service manager.
	 * 
	 * @param databaseServiceManager the new database service manager
	 */
	public void setDatabaseServiceManager(
			DatabaseServiceManager databaseServiceManager) {
		this.databaseServiceManager = databaseServiceManager;
	}	
}