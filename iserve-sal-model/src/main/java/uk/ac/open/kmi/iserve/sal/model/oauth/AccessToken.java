package uk.ac.open.kmi.iserve2.sal.model.oauth;

import uk.ac.open.kmi.iserve2.sal.model.common.URI;

public interface AccessToken extends Token {

	public RequestToken getRequestToken();

	public void setRequestToken(RequestToken requestToken);

	public URI getAccessAs();

	public void setAccessAs(URI accessAs);

}
