/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.books.presentation.login.openidconnect;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ronny Stauffer
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Entity
public class ClientRegistration /* implements Serializable */ {
    //private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100, unique = true)
    private String issuer;
    
    @NotNull
    @XmlElement(name="client_id")
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String clientIdentifier;
    @NotNull
    @XmlElement(name="client_secret")
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String clientSecret;    
    
    // Non-Public API
    public ClientRegistration() {
        
    }
    
    public static ClientRegistration create(String issuer) {
        if (issuer == null) {
            throw new NullPointerException("issuer must not be null!");
        }
        if (issuer.isEmpty()) {
            throw new IllegalArgumentException("issuer must not be empty!");
        }
        
        ClientRegistration clientRegistration = new ClientRegistration();
        clientRegistration.issuer = issuer;
        
        return clientRegistration;
    }
    
    // Non-Public API
    public Long getId() {
        return id;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        //hash += (id != null ? id.hashCode() : 0);
        hash = issuer.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ClientRegistration)) {
            return false;
        }
        ClientRegistration other = (ClientRegistration) object;
        //if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
        //    return false;
        //}
        //return true;
        return issuer.equals(other.issuer);
    }

    @Override
    public String toString() {
        //return "org.books.presentation.login.openidconnect.ClientRegistration[ id=" + id + " ]";
        return "org.books.presentation.login.openidconnect.ClientRegistration[ issuer=" + issuer + " ]";
    }
}