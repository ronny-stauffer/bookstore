package org.books.common.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Christoph Horber
 */
@Entity(name = "CustomerOrder")
@NamedQuery(name = "searchOrders", query = "SELECT o FROM CustomerOrder o WHERE o.date >= :fromDate and o.date <= :toDate")
public class Order extends BaseEntity {
    
    public static enum Status implements Serializable {
        open, closed, canceled
    }

    @Column(nullable = false, unique = true)
    private String number;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    private Address address;
    
    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    private CreditCard creditCard;
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<LineItem> items = new ArrayList<LineItem>();
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ORDER_DATE")
    private Date date;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<LineItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<LineItem> items) {
        this.items = items;
    }
    
    public void addItems(List<LineItem> items) {
        this.items.addAll(items);
    }

    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
