package de.fundrays.rest.dto;

import de.fundrays.model.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateDonationRequest(
        @Positive long amount,
        @NotNull PaymentMethod paymentMethod,
        String donorName,
        String donorEmail,
        String message
) {}
