package nl.amsta09.driver;

/**
 * Mainclass van de applicatie.
 *
 */

import nl.amsta09.web.JettyServer;

public class MainApp {
    
    public static void main( String[] args ) throws Exception
    {
    	JettyServer jettyServer = new JettyServer();
    	jettyServer.setHandler();
    	jettyServer.start();
    }
}