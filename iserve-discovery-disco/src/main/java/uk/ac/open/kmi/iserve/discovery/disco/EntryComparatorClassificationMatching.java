/*
   Copyright 2011  Knowledge Media Institute - The Open University

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
package uk.ac.open.kmi.iserve.discovery.disco;

import java.util.Comparator;
import java.util.HashMap;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;

import uk.ac.open.kmi.iserve.discovery.disco.util.DiscoveryUtil;

/**
 * Comparator for sorting Entries obtained when matching Entries
 * 
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 * @version $Rev$
 * $LastChangedDate$ 
 * $LastChangedBy$
 */
public class EntryComparatorClassificationMatching implements Comparator<Entry> {
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Entry arg0, Entry arg1) {
		
		String  link0 = arg0.getSelfLink().toString();
		String  link1 = arg1.getSelfLink().toString();
		
		if (arg0.equals(arg1) || link0.equals(link1)) {
			return 0;
		}
		
		// get the value for the match degree
		// get the actual matching degree and return the ordering
		
//		ExtensibleElement e = result.addExtension(DiscoveryUtil.MATCH_DEGREE);
//		e.setAttributeValue("num", Integer.toString(degreeNum));
//		e.setText(degree);
		
		return 0;
	}

}
