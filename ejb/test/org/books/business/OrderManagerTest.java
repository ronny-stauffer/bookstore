package org.books.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import junit.framework.Assert;
import org.books.common.data.Address;
import org.books.common.data.Book;
import org.books.common.data.CreditCard;
import org.books.common.data.LineItem;
import org.books.common.data.Order;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Christoph Horber
 */
public class OrderManagerTest {
    
    private static final Logger LOGGER = Logger.getLogger(OrderManagerTest.class.getName());

    private static final String JNDI_NAME_PREFIX = "java:global/bookstore/";
    private static final String ORDER_MANAGER = "OrderManager";
    private static final String CATALOG_SERVICE = "CatalogService";
    
    private static Context jndiContext;
    
    private static OrderManagerRemote orderManager;
    private static CatalogService catalogService;

    @BeforeClass
    public static void setup() throws JAXBException, NamingException  {
        jndiContext = new InitialContext();
        
        catalogService = (CatalogService) jndiContext.lookup(JNDI_NAME_PREFIX + CATALOG_SERVICE);
        orderManager = (OrderManagerRemote) jndiContext.lookup(JNDI_NAME_PREFIX + ORDER_MANAGER);
    }
    
    @Test
    public void testOrderBooks() throws Exception {
        // search books (make sure there are some books in catalog service -> run CatalogServiceTest)
        List<Book> catalogBooks = catalogService.searchBooks("java", null, null);
        Assert.assertNotNull(catalogBooks);
        Assert.assertTrue(catalogBooks.size() > 1);
        
        Address address = new Address();
        address.setName("name");
        address.setStreet("street");
        address.setZip("1234");
        address.setCity("city");
        address.setCountry("country");
        orderManager.addAddress(address);
        
        CreditCard creditCard = new CreditCard();
        creditCard.setType(CreditCard.Type.Visa);
        creditCard.setNumber("4111111111111111");
        creditCard.setExpiration(new Date());
        orderManager.addCreditCard(creditCard);
        
        // build line items
        List<LineItem> items = new ArrayList<LineItem>();
        for (Book book : catalogBooks) {
            items.add(createItem(book, Double.valueOf(Math.random() * 10).intValue()));
        }
        
        // order book
        Order order = orderManager.orderBooks(items);
        Assert.assertNotNull(order);
        System.out.println(order.getNumber());
    }

    private static LineItem createItem(Book book, int quantity) {
        LineItem item = new LineItem();
        item.setQuantity(quantity);
        item.setBook(book);
        return item;
    }
}
