package org.books.presentation.navigation;

import org.books.presentation.navigation.Navigation;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christoph Horber
 */
public class NavigationTest {

    private static final String ADMIN_DIRECTORY = "admin";

    /**
     * Test of fromCatalogBeanSearchBooks method, of class Navigation.
     */
    @Test
    public void testXHTMLPages() {
        for (Navigation.PAGES page : Navigation.PAGES.values()) {
            String pageName = page.page();
            
            File pageFile = new File("web/" + pageName + ".xhtml");
            File pageFile2 = new File("web/" + ADMIN_DIRECTORY + "/" + pageName + ".xhtml");
            Assert.assertTrue("File " + pageName + " must exist!", pageFile.exists() || pageFile2.exists());
        }
    }
}
