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


/**
 * iServe user log vocabulary
 * @author Dong LIU (OU)
 */
public class LOG {

	// Name spaces
	public static final String NS = "http://www.soa4all.eu/log#";
	public static final String NS_PREFIX = "log";

	// Classes
	public static final String LOG_ENTRY = NS + "LogEntry";
	public static final String SERVICE_REPOSITOY_LOG_ENTRY = NS + "ServiceRepositoyLogEntry";
	public static final String ACTION = NS + "Action";
	public static final String ITEM_CREATION = NS + "ItemCreation";
	public static final String ITEM_RETRIEVAL = NS + "ItemRetrieval";
	public static final String ITEM_UPDATING = NS + "ItemUpdating";
	public static final String ITEM_DELETING = NS + "ItemDeleting";


	// properties
	public static final String HAS_AGENT = NS + "hasAgent";
	public static final String HAS_ACTION = NS + "hasAction";
	public static final String HAS_DATE_TIME = NS + "hasDateTime";
	public static final String CREATED_ITEM = NS + "createdItem";
	public static final String RETRIEVED_ITEM = NS + "retrievedItem";
	public static final String UPDATED_ITEM = NS + "updatedItem";
	public static final String DELETED_ITEM = NS + "deletedItem";
	public static final String HAS_METHOD = NS + "hasMethod";
}
