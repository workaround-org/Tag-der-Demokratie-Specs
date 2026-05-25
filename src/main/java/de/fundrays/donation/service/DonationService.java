package de.fundrays.donation.service;

import de.fundrays.campaign.domain.CampaignStatus;
import de.fundrays.campaign.service.CampaignNotActiveException;
import de.fundrays.campaign.service.CampaignNotFoundException;
import de.fundrays.campaign.repository.CampaignRepository;
import de.fundrays.donation.domain.Donation;
import de.fundrays.donation.repository.DonationRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;

@ApplicationScoped
public class DonationService
{

	@Inject
	CampaignRepository campaignRepository;

	@Inject
	DonationRepository donationRepository;

	@Transactional
	public Donation submit(String campaignSlug, Donation donation)
	{
		var campaign = campaignRepository.findBySlug(campaignSlug)
			.orElseThrow(() -> new CampaignNotFoundException(campaignSlug));
		if (campaign.status != CampaignStatus.ACTIVE)
		{
			throw new CampaignNotActiveException(campaignSlug);
		}
		donation.campaign = campaign;
		donation.createdAt = Instant.now();
		donationRepository.persist(donation);
		return donation;
	}
}
