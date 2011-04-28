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

public class WidgetController {

	private ServiceCategoryWidget serviceCategoryWidget;

	private ServiceListWidget serviceListWidget;

	private ServiceDetailWidget serviceDetailWidget;

	private HeaderWidget headerWidget;

	private static WidgetController theController = new WidgetController();

	private WidgetController() {

	}

	public static WidgetController get() {
		return theController;
	}

	public ServiceListWidget getServiceListWidget() {
		return this.serviceListWidget;
	}

	public void setServiceListWidget(ServiceListWidget serviceListWidget) {
		this.serviceListWidget = serviceListWidget;
	}

	public ServiceDetailWidget getServiceDetailWidget() {
		return this.serviceDetailWidget;
	}

	public void setServiceDetailWidget(ServiceDetailWidget serviceDetailWidget) {
		this.serviceDetailWidget = serviceDetailWidget;
	}

	public ServiceCategoryWidget getServiceCategoryWidget() {
		return serviceCategoryWidget;
	}

	public void setServiceCategoryWidget(ServiceCategoryWidget serviceCategoryWidget) {
		this.serviceCategoryWidget = serviceCategoryWidget;
	}

	public void setHeaderWidget(HeaderWidget headerWidget) {
		this.headerWidget = headerWidget;
	}

	public HeaderWidget getHeaderWidget() {
		return headerWidget;
	}

	public void updateWhenLoggedIn(String result) {
		headerWidget.updateWhenLoggedIn(result);
		serviceDetailWidget.updateWhenLoggedIn(result);
	}

	public void updateWhenLoggedOut() {
		headerWidget.updateWhenLoggedOut();
		serviceDetailWidget.updateWhenLoggedOut();
	}

}
