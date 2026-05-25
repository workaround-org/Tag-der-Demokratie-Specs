package de.fundrays.service;

public class CampaignNotActiveException extends RuntimeException {

    public CampaignNotActiveException(String slug) {
        super("Campaign is not accepting donations: " + slug);
    }
}