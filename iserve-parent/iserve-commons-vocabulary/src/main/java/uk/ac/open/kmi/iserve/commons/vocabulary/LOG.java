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
package uk.ac.open.kmi.iserve.commons.vocabulary;

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

/**
 * iServe user log vocabulary
 * @author Dong LIU (OU)
 */
public class LOG {

	// Name spaces
	public static final String NS_URI = "http://www.soa4all.eu/log#";
	public static final String NS_PREFIX = "log";

	public static final String TIME_NS_URI = "http://www.w3.org/time#";
	public static final String TIME_NS_PREFIX = "time";

	// Classes
	public static final String LOG_ENTRY = NS_URI + "LogEntry";
	public static final URI LogEntry = new URIImpl(LOG_ENTRY);
	public static final String SERVICE_REPOSITOY_LOG_ENTRY = NS_URI + "ServiceRepositoyLogEntry";
	public static final URI ServiceRepositoyLogEntry = new URIImpl(SERVICE_REPOSITOY_LOG_ENTRY);
	public static final String ACTION = NS_URI + "Action";
	public static final URI Action = new URIImpl(ACTION);
	public static final String ITEM_CREATION = NS_URI + "ItemCreation";
	public static final URI ItemCreation = new URIImpl(ITEM_CREATION);
	public static final String ITEM_RETRIEVAL = NS_URI + "ItemRetrieval";
	public static final URI ItemRetrieval = new URIImpl(ITEM_RETRIEVAL);
	public static final String ITEM_UPDATING = NS_URI + "ItemUpdating";
	public static final URI ItemUpdating = new URIImpl(ITEM_UPDATING);
	public static final String ITEM_DELETING = NS_URI + "ItemDeleting";
	public static final URI ItemDeleting = new URIImpl(ITEM_DELETING);
	public static final String TIME_INSTANT = TIME_NS_URI + "Instant";
	public static final URI TimeInstant = new URIImpl(TIME_INSTANT);

	// properties
	public static final String HAS_AGENT = NS_URI + "hasAgent";
	public static final URI hasAgent = new URIImpl(HAS_AGENT);
	public static final String HAS_ACTION = NS_URI + "hasAction";
	public static final URI hasAction = new URIImpl(HAS_ACTION);
	public static final String HAS_DATE_TIME = NS_URI + "hasDateTime";
	public static final URI hasDateTime = new URIImpl(HAS_DATE_TIME);
	public static final String CREATED_ITEM = NS_URI + "createdItem";
	public static final URI createdItem = new URIImpl(CREATED_ITEM);
	public static final String RETRIEVED_ITEM = NS_URI + "retrievedItem";
	public static final URI retrievedItem = new URIImpl(RETRIEVED_ITEM);
	public static final String UPDATED_ITEM = NS_URI + "updatedItem";
	public static final URI updatedItem = new URIImpl(UPDATED_ITEM);
	public static final String DELETED_ITEM = NS_URI + "deletedItem";
	public static final URI deletedItem = new URIImpl(DELETED_ITEM);
	public static final String IN_XSD_DATE_TIME = TIME_NS_URI + "inXSDDateTime";
	public static final URI inXSDDateTime = new URIImpl(IN_XSD_DATE_TIME);
	public static final String HAS_METHOD = NS_URI + "hasMethod";
	public static final URI hasMethod = new URIImpl(HAS_METHOD);

}
