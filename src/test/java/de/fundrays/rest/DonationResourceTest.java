package de.fundrays.rest;

import de.fundrays.model.Campaign;
import de.fundrays.model.CampaignStatus;
import de.fundrays.repository.CampaignRepository;
import de.fundrays.repository.DonationRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class DonationResourceTest {

    @Inject
    CampaignRepository campaignRepository;

    @Inject
    DonationRepository donationRepository;

    @BeforeEach
    void setup() {
        QuarkusTransaction.requiringNew().run(() -> {
            donationRepository.deleteAll();
            campaignRepository.deleteAll();
        });
    }

    @Test
    void submit_createsDonationWithPendingStatus() {
        // given
        QuarkusTransaction.requiringNew().run(() ->
                campaignRepository.persist(aCampaign("active-campaign", CampaignStatus.ACTIVE)));
        var body = """
                {"amount":1500,"paymentMethod":"PAYPAL"}
                """;

        // when
        var response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/campaigns/active-campaign/donations");

        // then
        response.then().statusCode(201)
                .body("id", notNullValue())
                .body("amount", equalTo(1500))
                .body("status", equalTo("PENDING"));
    }

    @Test
    void submit_returns404ForUnknownCampaign() {
        // given — no campaigns in DB
        var body = """
                {"amount":1000,"paymentMethod":"STRIPE"}
                """;

        // when
        var response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/campaigns/nonexistent/donations");

        // then
        response.then().statusCode(404);
    }

    @Test
    void submit_returns422ForInactiveCampaign() {
        // given
        QuarkusTransaction.requiringNew().run(() ->
                campaignRepository.persist(aCampaign("paused-campaign", CampaignStatus.PAUSED)));
        var body = """
                {"amount":1000,"paymentMethod":"PAYPAL"}
                """;

        // when
        var response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/campaigns/paused-campaign/donations");

        // then
        response.then().statusCode(422)
                .body("message", notNullValue());
    }

    @Test
    void submit_returns400ForNonPositiveAmount() {
        // given — amount of 0 fails @Positive validation
        var body = """
                {"amount":0,"paymentMethod":"PAYPAL"}
                """;

        // when
        var response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/campaigns/any-campaign/donations");

        // then
        response.then().statusCode(400);
    }

    private Campaign aCampaign(String slug, CampaignStatus status) {
        Campaign c = new Campaign();
        c.slug = slug;
        c.title = "Campaign " + slug;
        c.goalAmount = 100000L;
        c.createdAt = Instant.now();
        c.status = status;
        return c;
    }
}
