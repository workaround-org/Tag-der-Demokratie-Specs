package de.fundrays.repository;

import de.fundrays.model.Campaign;
import de.fundrays.model.CampaignStatus;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class CampaignRepositoryTest {

    @Inject
    CampaignRepository campaignRepository;

    @Test
    void findBySlug_returnsMatchingCampaign() {
        // given
        campaignRepository.persist(aCampaign("spring-drive", CampaignStatus.ACTIVE));

        // when
        Optional<Campaign> result = campaignRepository.findBySlug("spring-drive");

        // then
        assertTrue(result.isPresent());
        assertEquals("spring-drive", result.get().slug);
    }

    @Test
    void findBySlug_returnsEmptyForUnknownSlug() {
        // given — no campaigns in DB

        // when
        Optional<Campaign> result = campaignRepository.findBySlug("nonexistent");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void findActive_returnsOnlyActiveCampaigns() {
        // given
        campaignRepository.persist(aCampaign("active-one", CampaignStatus.ACTIVE));
        campaignRepository.persist(aCampaign("paused-one", CampaignStatus.PAUSED));
        campaignRepository.persist(aCampaign("archived-one", CampaignStatus.ARCHIVED));

        // when
        List<Campaign> result = campaignRepository.findActive();

        // then
        assertEquals(1, result.size());
        assertEquals("active-one", result.get(0).slug);
    }

    @Test
    void findByStatus_returnsAllMatchingStatus() {
        // given
        campaignRepository.persist(aCampaign("completed-one", CampaignStatus.COMPLETED));
        campaignRepository.persist(aCampaign("completed-two", CampaignStatus.COMPLETED));
        campaignRepository.persist(aCampaign("active-one", CampaignStatus.ACTIVE));

        // when
        List<Campaign> result = campaignRepository.findByStatus(CampaignStatus.COMPLETED);

        // then
        assertEquals(2, result.size());
    }

    private Campaign aCampaign(String slug, CampaignStatus status) {
        Campaign c = new Campaign();
        c.slug = slug;
        c.title = "Campaign " + slug;
        c.goalAmount = 10000L;
        c.createdAt = Instant.now();
        c.status = status;
        return c;
    }
}
