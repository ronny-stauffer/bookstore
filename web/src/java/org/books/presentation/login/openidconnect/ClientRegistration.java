/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.books.presentation.login.openidconnect;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
// JAXB Mapping required by JAX-RS JSON-to-Object Mapping
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Entity
public class ClientRegistration /* implements Serializable */ {
    //private static final long serialVersionUID = 1L;

    /**
     * Database Primary Key in form of technical key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Business Key
     */
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
    
    @XmlElement(name="expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiration;
    
    // Non-Public API
    // Standard Constructor required by JPA
    // Dieser Konstruktor darf nur innerhalb des Persistence Layers verwendet werden.
    // Er gilt als nicht-exposed und darf z.B. durch die Business Logic nicht verwendet werden!
    public ClientRegistration() {
        
    }

    // Non-Public API
    public ClientRegistration(String issuer) {
        assert (issuer != null && !issuer.isEmpty());
        
        this.issuer = issuer;
    }
    
    // Non-Public API
    // Diese Methode darf nur innerhalb des Persistence Layers verwendet werden.
    // Sie gilt als nicht-exposed und darf z.B. durch die Business Logic nicht verwendet werden!
    public Long getId() {
        return id;
    }

    // Business Key
    public String getIssuer() {
        return issuer;
    }
    
    // Diesen Setter gibt es nur deshalb, weil diese Entity auch gleichzeitig eine Value Object ist (aus Gründen der Einfachkeit).
    public void setIssuer(String issuer) {
        this.issuer = issuer;
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
    
    public Date getExpiration() {
        return expiration;
    }
    
    public void setExpiration(Date expiration) {
        this.expiration = expiration;
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