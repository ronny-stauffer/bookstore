package org.books.business;

import java.util.Date;
import java.util.List;
import org.books.common.data.Order;
import org.books.common.exception.InvalidTimePeriodException;
import org.books.common.exception.OrderNotCancelableException;
import org.books.common.exception.OrderNotFoundException;

/**
 *
 * @author staufferr
 */
public interface OrderAdministrator {
    List<Order> searchOrders(Date fromDate, Date toDate) throws InvalidTimePeriodException;
    
    void cancelOrder(String number) throws OrderNotFoundException, OrderNotCancelableException;
}
