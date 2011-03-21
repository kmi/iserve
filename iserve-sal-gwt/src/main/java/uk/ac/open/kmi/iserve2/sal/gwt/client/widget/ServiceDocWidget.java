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

import java.util.List;

import uk.ac.open.kmi.iserve2.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve2.sal.model.common.URI;
import uk.ac.open.kmi.iserve2.sal.model.service.Service;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

public class ServiceDocWidget extends LayoutContainer {

	ContentPanel panel;

	String docUri;

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		setLayout(new FitLayout());

		// set up the tool bar
		ToolBar toolBar = new ToolBar();

		Button refreshButton = new Button("Refresh");
		toolBar.add(refreshButton);
		refreshButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				ServiceDetailWidget serviceDetail = WidgetController.get().getServiceDetailWidget();
				URI serviceUri = new URIImpl(WidgetController.get().getServiceListWidget().getSelectedService());
				serviceDetail.displayServiceDetails(serviceUri);
			}

		});
		toolBar.add(refreshButton);

		// the save button and menu
		Button saveButton = new Button("Download");
		saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				String form = GWT.getModuleBaseURL() + "download?format=raw&uri=" + URL.encodeComponent(docUri);
				Window.Location.assign(form);;
			}
		});
		toolBar.add(saveButton);

		panel = new ContentPanel();
		panel.setFrame(false);
		panel.setHeaderVisible(false);
		panel.setBodyBorder(false);
		panel.setLayout(new FitLayout());
		panel.setTopComponent(toolBar);
		add(panel);
	}

	public void displayServiceDocument(Service service) {
		List<URI> docUriList = service.getSources();
		if ( docUriList == null || docUriList.size() <= 0 )
			return;
		panel.el().mask("Loading...");
		docUri = docUriList.get(0).toString();
		panel.setUrl(docUri);
		panel.el().unmask();
		panel.layout();
	}

}
