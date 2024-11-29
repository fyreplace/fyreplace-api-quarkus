package app.fyreplace.api.emails;

public abstract class UserEmailBase extends EmailBase {
    @Override
    protected String path() {
        return email.user.active ? "/login" : "/register";
    }
}
