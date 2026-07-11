package api.service;

import api.entity.Learner;
import api.exception.CustomException;
import api.exception.LearnerNotFoundException;
import api.repository.LearnerRepository;
import api.requests.LearnerRequest;
import api.response.LearnerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LearnerServiceImpl implements LearnerService{

    @Autowired
    LearnerRepository learnerRepository;

    @Override
    public ResponseEntity<ServiceResponse<LearnerResponse>> addLearner(LearnerRequest learnerRequest) {

        Optional<Learner> optionalEmailLearner = learnerRepository.findByEmail(learnerRequest.getEmail());
        if (optionalEmailLearner.isPresent()) {
            throw new CustomException(learnerRequest.getEmail() + " is already registered!");
        }

        // 1. Convert DTO -> Entity
        Learner learner = new Learner();
        learner.setName(learnerRequest.getName());
        learner.setEmail(learnerRequest.getEmail());
        learner.setMobile(learnerRequest.getMobile());

        // 2. Save Entity in DB
        Learner savedLearner = learnerRepository.save(learner);

        // 3. Convert Entity -> DTO
        LearnerResponse learnerResponse = new LearnerResponse();
        learnerResponse.setName(savedLearner.getName());
        learnerResponse.setEmail(savedLearner.getEmail());
        learnerResponse.setMobile(savedLearner.getMobile());

        // 4. Wrap into generic ServiceResponse
        ServiceResponse<LearnerResponse> response = new ServiceResponse<>();
        response.setMessage("Learner created successfully");
        response.setData(learnerResponse);

        // 5. Return ResponseEntity with HTTP 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    public List<Learner> fetchAllLearner() {
        return learnerRepository.findAll();
    }

    @Override
    public List<Learner> fetchByName(String name) {
        return learnerRepository.findByName(name);
    }

    @Override
    public Learner fetchByEmail(String email) {
        Optional<Learner> learnerOptional = learnerRepository.findByEmail(email);
        if (learnerOptional.isEmpty()) {
            throw new RuntimeException("Learner with email: " + email + " not found!");
        }
        return learnerOptional.get();
    }

    @Override
    public Learner fetchByNameAndEmail(String name, String email) {
        return learnerRepository.findByNameAndEmail(name, email);
    }

    @Override
    public ResponseEntity<ServiceResponse<LearnerResponse>> fetchById(Long id) throws LearnerNotFoundException {

        Learner learner = learnerRepository.findById(id)
                .orElseThrow(() -> new LearnerNotFoundException("Learner not found with id: " + id));

        // Convert Entity → DTO
        LearnerResponse learnerResponse = new LearnerResponse();
        learnerResponse.setId(learner.getId());
        learnerResponse.setName(learner.getName());
        learnerResponse.setEmail(learner.getEmail());
        learnerResponse.setMobile(learner.getMobile());

        // Wrap response
        ServiceResponse<LearnerResponse> response = new ServiceResponse<>();
        response.setMessage("Learner fetched successfully");
        response.setData(learnerResponse);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ServiceResponse<String>> deleteById(Long id) {

        Learner learner = learnerRepository.findById(id)
                .orElseThrow(() -> new CustomException("Learner not found for id: " + id));

        learnerRepository.delete(learner);

        ServiceResponse<String> response = new ServiceResponse<>();
        response.setMessage("Learner with id: " + id + " deleted successfully!");

        return ResponseEntity.ok(response);
    }
}
