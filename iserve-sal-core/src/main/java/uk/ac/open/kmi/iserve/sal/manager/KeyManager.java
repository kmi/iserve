/*
   Copyright 2012  Knowledge Media Institute - The Open University

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
package uk.ac.open.kmi.iserve.sal.manager;


/**
 * Class Description
 * 
 * TODO: Complete
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface KeyManager {

	public abstract void saveConsumer(String consumerUriString,
			String consumerKey, String consumerSecret);

//	public abstract Consumer findConsumer(String consumerKey);
//
//	public abstract RequestToken findRequestToken(String requestTokenKey);
//
//	public abstract AccessToken findAccessToken(String accessTokenKey);

	public abstract void saveAuthorization(String requestTokenKey,
			String openid, String callback);

	public abstract void saveRequestToken(String tokenKey, String tokenSecret,
			String consumerKey);

	public abstract void deleteRequestToken(String tokenKey);

//	public abstract void saveAccessToken(String tokenKey, String tokenSecret,
//			RequestToken requestToken, String accessAs);

	public abstract void deleteAccessToken(String tokenKey);

}