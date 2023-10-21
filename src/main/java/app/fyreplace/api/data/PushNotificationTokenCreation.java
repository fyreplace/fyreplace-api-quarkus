package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PushNotificationTokenCreation(@NotNull PushNotificationToken.Service service, @NotBlank String token) {}
