package server.controllers;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import Database.DatabaseFacade;
import Model.User;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
public class UsersController {
	
	DatabaseFacade databaseFacade;
	
	public UsersController () {
		this(DatabaseFacade.getInstance());
	}
	
	public UsersController(DatabaseFacade databaseFacade) {
		this.databaseFacade = databaseFacade;
	}
	
	@RequestMapping(value = "/users", params = {"username", "password"})
    public User addUser(@RequestParam(value="username") String username, @RequestParam(value="password") String password) {
		return databaseFacade.addUser(username, password);
    }

	@RequestMapping(value = "/login", params = {"username", "password"})
    public User login(@RequestParam(value="username") String username, @RequestParam(value="password") String password) {
		return databaseFacade.getUser(username, password);
    }
	

	@RequestMapping( value = "/**", method = RequestMethod.OPTIONS ) 
	public ResponseEntity handle() {	
		return new ResponseEntity(HttpStatus.OK); 
	}
	
}
