package com.example.cms.ResponseHandler;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
public class BaseResponse {

     private int statusCode;
     private String statusMessage;
     private Object response;
     private Object error;

     // Constructors
     public BaseResponse() {}

     public BaseResponse(int statusCode, String statusMessage, Object response, Object error) {
          this.statusCode = statusCode;
          this.statusMessage = statusMessage;
          this.response = response;
          this.error = error;
     }

     // Getters and Setters
     public int getStatusCode() {
          return statusCode;
     }

     public void setStatusCode(int statusCode) {
          this.statusCode = statusCode;
     }

     public String getStatusMessage() {
          return statusMessage;
     }

     public void setStatusMessage(String statusMessage) {
          this.statusMessage = statusMessage;
     }

     public Object getResponse() {
          return response;
     }

     public void setResponse(Object response) {
          this.response = response;
     }

     public Object getError() {
          return error;
     }

     public void setError(Object error) {
          this.error = error;
     }

     public BaseResponse success(Object response) {
          return new BaseResponse(200, "OK", response, null);
     }

     public BaseResponse failure(Object error,String statusMessage, int code) {
          return new BaseResponse(code, statusMessage, null, error);
     }

     public BaseResponse error(int statusCode, String statusMessage, Object error) {
          return new BaseResponse(statusCode, statusMessage, null, error);
     }
}
