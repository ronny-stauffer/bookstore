package org.books.business.catalog;

import java.util.List;
import org.books.common.data.Book;
import org.books.common.exception.CatalogException;

/**
 *
 * @author Christoph Horber
 */
public interface Catalog {

    public List<Book> searchBooks(String title, String author, String publisher, int maxResults) throws CatalogException;
}
