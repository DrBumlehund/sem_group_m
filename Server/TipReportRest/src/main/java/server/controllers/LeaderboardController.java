package server.controllers;


import Database.DatabaseFacade;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
public class LeaderboadController {

    private DatabaseFacade databaseFacade;

    public LeaderboadController() {
        this(DatabaseFacade.getInstance())
    }

    public LeaderboadController(DatabaseFacade databaseFacade) {
        this.databaseFacade = databaseFacade;
    }


}