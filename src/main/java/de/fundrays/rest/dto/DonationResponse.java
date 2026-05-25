package de.fundrays.rest.dto;

import de.fundrays.model.DonationStatus;

import java.time.Instant;
import java.util.UUID;

public record DonationResponse(
        UUID id,
        long amount,
        String currency,
        DonationStatus status,
        Instant createdAt
) {}
