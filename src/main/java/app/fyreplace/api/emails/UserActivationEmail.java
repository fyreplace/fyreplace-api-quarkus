package app.fyreplace.api.emails;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public final class UserActivationEmail extends UserEmailBase {
    @Override
    protected String action() {
        return "connect";
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
        public static native TemplateInstance text(final TemplateCommonData d);

        public static native TemplateInstance html(final TemplateCommonData d);
    }
}
