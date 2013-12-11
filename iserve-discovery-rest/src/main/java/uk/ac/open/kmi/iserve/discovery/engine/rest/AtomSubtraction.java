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
package uk.ac.open.kmi.iserve.discovery.engine.rest;

import org.apache.abdera.model.Entry;

import javax.ws.rs.Path;
import java.util.List;
import java.util.Set;

/**
 * provides functionality to compute an intersection of two or more atom feeds
 *
 * @author Jacek Kopecky
 */
@Path("/{operator: andnot|minus|subtraction}")
public class AtomSubtraction extends AtomBase {

    @Override
    String combinatorFeedTitle(String[] feedTitles) {
        String title = "Subtraction in feed \"" + (feedTitles[0] == null ? "" : feedTitles[0]) + "\" of entries from " + (feedTitles.length - 1) + " feed(s): ";
        for (int i = 1; i < feedTitles.length; i++) {
            title += "\"" + (feedTitles[i] == null ? "" : feedTitles[i]) + "\"";
            if (i < (feedTitles.length - 1)) {
                title += ", ";
            } else {
                title += ".";
            }
        }
        return title;
    }

    @Override
    String combinatorName() {
        return "Subtraction";
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.engine.AtomBase#combineResults(java.util.Set, java.util.List)
     */
    @Override
    Set<Entry> combineResults(Set<Entry> combination, List<Entry> entries) {
        combination.removeAll(entries);
        return combination;
    }

}
