package api.service;

import api.entity.Learner;
import api.exception.LearnerNotFoundException;
import api.requests.LearnerRequest;
import api.response.LearnerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LearnerService {

    ResponseEntity<ServiceResponse<LearnerResponse>> addLearner(LearnerRequest learnerRequest);

    List<Learner> fetchAllLearner();

    List<Learner> fetchByName(String name);

    Learner fetchByEmail(String email);

    Learner fetchByNameAndEmail(String name, String email);

    ResponseEntity<ServiceResponse<LearnerResponse>> fetchById(Long id) throws LearnerNotFoundException;

    ResponseEntity<ServiceResponse<String>> deleteById(Long id);
}
