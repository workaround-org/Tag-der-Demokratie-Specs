package de.fundrays.repository;

import de.fundrays.model.Donation;
import de.fundrays.model.DonationStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DonationRepository implements PanacheRepository<Donation> {

    public List<Donation> findByCampaignId(UUID campaignId) {
        return list("campaign.id", campaignId);
    }

    public List<Donation> findByCampaignIdAndStatus(UUID campaignId, DonationStatus status) {
        return list("campaign.id = ?1 and status = ?2", campaignId, status);
    }

    public Optional<Donation> findByProviderRef(String paymentProviderRef) {
        return find("paymentProviderRef", paymentProviderRef).firstResultOptional();
    }

    /** Total confirmed amount in cents for a campaign */
    public long sumConfirmedByCampaignId(UUID campaignId) {
        Long result = getEntityManager()
                .createQuery("select sum(d.amount) from Donation d where d.campaign.id = ?1 and d.status = ?2", Long.class)
                .setParameter(1, campaignId)
                .setParameter(2, DonationStatus.CONFIRMED)
                .getSingleResult();
        return result == null ? 0L : result;
    }

    public long countConfirmedByCampaignId(UUID campaignId) {
        return count("campaign.id = ?1 and status = ?2", campaignId, DonationStatus.CONFIRMED);
    }
}
