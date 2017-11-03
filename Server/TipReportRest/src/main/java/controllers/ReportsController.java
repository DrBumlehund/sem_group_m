package controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import Database.DatabaseFacade;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
public class ReportsController {

	DatabaseFacade databaseFacade;
	
	public ReportsController () {
		this(DatabaseFacade.getInstance());
	}
	
	public ReportsController(DatabaseFacade databaseFacade) {
		this.databaseFacade = databaseFacade;
	}
	
	
}
