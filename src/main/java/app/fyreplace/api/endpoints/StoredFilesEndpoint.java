package app.fyreplace.api.endpoints;

import app.fyreplace.api.services.MimeTypeService;
import app.fyreplace.api.services.StorageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("stored-files")
public final class StoredFilesEndpoint {
    @ConfigProperty(name = "app.url")
    String appUrl;

    @Inject
    StorageService storageService;

    @Inject
    MimeTypeService mimeTypeService;

    @GET
    @Path("{path:.*}")
    @Operation(hidden = true)
    public Response getStoredFile(@PathParam("path") final String path) throws URISyntaxException {
        final var appUri = new URI(appUrl);
        final var requestUri = storageService.getUri(path);

        if (!appUri.getHost().equals(requestUri.getHost())) {
            throw new RedirectionException(Status.SEE_OTHER, requestUri);
        }

        try {
            final byte[] data;
            data = storageService.fetch(path);
            return Response.ok(data).type(mimeTypeService.getMimeType(data)).build();
        } catch (final IOException e) {
            throw new NotFoundException();
        }
    }
}
