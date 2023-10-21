package app.fyreplace.api.pushnotifications;

import app.fyreplace.api.data.Comment;

public interface PushNotificationDispatcher {
    void dispatch(final Comment comment);
}
