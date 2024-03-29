package app.fyreplace.api.emails;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.services.LocaleService;
import app.fyreplace.api.services.RandomService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.ResourceBundle;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public abstract class EmailBase extends Mail {
    @ConfigProperty(name = "app.url")
    String appUrl;

    @ConfigProperty(name = "app.front.url")
    String appFrontUrl;

    @Inject
    Mailer mailer;

    @Inject
    RandomService randomService;

    @Inject
    LocaleService localeService;

    private String code;

    private Email email;

    protected abstract String action();

    protected abstract TemplateInstance textTemplate();

    protected abstract TemplateInstance htmlTemplate();

    public void sendTo(final Email email) {
        this.email = email;
        mailer.send(this.setSubject(getResourceBundle().getString("subject"))
                .setText(textTemplate().render())
                .setHtml(htmlTemplate().render())
                .setTo(List.of(email.email)));
    }

    protected String getRandomCode() {
        if (code != null) {
            return code;
        }

        final var randomCode = new RandomCode();
        randomCode.email = email;
        randomCode.code = randomService.generateCode();
        randomCode.persist();
        code = randomCode.toString();
        return code;
    }

    protected String getLink() {
        return UriBuilder.fromUri(appFrontUrl)
                .queryParam("action", action())
                .fragment(email.email + ':' + getRandomCode())
                .build()
                .toString();
    }

    protected ResourceBundle getResourceBundle() {
        return localeService.getResourceBundle(this.getClass().getSimpleName());
    }
}
