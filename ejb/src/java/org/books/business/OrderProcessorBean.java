package org.books.business;

import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.books.common.data.Order;

/**
 *
 * @author Ronny Stauffer
 */
@MessageDriven(mappedName = "jms/bookstoreOrderQueue", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class OrderProcessorBean implements MessageListener {
    private static final Logger logger = Logger.getLogger(OrderProcessorBean.class.getName());

    @Resource
    private TimerService timerService;
    
    @PersistenceContext(unitName = "bookstore")
    private EntityManager em;

    @Override
    public void onMessage(Message message) {
        MapMessage mapMessage = (MapMessage) message;
        try {
            if ("processOrder".equals(message.getJMSType())) {
                String orderNumber = mapMessage.getString("orderNumber");
                if (orderNumber != null) {
                    timerService.createSingleActionTimer(/* 10 Minutes: */ 10 * 60 * 1000, new TimerConfig(orderNumber, true));
                } else {
                    throw new EJBException("'processOrder' message does not have a valid order number property!");
                }
            }
        } catch (JMSException e) {
            throw new EJBException("Error on receiving message!", e);
        }
    }

    @Timeout
    private void processOrder(Timer timer) {
        String orderNumber = (String) timer.getInfo();

        TypedQuery findOrderQuery = em.createQuery("SELECT o FROM CustomerOrder o WHERE o.number = :number", Order.class);
        findOrderQuery.setParameter("number", orderNumber);
        List<Order> orders = findOrderQuery.getResultList();
        if (!orders.isEmpty()) {
            logger.info(String.format("Closing order with number %s...", orderNumber));
            orders.get(0).setStatus(Order.Status.closed);
        } else {
            logger.warning(String.format("Cannot close order with number %s! The specified order does not exist anymore!", orderNumber));
        }
    }
}
