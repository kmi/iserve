package uk.ac.open.kmi.iserve.sal.model.oauth;

import uk.ac.open.kmi.iserve.sal.model.Entity;

public interface Token extends Entity {

	public String getTokenKey();

	public void setTokenKey(String tokenKey);

	public String getTokenSecret();

	public void setTokenSecret(String tokenSecret);

}
