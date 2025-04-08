/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2020-2021 Botts Innovative Research, Inc. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/
package com.sample.impl.sensor.drivername;

import net.opengis.sensorml.v20.PhysicalSystem;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.sensorML.SMLHelper;
import org.vast.swe.SWEHelper;


/**
 * Sensor driver providing sensor description, output registration, initialization and shutdown of driver and outputs.
 *
 * @author your_name
 * @since date
 */
public class Sensor extends AbstractSensorModule<Config> {

    private static final Logger logger = LoggerFactory.getLogger(Sensor.class);

    Output output;

    @Override
    protected void updateSensorDescription() {
        synchronized (sensorDescLock){
            super.updateSensorDescription();
            if (!sensorDescription.isSetDescription()) {
                sensorDescription.setDescription("A simulated sensor for training purposes, "+
                        "demonstrating how to build a driver.");

                SMLHelper smlHelper = new SMLHelper();
                smlHelper.edit((PhysicalSystem)sensorDescription)
                        .addIdentifier(smlHelper.identifiers.serialNumber( "1234567890"))
                        .addClassifier(smlHelper.classifiers.sensorType("Simulated Sensor Platform"))
                        .addCharacteristicList("operating_specs", smlHelper.characteristics.operatingCharacteristics()
                                .add("Voltage",smlHelper.characteristics.operatingVoltageRange(3.3,5,"V"))
                                .add("temperature",smlHelper.conditions.temperatureRange(-10,75,"Cel"))
                        )
                        .addCapabilityList("capabilities", smlHelper.capabilities.systemCapabilities()
                                .add("update_rate", smlHelper.capabilities.reportingFrequency(1.0))
                                .add("accuracy", smlHelper.capabilities.absoluteAccuracy(2.5,"m"))
                                .add("ttff_cold", smlHelper.createQuantity()
                                        .definition(SWEHelper.getDBpediaUri("Time_to_first_fix"))
                                        .label("Cold Start TTFF")
                                        .description("Time to first fix on cold start")
                                        .uomCode("s")
                                        .value(120)
                                )
                                .add("ttff_warm", smlHelper.createQuantity()
                                        .definition(SWEHelper.getDBpediaUri("Time_to_first_fix"))
                                        .label("Warm Start TTFF")
                                        .description("Time to first fix on warm start")
                                        .uomCode("s")
                                        .value(30)
                                )
                                .add("ttff_hot", smlHelper.createQuantity()
                                        .definition(SWEHelper.getDBpediaUri("Time_to_first_fix"))
                                        .label("Hot Start TTFF")
                                        .description("Time to first fix on hot start")
                                        .uomCode("s")
                                        .value(5)
                                )
                                .add("battery_life", smlHelper.characteristics.batteryLifetime(72,"h"))
                        );
            }
        }
    }

    @Override
    public void doInit() throws SensorHubException {

        super.doInit();

        // Generate identifiers
        generateUniqueID("urn:osh:sensor:simulated", config.serialNumber);
        generateXmlID("SIMULATED_SENSOR", config.serialNumber);

        // Create and initialize output
        output = new Output(this);

        addOutput(output, false);

        output.doInit();

        // TODO: Perform other initialization
    }

    @Override
    public void doStart() throws SensorHubException {

        if (null != output) {

            // Allocate necessary resources and start outputs
            output.doStart();
        }

        // TODO: Perform other startup procedures
    }

    @Override
    public void doStop() throws SensorHubException {

        if (null != output) {

            output.doStop();
        }

        // TODO: Perform other shutdown procedures
    }

    @Override
    public boolean isConnected() {

        // Determine if sensor is connected
        return output.isAlive();
    }
}
