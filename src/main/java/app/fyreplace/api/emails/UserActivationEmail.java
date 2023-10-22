package app.fyreplace.api.emails;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.Dependent;
import java.util.ResourceBundle;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public final class UserActivationEmail extends EmailBase {
    @ConfigProperty(name = "app.name")
    String appName;

    @Override
    protected String action() {
        return "connect";
    }

    @Override
    protected TemplateInstance textTemplate() {
        return Templates.text(getResourceBundle(), appName, getRandomCode(), getLink());
    }

    @Override
    protected TemplateInstance htmlTemplate() {
        return Templates.html(getResourceBundle(), appUrl, appName, getRandomCode(), getLink());
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance text(ResourceBundle res, String appName, String code, String link);

        public static native TemplateInstance html(
                ResourceBundle res, String appUrl, String appName, String code, String link);
    }
}
