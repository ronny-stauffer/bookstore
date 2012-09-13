package org.books.presentation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import org.books.common.data.Book;
import org.books.common.data.LineItem;
import org.books.presentation.navigation.Navigation;

/**
 *
 * @author Christoph Horber
 */
@ManagedBean(name="shoppingCartBean")
@SessionScoped
public class ShoppingCartBean {

    private static final int MAX_QUANTITY = 30;

    private List<LineItem> lineItems;
    
    private BigDecimal amount;
    
    private List<SelectItem> quantities;
    
    @ManagedProperty(value = "#{currency}")
    private CurrencyBean currency;
    
    /** Creates a new instance of ShoppingCart */
    public ShoppingCartBean() {
        lineItems = new LinkedList<LineItem>();
        initAmount();
        
        quantities = new ArrayList<SelectItem>();
        for (int i = 1; i < MAX_QUANTITY + 1; i++) {
            SelectItem item = new SelectItem(i);
            quantities.add(item);
        }
    }
    
    public void clear() {
        lineItems = new LinkedList<LineItem>();
        initAmount();
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String addBook(Book book) {
        LineItem item = findBook(book);
        
        if (item != null) {
            int currentQuantity = item.getQuantity();
            if (currentQuantity >= MAX_QUANTITY) {
                SelectItem newItem = new SelectItem(currentQuantity + 1);
                quantities.add(newItem);
            }
            item.setQuantity(currentQuantity + 1);
        } else {
            item = new LineItem();
            item.setBook(book);
            item.setQuantity(1);
            lineItems.add(item);
        }

        updateAmount();
        
        return Navigation.ShoppingCart.addBook(book);
    }
    
    private LineItem findBook(Book book) {
        for (LineItem lineItem : lineItems) {
            if (lineItem.getBook().getIsbn().equals(book.getIsbn())) {
                return lineItem;
            }
        }
        
        return null;
    }
    
    public String updateQuantity(Book book, int newQuantity) {
        updateAmount();
        
        return Navigation.ShoppingCart.updateQuantity();
    }
    
    public String removeBook(Book book) {
        LineItem item = findBook(book);
        lineItems.remove(item);
        
        updateAmount();
        
        return Navigation.ShoppingCart.removeBook(!containBooks());
    }
    
    public boolean containBooks() {
        return !lineItems.isEmpty();
    }
    
    public String addMoreBooks() {
        return Navigation.ShoppingCart.addMoreBooks();
    }

    /**
     * @return the lineItems
     */
    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }

    /**
     * @return the quantities
     */
    public List<SelectItem> getQuantities() {
        return quantities;
    }

    private void updateAmount() {
        initAmount();
        for (LineItem lineItem : lineItems) {
            BigDecimal quantity = new BigDecimal(lineItem.getQuantity());
            BigDecimal bookAmount = lineItem.getBook().getPrice().multiply(quantity);
            amount = amount.add(bookAmount);
        }
    }

    private void initAmount() {
        amount = new BigDecimal(0);
        amount.setScale(2);
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(CurrencyBean currency) {
        this.currency = currency;
    }
}