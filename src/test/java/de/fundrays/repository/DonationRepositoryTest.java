package de.fundrays.repository;

import de.fundrays.model.Campaign;
import de.fundrays.model.CampaignStatus;
import de.fundrays.model.Donation;
import de.fundrays.model.DonationStatus;
import de.fundrays.model.PaymentMethod;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class DonationRepositoryTest {

    @Inject
    DonationRepository donationRepository;

    @Inject
    CampaignRepository campaignRepository;

    private Campaign campaign;

    @BeforeEach
    void setup() {
        donationRepository.deleteAll();
        campaignRepository.deleteAll();

        campaign = new Campaign();
        campaign.slug = "test-campaign";
        campaign.title = "Test Campaign";
        campaign.goalAmount = 100000L;
        campaign.createdAt = Instant.now();
        campaign.status = CampaignStatus.ACTIVE;
        campaignRepository.persist(campaign);
    }

    @Test
    void findByCampaignId_returnsOnlyDonationsForThatCampaign() {
        // given
        donationRepository.persist(aDonation(campaign, 500L, DonationStatus.CONFIRMED, "ref-1"));
        donationRepository.persist(aDonation(campaign, 1000L, DonationStatus.PENDING, "ref-2"));

        // when
        List<Donation> result = donationRepository.findByCampaignId(campaign.id);

        // then
        assertEquals(2, result.size());
    }

    @Test
    void findByProviderRef_returnsMatchingDonation() {
        // given
        donationRepository.persist(aDonation(campaign, 500L, DonationStatus.PENDING, "paypal-txn-42"));

        // when
        Optional<Donation> result = donationRepository.findByProviderRef("paypal-txn-42");

        // then
        assertTrue(result.isPresent());
        assertEquals(500L, result.get().amount);
    }

    @Test
    void findByProviderRef_returnsEmptyForUnknownRef() {
        // given — no donations in DB

        // when
        Optional<Donation> result = donationRepository.findByProviderRef("unknown-ref");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void sumConfirmedByCampaignId_sumsOnlyConfirmedDonations() {
        // given
        donationRepository.persist(aDonation(campaign, 1000L, DonationStatus.CONFIRMED, "ref-1"));
        donationRepository.persist(aDonation(campaign, 2000L, DonationStatus.CONFIRMED, "ref-2"));
        donationRepository.persist(aDonation(campaign, 500L, DonationStatus.PENDING, "ref-3"));

        // when
        long total = donationRepository.sumConfirmedByCampaignId(campaign.id);

        // then
        assertEquals(3000L, total);
    }

    @Test
    void sumConfirmedByCampaignId_returnsZeroWhenNoDonations() {
        // given — no donations in DB

        // when
        long total = donationRepository.sumConfirmedByCampaignId(campaign.id);

        // then
        assertEquals(0L, total);
    }

    @Test
    void countConfirmedByCampaignId_countsOnlyConfirmedDonations() {
        // given
        donationRepository.persist(aDonation(campaign, 1000L, DonationStatus.CONFIRMED, "ref-1"));
        donationRepository.persist(aDonation(campaign, 1000L, DonationStatus.FAILED, "ref-2"));
        donationRepository.persist(aDonation(campaign, 1000L, DonationStatus.CONFIRMED, "ref-3"));

        // when
        long count = donationRepository.countConfirmedByCampaignId(campaign.id);

        // then
        assertEquals(2, count);
    }

    private Donation aDonation(Campaign c, long amount, DonationStatus status, String providerRef) {
        Donation d = new Donation();
        d.campaign = c;
        d.amount = amount;
        d.status = status;
        d.paymentMethod = PaymentMethod.PAYPAL;
        d.paymentProviderRef = providerRef;
        d.createdAt = Instant.now();
        return d;
    }
}
