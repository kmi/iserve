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
import uk.ac.open.kmi.iserve2.sal.gwt.model.StringModel;
import uk.ac.open.kmi.iserve2.sal.model.query.QueryResult;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SparqlQueryWidget extends LayoutContainer {

	private ComboBox<StringModel> comboRepo;

	private TextArea sparqlField;

	private FormPanel queryForm;

	private StringModel serviceRepo;

	private ServiceBrowseServiceAsync serviceBrowseService;

	@Override  
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);

		serviceBrowseService = ServiceBrowseServiceAsync.Util.getInstance();

		setLayout(new FillLayout());
		FormData formData = new FormData("100%");

		queryForm = new FormPanel();
//		queryForm.setWidth("100%");
		queryForm.setLayout(new FormLayout());
//		queryForm.setLayout(new FormLayout());
		queryForm.setPadding(5);
		queryForm.setHeaderVisible(false);  
		queryForm.setBodyBorder(false);
		queryForm.setButtonAlign(HorizontalAlignment.CENTER);
		queryForm.setScrollMode(Scroll.AUTO);
		queryForm.setLayoutData(new FormData("90%"));

		ListStore<StringModel> storeRepo = new ListStore<StringModel>(); 
		serviceRepo = new StringModel("Service");
		storeRepo.add(serviceRepo);
		storeRepo.add(new StringModel("Log"));

		comboRepo = new ComboBox<StringModel>();
		comboRepo.setFieldLabel("RDF Repository");
		comboRepo.setDisplayField("value");
//		comboRepo.setWidth(800);
		comboRepo.setTriggerAction(TriggerAction.ALL);  
		comboRepo.setStore(storeRepo);
		comboRepo.setEditable(false);
		comboRepo.setAllowBlank(false);
		comboRepo.setValue(serviceRepo);
//		comboRepo.setLayoutData(new FormData("100%"));
//		queryForm.add(comboRepo, formData);

		// Example of SPARQL query
		String queryString = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n" +
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
			"PREFIX wsl:<http://www.wsmo.org/ns/wsmo-lite#>\n" +
			"PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#>\n" +
			"PREFIX msm:<http://cms-wg.sti2.org/ns/minimal-service-model#>\n\n" +
			"SELECT ?s WHERE {\n" +
			"?s rdf:type msm:Service. \n" +
			"?s sawsdl:modelReference ?modelref . \n" +
			"?modelref rdfs:subClassOf <http://www.service-finder.eu/ontologies/ServiceCategories#Category> . \n" +
			"}\n";
		sparqlField = new TextArea();
		sparqlField.setFieldLabel("SPARQL Query");
		sparqlField.setName("sparql");
		sparqlField.setAllowBlank(false);
		sparqlField.setHeight(270);
		sparqlField.setValue(queryString);
//		sparqlField.setWidth(800);
//		sparqlField.setLayoutData(new FormData("100%"));
		queryForm.add(sparqlField, formData);

		Button queryBtn = new Button("Query");
		queryBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
//				String selectedRepo = comboRepo.getRawValue();
				String queryString = sparqlField.getRawValue();
				AsyncCallback<QueryResult> queryCallback = new AsyncCallback<QueryResult>() {

					public void onFailure(Throwable caught) {
						MessageBox.alert("Execute query", "Error: " + caught.getMessage(), null);
					}

					public void onSuccess(QueryResult result) {
						QueryResultWidget queryResultWidget = new QueryResultWidget(result);
						WidgetController.get().getServiceDetailWidget().addQueryResultWidget(queryResultWidget);
//						Info.display("OK", result.toString());
					}
				};

//				if ( selectedRepo.equalsIgnoreCase("Service") ) {
					serviceBrowseService.executeQuery(queryString, queryCallback);
//				} else {
//					serviceBrowseService.exectueLogQuery(queryString, queryCallback);;
//				}
			}
		});

		Button resetBtn = new Button("Reset");
		resetBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				comboRepo.setValue(serviceRepo);
				sparqlField.setRawValue("");
			}
		});

		queryForm.addButton(queryBtn);
		queryForm.addButton(resetBtn);  

		add(queryForm);
	}

}
