/***************************** BEGIN LICENSE BLOCK ***************************
 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2020-2025 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.sample.impl.sensor.drivername;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver implementation for the sensor.
 * <p>
 * This class is responsible for providing sensor information, managing output registration,
 * and performing initialization and shutdown for the driver and its outputs.
 */
public class Sensor extends AbstractSensorModule<Config> {
    static final String UID_PREFIX = "urn:osh:template_driver:";
    static final String XML_PREFIX = "TEMPLATE_DRIVER_";

    private static final Logger logger = LoggerFactory.getLogger(Sensor.class);

    Output output;
    Thread processingThread;
    volatile boolean doProcessing = true;

    @Override
    public void doInit() throws SensorHubException {
        super.doInit();

        // Generate identifiers
        generateUniqueID(UID_PREFIX, config.serialNumber);
        generateXmlID(XML_PREFIX, config.serialNumber);

        // Create and initialize output
        output = new Output(this);
        addOutput(output, false);
        output.doInit();
    }

    @Override
    public void doStart() throws SensorHubException {
        super.doStart();
        startProcessing();
    }

    @Override
    public void doStop() throws SensorHubException {
        super.doStop();
        stopProcessing();
    }

    @Override
    public boolean isConnected() {
        return processingThread != null && processingThread.isAlive();
    }

    /**
     * Starts the data processing thread.
     * <p>
     * This method simulates sensor data collection and processing by generating data samples at regular intervals.
     */
    public void startProcessing() {
        doProcessing = true;

        processingThread = new Thread(() -> {
            while (doProcessing) {
                // Simulate data collection and processing
                output.setData(System.currentTimeMillis(), "Sample Data");

                // Simulate a delay between data samples
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        processingThread.start();
    }

    /**
     * Signals the processing thread to stop.
     */
    public void stopProcessing() {
        doProcessing = false;
    }
}
