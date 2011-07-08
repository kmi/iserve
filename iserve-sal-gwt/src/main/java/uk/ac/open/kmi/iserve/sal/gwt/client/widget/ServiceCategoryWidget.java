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

import java.util.List;

import uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseServiceAsync;
import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceCategoryModel;
import uk.ac.open.kmi.iserve.sal.gwt.model.TaxonomyModel;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;

import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ServiceCategoryWidget extends LayoutContainer {

	private TreePanel<ServiceCategoryModel> treePanel;

	private TreePanelSelectionModel<ServiceCategoryModel> selectionModel;

	private QuickTip tip;

	private ToolBar toolBar;

	private ComboBox<TaxonomyModel> taxonomyComboBox;

	private StoreFilterField<ServiceCategoryModel> filter;

	private String selectedCategory;

	private TreeStore<ServiceCategoryModel> store;

	private ListStore<TaxonomyModel> taxStore;

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		setLayout(new FitLayout());

		selectedCategory = "";

		final ServiceBrowseServiceAsync serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();

		// trees store
		store = new TreeStore<ServiceCategoryModel>();
		serviceBrowseService.getAllCategories("Prefixed", new AsyncCallback<List<ServiceCategoryModel>>() {

			public void onFailure(Throwable caught) {
				MessageBox.alert("Load taxonomy", "Error: " + caught.getMessage(), null);
			}

			public void onSuccess(List<ServiceCategoryModel> scr) {
				for ( ServiceCategoryModel scm : scr ) {
					if ( scm.getParent() == null ) {
						store.add(scm, true);
					} else {
						store.add(scm.getParent(), scm, true);
					}
				}
			}
		});

		store.setKeyProvider(new ModelKeyProvider<ServiceCategoryModel>() {

			public String getKey(ServiceCategoryModel model) {
				return model.getId();
			}
		});

		treePanel = new TreePanel<ServiceCategoryModel>(store);
		treePanel.setBorders(false);
		treePanel.setStateful(true);
		treePanel.setDisplayProperty("displayName");

		selectionModel = new TreePanelSelectionModel<ServiceCategoryModel>();
		treePanel.setSelectionModel(selectionModel);

		selectionModel.addSelectionChangedListener(new SelectionChangedListener<ServiceCategoryModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<ServiceCategoryModel> se) {
				if ( se.getSelectedItem() != null ) {
					selectedCategory = se.getSelectedItem().getURI();
					WidgetController.get().getServiceListWidget().clearSearchField();
					WidgetController.get().getServiceListWidget().listServiceByCategory(se.getSelectedItem().getURI());
				}
			}
		});

	    filter = new StoreFilterField<ServiceCategoryModel>() {

			@Override
			protected boolean doSelect(Store<ServiceCategoryModel> store,
					ServiceCategoryModel parent, ServiceCategoryModel record,
					String property, String filter) {
	    		String name = parent.get("uri");
	    		name = name.toLowerCase();
	    		if (name.indexOf(filter.toLowerCase()) != -1) {
	    			return true;
	    		}
	    		return false;
			}

	    };

	    filter.bind(treePanel.getStore());
		toolBar = new ToolBar();
		IconButton filterBtn = new IconButton("icon-filter");
	    filterBtn.setWidth(20);
	    toolBar.add(filterBtn);
	    toolBar.add(filter);

		ToolBar taxToolBar = new ToolBar();
		Text taxText = new Text("Taxonomy:");
		taxStore = new ListStore<TaxonomyModel>();
		TaxonomyModel defaultTaxonomy = new TaxonomyModel("Service Finder", "Prefixed");
		taxStore.add(defaultTaxonomy);

		taxonomyComboBox = new ComboBox<TaxonomyModel>();
		taxonomyComboBox.setDisplayField("name");
		taxonomyComboBox.setTriggerAction(TriggerAction.ALL); 
		taxonomyComboBox.setStore(taxStore);
		taxonomyComboBox.setValue(defaultTaxonomy);

//		taxonomyComboBox.addKeyListener(new KeyListener() {
//			public void componentKeyUp(ComponentEvent event) {
//				if ( event.getKeyCode() == 13 ) {
//					String displayName = new URIImpl(taxonomyComboBox.getRawValue()).getLocalName();
//					TaxonomyModel taxonomy = new TaxonomyModel(displayName, taxonomyComboBox.getRawValue());
//					taxStore.add(taxonomy);
//					taxonomyComboBox.setRawValue("");
//					taxonomyComboBox.setValue(taxonomy);
//				}
//			}
//		});

		taxonomyComboBox.addSelectionChangedListener(new SelectionChangedListener<TaxonomyModel>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<TaxonomyModel> se) {
				store.removeAll();
				selectedCategory = "";
				serviceBrowseService.getAllCategories(se.getSelectedItem().getURI(), new AsyncCallback<List<ServiceCategoryModel>>() {

					public void onFailure(Throwable caught) {
						MessageBox.alert("Get all categories", "Error: " + caught.getMessage(), null);
					}

					public void onSuccess(List<ServiceCategoryModel> scr) {
						for ( ServiceCategoryModel scm : scr ) {
							if ( scm.getParent() == null ) {
								store.add(scm, true);
							} else {
								store.add(scm.getParent(), scm, true);
							}
						}
					}
				});
			}
		});

		serviceBrowseService.listTaxonomy(new AsyncCallback<List>() {

			public void onSuccess(List taxonomyList) {
				for ( URI taxonomyUri : (List<URI>) taxonomyList ) {
					if ( !taxonomyUri.toString().equalsIgnoreCase("file://service-categories-dong.rdfs") ) {
						TaxonomyModel taxonomy = new TaxonomyModel(taxonomyUri.getLocalName(), taxonomyUri.toString());
						taxStore.add(taxonomy);
					}
				}
				taxonomyComboBox.setEditable(false);
			}
			
			public void onFailure(Throwable error) {
				MessageBox.alert("Error", error.toString(), null);
			}
		});

		taxToolBar.add(taxText);
		taxToolBar.add(taxonomyComboBox);

		ContentPanel navPanel = new ContentPanel();
		navPanel.setHeading("Service Category");
		navPanel.setLayout(new FitLayout());
		navPanel.setTopComponent(taxToolBar);

		ContentPanel categoryPanel = new ContentPanel();
		categoryPanel.setHeaderVisible(false);
		categoryPanel.setBorders(false);
		categoryPanel.setBodyBorder(false);
		categoryPanel.setLayout(new FitLayout());
		categoryPanel.setTopComponent(toolBar);
		tip = new QuickTip(treePanel);
	    categoryPanel.add(treePanel);
	    navPanel.add(categoryPanel);

	    add(navPanel);
	}

	public String getSelectedCategory() {
		return selectedCategory;
	}

}
