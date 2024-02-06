package org.bigraphs.framework.simulation;

import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Dominik Grzelak
 */
@SpringBootTest(classes = ModelCheckingOptions.class)
@ExtendWith(SpringExtension.class) // for junit5, no RunWith necessary
@TestPropertySource("classpath:modelchecking-test.properties")
@EnableAutoConfiguration
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
