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
package uk.ac.open.kmi.iserve.sal.gwt.client;

import uk.ac.open.kmi.iserve.sal.gwt.client.widget.HeaderWidget;
import uk.ac.open.kmi.iserve.sal.gwt.client.widget.ProfileDialog;
import uk.ac.open.kmi.iserve.sal.gwt.client.widget.ServiceCategoryWidget;
import uk.ac.open.kmi.iserve.sal.gwt.client.widget.ServiceDetailWidget;
import uk.ac.open.kmi.iserve.sal.gwt.client.widget.ServiceListWidget;
import uk.ac.open.kmi.iserve.sal.gwt.client.widget.WidgetController;
import uk.ac.open.kmi.iserve.sal.model.user.User;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class iServeBrowser implements EntryPoint {

//	public static final String SERVICE_BROWSE_SERVICE = GWT.getModuleBaseURL() + "browse";

	private Viewport viewport;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		viewport = new Viewport();
		viewport.setLayout(new BorderLayout());

		// North panel
		createHeaderPanel();

		// create navigator panel
		createNavigatorPanel();

		// Centre panel
		createCentrePanel();

		RootPanel.get().add(viewport);

		validateSession();
	}

	private void createHeaderPanel() {
		HeaderWidget headerWidget = new HeaderWidget();
		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 25);
		northData.setMargins(new Margins(5, 5, 0, 5));
		WidgetController.get().setHeaderWidget(headerWidget);
		viewport.add(headerWidget, northData);		
	}

	private void createNavigatorPanel() {
		ServiceCategoryWidget serviceCategory = new ServiceCategoryWidget();
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 260);
		westData.setSplit(true);  
		westData.setCollapsible(true);
		westData.setMargins(new Margins(5));
		WidgetController.get().setServiceCategoryWidget(serviceCategory);
		viewport.add(serviceCategory, westData);
	}

	private void createCentrePanel() {
		LayoutContainer servicePanelWrapper = new LayoutContainer();
		servicePanelWrapper.setLayout(new BorderLayout());
		final ServiceListWidget serviceList = new ServiceListWidget();
		final ServiceDetailWidget serviceDetail = new ServiceDetailWidget();

		BorderLayoutData serviceListLayoutData = new BorderLayoutData(LayoutRegion.CENTER);
		serviceListLayoutData.setSplit(true);
		serviceListLayoutData.setMargins(new Margins(0, 0, 5, 0));
		servicePanelWrapper.add(serviceList, serviceListLayoutData);
		WidgetController.get().setServiceListWidget(serviceList);

		BorderLayoutData serviceDetailLayoutData = new BorderLayoutData(LayoutRegion.SOUTH, 400);
		serviceDetailLayoutData.setSplit(true);
		servicePanelWrapper.add(serviceDetail, serviceDetailLayoutData);
		WidgetController.get().setServiceDetailWidget(serviceDetail);

		BorderLayoutData centreData = new BorderLayoutData(LayoutRegion.CENTER);
		centreData.setMargins(new Margins(5, 5, 5, 0));
		viewport.add(servicePanelWrapper, centreData);
	}

	private void validateSession() {
		final ServiceBrowseServiceAsync serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();
		AsyncCallback<User> asyncCallback = new AsyncCallback<User>() {

			public void onFailure(Throwable caught) {
				MessageBox.alert("Invalid Login", "Error: " + caught.getMessage(), null);
			}

			public void onSuccess(User user) {
				if ( user != null ) {
					WidgetController.get().updateWhenLoggedIn(user.getOpenId().toString());
//					if ( user.getUserName() == null || user.getUserName() == "" ) {
//						Info.display("Info", "user.getUserName()");
//					} else {
//						Info.display("Info", user.getUserName());
//					}
					if ( user.getFoafId() == null ) {
						// show profile dialog
						ProfileDialog profileDialog = new ProfileDialog(user);
						profileDialog.show();
					}
				} else {
					WidgetController.get().updateWhenLoggedOut();
				}
			}
		};
		serviceBrowseService.validateSession(asyncCallback);
	}

}
