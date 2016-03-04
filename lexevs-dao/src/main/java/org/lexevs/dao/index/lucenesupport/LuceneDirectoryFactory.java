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
package org.lexevs.dao.index.lucenesupport;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.join.ToParentBlockJoinIndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.MMapDirectory;
import org.lexevs.dao.index.indexer.LuceneLoaderCode;
import org.lexevs.logging.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

public class LuceneDirectoryFactory implements FactoryBean {

	private String indexName;
	
	private Resource indexDirectory;
	
	private LuceneDirectoryCreator luceneDirectoryCreator;
	
	

	@Override
	public Object getObject() throws Exception {
		
		return luceneDirectoryCreator.getDirectory(indexName, indexDirectory.getFile());
	}
	
	@Override
	public Class<?> getObjectType() {
		return NamedDirectory.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexDirectory(Resource indexDirectory) {
		this.indexDirectory = indexDirectory;
	}

	public Resource getIndexDirectory() {
		return indexDirectory;
	}
	
	public void setLuceneDirectoryCreator(LuceneDirectoryCreator luceneDirectoryCreator) {
		this.luceneDirectoryCreator = luceneDirectoryCreator;
	}

	public LuceneDirectoryCreator getLuceneDirectoryCreator() {
		return luceneDirectoryCreator;
	}

	public static class NamedDirectory {
		private Directory directory;
		private String indexName;
		private SearcherManager searchManager;
//		private IndexSearcher indexSearcher;
		private ToParentBlockJoinIndexSearcher blockJoinSearcher;
		private IndexReader indexReader;

		public NamedDirectory(Directory directory, String indexName) {
			super();
			this.directory = directory;
			this.indexName = indexName;
			try {
				this.initIndexDirectory(directory);
				this.indexReader = this.createIndexReader(directory);
				this.searchManager = this.createIndexSearcherManager(this.indexReader);
				this.blockJoinSearcher = this.createBlockJoinIndexSearcher(this.indexReader);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private SearcherManager createIndexSearcherManager(
				IndexReader indexReader) {
			SearcherManager manager = null;
			try {
				manager =  new SearcherManager((DirectoryReader)indexReader, new SearcherFactory());
				warmSearcher(manager);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return manager;
		}

		private void warmSearcher(SearcherManager manager) {
			try {
				IndexSearcher search = manager.acquire();
				search.search(new TermQuery(new Term("propetyValue", "blood")), TopScoreDocCollector.create(10));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		public Directory getDirectory() {
			return directory;
		}
		public void setDirectory(Directory directory) {
			this.directory = directory;
		}
		public String getIndexName() {
			return indexName;
		}
		public void setIndexName(String indexName) {
			this.indexName = indexName;
		}
		
		public IndexSearcher getIndexSearcher() {
			try {
				return searchManager.acquire();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		public void releaseIndexSearcher(IndexSearcher indexSearcher) {
			try {
				searchManager.release(indexSearcher);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		
		/**
		 * @return the searchManager
		 */
		public SearcherManager getSearchManager() {
			return searchManager;
		}

		/**
		 * @param searchManager the searchManager to set
		 */
		public void setSearchManager(SearcherManager searchManager) {
			this.searchManager = searchManager;
		}

		public ToParentBlockJoinIndexSearcher getBlockJoinSearcher() {
			return blockJoinSearcher;
		}

		public void setBlockJoinSearcher(
				ToParentBlockJoinIndexSearcher blockJoinSearcher) {
			this.blockJoinSearcher = blockJoinSearcher;
		}

		public IndexReader getIndexReader() {
			return indexReader;
		}
		public void setIndexReader(IndexReader indexReader) {
			this.indexReader = indexReader;
		}
		
		protected IndexSearcher createIndexSearcher(IndexReader indexReader) throws Exception {
			return new IndexSearcher(indexReader);
		}
		private ToParentBlockJoinIndexSearcher createBlockJoinIndexSearcher(
				IndexReader indexReader) {
			return new ToParentBlockJoinIndexSearcher(indexReader);
		}
		protected IndexReader createIndexReader(Directory directory) throws Exception {
			IndexReader reader = DirectoryReader.open(directory);
			return reader;
		}

		private void initIndexDirectory(Directory directory) throws IOException,
			CorruptIndexException, LockObtainFailedException {


				IndexWriterConfig config = new IndexWriterConfig(LuceneLoaderCode.getAnaylzer());
				IndexWriter writer = new IndexWriter(directory, config);

				writer.close();
		}
		
		public NamedDirectory refresh() {
			try {
				IndexReader reopenedReader = DirectoryReader.open(this.directory);
				
				if(reopenedReader != this.indexReader){
					this.indexReader.close();
					this.indexReader = this.createIndexReader(this.directory);
				}
				this.searchManager.close();
				this.searchManager = this.createIndexSearcherManager(this.indexReader);
				LoggerFactory.getLogger().warn("Refreshing index: " + this.indexName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			return this;
		}

		public void remove() {
			try {
				this.indexReader.close();

				if(this.directory instanceof FSDirectory) {
					FSDirectory dir = (FSDirectory)this.directory;
					FileUtils.deleteDirectory(new File(dir.getDirectory().toUri()));
				}
				else if (this.directory instanceof MMapDirectory){
					MMapDirectory dir = (MMapDirectory)this.directory;
					FileUtils.deleteDirectory(new File(dir.getDirectory().toUri()));
				}
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}