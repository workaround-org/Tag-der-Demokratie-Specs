package de.fundrays.rest;

import de.fundrays.model.Donation;
import de.fundrays.rest.dto.CreateDonationRequest;
import de.fundrays.rest.dto.DonationResponse;
import de.fundrays.service.CampaignNotActiveException;
import de.fundrays.service.CampaignNotFoundException;
import de.fundrays.service.DonationService;
import org.jboss.resteasy.reactive.ResponseStatus;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/campaigns/{slug}/donations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DonationResource {

    @Inject
    DonationService donationService;

    @POST
    @ResponseStatus(201)
    public DonationResponse submit(@PathParam("slug") String slug, @Valid CreateDonationRequest request) {
        Donation donation = new Donation();
        donation.amount = request.amount();
        donation.paymentMethod = request.paymentMethod();
        donation.donorName = request.donorName();
        donation.donorEmail = request.donorEmail();
        donation.message = request.message();

        try {
            return toResponse(donationService.submit(slug, donation));
        } catch (CampaignNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (CampaignNotActiveException e) {
            throw new UnprocessableEntityException(e.getMessage());
        }
    }

    private DonationResponse toResponse(Donation d) {
        return new DonationResponse(d.id, d.amount, d.currency, d.status, d.createdAt);
    }
}
