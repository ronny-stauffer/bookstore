package org.books.business.catalog;

import com.amazon.webservices.Item;
import com.amazon.webservices.ItemAttributes;
import com.amazon.webservices.Items;
import com.amazon.webservices.Price;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.books.common.data.Book;

/**
 *
 * @author Christoph Horber
 */
class BookConverter {
    
    private static final Logger LOGGER = Logger.getLogger(BookConverter.class.getName());
    
    private final SimpleDateFormat dateFormatFull = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat dateFormatYearMonth = new SimpleDateFormat("yyyy-MM");
    private final SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
    
    List<Book> convert(List<Items> responses, int currentBookCount, int maxBooks) {
        List<Book> books = new ArrayList<Book>();
        for (Items items : responses) {
            int remainingBooks = maxBooks - currentBookCount - books.size();
            
            List<Book> newBooks = toBooks(items);
            for (int i = 0; i < newBooks.size() && i < remainingBooks; i++) {
                books.add(newBooks.get(i));
            }
        }
        
        return books;
    }
    
    private List<Book> toBooks(Items response) {
        List<Book> books = new ArrayList<Book>();
        for (Item item : response.getItem()) {
            Book book = convert(item);
            if (book != null) {
                books.add(book);
            }
        }
        return books;
    }
    
    private Book convert(Item item) {
        Book book = new Book();
        ItemAttributes bookAttributes = item.getItemAttributes();
        
        List<String> authors = bookAttributes.getAuthor();
        if (authors != null || authors.isEmpty()) {
            book.setAuthors(getAuthors(authors));
        } else { return null; }
        
        String binding = bookAttributes.getBinding();
        if (binding != null) {
            book.setBinding(binding);
        } else { return null; }
        
        String isbn = bookAttributes.getISBN();
        if (isbn != null) {
            book.setIsbn(isbn);
        } else { return null; }
        
        BigInteger pages = bookAttributes.getNumberOfPages();
        if (pages != null) {
            book.setPages(pages.intValue());
        } else { return null; }
        
        Price listPrice = bookAttributes.getListPrice();
        if (listPrice != null) {
            BigDecimal price = new BigDecimal(listPrice.getAmount()).divide(new BigDecimal(100));
            book.setPrice(price);
        } else { return null; }
        
        String publisher = bookAttributes.getPublisher();
        if (publisher != null) {
            book.setPublisher(publisher);
        } else { return null; }
        
        String title = bookAttributes.getTitle();
        if (title != null) {
            book.setTitle(title);
        } else { return null; }
        
        String date = bookAttributes.getPublicationDate();
        if (date != null) {
            book.setDate(parseDate(date));
        } else { return null; }
        
        return book;
    }
    
    private synchronized Date parseDate(String date) {
        // quick and dirty solution: try-and-error to get the date
        // SimpleDateFormat is not threadsafe
        try {
            return dateFormatFull.parse(date);
        } catch (ParseException ex) {
            try {
                return dateFormatYearMonth.parse(date);
            } catch (ParseException ex1) {
                try {
                    return dateFormatYear.parse(date);
                } catch (ParseException ex2) {
                    // unknown date format
                    LOGGER.log(Level.SEVERE, String.format("unknown date format, was %s", date), ex2);
                    return null;
                }
            }
        }
    }

    private String getAuthors(List<String> authors) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (String author : authors) {
            if (i++ > 0) { 
                builder.append(", ");
            }
            builder.append(author);
        }
        return builder.toString();
    }
}
