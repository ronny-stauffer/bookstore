package org.books.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.derby.drda.NetworkServerControl;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

/**
 *
 * @author Ronny Stauffer
 */
public class Launcher {

    private static final String APPLICATION_NAME = "Bookstore";
    private static final String DATABASE_NAME = "bookstore";
    private static final String GLASSFISH_CONFIG_FILE_NAME = "domain.xml";
    private static final String APPLICATION_CONFIG_FILE_NAME = "bookstore.properties";
    private static final String APPLICATION_PACKAGE_FILE_NAME = "bookstore.ear";
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Launcher.class.getName());

    public static void main(String[] args) /* throws GlassFishException */ {
        logger.info(String.format("Launching %s...", APPLICATION_NAME));

        // Determine installation path
        File installationPath = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String installationDirectoryPath = (installationPath.isDirectory() ? installationPath : installationPath.getParentFile()).getAbsolutePath();

        logger.info(String.format("Installation directory path: %s", installationDirectoryPath));

        logger.info("Starting Java DB (Apache Derby)...");
        NetworkServerControl serverControl;
        try {
            serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
            serverControl.start(null);
        } catch (Exception e) {
            throw new RuntimeException("Cannot start Java DB (Apache Derby)!", e);
        }

        logger.info(String.format("Creating database %s...", DATABASE_NAME));
        String driver = "org.apache.derby.jdbc.ClientDriver";
        String connectionURL = "jdbc:derby:" + /* installationDirectoryPath + "\\" + */ DATABASE_NAME + ";create=true"; 
        Connection connection = null;
        try {
            Class.forName(driver);
        } catch (java.lang.ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            connection = DriverManager.getConnection(connectionURL);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore exception
            }
        }

        logger.info("Starting Oracle Glassfish Embedded and deploying application...");
        GlassFishProperties glassfishProperties = new GlassFishProperties();
        String configFilePath = installationDirectoryPath + "\\" + GLASSFISH_CONFIG_FILE_NAME;
        String configFileURI = new File(configFilePath).toURI().toString();
        logger.info(String.format("Configuration file URI: %s", configFileURI));
        glassfishProperties.setConfigFileURI(configFileURI);
        glassfishProperties.setConfigFileReadOnly(true);

        try {
            GlassFishRuntime glassfishRuntime = GlassFishRuntime.bootstrap();
            GlassFish glassfish = glassfishRuntime.newGlassFish(glassfishProperties);
            glassfish.start();

            // Deploy plenus Server package to GlassFish
            File applicationPackage = new File(installationDirectoryPath + "\\" + APPLICATION_PACKAGE_FILE_NAME);
            Deployer deployer = glassfish.getDeployer();
            deployer.deploy(applicationPackage); // Can be invoked instead the variant above because other parameters are optional.

            System.out.println(String.format("Press <<Enter>> to stop the %s...", APPLICATION_NAME));
            // Wait for <<Enter>>
            try {
                new BufferedReader(new java.io.InputStreamReader(System.in)).readLine();
            } catch (IOException e) {
                // Ignore exception
            }

            // Teardown GlassFish
            glassfish.dispose();
            glassfishRuntime.shutdown();
        } catch (GlassFishException e) {
            throw new RuntimeException("Glassfish error!", e);
        }

        logger.info("Stopping Java DB (Apache Derby)...");
        try {
            serverControl.shutdown();
        } catch (Exception e) {
            throw new RuntimeException("Cannot stop Java DB (Apache Derby)!", e);
        }
    }
}