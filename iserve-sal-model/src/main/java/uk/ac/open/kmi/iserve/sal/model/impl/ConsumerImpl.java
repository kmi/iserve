package uk.ac.open.kmi.iserve.sal.model.impl;

import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.oauth.Consumer;

public class ConsumerImpl extends EntityImpl implements Consumer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4877159772179734132L;

	private String consumerKey;

	private String consumerSecret;

	public ConsumerImpl() {
		super();
		setConsumerKey(null);
		setConsumerSecret(null);
	}

	public ConsumerImpl(URI uri) {
		super(uri);
		setConsumerKey(null);
		setConsumerSecret(null);
	}

	public ConsumerImpl(URI uri, String consumerKey, String consumerSecret) {
		super(uri);
		setConsumerKey(consumerKey);
		setConsumerSecret(consumerSecret);
	}

	public ConsumerImpl(String uriString, String consumerKey, String consumerSecret) {
		super(new URIImpl(uriString));
		setConsumerKey(consumerKey);
		setConsumerSecret(consumerSecret);
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

}
