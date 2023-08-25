package app.fyreplace.api.emails;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.Dependent;
import java.util.ResourceBundle;

@Dependent
public final class EmailVerificationEmail extends EmailBase {
    @Override
    protected String action() {
        return "email";
    }

    @Override
    protected TemplateInstance textTemplate() {
        return Templates.text(getResourceBundle(), getRandomCode(), getLink());
    }

    @Override
    protected TemplateInstance htmlTemplate() {
        return Templates.html(getResourceBundle(), getRandomCode(), getLink());
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance text(ResourceBundle res, String code, String link);

        public static native TemplateInstance html(ResourceBundle res, String code, String link);
    }
}
