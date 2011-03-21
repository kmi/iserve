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
package uk.ac.open.kmi.iserve2.sal.gwt.client.widget;

import uk.ac.open.kmi.iserve2.sal.gwt.client.ServiceBrowseServiceAsync;
import uk.ac.open.kmi.iserve2.sal.model.common.URI;
import uk.ac.open.kmi.iserve2.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve2.sal.model.service.Service;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;

public class ServiceDetailWidget extends LayoutContainer {

	private TabItem info;

	private TabItem doc;

	private TabItem upload;

	private TabItem query;

	private ServiceInfoWidget operationWidget;

	private ServiceDocWidget docWidget;

	private ServiceUploadWidget uploadWidget;

	private SparqlQueryWidget queryWidget;

	private TabPanel tabPanel;

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		setLayout(new FitLayout());
		tabPanel = new TabPanel();

		info = new TabItem("Info");
		info.setLayout(new FitLayout());
		info.setHeight("100%");
		info.setWidth("100%");
		operationWidget = new ServiceInfoWidget();
		info.add(operationWidget);
		tabPanel.add(info);

		doc = new TabItem("Document");
		docWidget =  new ServiceDocWidget();
		doc.add(docWidget);
		doc.setLayout(new FitLayout());
		tabPanel.add(doc);

		query = new TabItem("Query");
		query.setLayout(new FitLayout());
		queryWidget = new SparqlQueryWidget();
		query.add(queryWidget);
		tabPanel.add(query);
		
		upload =  new TabItem("Upload");
		upload.setLayout(new FitLayout());
		uploadWidget = new ServiceUploadWidget();
		upload.add(uploadWidget);

		tabPanel.setHeight("100%");
		tabPanel.setMinTabWidth(100);
		add(tabPanel);

		tabPanel.addListener(Events.Select, new Listener<TabPanelEvent>() {

			public void handleEvent(TabPanelEvent be) {
				TabItem tabItem = be.getItem();
				if ( tabItem != null && tabItem.getText().endsWith("Info") ) {
					if ( WidgetController.get() != null &&
							WidgetController.get().getServiceListWidget() != null &&
							WidgetController.get().getServiceListWidget().getSelectedService() != null &&
							WidgetController.get().getServiceListWidget().getSelectedService() != "" ) {
						URI serviceUri = new URIImpl(WidgetController.get().getServiceListWidget().getSelectedService());
						displayServiceDetails(serviceUri);
					}
				}
			}

		});
	}

	public void displayServiceDetails(URI serviceUri) {
		if ( serviceUri != null ) {
			final ServiceBrowseServiceAsync serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();
			serviceBrowseService.getService(serviceUri, new AsyncCallback<Service>() {

				public void onFailure(Throwable throwable) {
					info.removeAll();
					doc.removeAll();
					HTMLPanel errPanel = new HTMLPanel("Error: " + throwable);
					info.add(errPanel);
					info.layout();
				}

				public void onSuccess(Service service) {
					// FIXME: all the tabs should be refreshed.
					operationWidget.displayServiceInfo(service);
					docWidget.displayServiceDocument(service);
				}				
			});
		}
	}

	public void clearContents() {
		operationWidget.clearContents();
	}

	public void updateWhenLoggedIn(String result) {
		tabPanel.add(upload);
		upload.setVisible(true);
		upload.enable();
	}

	public void updateWhenLoggedOut() {
		tabPanel.remove(upload);
		upload.setVisible(false);
		upload.disable();
	}

	public void addQueryResultWidget(QueryResultWidget queryResultWidget) {
		TabItem tabItem = new TabItem("Query Result");
		tabItem.setClosable(true);
		tabItem.setLayout(new FitLayout());
		tabItem.add(queryResultWidget);
		tabPanel.add(tabItem);
		tabPanel.setSelection(tabItem);
	}

}
