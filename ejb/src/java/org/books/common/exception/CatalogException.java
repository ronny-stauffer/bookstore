package org.books.common.exception;

import java.util.List;

/**
 *
 * @author Christoph Horber
 */
public class CatalogException extends Exception {

    private List<String> details;

    public CatalogException(List<String> details) {
        this.details = details;
    }

    public List<String> getDetails() {
        return details;
    }
}
