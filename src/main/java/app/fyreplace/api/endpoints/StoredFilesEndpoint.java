package app.fyreplace.api.endpoints;

import app.fyreplace.api.services.MimeTypeService;
import app.fyreplace.api.services.StorageService;
import io.quarkus.arc.properties.UnlessBuildProperty;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("stored-files")
@UnlessBuildProperty(name = "app.storage.type", stringValue = "s3")
public final class StoredFilesEndpoint {
    @Inject
    StorageService storageService;

    @Inject
    MimeTypeService mimeTypeService;

    @GET
    @Path("{path:.*}")
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404")
    public Response retrieve(@PathParam("path") final String path) {
        try {
            final byte[] data;
            data = storageService.fetch(path);
            return Response.ok(data).type(mimeTypeService.getMimeType(data)).build();
        } catch (final IOException e) {
            throw new NotFoundException();
        }
    }
}
