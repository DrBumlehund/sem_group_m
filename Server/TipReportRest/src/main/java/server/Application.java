package server;

import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
    	HashMap<String, Object> props = new HashMap<>();
    	props.put("server.port", 8080);

    	new SpringApplicationBuilder()
    	    .sources(Application.class)                
    	    .properties(props)
    	    .run(args);
    }
}
