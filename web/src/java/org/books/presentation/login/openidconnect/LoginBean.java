package org.books.presentation.login.openidconnect;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import org.books.presentation.MessageFactory;
import org.books.presentation.login.data.User;
import org.books.presentation.login.openidconnect.data.ClientRegistration;
import org.books.presentation.login.openidconnect.data.ClientRegistrationRepository;
import org.books.presentation.login.openidconnect.data.Issuer;
import org.books.presentation.login.openidconnect.data.ProviderConfiguration;
import org.books.presentation.navigation.Navigation;

/**
 * OpenID Connect Login Steps:
 * 1. Initiate Authentication
 * 1.1 Provider Discovery (using Simple Web Discovery)
 *      Lookup instance of OpenID Connect Issuer Service Type
 *      -> Location to OpenID Connect Issuer Service
 *      Fetch Provider Configuration (using the issuer service)
 *      -> Provider Configuration
 * 1.2 Dynamic Client (= Service Provider) Registration
 *      -> Client Registration
 *          * Client Identifier
 *          * Client Secret
 * 1.3 Authorization
 *      Send Authorization Request (End-User Redirection to Provider)
 * 2. End-User Redirection to Client (Callback)
 * 3. Fetch ID Token and Access Token
 *      Send Token Request and process response
 * 3.1 Decode ID Token
 * 4. Verify ID Token
 * 5. Fetch Userinfo
 *      Send Userinfo Request and process response
 * 6. End-User Redirection to After-Login Page (of this application)
 * 
 * @author Ronny Stauffer
 */
@ManagedBean(name = LoginBean.NAME)
@SessionScoped
public class LoginBean {
    public static final String NAME = "login";
    
    private static final String GOOGLE_SHORTCUT = "google";
    private static final String GOOGLE_OPENID_CONNECT_IDENTIFIER_REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@gmail.com";
    private static final String EMAIL_ADDRESS_REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String URL_SCHEME_REGEX = "https?://";
    private static final String HTTPS_SCHEME = "https://";
    //private static final String FRAGMENT_REGEX = "#.*$";
    
    /**
     * Self-defined identifier
     */
    private static final String OPENID4US_SWD_HOST = "connect.openid4.us";
    private static final String OPENID4US_OPENID_CONNECT_ISSUER_SERVICE_LOCATION = "https://connect.openid4.us/abop";
    
    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String GOOGLE_AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/auth";
    private static final String GOOGLE_TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/token";
    private static final String GOOGLE_USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v1/userinfo";
    public static final String GOOGLE_DISCRIMINATOR = "google";
    public  static final ProviderConfiguration GOOGLE_PROVIDER_CONFIGURATION;
    private static final String GOOGLE_CLIENT_IDENTIFIER = "486945023370.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "vvPGiAWvm8s3qzRDHVt-5Z9G";
    
    private static final String OXAUTH_ISSUER = "http://localhost:8082/oxauth-server-0.3";

    private static final String WENOU_ISSUER = "https://wenou-test.wenoit.org";
    
    private static final String WEB_APPLICATION_TYPE = "web";
    
    /**
     * Code Authorization Response Type (for Code Flow (Basic Client Profile))
     */
    private static final String CODE_AUTHORIZATION_RESPONSE_TYPE = "code";
    
    /**
     * OpenID Authorization Scope (for OpenID Connect request)
     */
    private static final String OPENID_AUTHORIZATION_SCOPE = "openid";
    private static final String PROFILE_AUTHORIZATION_SCOPE = "profile";
    private static final String ADDRESS_AUTHORIZATION_SCOPE = "address";
    private static final String EMAIL_AUTHORIZATION_SCOPE = "email";
    
    /**
     * Authorization State Value
     */
    private static final String AUTHORIZATION_STATE_VALUE = "123";

    /**
     * Client Application Name
     */
    public static final String APPLICATION_NAME = "Bookstore";
    
    /**
     * Client Logo URL
     */
    public static final String LOGO_URL = "http://dl.dropbox.com/u/42443428/books.jpg";
    
    /**
     * Client Callback URI
     */
    public static final String CALLBACK_URI = "http://localhost:8080/bookstore/login/callback";
    
    public static final String PROVIDER_CONFIGURATION_LOGIN_CONTEXT_KEY = "openIdConnectProviderConfiguration";
    public static final String USER_LOGIN_CONTEXT_KEY = "user";
    public static final String LOGIN_MESSAGE_LOGIN_CONTEXT_KEY = "loginMessage";
    
    private static final Logger LOGGER = Logger.getLogger(LoginBean.class.getName());
    
    static {
        ProviderConfiguration providerConfiguration = new ProviderConfiguration();
        providerConfiguration.issuer = GOOGLE_ISSUER;
        providerConfiguration.authorization_endpoint = GOOGLE_AUTHORIZATION_ENDPOINT;
        providerConfiguration.token_endpoint = GOOGLE_TOKEN_ENDPOINT;
        providerConfiguration.userinfo_endpoint = GOOGLE_USERINFO_ENDPOINT;
        GOOGLE_PROVIDER_CONFIGURATION = providerConfiguration;
    }    
    
    @PersistenceContext(unitName = "openIDConnect")
    private EntityManager entityManager;
    
    @Resource
    UserTransaction transaction;
    
    @NotNull
    @Size(min = 1, max = 100)
    private String openIDConnectIdentifier;

    public String getOpenIDConnectIdentifier() {
        return openIDConnectIdentifier;
    }

    public void setOpenIDConnectIdentifier(String openIDConnectIdentifier) {
        this.openIDConnectIdentifier = openIDConnectIdentifier;
    }
    
    public String getLoginWithGoogleLink() {
        try {
        return GOOGLE_AUTHORIZATION_ENDPOINT + "?"
                + "response_type=" + CODE_AUTHORIZATION_RESPONSE_TYPE + "&"
                + "client_id=" + GOOGLE_CLIENT_IDENTIFIER + "&"
                + "redirect_uri=" + URLEncoder.encode(CALLBACK_URI, "utf-8") + "&"
                + "scope=" + OPENID_AUTHORIZATION_SCOPE + "+" + PROFILE_AUTHORIZATION_SCOPE + "&"
                + "state=" + GOOGLE_DISCRIMINATOR + AUTHORIZATION_STATE_VALUE + "&"
                + "display=popup";
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean getIsLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey(USER_LOGIN_CONTEXT_KEY);
    }
    
    public String getLoggedInLabel() {
        if (!getIsLoggedIn()) {
            throw new IllegalStateException("Nobody is logged in!");
        }
        
        String loggedInLabel;
        
        User user = (User)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(USER_LOGIN_CONTEXT_KEY);
        loggedInLabel = String.format("%s %s", user.getFirstName(), user.getLastName());
        
        return loggedInLabel;
    }
    
    public String getLoggedInUserPhotoURL() {
        if (!getIsLoggedIn()) {
            throw new IllegalStateException("Nobody is logged in!");
        }
        
        String loggedInUserPhotoURL;
        
        User user = (User)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(USER_LOGIN_CONTEXT_KEY);
        loggedInUserPhotoURL = user.getPhotoURL();
        
        return loggedInUserPhotoURL;        
    }
    
    public String login() {
        return Navigation.login();
    }
    
    public String logout() {
        //FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(USER_LOGIN_CONTEXT_KEY);
        
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.invalidateSession();
        
        String afterLogoutURL = externalContext.getRequestContextPath();
        
        LOGGER.info(String.format("After-Logout URL: %s", afterLogoutURL));
    
        try {
            externalContext.redirect(afterLogoutURL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
    
    public String getLoginMessage() {
        String loginMessage = null;
        
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        if (sessionMap.containsKey(LOGIN_MESSAGE_LOGIN_CONTEXT_KEY)) {
            loginMessage = (String)sessionMap.get(LOGIN_MESSAGE_LOGIN_CONTEXT_KEY);
            sessionMap.remove(LOGIN_MESSAGE_LOGIN_CONTEXT_KEY);
        }
        
        return loginMessage;
    }
        
    private class InvalidOpenIDConnectIdentifierException extends Exception {
        
    }
    
    public String initiateAuthentication() {
        assert openIDConnectIdentifier != null && openIDConnectIdentifier.length() > 1;

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCertificatesTrustManager = new TrustManager[] { new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            
            }
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            
            }
        }};
        
        // Install the all-trusting trust manager
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificatesTrustManager, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }       
                
        Client client = Client.create();
        
        ProviderConfiguration providerConfiguration = null;
        if (GOOGLE_SHORTCUT.equals(openIDConnectIdentifier) || openIDConnectIdentifier.matches(GOOGLE_OPENID_CONNECT_IDENTIFIER_REGEX)) {
//           providerConfiguration = new ProviderConfiguration();
//           providerConfiguration.issuer = GOOGLE_ISSUER;
//           providerConfiguration.authorization_endpoint = GOOGLE_AUTHORIZATION_ENDPOINT;
//           providerConfiguration.token_endpoint = GOOGLE_TOKEN_ENDPOINT;
//           providerConfiguration.userinfo_endpoint = GOOGLE_USERINFO_ENDPOINT;
             providerConfiguration = GOOGLE_PROVIDER_CONFIGURATION;
        } else {
            String swdPrincipal;
            String swdHost;
            String protocol = null;
            try {
                if (openIDConnectIdentifier.matches(EMAIL_ADDRESS_REGEX)) {
                    // E-Mail Address Identifier
                    String eMailAddress = openIDConnectIdentifier;

                    swdPrincipal = eMailAddress;

                    int atIndex = eMailAddress.indexOf("@");
                    swdHost = eMailAddress.substring(atIndex + 1);
                } else {
                    // URL Identifier
                    String _url = openIDConnectIdentifier;

                    Pattern schemePattern = Pattern.compile("^" + URL_SCHEME_REGEX);
                    Matcher schemeMatcher = schemePattern.matcher(_url);
                    if (!schemeMatcher.find()) {
                        _url = HTTPS_SCHEME + _url;
                    }

                    URL url;
                    try {
                        url = new URL(_url);
                    } catch (MalformedURLException e) {
                        throw new InvalidOpenIDConnectIdentifierException();
                    }

                    //TODO Remove possible fragment from URL

                    swdPrincipal = url.toString();

                    if (url.getHost() == null || url.getHost().isEmpty()) {
                        throw new InvalidOpenIDConnectIdentifierException();
                    }
                    String _swdHost = /* url.getProtocol() + "://" + */ url.getHost();
                    if (url.getPort() != -1) {
                        _swdHost += ":" + url.getPort();
                    }
                    swdHost = _swdHost;

                    protocol = url.getProtocol();
                }

                LOGGER.info(String.format("SWD Principal: %s", swdPrincipal));
                LOGGER.info(String.format("SWD Host: %s", swdHost));

                Issuer issuer;
                if (OPENID4US_SWD_HOST.equals(swdHost)) {
                    issuer = new Issuer();
                    List<String> locations = new ArrayList<String>();
                    locations.add(OPENID4US_OPENID_CONNECT_ISSUER_SERVICE_LOCATION);
                    issuer.locations = locations;
                } else {
                    try {
                        //TODO Check correct use of protocol
                        issuer = client.resource(protocol + "://" + swdHost + "/.well-known/simple-web-discovery")
                            .queryParam("principal", URLEncoder.encode(swdPrincipal, "utf-8"))
                            .queryParam("service", URLEncoder.encode("http://openid.net/specs/connect/1.0/issuer", "utf-8"))
                            .accept(MediaType.APPLICATION_JSON_TYPE).get(Issuer.class);

                        for (String issuerServiceLocation : issuer.locations) {
                            LOGGER.info(String.format("Issuer Service Location: %s", issuerServiceLocation));
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
                
                // Fetch provider configuration
                providerConfiguration = client.resource(issuer.locations.get(0) + "/.well-known/openid-configuration")
                        .accept(MediaType.APPLICATION_JSON_TYPE).get(ProviderConfiguration.class);
            } catch (InvalidOpenIDConnectIdentifierException e) {
                MessageFactory.info("org.books.Bookstore.INVALID_OPENID_CONNECT_IDENTIFIER");
            }
        }
        openIDConnectIdentifier = null;
        
        LOGGER.info(String.format("Provider Registration Endpoint: %s", providerConfiguration.registration_endpoint));
        LOGGER.info(String.format("Provider Authorization Endpoint: %s", providerConfiguration.authorization_endpoint));
        LOGGER.info(String.format("Provider Token Endpoint: %s", providerConfiguration.token_endpoint));
        LOGGER.info(String.format("Provider Userinfo Endpoint: %s", providerConfiguration.userinfo_endpoint));
        
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(PROVIDER_CONFIGURATION_LOGIN_CONTEXT_KEY, providerConfiguration);

        ClientRegistration clientRegistration = getClientRegistration(providerConfiguration);
           
        Calendar timestamp = Calendar.getInstance();
        timestamp.add(Calendar.HOUR_OF_DAY, 1);
            
        LOGGER.info(String.format("Timestamp for Client Registration expiration check: %s", timestamp.getTime()));
        
        if (clientRegistration == null
                || (clientRegistration.getExpiration() != null
                    && !timestamp.getTime().before(clientRegistration.getExpiration()))) {
            clientRegistration = performDynamicClientRegistration(providerConfiguration);
        }
        
        String authorizationRequestURL = null;
        try {
            WebResource authorizationResource = client.resource(providerConfiguration.authorization_endpoint)
                .queryParam("response_type", CODE_AUTHORIZATION_RESPONSE_TYPE)
                .queryParam("client_id", clientRegistration.getClientIdentifier())
                .queryParam("redirect_uri", URLEncoder.encode(CALLBACK_URI, "utf-8"))
                .queryParam("scope", OPENID_AUTHORIZATION_SCOPE + " " + PROFILE_AUTHORIZATION_SCOPE /* + " " + ADDRESS_AUTHORIZATION_SCOPE */ + " " + EMAIL_AUTHORIZATION_SCOPE)
                .queryParam("state", AUTHORIZATION_STATE_VALUE);
            if (WENOU_ISSUER.equals(providerConfiguration.issuer)) {
                authorizationResource = authorizationResource
                    .queryParam("nonce", "123"); // Required for Wenou altough optional according to the OpenID Connect specification
            }
            //if (OXAUTH_ISSUER.equals(providerConfiguration.issuer)) {
                //authorization = authorization
                //    .queryParam("request", "abc");
            //}
            authorizationRequestURL = authorizationResource
                .getURI().toURL().toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info(String.format("Authorization Request URL: %s", authorizationRequestURL));
        
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        try {
            externalContext.redirect(authorizationRequestURL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return null;        
    }
    
    /**
     * Gets a valid client registration for the specified provider, either by using hardcoded values or by doing a lookup in the registration store.
     * @param providerConfiguration
     * @return 
     */
    public ClientRegistration getClientRegistration(ProviderConfiguration providerConfiguration) {
        if (providerConfiguration == null) {
            throw new NullPointerException("providerConfiguration must not be null!");
        }
        
        ClientRegistration clientRegistration = null;
        
        if (GOOGLE_ISSUER.equals(providerConfiguration.issuer)) {
            clientRegistration = new ClientRegistrationRepository(entityManager).create(GOOGLE_ISSUER);
            clientRegistration.setClientIdentifier(GOOGLE_CLIENT_IDENTIFIER);
            clientRegistration.setClientSecret(GOOGLE_CLIENT_SECRET);
        } else {
//            try {
//                clientRegistration = em.createQuery("select clientRegistration from ClientRegistration clientRegistration where clientRegistration.issuer = :issuer", ClientRegistration.class)
//                    .setParameter("issuer", providerConfiguration.issuer)
//                    .getSingleResult();
//            } catch (NoResultException e) {
//                // Ignore exception
//            }
            clientRegistration = new ClientRegistrationRepository(entityManager).findValid(providerConfiguration.issuer);
        }
        
        return clientRegistration;
    }
    
    /**
     * Performs a dynamic client registration.
     * @return 
     */
    private ClientRegistration performDynamicClientRegistration(ProviderConfiguration providerConfiguration) {
        assert providerConfiguration != null;
        
        ClientRegistration clientRegistration = null;

        Client client = Client.create();
        try {
            clientRegistration = client.resource(providerConfiguration.registration_endpoint)
                //.queryParam("type", "client_associate")
                //.queryParam("application_name", "Bookstore") // Required for oxAuth altough optional according to the OpenID Connect specification
                //.queryParam("application_type", "web") // Required for oxAuth altough optional according to the OpenID Connect specification
                //.queryParam("redirect_uris", URLEncoder.encode(CALLBACK_URI, "utf-8"))
                //.queryParam("logo_url", URLEncoder.encode(LOGO_URL, "utf-8"))
                ////.queryParam("user_id_type", "pairwise")
                //.queryParam("token_endpoint_auth_type", "client_secret_basic")
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientRegistration.class,
                        "type=client_associate" + "&"
                        + "application_name=" + APPLICATION_NAME + "&" // Required for oxAuth altough optional according to the OpenID Connect specification
                        + "application_type=" + WEB_APPLICATION_TYPE + "&" // Required for oxAuth altough optional according to the OpenID Connect specification
                        + "redirect_uris=" + URLEncoder.encode(CALLBACK_URI, "utf-8") + "&"
                        + "logo_url=" + URLEncoder.encode(LOGO_URL, "utf-8") + "&"
                        //+ "user_id_type=pairwise" + "&"
                        + "token_endpoint_auth_type=client_secret_basic"
                    );
            
            if (clientRegistration.getExpiration() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(clientRegistration.getExpiration());
                if (calendar.get(Calendar.YEAR) > 9999) {
                    clientRegistration.setExpiration(null);
                }
            }

            LOGGER.info(String.format("Client Registration Client Identifier: %s", clientRegistration.getClientIdentifier()));
            LOGGER.info(String.format("Client Registration Client Secret: %s", clientRegistration.getClientSecret()));
            LOGGER.info(String.format("Client Registration Client Expiration: %s", clientRegistration.getExpiration() != null ? clientRegistration.getExpiration() : "<No expiration>"));
            
            clientRegistration.setIssuer(providerConfiguration.issuer);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        
        try {
            transaction.begin();
            
            new ClientRegistrationRepository(entityManager).update(clientRegistration);

            transaction.commit();
        } catch (Exception e) {
            try {
                transaction.rollback();
            } catch (Exception e2) {
                // Ignore exception
            }
            
            throw new RuntimeException(e);
        }        
        
        return clientRegistration;
    }
}