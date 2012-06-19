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

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.open.kmi.iserve.sal.model.impl.QueryResultImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.QueryRowImpl;
import uk.ac.open.kmi.iserve.sal.model.query.QueryResult;
import uk.ac.open.kmi.iserve.sal.model.query.QueryRow;

public class SPARQLQueryResultParser {

	private static List<String> parseVariables(Node headNode) {
		List<String> varList = new ArrayList<String>();
		NodeList nodeList= headNode.getChildNodes();
		for ( int i = 0; i < nodeList.getLength(); i++ ) {
			Node node = nodeList.item(i);
			if ( node.getNodeName().equalsIgnoreCase("variable") ) {
				String variable = node.getAttributes().getNamedItem("name").getTextContent();
				varList.add(variable);
			}
		}
		return varList;
	}

	private static QueryRow parseResult(Node resultNode) {
		QueryRow result = new QueryRowImpl();
		Map<String, String> rowData = new HashMap<String, String>();
		Map<String, String> rowDatatype = new HashMap<String, String>();
		NodeList nodeList= resultNode.getChildNodes();
		for ( int i = 0; i < nodeList.getLength(); i++ ) {
			Node node = nodeList.item(i);
			if ( node.getNodeName().equalsIgnoreCase("binding") ) {
				String variable = node.getAttributes().getNamedItem("name").getTextContent();
				NodeList vNodeList = node.getChildNodes();
				for ( int j = 0; j < vNodeList.getLength(); j++ ) {
					Node vNode = vNodeList.item(j);
					if ( !vNode.getNodeName().equalsIgnoreCase("#text") ) {
						rowDatatype.put(variable, vNode.getNodeName());
						rowData.put(variable, vNode.getTextContent());
//						System.out.println(variable + ": " + vNode.getTextContent() + ", type: " + vNode.getNodeName());
					}
				}
			}
		}

		result.setQueryRow(rowDatatype, rowData);
		return result;
	}

	private static List<QueryRow> parseResults(Node resultsNode) {
		List<QueryRow> rowList = new ArrayList<QueryRow>();
		NodeList nodeList= resultsNode.getChildNodes();
		for ( int i = 0; i < nodeList.getLength(); i++ ) {
			Node node = nodeList.item(i);
			if ( node.getNodeName().equalsIgnoreCase("result") ) {
//				System.out.println(node.getNodeName());
				rowList.add(parseResult(node));
//				QueryRow result = new QueryRowImpl();
//				Map<String, String> rowData = new HashMap<String, String>();
//				Map<String, String> rowDatatype = new HashMap<String, String>();
			}
		}
		return rowList;
	}

	public static QueryResult parse(String queryResultString) {
		QueryResult result = new QueryResultImpl();
		try {
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(queryResultString));
			Document dom = builder.parse(is);
			NodeList nodeList= dom.getDocumentElement().getChildNodes();
			for ( int i = 0; i < nodeList.getLength(); i++ ) {
				Node node = nodeList.item(i);
				if ( node.getNodeName().equalsIgnoreCase("head") ) {
					// variables
					result.setVariables(parseVariables(node));
				} else if ( node.getNodeName().equalsIgnoreCase("results") ) {
					// results
					result.setQueryRows(parseResults(node));
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return result;
	}

}
