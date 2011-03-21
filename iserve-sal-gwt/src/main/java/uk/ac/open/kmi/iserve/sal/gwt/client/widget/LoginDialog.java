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
import uk.ac.open.kmi.iserve.sal.gwt.model.StringModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginDialog extends Dialog {

	protected ComboBox<StringModel> comboProvider; // OpenId Provider

	protected TextField<String> identifier;

	protected TextField<String> openId;

	protected Button reset;

	protected Button login;

	protected Status status;

	public LoginDialog() {
		setLayout(new FormLayout());

		setButtonAlign(HorizontalAlignment.LEFT);
		setButtons("");
		setHeading("Login");
		setModal(true);
		setBodyBorder(true);
		setResizable(false);
		setWidth(345);

		openId = new TextField<String>();
		openId.setFieldLabel("OpenID");
		openId.setWidth(300);
		openId.addKeyListener(new KeyListener() {
			public void componentKeyUp(ComponentEvent event) {
				validate();
				if ( event.getKeyCode() == 13 ) {
					onSubmit();
				}
			}
		});

		// combo box for openID provider
		ListStore<StringModel> storeProvider = new ListStore<StringModel>(); 
		StringModel myopenid = new StringModel("MyOpenID");
		storeProvider.add(myopenid);
		storeProvider.add(new StringModel("Yahoo"));
		storeProvider.add(new StringModel("Google"));

		comboProvider = new ComboBox<StringModel>();
		comboProvider.setFieldLabel("Provider");
		comboProvider.setDisplayField("value");
		comboProvider.setWidth(200);
		comboProvider.setTriggerAction(TriggerAction.ALL);  
		comboProvider.setStore(storeProvider);
		comboProvider.setEditable(false);
		comboProvider.setAllowBlank(false);
		comboProvider.setValue(myopenid);
		comboProvider.addSelectionChangedListener(new SelectionChangedListener<StringModel>() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent<StringModel> se) {
				generateFullOpenID();
				validate();
			}
		});

		identifier = new TextField<String>();
		identifier.setFieldLabel("ID");
		identifier.setWidth(200);
		identifier.addKeyListener(new KeyListener() {
			public void componentKeyUp(ComponentEvent event) {
				generateFullOpenID();
				validate();
				if ( event.getKeyCode() == 13 ) {
					onSubmit();
				}
			}
		});

		add(comboProvider);
		add(identifier);
		add(openId);
		setFocusWidget(comboProvider);
	}

	@Override
	protected void createButtons() {
		super.createButtons();
		status = new Status();
		status.setBusy("please wait...");
		status.hide();
		status.setAutoWidth(true);
		getButtonBar().add(status);
		getButtonBar().add(new FillToolItem());

		reset = new Button("Reset");
		reset.addSelectionListener(new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) {
				comboProvider.reset();
				identifier.reset();
				openId.reset();
				identifier.focus();
				validate();
			}

		});

		login = new Button("Login");
		login.disable();
		login.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				onSubmit();
			}
		});

		addButton(reset);
		addButton(login);
	}

	protected void onSubmit() {
		status.show();
		getButtonBar().disable();
		final ServiceBrowseServiceAsync serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();
		String url = GWT.getModuleBaseURL().replaceAll(GWT.getModuleName() + "/", "");
		String openIdUri = openId.getValue();
		serviceBrowseService.login(openIdUri, url, new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				MessageBox.alert("Invalid Login", "Error: " + caught.getMessage(), null);
			}

			public void onSuccess(String result) {
				if (result == null || result.length() == 0) {
					MessageBox.alert("Error", "Log-in failed!", null);                        
				} else if (result.startsWith("Error")) {
                	MessageBox.alert("Error", result.replaceFirst("Error:", ""), null);
                } else {
                	LoginDialog.this.hide();
                	redirect(result);
                }
			}
		});

	}

	protected boolean hasValue(TextField<String> field) {
		return field.getValue() != null && field.getValue().length() > 0;
	}

	protected void validate() {
		login.setEnabled(hasValue(openId));
	}

	protected void generateFullOpenID() {
		// generate the full OpenID.
		if ( identifier.getValue() != null ) {
			String provider = comboProvider.getValue().getValue();
			if ( provider.equalsIgnoreCase("MyOpenID") ) {
				openId.setValue("http://" + identifier.getValue() + ".myopenid.com");
			} else if ( provider.equalsIgnoreCase("Yahoo") ) {
				openId.setValue("https://me.yahoo.com/" + identifier.getValue());
			} else if ( provider.equalsIgnoreCase("Google") ) {
				openId.setValue("http://openid-provider.appspot.com/" + identifier.getValue());
			}
		}
	}

	//redirect the browser to the given url
	public static native void redirect(String url)/*-{
		$wnd.location = url;
	}-*/;

}
