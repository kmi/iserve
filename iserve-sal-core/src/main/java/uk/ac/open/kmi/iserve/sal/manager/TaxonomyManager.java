/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.ac.open.kmi.iserve.sal.manager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.sal.config.SalConfig;
import uk.ac.open.kmi.iserve.sal.exception.TaxonomyException;
import uk.ac.open.kmi.iserve.sal.model.impl.CategoryImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve.sal.model.taxonomy.Category;

public class TaxonomyManager extends BaseSemanticManager {

	public TaxonomyManager(SalConfig config) throws RepositoryException {
		super(config.getImporterConfig().getRepoServerUrl(), config.getImporterConfig().getRepoName());
	}

	public List<String> listTaxonomy() {
		String queryString = "SELECT DISTINCT ?c WHERE {"
			+ "GRAPH ?c { ?t " + RDF.type.toSPARQL() + " <http://www.wsmo.org/ns/wsmo-lite#FunctionalClassificationRoot> . }}";
		RepositoryModel model = repoConnector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		List<String> result = new ArrayList<String>();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String contextUriString = row.getValue("c").toString();
			result.add(contextUriString);
		}
		repoConnector.closeRepositoryModel(model);
		model = null;
		return result;
	}

	public String addTaxonomy(String taxonomyUri) throws TaxonomyException {
		if ( (null == taxonomyUri) || ("".equalsIgnoreCase(taxonomyUri)) ) {
			throw new TaxonomyException("Taxonomy URI is null");
		}
		try {
			URL url = new URL(taxonomyUri);
			InputStream is = url.openStream();
			RepositoryModel model = repoConnector.openRepositoryModel();
			model = repoConnector.openRepositoryModel(taxonomyUri);
			model.readFrom(is);
			repoConnector.closeRepositoryModel(model);
			return taxonomyUri;
		} catch (MalformedURLException e) {
			throw new TaxonomyException(e);
		} catch (IOException e) {
			throw new TaxonomyException(e);
		}
	}

	public List<Category> loadTaxonomy(String taxonomyUri) throws TaxonomyException {
		if ( (null == taxonomyUri) || ("".equalsIgnoreCase(taxonomyUri)) ) {
			throw new TaxonomyException("Taxonomy URI is null");
		}
		List<Category> result = new ArrayList<Category>();
		List<Category> roots = getRootCategories(taxonomyUri);
		if ( roots != null ) {
			result.addAll(roots);
		}
		RepositoryModel model = repoConnector.openRepositoryModel(taxonomyUri);
		result.addAll(getAllSubCategory(model, taxonomyUri));
		repoConnector.closeRepositoryModel(model);
		return result;
	}

	private List<Category> getRootCategories(String taxonomyUri) throws TaxonomyException {
		if ( (null == taxonomyUri) || ("".equalsIgnoreCase(taxonomyUri)) ) {
			throw new TaxonomyException("Taxonomy URI is null");
		}
		String queryString = "SELECT DISTINCT ?r WHERE { GRAPH <" + taxonomyUri +
			"> { ?r " + RDF.type.toSPARQL() + " " + MSM.FunctionalClassificationRoot.toSPARQL() + " . }" +
			"} ORDER BY ?r";
		List<Category> result = new ArrayList<Category>();
		RepositoryModel model = repoConnector.openRepositoryModel(taxonomyUri);
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String fcrUriString = row.getValue("r").toString();
			Category fcr = new CategoryImpl(new URIImpl(fcrUriString), null);
			result.add(fcr);
		}
		iter.close();
		repoConnector.closeRepositoryModel(model);
		return result;
	}

	private List<Category> getAllSubCategory(RepositoryModel model, String taxonomyUri) throws TaxonomyException {
		// we assume that the rule set of the repository is at least rdfs
		String queryString = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
			"SELECT ?c ?cl WHERE { GRAPH <" + taxonomyUri+ "> {" +
			"?c rdfs:subClassOf ?cl .  \n" +
			"OPTIONAL { ?sc rdfs:subClassOf ?cl. ?c rdfs:subClassOf ?sc .  \n" +
			"filter(?cl != ?sc && ?sc != ?c) }  \n" +
			"filter (!bound(?sc) && isURI(?cl) && ?cl != ?c) \n" +
			"}}" +
			" ORDER BY ?c";
		QueryResultTable qrt = model.sparqlSelect(queryString);
		if ( qrt != null ) {
			ClosableIterator<org.ontoware.rdf2go.model.QueryRow> iter = qrt.iterator();
			if ( iter == null )
				return null;
			List<Category> result = new ArrayList<Category>();
			while ( iter.hasNext() ) {
				org.ontoware.rdf2go.model.QueryRow qr = iter.next();
				String categoryUriString = qr.getValue("c").toString();
				String superCategoryUriString = qr.getValue("cl").toString();
//				System.out.println(categoryUriString + " : " + superCategoryUriString);
				Category newCategory = new CategoryImpl(new URIImpl(categoryUriString), new URIImpl(superCategoryUriString));
				result.add(newCategory);
			}
			iter.close();
			iter = null;
			qrt = null;
			return result;
		}
		return null;
	}

}
