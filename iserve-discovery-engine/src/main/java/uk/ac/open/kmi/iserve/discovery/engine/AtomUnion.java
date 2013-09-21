package uk.ac.open.kmi.iserve.discovery.engine;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import org.apache.abdera.model.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Path("/atom/{operator: or|and|union}")
public class AtomUnion extends AtomBase {

	@Override
	String combinatorFeedTitle(String[] feedTitles) {
		String title = "Union of entries from " + feedTitles.length + " feeds: ";
        for (int i = 0; i<feedTitles.length; i++ ) {
            title += "\"" + (feedTitles[i] == null ? "" : feedTitles[i]) + "\"";
            if (i < (feedTitles.length-1)) {
                title += ", ";
            } else {
                title += ".";
            }
        }
        return title;
	}

	@Override
	String combinatorName() {
		return "Union";
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.engine.AtomBase#combineResults(java.util.Set, java.util.List)
	 */
	@Override
	Set<Entry> combineResults(Set<Entry> combination, List<Entry> entries) {
		combination.addAll(entries);
		return combination;
	}

}
