package org.books.business;

import java.math.BigDecimal;
import java.util.*;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.jms.*;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import org.books.common.data.Address;
import org.books.common.data.Book;
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
@Stateful(name = "OrderManager")
public class OrderManagerBean implements OrderManagerRemote, OrderManagerLocal {
    @Resource(lookup = "jms/bookstoreConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(lookup = "jms/bookstoreOrderQueue")
    private Queue bookOrderQueue;
    
    private Address address;
    
    private CreditCard creditCard;
    
    @PersistenceContext(unitName = "bookstore")
    private EntityManager em;
    
    @Override
    public Address addAddress(Address address) {
        em.persist(address);
        this.address = address;
        return address;
    }

    @Override
    public CreditCard addCreditCard(CreditCard creditCard) {
        em.persist(creditCard);
        this.creditCard = creditCard;
        return creditCard;
    }
    
    @Override
    public Order orderBooks(List<LineItem> items) throws CreditCardExpiredException, MissingLineItemsException {
        
        if (address == null || creditCard == null) {
            throw new MissingDataException();
        }
        
        if (items.isEmpty()) {
            throw new MissingLineItemsException();
        }
        
        validate(creditCard);
        BigDecimal amount = BigDecimal.ZERO;
        for (LineItem item : items) {
            TypedQuery findBookQuery = em.createNamedQuery("findBook", Book.class);
            findBookQuery.setParameter("isbn", item.getBook().getIsbn());
            List<Book> books = findBookQuery.getResultList();
            if (!books.isEmpty()) {
                item.setBook(books.get(0));
            } else {
                em.persist(item.getBook());
            }
            
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            amount = amount.add(item.getBook().getPrice().multiply(quantity));
        }
        Order order = new Order();
        order.setNumber(UUID.randomUUID().toString());
        order.setDate(new Date(System.currentTimeMillis()));
        order.setAmount(amount);
        order.setStatus(Order.Status.open);
        order.setAddress(address);
        order.setCreditCard(creditCard);
        order.addItems(items);
        
        address = em.merge(address);
        creditCard = em.merge(creditCard);
        
        em.persist(order);
        
        em.flush();
        
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, 0);
            MessageProducer producer = session.createProducer(bookOrderQueue);
            MapMessage message = session.createMapMessage();
            message.setJMSType("processOrder");
            message.setString("orderNumber", order.getNumber());
            producer.send(message);
        } catch (JMSException e) {
            throw new EJBException("Cannot send 'processOrder' message!", e);
        }
        
        em.detach(order);
        
        return order;
    }
    
    private static void validate(CreditCard creditCard) throws CreditCardExpiredException {
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar expiration = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiration.setTime(creditCard.getExpiration());
        expiration.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        if (compare(expiration.getTime(), today.getTime()) < 0) {
            throw new CreditCardExpiredException();
        }
    }
  
    private static int compare(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        int month1 = calendar.get(Calendar.MONTH);
        int day1 = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTime(date2);
        int year2 = calendar.get(Calendar.YEAR);
        int month2 = calendar.get(Calendar.MONTH);
        int day2 = calendar.get(Calendar.DAY_OF_MONTH);
        if (year1 != year2) {
            return year1 - year2;
        }
        if (month1 != month2) {
            return month1 - month2;
        }
        return day1 - day2;
    }
}
