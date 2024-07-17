package app.fyreplace.api.filters;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;

@SuppressWarnings("unused")
@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
public final class OpenAPIFilter implements OASFilter {
    @Override
    public Operation filterOperation(final Operation operation) {
        operation
                .getResponses()
                .addAPIResponse("default", OASFactory.createAPIResponse().description("Unexpected error"));
        return operation;
    }
}
