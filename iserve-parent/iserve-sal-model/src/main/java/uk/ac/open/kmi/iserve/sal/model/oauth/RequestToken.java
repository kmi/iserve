package uk.ac.open.kmi.iserve.sal.model.oauth;

import java.util.Date;

import uk.ac.open.kmi.iserve.sal.model.common.URI;

public interface RequestToken extends Token {

	public URI getCallback();

	public void setCallback(URI callback);

	public URI getGrantedBy();

	public void setGrantedBy(URI grantedBy);

	public Date getGrantedAt();

	public void setGrantedAt(Date grantedAt);

	public URI getConsumer();

	public void setConsumer(URI consumer);

}
