package org.books.common.data;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The class Catalog contains the data of a catalog.
 * 
 * @author Stephan Fischli
 * @version 2.0
 */
@XmlRootElement(name = "catalog")
public class Catalog {

    private List<Book> books;

    @XmlElement(name = "book")
    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}
