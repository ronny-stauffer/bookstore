package org.books.common.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Christoph Horber
 */
@Entity
public class CreditCard extends BaseEntity {
    
    public static enum Type implements Serializable {
        MasterCard, Visa
    }
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private Type type;
    
    @Column(nullable = false, length = 20)
    private String number;

    @Temporal(TemporalType.DATE)
    private Date expiration;

    public CreditCard() {
        
    }
    
    public CreditCard(CreditCard creditCard) {
        this.type = creditCard.type;
        this.number = creditCard.number;
        this.expiration = creditCard.expiration;
    }
    
    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
