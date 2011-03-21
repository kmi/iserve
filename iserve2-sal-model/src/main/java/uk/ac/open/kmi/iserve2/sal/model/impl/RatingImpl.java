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
package uk.ac.open.kmi.iserve2.sal.model.impl;

import uk.ac.open.kmi.iserve2.sal.model.review.Rating;

public class RatingImpl extends ReviewImpl implements Rating {

	private static final long serialVersionUID = -8990915074712639641L;

	private double value;

	private double maxValue;

	private double minValue;

	public RatingImpl() {
		setValue(0);
		setMinValue(0);
		setMaxValue(0);
	}

	public RatingImpl(double value, double minValue, double maxValue) {
		setValue(value);
		setMinValue(minValue);
		setMaxValue(maxValue);
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getValue() {
		return value;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
