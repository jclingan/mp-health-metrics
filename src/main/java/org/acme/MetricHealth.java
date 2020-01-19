package org.acme;

import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricRegistry.Type;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

@Liveness
public class MetricHealth implements HealthCheck {
    @Inject
    @RegistryType(type = Type.APPLICATION)
    MetricRegistry registry;

    float getFallbackRatio() {
        float fallbackCount = registry.getMeters().get(new MetricID("ping")).getCount();
        float pingCount = registry.getMeters().get(new MetricID("fallbackPing")).getCount();

        if (pingCount == 0f) {
            return 0f;
        }
        return fallbackCount / pingCount;
    }

    @Override
    public HealthCheckResponse call() {
        String message = "";
        float fallbackRatio = 0f;
        boolean state;

        float pingCount = registry.getMeters().get(new MetricID("ping")).getCount();
        float fallbackCount = registry.getMeters().get(new MetricID("fallbackPing")).getCount();

        if (pingCount == 0) {
            fallbackRatio = 1.0f;
        } else {
            fallbackRatio = fallbackCount / pingCount;
        }

        if (fallbackRatio > .6f) {
            message = "Fallback Count (Too many)";
            state = false;
        } else {
            message = "Fallback Count (ok)";
            state = true;
        }

        return HealthCheckResponse.named("MetricsHealthCheck")
            .state(state)
            .withData("pingCount", "" + pingCount)
            .withData("fallbackCount", "" + fallbackCount)
            .withData("ratio", "" + fallbackRatio)
            .withData("message", message)
            .build();
    }

}