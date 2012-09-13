package org.books.presentation;

import java.util.LinkedList;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;

/**
 * @author Christoph Horber
 */
@ManagedBean
@SessionScoped
public class NavigationBean {

    private static final int MAX_HISTORY_SIZE = 10;
    
    private final LinkedList<String> history;

    public NavigationBean() {
        history = new LinkedList<String>();
    }
    
    public void setLastPage(String navigationCase) {
        history.push(navigationCase);
        if (history.size() > MAX_HISTORY_SIZE ) {
            history.pollLast();
        }
    }
    
    public String getLastPage() {
        return history.pop();
    }
}
