package org.books.common.data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 *
 * @author Christoph Horber
 */
@Entity
public class LineItem extends BaseEntity {
    
    @Column(nullable = false, precision = 3)
    private Integer quantity;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Book book;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
