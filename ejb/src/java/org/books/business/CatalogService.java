package org.books.business;

import java.util.List;
import org.books.common.data.Book;
import org.books.common.exception.InvalidCriteriaException;

/**
 *
 * @author Christoph Horber
 */
public interface CatalogService {
    
    Book addBook(Book book);
    
    List<Book> searchBooks(String title, String author, String publisher) throws InvalidCriteriaException;
    
    Book findBook(String isbn);
    
    Book updateBook(Book book);
    
    void removeBook(Book book);
}
