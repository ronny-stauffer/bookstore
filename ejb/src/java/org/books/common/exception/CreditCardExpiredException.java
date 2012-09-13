package org.books.common.exception;

import javax.ejb.ApplicationException;

/**
 * The exception CreditCardExpiredException is thrown if a credit card has
 * expired.
 * 
 * @author Stephan Fischli
 * @version 2.0
 */
@ApplicationException(rollback = true)
public class CreditCardExpiredException extends Exception {
}
