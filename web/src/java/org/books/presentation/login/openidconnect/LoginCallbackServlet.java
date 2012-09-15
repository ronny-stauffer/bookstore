/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.books.presentation.login.openidconnect;

import com.nimbusds.jwt.JWT;
import com.nimbusds.openid.connect.ParseException;
import com.nimbusds.openid.connect.SerializeException;
import com.nimbusds.openid.connect.claims.ClientID;
import com.nimbusds.openid.connect.claims.sets.IDTokenClaims;
import com.nimbusds.openid.connect.http.HTTPRequest;
import com.nimbusds.openid.connect.http.HTTPRequest.Method;
import com.nimbusds.openid.connect.messages.AccessToken;
import com.nimbusds.openid.connect.messages.AccessTokenRequest;
import com.nimbusds.openid.connect.messages.AccessTokenResponse;
import com.nimbusds.openid.connect.messages.AuthorizationCode;
import com.nimbusds.openid.connect.messages.AuthorizationResponse;
import com.nimbusds.openid.connect.messages.ClientAuthentication;
import com.nimbusds.openid.connect.messages.ClientSecretPost;
import com.sun.jersey.api.client.Client;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

/**
 *
 * @author ue56923
 */
public class LoginCallbackServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginBean.class.getName());    
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        response.setContentType("text/html;charset=UTF-8");
//        PrintWriter out = response.getWriter();
//        try {
//            /* TODO output your page here. You may use following sample code. */
//            out.println("<html>");
//            out.println("<head>");
//            out.println("<title>Servlet LoginCallbackServlet</title>");            
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<h1>Servlet LoginCallbackServlet at " + request.getContextPath() + "</h1>");
//            out.println("</body>");
//            out.println("</html>");
//        } finally {            
//            out.close();
//        }
        
        Client client = Client.create();
        
        String requestURL = request.getRequestURL().append("?").append(request.getQueryString()).toString();
        
        LOGGER.info(String.format("Authorization Callback URL: %s", requestURL));
        
        URL authorizationCallbackURL = new URL(requestURL);        
        
        AuthorizationCode authorizationCode;
        try {
            AuthorizationResponse authorizationResponse = AuthorizationResponse.parse(authorizationCallbackURL);
            authorizationCode = authorizationResponse.getAuthorizationCode();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } 
        
        LOGGER.info(String.format("Authorization Code: %s", authorizationCode.getValue()));
        
        HttpSession session = request.getSession();
        
        LOGGER.info("HTTP Session Attributes:");
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            LOGGER.info(String.format("\tAttribute Name: %s", attributeName));
        }
        
        ProviderConfiguration providerConfiguration = getFromSession(request, LoginBean.PROVIDER_CONFIGURATION_SESSION_ATTRIBUTE_NAME);
        
        LoginBean loginBean = getFromSession(request, LoginBean.NAME);
        ClientRegistration clientRegistration = loginBean.getClientRegistration(providerConfiguration);
        ClientID clientIdentifier = new ClientID();
        clientIdentifier.setClaimValue(clientRegistration.getClientIdentifier());
        //TODO Choose right authentication option
        ClientAuthentication clientAuthenticaion = //new ClientSecretBasic(clientIdentifier, clientRegistration.getClientSecret());
                                                     new ClientSecretPost(clientIdentifier, clientRegistration.getClientSecret());
        
        AccessTokenRequest tokenRequest = new AccessTokenRequest(authorizationCode, new URL(LoginBean.CALLBACK_URI), clientAuthenticaion);

        HTTPRequest tokenHTTPRequest;
        try {
            tokenHTTPRequest = tokenRequest.toHTTPRequest();
        } catch (SerializeException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info(String.format("Token HTTP Request Method: %s", tokenHTTPRequest.getMethod()));
        LOGGER.info(String.format("Token HTTP Request Authorization: %s", tokenHTTPRequest.getAuthorization()));
        LOGGER.info(String.format("Token HTTP Request Query: %s", tokenHTTPRequest.getQuery()));
        
        assert Method.POST.equals(tokenHTTPRequest.getMethod());
        
        String tokenHTTPResponseBody = client.resource(providerConfiguration.token_endpoint)
            .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .header("Authorization:", tokenHTTPRequest.getAuthorization())
            .post(String.class, tokenHTTPRequest.getQuery());
        
        LOGGER.info(String.format("Token HTTP Response Body: %s", tokenHTTPResponseBody));
        
//        HTTPResponse tokenHTTPResponse;
//        try {
//            tokenHTTPResponse = new HTTPResponse(200);
//            tokenHTTPResponse.setContentType(new ContentType(MediaType.APPLICATION_JSON)); // We use the application/json type definition from JAX-RS
//            tokenHTTPResponse.setContent(tokenHTTPResponseBody);
//        } catch (javax.mail.internet.ParseException e) {
//            throw new RuntimeException(e);
//        }

        JSONObject tokensJSONObject;
        try {
            tokensJSONObject = (JSONObject)JSONValue.parseStrict(tokenHTTPResponseBody);
        } catch (net.minidev.json.parser.ParseException e) {
            throw new RuntimeException(e);
        }
        
        //tokensJSONObject.put("nonce", "123");
        
        //LOGGER.info(String.format("Token JSON Object: %s", tokensJSONObject.toJSONString()));
        
        AccessToken accessToken;
        JWT idJWTToken;
        try {
            AccessTokenResponse tokenResponse = AccessTokenResponse.parse(/* tokenHTTPResponse */ tokensJSONObject);
            accessToken = tokenResponse.getAccessToken();
            idJWTToken = tokenResponse.getIDToken();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        
        JSONObject idTokenJSONObject = idJWTToken.getClaimsSet().toJSONObject();
        
//        List<String> keysToRemove = new ArrayList<String>();
//        for (String key : idTokenJSONObject.keySet()) {
//            if (!("iss".equals(key)
//                    || "user_id".equals(key)
//                    || "aud".equals(key)
//                    || "iat".equals(key)
//                    /* || "exp".equals(key) */)) { // Not allowd by NimbusDS OpenID Connect SDK altough required by OpenID Connect Specification
//                keysToRemove.add(key);
//            }
//        }
//        for (String key : keysToRemove) {
//            idTokenJSONObject.remove(key);
//        }
//        idTokenJSONObject.put("nonce", "123"); // Required by NimbusDS OpenID Connect SDK altough not required by OpenID Connect Specification
        
        JSONObject idTokenJSONObject2 = new JSONObject();
        idTokenJSONObject2.put("iss", idTokenJSONObject.get("iss"));
        idTokenJSONObject2.put("user_id", idTokenJSONObject.get("user_id"));
        idTokenJSONObject2.put("aud", idTokenJSONObject.get("aud"));
        idTokenJSONObject2.put("iat", idTokenJSONObject.get("iat"));
        //idTokenJSONObject2.put("exp", idTokenJSONObject.get("exp")); // Not allowd by NimbusDS OpenID Connect SDK altough required by OpenID Connect Specification
        idTokenJSONObject2.put("nonce", "123");  // Required by NimbusDS OpenID Connect SDK altough not required by OpenID Connect Specification
        
        LOGGER.info(String.format("ID Token JSON Object: %s", idTokenJSONObject2.toJSONString()));
        
        LOGGER.info("ID Token Reserved Keys:");
        for (String key : IDTokenClaims.getReservedClaimNames()) {
            LOGGER.info(String.format("\tKey: %s", key));
        }
        
        IDTokenClaims idToken;
        try {
            idToken = IDTokenClaims.parse(idTokenJSONObject2);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        
        LOGGER.info(String.format("ID Token Issuer: %s", idToken.getIssuer().getClaimValue()));
        LOGGER.info(String.format("ID Token User Identifier: %s", idToken.getUserID().getClaimValue()));
        LOGGER.info(String.format("ID Token Audience: %s", idToken.getAudience().getClaimValue()));
        LOGGER.info(String.format("ID Token Issue Time: %s", idToken.getIssueTime().getClaimValueAsDate()));
        LOGGER.info(String.format("ID Token Authentication Time: %s", idToken.getAuthenticationTime().getClaimValueAsDate()));
        
        LOGGER.info("Proceeding...");
    }
    
    private <T> T getFromSession(HttpServletRequest request, String key) {
        assert key != null && !key.isEmpty();
        
        HttpSession session = request.getSession();
        
        T value = (T)session.getAttribute(key);
        if (value == null) {
            throw new IllegalStateException(String.format("'%s' is undefined! Probably, this request had occured outside of a login procedure?!", key));
        }
        
        return value;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
