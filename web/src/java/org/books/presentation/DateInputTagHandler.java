package org.books.presentation;

import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;

/**
 * @author Christoph Horber
 */
public class DateInputTagHandler extends ComponentHandler {
    
    public DateInputTagHandler(ComponentConfig config) {
        super(config);
        getRequiredAttribute(DateInput.PropertyKeys.numYears.name());
    }
}
