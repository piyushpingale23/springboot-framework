package api.controller;

import api.entity.Learner;
import api.exception.LearnerNotFoundException;
import api.requests.LearnerRequest;
import api.response.LearnerResponse;
import api.service.LearnerService;
import api.service.ServiceResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LearnerController {

    @Autowired
    LearnerService learnerService;

    ResponseEntity<LearnerResponse> finalResponse = null;

//    curl --location 'http://localhost:8090/api/learners' \
//            --header 'Content-Type: application/json' \
//            --data-raw '{
//            "name": "Piyush Ingale",
//            "email": "piyush@example.com",
//            "mobile": "9876543210"}'
    @PostMapping("/learners")
    public ResponseEntity<ServiceResponse<LearnerResponse>> addLearner(@Valid @RequestBody LearnerRequest learnerRequest) {
        return learnerService.addLearner(learnerRequest);
    }

    @GetMapping("/learners")
    public List<Learner> getLearners (@RequestParam (value = "name", required = false) String name,
                                      @RequestParam (value = "email", required = false) String email) {


        if (name == null && email == null) {
            return learnerService.fetchAllLearner();
        } else if (name != null && email == null) {
            return learnerService.fetchByName(name);
        } else if (name == null) {
            return List.of(learnerService.fetchByEmail(email));
        } else {
            return List.of(learnerService.fetchByNameAndEmail(name, email));
        }

    }

    @GetMapping("/learners/{id}")
    public ResponseEntity<ServiceResponse<LearnerResponse>> fetchById (@PathVariable Long id) throws LearnerNotFoundException {
        return learnerService.fetchById(id);
    }

    @DeleteMapping("/learners/{id}")
    public ResponseEntity<ServiceResponse<String>> deleteById (@PathVariable Long id) {
        return learnerService.deleteById(id);
    }

}
