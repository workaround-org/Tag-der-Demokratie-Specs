package de.fundrays.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record CreateCampaignRequest(
        @NotBlank String slug,
        @NotBlank String title,
        String description,
        @Positive long goalAmount,
        String currency,
        Instant deadline,
        String coverImageUrl
) {}
