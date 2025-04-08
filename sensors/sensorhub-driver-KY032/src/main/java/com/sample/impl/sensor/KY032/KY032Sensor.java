/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2020-2021 Botts Innovative Research, Inc. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/
package com.sample.impl.sensor.KY032;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ADDED IMPORT FOR MODULES

// myNote: Added for KY-032 Sensor
// PI4J DEPENDENCIES
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalState;


/**
 * Sensor driver providing sensor description, output registration, initialization and shutdown of driver and outputs.
 *
 * @author your_name
 * @since date
 */
public class KY032Sensor extends AbstractSensorModule<KY032Config> {

    private static final Logger logger = LoggerFactory.getLogger(KY032Sensor.class);

    KY032Output output;

    // myNote:
    // PI4J VARIABLES:
    Context pi4j;
    DigitalInput pi4jInput;

    @Override
    protected void updateSensorDescription() {
        synchronized (sensorDescLock){              // synchronized function provides for thread synchronization, ensuring that one thread can execute function at a time
            super.updateSensorDescription();
            sensorDescription.setDescription("This shall be my modified description!");

            if (!sensorDescription.isSetDescription()) {
                sensorDescription.setDescription("My Set Description in Sensor.Java");
            }
        }
    }

    @Override
    public void doInit() throws SensorHubException {

        super.doInit();
        System.out.println("Sensor Initialized...");
        // Generate identifiers
        generateUniqueID("[URN]", config.serialNumber);
        //generateUniqueID("[URN]", "This is a test");
        generateXmlID("[XML-PREFIX]", config.serialNumber);


        // TODO: Perform other initialization
        // myNote:
        // initalize pi4jContext
        System.out.println("Creating Sensor...");

        // Create Pi4J Context
        pi4j = Pi4J.newAutoContext();
        // Create a DigitalInput Configuration
        DigitalInputConfig inputConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("sensor")
                .name("Obstacle Sensor")
                .address(config.GPIO_BCM_NUMBER)
                .build();

        // CREATE A DigitalInput Instance using inputConfig configuration to store the Raspberry Pi's input
        pi4jInput = pi4j.create(inputConfig);
        System.out.println("pi4j configuration complete...");

        // Create and initialize output
        output = new KY032Output(this);
        addOutput(output, false);
        output.doInit();
    }

    @Override
    public void doStart() throws SensorHubException {
        System.out.println("Sensor has been started...");
        if (null != output) {
            // Allocate necessary resources and start outputs
            //output.doStart(pi4jInput);
            System.out.println("Listening to Sensor...");
            pi4jInput.addListener(e -> {
                System.out.println(e.state());
                // This may be a way to pass the state over
                output.doStart(e.state() == DigitalState.LOW);
            });
    }

        // TODO: Perform other startup procedures

    }

@Override
    public void doStop() throws SensorHubException {

        if (null != output) {
            output.doStop();
        }
        // TODO: Perform other shutdown procedures
        pi4j.shutdown();
        System.out.println("pi4j instance has been terminated...");

    }

    @Override
    public boolean isConnected() {

        // Determine if sensor is connected
        return output.isAlive();
    }

}
