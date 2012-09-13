package org.books.presentation.login.openidconnect;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ronny Stauffer
 */
@XmlRootElement
public class Issuer {
    public List<String> locations; 
}
