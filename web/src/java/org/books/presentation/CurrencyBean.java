package org.books.presentation;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * @author Christoph Horber
 */
@ManagedBean(name = "currency")
@SessionScoped
public class CurrencyBean {

    private Currency currency = Currency.CHF;

    private enum Currency {
        CHF("CHF"), EUR("€"), USD("$");
        
        private final String symbol;
        
        private Currency(String symbol) {
            this.symbol = symbol;
        }

        private String getSymbol() {
            return symbol;
        }
    }

    public CurrencyBean() {
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(String currency) {
        this.currency = Currency.valueOf(currency);
    }
    
    public String getCurrency() {
        return currency.getSymbol();
    }

    public boolean active(String currency) {
        return this.currency == Currency.valueOf(currency);
    }
}
