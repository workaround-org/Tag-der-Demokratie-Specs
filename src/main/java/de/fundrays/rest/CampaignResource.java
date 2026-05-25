package de.fundrays.rest;

import de.fundrays.model.Campaign;
import de.fundrays.rest.dto.CampaignResponse;
import de.fundrays.rest.dto.CreateCampaignRequest;
import de.fundrays.service.CampaignService;
import de.fundrays.service.SlugConflictException;
import org.jboss.resteasy.reactive.ResponseStatus;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/campaigns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CampaignResource {

    @Inject
    CampaignService campaignService;

    @GET
    public List<CampaignResponse> listActive() {
        return campaignService.findActive().stream()
                .map(this::toResponse)
                .toList();
    }

    @GET
    @Path("/{slug}")
    public CampaignResponse getBySlug(@PathParam("slug") String slug) {
        return campaignService.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Campaign not found: " + slug));
    }

    @POST
    @ResponseStatus(201)
    @RolesAllowed("admin")
    public CampaignResponse create(@Valid CreateCampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.slug = request.slug();
        campaign.title = request.title();
        campaign.description = request.description();
        campaign.goalAmount = request.goalAmount();
        if (request.currency() != null) {
            campaign.currency = request.currency();
        }
        campaign.deadline = request.deadline();
        campaign.coverImageUrl = request.coverImageUrl();

        try {
            return toResponse(campaignService.create(campaign));
        } catch (SlugConflictException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    private CampaignResponse toResponse(Campaign c) {
        return new CampaignResponse(
                c.id, c.slug, c.title, c.description,
                c.goalAmount, c.currency, c.deadline, c.createdAt,
                c.status, c.coverImageUrl,
                campaignService.getRaisedAmount(c.id),
                campaignService.getDonationCount(c.id));
    }
}
