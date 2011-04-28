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

import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceUploadData;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
//import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
//import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;

public class ServiceUploadWidget extends LayoutContainer {

	private ComboBox<ServiceUploadData> comboOWLSVersion;

	private Radio urlRadio;

	private TextField<String> urlField;

	private Radio fileRadio;

	private FileUploadField fileField;

	private Radio contentRadio;

	private TextArea contentField;

	private FormPanel uploadForm;

	@Override  
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		setLayout(new FillLayout());
		FormData formData = new FormData("100%");

		uploadForm = new FormPanel();
		uploadForm.setPadding(5);
		uploadForm.setHeaderVisible(false);  
		uploadForm.setBodyBorder(false);
		uploadForm.setEncoding(Encoding.MULTIPART);
		uploadForm.setButtonAlign(HorizontalAlignment.CENTER);
		uploadForm.setScrollMode(Scroll.AUTO);

		ListStore<ServiceUploadData> storeType = new ListStore<ServiceUploadData>(); 
		ServiceUploadData uploadWSDL = new ServiceUploadData("Annotated WSDL");
//		ServiceUploadData uploadRDF = new ServiceUploadData("RDF");
		ServiceUploadData uploadHtml = new ServiceUploadData("Annotated HTML");
		ServiceUploadData uploadOwls = new ServiceUploadData("OWL-S File");
		storeType.add(uploadHtml);
		storeType.add(uploadWSDL);
		storeType.add(uploadOwls);
		createWSDLVersionSelection();
		createOWLSVersionSelection();
//		createRDFFomratSelection();

		FieldSet sourceSet = new FieldSet();
		sourceSet.setHeading("Data Source");
		sourceSet.setLayout(new FormLayout());

		RadioGroup radioGroup = new RadioGroup("radio");
		urlRadio = new Radio();
		urlRadio.setHideLabel(true);
		urlRadio.setValueAttribute("url");
//		urlRadio.setName("radio");
		urlRadio.setBoxLabel("URL");
		urlRadio.setValue(true);
		radioGroup.add(urlRadio);
		sourceSet.add(urlRadio);  

		urlField = new TextField<String>();
		urlField.setName("url");
		urlField.setHideLabel(true);
		urlField.setAllowBlank(true);
		sourceSet.add(urlField, formData);

		fileRadio = new Radio();  
		fileRadio.setHideLabel(true);
		fileRadio.setValueAttribute("file");
//		fileRadio.setName("radio");
		fileRadio.setBoxLabel("File");
		radioGroup.add(fileRadio);
		sourceSet.add(fileRadio);  

		fileField = new FileUploadField();
		fileField.setHideLabel(true);
		fileField.setAllowBlank(true);
		fileField.setName("file");
//		fileField.disable();
		sourceSet.add(fileField, formData);

		contentRadio = new Radio();
		contentRadio.setHideLabel(true);
		contentRadio.setValueAttribute("content");
//		contentRadio.setName("radio");
		contentRadio.setBoxLabel("Content");
		radioGroup.add(contentRadio);
		sourceSet.add(contentRadio, formData);

		contentField = new TextArea();
		contentField.setName("content");
		contentField.setHideLabel(true);
		contentField.setAllowBlank(true);
//		contentField.disable();
		sourceSet.add(contentField, formData);

//		uploadForm.add(typeSet);
		uploadForm.add(sourceSet);

		uploadForm.setAction("iServeBrowser/upload");
		uploadForm.setMethod(Method.POST);
		uploadForm.addListener(Events.Submit, new Listener<FormEvent>() {
			public void handleEvent(FormEvent be) {
				String resultHtml = be.getResultHtml();
				if ( resultHtml.contains("Successfully add") ) {
					MessageBox.info("Info", resultHtml, null);
				} else {
					MessageBox.alert("Error", resultHtml, null);
				}
//				// reset the file upload field
//				fileField.setRawValue("");
//				contentField.setRawValue("");
//				urlField.setRawValue("");
//				urlRadio.setValue(true);
//				fileRadio.setValue(false);
//				contentRadio.setValue(false);
				fileField.clear();
				urlField.clear();
				contentField.clear();
				urlRadio.setValue(true);
				fileRadio.setValue(false);
				contentRadio.setValue(false);

//				if ( resultHtml.equalsIgnoreCase("success") ) {
//					// refresh the service list.
//					String selectedCategory = WidgetController.get().getServiceCategoryWidget().getSelectedCategory();
//					WidgetController.get().getServiceListWidget().listServiceByCategory(selectedCategory);
//					MessageBox.info("Infomation", "Successfully upload to the server.", null);
//				} else {
//					MessageBox.alert("Alert", "Errors happen in uploading!", null);
//				}
				// refresh the services list.
				WidgetController.get().getServiceListWidget()
					.listServiceByCategory(WidgetController.get().getServiceCategoryWidget().getSelectedCategory());
			}
		});

		Button uploadBtn = new Button("Upload");
		uploadBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if ( urlRadio.getValue() == true && urlField.getRawValue() == "" ) {
					MessageBox.alert("Error", "The URL is null!", null);
					return;
				}
				if ( fileRadio.getValue() == true && fileField.getRawValue() == "" ) {
					MessageBox.alert("Error", "The file path is null!", null);
					return;
				}
				if ( contentRadio.getValue() == true && contentField.getRawValue() == "" ) {
					MessageBox.alert("Error", "The content is null!", null);
					return;
				}
				uploadForm.submit();
			}
		});

		Button resetBtn = new Button("Reset");
		resetBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
//				// reset the file upload field
//				fileField.setRawValue("");
//				contentField.setRawValue("");
//				urlField.setRawValue("");
//				urlRadio.setValue(true);
//				fileRadio.setValue(false);
//				contentRadio.setValue(false);

				fileField.clear();
				urlField.clear();
				contentField.clear();
				urlRadio.setValue(true);
				fileRadio.setValue(false);
				contentRadio.setValue(false);
			}
		});

		uploadForm.addButton(uploadBtn);
		uploadForm.addButton(resetBtn);  

		add(uploadForm);
	}

	private void createWSDLVersionSelection() {
		ListStore<ServiceUploadData> storeWSDLVersion = new ListStore<ServiceUploadData>();
		//ServiceUploadData ver11 = new ServiceUploadData("1.1");
		ServiceUploadData ver20 = new ServiceUploadData("2.0");
		//storeWSDLVersion.add(ver11);
		storeWSDLVersion.add(ver20);
	}

	private void createOWLSVersionSelection() {
		ListStore<ServiceUploadData> storeOWLSVersion = new ListStore<ServiceUploadData>();
		//ServiceUploadData ver11 = new ServiceUploadData("1.1");
		ServiceUploadData ver11 = new ServiceUploadData("1.1");
		//storeWSDLVersion.add(ver11);
		storeOWLSVersion.add(ver11);

		comboOWLSVersion = new ComboBox<ServiceUploadData>();  
		comboOWLSVersion.setFieldLabel("Version");
		comboOWLSVersion.setName("version");
		comboOWLSVersion.setDisplayField("value");  
		comboOWLSVersion.setTriggerAction(TriggerAction.ALL);  
		comboOWLSVersion.setStore(storeOWLSVersion);  
		comboOWLSVersion.setEditable(false);
		comboOWLSVersion.setVisible(false);
		comboOWLSVersion.setAllowBlank(false);
		comboOWLSVersion.setValue(ver11);
//		typeSet.add(comboOWLSVersion);
	}

}
