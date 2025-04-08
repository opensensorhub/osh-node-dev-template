package com.sample.impl.sensor.drivername;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SensorTest extends TestBase {
    @Test
    public void testSensor() {
        assertTrue(sensor.isStarted());
        assertTrue(sensor.isConnected());

        assertEquals(Sensor.UID_PREFIX + "123456789", sensor.getUniqueIdentifier());
        assertEquals("Sensor Template", sensor.getName());
        assertEquals("Description of the sensor", sensor.getDescription());
    }
}
