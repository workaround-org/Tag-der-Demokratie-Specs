package de.fundrays.web;

import de.fundrays.model.Campaign;
import de.fundrays.model.CampaignStatus;
import de.fundrays.repository.CampaignRepository;
import de.fundrays.repository.DonationRepository;
import de.fundrays.service.CampaignService;
import de.fundrays.service.SlugConflictException;
import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/admin/campaigns")
@RolesAllowed("admin")
public class AdminCampaigns extends Controller {

    @Inject
    CampaignRepository campaignRepository;

    @Inject
    DonationRepository donationRepository;

    @Inject
    CampaignService campaignService;

    @CheckedTemplate
    static class Templates {
        static native TemplateInstance index(List<Campaign> campaigns, Map<UUID, Long> raisedByCampaign);
        static native TemplateInstance create();
        static native TemplateInstance edit(Campaign campaign, String deadlineValue);
    }

    @GET
    @Path("/")
    public TemplateInstance index() {
        List<Campaign> campaigns = campaignRepository.listAllOrdered();
        Map<UUID, Long> raisedByCampaign = campaigns.stream()
                .collect(Collectors.toMap(c -> c.id, c -> donationRepository.sumConfirmedByCampaignId(c.id)));
        return Templates.index(campaigns, raisedByCampaign);
    }

    @GET
    @Path("/new")
    public TemplateInstance create() {
        return Templates.create();
    }

    @POST
    @Path("/")
    @Transactional
    public void save(
            @NotBlank @RestForm String slug,
            @NotBlank @RestForm String title,
            @RestForm String description,
            @Positive @RestForm double goalAmountEuros,
            @RestForm String deadline,
            @RestForm String coverImageUrl) {

        if (validationFailed()) {
            create();
            return;
        }

        Campaign campaign = new Campaign();
        campaign.slug = slug.strip();
        campaign.title = title.strip();
        campaign.description = description;
        campaign.goalAmount = Math.round(goalAmountEuros * 100);
        campaign.deadline = parseDeadline(deadline);
        campaign.coverImageUrl = coverImageUrl;

        try {
            campaignService.create(campaign);
        } catch (SlugConflictException e) {
            flash("error", "Slug bereits vergeben: " + slug);
            create();
            return;
        }

        flash("message", "Kampagne erstellt.");
        index();
    }

    @GET
    @Path("/{slug}/edit")
    public TemplateInstance edit(@RestPath String slug) {
        Campaign campaign = campaignRepository.findBySlug(slug).orElse(null);
        notFoundIfNull(campaign);
        String deadlineValue = campaign.deadline != null
                ? campaign.deadline.atZone(ZoneOffset.UTC).toLocalDate().toString()
                : "";
        return Templates.edit(campaign, deadlineValue);
    }

    @POST
    @Path("/{slug}")
    @Transactional
    public void update(
            @RestPath String slug,
            @NotBlank @RestForm String title,
            @RestForm String description,
            @Positive @RestForm double goalAmountEuros,
            @RestForm String deadline,
            @RestForm String coverImageUrl,
            @RestForm String status) {

        if (validationFailed()) {
            edit(slug);
            return;
        }

        campaignService.update(slug, c -> {
            c.title = title.strip();
            c.description = description;
            c.goalAmount = Math.round(goalAmountEuros * 100);
            c.deadline = parseDeadline(deadline);
            c.coverImageUrl = coverImageUrl;
            if (status != null && !status.isBlank()) {
                c.status = CampaignStatus.valueOf(status);
            }
        });

        flash("message", "Kampagne aktualisiert.");
        index();
    }

    @POST
    @Path("/{slug}/archive")
    @Transactional
    public void archive(@RestPath String slug) {
        campaignService.update(slug, c -> c.status = CampaignStatus.ARCHIVED);
        flash("message", "Kampagne archiviert.");
        index();
    }

    @POST
    @Path("/{slug}/activate")
    @Transactional
    public void activate(@RestPath String slug) {
        campaignService.update(slug, c -> c.status = CampaignStatus.ACTIVE);
        flash("message", "Kampagne aktiviert.");
        index();
    }

    private static java.time.Instant parseDeadline(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
