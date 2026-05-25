package de.fundrays.campaign.api;

import de.fundrays.campaign.domain.Campaign;
import de.fundrays.campaign.service.CampaignNotFoundException;
import de.fundrays.campaign.service.CampaignService;
import de.fundrays.campaign.service.SlugConflictException;
import de.fundrays.shared.ConflictException;
import org.jboss.resteasy.reactive.ResponseStatus;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/campaigns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CampaignResource
{

	@Inject
	CampaignService campaignService;

	@GET
	public List<CampaignResponse> listActive()
	{
		return campaignService.findActive().stream()
			.map(this::toResponse)
			.toList();
	}

	@GET
	@Path("/{slug}")
	public CampaignResponse getBySlug(@PathParam("slug") String slug)
	{
		return campaignService.findBySlug(slug)
			.map(this::toResponse)
			.orElseThrow(() -> new NotFoundException("Campaign not found: " + slug));
	}

	@PATCH
	@Path("/{slug}")
	@RolesAllowed("admin")
	public CampaignResponse update(@PathParam("slug") String slug, @Valid UpdateCampaignRequest request)
	{
		try
		{
			return toResponse(campaignService.update(slug, c -> {
				if (request.title() != null) c.title = request.title();
				if (request.description() != null) c.description = request.description();
				if (request.goalAmount() != null) c.goalAmount = request.goalAmount();
				if (request.currency() != null) c.currency = request.currency();
				if (request.deadline() != null) c.deadline = request.deadline();
				if (request.coverImageUrl() != null) c.coverImageUrl = request.coverImageUrl();
				if (request.status() != null) c.status = request.status();
			}));
		}
		catch (CampaignNotFoundException e)
		{
			throw new NotFoundException(e.getMessage());
		}
	}

	@POST
	@ResponseStatus(201)
	@RolesAllowed("admin")
	public CampaignResponse create(@Valid CreateCampaignRequest request)
	{
		Campaign campaign = new Campaign();
		campaign.slug = request.slug();
		campaign.title = request.title();
		campaign.description = request.description();
		campaign.goalAmount = request.goalAmount();
		if (request.currency() != null)
		{
			campaign.currency = request.currency();
		}
		campaign.deadline = request.deadline();
		campaign.coverImageUrl = request.coverImageUrl();

		try
		{
			return toResponse(campaignService.create(campaign));
		}
		catch (SlugConflictException e)
		{
			throw new ConflictException(e.getMessage());
		}
	}

	private CampaignResponse toResponse(Campaign c)
	{
		return new CampaignResponse(
			c.id, c.slug, c.title, c.description,
			c.goalAmount, c.currency, c.deadline, c.createdAt,
			c.status, c.coverImageUrl,
			campaignService.getRaisedAmount(c.id),
			campaignService.getDonationCount(c.id));
	}
}
