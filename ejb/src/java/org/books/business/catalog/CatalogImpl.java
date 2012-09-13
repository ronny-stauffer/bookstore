package org.books.business.catalog;

import com.amazon.webservices.*;
import java.util.ArrayList;
import java.util.List;
import org.books.common.data.Book;
import org.books.common.exception.CatalogException;

/**
 *
 * @author Christoph Horber
 */
public class CatalogImpl implements Catalog {
    
    // AWS limit constants
    private static final int ITEMS_PER_PAGE = 10;
    private static final int ITEM_PAGE_LIMIT = 10;
    private static final int BATCH_REQUEST_LIMIT = 2;

    private final BookConverter bookConverter = new BookConverter();
    
    @Override
    public List<Book> searchBooks(String title, String author, String publisher, int maxResults) throws CatalogException {
        if (title == null) {
            throw new NullPointerException("title");
        }
        if (author == null) {
            throw new NullPointerException("author");
        }
        if (publisher == null) {
            throw new NullPointerException("publisher");
        }
        if (maxResults < 0) {
            throw new IllegalArgumentException("maxResults");
        }
      
        List<Book> books = new ArrayList<Book>();

        if (title.isEmpty()
                && author.isEmpty()
                && publisher.isEmpty()) {
            return books; // abort
        }
        
        if (maxResults == 0) {
            return books; // abort
        }
        
        BookSearch bookSearch = new BookSearch(title, author, publisher, maxResults);
        Integer totalPages = null; // holds the maximum number of totalPages provided by AWS
        
        // if we expect "heavy" load, get the lightweight call to get the totalPages
        if (maxResults > BATCH_REQUEST_LIMIT * ITEMS_PER_PAGE) {
            totalPages = bookSearch.readTotalPages();
        }
        
        int nextItemPage = 1; // for the first request start at item page 1
        
        while (maxResultsNotReached(books.size(), maxResults) && moreItemPagesAvailable(nextItemPage, totalPages)) {
            // get the maximum allowed and necessary pageCount to optimize the number of calls
            // pageCount indicates how many ItemSearchRequests are contained in one itemsearch call
            int pageCount = getPageCount(books.size(), maxResults, nextItemPage, totalPages);
            
            List<Items> responses = bookSearch.search(nextItemPage, pageCount);
            if (responses == null) { 
                break; // abort
            }
            
            // convert response to valid books, stop if maxResults limit has been reached
            books.addAll(bookConverter.convert(responses, books.size(), maxResults));
            
            // get totalPages after first call if we did not made the lightweight getTotalPages call first
            if (totalPages == null) {
                totalPages = bookSearch.getTotalPages(responses);
            }
            
            // update itemPage
            nextItemPage = nextItemPage + pageCount;
        }
        
        return books;
    }
    
    // this algorithm is used to optimize the number of item requests per item search
    // unfortunately AWS supports currently only 2...
    private static int getPageCount(final int bookResults, final int maxResults, final int currentItemPage, final Integer availablePages) {
        Integer totalPages = availablePages;
        if (totalPages == null) {
            // on first request we do not know yet the total pages
            // lets assume we can get all of them
            totalPages = Integer.MAX_VALUE;
        }
        int remainingAvailablePages = totalPages - currentItemPage;
        double remainingBooks = maxResults - bookResults;
        Double ceil = new Double(Math.ceil(remainingBooks / ITEMS_PER_PAGE));
        int possiblePageCount = ceil.intValue();
        
        // take account of the BATCH_REQUEST_LIMIT
        if (possiblePageCount >= BATCH_REQUEST_LIMIT || remainingAvailablePages >= BATCH_REQUEST_LIMIT) {
            return BATCH_REQUEST_LIMIT;
        } else if (possiblePageCount > remainingAvailablePages) {
            return remainingAvailablePages;
        } else {
            return possiblePageCount;
        }
    }
    
    private static boolean maxResultsNotReached(int books, int maxResults) {
        return books < maxResults;
    }
    
    private static boolean moreItemPagesAvailable(int nextItemPage, Integer totalPages) {
        if (totalPages == null) {
            return true; // first run, total pages not yet set
        } else if (nextItemPage > ITEM_PAGE_LIMIT) {
            // not allowed, algorithm will abort 
            // and return the books he has found (in agreement with s.fischli)
            return false; 
        }
        return nextItemPage <= totalPages;
    }
}
