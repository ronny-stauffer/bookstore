package org.books.business.catalog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.books.common.data.Book;

import org.books.common.exception.CatalogException;

/**
 * The class Bookstore implements a bookstore.
 *
 * @author Stephan Fischli
 * @author Ronny Stauffer
 * @version 2.0
 */
public class LocalCatalogImpl implements Catalog {

    private static final String CATALOG_FILE = "/data/catalog.xml";
    private List<Book> books;
    private static final Logger LOGGER = Logger.getLogger(LocalCatalogImpl.class.getName());

    public LocalCatalogImpl() {
        try {
            JAXBContext context = JAXBContext.newInstance(org.books.common.data.Catalog.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream stream = getClass().getResourceAsStream(CATALOG_FILE);
            org.books.common.data.Catalog catalog = (org.books.common.data.Catalog) unmarshaller.unmarshal(stream);
            books = catalog.getBooks();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized List<Book> searchBooks(String title, String author, String publisher, int maxResults) throws CatalogException {
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

        LOGGER.info("Searching books...");

        title = title.toLowerCase();
        author = author.toLowerCase();
        publisher = publisher.toLowerCase();
        List<Book> results = new ArrayList<Book>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().indexOf(title) >= 0
                    && book.getAuthors().toLowerCase().indexOf(author) >= 0
                    && book.getPublisher().toLowerCase().indexOf(publisher) >= 0) {
                results.add(clone(book));
            }
        }

        LOGGER.info("...done.");

        return results;
    }

    @SuppressWarnings("unchecked")
    private <T> T clone(T object) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            new ObjectOutputStream(os).writeObject(object);
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            return (T) new ObjectInputStream(is).readObject();
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            return object;
        }
    }
}