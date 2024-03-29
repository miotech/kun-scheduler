package com.miotech.kun.commons.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ExtendWith(SystemStubsExtension.class)
@PrepareForTest(EnvironmentUtils.class)
public class PropsUtilsTest {

    @SystemStub
    public EnvironmentVariables environmentVariables
            = new EnvironmentVariables();

    @Test
    public void loadPropsFromEnvWithKunPrefix_shouldContains() {
        //prepare env
        environmentVariables.set("KUN_INFRA_TEST_ENV1", "value1");
        environmentVariables.set("KUN_INFRA_TEST_ENV2", "value2");
        Props props = PropsUtils.loadPropsFromEnv("INFRA");

        assertThat(props.get("test.env1"), is("value1"));
        assertThat(props.get("test.env2"), is("value2"));
    }

    @Test
    public void loadPropsFromEnvWithoutKunPrefix_shouldNotContains() {
        //prepare env
        environmentVariables.set("KUN_INFRA_TEST_ENV1", "value1");
        environmentVariables.set("SYSTEM_TEST_ENV2", "value2");
        Props props = PropsUtils.loadPropsFromEnv("INFRA");

        assertThat(props.get("test.env1"), is("value1"));
        assertThat(props.containsKey("system.test.env2"), is(false));
    }

    @Test
    public void loadMutiProps_priorityShouldInOrder() {
        //prepare env
        environmentVariables.set("KUN_INFRA_TESTSECTION1_TESTKEY1", "envValue1");
        environmentVariables.set("KUN_INFRA_TESTSECTION1_TESTKEY2", "envValue2");
        environmentVariables.set("KUN_INFRA_TESTSECTION2_TESTKEY4", "envValue3");
        Props props = PropsUtils.loadAppProps("application-test.yaml");
        props.addProps(PropsUtils.loadPropsFromEnv("INFRA"));

        assertThat(props.get("testSection1.testKey1"), is("envValue1"));
        assertThat(props.get("testSection1.testKey2"), is("envValue2"));
        assertThat(props.get("testSection2.testKey3"), is("fileValue3"));
        assertThat(props.get("testSection2.testKey4"), is("envValue3"));

    }
}
