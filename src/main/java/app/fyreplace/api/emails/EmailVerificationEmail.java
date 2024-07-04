package app.fyreplace.api.emails;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.Dependent;

@Dependent
public final class EmailVerificationEmail extends EmailBase {
    @Override
    protected String action() {
        return "email";
    }

    @Override
    protected TemplateInstance textTemplate() {
        return Templates.text(getTemplateCommonData());
    }

    @Override
    protected TemplateInstance htmlTemplate() {
        return Templates.html(getTemplateCommonData());
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance text(TemplateCommonData d);

        public static native TemplateInstance html(TemplateCommonData d);
    }
}
