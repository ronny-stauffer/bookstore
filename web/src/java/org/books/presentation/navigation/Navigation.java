package org.books.presentation.navigation;

import java.util.List;
import org.books.common.data.Book;

/**
 *
 * @author Christoph Horber
 */
public class Navigation {
    enum PAGES {
        LOGIN("login"),
        CATALOG("catalog"),        
        SEARCH_RESULTS("searchResults"),
        BOOK_DETAILS("bookDetails"),
        SHOPPING_CART("shoppingCart"),
        ADDRESS("address"),        
        CREDIT_CARD("creditCard"),
        ORDER_SUMMARY("orderSummary"),
        ORDER_CONFIRMATION("orderConfirmation"),
        ORDER_SEARCH("orderSearch"),
        ORDER_LIST("orderList"),
        ORDER_DETAILS("orderDetails");
        
        private final String pageName;
        
        PAGES(String page) {
            this.pageName = page;
        }
        
        String page() {
            return pageName;
        }
    }
    
    public static String login() {
        return PAGES.LOGIN.page();
    }

    public static class Catalog {
        public static String searchBooks(List<Book> results) {
            if (results != null && !results.isEmpty()) {
                return PAGES.SEARCH_RESULTS.page();  
            } else {
                return null; // XHTML.CATALOG.page();
            }
        }
        
        public static String searchBooks(Book book) {
            if (book != null) {
                return PAGES.BOOK_DETAILS.page();  
            } else {
                return null; // XHTML.CATALOG.page();
            }
        }

        public static String addToShoppingCart() {
            return PAGES.SHOPPING_CART.page();
        }
    } 
    
    public static class ShoppingCart {
        public static String addBook(Book book) {
            return PAGES.SHOPPING_CART.page();
        }

        public static String updateQuantity() {
            return PAGES.SHOPPING_CART.page();
        }

        public static String removeBook(boolean emptyShoppingCart) {
            if (emptyShoppingCart) {
                return PAGES.CATALOG.page();
            } else {
                return PAGES.SHOPPING_CART.page();
            }
        }

        public static String addMoreBooks() {
            return PAGES.SEARCH_RESULTS.page();
        }
    }
    
    public static class Order {
        public static String order() {
            return PAGES.ADDRESS.page();
        }
        public static String creditCard() {
            return PAGES.CREDIT_CARD.page();
        }
        public static String summary() {
            return PAGES.ORDER_SUMMARY.page();
        }
        public static String changeAddress() {
            return PAGES.ADDRESS.page();
        }
        public static String changeCreditCard() {
            return PAGES.CREDIT_CARD.page();
        }
        public static String submit() {
            return PAGES.ORDER_CONFIRMATION.page();
        }
        public static String close() {
            return PAGES.CATALOG.page();
        }        
    }
    
    public static class Orders {
        public static String search() {
            return PAGES.ORDER_LIST.page();
        }
        public static String invalidSearch() {
            return PAGES.ORDER_SEARCH.page();
        }
        public static String searchOrders(org.books.common.data.Order order) {
            if (order != null) {
                return PAGES.ORDER_DETAILS.page();  
            } else {
                return null; // XHTML.ORDER_LIST.page();
            }
        }
        public static String closeList() {
            return PAGES.ORDER_SEARCH.page();
        }
        public static String closeDetails() {
            return PAGES.ORDER_LIST.page();
        }
    }
}
