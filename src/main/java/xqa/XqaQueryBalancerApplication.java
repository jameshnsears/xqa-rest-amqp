package xqa;

import java.util.EnumSet;
import java.util.UUID;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.jdbi.v3.core.Jdbi;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.health.QueryBalancerHealthCheck;
import xqa.resources.SearchResource;
import xqa.resources.XQueryResource;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

public class XqaQueryBalancerApplication extends Application<XqaQueryBalancerConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(XqaQueryBalancerApplication.class);
    private final String serviceId;

    public XqaQueryBalancerApplication() {
        super();
        serviceId = "querybalancer/" + UUID.randomUUID().toString().split("-")[0];
    }

    public static void main(final String[] args) throws Exception {
        new XqaQueryBalancerApplication().run(args);
    }

    @Override
    public String getName() {
        return serviceId;
    }

    @Override
    public void initialize(final Bootstrap<XqaQueryBalancerConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(final XqaQueryBalancerConfiguration configuration, final Environment environment) throws Exception {
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        environment.healthChecks().register("QueryBalancerHealthCheck", new QueryBalancerHealthCheck());

        final JdbiFactory factory = new JdbiFactory();
        LOGGER.info(String.format("databaseUrl=%s", configuration.getDataSourceFactory().getUrl()));

        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");

        environment.jersey().register(new SearchResource(jdbi));
        environment.jersey().register(new XQueryResource(configuration.getMessageBrokerConfiguration(), serviceId));
    }
}
