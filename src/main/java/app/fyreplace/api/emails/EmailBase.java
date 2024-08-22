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
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public abstract class EmailBase extends Mail {
    @ConfigProperty(name = "app.name")
    String appName;

    @ConfigProperty(name = "app.url")
    URI appUrl;

    @ConfigProperty(name = "app.front.url")
    URI appFrontUrl;

    @ConfigProperty(name = "app.website.url")
    URI appWebsiteUrl;

    @Inject
    Mailer mailer;

    @Inject
    RandomService randomService;

    @Inject
    LocaleService localeService;

    private RandomCode code;

    private Email email;

    protected abstract String action();

    protected abstract TemplateInstance textTemplate();

    protected abstract TemplateInstance htmlTemplate();

    public void sendTo(final Email email) {
        this.email = email;
        var formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
        var date = getTemplateCommonData().expiration.atZone(ZoneId.systemDefault());
        mailer.send(this.setSubject(getResourceBundle().getString("subject"))
                .setHeaders(Map.of("Expires", Collections.singletonList(formatter.format(date))))
                .setText(textTemplate().render())
                .setHtml(htmlTemplate().render())
                .setTo(List.of(email.email)));
    }

    protected RandomCode getRandomCode() {
        if (code != null) {
            return code;
        }

        final var randomCode = new RandomCode();
        randomCode.email = email;
        randomCode.code = randomService.generateCode();
        randomCode.persist();
        code = randomCode;
        return code;
    }

    protected String getLink() {
        return UriBuilder.fromUri(appFrontUrl.toString())
                .queryParam("action", action())
                .fragment(email.email + ':' + getRandomCode())
                .build()
                .toString();
    }

    protected ResourceBundle getResourceBundle() {
        return localeService.getResourceBundle(this.getClass().getSimpleName());
    }

    protected TemplateCommonData getTemplateCommonData() {
        return new TemplateCommonData(getResourceBundle(), appName, appUrl, appWebsiteUrl, getRandomCode(), getLink());
    }

    public static final class TemplateCommonData {
        public final ResourceBundle res;
        public final String appName;
        public final URI appUrl;
        public final URI websiteUrl;
        public final RandomCode code;
        public final String link;
        public final Instant expiration = Instant.now().plus(RandomCode.lifetime);

        private TemplateCommonData(
                ResourceBundle res, String appName, URI appUrl, URI websiteUrl, RandomCode code, String link) {
            this.res = res;
            this.appName = appName;
            this.appUrl = appUrl;
            this.websiteUrl = websiteUrl;
            this.code = code;
            this.link = link;
        }
    }
}
