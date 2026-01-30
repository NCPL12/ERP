package com.ncpl.sales.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ncpl.sales.model.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{
	 @InitBinder
	    public void initBinder(WebDataBinder dataBinder) {
	        dataBinder.setAutoGrowCollectionLimit(500);
	    }
	
	@ExceptionHandler(value= {Exception.class}) 
	public ResponseEntity<?> handleErrors(Exception e){ 
		e.printStackTrace(); 
		ErrorResponse errorRexpo = new ErrorResponse();
		errorRexpo.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		errorRexpo.setErrorMessage(e.getMessage());
		return new ResponseEntity<ErrorResponse>(errorRexpo, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
