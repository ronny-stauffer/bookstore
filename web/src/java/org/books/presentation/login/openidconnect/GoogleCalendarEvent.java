package org.books.presentation.login.openidconnect;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ronny Stauffer
 */
@XmlRootElement
public class GoogleCalendarEvent {
    public static class Timestamp {
        public String dateTime;
        //public final String timeZone = "Europe/Zurich";
    }
    
    public String summary;
    public String description;
    public final Timestamp start = new Timestamp();
    public final Timestamp end = new Timestamp();
    public String location;
}
