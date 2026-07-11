package api.exception;

import api.service.ServiceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LearnerNotFoundException.class)
    public ResponseEntity<ServiceResponse<Void>> learnerNotFoundException(LearnerNotFoundException exception) {

        ServiceResponse<Void> response = new ServiceResponse<>();
        response.setMessage(exception.getMessage());
        response.setData(null);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ServiceResponse<Void>> customExceptionHandler (CustomException exception) {
        ServiceResponse<Void> response = new ServiceResponse<>();
        response.setMessage(exception.getMessage());
        response.setData(null);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceResponse<List<String>>> methodArgumentExceptionHandler(MethodArgumentNotValidException exception) {

        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ServiceResponse<List<String>> response = new ServiceResponse<>();
        response.setMessage("Validation failed");
        response.setData(errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResponse<Void>> globalExceptionHandler (Exception exception) {
        ServiceResponse<Void> response = new ServiceResponse<>();
        response.setMessage(exception.getMessage());
        response.setData(null);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}


//Client Request
//      |
//      v
//Controller Method
//      |
//      |-- Exception thrown
//      v
//@RestControllerAdvice
//      |
//      |-- Matching @ExceptionHandler found?
//      |         |
//      |         +--> Yes -> Execute handler method -> Return custom response
//      |
//      +--> No
//            |
//            v
//Spring Boot Default Exception Handling
//            |
//            v
//Return Error Response (500, 404, etc.)
