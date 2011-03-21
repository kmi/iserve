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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;

public class AboutDialog extends Dialog {

	private Html aboutIServe;

	private Button okButton;

	public AboutDialog() {
		VBoxLayout layout = new VBoxLayout();  
        layout.setPadding(new Padding(5));  
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);  
		setLayout(layout);

		setButtons("");
		setButtonAlign(HorizontalAlignment.CENTER);
		setModal(true);
		setBodyBorder(true);
		setResizable(false);
		setClosable(false);
		setWidth(300);
		setHeight(150);

		setHeading("About iServe Browser");
		aboutIServe = new Html("<div align=center><b>iServe Browser v0.3</b></div>" +
				"<div align=center><a href='http://kmi.open.ac.uk/' target='_blank'><img src=\"resources/logo/kmi-logo-sm.png\" alt=\"KMi logo\" /></a>&nbsp;&nbsp;" +
				"<a href='http://www.soa4all.eu/' target='_blank'><img src=\"resources/logo/soa4all-logo-sm.png\" alt=\"SOA4All logo\" /></a></div>" +
				"<div align=center>Dong Liu, Carlos Pedrinaci, Jacek Kopecky</div>" +
				"<div align=center>Knowledge Media Institute, The Open University</div>");
		add(aboutIServe);
	}

	@Override
	protected void createButtons() {
		super.createButtons();
		okButton = new Button("OK");
		okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				AboutDialog.this.hide();
			}
			
		});
		addButton(okButton);
	}

}
