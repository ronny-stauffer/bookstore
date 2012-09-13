package org.books.presentation;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

/**
 *
 * @author Christoph Horber
 */
@FacesRenderer(componentFamily=UIInput.COMPONENT_FAMILY, rendererType=DateInputRenderer.RENDERER_TYPE)
public class DateInputRenderer extends Renderer {
    
    public static final String RENDERER_TYPE = "org.books.DateInputRenderer";

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        DateInput dateInput = (DateInput) component;
        Object value = dateInput.getSubmittedValue();
        if (value == null) {
            value = dateInput.getValue();
        }
        
        Calendar calendar = Calendar.getInstance();
        int month = 0;
        int selectedYear = 0;
        
        Integer numYears = dateInput.getNumYears();
        if (numYears == null) {
            numYears = 1;
        }
        
        Integer startYear = dateInput.getStartYear();
        if (startYear == null) {
            startYear = calendar.get(Calendar.YEAR);
        }
        
        if (value != null) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime((Date) value);
            month = cal.get(Calendar.MONTH) + 1;
            selectedYear = cal.get(Calendar.YEAR);
        } else {
            month = calendar.get(Calendar.MONTH) + 1;
            selectedYear = calendar.get(Calendar.YEAR);
        }

        ResponseWriter writer = context.getResponseWriter();
        
        // month selection
        writer.startElement("select", dateInput);
        writer.writeAttribute("name", dateInput.getClientId() + ":month", null);
        for (int i = 1; i <= 12; i++) {
            writer.startElement("option", dateInput);
            writer.writeAttribute("value", i, null);
            if (month == i) {
                writer.writeAttribute("selected", "true", null);
            }
            writer.writeText(i, null);
            writer.endElement("option");
        }
        
        writer.endElement("select");
        
        
        // year selection
        writer.startElement("select", dateInput);
        writer.writeAttribute("name", dateInput.getClientId() + ":year", null);
        int endYear = startYear + numYears;
        for (int i = startYear; i < endYear; i++) {
            writer.startElement("option", dateInput);
            writer.writeAttribute("value", i, null);
            if (i == selectedYear) {
                writer.writeAttribute("selected", "true", null);
            }
            writer.writeText(i, null);
            writer.endElement("option");
        }
        
        writer.endElement("select");
        
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        int month = Integer.parseInt(params.get(component.getClientId() + ":month"));
        int year = Integer.parseInt(params.get(component.getClientId() + ":year"));
        GregorianCalendar cal = new GregorianCalendar();
        cal.setLenient(true);
        cal.set(year, month, 0, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        ((UIInput)component).setSubmittedValue(cal.getTime());
    }
}