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
package uk.ac.open.kmi.iserve.sal.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.vocabulary.RDF;

import uk.ac.open.kmi.iserve.commons.io.URIUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;

public class HtmlUtil {


	public static String LIST_HTML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
		"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
		"  <head>\n" +
		"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
		"    <title><<<title>>></title>\n" +
		"    <style>\n" +
		"	  table { border-collapse: collapse ; border: 1px solid black ; }\n" +
		"	  td, th\n" +
		" 	  { border: 1px solid black ;\n" +
		"	    padding-left:0.5em; padding-right: 0.5em;\n" +
		"	    padding-top:0.2ex ; padding-bottom:0.2ex\n" +
		"	  }\n" +
		"	</style>\n" +
		"  </head>\n" +
		"  <body>\n" +
		"    <h1><<<title>>></h1>\n" +
		"    <div>\n";

	public static String LIST_HTML_SUFFIX = "    </div>\n" +
		"  </body>\n" +
		"</html>";

	public static String uriToLink(String uri) {
		return "<a href=\"" + uri + "\" target=\"_blank\">" + URIUtil.getLocalName(uri) + "</a>";
	}

	public static String uriListToTable(List<String> uriList) {
		StringBuffer sb = new StringBuffer();
		sb.append("<table>");
		for ( String uri : uriList ) {
			sb.append("<tr><td>" + uriToLink(uri) + "</td></tr>\n");
		}
		sb.append("</table>");
		return sb.toString();
	}

	public static String modelToTable(Model model, String id) {
		if ( null == model || model.isEmpty() == true ) return null;
		StringBuffer sb = new StringBuffer();
		sb.append("<table>");
		if ( model.isOpen() == false ) {
			model.open();
		}

		List<String> visualizedResources = new ArrayList<String>();
		QueryResultTable qrt = model.sparqlSelect("SELECT DISTINCT ?s WHERE {?s " + RDF.type.toSPARQL() + " " + MSM.Service.toSPARQL() + " \n" +
				"FILTER REGEX(str(?s), \"" + id + "\", \"i\")} ");
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			Resource subject = row.getValue("s").asResource();
			visualizedResources.add(subject.toString());
			sb.append("<tr><td>" + uriToLink(subject.toString()) + "</td><td/><td/></tr>\n");
			propertyValueToTable(model, subject, sb);
		}
		iter.close();

		qrt = model.sparqlSelect("SELECT DISTINCT ?s WHERE {?s " + RDF.type.toSPARQL() + " " + MSM.Operation.toSPARQL() + " \n" +
				"FILTER REGEX(str(?s), \"" + id + "\", \"i\")} ");
		iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			Resource subject = row.getValue("s").asResource();
			visualizedResources.add(subject.toString());
			sb.append("<tr><td>" + uriToLink(subject.toString()) + "</td><td/><td/></tr>\n");
			propertyValueToTable(model, subject, sb);
		}
		iter.close();

		qrt = model.sparqlSelect("SELECT DISTINCT ?s WHERE {?s " + RDF.type.toSPARQL() + " " + MSM.Message.toSPARQL() + " \n" +
				"FILTER REGEX(str(?s), \"" + id + "\", \"i\")} ");
		iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			Resource subject = row.getValue("s").asResource();
			visualizedResources.add(subject.toString());
			sb.append("<tr><td>" + uriToLink(subject.toString()) + "</td><td/><td/></tr>\n");
			propertyValueToTable(model, subject, sb);
		}
		iter.close();

		qrt = model.sparqlSelect("SELECT DISTINCT ?s WHERE {?s " + RDF.type.toSPARQL() + " " + MSM.MessagePart.toSPARQL() + " \n" +
				"FILTER REGEX(str(?s), \"" + id + "\", \"i\")} ");
		iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			Resource subject = row.getValue("s").asResource();
			visualizedResources.add(subject.toString());
			sb.append("<tr><td>" + uriToLink(subject.toString()) + "</td><td/><td/></tr>\n");
			propertyValueToTable(model, subject, sb);
		}
		iter.close();

		qrt = model.sparqlSelect("SELECT DISTINCT ?s WHERE {?s ?p ?o \n" +
				"FILTER REGEX(str(?s), \"" + id + "\", \"i\")} ");
		iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			Resource subject = row.getValue("s").asResource();
			if ( isInList(subject.toString(), visualizedResources) == false &&
					subject.toString().contains("resource/documents") == false ) {
				sb.append("<tr><td>" + uriToLink(subject.toString()) + "</td><td/><td/></tr>\n");
				propertyValueToTable(model, subject, sb);				
			}
		}
		iter.close();

		model.close();
		sb.append("</table>\n");
		return sb.toString();
	}

	private static void propertyValueToTable(Model model, Resource subject, StringBuffer sb) {
		ClosableIterator<Statement> iter = model.findStatements(subject, Variable.ANY, null);
		while ( iter.hasNext() ) {
			Statement stmt = iter.next();
			sb.append("<tr><td/>");
			String uriString = stmt.getPredicate().toString();
			sb.append("<td>" + uriToLink(uriString) + "</td>");
			if ( stmt.getObject() instanceof Resource ||
					stmt.getObject() instanceof URI ) {
				uriString = stmt.getObject().toString();
				sb.append("<td>" + uriToLink(uriString) + "</td>");
			} else {
				uriString = stmt.getObject().asLiteral().getValue();
				uriString = StringEscapeUtils.escapeHtml(uriString);
				sb.append("<td>" + uriString + "</td>");
			}
			sb.append("</tr>\n");
		}
		iter.close();
	}

	private static boolean isInList(String str, List<String> strList) {
		for ( String strInList : strList ) {
			if ( strInList.equals(str) ) {
				return true;
			}
		}
		return false;
	}

}
