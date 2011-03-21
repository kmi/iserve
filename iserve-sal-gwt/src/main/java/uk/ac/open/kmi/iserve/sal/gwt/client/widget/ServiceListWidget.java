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
import java.util.Map;

import uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseServiceAsync;
import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceListModel;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ServiceListWidget extends LayoutContainer {

	private ListStore<ServiceListModel> store;

	private Grid<ServiceListModel> grid;

	private RpcProxy<PagingLoadResult<ServiceListModel>>  proxy;

	private QuickTip tip;

	private ServiceBrowseServiceAsync serviceBrowseService;

	private String selectedService;

	private String queryString;

	private QueryStringGenerator queryStringGenerator;

	private PagingLoader<PagingLoadResult<ModelData>> loader;

	private TextField<String> searchField;

	private String directType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	private String msmNS = "http://cms-wg.sti2.org/ns/minimal-service-model#";

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		setLayout(new FitLayout());

		selectedService = "";
		queryStringGenerator = new QueryStringGenerator();

		serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig uriColumn = new ColumnConfig("label", "Name", 800);
		configs.add(uriColumn);

		ColumnConfig authorColumn = new ColumnConfig("author", "Created By", 300);
		configs.add(authorColumn);

		ColumnConfig dateColumn = new ColumnConfig("createTime", "Last Updated", 200);
		dateColumn.setAlignment(HorizontalAlignment.RIGHT);
		dateColumn.setDateTimeFormat(DateTimeFormat.getLongDateTimeFormat());
		configs.add(dateColumn);

		proxy = new RpcProxy<PagingLoadResult<ServiceListModel>>() {
			@Override
			public void load(Object loadConfig, AsyncCallback<PagingLoadResult<ServiceListModel>> callback) {
				serviceBrowseService.listServicesByQuery(queryString, (PagingLoadConfig) loadConfig, (AsyncCallback<PagingLoadResult<ServiceListModel>>) callback);
			}
		};

		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);  
		loader.setRemoteSort(true);

		store = new ListStore<ServiceListModel>(loader);

		final PagingToolBar pagingToolBar = new PagingToolBar(50);
		pagingToolBar.bind(loader);

		ColumnModel cm = new ColumnModel(configs);
		grid = new Grid<ServiceListModel>(store, cm);
		grid.setStateId("pagingGridExample");
		grid.setStateful(true);
		grid.setAutoExpandColumn("label");
		grid.setLoadMask(true);
		grid.setBorders(false);
		grid.setStripeRows(true);

		grid.addListener(Events.Attach, new Listener<GridEvent<ServiceListModel>>() {
			public void handleEvent(GridEvent<ServiceListModel> be) {
				PagingLoadConfig config = new BasePagingLoadConfig();
				config.setOffset(0);
				config.setLimit(50);

				Map<String, Object> state = grid.getState();
				if (state.containsKey("offset")) {
					int offset = (Integer)state.get("offset");
					int limit = (Integer)state.get("limit");
					config.setOffset(offset);
					config.setLimit(limit);
				}

				// sorting field
				if (state.containsKey("sortField")) {
					config.setSortField((String)state.get("sortField"));
					config.setSortDir(SortDir.valueOf((String)state.get("sortDir")));
				}
				loader.load(config);
			}
		});

		GridSelectionModel<ServiceListModel> selectionModel = new GridSelectionModel<ServiceListModel>();
		grid.setSelectionModel(selectionModel);

		selectionModel.addSelectionChangedListener(new SelectionChangedListener<ServiceListModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<ServiceListModel> se) {
				if ( se.getSelectedItem() != null ) {
					String selectedUri = se.getSelectedItem().getURIString();
					if ( selectedUri != null && selectedUri != "" ) {
						selectedService = selectedUri;
						WidgetController.get().getServiceDetailWidget().displayServiceDetails(new URIImpl(selectedUri));
					}
				}
			}
		});

		// Set up the context menu
		Menu contextMenu = new Menu();
		contextMenu.setWidth(140);

		MenuItem reviewService = new MenuItem();
		reviewService.setText("Add Review...");
		reviewService.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				final List<ServiceListModel> selectedServices = grid.getSelectionModel().getSelectedItems();
				ReviewDialog reviewDialog = new ReviewDialog(selectedServices.get(0));
				reviewDialog.show();
			}

		});
		contextMenu.add(reviewService);

		MenuItem deleteService = new MenuItem();
		deleteService.setText("Remove Service");
		deleteService.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				final List<ServiceListModel> selectedServices = grid.getSelectionModel().getSelectedItems();
				// Remove from both service repository and the store of grid
				List<URI> serviceURIs = new ArrayList<URI>();
				for ( ServiceListModel selectedService : selectedServices ) {
					serviceURIs.add(new URIImpl(selectedService.getURIString()));
				}

				serviceBrowseService.removeServices(serviceURIs, new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						MessageBox.alert("Remove services", "Error: " + caught.getMessage(), null);
					}

					public void onSuccess(Boolean result) {
						if ( result.booleanValue() == true ) {
							for ( ServiceListModel selectedService : selectedServices ) {
								Info.display("Remove Service", selectedService.getURIString());
								WidgetController.get().getServiceDetailWidget().clearContents();
								WidgetController.get().getServiceListWidget()
									.listServiceByCategory(WidgetController.get().getServiceCategoryWidget().getSelectedCategory());
							}
						} else {
							// TODO: reasons
							MessageBox.alert("Remove services", "Error: Fail to remove the service from iServe", null);
						}
					}

				});
			}
		});
		contextMenu.add(deleteService);

		grid.setContextMenu(contextMenu); // end of setting up the context menu

		// Set up the tool bar
		ToolBar toolBar = new ToolBar();

		Button refreshButton = new Button("Refresh");
		refreshButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				loader.load();
			}

		});
		toolBar.add(refreshButton); // end of refresh button

		final Button searchTypeButton = new Button("Search by Operation Name");
		Menu searchTypeMenu = new Menu();

		MenuItem byOperation = new MenuItem("Search by Operation Name");
		byOperation.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				searchTypeButton.setText("Search by Operation Name");
			}
			
		});
		searchTypeMenu.add(byOperation);

		MenuItem byCategory = new MenuItem("Search by Categorisation");
		byCategory.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				searchTypeButton.setText("Search by Categorisation");
			}
		});
		searchTypeMenu.add(byCategory);

		MenuItem byInput = new MenuItem("Search by Input Parameter");
		byInput.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				searchTypeButton.setText("Search by Input Parameter");
			}
		});
		searchTypeMenu.add(byInput);

		MenuItem byOutput = new MenuItem("Search by Output Parameter");
		byOutput.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				searchTypeButton.setText("Search by Output Parameter");
			}
		});
		searchTypeMenu.add(byOutput);

		MenuItem byAddress = new MenuItem("Search by Address");
		byAddress.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				searchTypeButton.setText("Search by Address");
			}
		});
		searchTypeMenu.add(byAddress);

		MenuItem byID = new MenuItem("Search by ID");
		byID.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				searchTypeButton.setText("Search by ID");
			}
		});
		searchTypeMenu.add(byID);

		searchTypeButton.setMenu(searchTypeMenu);
		toolBar.add(searchTypeButton); // end of searching type button

		searchField = new TextField<String>();
        searchField.setWidth(120);
        searchField.setMaxLength(40);
        searchField.setSelectOnFocus(true);
 
        searchField.addKeyListener(new KeyListener() {
        	@Override
        	public void componentKeyPress(ComponentEvent event) {
                if ( event.getKeyCode() == 13 ) {
                	queryString = queryStringGenerator.generate(searchField.getRawValue(), searchTypeButton.getText());
                	PagingLoadConfig config = new BasePagingLoadConfig();
                	config.setOffset(0);
                	config.setLimit(50);
                	loader.load(config);
                }
                super.componentKeyPress(event);
            }

        });
        toolBar.add(searchField); // end of searching field

        Button searchButton = new Button("Search");
		searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
            	queryString = queryStringGenerator.generate(searchField.getRawValue(), searchTypeButton.getText());
            	PagingLoadConfig config = new BasePagingLoadConfig();
            	config.setOffset(0);
            	config.setLimit(50);
               	loader.load(config);
			}
		});
		toolBar.add(searchButton); // end of searching button

		// Advanced Search
		Button advancedSearchButton = new Button("Advanced Search");
		advancedSearchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
//				AdvancedSearchDialog advancedSearchDialog = new AdvancedSearchDialog();
//				advancedSearchDialog.show();
			}
		});
//		toolBar.add(advancedSearchButton);

		ContentPanel cp = new ContentPanel();
		cp.setHeading("Service List");
		cp.setLayout(new FitLayout());
		cp.setTopComponent(toolBar);
		tip = new QuickTip(grid);
		cp.add(grid);
		cp.setBottomComponent(pagingToolBar);
		add(cp);
	}

	public void listServiceByQuery(String query) {
		if ( null == query || "" == query )
			return;
		queryString = query;
		PagingLoadConfig config = new BasePagingLoadConfig();
		config.setOffset(0);
		config.setLimit(50);
		loader.load(config);
	}

	public void listServiceByCategory(String category) {
		if ( null == category || "" == category )
			return;
		selectedService = "";
		if ( category.equalsIgnoreCase("All") ) {
			queryString = "SELECT DISTINCT * WHERE { ?s <" + directType + "> <" + msmNS + "Service> . " +
					"OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?sl . }" +
//					logQueryClauses + " ?s . } " +
					"}";
		} else {
			queryString = "SELECT DISTINCT * WHERE { ?s <" + directType + "> <" + msmNS + "Service> . " +
					"OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?sl . }" +
//					logQueryClauses + " ?s . } " +
					"?s <http://www.w3.org/ns/sawsdl#modelReference> ?cat . " +
					"?cat <http://www.w3.org/2000/01/rdf-schema#subClassOf> <" + category + "> }";
		}
		PagingLoadConfig config = new BasePagingLoadConfig();
		config.setOffset(0);
		config.setLimit(50);
		loader.load(config);
	}

	public String getSelectedService() {
		return selectedService;
	}

	public void clearSearchField() {
		searchField.clear();
	}

	private class QueryStringGenerator {

		private String generateRegex(String varName, String rawKeys) {
			String trimedKeys = rawKeys.trim();
			String[] tokens = trimedKeys.split(" ");
			StringBuffer sb = new StringBuffer();
			for ( int i = 0; i < tokens.length; i++ ) {
				if ( i > 0 ) {
					sb.append(" && ");
				}
				sb.append("(regex(str(");
				sb.append(varName);
				sb.append("), \"(");
				sb.append(tokens[i]);
				sb.append(")\", \"i\"))");
			}
			return sb.toString();
		}

		public String generate(String rawKeys, String type) {
			String result = "";
			if ( type.endsWith("Name") ) {
				result = "SELECT DISTINCT * WHERE { ?s <" + directType + "> <" + msmNS + "Service> . " +
						"OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?sl . }" +
//						logQueryClauses + " ?s . } " +
						"?s <" + msmNS + "hasOperation> ?o FILTER (" +
						generateRegex("?o", rawKeys) + ") }";
			} else if (type.endsWith("Categorisation")) {
				// Search by modelReference
				result = "SELECT DISTINCT * WHERE { ?s <" + directType + "> <" + msmNS + "Service> . " +
						"OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?sl . }" +
//						logQueryClauses + " ?s . } " +
						"?s <http://www.w3.org/ns/sawsdl#modelReference> ?o FILTER (" +
						generateRegex("?o", rawKeys) + ") }";
			} else if (type.endsWith("ID")) {
				// Search by modelReference
				result = "SELECT DISTINCT * WHERE { ?s <" + directType + "> <" + msmNS + "Service> FILTER (" +
						generateRegex("?s", rawKeys) + ") . " +
						"OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?sl . }" +
//						logQueryClauses + " ?s . } " +
						"}";
			} else if (type.endsWith("Input Parameter")) {
				result = "SELECT DISTINCT * WHERE { ?s <" + directType + "> <" + msmNS + "Service> . " +
//						logQueryClauses + " ?s . } " +
						"OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?sl . }" +
						"?s <" + msmNS + "hasOperation> ?o . ?o <" + msmNS + "hasInput> ?i . ?i <http://www.w3.org/ns/sawsdl#modelReference> ?it FILTER (" +
						generateRegex("?it", rawKeys) + ") }";
			} else if (type.endsWith("Output Parameter")) {
				result = "SELECT DISTINCT * WHERE { ?s <" + directType + "> <" + msmNS + "Service> . " +
						"OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?sl . }" +
//						logQueryClauses + " ?s . } " +
						"?s <" + msmNS + "hasOperation> ?op . ?op <" + msmNS + "hasOutput> ?o . ?o <http://www.w3.org/ns/sawsdl#modelReference> ?ot FILTER (" +
						generateRegex("?ot", rawKeys) + ") }";
			} else if (type.endsWith("Address")) {
				result = "SELECT DISTINCT * WHERE { ?s <" + directType + "> <" + msmNS + "Service> . " +
						"OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?sl . }" +
//						logQueryClauses + " ?s . } " +
						"?s <" + msmNS + "hasOperation> ?o . ?o <http://www.wsmo.org/ns/hrests#hasAddress> ?a FILTER (" +
						generateRegex("?a", rawKeys) + ") }";
			}
			return result;
		}
	}

}
