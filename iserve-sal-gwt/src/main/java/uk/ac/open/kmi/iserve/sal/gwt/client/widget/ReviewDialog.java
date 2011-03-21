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
import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceListModel;
import uk.ac.open.kmi.iserve.sal.gwt.model.StringModel;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve.sal.model.review.Comment;
import uk.ac.open.kmi.iserve.sal.model.impl.RatingImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.CommentImpl;
import uk.ac.open.kmi.iserve.sal.model.review.Rating;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ReviewDialog extends Dialog {

	protected TextField<String> serviceUriField;

	protected ComboBox<StringModel> ratingCombo;

	protected TextArea commentField;

	protected Button submit;

	protected Button cancel;

	private String serviceUriString;

	public ReviewDialog(ServiceListModel serviceListModel) {
		setLayout(new FormLayout());

		setButtonAlign(HorizontalAlignment.CENTER);
		setButtons("");
		setHeading("Review Service");
		setModal(false);
		setBodyBorder(true);
		setResizable(false);
		setWidth(345);

		this.serviceUriString = serviceListModel.getURIString();
		serviceUriField = new TextField<String>();
		serviceUriField.setFieldLabel("Service Name");
		serviceUriField.setToolTip(serviceUriString);
		serviceUriField.setValue(serviceListModel.getName());
		add(serviceUriField, new FormData("100%"));

		ListStore<StringModel> ratings = new ListStore<StringModel>();
		ratings.add(new StringModel("1"));
		ratings.add(new StringModel("2"));
		ratings.add(new StringModel("3"));
		ratings.add(new StringModel("4"));
		ratings.add(new StringModel("5"));

		ratingCombo = new ComboBox<StringModel>();
		ratingCombo.setFieldLabel("Rating");
		ratingCombo.setStore(ratings);
		ratingCombo.setDisplayField("value");
		ratingCombo.setEditable(false);
		ratingCombo.setTriggerAction(TriggerAction.ALL);
		add(ratingCombo, new FormData("100%"));

		commentField = new TextArea();
		commentField.setFieldLabel("Comment");
		add(commentField, new FormData("100%"));
	}

	@Override
	protected void createButtons() {
		super.createButtons();
		submit = new Button("Submit");
		submit.addSelectionListener(new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) {
				ReviewDialog.this.hide();
				// TODO: Check logged in first
				Rating rating = null;
				if ( ratingCombo.getRawValue() != null ) {
					rating = new RatingImpl();
					rating.setValue(Double.valueOf(ratingCombo.getRawValue()));
					rating.setMinValue(1.0);
					rating.setMaxValue(5.0);
				}
				Comment comment = null;
				if ( commentField.getRawValue() != null ) {
					comment = new CommentImpl(commentField.getRawValue());
				}

				final ServiceBrowseServiceAsync serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();
				serviceBrowseService.reviewService(new URIImpl(serviceUriString), rating, comment, new AsyncCallback<Boolean>() {

					public void onSuccess(Boolean result) {
						if ( result.booleanValue() == true ) {
							Info.display("Review service", "Seccessfully store reviews to LUF");
							WidgetController.get().getServiceDetailWidget().displayServiceDetails(new URIImpl(serviceUriString));
						} else {
							MessageBox.alert("Review service", "Error: Fail to store reviews to LUF", null);
						}
					}

					public void onFailure(Throwable caught) {
						MessageBox.alert("Review services", "Error: " + caught.getMessage(), null);
					}
				});
			}

		});

		cancel = new Button("Cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) {
				ReviewDialog.this.hide();
			}

		});

		addButton(submit);
		addButton(cancel);
	}

}
