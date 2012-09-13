package org.books.presentation;

import java.util.Collections;
import org.books.presentation.navigation.Navigation;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.books.business.CatalogServiceLocal;
import org.books.common.data.Book;
import org.books.common.exception.InvalidCriteriaException;

/**
 *
 * @author Christoph Horber
 */
@ManagedBean (name="catalogBean")
@SessionScoped
public class CatalogBean {

    private String title;
    
    private String author;
    
    private String publisher;
    
    private List<Book> bookResults = Collections.emptyList();
    
    private Book selectedBook;
    
    @EJB
    private CatalogServiceLocal catalogService;
    
    public String searchBooks() {
        bookResults = null;
        
        try {
            
            bookResults = catalogService.searchBooks(title, author, publisher);
            
            if (bookResults.isEmpty()) {
                MessageFactory.info("org.books.Bookstore.NO_BOOKS_FOUND");
            }
        } catch (InvalidCriteriaException ex) {
            Logger.getLogger(CatalogBean.class.getName()).log(Level.SEVERE, null, ex);
            MessageFactory.error("org.books.Bookstore.MISSING_SEARCH_CRITERIA");
            
            return Navigation.Catalog.searchBooks(bookResults);
        }
        
        return Navigation.Catalog.searchBooks(bookResults);
    }

    public String selectBook(Book selectedBook) {
        this.selectedBook = selectedBook;
        return Navigation.Catalog.searchBooks(selectedBook);
    }
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the publisher
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * @param publisher the publisher to set
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    /**
     * @return the bookResults
     */
    public List<Book> getBookResults() {
        return bookResults;
    }

    /**
     * @return the selectedBook
     */
    public Book getSelectedBook() {
        return selectedBook;
    }
    
    public boolean bookResultsEmpty() {
        return bookResults.isEmpty();
    }
}
