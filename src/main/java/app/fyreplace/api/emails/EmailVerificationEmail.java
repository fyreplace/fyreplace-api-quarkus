package app.fyreplace.api.emails;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.Dependent;

@Dependent
public final class EmailVerificationEmail extends EmailBase {
    @Override
    protected String getAction() {
        return "connect";
    }

    @Override
    protected TemplateInstance textTemplate() {
        return Templates.text(getRandomCode(), getLink());
    }

    @Override
    protected TemplateInstance htmlTemplate() {
        return Templates.html(getRandomCode(), getLink());
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance text(String code, String link);

        public static native TemplateInstance html(String code, String link);
    }
}
