package org.books.presentation;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;

/**
 * @author Christoph Horber
 */
@FacesComponent(DateInput.DATE_INPUT_COMPONENT_TYPE)
public class DateInput extends UIInput {

    public static final String DATE_INPUT_COMPONENT_TYPE = "org.books.DateInput";
    
    public enum PropertyKeys {
        startYear, numYears;
    }
    
    public Integer getStartYear() {
        return getInt(PropertyKeys.startYear);
    }
    
    public Integer getNumYears() {
        return getInt(PropertyKeys.numYears);
    }
    
    public void setStartYear(Integer startYears) {
        setInt(PropertyKeys.startYear, startYears);
    }
    
    public void setNumYears(Integer numYears) {
        setInt(PropertyKeys.numYears, numYears);
    }
    
    private Integer getInt(PropertyKeys key) {
        Object value = getStateHelper().eval(key);
        return value != null ? (Integer) value : null;
    }
    
    private void setInt(PropertyKeys key, Integer value) {
        getStateHelper().put(key, value);
    }
}
