package de.fundrays.rest;

import de.fundrays.rest.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class UnprocessableEntityException extends WebApplicationException {

    public UnprocessableEntityException(String message) {
        super(Response.status(422)
                .entity(new ErrorResponse(message))
                .type(MediaType.APPLICATION_JSON)
                .build());
    }
}
