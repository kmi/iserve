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
package uk.ac.open.kmi.iserve2.commons.io;

public class StringUtil {

	public static String formatUri(String uri) {
		String result = uri;
		if ( result.endsWith("/") == false ) {
			result += "/";
		}
		if ( result.startsWith("http://") == false && result.startsWith("https://") == false ) {
			result = "http://" + result;
		}
		return result;
	}

	public static String subStrings(String str1, String str2) {
		int string2Length = str2.length();
		int s = str1.indexOf(str2);
		return str1.substring(0, s) + str1.substring(s + string2Length);
	}

}
