package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.Report;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("reports")
public final class ReportsEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @GET
    @RolesAllowed("MODERATOR")
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    public Iterable<Report> listReports(@QueryParam("page") @PositiveOrZero final int page) {
        return Report.findAll(Report.sorting()).page(page, pagingSize).list();
    }
}
