package uk.ac.open.kmi.iserve2.sal.model.oauth;

import uk.ac.open.kmi.iserve2.sal.model.Entity;

public interface Token extends Entity {

	public String getTokenKey();

	public void setTokenKey(String tokenKey);

	public String getTokenSecret();

	public void setTokenSecret(String tokenSecret);

}
