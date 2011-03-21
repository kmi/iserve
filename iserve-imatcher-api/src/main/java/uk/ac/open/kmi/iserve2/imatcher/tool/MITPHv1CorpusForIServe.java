/*
 * $Id: MITPHv1Corpus.java 448 2007-12-10 17:01:19Z kiefer $
 *
 * Created by Christoph Kiefer, kiefer@ifi.uzh.ch
 *
 * See LICENSE for more information about licensing and warranties.
 */
package uk.ac.open.kmi.iserve.imatcher.tool;

import java.util.ArrayList;

import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;

import simpack.util.corpus.StringUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.wcohen.ss.BasicStringWrapper;
import com.wcohen.ss.api.StringWrapper;

public class MITPHv1CorpusForIServe {

	private static String qstr = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			"SELECT DISTINCT ?description WHERE {\n" +
//			"SELECT DISTINCT ?s ?description WHERE {\n" +
//			"?s rdf:type <http://cms-wg.sti2.org/ns/minimal-service-model#Service> . \n" +
			"?s rdfs:comment ?description . \n" +
			"}";

	public static ArrayList<StringWrapper> run(Model model) {
		ArrayList<StringWrapper> corpus = new ArrayList<StringWrapper>();
		Query query = QueryFactory.create(qstr);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		for (; results.hasNext();) {
			QuerySolution qs = results.nextSolution();
			RDFNode queryNode = qs.get("description");
			if (queryNode.isLiteral()) {
				Literal l = (Literal) queryNode;
				String textDescription = l.getLexicalForm();
				// a lot of pre-processing here
				textDescription = MITPHv1CorpusForIServe.clean(textDescription);
				// catch empty descriptions
				if ( !textDescription.equals("") ) {
//					System.out.println("textDescription: " + textDescription);
					corpus.add(new BasicStringWrapper(textDescription));
				}
			}
		}

		qe.close();
		return corpus;
	}

	public static String clean(String textDescription) {
		// System.out.println("Original " + textDescription);
		if (textDescription.equals("")) {
			return textDescription;
		}
		// to lower case
		textDescription = textDescription.toLowerCase();
		// remove key=value pairs
		textDescription = textDescription.replaceAll("\\w+=\\w+(\\.\\w+)?", "");
		// remove punctuation
		textDescription = textDescription.replaceAll("\\p{Punct}+", " ");
		// remove digits
		String REAL_NUMBER = "\\d+";
		textDescription = textDescription.replaceAll(REAL_NUMBER, "");
		// remove special
		textDescription = textDescription.replaceAll("top", "");
		textDescription = textDescription.replaceAll("arial", "");
		textDescription = textDescription.replaceAll("htm", "");
		textDescription = textDescription.replaceAll("nbsp", "");

		Tag[] tags = HTML.getAllTags();
		CSS.Attribute[] cssAttributes = CSS.getAllAttributeKeys();
		HTML.Attribute[] htmlAttributes = HTML.getAllAttributeKeys();

		String[] items = textDescription.split("\\s+");

		for (int i = 0; i < items.length; i++) {
			for (Tag t : tags) {
				if (items[i].equals(t.toString())) {
					items[i] = "";
					// System.out.println("Removing " + t.toString());
					// changed = true;
				}
			}
		}

		// remove html tag attributes
		for (int i = 0; i < items.length; i++) {
			for (HTML.Attribute a : htmlAttributes) {
				if (items[i].equals(a.toString())) {
					items[i] = "";
					// System.out.println("Removing " + a.toString());
				}
			}
		}
		// remove css attributes
		for (int i = 0; i < items.length; i++) {
			for (CSS.Attribute a : cssAttributes) {
				if (items[i].equals(a.toString())) {
					items[i] = "";
					// System.out.println("Removing " + a.toString());
				}
			}
		}

		// construct whole string
		textDescription = "";
		for (String str : items) {
			textDescription += str.trim() + " ";
		}
		textDescription.trim();
		// finally clean the string (stemming)
		textDescription = StringUtils.clean(textDescription);

		// remove single characters
		items = textDescription.split("\\s+");
		for (int i = 0; i < items.length; i++) {
			if (items[i].length() == 1) {
				items[i] = "";
			}
		}
		// trim final strings
		textDescription = "";
		for (String str : items) {
			textDescription += str.trim() + " ";
		}
		textDescription.trim();

		if (textDescription.equals("") || textDescription.equals(" ")
				|| textDescription.equals("  ")) {
			return "";
		} else {
			return textDescription;
		}
	}

}
