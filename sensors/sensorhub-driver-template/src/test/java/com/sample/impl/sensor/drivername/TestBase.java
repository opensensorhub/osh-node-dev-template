package com.sample.impl.sensor.drivername;

import org.junit.After;
import org.junit.Before;

/**
 * Base class for unit tests which initializes the sensor before each test and cleans up after.
 */
public class TestBase {
    Sensor sensor;
    Output output;

    @Before
    public void init() throws Exception {
        Config config = new Config();
        config.serialNumber = "123456789";
        config.name = "Sensor Template";
        config.description = "Description of the sensor";
        sensor = new Sensor();
        sensor.init(config);
        sensor.start();
        output = sensor.output;
    }

    @After
    public void cleanup() throws Exception {
        if (null != sensor) {
            sensor.stop();
        }
    }
}
