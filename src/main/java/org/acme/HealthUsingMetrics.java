package org.acme;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Metered;

@Path("/ping")
public class HealthUsingMetrics {
    @Inject
    @ConfigProperty(name="delay")
    Optional<Integer> delay;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timeout(value = 500)
    @Fallback(fallbackMethod = "pingTimeout")
    @Metered(absolute = true, name="ping")
    public String ping() {
        try {
            int delayTime = (int)(Math.random()*delay.orElse(1000));
            System.out.println("** Waiting " + delayTime + "ms **");
            TimeUnit.MILLISECONDS.sleep(delayTime);
        } catch (InterruptedException ex) {
        }

        return "** Pong **";
    }

    @Metered(absolute = true, name="fallbackPing")
    public String pingTimeout() {
        return "** timeout **";
    }
}