/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ue56923
 */
public class EncodingTests {
    
    public EncodingTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Test
    public void testRFC3339Timestamps() {
        Calendar calendar = Calendar.getInstance();
        
        TimeZone timeZone = calendar.getTimeZone();
        System.out.println(String.format("Time Zone ID: %s", timeZone.getID()));
        System.out.println(String.format("Time Zone Display Name: %s", timeZone.getDisplayName()));

        Date timestamp = calendar.getTime();
        String rfc3339Timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(timestamp);
        System.out.println(String.format("RFC3339 Timestamp: %s", rfc3339Timestamp));

        String rfc3339Timestamp2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(timestamp);
        System.out.println(String.format("RFC3339 Timestamp 2: %s", rfc3339Timestamp2));
    }
    
    @Test
    public void testUriBuilder() {
        // application/x-www-form-urlencoded
        
        //System.out.println(UriBuilder.fromPath("").queryParam("test", "{arg0}").build("http://example.com/joe"));
        try {
            System.out.println(URLEncoder.encode("http://example.com/joe", "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
