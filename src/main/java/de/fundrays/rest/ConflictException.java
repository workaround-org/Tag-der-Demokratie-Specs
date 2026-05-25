package de.fundrays.rest;

import de.fundrays.rest.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ConflictException extends WebApplicationException {

    public ConflictException(String message) {
        super(Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(message))
                .type(MediaType.APPLICATION_JSON)
                .build());
    }
}
