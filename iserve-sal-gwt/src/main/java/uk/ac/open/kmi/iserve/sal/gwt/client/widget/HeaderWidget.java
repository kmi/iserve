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

import uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseServiceAsync;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve.sal.model.user.User;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class HeaderWidget extends LayoutContainer {

	private String openId;

	private Html userId;

	private Button loginButton;

	private Button aboutButton;

	@Override
	protected void onRender(Element parent, int index) {
		initJavaScriptAPI(this);
		super.onRender(parent, index);
		setLayout(new FitLayout());

		FlowLayout layout = new FlowLayout();

		LayoutContainer container = new LayoutContainer(layout);

		Html name = new Html();
		name.setStyleAttribute("float", "left");
		name.setHtml("<img src=\"resources/logo/iserve-browser-logo.png\" height=20 alt=\"iServe logo\" />");
		container.add(name);

		userId = new Html("");
		userId.setStyleAttribute("float", "right");

		aboutButton = new Button("About");
		aboutButton.setStyleAttribute("float", "right");
		aboutButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				AboutDialog aboutDialog = new AboutDialog();
				aboutDialog.show();
			}
			
		});
		container.add(aboutButton);

		loginButton = new Button("Log In");
		loginButton.setStyleAttribute("float", "right");
		loginButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if (ce.getButton().getText().endsWith("In")) {
					LoginDialog loginDialog = new LoginDialog();
					loginDialog.show();
				} else {
					final ServiceBrowseServiceAsync serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();
					serviceBrowseService.logout("", new AsyncCallback<Boolean>() {

						public void onSuccess(Boolean result) {
							WidgetController.get().updateWhenLoggedOut();
						}

						public void onFailure(Throwable caught) {
							MessageBox.alert("Logout", "Error: " + caught.getMessage(), null);
						}

					});
				}
			}
		});
		container.add(loginButton);

		container.add(userId);
		add(container);
	}

	public void openProfileDialog() {
		final ServiceBrowseServiceAsync serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();
		serviceBrowseService.getUser(new URIImpl(openId), new AsyncCallback<User>() {
			
			public void onSuccess(User user) {
				ProfileDialog profileDialog = new ProfileDialog(user);
				profileDialog.show();
			}
			
			public void onFailure(Throwable caught) {
				MessageBox.alert("Open profile Dialog", "Error: " + caught.getMessage(), null);
			}
		});
	}

	public void updateWhenLoggedIn(String result) {
		openId = result;
		userId.setHtml("<a href=javascript:openDialog()><b>" + result + "</b></a>");
		loginButton.setText("Log Out");
		layout();
	}

	public void updateWhenLoggedOut() {
		userId.setHtml("");
		loginButton.setText("Log In");
		layout();
	}

	public static native void initJavaScriptAPI(HeaderWidget headerWidget)/*-{
		$wnd.openDialog = function () {
			headerWidget.@uk.ac.open.kmi.iserve.sal.gwt.client.widget.HeaderWidget::openProfileDialog()();
		};
	}-*/;

}

