package app.fyreplace.api.emails;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public final class UserActivationEmail extends EmailBase {
    @ConfigProperty(name = "app.name")
    String appName;

    @Override
    protected String getAction() {
        return "connect";
    }

    @Override
    protected TemplateInstance textTemplate() {
        return Templates.text(appName, getRandomCode(), getLink());
    }

    @Override
    protected TemplateInstance htmlTemplate() {
        return Templates.html(appName, getRandomCode(), getLink());
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance text(String appName, String code, String link);

        public static native TemplateInstance html(String appName, String code, String link);
    }
}
