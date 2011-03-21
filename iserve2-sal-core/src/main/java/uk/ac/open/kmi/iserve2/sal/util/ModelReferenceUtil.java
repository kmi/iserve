package uk.ac.open.kmi.iserve2.sal.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.tika.Tika;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.ontoware.rdf2go.vocabulary.RDFS;
import org.ontoware.rdf2go.vocabulary.OWL;
import org.ontoware.rdf2go.vocabulary.XSD;
import org.openrdf.rdf2go.RepositoryModel;

import uk.ac.open.kmi.iserve2.commons.io.IOUtil;
import uk.ac.open.kmi.iserve2.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve2.commons.io.URIUtil;

public class ModelReferenceUtil {

	private static ModelReferenceUtil instance = new ModelReferenceUtil();

	private RDFRepositoryConnector connector;

	private Tika tika;

	private ModelReferenceUtil() {
		tika = new Tika();
	}

	public static ModelReferenceUtil getInstance() {
		return instance;
	}

	public void setRDFRepositoryConnector(RDFRepositoryConnector connector) {
		this.connector = connector;
	}

	public void setProxy(String proxyHost, String proxyPort) {
		if ( proxyHost != null && proxyPort != null ) {
			Properties prop = System.getProperties();
			prop.put("http.proxyHost", proxyHost);
			prop.put("http.proxyPort", proxyPort);
		}
	}

	public void inputModelReference(String modelrefUri) throws MalformedURLException, IOException {
		String contextUri = URIUtil.getNameSpace(modelrefUri);
		RepositoryModel model = connector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect("SELECT DISTINCT ?g where {" +
				" graph ?g { ?s ?p ?o . }" +
				"}");
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while( iter.hasNext() ) {
			QueryRow row = iter.next();
			if ( row.getValue("g").toString().equals(contextUri) ) {
				iter.close();
				connector.closeRepositoryModel(model);
				return;
			}
		}
		iter.close();
		connector.closeRepositoryModel(model);

		System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
		System.setProperty("sun.net.client.defaultReadTimeout", "3000");
		String mimeType = tika.detect(new URL(modelrefUri));
			
		if ( mimeType.contains("html") == true ) {
			throw new IOException("It does not seem to be an ontology");
		}

		String ontologyString = IOUtil.readString(new URL(modelrefUri));
		model = connector.openRepositoryModel(URIUtil.getNameSpace(modelrefUri));
		model.readFrom(new ByteArrayInputStream(ontologyString.getBytes()));
		connector.closeRepositoryModel(model);
	}

	public String getDatatype(String modelrefUri) {
		if ( isProperty(modelrefUri) ) {
			return getRange(modelrefUri);
		}
		if ( isClass(modelrefUri) ) {
			return getPropertyRange(modelrefUri);
		}
		return null;
	}

	private boolean isProperty(String modelrefUri) {
		String queryString = "SELECT ?t WHERE {\n" +
			"<" + modelrefUri + "> " + RDF.type.toSPARQL() + " ?t . " +
			"}";
		RepositoryModel model = connector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String typeUri = row.getValue("t").asURI().toString();
			if ( typeUri.endsWith(RDF.Property.toString()) ||
					typeUri.endsWith(OWL.ObjectProperty.toString()) ||
					typeUri.endsWith(OWL.DatatypeProperty.toString()) ||
					typeUri.endsWith(OWL.TransitiveProperty.toString()) ||
					typeUri.endsWith(OWL.SymmetricProperty.toString()) ||
					typeUri.endsWith(OWL.FunctionalProperty.toString()) ||
					typeUri.endsWith(OWL.InverseFunctionalProperty.toString()) ) {
				iter.close();
				connector.closeRepositoryModel(model);
				return true;
			}
		}
		iter.close();
		connector.closeRepositoryModel(model);
		return false;
	}

	private boolean isClass(String modelrefUri) {
		String queryString = "SELECT ?t WHERE {\n" +
			"<" + modelrefUri + "> " + RDF.type.toSPARQL() + " ?t . " +
			"}";
		RepositoryModel model = connector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String typeUri = row.getValue("t").asURI().toString();
			if ( typeUri.endsWith(RDF.RDF_NS + "Class") ||
					typeUri.endsWith(RDFS.Class.toString()) ||
					typeUri.endsWith(OWL.Class.toString()) ) {
				iter.close();
				connector.closeRepositoryModel(model);
				return true;
			}
		}
		iter.close();
		connector.closeRepositoryModel(model);
		return false;
	}

	public String getLabel(String modelrefUri) {
		String label = null;
		String queryString = "SELECT ?l WHERE {\n" +
			"<" + modelrefUri + "> " + RDFS.label.toSPARQL() + " ?l . " +
			"}";
		RepositoryModel model = connector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			label = row.getValue("l").asLiteral().getValue();
		}
		iter.close();
		connector.closeRepositoryModel(model);
		return label;
	}

	public String getComment(String modelrefUri) {
		String comment = null;
		String queryString = "SELECT ?c WHERE {\n" +
			"<" + modelrefUri + "> " + RDFS.comment.toSPARQL() + " ?c . " +
			"}";
		RepositoryModel model = connector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			comment = row.getValue("c").asLiteral().getValue();
		}
		iter.close();
		connector.closeRepositoryModel(model);
		return comment;
	}

	private String getRange(String modelrefUri) {
		String queryString = "SELECT ?r WHERE {\n" +
			"<" + modelrefUri + "> " + RDFS.range.toSPARQL() + " ?r . " +
			"}";
		RepositoryModel model = connector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		String result = null;
		String result2 = null;
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			if ( row.getValue("r") != null ) {
				String rangeType = row.getValue("r").toString();
				if ( rangeType.startsWith(XSD.XSD_NS) ) {
					result = rangeType;
				}
				if ( rangeType.endsWith(RDFS.Literal.toString()) ) {
					result2 = XSD._string.toString();
				}
				if ( isClass(rangeType) ) {
					result2 = XSD._anyURI.toString();
				}
			}
		}
		iter.close();
		connector.closeRepositoryModel(model);
		if ( result != null )
			return result;
		return result2;
	}

	private String getPropertyRange(String modelrefUri) {
		String queryString = "SELECT DISTINCT ?r ?r2 WHERE {\n" +
			" <" + modelrefUri + "> " + RDFS.subClassOf.toSPARQL() + " ?s . \n" +
			" OPTIONAL { \n" +
			"  ?p " + RDF.type.toSPARQL() + " " + RDF.Property.toSPARQL() + ". \n" +
			"  ?s ?p ?r . \n" +
			"  OPTIONAL { ?r " + RDF.type.toSPARQL() + " " + RDFS.Datatype.toSPARQL() + " . }\n" +
			" } \n" +
			" OPTIONAL { ?p2 " + RDFS.domain.toSPARQL() + " ?s . \n" +
			"  ?p2 " + RDFS.range.toSPARQL() + " ?r2 . \n" +
			" }\n" +
			"}";
		RepositoryModel model = connector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		String result = null;
		String result2 = null;
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			if ( row.getValue("r") != null ) {
				String rangeType = row.getValue("r").toString();
				if ( rangeType.startsWith(XSD.XSD_NS) ) {
					result = rangeType;
				}
				if ( rangeType.endsWith(RDFS.Literal.toString()) ) {
					result2 = XSD._string.toString();
				}
				if ( isClass(rangeType) ) {
					result2 = XSD._anyURI.toString();
				}
			}
			if ( row.getValue("r2") != null ) {
				String rangeType = row.getValue("r2").toString();
				if ( rangeType.startsWith(XSD.XSD_NS) ) {
					return rangeType;
				}
				if ( rangeType.endsWith(RDFS.Literal.toString()) ) {
					result2 = XSD._string.toString();
				}
			}
		}
		iter.close();
		connector.closeRepositoryModel(model);
		if ( result != null )
			return result;
		return result2;
	}

//	public static void main(String[] args) {
//		String modelRefUri = "http://purl.org/iserve/ontology/owlstc/concept.owl#Price";
////		String modelRefUri = "http://localhost:8080/PoliceAPI.rdfs#hasID";
////		String modelRefUri = "http://localhost:8080/PoliceAPI.rdfs#PoliceFunctionalUnit";
////		String modelRefUri = "http://purl.org/iserve/ontology/owlstc/books.owl#Book";
//		try {
//			ModelReferenceUtil.getInstance().inputModelReference(modelRefUri);
////			ModelReferenceUtil.getInstance().getNameDescription(modelRefUri);
//			ModelReferenceUtil.getInstance().getDatatype(modelRefUri);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

}
