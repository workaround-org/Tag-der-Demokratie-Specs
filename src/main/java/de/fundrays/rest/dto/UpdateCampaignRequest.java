package de.fundrays.rest.dto;

import de.fundrays.model.CampaignStatus;

import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record UpdateCampaignRequest(
        String title,
        String description,
        @Positive Long goalAmount,
        String currency,
        Instant deadline,
        String coverImageUrl,
        CampaignStatus status
) {}
