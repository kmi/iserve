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
import uk.ac.open.kmi.iserve2.sal.model.impl.UserImpl;
import uk.ac.open.kmi.iserve2.sal.model.user.User;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProfileDialog extends Dialog {

	protected TextField<String> userName;

	protected TextField<String> password;

	protected TextField<String> newPassword;

	protected TextField<String> confirmPassword;

	protected TextField<String> openId;

	protected TextField<String> foafId;

//	protected Button check;

	protected Button cancel;

	protected Button save;

	protected Status status;

	protected boolean isNewUser;

	public ProfileDialog(User userProfile) {
		isNewUser = false;
		setLayout(new FormLayout());

		setButtonAlign(HorizontalAlignment.LEFT);
		setButtons("");
		setHeading("Profile");
		setModal(true);
		setBodyBorder(true);
		setResizable(false);
		setWidth(322);

		userName = new TextField<String>();
		userName.setFieldLabel("User Name");
		if ( userProfile != null && userProfile.getUserName() != null && userProfile.getUserName() != "" ) {
			userName.setReadOnly(true);
			userName.setValue(userProfile.getUserName());
		} else {
			userName.setReadOnly(false);
			isNewUser = true;
		}
		userName.setWidth(300);
		add(userName);

		password = new TextField<String>();
		password.setFieldLabel("Password");
		password.setPassword(true);
		password.setWidth(300);
		if ( userProfile != null && userProfile.getUserName() != null && userProfile.getUserName() != "" ) {
			add(password);
		} else {
			isNewUser = true;
		}

		newPassword = new TextField<String>();
		newPassword.setFieldLabel("New Password");
		newPassword.setPassword(true);
		newPassword.setWidth(300);
		add(newPassword);

		confirmPassword = new TextField<String>();
		confirmPassword.setFieldLabel("Re-type Password");
		confirmPassword.setPassword(true);
		confirmPassword.setWidth(300);
		add(confirmPassword);

		openId = new TextField<String>();
		openId.setFieldLabel("OpenID");
		openId.setWidth(300);
		openId.setValue(userProfile.getOpenId().toString());
		openId.setReadOnly(true);
		add(openId);

		foafId = new TextField<String>();
		foafId.setFieldLabel("FOAF ID");
		foafId.setWidth(300);
		foafId.setEmptyText("A FOAF ID MUST BE A URI!");
		foafId.setReadOnly(!isNewUser);
		foafId.addKeyListener(new KeyListener() {
			public void componentKeyUp(ComponentEvent event) {
				validate();
				if ( event.getKeyCode() == 13 ) {
					onSave();
				}
			}
		});

		if ( userProfile != null && userProfile.getFoafId() != null ) {
			foafId.setValue(userProfile.getFoafId().toString());
			validate();
		}
		add(foafId);
	}

	protected boolean hasUriValue(TextField<String> field) {
		if ( field.getValue() != null && field.getValue().length() > 0 ) {
			if ( field.getValue().startsWith("http") ) {
				return true;
			}
		}
		return false;
	}

	protected void validate() {
		if ( hasUriValue(foafId) == true ) {
			save.setEnabled(true);
		} else {
			save.setEnabled(false);
		}
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

		cancel = new Button("Cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				ProfileDialog.this.hide();
			}
		});
		addButton(cancel);

//		check = new Button("Check");
//		check.addSelectionListener(new SelectionListener<ButtonEvent>() {
//			public void componentSelected(ButtonEvent ce) {
//				// check if user name and the FOAF ID is unique
//				onCheck();
//			}
//		});
//		addButton(check);

		save = new Button("Save");
		save.setEnabled(false);
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if ( newPassword.getRawValue().equals(confirmPassword.getRawValue()) ) {
					onSave();
				} else {
					Info.display("Error", "Please verify your password again");
				}
			}
		});
		addButton(save);
	}

//	protected void onCheck() {
//		status.show();
//		getButtonBar().disable();
//		final User user = new UserImpl();
//		user.setOpenId(new URIImpl(openId.getRawValue()));
//		if ( foafId.getRawValue() != null && foafId.getRawValue() != "" ) {
//			user.setFoafId(new URIImpl(foafId.getRawValue()));
//		}
//		if ( userName.getRawValue() != null && userName.getRawValue() != "" ) {
//			user.setUserName(userName.getRawValue());
//		}
//		if ( password.getRawValue() != null && password.getRawValue() != "" ) {
//			user.setPassword(password.getRawValue());
//		}
//		final ServiceBrowseServiceAsync serviceBrowseService = (ServiceBrowseServiceAsync) Registry.get(ServiceBrowser.SERVICE_BROWSE_SERVICE);
//		AsyncCallback<String> asyncCallback = new AsyncCallback<String>(){
//
//			public void onFailure(Throwable caught) {
//				Info.display("Error", caught.toString());
//			}
//
//			public void onSuccess(String result) {
//				status.hide();
//				getButtonBar().enable();
//				Info.display("Info", result);
//			}
//		};
//		serviceBrowseService.checkUser(user, asyncCallback);
//	}

	protected void onSave() {
		status.show();
		getButtonBar().disable();
		final User user = new UserImpl();
		user.setFoafId(new URIImpl(foafId.getRawValue()));
		user.setOpenId(new URIImpl(openId.getRawValue()));
		user.setUserName(userName.getRawValue());
		user.setPassword(password.getRawValue());

		if ( newPassword.getRawValue() != null && newPassword.getRawValue() != "" ) {
			user.setNewPassword(newPassword.getRawValue());
			if ( isNewUser == true ) {
				user.setPassword(newPassword.getRawValue());
			}
		}
		final ServiceBrowseServiceAsync serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();
		AsyncCallback<URI> asyncCallback = new AsyncCallback<URI>(){
			public void onFailure(Throwable caught) {
				MessageBox.alert("Save user profile", "Error: " + caught.getMessage(), null);
				status.hide();
				getButtonBar().enable();
			}

			public void onSuccess(URI result) {
				ProfileDialog.this.hide();
				Info.display("Info", "OK");
//				Info.display("Info", result.toString());
//				if (result.toString().equalsIgnoreCase("OK")) {
//					ProfileDialog.this.hide();
//				} else {
//					status.hide();
//					getButtonBar().enable();
//				}
			}
		};

		if ( isNewUser == true ) {
			serviceBrowseService.addUser(user, asyncCallback);
		} else {
			serviceBrowseService.updateUser(user, asyncCallback);
		}
	}

}
