/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


/**
 * @author Dominik Grzelak
 */
@SpringBootTest(classes = ModelCheckingOptions.class)
@ExtendWith(SpringExtension.class) // for junit5, no RunWith necessary
@TestPropertySource("classpath:modelchecking-test.properties")
@EnableAutoConfiguration
@Disabled
public class ConfigurationTest {

    @Autowired
    private ModelCheckingOptions modelCheckingOptions;

    @Test
    public void name() {
        assertTrue(modelCheckingOptions.isMeasureTime());
        ModelCheckingOptions.TransitionOptions opts = modelCheckingOptions.get(ModelCheckingOptions.Options.TRANSITION);
        ModelCheckingOptions.ExportOptions optsExport = modelCheckingOptions.get(ModelCheckingOptions.Options.EXPORT);
        assertEquals(1309, opts.getMaximumTransitions());
        assertFalse(optsExport.getPrintCanonicalStateLabel());

    }
}
