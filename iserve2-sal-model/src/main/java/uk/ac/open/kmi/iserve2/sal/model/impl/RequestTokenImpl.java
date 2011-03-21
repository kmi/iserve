package uk.ac.open.kmi.iserve2.sal.model.impl;

import java.util.Date;

import uk.ac.open.kmi.iserve2.sal.model.common.URI;
import uk.ac.open.kmi.iserve2.sal.model.oauth.RequestToken;

public class RequestTokenImpl extends TokenImpl implements RequestToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4043483337302052685L;

	private URI callback;

	private URI grantedBy;

	private URI consumer;

	private Date grantedAt;

	public RequestTokenImpl() {
		super();
	}

	public RequestTokenImpl(URI uri) {
		super(uri);
	}

	public RequestTokenImpl(URI uri, String tokenKey, String tokenSecret) {
		super(uri, tokenKey, tokenSecret);
	}

	public RequestTokenImpl(String uriString, String tokenKey, String tokenSecret) {
		super(uriString, tokenKey, tokenSecret);
	}

	public URI getCallback() {
		return callback;
	}

	public void setCallback(URI callback) {
		this.callback = callback;
	}

	public URI getGrantedBy() {
		return grantedBy;
	}

	public void setGrantedBy(URI grantedBy) {
		this.grantedBy = grantedBy;
	}

	public Date getGrantedAt() {
		return grantedAt;
	}

	public void setGrantedAt(Date grantedAt) {
		this.grantedAt = grantedAt;
	}

	public URI getConsumer() {
		return consumer;
	}

	public void setConsumer(URI consumer) {
		this.consumer = consumer;
	}

}
