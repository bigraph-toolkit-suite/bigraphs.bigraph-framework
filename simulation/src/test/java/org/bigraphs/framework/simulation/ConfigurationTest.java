/*
 * Copyright (c) 2026 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.simulation;

import static org.junit.jupiter.api.Assertions.*;

import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Dominik Grzelak
 */
@SpringBootTest(classes = ConfigurationTest.TestConfig.class)
@TestPropertySource("classpath:modelchecking-test.properties")
class ConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "model-checking.measure-time=true",
                    "model-checking.transition-options.maximum-transitions=1234",
                    "model-checking.export-options.print-canonical-state-label=false"
            );

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration.class
    })

    @Configuration
    @EnableConfigurationProperties(ModelCheckingOptions.class)
    static class TestConfig {
    }

    @Test
    void test_bind_config_properties() {
        contextRunner.run(ctx -> {
            ModelCheckingOptions opts = ctx.getBean(ModelCheckingOptions.class);
            System.out.println(opts);
            assertTrue(opts.isMeasureTime());
            ModelCheckingOptions.TransitionOptions tOpts = opts.get(ModelCheckingOptions.Options.TRANSITION);
            ModelCheckingOptions.ExportOptions eOpts = opts.get(ModelCheckingOptions.Options.EXPORT);
            assertEquals(1234, tOpts.getMaximumTransitions());
            assertFalse(eOpts.getPrintCanonicalStateLabel());
        });
    }

    @Autowired
    private ModelCheckingOptions modelCheckingOptions;

    @Test
    void test_configuration() {
        assertTrue(modelCheckingOptions.isMeasureTime());
        ModelCheckingOptions.TransitionOptions tOpts = modelCheckingOptions.get(ModelCheckingOptions.Options.TRANSITION);
        ModelCheckingOptions.ExportOptions eOpts = modelCheckingOptions.get(ModelCheckingOptions.Options.EXPORT);
        assertEquals(1309, tOpts.getMaximumTransitions());
        assertFalse(eOpts.getPrintCanonicalStateLabel());
    }
}