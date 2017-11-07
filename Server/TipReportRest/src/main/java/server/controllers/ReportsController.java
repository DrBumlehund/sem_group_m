package server.controllers;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Database.DatabaseFacade;
import Model.Report;
import Model.User;

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

	@RequestMapping(value = "/reports", params = {"only-coordinates"})
    public ArrayList<Report> getReports(@RequestParam(value="only-coordinates") Boolean onlyCoordinates) {
		if (onlyCoordinates) {
			return databaseFacade.getReportCoordinates();
		} else {
			return databaseFacade.getReports();
		}
    }

	@RequestMapping(value = "/reports", params = {"latitude", "longitude", "comment", "user-id"})
	public Report addReport (@RequestParam(value="latitude") double latitude, @RequestParam(value="longitude") double longitude, @RequestParam(value="comment") String comment, @RequestParam(value="userId") int userId) {
		return databaseFacade.addReport(latitude, longitude, comment, userId);
	}	
}
