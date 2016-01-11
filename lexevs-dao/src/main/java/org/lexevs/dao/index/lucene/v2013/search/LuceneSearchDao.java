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
package org.lexevs.dao.index.lucene.v2013.search;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.lexevs.dao.database.utility.DaoUtility;
import org.lexevs.dao.index.access.search.SearchDao;
import org.lexevs.dao.index.indexregistry.IndexRegistry;
import org.lexevs.dao.index.lucene.AbstractFilteringLuceneIndexTemplateDao;
import org.lexevs.dao.index.lucenesupport.LuceneIndexTemplate;
import org.lexevs.dao.index.lucenesupport.MultiBaseLuceneIndexTemplate;
import org.lexevs.dao.index.version.LexEvsIndexFormatVersion;
import org.lexevs.dao.indexer.utility.ConcurrentMetaData;

/**
 * The Class LuceneEntityDao.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class LuceneSearchDao extends AbstractFilteringLuceneIndexTemplateDao implements SearchDao {
	
	/** The supported index version2013. */
	public static LexEvsIndexFormatVersion supportedIndexVersion2013 = LexEvsIndexFormatVersion.parseStringToVersion("2013");
	
	IndexRegistry registry;
	
//	private static final Comparator<ScoreDoc> SCORE_DOC_COMPARATOR = new Comparator<ScoreDoc>(){
//		@Override
//		public int compare(ScoreDoc o1, ScoreDoc o2) {
//			return FieldComparator.RelevanceComparator.compare(o1., o2);
//		
//		}
//	};

	@Override
	public void addDocuments(String codingSchemeUri, String version,
			List<Document> documents, Analyzer analyzer) {
		getLuceneIndexTemplate(codingSchemeUri, version).addDocuments(documents, analyzer);
	}

	@Override
	public void deleteDocuments(String codingSchemeUri, String version,
			Query query) {
		getLuceneIndexTemplate(codingSchemeUri, version).removeDocuments(query);
	}
	
	@Override
	public String getIndexName(String codingSchemeUri, String version) {
		return getLuceneIndexTemplate(codingSchemeUri, version).getIndexName();
	}

	@Override
	public Filter getCodingSchemeFilter(String uri, String version) {
		return null;
	}

	@Override
	public Document getById(int id) {
		return this.getLuceneIndexTemplate().getDocumentById(id);
	}

	@Override
	public List<ScoreDoc> query(Query query) {
		try {
			LuceneIndexTemplate template = this.getLuceneIndexTemplate();
	
			TopScoreDocCollector collector = TopScoreDocCollector.create(template.getMaxDoc());
			
			template.search(query, null, collector);
			final List<ScoreDoc> docs  = Arrays.asList(collector.topDocs().scoreDocs);
			
			return docs;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<ScoreDoc> query(Query query,
			Set<AbsoluteCodingSchemeVersionReference> codeSystemsToInclude) {
		List<AbsoluteCodingSchemeVersionReference> list = Arrays.asList(codeSystemsToInclude.
				toArray(new AbsoluteCodingSchemeVersionReference[codeSystemsToInclude.size()]));
		return registry.getCommonLuceneIndexTemplate(list).search(query, null);
	}

	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.index.access.AbstractBaseIndexDao#doGetSupportedLexEvsIndexFormatVersions()
	 */
	@Override
	public List<LexEvsIndexFormatVersion> doGetSupportedLexEvsIndexFormatVersions() {
		return DaoUtility.createList(LexEvsIndexFormatVersion.class, supportedIndexVersion2013);
	}
	
	protected LuceneIndexTemplate getLuceneIndexTemplate(
			String codingSchemeUri, String version) {
		return registry.getLuceneIndexTemplate(codingSchemeUri, version);
	}

	public LuceneIndexTemplate getLuceneIndexTemplate() {
		return new MultiBaseLuceneIndexTemplate(MultiBaseLuceneIndexTemplate.getNamedDirectories(ConcurrentMetaData.getInstance()));
	}

	public IndexRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(IndexRegistry registry) {
		this.registry = registry;
	}




}