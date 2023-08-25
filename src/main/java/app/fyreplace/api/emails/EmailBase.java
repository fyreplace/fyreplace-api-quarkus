package app.fyreplace.api.emails;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.services.LocaleService;
import app.fyreplace.api.services.RandomService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

public abstract class EmailBase extends Mail {
    @ConfigProperty(name = "app.url")
    String appUrl;

    @Inject
    Mailer mailer;

    @Inject
    RandomService randomService;

    @Inject
    LocaleService localeService;

    private String code;

    private Email email;

    private final Logger logger = Logger.getLogger(this.getClass());

    protected abstract String action();

    protected abstract TemplateInstance textTemplate();

    protected abstract TemplateInstance htmlTemplate();

    @Blocking
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
        try {
            final var url = new URL(new URL(appUrl), "?action=" + action());
            return String.format("%s#%s:%s", url, email.user.username, getRandomCode());
        } catch (final MalformedURLException e) {
            logger.error("Could not generate link", e);
            return null;
        }
    }

    protected ResourceBundle getResourceBundle() {
        return localeService.getResourceBundle(this.getClass().getSimpleName());
    }
}
