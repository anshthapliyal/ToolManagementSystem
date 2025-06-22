package com.coditas.tool.management.system.exception.handler;

import com.coditas.tool.management.system.dto.sharedResponse.GeneralErrorResponse;
import com.coditas.tool.management.system.exception.PremiseNotFoundException;
import com.coditas.tool.management.system.exception.ResourceNotFoundException;
import com.coditas.tool.management.system.exception.UnauthorizedException;
import com.coditas.tool.management.system.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    //For General Exceptions
    @ExceptionHandler
    public ResponseEntity<GeneralErrorResponse> handleException(Exception exception){//for any other types of exceptions
        //Create object of General Error Response
        GeneralErrorResponse error = new GeneralErrorResponse();
        //Set fields
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());  //400
        error.setMessage(exception.getMessage());
        error.setTime(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);  //ResponseEntity<>(Body, StatusCode)
    }

    //All JWT Exceptions: BadCredentialsException, SecurityException, MalformedJwtException, ExpiredJwtException,
    //UnsupportedJwtException
    @ExceptionHandler
    public ResponseEntity<GeneralErrorResponse> handleException(UnauthorizedException exception){
        GeneralErrorResponse error = new GeneralErrorResponse();
        error.setErrorCode(HttpStatus.UNAUTHORIZED.value()); //401
        error.setMessage(exception.getMessage());
        error.setTime(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    //for IllegalArgumentException
    @ExceptionHandler
    public ResponseEntity<GeneralErrorResponse> handleException(IllegalArgumentException exception) {
        GeneralErrorResponse error = new GeneralErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value()); //400
        error.setMessage(exception.getMessage());
        error.setTime(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //For Validation Related Exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GeneralErrorResponse> handleException(MethodArgumentNotValidException exception){

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        GeneralErrorResponse error = new GeneralErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value()); //400
        error.setMessage(message);
        error.setTime(LocalDateTime.now());

        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    //UserNotFoundException
    @ExceptionHandler
    public ResponseEntity<GeneralErrorResponse> handleException(UserNotFoundException exception) {
        GeneralErrorResponse error = new GeneralErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());   //400
        error.setMessage(exception.getMessage());
        error.setTime(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //PremiseNotFoundException
    @ExceptionHandler
    public ResponseEntity<GeneralErrorResponse> handleException(PremiseNotFoundException exception) {
        GeneralErrorResponse error = new GeneralErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());   //400
        error.setMessage(exception.getMessage());
        error.setTime(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<GeneralErrorResponse> handleException(ResourceNotFoundException exception) {
        GeneralErrorResponse error = new GeneralErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());   //400
        error.setMessage(exception.getMessage());
        error.setTime(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
