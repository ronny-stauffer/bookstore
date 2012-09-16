package org.books.presentation;

import com.nimbusds.openid.connect.messages.AccessToken;
import com.sun.jersey.api.client.Client;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.ws.rs.core.MediaType;
import org.books.business.OrderManagerLocal;
import org.books.common.data.Address;
import org.books.common.data.CreditCard;
import org.books.common.data.LineItem;
import org.books.common.data.Order;
import org.books.common.exception.CreditCardExpiredException;
import org.books.common.exception.MissingLineItemsException;
import org.books.presentation.login.data.User;
import org.books.presentation.login.openidconnect.GoogleCalendarEvent;
import org.books.presentation.login.openidconnect.LoginBean;
import org.books.presentation.login.openidconnect.data.ProviderConfiguration;
import org.books.presentation.navigation.Navigation;

/**
 * @author Christoph Horber
 */
@ManagedBean(name = "orderBean")
@SessionScoped
public class OrderBean {
    public static final String MISSING_LINE_ITEMS = "org.books.Bookstore.MISSING_LINE_ITEMS";   
    public static final String EXPIRED_CREDIT_CARD = "org.books.Bookstore.EXPIRED_CREDIT_CARD";

    private static final Logger LOGGER = Logger.getLogger(OrderBean.class.getName());    
    
    private final List<SelectItem> cardTypes;
    private final CreditCard creditCard;
    private final Address address;
    private List<SelectItem> countries;
    private Order order;
    
    @ManagedProperty(value = "#{shoppingCartBean}")
    private ShoppingCartBean shoppingCart;
    
    @EJB
    private OrderManagerLocal orderManager;
    
    /** Creates a new instance of OrderBean */
    public OrderBean() {
        cardTypes = initCardTypes();

        address = new Address();
        creditCard = new CreditCard();
    }

    public String order() {
        return Navigation.Order.order();
    }

    public String creditCardInput() {
        return Navigation.Order.creditCard();
    }
    
    public String summary() {
        return Navigation.Order.summary();
    }

    public String changeAddress() {
        return Navigation.Order.changeAddress();
    }
    
    public String changeCreditCard() {
        return Navigation.Order.changeCreditCard();
    }
    
    public String submit() {
        try {
            orderManager.addAddress(new Address(address));
            orderManager.addCreditCard(new CreditCard(creditCard));
            order = orderManager.orderBooks(shoppingCart.getLineItems());

            Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            //TODO Determine Identity Provider by user logged in
            if (session.containsKey(LoginBean.PROVIDER_CONFIGURATION_LOGIN_CONTEXT_KEY)) {
                ProviderConfiguration providerConfiguration = (ProviderConfiguration)session.get(LoginBean.PROVIDER_CONFIGURATION_LOGIN_CONTEXT_KEY);
                if (LoginBean.GOOGLE_ISSUER.equals(providerConfiguration.issuer)) {
                    if (session.containsKey(LoginBean.ACCESS_TOKEN_LOGIN_CONTEXT_KEY)) {
                        AccessToken accessToken = (AccessToken)session.get(LoginBean.ACCESS_TOKEN_LOGIN_CONTEXT_KEY);

                        LOGGER.info("Create Google Calendar event...");
                        
                        Date timestamp = Calendar.getInstance().getTime();
                        
                        GoogleCalendarEvent calendarEvent = new GoogleCalendarEvent();
                        calendarEvent.summary = "Books ordered!";
                        calendarEvent.description = getDescription(order);
                        calendarEvent.start.dateTime = /* "2012-09-16T23:00:00.000" */ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(timestamp);
                        calendarEvent.end.dateTime = calendarEvent.start.dateTime;
                        calendarEvent.location = LoginBean.APPLICATION_NAME;

                        Client client = Client.create();
                        client.resource("https://www.googleapis.com/calendar/v3/calendars/primary/events")
                            .queryParam("key", "AIzaSyBvqvbkMo9dUnUWQpa6zPKmZfVgRuiWxTE")
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", accessToken.toAuthorizationHeader())
                            .entity(calendarEvent)
                            .post();
                        
                        LOGGER.info("...done.");
                    }
                }
            }
        } catch (MissingLineItemsException e) {
            MessageFactory.error(MISSING_LINE_ITEMS);
            
            return null;
        } catch (CreditCardExpiredException e) {
            MessageFactory.error(EXPIRED_CREDIT_CARD);
            
            return null;
        }
        
        shoppingCart.clear();
        
        return Navigation.Order.submit();
    }
    
    private String getDescription(Order order) {
        assert order != null;
        
        StringBuilder description = new StringBuilder();
        description.append(String.format("Order Number: %s\n", order.getNumber()));
        description.append("\n");
        description.append("Books:\n");
        for (LineItem item : order.getItems()) {
            description.append(String.format("\t%s\n", item.getBook().getTitle()));
        }
        
        return description.toString();
    }
    
    public String close() {
        return Navigation.Order.close();
    }

    /**
     * @return the cardTypes
     */
    public List<SelectItem> getCardTypes() {
        return cardTypes;
    }

    /**
     * @return the creditCard
     */
    public CreditCard getCreditCard() {
        return creditCard;
    }

    /**
     * @return the address
     */
    public Address getAddress() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        if (sessionMap.containsKey(LoginBean.USER_LOGIN_CONTEXT_KEY)) {
            User user = (User)sessionMap.get(LoginBean.USER_LOGIN_CONTEXT_KEY);
            if (address.getName() == null || address.getName().isEmpty()) {
                address.setName(String.format("%s %s", user.getFirstName(), user.getLastName()));
            }
            if (user.getEMailAddress() != null) {
                if (address.geteMailAddress() == null || address.geteMailAddress().isEmpty())
                address.seteMailAddress(user.getEMailAddress());
            }
        }        
        
        return address;
    }

    /**
     * @return the countries
     */
    public List<SelectItem> getCountries() {
        countries = new ArrayList<SelectItem>();

        countries.add(new SelectItem(null));
        
        for (Country country : Country.values()) {
            String countryText = TextFactory.getResourceText("texts", "country." + country);
            countries.add(new SelectItem(country, countryText));
        }
        
        Collections.sort(countries, selectItemComparator);
        
        return countries;
    }

    /**
     * @return the order
     */
    public Order getOrder() {
        return order;
    }

    public String getOrderStatus() {
        String orderStatusText;
        
        Order.Status orderStatus = Order.Status.open;
        if (order != null) {
            orderStatus = order.getStatus();
        }
        
        orderStatusText = TextFactory.getResourceText("texts", "orderStatus." + orderStatus);
        
        return orderStatusText;
    }
        
    /**
     * @param shoppingCart the shoppingCart to set
     */
    public void setShoppingCart(ShoppingCartBean shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    private List<SelectItem> initCardTypes() {
        List<SelectItem> types = new ArrayList<SelectItem>();
        types.add(new SelectItem(null));
        for (CreditCard.Type type : CreditCard.Type.values()) {
            types.add(new SelectItem(type));
        }
        return types;
    }
    
    private static SelectItemComparator selectItemComparator = new SelectItemComparator();
    
    private static class SelectItemComparator implements Comparator<SelectItem> {

        @Override
        public int compare(SelectItem t, SelectItem t1) {
            if (t.getLabel() == null) {
                return -1;
            }
            if (t1.getLabel() == null) {
                return 1;
            }
            return t.getLabel().compareTo(t1.getLabel());
        }
    }
}
