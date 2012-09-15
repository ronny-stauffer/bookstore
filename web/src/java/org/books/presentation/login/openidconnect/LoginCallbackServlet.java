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
import com.nimbusds.openid.connect.claims.sets.UserInfoClaims;
import com.nimbusds.openid.connect.http.HTTPRequest;
import com.nimbusds.openid.connect.http.HTTPRequest.Method;
import com.nimbusds.openid.connect.http.HTTPResponse;
import com.nimbusds.openid.connect.messages.AccessToken;
import com.nimbusds.openid.connect.messages.AccessTokenRequest;
import com.nimbusds.openid.connect.messages.AccessTokenResponse;
import com.nimbusds.openid.connect.messages.AuthorizationCode;
import com.nimbusds.openid.connect.messages.AuthorizationErrorResponse;
import com.nimbusds.openid.connect.messages.AuthorizationResponse;
import com.nimbusds.openid.connect.messages.ClientAuthentication;
import com.nimbusds.openid.connect.messages.ClientSecretPost;
import com.nimbusds.openid.connect.messages.ErrorCode;
import com.nimbusds.openid.connect.messages.Request;
import com.nimbusds.openid.connect.messages.UserInfoRequest;
import com.nimbusds.openid.connect.messages.UserInfoResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.mail.internet.ContentType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.books.presentation.login.User;

/**
 *
 * @author ue56923
 */
public class LoginCallbackServlet extends HttpServlet {
    public static class LoginError extends Exception {
        public enum Error {
            authorizationDenied("The authorization is denied by the user!"),
            authorizationInvalid("The authorization is invalid!"),
            userNameUnknown("The user name is unknown!");
            
            private final String message;
            
            Error(String message) {
                assert message != null && !message.isEmpty();
                
                this.message = message;
            }
            
            String getMessage() {
                return message;
            }
        }
        
        public LoginError(Error error) {
            super(error.getMessage());
        }
    }
    
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
        try {
            proceedWithLogin(request);
        } catch (LoginError e) {
            LOGGER.info(String.format("Login Error: %s", e.getMessage()));
            
            FacesMessage errorMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), "");
            //FacesContext facesContext = FacesContext.getCurrentInstance(); // Doesn't work!
            //.addMessage(null, errorMessage);
            // Workaround
            addToLoginContext(request, LoginBean.LOGIN_MESSAGE_LOGIN_CONTEXT_KEY, errorMessage.getSummary());
        }

        String returnURL = getServletContext().getContextPath(); //getRealPath("/");
        
        LOGGER.info(String.format("Return URL: %s", returnURL));
        
        response.sendRedirect(returnURL);
    }
    
    private void proceedWithLogin(HttpServletRequest request) throws LoginError, IOException {
        Client client = Client.create();
        
        String requestURL = request.getRequestURL().append("?").append(request.getQueryString()).toString();
        
        LOGGER.info(String.format("Authorization Callback URL: %s", requestURL));
        
        URL authorizationCallbackURL = new URL(requestURL);        
        
        try {
            AuthorizationErrorResponse authorizationErrorResponse = AuthorizationErrorResponse.parse(authorizationCallbackURL);
            if (ErrorCode.ACCESS_DENIED.equals(authorizationErrorResponse.getErrorCode())) {
                throw new LoginError(LoginError.Error.authorizationDenied);
            } else {
                throw new RuntimeException(String.format("Error from Provider: %s", authorizationErrorResponse.getErrorCode()));
            }
        } catch (ParseException e) {
            // Ignore exception
            
            int dummy = 0;
        }
        
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
        
        ProviderConfiguration providerConfiguration = getFromLoginContext(request, LoginBean.PROVIDER_CONFIGURATION_LOGIN_CONTEXT_KEY);
        
        LoginBean loginBean = getFromLoginContext(request, LoginBean.NAME);
        ClientRegistration clientRegistration = loginBean.getClientRegistration(providerConfiguration);
        ClientID clientIdentifier = new ClientID();
        clientIdentifier.setClaimValue(clientRegistration.getClientIdentifier());
        //TODO Choose right authentication option
        ClientAuthentication clientAuthentication = //new ClientSecretBasic(clientIdentifier, clientRegistration.getClientSecret());
                                                     new ClientSecretPost(clientIdentifier, clientRegistration.getClientSecret());
        
        AccessTokenRequest tokenRequest = new AccessTokenRequest(authorizationCode, new URL(LoginBean.CALLBACK_URI), clientAuthentication);

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
            .header("Authorization", tokenHTTPRequest.getAuthorization())
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
        idTokenJSONObject2.put("exp", idTokenJSONObject.get("exp")); // Not allowd by original NimbusDS OpenID Connect SDK altough required by OpenID Connect Specification
        idTokenJSONObject2.put("nonce", "123"); // Required by NimbusDS OpenID Connect SDK altough not required by OpenID Connect Specification
        
        LOGGER.info(String.format("ID Token JSON Object: %s", idTokenJSONObject2.toJSONString()));
        
//        LOGGER.info("ID Token Reserved Keys:");
//        for (String key : IDTokenClaims.getReservedClaimNames()) {
//            LOGGER.info(String.format("\tKey: %s", key));
//        }
        
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
        LOGGER.info(String.format("ID Token Expiriation Time: %s", idToken.getExpirationTime().getClaimValueAsDate()));
        
        if (clientRegistration.getClientIdentifier().equals(idToken.getAudience().getClaimValue())
                && Calendar.getInstance().getTime().before(idToken.getExpirationTime().getClaimValueAsDate())) {
            LOGGER.info("ID Token is valid.");
            LOGGER.info("User has successfully logged in!");
            
//            User testUser = User
//                    .firstName("John")
//                    .lastName("Doe")
//                    .eMailAddress("john.doe@example.com")
//                    .build();
//            addToLoginContext(request, LoginBean.USER_LOGIN_CONTEXT_KEY, testUser);
            
            UserInfoRequest userInfoRequest = new UserInfoRequest(/* Method.POST, */ accessToken);
            Result<UserInfoResponse> result = performRequest("Userinfo", userInfoRequest, providerConfiguration.userinfo_endpoint, UserInfoResponse.class, new JSONTransformer() {
                        @Override
                        public JSONObject transform(JSONObject jsonObject) {
                            // Map Google's id attribute to the required user_id attribute 
                            if (jsonObject.containsKey("id")) {
                                jsonObject.put("user_id", jsonObject.get("id"));
                                jsonObject.remove("id");
                            }
                            
                            // Remove empty attributes
                            List<String> keysToRemove = new ArrayList<String>();
                            for (String key : jsonObject.keySet()) {
                                Object value = jsonObject.get(key);
                                if (value instanceof String
                                        && ((String)value).isEmpty()) {
                                    keysToRemove.add(key);
                                }
                            }
                            for (String key : keysToRemove) {
                                jsonObject.remove(key);
                            }                            
                            
                            return jsonObject;
                        }
                    });
            if (result.isOK()) {
                UserInfoResponse userInfoResponse = result.getResponse();
                UserInfoClaims userInfo = userInfoResponse.getUserInfoClaims();
                
                if (userInfo.getGivenName() == null) {
                    throw new LoginError(LoginError.Error.userNameUnknown);
                }
                String givenName = userInfo.getGivenName().getClaimValue();
                if (userInfo.getFamilyName() == null) {
                    throw new LoginError(LoginError.Error.userNameUnknown);
                }
                String familyName = userInfo.getFamilyName().getClaimValue();
                String eMail = null;
                if (userInfo.getEmail() != null) {
                    eMail = userInfo.getEmail().getClaimValue().getAddress();
                }
                
                LOGGER.info(String.format("Userinfo Given Name: %s", givenName));
                LOGGER.info(String.format("Userinfo Familiy Name: %s", familyName));
                LOGGER.info(String.format("Userinfo E-Mail: %s", eMail != null ? eMail : "<Unknown>"));
                
                User user = User
                    .firstName(givenName)
                    .lastName(familyName)
                    .eMailAddress(eMail)
                    .build();

                addToLoginContext(request, LoginBean.USER_LOGIN_CONTEXT_KEY, user);
            } else {
                throw new RuntimeException(result.getError());
            }
        } else {
            LOGGER.info("ID Token is invalid!");
            
            throw new LoginError(LoginError.Error.authorizationInvalid);
        }
    }
    
    private <T> T getFromLoginContext(HttpServletRequest request, String key) {
        assert key != null && !key.isEmpty();
        
        HttpSession session = request.getSession();
        T value = (T)session.getAttribute(key);
        if (value == null) {
            throw new IllegalStateException(String.format("'%s' is undefined! Probably, this request had occured outside of a login procedure?!", key));
        }
        
        return value;
    }
    
    private void addToLoginContext(HttpServletRequest request, String key, Object value) {
        assert key != null && !key.isEmpty();
        assert value != null;
        
        HttpSession session = request.getSession();
        session.setAttribute(key, value);
    }

    private static class Result<T> {
        public enum Status {
            NOK,
            OK
        }
        
        private Status status = Status.NOK;
        private T response;
        protected Exception error;
        
        protected Result() {
            
        }
        
        public static <T> Result<T> create(T response) {
            Result<T> result = new Result<T>();
            result.status = Status.OK;
            result.response = response;
            
            return result;
        }
        
        public boolean isOK() {
            return Status.OK.equals(status);
        }
        
        public T getResponse() {
            if (!Status.OK.equals(status)) {
                throw new IllegalStateException("Result is NOK!");
            }
            
            return response;
        }
        
        public Exception getError() {
            if (error == null) {
                throw new IllegalStateException("There is no error!");
            }
            
            return error;
        }
    }
    
    private static class NOKResult<T> extends Result<T> {
        private NOKResult() {
            
        }
        
        public static <T> NOKResult<T> create(Exception error) {
            if (error == null) {
                throw new NullPointerException("error must not be null!");
            }
            
            NOKResult<T> nokResult = new NOKResult<T>();
            nokResult.error = error;
            
            return nokResult;
        }
    }
    
    private interface JSONTransformer {
        public JSONObject transform(JSONObject jsonObject);
    }

    private <T> Result<T> performRequest(String requestName, Request request, String endpoint, Class<T> responseType) {
        return performRequest(requestName, request, endpoint, responseType, null);
    }
    
    private <T> Result<T> performRequest(String requestName, Request request, String endpoint, Class<T> responseType, JSONTransformer jsonTransformer) {
        assert requestName != null && !requestName.isEmpty();
        assert request != null;
        assert endpoint != null && !endpoint.isEmpty();
        
        try {
        HTTPRequest httpRequest;
        try {
            httpRequest = request.toHTTPRequest();
        } catch (SerializeException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info(String.format("%s HTTP Request Method: %s", requestName, httpRequest.getMethod()));
        LOGGER.info(String.format("%s HTTP Request Authorization: %s", requestName, httpRequest.getAuthorization() != null ? httpRequest.getAuthorization() : "<No authorization>"));
        LOGGER.info(String.format("%s HTTP Request Query: %s", requestName, httpRequest.getQuery() != null ? httpRequest.getQuery() : "<No query>"));
        
        Client client = Client.create();
        if (Method.GET.equals(httpRequest.getMethod())) {
            endpoint += "?" + httpRequest.getQuery();
        }
        WebResource resource = client.resource(endpoint);
        Builder builder = resource
            .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE);
        if (httpRequest.getAuthorization() != null) {
            builder = builder
                .header("Authorization", httpRequest.getAuthorization());
        }
        String httpResponseBody;
        switch (httpRequest.getMethod()) {
            case GET:
                httpResponseBody = builder.get(String.class);
                
                break;
            case POST:
                httpResponseBody = builder.post(String.class, httpRequest.getQuery());
                
                break;
            default:
                throw new UnsupportedOperationException(String.format("HTTP request method '%s' is not supported!", httpRequest.getMethod()));
        }
        
        LOGGER.info(String.format("%s HTTP Response Body: %s", requestName, httpResponseBody));
        
        JSONObject jsonObject;
        try {
            jsonObject = (JSONObject)JSONValue.parseStrict(httpResponseBody);
        } catch (net.minidev.json.parser.ParseException e) {
            throw new RuntimeException(e);
        }        
        
        JSONObject transformedJSONObject = jsonObject;        
        if (jsonTransformer != null) {
            transformedJSONObject = jsonTransformer.transform(jsonObject);
        }
        
        HTTPResponse httpResponse;
        try {
            httpResponse = new HTTPResponse(200);
            httpResponse.setContentType(new ContentType(MediaType.APPLICATION_JSON)); // We use the application/json type definition from JAX-RS
            httpResponse.setContent(/* httpResponseBody */ transformedJSONObject.toJSONString());
        } catch (javax.mail.internet.ParseException e) {
            throw new RuntimeException(e);
        }
        
        T response = null;
        try {
            java.lang.reflect.Method parseMethod = responseType.getDeclaredMethod("parse", HTTPResponse.class);
            response = (T)parseMethod.invoke(null, httpResponse);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Response type '%s' doesn't support HTTP response parsing!", responseType.getName()));
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw new RuntimeException(e.getCause());
            }
            
            throw new RuntimeException("Unknown error while parsing HTTP response!", e);
        }
        
        return Result.create(response);
        } catch (Exception e) {
            return NOKResult.create(e);
        }
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
