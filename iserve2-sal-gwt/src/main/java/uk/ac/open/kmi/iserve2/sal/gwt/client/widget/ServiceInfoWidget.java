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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.open.kmi.iserve2.sal.gwt.model.OperationInfoModel;
import uk.ac.open.kmi.iserve2.sal.model.impl.CommentImpl;
import uk.ac.open.kmi.iserve2.sal.model.impl.RatingImpl;
import uk.ac.open.kmi.iserve2.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve2.sal.model.common.URI;
import uk.ac.open.kmi.iserve2.sal.model.review.Comment;
import uk.ac.open.kmi.iserve2.sal.model.service.MessageContent;
import uk.ac.open.kmi.iserve2.sal.model.service.MessagePart;
import uk.ac.open.kmi.iserve2.sal.model.service.ModelReference;
import uk.ac.open.kmi.iserve2.sal.model.service.Operation;
import uk.ac.open.kmi.iserve2.sal.model.review.Rating;
import uk.ac.open.kmi.iserve2.sal.model.review.Review;
import uk.ac.open.kmi.iserve2.sal.model.service.Service;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

public class ServiceInfoWidget extends LayoutContainer {

	private TreeStore<OperationInfoModel> store;

	private TreeGrid<OperationInfoModel> tree;

	private String serviceUri;

	private QuickTip tip;

	private ContentPanel panel;

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		setLayout(new FitLayout());

		serviceUri = "";

		store = new TreeStore<OperationInfoModel>();

		ColumnConfig prop = new ColumnConfig("propName", "Property", 300);
		prop.setSortable(disabled);
		prop.setRenderer(new TreeGridCellRenderer<OperationInfoModel>());
		ColumnConfig value = new ColumnConfig("value", "Value", 400);
		value.setSortable(disabled);
		ColumnModel cm = new ColumnModel(Arrays.asList(prop, value));  

		tree = new TreeGrid<OperationInfoModel>(store, cm);
		tree.setBorders(false);
		tree.setAutoExpandColumn("propName");
		tree.setStripeRows(true);

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

		// the save button and menu
		Button saveButton = new Button("Download");
		Menu saveMenu = new Menu();
		MenuItem asXml = new MenuItem("Save as RDF/XML");

		asXml.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if ( serviceUri == "" ) {
					MessageBox.alert("Export service", "Please select a service first", null);
				} else {
					String form = GWT.getModuleBaseURL() + "download?format=xml&uri=" + URL.encodeComponent(serviceUri);
					Window.Location.assign(form);
				}
			}
		});
		saveMenu.add(asXml);

		MenuItem asN3 = new MenuItem("Save as N-Triples");
		asN3.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if ( serviceUri == "" ) {
					MessageBox.alert("Export service", "Please select a service first", null);
				} else {
					String form = GWT.getModuleBaseURL() + "download?format=nt&&uri=" + URL.encodeComponent(serviceUri);
					Window.Location.assign(form);
				}
			}
		});
		saveMenu.add(asN3);

		MenuItem asTrig = new MenuItem("Save as Trig");
		asTrig.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if ( serviceUri == "" ) {
					MessageBox.alert("Export service", "Please select a service first", null);
				} else {
					String form = GWT.getModuleBaseURL() + "download?format=trig&&uri=" + URL.encodeComponent(serviceUri);
					Window.Location.assign(form);
				}
			}
		});
		saveMenu.add(asTrig);

		MenuItem asTrix = new MenuItem("Save as Trix");
		asTrix.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if ( serviceUri == "" ) {
					MessageBox.alert("Export service", "Please select a service first", null);
				} else {
					String form = GWT.getModuleBaseURL() + "download?format=trix&&uri=" + URL.encodeComponent(serviceUri);
					Window.Location.assign(form);
				}
			}
		});
		saveMenu.add(asTrix);

		MenuItem asTurtle = new MenuItem("Save as Turtle");
		asTurtle.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if ( serviceUri == "" ) {
					MessageBox.alert("Export service", "Please select a service first", null);
				} else {
					String form = GWT.getModuleBaseURL() + "download?format=ttl&&uri=" + URL.encodeComponent(serviceUri);
					Window.Location.assign(form);
				}
			}
		});
		saveMenu.add(asTurtle);

		saveButton.setMenu(saveMenu);
		toolBar.add(saveButton);


//		// the expand button and menu
//		Button expandButton = new Button("Expand All");
//		expandButton.addListener(Events.OnClick,  new Listener<ButtonEvent>() {
//
//			public void handleEvent(ButtonEvent be) {
//				if ( be.isRightClick() == false ) {
//					tree.setExpanded((OperationInfoModel) tree.getModel(), true, true);
//				}
//			}
//		});
//		toolBar.add(expandButton);
//
//		// the collapse button and menu
//		Button collapseButton = new Button("Collapse All");
//		toolBar.add(collapseButton);

		panel = new ContentPanel();
		panel.setFrame(false);
		panel.setHeaderVisible(false);
		panel.setLayout(new FitLayout());
		panel.setBodyBorder(false);
		panel.setTopComponent(toolBar);
		panel.add(tree);
		tip = new QuickTip(tree);
		add(panel);
	}

	public void displayServiceInfo(Service service) {
		panel.el().mask("Loading...");
		store.removeAll();
		List<OperationInfoModel> data = new ArrayList<OperationInfoModel>();
		serviceUri = service.getURI().toString();
		data.add(new OperationInfoModel("<b>URI</b>", createLink(service.getURI().toString(), service.getURI().getLocalName())));

		if ( service.getSeeAlso() != null && service.getSeeAlso().size() > 0 ) {
			int i = 0;
			for ( URI seeAlso : service.getSeeAlso()) {
				if ( i == 0 ) {
					data.add(new OperationInfoModel("<b>See Also</b>", createLink(seeAlso.toString(), seeAlso.getLocalName())));
				} else {
					data.add(new OperationInfoModel("", createLink(seeAlso.toString(), seeAlso.getLocalName())));
				}
				i++;
			}
		}

		if ( service.getSources() != null && service.getSources().size() > 0 ) {
			int i = 0;
			for ( URI def : service.getSources()) {
				if ( i == 0 ) {
					data.add(new OperationInfoModel("<b>Source</b>", createLink(def.toString(), def.getLocalName())));
				} else {
					data.add(new OperationInfoModel("", createLink(def.toString(), def.getLocalName())));
				}
				i++;
			}
		}

		convertModelReferences("<b>Model Reference</b>", data, service.getModelReferences());

		// display rating and comment from LUF
		convertReviews("<b>Review</b>", data, service.getReviews());

		List<OperationInfoModel> opList = new ArrayList<OperationInfoModel>();
		if ( service.getOperations() != null && service.getOperations().size() > 0 ) {
			for ( Operation op : service.getOperations()) {
				convertOperation(opList, op);
			}
		}

		// TODO: address
		OperationInfoModel[] nullOpInfo = new OperationInfoModel[0];
		OperationInfoModel opNode = new OperationInfoModel("<b>Operations</b>", "", opList.toArray(nullOpInfo));
		data.add(opNode);

		store.add(data, true);
		panel.el().unmask();
	}

	private void convertReviews(String rootNodeName, List<OperationInfoModel> parent, List<Review> reviews) {
		if ( reviews == null || reviews.size() <= 0 )
			return;
		OperationInfoModel opNode = new OperationInfoModel(rootNodeName, "");
		for ( Review review : reviews ) {
			String reviewerTime = "By " + review.getReviewerUri().toString() + "<br>At " + review.getCreateTime();
			if ( review instanceof RatingImpl ) {
				OperationInfoModel ratingNode = new OperationInfoModel("<b><span qtip='" + reviewerTime + "'>Rating</span></b>", "" + ((Rating)review).getValue() + " / " + ((Rating)review).getMaxValue());
				opNode.add(ratingNode);
			} else if ( review instanceof CommentImpl ) {
				OperationInfoModel commentNode = new OperationInfoModel("<b><span qtip='" + reviewerTime + "'>Comment</span></b> ", "" + ((Comment)review).getContent());
				opNode.add(commentNode);
			}
		}
		parent.add(opNode);
	}

	private void convertOperation(List<OperationInfoModel> parent, Operation op) {
		List<OperationInfoModel> opProp = new ArrayList<OperationInfoModel>();

		//Model References
		convertModelReferences("<b>Model Reference</b>", opProp, op.getModelReferences());

		List<OperationInfoModel> msgList = new ArrayList<OperationInfoModel>();

		convertUriList("<b>Address</b>", opProp, op.getAddresses());

		// Input Messages
		convertMessages("Input Message", msgList, op.getInputs());

		// Input Faults
		convertMessages("Input Fault", msgList, op.getInputFaults());

		// Output Messages
		convertMessages("Output Message", msgList, op.getOutputs());

		// Output Faults
		convertMessages("Output Fault", msgList, op.getOutputFaults());

		OperationInfoModel[] nullOpInfo = new OperationInfoModel[0];
//		OperationInfoModel msgNode = new OperationInfoModel("<b>Messages</b>", "", msgList.toArray(nullOpInfo));
		String label = op.getURI().getLocalName();
		if ( op.getLabels() != null && op.getLabels().size() > 0 && op.getLabels().get(0) != null && op.getLabels().get(0) != "" ) {
			label = op.getLabels().get(0);
		}

		OperationInfoModel opNode = new OperationInfoModel("<b><span qtip='" + op.getURI().toString() + "'>" + label + "</span></b>",
				"", opProp.toArray(nullOpInfo));

		for ( OperationInfoModel msg : msgList )
			opNode.add(msg);
		parent.add(opNode);
	}

	private void convertMessages(String rootNodeName, List<OperationInfoModel> parent, List<MessageContent> msgs) {
		if ( msgs == null || msgs.size() <= 0 )
			return;

		for ( MessageContent msg : msgs ) {
			OperationInfoModel msgNode = new OperationInfoModel("<b>" + rootNodeName + " Name</b>", createServiceLink(msg.getURI().toString(), msg.getLabels()));
			parent.add(msgNode);
			convertModelReferences("&nbsp;&nbsp;Parameter", parent, msg.getModelReferences());
			convertUriList("&nbsp;&nbsp;Lowering Schema Mapping", parent, msg.getLoweringSchemaMappings());
			convertUriList("&nbsp;&nbsp;Lifting Schema Mapping", parent, msg.getLiftingSchemaMappings());
			convertParts("&nbsp;&nbsp;Part", parent, msg.getAllParts());
		}
	}

	private void convertParts(String rootNodeName, List<OperationInfoModel> parent, List<MessagePart> allParts) {
		if ( allParts == null || allParts.size() <= 0 )
			return;
		for ( MessagePart part : allParts ) {
			OperationInfoModel opNode = new OperationInfoModel(rootNodeName, createServiceLink(part.getURI().toString(), part.getLabels()));
			parent.add(opNode);
			convertModelReferences("&nbsp;&nbsp;&nbsp;&nbsp;Parameter", parent, part.getModelReferences());
		}
	}

	private void convertUriList(String rootNodeName, List<OperationInfoModel> parent, List<URI> schemaMappings) {
		if ( schemaMappings == null || schemaMappings.size() <= 0 )
			return;
		URI uri = schemaMappings.get(0);
		if ( null == uri || null == uri.toString() || null == uri.getLocalName() )
			return;
		OperationInfoModel opNode = new OperationInfoModel(rootNodeName, createLink(uri.toString(), uri.getLocalName()));
		parent.add(opNode);
		if ( schemaMappings.size() > 1 ) {
			for ( int i = 1; i < schemaMappings.size(); i++ ) {
				uri = schemaMappings.get(i);
				if ( uri != null || uri.toString() != null || uri.getLocalName() != null ) {
					OperationInfoModel uriNode = new OperationInfoModel("", createLink(uri.toString(), uri.getLocalName()));
					parent.add(uriNode);
				}
			}
		}
	}

	private void convertModelReferences(String rootNodeName, List<OperationInfoModel> parent, List<ModelReference> modelReferences) {
		if ( modelReferences == null || modelReferences.size() <= 0 )
			return;
		OperationInfoModel opNode = new OperationInfoModel(rootNodeName, createLink(modelReferences.get(0).getURI().toString(), modelReferences.get(0).getURI().toString()));
		parent.add(opNode);
		if ( modelReferences.size() > 1 ) {
			for ( int i = 1; i < modelReferences.size(); i++ ) {
				URI uri = modelReferences.get(i).getURI();
				OperationInfoModel uriNode = new OperationInfoModel("", createLink(uri.toString(), uri.toString()));
				parent.add(uriNode);
			}
		}
	}

	private String createLink(String uri, String text) {
		if ( text == null || text == "" ) {
			text = uri;
		}
		String result = "<span qtip='" + uri + "'><a href=\"" + uri + "\" target=\"_blank\">" + text + "</a></span>";
		return result;
	}

	public void clearContents() {
		store.removeAll();
		serviceUri = "";
	}

	// FIXME: temporarily for dealing with URL
	private String createServiceLink(String uri, List<String> labels) {
		String label = "";
		if ( labels != null && labels.size() > 0 && labels.get(0) != null && labels.get(0).toString() != null && labels.get(0).toString() != "" ) {
			label = labels.get(0).toString();
		} else {
			label = new URIImpl(uri).getLocalName();
		}
//		String url = uri.replaceAll("http://local.soa4all.eu/browser/resource/services#", "http://localhost:8080/browser/resource/services/");
		String result = "<span qtip='" + uri + "'><a href=\"" + uri + "\" target=\"_blank\">" + label + "</a></span>";
		return result;
	}

}
