package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.Report;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("reports")
public final class ReportsEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @GET
    @RolesAllowed("MODERATOR")
    @APIResponse(responseCode = "200")
    public Iterable<Report> list(@QueryParam("page") @PositiveOrZero final int page) {
        return Report.findAll(Report.sorting()).page(page, pagingSize).list();
    }
}
