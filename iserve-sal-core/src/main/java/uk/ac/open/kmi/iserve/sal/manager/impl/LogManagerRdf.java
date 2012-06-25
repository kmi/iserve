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
package uk.ac.open.kmi.iserve.sal.manager.impl;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.ontoware.rdf2go.vocabulary.XSD;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.DC;
import uk.ac.open.kmi.iserve.commons.vocabulary.LOG;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.manager.LogManager;
import uk.ac.open.kmi.iserve.sal.model.impl.LogItemImpl;
import uk.ac.open.kmi.iserve.sal.model.log.LogItem;

public class LogManagerRdf implements LogManager {

	private static final String LOG_ENTRY_PREFIX = LOG.NS_URI + "logEntry";

	private static final String ACTION_PREFIX = LOG.NS_URI + "action";

	private static final String TIME_INSTANT_PREFIX = LOG.TIME_NS_URI + "instant";

	private RDFRepositoryConnector repoConnector;

	/**
	 * Constructor for the Log Manager. Protected to avoid external access.
	 * Any access to this should take place through the iServeManager
	 * 
	 * @param configuration
	 * @throws RepositoryException
	 */
	protected LogManagerRdf(SystemConfiguration configuration) throws RepositoryException {
		repoConnector = new RDFRepositoryConnector(configuration.getLogServerUrl().toString(),
				configuration.getLogsRepositoryName());
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.LogManager#log(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String)
	 */
	@Override
	public void log(String agentUri, String actionUri, String object, Date time, String method) throws LogException {
		URI action = null;
		if ( actionUri.toString().endsWith(LOG.ITEM_CREATION) ) {
			action = LOG.createdItem;
		} else if ( actionUri.toString().endsWith(LOG.ITEM_RETRIEVAL) ) {
			action = LOG.retrievedItem;
		} else if ( actionUri.toString().endsWith(LOG.ITEM_UPDATING) ) {
			action = LOG.updatedItem;
		} else if ( actionUri.toString().endsWith(LOG.ITEM_DELETING) ) {
			action = LOG.deletedItem;
		}

		if ( null == action ) {
			throw new LogException("Unknown Action: " + actionUri.toString());
		}

		RepositoryModel repoModel = repoConnector.openRepositoryModel();
		// FIXME: only logging ONCE within ONE millisecond.
		long currentTime = System.currentTimeMillis();
		URI logEntryInst = repoModel.createURI(LOG_ENTRY_PREFIX + currentTime);
		URI actionInst = repoModel.createURI(ACTION_PREFIX + currentTime);
		URI timeInstantInst = repoModel.createURI(TIME_INSTANT_PREFIX + currentTime);
		URI actionType = repoModel.createURI(actionUri);
		URI agnetInst = repoModel.createURI(agentUri);
		URI objectInst = repoModel.createURI(object);

		DatatypeFactory datatypeFactory;
		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new LogException(e);
		}
		GregorianCalendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		utcCalendar.setTime(time);
		XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(utcCalendar);
		xmlCalendar = xmlCalendar.normalize();

		repoModel.addStatement(actionInst, RDF.type, actionType);
		repoModel.addStatement(actionInst, action, objectInst);
		repoModel.addStatement(timeInstantInst, RDF.type, LOG.TimeInstant);
		repoModel.addStatement(timeInstantInst, LOG.inXSDDateTime, repoModel.createDatatypeLiteral(xmlCalendar.toXMLFormat(), XSD._dateTime));
		repoModel.addStatement(logEntryInst, RDF.type, LOG.ServiceRepositoyLogEntry);
		repoModel.addStatement(logEntryInst, LOG.hasAction, actionInst);
		repoModel.addStatement(logEntryInst, LOG.hasAgent, agnetInst);
		repoModel.addStatement(logEntryInst, LOG.hasDateTime, timeInstantInst);
		repoModel.addStatement(logEntryInst, LOG.hasMethod, method);

		if ( actionUri.endsWith(LOG.CREATED_ITEM) ) {
			repoModel.addStatement(objectInst, DC.creator, agnetInst);
		}

		repoConnector.closeRepositoryModel(repoModel);
		repoModel = null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.LogManager#getAllLogItems()
	 */
	@Override
	public Map<String, LogItem> getAllLogItems() throws DatatypeConfigurationException {
		Map<String, LogItem> result = new HashMap<String, LogItem>();
		String logQueryString = "SELECT DISTINCT ?uri ?agent ?t WHERE { " +
			"?log " + RDF.type.toSPARQL() + " " + LOG.ServiceRepositoyLogEntry.toSPARQL() + " . \n" +
			"?log " + LOG.hasAction.toSPARQL() + " ?a . \n" +
			"?log " + LOG.hasDateTime.toSPARQL() + " ?inst . \n" +
			"?inst " + LOG.inXSDDateTime.toSPARQL() + " ?t . \n" +
			"?log " + LOG.hasAgent.toSPARQL() + " ?agent . \n" +
			"?a " + LOG.createdItem.toSPARQL() + " ?uri }";
		RepositoryModel model = repoConnector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(logQueryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String uri = row.getValue("uri").toString();
			String author = row.getValue("agent").toString();
			String timeString = row.getValue("t").toString();
			timeString = timeString.substring(0, timeString.indexOf('^'));
			DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(timeString);
			GregorianCalendar cal = xmlCalendar.toGregorianCalendar();
			Date time = cal.getTime();
			LogItem log = new  LogItemImpl(author, time);
			result.put(uri, log);
		}
		iter.close();
		repoConnector.closeRepositoryModel(model);
		return result;
	}

}
