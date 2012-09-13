package org.books.presentation.login.openidconnect;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ronny Stauffer
 */
@XmlRootElement
public class ProviderConfiguration {
    public String issuer;
    public String registration_endpoint;
    public String authorization_endpoint;
}
