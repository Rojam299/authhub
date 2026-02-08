package ai.mailhub.authhub.adapter.out.exception;

import lombok.Getter;

/**
 * If an exception mentions a provider, protocol, HTTP, or external system → adapter/out
 */
@Getter
public class ProviderTokenRefreshException extends RuntimeException{

    private final String provider ;
    private final int statusCode ;


    public ProviderTokenRefreshException(String provider, int statusCode, String message){
        super(message) ;
        this.provider=provider ;
        this.statusCode=statusCode ;
    }



}
