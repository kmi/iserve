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
package uk.ac.open.kmi.iserve.sal.gwt.client.widget;

import java.util.ArrayList;
import java.util.List;

import uk.ac.open.kmi.iserve.sal.gwt.model.QueryResultDisplayModel;
import uk.ac.open.kmi.iserve.sal.model.query.QueryResult;
import uk.ac.open.kmi.iserve.sal.model.query.QueryRow;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

public class QueryResultWidget extends LayoutContainer {

	private ListStore<QueryResultDisplayModel> store;

	private Grid<QueryResultDisplayModel> grid;

	private QueryResult queryResult;

	public QueryResultWidget(QueryResult queryResult) {
		this.queryResult = queryResult;
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		if ( queryResult == null || queryResult.getVariables() == null || queryResult.getVariables().size() <= 0 ) {
			return;
		}

		setLayout(new FitLayout());
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		for ( String var : queryResult.getVariables() ) {
			ColumnConfig uriColumn = new ColumnConfig(var, var, 300);
			configs.add(uriColumn);
		}

		store = new ListStore<QueryResultDisplayModel>();

		for ( QueryRow queryRow: queryResult.getQueryRows() ) {
			store.add(new QueryResultDisplayModel(queryResult.getVariables(), queryRow));
		}

		ColumnModel cm = new ColumnModel(configs);
		grid = new Grid<QueryResultDisplayModel>(store, cm);
//		grid.setAutoExpandColumn("s");
		grid.setBorders(false);
		grid.setStripeRows(true);

		ContentPanel panel = new ContentPanel();
		panel.setFrame(false);
		panel.setHeaderVisible(false);
		panel.setLayout(new FitLayout());
		panel.setBodyBorder(false);
		panel.add(grid);
		add(panel);
	}

}
