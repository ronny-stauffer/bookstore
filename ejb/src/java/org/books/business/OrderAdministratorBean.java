package org.books.business;

import java.util.Date;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.books.common.data.Order;
import org.books.common.exception.InvalidTimePeriodException;
import org.books.common.exception.OrderNotCancelableException;
import org.books.common.exception.OrderNotFoundException;

/**
 *
 * @author staufferr
 */
@Stateless
@RolesAllowed("employee")
public class OrderAdministratorBean implements OrderAdministratorRemote, OrderAdministratorLocal {
    @PersistenceContext(unitName = "bookstore")
    private EntityManager em;
    
    @Override
    public List<Order> searchOrders(Date fromDate, Date toDate) throws InvalidTimePeriodException {
        if (fromDate.after(toDate)) {
            throw new InvalidTimePeriodException();
        }
        
        TypedQuery searchOrdersQuery = em.createNamedQuery("searchOrders", Order.class);
        searchOrdersQuery.setParameter("fromDate", fromDate);
        searchOrdersQuery.setParameter("toDate", toDate);
        return searchOrdersQuery.getResultList();
    }

    @RolesAllowed("manager")
    @Override    
    public void cancelOrder(String number) throws OrderNotFoundException, OrderNotCancelableException {
        TypedQuery findOrderQuery = em.createQuery("SELECT o FROM CustomerOrder o WHERE o.number = :number", Order.class);
        findOrderQuery.setParameter("number", number);
        List<Order> orders = findOrderQuery.getResultList();
        if (!orders.isEmpty()) {
            Order order = orders.get(0);
            if (order.getStatus() != Order.Status.open) {
                    throw new OrderNotCancelableException();
            }
            order.setStatus(Order.Status.canceled);
            return;
        }
        throw new OrderNotFoundException();
    }
}
