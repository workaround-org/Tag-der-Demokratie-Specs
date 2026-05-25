package de.fundrays.campaign.api;

import de.fundrays.campaign.domain.CampaignStatus;

import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record UpdateCampaignRequest(
	String title,
	String description,
	@Positive Long goalAmount,
	String currency,
	Instant deadline,
	String coverImageUrl,
	CampaignStatus status)
{
}
