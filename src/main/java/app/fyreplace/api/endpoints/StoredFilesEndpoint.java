package app.fyreplace.api.endpoints;

import app.fyreplace.api.services.ImageService;
import app.fyreplace.api.services.StorageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("stored-files")
public final class StoredFilesEndpoint {
    @Inject
    StorageService storageService;

    @Inject
    ImageService imageService;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("{path:.*}")
    @Operation(hidden = true)
    public Response getStoredFile(@PathParam("path") final String path) throws URISyntaxException {
        final var pathUri = storageService.getUri(path);

        if (!uriInfo.getBaseUri().getHost().equals(pathUri.getHost())) {
            throw new RedirectionException(Status.SEE_OTHER, pathUri);
        }

        try {
            final byte[] data;
            data = storageService.fetch(path);
            return Response.ok(data).type(imageService.getMimeType(data)).build();
        } catch (final IOException e) {
            throw new NotFoundException();
        }
    }
}
