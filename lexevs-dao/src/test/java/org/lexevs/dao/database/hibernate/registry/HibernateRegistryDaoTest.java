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
package org.lexevs.dao.database.hibernate.registry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.LexGrid.LexBIG.DataModel.Core.types.CodingSchemeVersionStatus;
import org.LexGrid.LexBIG.Exceptions.LBParameterException;
import org.junit.Test;
import org.lexevs.dao.test.LexEvsDbUnitTestBase;
import org.lexevs.registry.model.RegistryEntry;
import org.lexevs.registry.service.Registry.ResourceType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateRegistryDaoTest.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@TransactionConfiguration
public class HibernateRegistryDaoTest extends LexEvsDbUnitTestBase {

	/** The hibernate registry dao. */
	@Resource
	private HibernateRegistryDao hibernateRegistryDao;
	
	/** The data source. */
	@Resource
	private DataSource dataSource;
	
	
	/**
	 * Test insert coding scheme entry.
	 */
	@Test
	public void testInsertCodingSchemeEntry(){
		final Timestamp activationDate = new Timestamp(1l);
		final Timestamp deActivationDate = new Timestamp(2l);
		final Timestamp lastUpdateDate = new Timestamp(3l);
		
		RegistryEntry entry = new RegistryEntry();
		entry.setResourceType(ResourceType.CODING_SCHEME);
		entry.setActivationDate(activationDate);
		entry.setBaseRevision("1");
		entry.setDbName("db name");
		entry.setDbSchemaDescription("description");
		entry.setDbSchemaVersion("1.1");
		entry.setDbUri("dbUri://");
		entry.setDeactivationDate(deActivationDate);
		entry.setFixedAtRevision("2");
		entry.setLastUpdateDate(lastUpdateDate);
		entry.setIsLocked(true);
		entry.setPrefix("prefix");
		entry.setResourceUri("uri://");
		entry.setResourceVersion("v1");
		entry.setStatus(CodingSchemeVersionStatus.ACTIVE.toString());
		entry.setTag("tag");
		entry.setStagingPrefix("staging-prefix");
			
		hibernateRegistryDao.insertRegistryEntry(entry);
		
		JdbcTemplate template = new JdbcTemplate(dataSource);
		
		template.queryForObject("Select * from registry", new RowMapper(){

			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				
				assertNotNull(rs.getString(1));
				assertEquals(rs.getString(2), "uri://");
				assertEquals(rs.getString(3), "v1");
				assertEquals(rs.getObject(4), ResourceType.CODING_SCHEME.toString());
				assertEquals(rs.getString(5), "dbUri://");
				assertEquals(rs.getString(6), "db name");
				assertEquals(rs.getString(7), "prefix");
				assertEquals(rs.getString(8), "staging-prefix");
				assertEquals(rs.getString(9), CodingSchemeVersionStatus.ACTIVE.toString());
				assertEquals(rs.getString(10), "tag");
				assertEquals(rs.getTimestamp(11), lastUpdateDate);
				assertEquals(rs.getTimestamp(12), activationDate);
				assertEquals(rs.getTimestamp(13), deActivationDate);
				assertEquals(rs.getString(14), "1");
				assertEquals(rs.getString(15), "2");
				assertEquals("1", rs.getString(16));
				assertEquals(rs.getString(17), "1.1");
				assertEquals(rs.getString(18), "description");
			
				return true;
			}
		});
	}
	
	/**
	 * Test get coding scheme entry.
	 * 
	 * @throws LBParameterException the LB parameter exception
	 */
	@Test
	@Transactional
	public void testGetCodingSchemeEntry() throws LBParameterException{
		RegistryEntry entry = new RegistryEntry();
		entry.setPrefix("prefix");
		entry.setStatus(CodingSchemeVersionStatus.ACTIVE.toString());
		entry.setTag("tag");
		entry.setResourceUri("uri");
		entry.setResourceVersion("version");
		entry.setResourceType(ResourceType.CODING_SCHEME);
		
		hibernateRegistryDao.insertRegistryEntry(entry);
		
		RegistryEntry foundEntry = hibernateRegistryDao.getRegistryEntryForUriAndVersion("uri", "version");
		
		assertNotNull(foundEntry);
	}
	
	/**
	 * Test change tag.
	 * 
	 * @throws LBParameterException the LB parameter exception
	 */
	@Test
	@Transactional
	public void testChangeTag() throws LBParameterException{
		RegistryEntry entry = new RegistryEntry();
		entry.setPrefix("prefix");
		entry.setStatus(CodingSchemeVersionStatus.ACTIVE.toString());
		entry.setTag("tag");
		entry.setResourceUri("uri2");
		entry.setResourceVersion("version");
		entry.setResourceType(ResourceType.CODING_SCHEME);

		hibernateRegistryDao.insertRegistryEntry(entry);
		
		entry.setTag("new tag");
		hibernateRegistryDao.updateRegistryEntry(entry);
		
		RegistryEntry foundEntry = hibernateRegistryDao.getRegistryEntryForUriAndVersion("uri2", "version");
		
		assertEquals("new tag", foundEntry.getTag());
	}
}