// THIS CLASS IS NO LONGER NEEDED. THIS CLASS WAS INITIALLY CRATED AS
// A STANDALONE APPLICATION JUST TO GET THE KY032 SENSOR WORKING
// PROGRESSION OF OSH-NODE APP INITIALLY IMPORTED THIS INTO OUTPUT
package com.sample.impl.sensor.KY032;

// PI4J DEPENDENCIES
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;

public class KY032 {
    private final Context pi4j;
    private final DigitalInput input;

    // CONSTRUCTOR
    // Configure GPIO pin (physical pin 16 = BCM pin 23)
    public KY032(int BCM_GPIO_PIN){
        System.out.println("Creating Sensor...");

        // Create Pi4J Context
        this.pi4j = Pi4J.newAutoContext();
        // Create a DigitalInput Configuration
        DigitalInputConfig inputConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("sensor")
                .name("Obstacle Sensor")
                .address(BCM_GPIO_PIN)
                .build();

        // CREATE A DigitalInput Instance using inputConfig configuration to store the Raspberry Pi's input
        this.input = pi4j.create(inputConfig);
    }

    // READ SENSOR VALUE.
    public boolean readSensor() {
        // Read the input of the GPIO signal (output of the KY032 Sensor)
        // In Transistor-Transistor Logic (TTL), a high signal is typically represented by a voltage between 2.0 and 5.0 volts,
        // while a low signal is represented by a voltage between 0.0 and 0.8 volts
        //return input.isHigh();
        return input.addListener().isHigh();

    }

    // END pi4j
    public String shutDownSensor() {
        pi4j.shutdown();
        return "pi4j instance has been shutdown";
    }

}
