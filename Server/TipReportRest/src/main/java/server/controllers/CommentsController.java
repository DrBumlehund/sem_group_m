package server.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Database.DatabaseFacade;
import Model.Report;
import Model.UserComment;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
public class CommentsController {

	DatabaseFacade databaseFacade;
	
	public CommentsController () {
		this(DatabaseFacade.getInstance());
	}
	
	public CommentsController(DatabaseFacade databaseFacade) {
		this.databaseFacade = databaseFacade;
	}

	@RequestMapping(value = "/comments", params = {"report-id", "comment", "user-id"})
	public UserComment addComment (@RequestParam(value="report-id") int reportId, @RequestParam(value="comment") String comment, @RequestParam(value="userId") int userId) {
		return databaseFacade.addComment(reportId, comment, userId);
	}
}
