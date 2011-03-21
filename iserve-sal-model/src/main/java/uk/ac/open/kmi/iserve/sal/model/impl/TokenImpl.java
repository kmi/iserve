package uk.ac.open.kmi.iserve2.sal.model.impl;

import uk.ac.open.kmi.iserve2.sal.model.common.URI;
import uk.ac.open.kmi.iserve2.sal.model.oauth.Token;

public class TokenImpl extends EntityImpl implements Token {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9063558971581295425L;

	private String tokenKey;

	private String tokenSecret;

	public TokenImpl() {
		super();
		setTokenKey(null);
		setTokenSecret(null);
	}

	public TokenImpl(URI uri) {
		super(uri);
		setTokenKey(null);
		setTokenSecret(null);
	}

	public TokenImpl(URI uri, String tokenKey, String tokenSecret) {
		super(uri);
		setTokenKey(tokenKey);
		setTokenSecret(tokenSecret);
	}

	public TokenImpl(String uriString, String tokenKey, String tokenSecret) {
		super(new URIImpl(uriString));
		setTokenKey(tokenKey);
		setTokenSecret(tokenSecret);
	}

	public String getTokenKey() {
		return tokenKey;
	}

	public void setTokenKey(String tokenKey) {
		this.tokenKey = tokenKey;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

}
