package org.books.business;

import java.io.InputStream;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.books.common.data.Book;
import org.books.common.data.Catalog;
import org.books.common.exception.InvalidCriteriaException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Christoph Horber
 */
public class CatalogServiceTest {

    private static final String CATALOG_FILE = "catalog.xml";

    private static final String JNDI_NAME = "java:global/bookstore/CatalogService";
    private static Context jndiContext;
    private static CatalogService catalogService;
    
    public CatalogServiceTest() {
    }
    
    @BeforeClass
    public static void setup() throws JAXBException, NamingException  {
        jndiContext = new InitialContext();
        
        catalogService = (CatalogService) jndiContext.lookup(JNDI_NAME);

        List<Book> books = readBooksFromXmlFile();
        for (Book book : books) {
            catalogService.addBook(book);
        }
    }
    
    @Test
    public void testSearchBooksByTitle() throws InvalidCriteriaException {
        List<Book> b = catalogService.searchBooks("java", null, null);
        Assert.assertEquals(10, b.size());
    }
    
    @Test
    public void testSearchBooksByAuthor() throws InvalidCriteriaException {
        List<Book> b = catalogService.searchBooks(null, "David", null);
        Assert.assertEquals(3, b.size());

        List<Book> b2 = catalogService.searchBooks(null, "David Flanagan", null);
        Assert.assertEquals(2, b2.size());
    }
    
    @Test
    public void testSearchBooksByPublisher() throws InvalidCriteriaException {
        List<Book> b = catalogService.searchBooks(null, null, "O'Reilly");
        Assert.assertEquals(5, b.size());
    }
     
    @Test
    public void testAddBook() {
        Book book = new Book();
        book.setIsbn("1234");
        book.setTitle("bookstore");
        book.setAuthors("Christoph Horber");
        book.setPublisher("BFH");
        catalogService.addBook(book);
        
        Book b = catalogService.findBook("1234");
        Assert.assertNotNull(b);
    }
    
    @Test
    public void testRemoveBook() {
        Book b = new Book();
        b.setIsbn("9999");
        b.setTitle("My Test");
        b.setAuthors("Christoph Horber");
        b.setPublisher("BFH");
        Book book = catalogService.addBook(b);
        
        Assert.assertNotNull(catalogService.findBook("9999"));
        
        catalogService.removeBook(book);
        
        Assert.assertNull(catalogService.findBook("9999"));
    }
        
    @Test
    public void testUpdateBook() {
        Book b = new Book();
        b.setIsbn("9998");
        b.setTitle("My Test");
        b.setAuthors("Christoph Horber");
        b.setPublisher("BFH");
        Book book = catalogService.addBook(b);
        
        Assert.assertNotNull(catalogService.findBook("9998"));
        
        book.setTitle("new Title");
        catalogService.updateBook(book);
        
        Book updatedBook = catalogService.findBook("9998");
        Assert.assertEquals("new Title", updatedBook.getTitle());
    }
    
    @Test(expected = InvalidCriteriaException.class)
    public void testSearchBooksInvalidCriteria() throws InvalidCriteriaException {
        catalogService.searchBooks(null, null, null);
    }
    
    private static List<Book> readBooksFromXmlFile() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Catalog.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream stream = CatalogServiceTest.class.getResourceAsStream(CATALOG_FILE);
        Catalog catalog = (Catalog) unmarshaller.unmarshal(stream);
        return catalog.getBooks();
    }
}