package api.exception;


public class CustomException extends RuntimeException {

    public CustomException(String msg) {
        super(msg);
    }
}

//Repository
//    |
//    v
//Service
//    |
//    |-- throw new CustomException(...)
//    |
//    v
//Controller
//    |
//    |-- No try-catch here
//    |
//    v
//Spring Exception Resolver
//    |
//    |-- Finds matching @ExceptionHandler(CustomException.class)
//    |
//    v
//@RestControllerAdvice
//    |
//    v
//Response to Client
