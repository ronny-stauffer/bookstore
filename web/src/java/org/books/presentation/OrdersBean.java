package org.books.presentation;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.books.business.OrderAdministratorLocal;
import org.books.common.data.Order;
import org.books.common.exception.InvalidTimePeriodException;
import org.books.common.exception.OrderNotCancelableException;
import org.books.common.exception.OrderNotFoundException;
import org.books.presentation.navigation.Navigation;

/**
 *
 * @author Ronny Stauffer
 */
@ManagedBean(name = "ordersBean")
@SessionScoped
public class OrdersBean {
    
    private static final String INVALID_TIME_PERIOD = "org.books.Bookstore.INVALID_TIME_PERIOD";
    
    private static final String ORDER_NOT_FOUND = "org.books.Bookstore.ORDER_NOT_FOUND";
    
    private static final String ORDER_NOT_CANCELABLE = "org.books.Bookstore.ORDER_NOT_CANCELABLE";
    
    private Date fromDate = Calendar.getInstance().getTime();
    private Date toDate = Calendar.getInstance().getTime();
    
    List<Order> orders = Collections.emptyList();

    private Order selectedOrder;
    
    @EJB
    private OrderAdministratorLocal orderAdministrator;
    
    /**
     * @return the fromDate
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate the fromDate to set
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * @return the toDate
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * @param toDate the toDate to set
     */
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
    
    public String searchOrders() {

        try {
            orders = orderAdministrator.searchOrders(getBeginningOfDay(fromDate), getEndOfDay(toDate));
        } catch (InvalidTimePeriodException e) {
            
            orders = Collections.emptyList();
            
            MessageFactory.error(INVALID_TIME_PERIOD);
            
            return Navigation.Orders.invalidSearch();
        }
        
        return Navigation.Orders.search();
    }

    /**
     * Returns the beginning of a 24-hour period.
     * @param date
     * @return 
     */
    private Date getBeginningOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);            
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    /**
     * Returns the end of a 24-hour period.
     * @param date
     * @return 
     */
    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);            
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
    
    public boolean hasOrders() {
        return !orders.isEmpty();
    }

    /**
     * @return the lineItems
     */
    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public String getOrderStatus(Order order) {
        String orderStatusText = TextFactory.getResourceText("texts", "orderStatus." + order.getStatus());
        
        return orderStatusText;
    }
    
    public String selectOrder(Order selectedOrder) {
        this.selectedOrder = selectedOrder;
        return Navigation.Orders.searchOrders(selectedOrder);
    }
    
    public String cancelOrder(Order order) {
        String orderNumber = order.getNumber();
        
        try {
            //bookstore().cancelOrder(orderNumber);
            orderAdministrator.cancelOrder(orderNumber);
        } catch (OrderNotFoundException ex) {
            
            Logger.getLogger(OrdersBean.class.getName()).log(Level.SEVERE, null, ex);
            MessageFactory.error(ORDER_NOT_FOUND, orderNumber);
            
        } catch (OrderNotCancelableException ex) {
            
            Logger.getLogger(OrdersBean.class.getName()).log(Level.SEVERE, null, ex);
            MessageFactory.error(ORDER_NOT_CANCELABLE, orderNumber);
        }
        
        return searchOrders();
    }
    
    public String closeOrderList() {
        return Navigation.Orders.closeList();
    }
    
    /**
     * @return the selectedOrder
     */
    public Order getSelectedOrder() {
        return selectedOrder;
    }
    
    public String closeOrderDetails() {
        return Navigation.Orders.closeDetails();
    }
    
//    private static Bookstore bookstore() {
//        return Bookstore.getInstance();
//    }
}
