package org.books.common.exception;

import javax.ejb.ApplicationException;

/**
 * The exception MissingLineItemsException is thrown if the line items of an
 * order are missing.
 * 
 * @author Stephan Fischli
 * @version 2.0
 */
@ApplicationException(rollback = true)
public class MissingLineItemsException extends Exception {
}
