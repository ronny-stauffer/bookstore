package org.books.business;

import java.util.Date;
import java.util.List;
import org.books.common.data.Address;
import org.books.common.data.CreditCard;
import org.books.common.data.LineItem;
import org.books.common.data.Order;
import org.books.common.exception.CreditCardExpiredException;
import org.books.common.exception.InvalidTimePeriodException;
import org.books.common.exception.MissingDataException;
import org.books.common.exception.MissingLineItemsException;
import org.books.common.exception.OrderNotCancelableException;
import org.books.common.exception.OrderNotFoundException;

/**
 *
 * @author Christoph Horber
 */
public interface OrderManager {
    
    Address addAddress(Address address);
    
    CreditCard addCreditCard(CreditCard creditCard);
    
    Order orderBooks(List<LineItem> items) throws CreditCardExpiredException, MissingLineItemsException, MissingDataException;
}
