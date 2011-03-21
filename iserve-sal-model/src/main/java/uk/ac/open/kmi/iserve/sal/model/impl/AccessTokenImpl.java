package uk.ac.open.kmi.iserve.sal.model.impl;

import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.oauth.AccessToken;
import uk.ac.open.kmi.iserve.sal.model.oauth.RequestToken;

public class AccessTokenImpl extends TokenImpl implements AccessToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8260548063053152477L;

	private RequestToken requestToken;

	private URI accessAs;

	public AccessTokenImpl() {
		super();
	}

	public AccessTokenImpl(URI uri) {
		super(uri);
	}

	public AccessTokenImpl(URI uri, String tokenKey, String tokenSecret) {
		super(uri, tokenKey, tokenSecret);
	}

	public AccessTokenImpl(String uriString, String tokenKey, String tokenSecret) {
		super(uriString, tokenKey, tokenSecret);
	}

	public RequestToken getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(RequestToken requestToken) {
		this.requestToken = requestToken;
	}

	public URI getAccessAs() {
		return accessAs;
	}

	public void setAccessAs(URI accessAs) {
		this.accessAs = accessAs;
	}

}
