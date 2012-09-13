package org.books.presentation;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

/**
 * @author Christoph Horber
 */
public class TextFactory {

    public static String getResourceText(String bundleName, String key, Object... params) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            Application app = context.getApplication();
            ResourceBundle bundle = app.getResourceBundle(context, bundleName);
            return MessageFormat.format(bundle.getString(key), params);
        } catch (MissingResourceException e) {
            return "???" + key + "???";
        }
    }
}
