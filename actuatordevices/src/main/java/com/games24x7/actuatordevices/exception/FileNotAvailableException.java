package com.games24x7.actuatordevices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;


public class FileNotAvailableException extends RuntimeException
{
    public FileNotAvailableException(String message)
    {
        super(message);
    }
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
