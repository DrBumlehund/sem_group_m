package server;

import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import Database.DatabaseFacade;

@SpringBootApplication
public class Application {

	private static int port = 8080;
	private static String dbSchemaName = "tip_report";
	/**
	 * 
	 * @param args
	 * Argument 1: An integer describing the port to run the rest server on. Defaults to 8080
	 * Argument 2: A string describing the schema name of the database to use. Defaults to tip_report
	 */
    public static void main(String[] args) {
    	if (args.length > 0) {
    		int javaArgsPort = Integer.parseInt(args[0]);
    		port = javaArgsPort;
    	}
    	if (args.length > 1) {
    		dbSchemaName = args[1];
    	}
    	DatabaseFacade.getInstance().setSchemaName(dbSchemaName);
    	
    	HashMap<String, Object> props = new HashMap<>();
    	props.put("server.port", port);

    	new SpringApplicationBuilder()
    	    .sources(Application.class)                
    	    .properties(props)
    	    .run(args);
    }
}
