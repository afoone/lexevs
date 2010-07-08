/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
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
package org.lexevs.dao.database.ibatis.valuesets;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Exceptions.LBRevisionException;
import org.LexGrid.commonTypes.Properties;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.naming.Mappings;
import org.LexGrid.naming.SupportedAssociation;
import org.LexGrid.naming.SupportedCodingScheme;
import org.LexGrid.naming.SupportedConceptDomain;
import org.LexGrid.naming.SupportedContext;
import org.LexGrid.naming.SupportedHierarchy;
import org.LexGrid.naming.SupportedNamespace;
import org.LexGrid.naming.SupportedProperty;
import org.LexGrid.naming.SupportedSource;
import org.LexGrid.naming.SupportedStatus;
import org.LexGrid.naming.URIMap;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.LexGrid.valueSets.DefinitionEntry;
import org.LexGrid.valueSets.EntityReference;
import org.LexGrid.valueSets.ValueSetDefinition;
import org.LexGrid.valueSets.ValueSetDefinitions;
import org.LexGrid.versions.EntryState;
import org.LexGrid.versions.types.ChangeType;
import org.apache.commons.lang.StringUtils;
import org.lexevs.cache.annotation.CacheMethod;
import org.lexevs.cache.annotation.Cacheable;
import org.lexevs.cache.annotation.ClearCache;
import org.lexevs.dao.database.access.valuesets.VSDefinitionEntryDao;
import org.lexevs.dao.database.access.valuesets.VSEntryStateDao;
import org.lexevs.dao.database.access.valuesets.VSPropertyDao;
import org.lexevs.dao.database.access.valuesets.ValueSetDefinitionDao;
import org.lexevs.dao.database.access.valuesets.VSPropertyDao.ReferenceType;
import org.lexevs.dao.database.access.versions.VersionsDao;
import org.lexevs.dao.database.constants.classifier.mapping.ClassToStringMappingClassifier;
import org.lexevs.dao.database.ibatis.AbstractIbatisDao;
import org.lexevs.dao.database.ibatis.codingscheme.parameter.InsertOrUpdateURIMapBean;
import org.lexevs.dao.database.ibatis.parameter.PrefixedParameter;
import org.lexevs.dao.database.ibatis.parameter.PrefixedParameterTriple;
import org.lexevs.dao.database.ibatis.parameter.PrefixedParameterTuple;
import org.lexevs.dao.database.ibatis.valuesets.parameter.InsertOrUpdateValueSetsMultiAttribBean;
import org.lexevs.dao.database.ibatis.valuesets.parameter.InsertValueSetDefinitionBean;
import org.lexevs.dao.database.schemaversion.LexGridSchemaVersion;
import org.lexevs.dao.database.utility.DaoUtility;
import org.springframework.orm.ibatis.SqlMapClientCallback;

import com.ibatis.sqlmap.client.SqlMapExecutor;

/**
 * The Class IbatisValueSetDefinitionDao.
 * 
 * @author <a href="mailto:dwarkanath.sridhar@mayo.edu">Sridhar Dwarkanath</a>
 */
@Cacheable(cacheName = "IbatisValueSetDefinitionDao")
public class IbatisValueSetDefinitionDao extends AbstractIbatisDao implements ValueSetDefinitionDao {
	
	/** The supported datebase version. */
	private LexGridSchemaVersion supportedDatebaseVersion = LexGridSchemaVersion.parseStringToVersion("2.0");
	
	public static String VALUESETDEFINITION_NAMESPACE = "ValueSetDefinition.";
	
	public static String VS_MULTIATTRIB_NAMESPACE = "VSMultiAttrib.";
	
	public static String VS_MAPPING_NAMESPACE = "VSMapping.";
	
	private static String SUPPORTED_ATTRIB_GETTER_PREFIX = "_supported";
	
	public static String INSERT_VALUESET_DEFINITION_SQL = VALUESETDEFINITION_NAMESPACE + "insertValueSetDefinition";
	
	public static String INSERT_DEFINITION_ENTRY_SQL = VALUESETDEFINITION_NAMESPACE + "insertDefinitionEntry";
	
	public static String GET_VALUESET_DEFINITION_URIS_SQL = VALUESETDEFINITION_NAMESPACE + "getValueSetDefinitionURIs";
	
	public static String GET_VALUESET_DEFINITION_URI_FOR_VALUESET_NAME_SQL = VALUESETDEFINITION_NAMESPACE + "getValueSetDefinitionURIForValueSetName";
	
	public static String GET_VALUESET_DEFINITION_GUID_BY_VALUESET_DEFINITION_URI_SQL = VALUESETDEFINITION_NAMESPACE + "getValueSetDefinitionGuidByValueSetDefinitionURI";
	
	public static String GET_VALUESET_DEFINITION_BY_VALUESET_DEFINITION_URI_SQL = VALUESETDEFINITION_NAMESPACE + "getValueSetDefinitionByValueSetURI";
	
	public static String GET_VALUESET_DEFINITION_METADATA_BY_VALUESET_DEFINITION_URI_SQL = VALUESETDEFINITION_NAMESPACE + "getValueSetDefinitionMetaDataByValueSetURI";
	
	public static String GET_DEFINITION_ENTRY_BY_VALUESET_DEFINITION_GUID_SQL = VALUESETDEFINITION_NAMESPACE + "getDefinitionEntryByValueSetGuid";
	
	public static String REMOVE_VALUESET_DEFINITION_BY_VALUESET_DEFINITION_URI_SQL = VALUESETDEFINITION_NAMESPACE + "removevalueSetDefinitionByValueSetDefinitionURI";
	
	public static String REMOVE_DEFINITION_ENTRY_BY_VALUESET_DEFINITION_GUID_SQL = VALUESETDEFINITION_NAMESPACE + "removeDefinitionEntryByValueSetDefinitionGuid";
	
	public static String GET_SOURCE_LIST_BY_PARENT_GUID_AND_TYPE_SQL = VS_MULTIATTRIB_NAMESPACE + "getSourceListByParentGuidandType";
	
	public static String GET_CONTEXT_LIST_BY_PARENT_GUID_AND_TYPE_SQL = VS_MULTIATTRIB_NAMESPACE + "getContextListByParentGuidandType";
	
	public static String INSERT_MULTI_ATTRIB_SQL = VS_MULTIATTRIB_NAMESPACE + "insertMultiAttrib";
	
	public static String DELETE_SOURCE_BY_PARENT_GUID_AND_TYPE_SQL = VS_MULTIATTRIB_NAMESPACE + "deleteSourceByParentGuidAndType";
	
	public static String DELETE_CONTEXT_BY_PARENT_GUID_AND_TYPE_SQL = VS_MULTIATTRIB_NAMESPACE + "deleteContextByParentGuidAndType";
	
	public static String DELETE_PICKLIST_ENTRY_CONTEXT_BY_PICKLIST_GUID_SQL = VS_MULTIATTRIB_NAMESPACE + "deletePickListEntryContextByPickListGuid";
	
	public static String GET_URIMAPS_BY_REFERENCE_GUID_SQL = VS_MAPPING_NAMESPACE + "getURIMaps";
	
	public static String GET_URIMAPS_BY_REFERENCE_GUID_LOCALNAME_AND_TYPE_SQL = VS_MAPPING_NAMESPACE + "getURIMapByLocalNameAndType";
	
	public static String GET_VALUESETDEFINITIONURI_FOR_SUPPORTED_TAG_AND_VALUE_SQL = VS_MAPPING_NAMESPACE + "getValueSetDefinitionURIForSupportedTagAndValue";
	
	public static String INSERT_URIMAPS_SQL = VS_MAPPING_NAMESPACE + "insertURIMap";
	
	public static String UPDATE_URIMAPS_BY_LOCALID_SQL = VS_MAPPING_NAMESPACE + "updateUriMapByLocalId";
	
	public static String DELETE_URIMAPS_BY_REFERENCE_GUID_SQL = VS_MAPPING_NAMESPACE + "deleteMappingsByReferenceGuid";
	
	private static String GET_VALUESET_DEFINITION_METADATA_BY_UID_SQL = VALUESETDEFINITION_NAMESPACE + "getValueSetDefinitionMetadataByUId";
	
	private static String UPDATE_VALUE_SET_DEFINITION_BY_ID_SQL = VALUESETDEFINITION_NAMESPACE + "updateValueSetDefinitionByUId";
	
	private static String UPDATE_MULTI_ATTRIB_ENTRYSTATE_UID_BY_ID_AND_TYPE_SQL = VS_MULTIATTRIB_NAMESPACE + "updateMultiAttribEntryStateUId";
	
	private static String UPDATE_VALUE_SET_DEFINITION_VERSIONABLE_CHANGES_BY_ID_SQL = VALUESETDEFINITION_NAMESPACE + "updateValueSetDefVersionableChangesByUId";
	
	private static String GET_ENTRYSTATE_UID_BY_VALUESET_DEFINITION_UID_SQL = VALUESETDEFINITION_NAMESPACE + "getEntryStateUIdByValuesetDefUId";
	
	private static String UPDATE_VALUESETDEFINITION_ENTRYSTATE_UID_SQL = VALUESETDEFINITION_NAMESPACE + "updateValueSetDefinitinEntryStateUId";
	
	private static String GET_VALUESET_DEFINITION_LATEST_REVISION_ID_BY_UID = VALUESETDEFINITION_NAMESPACE + "getValueSetDefinitionLatestRevisionIdByUId";
	
	private static String GET_PREV_REV_ID_FROM_GIVEN_REV_ID_FOR_VALUESETDEF_SQL = VALUESETDEFINITION_NAMESPACE + "getPrevRevisionIdFromGivenRevIdForValueSetDefinition";
	
	private static String GET_VALUESET_DEFINITION_METADATA_FROM_HISTORY_BY_REVISION_SQL = VALUESETDEFINITION_NAMESPACE + "getValueSetDefinitionMetaDataByRevision";
	
	public static String GET_SOURCE_LIST_FROM_HISTORY_BY_PARENT_ENTRYSTATEGUID_AND_TYPE_SQL = VS_MULTIATTRIB_NAMESPACE + "getSourceListFromHistoryByParentEntryStateGuidandType";
	
	public static String GET_CONTEXT_LIST_FROM_HISTORY_BY_PARENT_ENTRYSTATEGUID_AND_TYPE_SQL = VS_MULTIATTRIB_NAMESPACE + "getContextListFromHistoryByParentEntryStateGuidandType";
	
	public static String GET_DEFINITION_ENTRY_LIST_BY_VALUESET_DEFINITION_URI_SQL = VALUESETDEFINITION_NAMESPACE + "getDefinitionEntryListByValSetDefURI";
	
	public static String GET_VALUESET_DEF_PROPERTY_LIST_BY_VALUESET_DEFINITION_URI_SQL = VALUESETDEFINITION_NAMESPACE + "getValueSetDefPropertyListByValSetDefURI";
	
	/** The versions dao. */
	private VersionsDao versionsDao;
	
	private VSDefinitionEntryDao vsDefinitionEntryDao;
	
	private VSPropertyDao vsPropertyDao;
	
	private VSEntryStateDao vsEntryStateDao;
	
	/** The class to string mapping classifier. */
	private ClassToStringMappingClassifier classToStringMappingClassifier = new ClassToStringMappingClassifier();

	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.valuesets.ValueSetDefinitionDao#getValueSetDefinitionByURI(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
//	@CacheMethod
	public ValueSetDefinition getValueSetDefinitionByURI(String valueSetDefinitionURI) {
		String prefix = this.getPrefixResolver().resolveDefaultPrefix();
		InsertValueSetDefinitionBean vsdBean = (InsertValueSetDefinitionBean) 
			this.getSqlMapClientTemplate().queryForObject(GET_VALUESET_DEFINITION_METADATA_BY_VALUESET_DEFINITION_URI_SQL, 
				new PrefixedParameter(prefix, valueSetDefinitionURI));
		
		ValueSetDefinition vsd = null;
		if (vsdBean != null)
		{
			vsd = vsdBean.getValueSetDefinition();
			
			String vsdGuid = vsdBean.getUId();
			
			vsd.setEntryState(vsEntryStateDao.getEntryStateByUId(vsdBean.getEntryStateUId()));
			
			List<DefinitionEntry> des = this.getSqlMapClientTemplate().queryForList(GET_DEFINITION_ENTRY_BY_VALUESET_DEFINITION_GUID_SQL,
					new PrefixedParameter(prefix, vsdGuid));
			
			if (des != null)
				vsd.setDefinitionEntry(des);			
			
			List<Property> props = vsPropertyDao.getAllPropertiesOfParent(vsdGuid, ReferenceType.VALUESETDEFINITION);
			
			if (props != null)				
			{
				Properties properties = new Properties();
				properties.getPropertyAsReference().addAll(props);
				vsd.setProperties(properties);
			}
			
			// Get value set definition source list
			List<Source> sourceList = this.getSqlMapClientTemplate().queryForList(GET_SOURCE_LIST_BY_PARENT_GUID_AND_TYPE_SQL, 
					new PrefixedParameterTuple(prefix, vsdGuid, ReferenceType.VALUESETDEFINITION.name())); 
			
			if (sourceList != null)
				vsd.setSource(sourceList);
			
			// Get realm or context list
			List<String> contextList = this.getSqlMapClientTemplate().queryForList(GET_CONTEXT_LIST_BY_PARENT_GUID_AND_TYPE_SQL, 
					new PrefixedParameterTuple(prefix, vsdGuid, ReferenceType.VALUESETDEFINITION.name())); 
			
			if (contextList != null)
				vsd.setRepresentsRealmOrContext(contextList);
			
			vsd.setMappings(getMappings(vsdGuid));
		}
		return vsd;
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.valuesets.ValueSetDefinitionDao#getGuidFromvalueSetDefinitionURI(java.lang.String)
	 */
	@Override
//	@CacheMethod
	public String getGuidFromvalueSetDefinitionURI(String valueSetDefinitionURI) {
		
		String prefix = this.getPrefixResolver().resolveDefaultPrefix();
		
		String valueSetDefGuid = (String) 
		this.getSqlMapClientTemplate().queryForObject(GET_VALUESET_DEFINITION_GUID_BY_VALUESET_DEFINITION_URI_SQL, 
			new PrefixedParameter(prefix, valueSetDefinitionURI));
		
		return valueSetDefGuid;
	}

	@SuppressWarnings("unchecked")
	@Override
	@CacheMethod
	public List<String> getAllValueSetDefinitionsWithNoName() throws LBException {
		return this.getSqlMapClientTemplate().queryForList(GET_VALUESET_DEFINITION_URI_FOR_VALUESET_NAME_SQL,
				new PrefixedParameter(this.getPrefixResolver().resolveDefaultPrefix(), " "));
	}

	@SuppressWarnings("unchecked")
	@Override
	@CacheMethod
	public List<String> getValueSetDefinitionURIsForName(String valueSetDefinitionName)
			throws LBException {
		if (valueSetDefinitionName == null)
			return getValueSetDefinitionURIs();		
		else if (StringUtils.isBlank(valueSetDefinitionName))
			return getAllValueSetDefinitionsWithNoName();
		else
			return this.getSqlMapClientTemplate().queryForList(GET_VALUESET_DEFINITION_URI_FOR_VALUESET_NAME_SQL,
					new PrefixedParameter(this.getPrefixResolver().resolveDefaultPrefix(), valueSetDefinitionName));
	}

	@Override
	public String insertValueSetDefinition(String systemReleaseURI,
			ValueSetDefinition vsdef, Mappings mappings) {
		
		String valueSetDefinitionGuid = this.createUniqueId();
		String vsEntryStateGuid = this.createUniqueId();
		
		String systemReleaseId = this.versionsDao.getSystemReleaseIdByUri(systemReleaseURI);
		
		EntryState entryState = vsdef.getEntryState();
		
		if (entryState != null)
		{
			this.vsEntryStateDao.insertEntryState(vsEntryStateGuid, valueSetDefinitionGuid, 
					ReferenceType.VALUESETDEFINITION.name(), null, entryState);
		}
		
		InsertValueSetDefinitionBean vsDefBean = new InsertValueSetDefinitionBean();
		vsDefBean.setUId(valueSetDefinitionGuid);
		vsDefBean.setValueSetDefinition(vsdef);
		vsDefBean.setPrefix(this.getPrefixResolver().resolveDefaultPrefix());
		vsDefBean.setSystemReleaseUId(systemReleaseId);
		vsDefBean.setEntryStateUId(vsEntryStateGuid);
		
		// insert into value set definition table
		this.getSqlMapClientTemplate().insert(INSERT_VALUESET_DEFINITION_SQL, vsDefBean);
		
		// insert definition entry
		for (DefinitionEntry vsdEntry : vsdef.getDefinitionEntryAsReference()) 
		{
			this.vsDefinitionEntryDao.insertDefinitionEntry(valueSetDefinitionGuid, vsdEntry);
		}
		
		// insert value set definition properties
		if (vsdef.getProperties() != null)
		{
			for (Property property : vsdef.getProperties().getPropertyAsReference())
			{
				this.vsPropertyDao.insertProperty(valueSetDefinitionGuid, ReferenceType.VALUESETDEFINITION, property);
			}
		}
		
		// insert realm or context list
		for (String context : vsdef.getRepresentsRealmOrContextAsReference())
		{
			InsertOrUpdateValueSetsMultiAttribBean insertOrUpdateValueSetsMultiAttribBean = new InsertOrUpdateValueSetsMultiAttribBean();
			insertOrUpdateValueSetsMultiAttribBean.setUId(this.createUniqueId());
			insertOrUpdateValueSetsMultiAttribBean.setReferenceUId(valueSetDefinitionGuid);
			insertOrUpdateValueSetsMultiAttribBean.setReferenceType(ReferenceType.VALUESETDEFINITION.name());
			insertOrUpdateValueSetsMultiAttribBean.setAttributeType(SQLTableConstants.TBLCOLVAL_SUPPTAG_CONTEXT);
			insertOrUpdateValueSetsMultiAttribBean.setAttributeValue(context);
			insertOrUpdateValueSetsMultiAttribBean.setRole(null);
			insertOrUpdateValueSetsMultiAttribBean.setSubRef(null);
			insertOrUpdateValueSetsMultiAttribBean.setEntryStateUId(vsEntryStateGuid);
			insertOrUpdateValueSetsMultiAttribBean.setPrefix(this.getPrefixResolver().resolveDefaultPrefix());
			
			this.getSqlMapClientTemplate().insert(INSERT_MULTI_ATTRIB_SQL, insertOrUpdateValueSetsMultiAttribBean);
		}
		
		// insert value set definition source list
		for (Source source : vsdef.getSourceAsReference())
		{
			InsertOrUpdateValueSetsMultiAttribBean insertOrUpdateValueSetsMultiAttribBean = new InsertOrUpdateValueSetsMultiAttribBean();
			insertOrUpdateValueSetsMultiAttribBean.setUId(this.createUniqueId());
			insertOrUpdateValueSetsMultiAttribBean.setReferenceUId(valueSetDefinitionGuid);
			insertOrUpdateValueSetsMultiAttribBean.setReferenceType(ReferenceType.VALUESETDEFINITION.name());
			insertOrUpdateValueSetsMultiAttribBean.setAttributeType(SQLTableConstants.TBLCOLVAL_SUPPTAG_SOURCE);
			insertOrUpdateValueSetsMultiAttribBean.setAttributeValue(source.getContent());
			insertOrUpdateValueSetsMultiAttribBean.setRole(source.getRole());
			insertOrUpdateValueSetsMultiAttribBean.setSubRef(source.getSubRef());
			insertOrUpdateValueSetsMultiAttribBean.setEntryStateUId(vsEntryStateGuid);
			insertOrUpdateValueSetsMultiAttribBean.setPrefix(this.getPrefixResolver().resolveDefaultPrefix());
			
			this.getSqlMapClientTemplate().insert(INSERT_MULTI_ATTRIB_SQL, insertOrUpdateValueSetsMultiAttribBean);
		}
		
		// insert value set definition mappings
		if (mappings != null)
			insertMappings(valueSetDefinitionGuid, mappings);
		
		if (vsdef.getMappings() != null)
			insertMappings(valueSetDefinitionGuid, vsdef.getMappings());
		
//		insertNonSuppliedMappings(valueSetDefinitionGuid, vsdef);
		
		// insert missing concept domain and usage context mappings
		insertConceptDomainAndUsageContextMappings(valueSetDefinitionGuid, vsdef);
		
		return valueSetDefinitionGuid;		
	}

	@Override
	public String insertHistoryValueSetDefinition(String valueSetDefUId) {

		String prefix = getPrefix();
		
		InsertValueSetDefinitionBean vsDefBean = (InsertValueSetDefinitionBean) this
				.getSqlMapClientTemplate().queryForObject(
						GET_VALUESET_DEFINITION_METADATA_BY_UID_SQL,
						new PrefixedParameter(prefix, valueSetDefUId));
	
		String histPrefix = this.getPrefixResolver().resolveHistoryPrefix();
		
		vsDefBean.setPrefix(histPrefix);
		
		this.getSqlMapClientTemplate().insert(
				INSERT_VALUESET_DEFINITION_SQL, vsDefBean);
		
		for (InsertOrUpdateValueSetsMultiAttribBean vsMultiAttrib : vsDefBean.getVsMultiAttribList())
		{
			vsMultiAttrib.setPrefix(histPrefix);
			
			this.getSqlMapClientTemplate().insert(INSERT_MULTI_ATTRIB_SQL, vsMultiAttrib);
		}
		
		if (!vsEntryStateExists(prefix, vsDefBean.getEntryStateUId())) {

			EntryState entryState = new EntryState();

			entryState.setChangeType(ChangeType.NEW);
			entryState.setRelativeOrder(0L);

			vsEntryStateDao
					.insertEntryState(vsDefBean.getEntryStateUId(),
							vsDefBean.getUId(), ReferenceType.VALUESETDEFINITION.name(), null,
							entryState);
		}
		
		return vsDefBean.getEntryStateUId();
	}
	
	@Override
	public void insertValueSetDefinitions(String systemReleaseURI,
			ValueSetDefinitions vsdefs, Mappings mappings) {

		for (ValueSetDefinition vsdef : vsdefs.getValueSetDefinitionAsReference())
		{
			insertValueSetDefinition(systemReleaseURI, vsdef, mappings);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.valuesets.ValueSetDefinitionDao#insertValueSetDefinition(java.lang.String, org.LexGrid.valueDomains.ValueSetDefinition)
	 */
	@Override
	public String insertValueSetDefinition(String systemReleaseUri,
			ValueSetDefinition definition) {
		
		return insertValueSetDefinition(systemReleaseUri, definition, null);
	}

	@Override
	public String updateValueSetDefinition(String valueSetDefUId,
			ValueSetDefinition valueSetDefinition) {

		String prefix = this.getPrefixResolver().resolveDefaultPrefix();
		String entryStateUId = this.createUniqueId();
		
		InsertValueSetDefinitionBean bean = new InsertValueSetDefinitionBean();
		bean.setPrefix(prefix);
		bean.setValueSetDefinition(valueSetDefinition);
		bean.setUId(valueSetDefUId);
		bean.setEntryStateUId(entryStateUId);
		
		this.getSqlMapClientTemplate().update(UPDATE_VALUE_SET_DEFINITION_BY_ID_SQL, bean);
		
		if( valueSetDefinition.getSourceCount() != 0 ) {
			
			this.getSqlMapClientTemplate().delete(
					DELETE_SOURCE_BY_PARENT_GUID_AND_TYPE_SQL,
					new PrefixedParameterTuple(prefix, valueSetDefUId, ReferenceType.VALUESETDEFINITION.name()));
			
			Source[] sourceList = valueSetDefinition.getSource();
			
			for (int i = 0; i < sourceList.length; i++) {
				InsertOrUpdateValueSetsMultiAttribBean insertOrUpdateValueSetsMultiAttribBean = new InsertOrUpdateValueSetsMultiAttribBean();
				insertOrUpdateValueSetsMultiAttribBean.setUId(this.createUniqueId());
				insertOrUpdateValueSetsMultiAttribBean.setReferenceUId(valueSetDefUId);
				insertOrUpdateValueSetsMultiAttribBean.setReferenceType(ReferenceType.VALUESETDEFINITION.name());
				insertOrUpdateValueSetsMultiAttribBean.setAttributeType(SQLTableConstants.TBLCOLVAL_SUPPTAG_SOURCE);
				insertOrUpdateValueSetsMultiAttribBean.setAttributeValue(sourceList[i].getContent());
				insertOrUpdateValueSetsMultiAttribBean.setRole(sourceList[i].getRole());
				insertOrUpdateValueSetsMultiAttribBean.setSubRef(sourceList[i].getSubRef());
				insertOrUpdateValueSetsMultiAttribBean.setEntryStateUId(entryStateUId);
				insertOrUpdateValueSetsMultiAttribBean.setPrefix(prefix);
				
				this.getSqlMapClientTemplate().insert(INSERT_MULTI_ATTRIB_SQL, insertOrUpdateValueSetsMultiAttribBean);
			}
		} else {
			
			this.getSqlMapClientTemplate().update(
					UPDATE_MULTI_ATTRIB_ENTRYSTATE_UID_BY_ID_AND_TYPE_SQL,
					new PrefixedParameterTriple(prefix, valueSetDefUId,
							SQLTableConstants.TBLCOLVAL_SUPPTAG_SOURCE,
							entryStateUId));
		}
		
		if( valueSetDefinition.getRepresentsRealmOrContextCount() != 0 ) {
			
			this.getSqlMapClientTemplate().delete(
					DELETE_CONTEXT_BY_PARENT_GUID_AND_TYPE_SQL,
					new PrefixedParameterTuple(prefix, valueSetDefUId,
							ReferenceType.VALUESETDEFINITION.name()));
			
			String[] contextList = valueSetDefinition.getRepresentsRealmOrContext();
			
			for (int i = 0; i < contextList.length; i++) {
				InsertOrUpdateValueSetsMultiAttribBean insertOrUpdateValueSetsMultiAttribBean = new InsertOrUpdateValueSetsMultiAttribBean();
				insertOrUpdateValueSetsMultiAttribBean.setUId(this.createUniqueId());
				insertOrUpdateValueSetsMultiAttribBean.setReferenceUId(valueSetDefUId);
				insertOrUpdateValueSetsMultiAttribBean.setReferenceType(ReferenceType.VALUESETDEFINITION.name());
				insertOrUpdateValueSetsMultiAttribBean.setAttributeType(SQLTableConstants.TBLCOLVAL_SUPPTAG_CONTEXT);
				insertOrUpdateValueSetsMultiAttribBean.setAttributeValue(contextList[i]);
				insertOrUpdateValueSetsMultiAttribBean.setRole(null);
				insertOrUpdateValueSetsMultiAttribBean.setSubRef(null);
				insertOrUpdateValueSetsMultiAttribBean.setEntryStateUId(entryStateUId);
				insertOrUpdateValueSetsMultiAttribBean.setPrefix(prefix);
				
				this.getSqlMapClientTemplate().insert(INSERT_MULTI_ATTRIB_SQL, insertOrUpdateValueSetsMultiAttribBean);
			}
		} else {
			
			this.getSqlMapClientTemplate().update(
					UPDATE_MULTI_ATTRIB_ENTRYSTATE_UID_BY_ID_AND_TYPE_SQL,
					new PrefixedParameterTriple(prefix, valueSetDefUId,
							SQLTableConstants.TBLCOLVAL_SUPPTAG_CONTEXT,
							entryStateUId));
		}
		
		return entryStateUId;
	}
	
	@Override
	public String updateValueSetDefinitionVersionableChanges(
			String valueSetDefUId, ValueSetDefinition valueSetDefinition) {
		

		String prefix = this.getPrefixResolver().resolveDefaultPrefix();
		String entryStateUId = this.createUniqueId();
		
		InsertValueSetDefinitionBean bean = new InsertValueSetDefinitionBean();
		bean.setPrefix(prefix);
		bean.setValueSetDefinition(valueSetDefinition);
		bean.setUId(valueSetDefUId);
		bean.setEntryStateUId(entryStateUId);
		
		this.getSqlMapClientTemplate().update(UPDATE_VALUE_SET_DEFINITION_VERSIONABLE_CHANGES_BY_ID_SQL, bean);
		
		this.getSqlMapClientTemplate().update(
				UPDATE_MULTI_ATTRIB_ENTRYSTATE_UID_BY_ID_AND_TYPE_SQL,
				new PrefixedParameterTriple(prefix, valueSetDefUId,
						SQLTableConstants.TBLCOLVAL_SUPPTAG_SOURCE,
						entryStateUId));
		
		this.getSqlMapClientTemplate().update(
				UPDATE_MULTI_ATTRIB_ENTRYSTATE_UID_BY_ID_AND_TYPE_SQL,
				new PrefixedParameterTriple(prefix, valueSetDefUId,
						SQLTableConstants.TBLCOLVAL_SUPPTAG_CONTEXT,
						entryStateUId));
		
		return entryStateUId;
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.valuesets.ValueSetDefinitionDao#getValueSetDefinitionURIs()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getValueSetDefinitionURIs() {
		return this.getSqlMapClientTemplate().queryForList(GET_VALUESET_DEFINITION_URIS_SQL, new PrefixedParameter(this.getPrefixResolver().resolveDefaultPrefix(), null));
	}	
	
	/**
	 * Gets the prefix.
	 * 
	 * @return the prefix
	 */
	protected String getPrefix() {
		return this.getPrefixResolver().resolveDefaultPrefix();
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.AbstractBaseDao#doGetSupportedLgSchemaVersions()
	 */
	@Override
	public List<LexGridSchemaVersion> doGetSupportedLgSchemaVersions() {
		return DaoUtility.createNonTypedList(supportedDatebaseVersion);
	}

	/**
	 * Gets the versions dao.
	 * 
	 * @return the versions dao
	 */
	public VersionsDao getVersionsDao() {
		return versionsDao;
	}

	/**
	 * Sets the versions dao.
	 * 
	 * @param versionsDao the new versions dao
	 */
	public void setVersionsDao(VersionsDao versionsDao) {
		this.versionsDao = versionsDao;
	}

	@ClearCache
	public void removeValueSetDefinitionByValueSetDefinitionURI(String valueSetDefinitionURI) {
		
		String prefix = this.getPrefixResolver().resolveDefaultPrefix();
		String valueSetDefGuid = (String) this.getSqlMapClientTemplate().queryForObject(GET_VALUESET_DEFINITION_GUID_BY_VALUESET_DEFINITION_URI_SQL, new PrefixedParameter(prefix, valueSetDefinitionURI));
		
		//remove entrystates
		this.vsEntryStateDao.deleteAllEntryStatesOfValueSetDefinitionByUId(valueSetDefGuid);
		
		// remove definition entries
		this.getSqlMapClientTemplate().delete(REMOVE_DEFINITION_ENTRY_BY_VALUESET_DEFINITION_GUID_SQL, new PrefixedParameter(prefix, valueSetDefGuid));
		
		// remove value set properties
		this.vsPropertyDao.deleteAllValueSetDefinitionProperties(valueSetDefGuid);
		
		// remove value set definition mappings
		deleteValueSetDefinitionMappings(valueSetDefGuid);
		
		// remove value set definition source list
		this.getSqlMapClientTemplate().delete(DELETE_SOURCE_BY_PARENT_GUID_AND_TYPE_SQL, new PrefixedParameterTuple(prefix, valueSetDefGuid, ReferenceType.VALUESETDEFINITION.name()));
		
		// remove realm or context list
		this.getSqlMapClientTemplate().delete(DELETE_CONTEXT_BY_PARENT_GUID_AND_TYPE_SQL, new PrefixedParameterTuple(prefix, valueSetDefGuid, ReferenceType.VALUESETDEFINITION.name()));
		
		// remove value set definition
		this.getSqlMapClientTemplate().
			delete(REMOVE_VALUESET_DEFINITION_BY_VALUESET_DEFINITION_URI_SQL, new PrefixedParameter(prefix, valueSetDefinitionURI));	
	}
	
	@SuppressWarnings("unchecked")
	private Mappings getMappings(String referenceGuid) {
		Mappings mappings = new Mappings();
		
		List<URIMap> uriMaps = this.getSqlMapClientTemplate().queryForList(	
				GET_URIMAPS_BY_REFERENCE_GUID_SQL, 
				new PrefixedParameterTuple(this.getPrefixResolver().resolveDefaultPrefix(), referenceGuid, ReferenceType.VALUESETDEFINITION.name()));
		
		for(URIMap uriMap : uriMaps) {
			DaoUtility.insertIntoMappings(mappings, uriMap);
		}
		
		return mappings;
	}
	
	@SuppressWarnings("unchecked")
	public void insertMappings(String referenceGuid, Mappings mappings){
		if(mappings == null){
			return;
		}
		for(Field field : mappings.getClass().getDeclaredFields()){
			if(field.getName().startsWith(SUPPORTED_ATTRIB_GETTER_PREFIX)){
				field.setAccessible(true);
				try {
					List<URIMap> urimapList = (List<URIMap>) field.get(mappings);
					this.insertURIMap(referenceGuid, urimapList);
				} catch (Exception e) {
					if (e.getMessage().indexOf("Duplicate") == -1 && e.getMessage().indexOf("unique constraint") == -1)
						throw new RuntimeException(e);
				} 
			}
		}
	}
	
	public void insertURIMap(final String referenceGuid,
			final List<URIMap> urimapList) {
		final String prefix  = this.getPrefixResolver().resolveDefaultPrefix();
		this.getSqlMapClientTemplate().execute(new SqlMapClientCallback(){
	
			public Object doInSqlMapClient(SqlMapExecutor executor)
			throws SQLException {
				executor.startBatch();
				for(URIMap uriMap : urimapList){
					String uriMapId = UUID.randomUUID().toString();
					
					executor.insert(INSERT_URIMAPS_SQL, 
							buildInsertOrUpdateURIMapBean(
									prefix,
									uriMapId, 
									referenceGuid,
									classToStringMappingClassifier.classify(uriMap.getClass()),
									uriMap));
				}
				return executor.executeBatch();
			}	
		});		
	}
	
	public void insertURIMap(String referenceGuid, URIMap uriMap) {
		String uriMapId = this.createUniqueId();
		this.getSqlMapClientTemplate().insert(
				INSERT_URIMAPS_SQL, buildInsertOrUpdateURIMapBean(
						this.getPrefixResolver().resolveDefaultPrefix(),
									uriMapId, 
									referenceGuid,
									classToStringMappingClassifier.classify(uriMap.getClass()),
									uriMap));
	}
	
	/**
	 * Builds the insert uri map bean.
	 * 
	 * @param prefix the prefix
	 * @param uriMapId the uri map id
	 * @param codingSchemeId the coding scheme id
	 * @param supportedAttributeTag the supported attribute tag
	 * @param uriMap the uri map
	 * 
	 * @return the insert uri map bean
	 */
	protected InsertOrUpdateURIMapBean buildInsertOrUpdateURIMapBean(String prefix, String uriMapId, String referenceGuid, String supportedAttributeTag, URIMap uriMap){
		InsertOrUpdateURIMapBean bean = new InsertOrUpdateURIMapBean();
		bean.setPrefix(prefix);
		bean.setSupportedAttributeTag(supportedAttributeTag);
		bean.setCodingSchemeUId(referenceGuid);
		bean.setReferenceType(ReferenceType.VALUESETDEFINITION.name());
		bean.setUriMap(uriMap);
		bean.setUId(uriMapId);
		
		if (uriMap instanceof SupportedHierarchy)
		{
			String associations = null;
			List<String> associationList = ((SupportedHierarchy) uriMap).getAssociationNamesAsReference();
			if (associationList != null) {
				for (int i = 0; i < associationList.size(); i++) {
					String assoc = (String) associationList.get(i);
					associations = i == 0 ? assoc : (associations += ("," + assoc));
				}
				bean.setAssociationNames(associations);
			}
		}
		
		
		return bean;
	}
	
	@ClearCache
	public void deleteValueSetDefinitionMappings(String referenceGuid) {
		this.getSqlMapClientTemplate().delete(
				DELETE_URIMAPS_BY_REFERENCE_GUID_SQL, 
				new PrefixedParameterTuple(this.getPrefixResolver().resolveDefaultPrefix(), referenceGuid, ReferenceType.VALUESETDEFINITION.name()));
	}
	
	/**
	 * @return the vsPropertyDao
	 */
	public VSPropertyDao getVsPropertyDao() {
		return vsPropertyDao;
	}

	/**
	 * @param vsPropertyDao the vsPropertyDao to set
	 */
	public void setVsPropertyDao(VSPropertyDao vsPropertyDao) {
		this.vsPropertyDao = vsPropertyDao;
	}

	/**
	 * @return the vsEntryStateDao
	 */
	public VSEntryStateDao getVsEntryStateDao() {
		return vsEntryStateDao;
	}

	/**
	 * @param vsEntryStateDao the vsEntryStateDao to set
	 */
	public void setVsEntryStateDao(VSEntryStateDao vsEntryStateDao) {
		this.vsEntryStateDao = vsEntryStateDao;
	}

	@Override
	public String getValueSetDefEntryStateUId(String valueSetDefUId) {
		
		String prefix = this.getPrefixResolver().resolveDefaultPrefix();
		
		return (String) this.getSqlMapClientTemplate().queryForObject(GET_ENTRYSTATE_UID_BY_VALUESET_DEFINITION_UID_SQL,
				new PrefixedParameter(prefix, valueSetDefUId));
	}

	@Override
	public void updateValueSetDefEntryStateUId(String valueSetDefUId,
			String entryStateUId) {

		String prefix = this.getPrefixResolver().resolveDefaultPrefix();
		
		this.getSqlMapClientTemplate().update(
				UPDATE_VALUESETDEFINITION_ENTRYSTATE_UID_SQL, 
				new PrefixedParameterTuple(prefix, valueSetDefUId, entryStateUId));
		
		this.getSqlMapClientTemplate().update(
				UPDATE_MULTI_ATTRIB_ENTRYSTATE_UID_BY_ID_AND_TYPE_SQL,
				new PrefixedParameterTriple(prefix, valueSetDefUId,
						SQLTableConstants.TBLCOLVAL_SUPPTAG_SOURCE,
						entryStateUId));
		
		this.getSqlMapClientTemplate().update(
				UPDATE_MULTI_ATTRIB_ENTRYSTATE_UID_BY_ID_AND_TYPE_SQL,
				new PrefixedParameterTriple(prefix, valueSetDefUId,
						SQLTableConstants.TBLCOLVAL_SUPPTAG_CONTEXT,
						entryStateUId));
	}

	/**
	 * @return the vsDefinitionEntryDao
	 */
	public VSDefinitionEntryDao getVsDefinitionEntryDao() {
		return vsDefinitionEntryDao;
	}

	/**
	 * @param vsDefinitionEntryDao the vsDefinitionEntryDao to set
	 */
	public void setVsDefinitionEntryDao(VSDefinitionEntryDao vsDefinitionEntryDao) {
		this.vsDefinitionEntryDao = vsDefinitionEntryDao;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@CacheMethod
	public List<String> getValueSetDefinitionURIForSupportedTagAndValue(
			String supportedTag, String value) {
		return (List<String>) this.getSqlMapClientTemplate().queryForList(GET_VALUESETDEFINITIONURI_FOR_SUPPORTED_TAG_AND_VALUE_SQL,
				new PrefixedParameterTuple(this.getPrefixResolver().resolveDefaultPrefix(), supportedTag, value));
	}

	@Override
	public void insertDefinitionEntry(ValueSetDefinition vsdef,
			DefinitionEntry definitionEntry) {
		String vsdGUID = getGuidFromvalueSetDefinitionURI(vsdef.getValueSetDefinitionURI());
		if (vsdGUID != null)
			this.vsDefinitionEntryDao.insertDefinitionEntry(vsdGUID, definitionEntry);
		
	}
	
	@Override
	public String getLatestRevision(String valueSetDefUId) {

		String prefix = this.getPrefixResolver().resolveDefaultPrefix();
		
		return (String) this.getSqlMapClientTemplate().queryForObject(
				GET_VALUESET_DEFINITION_LATEST_REVISION_ID_BY_UID, 
				new PrefixedParameter(prefix, valueSetDefUId));
	}

	
	@SuppressWarnings("unused")
	private void insertNonSuppliedMappings(String vsdGuid, ValueSetDefinition vsDef){
		if (vsDef == null)
			return;
		
		Mappings mappings = new Mappings();
		
		for (Source src : vsDef.getSourceAsReference())
		{
			SupportedSource suppSrc = new SupportedSource();
			suppSrc.setLocalId(src.getContent());
			suppSrc.setContent(src.getContent());
			suppSrc.setUri(null);
			mappings.addSupportedSource(suppSrc);
		}
		for (String ctx : vsDef.getRepresentsRealmOrContextAsReference())
		{
			SupportedContext suppCtx = new SupportedContext();
			suppCtx.setLocalId(ctx);
			suppCtx.setContent(ctx);
			suppCtx.setUri(null);
			mappings.addSupportedContext(suppCtx);
		}
		if (vsDef.getDefaultCodingScheme() != null)
		{
			SupportedCodingScheme suppCS = new SupportedCodingScheme();
			suppCS.setLocalId(vsDef.getDefaultCodingScheme());
			suppCS.setContent(vsDef.getDefaultCodingScheme());
			suppCS.setUri(null);
			mappings.addSupportedCodingScheme(suppCS);
		}
		if (vsDef.getConceptDomain() != null)
		{
			SupportedConceptDomain suppCD = new SupportedConceptDomain();
			suppCD.setLocalId(vsDef.getConceptDomain());
			suppCD.setContent(vsDef.getConceptDomain());
			suppCD.setUri(null);
			mappings.addSupportedConceptDomain(suppCD);
		}
		if (vsDef.getStatus() != null)
		{
			String status = vsDef.getStatus();
			SupportedStatus suppStatus = new SupportedStatus();
			suppStatus.setLocalId(status);
			suppStatus.setContent(status);
			suppStatus.setUri(null);
			mappings.addSupportedStatus(suppStatus);
		}
		if (vsDef.getDefinitionEntry() != null)
		{
			for (DefinitionEntry de : vsDef.getDefinitionEntryAsReference())
			{
				if (de.getCodingSchemeReference() != null)
				{
					String cs = de.getCodingSchemeReference().getCodingScheme();
					SupportedCodingScheme suppCS = new SupportedCodingScheme();
					suppCS.setLocalId(cs);
					suppCS.setContent(cs);
					suppCS.setUri(null);
					mappings.addSupportedCodingScheme(suppCS);
				}
				else if (de.getEntityReference() != null)
				{
					EntityReference er = de.getEntityReference();
					String ns = er.getEntityCodeNamespace();
					if (StringUtils.isNotEmpty(ns))
					{
						SupportedNamespace suppNS = new SupportedNamespace();
						suppNS.setLocalId(ns);
						suppNS.setContent(ns);
						suppNS.setUri(null);
						mappings.addSupportedNamespace(suppNS);
					}
					String assn = er.getReferenceAssociation();
					if (StringUtils.isNotEmpty(assn))
					{
						SupportedAssociation suppAssn = new SupportedAssociation();
						suppAssn.setLocalId(assn);
						suppAssn.setContent(assn);
						suppAssn.setUri(null);
						mappings.addSupportedAssociation(suppAssn);
					}
				}
				else if (de.getPropertyReference() != null)
				{
					String prop = de.getPropertyReference().getPropertyName();
					if (prop != null)
					{
						SupportedProperty suppProp = new SupportedProperty();
						suppProp.setLocalId(prop);
						suppProp.setContent(prop);
						suppProp.setUri(null);
						mappings.addSupportedProperty(suppProp);
					}
				}
			}
		}
		
		insertMappings(vsdGuid, mappings);
	}
	
	private void insertConceptDomainAndUsageContextMappings(String vsdGuid, ValueSetDefinition vsDef){
		if (vsDef == null)
			return;
		
		Mappings mappings = new Mappings();
		
		for (String ctx : vsDef.getRepresentsRealmOrContextAsReference())
		{
			SupportedContext suppCtx = new SupportedContext();
			suppCtx.setLocalId(ctx);
			suppCtx.setContent(ctx);
			suppCtx.setUri(null);
			mappings.addSupportedContext(suppCtx);
		}
		if (vsDef.getConceptDomain() != null)
		{
			SupportedConceptDomain suppCD = new SupportedConceptDomain();
			suppCD.setLocalId(vsDef.getConceptDomain());
			suppCD.setContent(vsDef.getConceptDomain());
			suppCD.setUri(null);
			mappings.addSupportedConceptDomain(suppCD);
		}
		
		insertMappings(vsdGuid, mappings);
	}
	
	@Override
	public boolean entryStateExists(String entryStateUId) {
		String prefix = this.getPrefix();
		
		return	super.vsEntryStateExists(prefix, entryStateUId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ValueSetDefinition resolveValueSetDefinitionByRevision(String valueSetDefURI,
			String revisionId) throws LBRevisionException {
		
		String prefix = this.getPrefix();
		String tempRevId = revisionId;
		
		String valueSetDefUId = this
				.getGuidFromvalueSetDefinitionURI(valueSetDefURI);

		if (valueSetDefUId == null) {
			throw new LBRevisionException(
					"ValueSetDefinition "
							+ valueSetDefUId
							+ " doesn't exist in lexEVS. "
							+ "Please check the URI. Its possible that the given ValueSetDefinition "
							+ "has been REMOVEd from the lexEVS system in the past.");
		}

		String valueSetDefRevisionId = this
				.getLatestRevision(valueSetDefUId);

		// 1. If 'revisionId' is null or 'revisionId' is the latest revision of the valueSetDefinition
		// then use getValueSetDefinitionByURI to get the ValueSetDefinition object and return.
		
		if (revisionId == null || valueSetDefRevisionId == null ) {
			return this.getValueSetDefinitionByURI(valueSetDefURI);
		}
		
		// 2. Get the earliest revisionId on which change was applied on given 
		// valueset definition with reference to given revisionId.
		
		HashMap revisionIdMap = (HashMap) this.getSqlMapClientTemplate()
				.queryForMap(
						GET_PREV_REV_ID_FROM_GIVEN_REV_ID_FOR_VALUESETDEF_SQL,
						new PrefixedParameterTuple(prefix, valueSetDefURI,
								revisionId), "revId", "revAppliedDate");
		
		if (revisionIdMap.isEmpty()) {
			revisionId = null;
		} else {
			revisionId = (String) revisionIdMap.keySet().toArray()[0];

			if (valueSetDefRevisionId.equals(revisionId)) {
				return this.getValueSetDefinitionByURI(valueSetDefURI);
			}
		}
			
		// 3. Get the valueSet definition data from history.
		ValueSetDefinition valueSetDefinition = null;
		InsertValueSetDefinitionBean vsDefBean = null;
			
		vsDefBean = (InsertValueSetDefinitionBean) this
				.getSqlMapClientTemplate().queryForObject(
						GET_VALUESET_DEFINITION_METADATA_FROM_HISTORY_BY_REVISION_SQL,
						new PrefixedParameterTuple(getPrefix(), valueSetDefURI,
								revisionId));
		
		if (vsDefBean != null) {
			
			valueSetDefinition = vsDefBean.getValueSetDefinition();
			
			// Get value set definition source
			List<Source> sourceList = this
					.getSqlMapClientTemplate()
					.queryForList(
							GET_SOURCE_LIST_FROM_HISTORY_BY_PARENT_ENTRYSTATEGUID_AND_TYPE_SQL,
							new PrefixedParameterTuple(prefix, vsDefBean
									.getEntryStateUId(),
									ReferenceType.VALUESETDEFINITION.name()));

			if (sourceList != null)
				valueSetDefinition.setSource(sourceList);

			// Get value set definition context
			List<String> contextList = this
					.getSqlMapClientTemplate()
					.queryForList(
							GET_CONTEXT_LIST_FROM_HISTORY_BY_PARENT_ENTRYSTATEGUID_AND_TYPE_SQL,
							new PrefixedParameterTuple(prefix, vsDefBean
									.getEntryStateUId(),
									ReferenceType.VALUESETDEFINITION.name()));

			if (contextList != null)
				valueSetDefinition.setRepresentsRealmOrContext(contextList);
		}
		
		// 4. If value set definition is not in history, get it from base table.
		if (valueSetDefinition == null) {
			InsertValueSetDefinitionBean valueSetDefBean = (InsertValueSetDefinitionBean) this
					.getSqlMapClientTemplate().queryForObject(
							GET_VALUESET_DEFINITION_METADATA_BY_UID_SQL,
							new PrefixedParameterTuple(prefix, valueSetDefUId,
									revisionId));

			valueSetDefinition = valueSetDefBean.getValueSetDefinition();

			for (InsertOrUpdateValueSetsMultiAttribBean multiAttrib : valueSetDefBean
					.getVsMultiAttribList()) {

				if (SQLTableConstants.TBLCOLVAL_SUPPTAG_SOURCE
						.equals(multiAttrib.getAttributeType())) {
					Source source = new Source();

					source.setRole(multiAttrib.getRole());
					source.setSubRef(multiAttrib.getSubRef());
					source.setContent(multiAttrib.getAttributeValue());

					valueSetDefinition.addSource(source);
				} else if (SQLTableConstants.TBLCOLVAL_SUPPTAG_CONTEXT
						.equals(multiAttrib.getAttributeType())) {

					valueSetDefinition.addRepresentsRealmOrContext(multiAttrib
							.getAttributeValue());
				}
			}
		}
		
		// 5. Get all definition entry nodes.
		
		List<String> definitionEntryRuleOrderList = this.getSqlMapClientTemplate().queryForList(
				GET_DEFINITION_ENTRY_LIST_BY_VALUESET_DEFINITION_URI_SQL,
				new PrefixedParameter(prefix, valueSetDefURI));
			
		for (String ruleOrder : definitionEntryRuleOrderList) {
			DefinitionEntry definitionEntry = null;

			try {
				definitionEntry = vsDefinitionEntryDao
						.resolveDefinitionEntryByRevision(valueSetDefURI, ruleOrder,
								revisionId);
			} catch (LBRevisionException e) {
				continue;
			}

			valueSetDefinition.addDefinitionEntry(definitionEntry);
		}
		
		// 6. Get all value set definition properties.
		
		List<String> propertyList = this.getSqlMapClientTemplate().queryForList(
				GET_VALUESET_DEF_PROPERTY_LIST_BY_VALUESET_DEFINITION_URI_SQL,
				new PrefixedParameter(prefix, valueSetDefURI));
		
		Properties properties = new Properties();
		
		for (String propId : propertyList) {
			Property prop = null;
			
			try {
				prop = vsPropertyDao.resolveVSPropertyByRevision(
						valueSetDefUId, propId, revisionId);
			} catch (LBRevisionException e) {
				continue;
			}
			properties.addProperty(prop);
		}
		
		valueSetDefinition.setProperties(properties);
		
		return valueSetDefinition;
	}
}
