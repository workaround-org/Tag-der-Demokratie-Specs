package de.fundrays.donation.api;

import de.fundrays.donation.domain.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateDonationRequest(
	@Positive long amount,
	@NotNull PaymentMethod paymentMethod,
	String donorName,
	String donorEmail,
	String message)
{
}
