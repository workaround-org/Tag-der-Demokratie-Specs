package de.fundrays.service;

public class CampaignNotFoundException extends RuntimeException {

    public CampaignNotFoundException(String slug) {
        super("Campaign not found: " + slug);
    }
}