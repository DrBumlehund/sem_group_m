package server.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Database.DatabaseFacade;
import Model.Report;
import Model.Vote;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
public class VotesController {

	DatabaseFacade databaseFacade;
	
	public VotesController () {
		this(DatabaseFacade.getInstance());
	}
	
	public VotesController(DatabaseFacade databaseFacade) {
		this.databaseFacade = databaseFacade;
	}
	
	@RequestMapping(value = "/votes", params = {"report-id", "vote", "user-id"})
	public Vote addVote (@RequestParam(value="report-id") int reportId, @RequestParam(value="vote") boolean vote, @RequestParam(value="user-id") int userId) {
		return databaseFacade.addVote(reportId, vote, userId);
	}
}
