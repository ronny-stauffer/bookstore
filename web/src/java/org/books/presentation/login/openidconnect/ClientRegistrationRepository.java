package org.books.presentation.login.openidconnect;

import java.util.Calendar;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author Ronny Stauffer
 */
public class ClientRegistrationRepository {
    private static final Logger LOGGER = Logger.getLogger(ClientRegistrationRepository.class.getName());
    
    private final EntityManager entityManager;
    
    public ClientRegistrationRepository(EntityManager entityManager) {
        if (entityManager == null) {
            throw new NullPointerException("entityManager must not be null!");
        }
        
        this.entityManager = entityManager;
    }
    
    public ClientRegistration create(String issuer) {
        if (issuer == null) {
            throw new NullPointerException("issuer must not be null!");
        }
        if (issuer.isEmpty()) {
            throw new IllegalArgumentException("issuer must not be empty!");
        }
        
        ClientRegistration clientRegistration = new ClientRegistration(issuer);
        
        return clientRegistration;
    }    
    
    public ClientRegistration find(String issuer) {
        if (issuer == null) {
            throw new NullPointerException("issuer must not be null!");
        }
        if (issuer.isEmpty()) {
            throw new IllegalArgumentException("issuer must not be empty!");
        }

        ClientRegistration clientRegistration = null;
        try {
            clientRegistration = entityManager.createQuery("select clientRegistration from ClientRegistration clientRegistration where clientRegistration.issuer = :issuer", ClientRegistration.class)
                .setParameter("issuer", issuer)
                .getSingleResult();
        } catch (NoResultException e) {
            // Ignore exception
        }
        
        return clientRegistration;
    }
    
    public ClientRegistration findValid(String issuer) {
        ClientRegistration validClientRegisration = null;
        
        ClientRegistration clientRegistration = find(issuer);
        if (clientRegistration != null) {
            Calendar timestamp = Calendar.getInstance();
            if (clientRegistration.getExpiration() == null
                    || timestamp.getTime().before(clientRegistration.getExpiration())) {
                validClientRegisration = clientRegistration;
            }
        }
        
        return validClientRegisration;
    }
    
    public ClientRegistration persist(ClientRegistration clientRegistration) {
        if (clientRegistration == null) {
            throw new NullPointerException("clientRegistration must not be null!");
        }
        
        ClientRegistration persistentClientRegistration;
        
        if (clientRegistration.getId() == null) {
            entityManager.persist(clientRegistration);
            persistentClientRegistration = clientRegistration;
        } else {
            throw new UnsupportedOperationException();
        }
        
        return persistentClientRegistration;
    }
    
    public void update(ClientRegistration clientRegistration) {
        if (clientRegistration == null) {
            throw new NullPointerException("clientRegistration must not be null!");
        }

        ClientRegistration existingClientRegistration = find(clientRegistration.getIssuer());
        if (existingClientRegistration != null) {
            existingClientRegistration.setClientIdentifier(clientRegistration.getClientIdentifier());
            existingClientRegistration.setClientSecret(clientRegistration.getClientSecret());
            existingClientRegistration.setExpiration(clientRegistration.getExpiration());
        } else {
            persist(clientRegistration);
        }
    }
}
