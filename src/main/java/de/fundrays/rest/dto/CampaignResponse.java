package de.fundrays.rest.dto;

import de.fundrays.model.CampaignStatus;

import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        String slug,
        String title,
        String description,
        long goalAmount,
        String currency,
        Instant deadline,
        Instant createdAt,
        CampaignStatus status,
        String coverImageUrl,
        long raisedAmount,
        long donationCount
) {}
