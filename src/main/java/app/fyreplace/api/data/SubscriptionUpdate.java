package app.fyreplace.api.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record SubscriptionUpdate(@Schema(required = true) boolean subscribed) {}
