package xqa.integration;

import static org.assertj.core.api.Assertions.fail;

import org.junit.ClassRule;
import org.junit.Test;

import io.dropwizard.testing.junit.DropwizardAppRule;
import xqa.XqaDbRestConfiguration;

public class HealthTest {
    @ClassRule
    public static final DropwizardAppRule<XqaDbRestConfiguration> RULE = TestSuite.RULE;

    @Test
    public void health() {
        fail("todo - use http://square.github.io/okhttp/ ?");
    }
}