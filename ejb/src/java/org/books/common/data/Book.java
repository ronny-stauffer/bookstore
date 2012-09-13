package org.books.common.data;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Christoph Horber
 */
@Entity
@NamedQuery(name = "findBook", query = "SELECT b FROM Book b WHERE b.isbn = :isbn")
public class Book extends BaseEntity {
    
    private static final long serialVersionUID = 1L;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String authors;
    
    @Column(nullable = false)
    private String publisher;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "publication_date")
    private Date date;
    
    private String binding;
    
    @Column(precision = 6, scale = 0)
    private Integer pages;
    
    @Column(precision = 6, scale = 2)
    private BigDecimal price;
    
    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Book{" + "id=" + getId() + ", isbn=" + isbn + ", title=" + title + ", authors=" + authors + ", publisher=" + publisher + ", date=" + date + ", binding=" + binding + ", pages=" + pages + ", price=" + price + '}';
    }
}
