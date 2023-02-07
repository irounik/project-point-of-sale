package com.increff.ironic.pos.spring;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.MessageData;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class AppRestControllerAdvice {

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageData handle(ApiException e) {
        MessageData data = new MessageData();
        data.setMessage(e.getMessage());
        return data;
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageData handle(Throwable e) {
        MessageData data = new MessageData();
        data.setMessage("An unknown error has occurred - " + e.getMessage());
        return data;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageData handleMismatch(MethodArgumentTypeMismatchException e) {
        MessageData data = new MessageData();
        data.setMessage("Invalid Input, please check argument type!");
        return data;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageData hande(HttpMessageNotReadableException ex) {
        JsonMappingException jme = (JsonMappingException) ex.getCause();
        MessageData data = new MessageData();
        data.setMessage(jme.getPath().get(0).getFieldName() + " provided is invalid");
        return data;
    }

}