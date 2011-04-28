package uk.ac.open.kmi.iserve.sal.model.oauth;

import uk.ac.open.kmi.iserve.sal.model.Entity;

public interface Consumer extends Entity {

	public String getConsumerKey();

	public void setConsumerKey(String consumerKey);

	public String getConsumerSecret();

	public void setConsumerSecret(String consumerSecret);

}
