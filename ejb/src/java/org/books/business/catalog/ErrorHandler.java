package org.books.business.catalog;

import com.amazon.webservices.Errors;
import com.amazon.webservices.OperationRequest;
import java.util.ArrayList;
import java.util.List;
import org.books.common.exception.CatalogException;

/**
 *
 * @author Christoph Horber
 */
abstract class ErrorHandler {

    static void handle(OperationRequest request) throws CatalogException {
        Errors errors = request.getErrors();
        if (errors != null) {
            List<String> details = new ArrayList<String>();
            for (Errors.Error error : errors.getError()) {
                details.add(String.format("ErrorCode: %s, ErrorMessage=%s", error.getCode(), error.getMessage()));
            }
            throw new CatalogException(details);
        }
    }
}
