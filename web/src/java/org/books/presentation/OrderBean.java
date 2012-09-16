package org.books.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.books.business.OrderManagerLocal;
import org.books.common.data.Address;
import org.books.common.data.CreditCard;
import org.books.common.data.Order;
import org.books.common.exception.CreditCardExpiredException;
import org.books.common.exception.MissingLineItemsException;
import org.books.presentation.login.data.User;
import org.books.presentation.login.openidconnect.LoginBean;
import org.books.presentation.navigation.Navigation;

/**
 * @author Christoph Horber
 */
@ManagedBean(name = "orderBean")
@SessionScoped
public class OrderBean {
    public static final String MISSING_LINE_ITEMS = "org.books.Bookstore.MISSING_LINE_ITEMS";   
    public static final String EXPIRED_CREDIT_CARD = "org.books.Bookstore.EXPIRED_CREDIT_CARD";

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
