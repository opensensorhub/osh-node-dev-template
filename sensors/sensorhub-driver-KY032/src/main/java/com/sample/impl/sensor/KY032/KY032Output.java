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

import com.pi4j.io.gpio.digital.DigitalState;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.data.DataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.swe.SWEHelper;
// pi4J imports
import com.pi4j.io.gpio.digital.DigitalInput;

/**
 * Output specification and provider for {@link KY032Sensor}.
 *
 * @author your_name
 * @since date
 */
public class KY032Output extends AbstractSensorOutput<KY032Sensor> implements Runnable {

    private static final String SENSOR_OUTPUT_NAME = "KY-032 SENSOR";
    private static final String SENSOR_OUTPUT_LABEL = "KY-032";
    private static final String SENSOR_OUTPUT_DESCRIPTION = "The KY-032 Obstacle Avoidance Sensor module is a distance-adjustable, infrared proximity sensor designed for wheeled robots";

    private static final Logger logger = LoggerFactory.getLogger(KY032Output.class);

    // PROPERTIES ARE DEFINIED AND VALUES INITIALIZED IN DOINIT()
    private DataRecord dataStruct;                  // Used to describe and define the structure of each output
    private DataEncoding dataEncoding;              // used to provide the default encoding method for each output

    // PROPERTIES DEFINED BY OUTPUT TEMPLATE
    private Boolean stopProcessing = false;
    private final Object processingLock = new Object();

    private static final int MAX_NUM_TIMING_SAMPLES = 10;
    private int setCount = 0;
    private final long[] timingHistogram = new long[MAX_NUM_TIMING_SAMPLES];
    private final Object histogramLock = new Object();

    private Thread worker;
    // myNote:
    // private DigitalInput sensorInputDetection;
    private volatile boolean sensorDetectionReading;

    // CREATE A SENSOR INSTANCE USING MY KY032 CLASS
    //private final KY032 myKY032 = new KY032(23);
    /**
     * Constructor
     * @param parentSensor Sensor driver providing this output
     */
    KY032Output(KY032Sensor parentSensor) {
        super(SENSOR_OUTPUT_NAME, parentSensor);    // Creates an instance of the OUPUT
        logger.debug("Output created");
        System.out.println("Output Created...");
    }



    /**
     * Initializes the data structure for the output, defining the fields, their ordering,
     * and data types.
     */
    void doInit() {

        logger.debug("Initializing Output");
        System.out.println("Initializing Output");

        // Get an instance of SWE Factory suitable to build components
        //GeoPosHelper sweFactory = new GeoPosHelper();
        SWEHelper sweFactory = new SWEHelper();         // Create sweFactory Record using SWEHelper Data Model:

        // TODO: Create data record description
        // MyNote:
        // CREATE DATA STRUCTURE FOR KY032 SENSOR
        dataStruct = sweFactory.createRecord()
                .name(SENSOR_OUTPUT_NAME)
                .label(SENSOR_OUTPUT_LABEL)
                .definition("urn:osh:data:KY032")
                .description(SENSOR_OUTPUT_DESCRIPTION)
                .addField("time", sweFactory.createTime()
                        .asSamplingTimeIsoUTC()
                        .label("Sample Time")
                        .description("Time of data collection"))
                // MyNote:
                // ADDED FIELD FOR OBSTRUCTION
                .addField("ObstructionDetected", sweFactory.createBoolean()
                        .label("Obstruction Detected:"))
                .build();

        dataEncoding = sweFactory.newTextEncoding(",", "\n");
        logger.debug("Initializing Output Complete");
        System.out.println("Output Initialized...");
    }

    /**
     * Begins processing data for output
     */
    public void doStart(boolean sensorDetectionReading ) {
        // myNote:
        // Add sensor reading to this global class instance
        System.out.println("Ouput Starting...");
        this.sensorDetectionReading = sensorDetectionReading;

        // Instantiate a new worker thread
        worker = new Thread(this, this.name);

        // TODO: Perform other startup
        logger.info("Starting worker thread: {}", worker.getName());

        // Start the worker thread
        worker.start();
    }

    /**
     * Terminates processing data for output
     */
    public void doStop() {
        synchronized (processingLock) {
            stopProcessing = true;
        }

        // TODO: Perform other shutdown procedures
    }

    /**
     * Check to validate data processing is still running
     *
     * @return true if worker thread is active, false otherwise
     */
    public boolean isAlive() {

        return worker.isAlive();
    }

    @Override
    public DataComponent getRecordDescription() {
        return dataStruct;
    }

    @Override
    public DataEncoding getRecommendedEncoding() {
        return dataEncoding;
    }

    @Override
    public double getAverageSamplingPeriod() {

        long accumulator = 0;

        synchronized (histogramLock) {

            for (int idx = 0; idx < MAX_NUM_TIMING_SAMPLES; ++idx) {

                accumulator += timingHistogram[idx];
            }
        }

        return accumulator / (double) MAX_NUM_TIMING_SAMPLES;
    }



    @Override
    public void run() {
        System.out.println("Thread Running...");
        boolean processSets = true;
        long lastSetTimeMillis = System.currentTimeMillis();

        try {
            while (processSets) {
                DataBlock dataBlock;
                if (latestRecord == null) {
                    dataBlock = dataStruct.createDataBlock();
                } else {
                    dataBlock = latestRecord.renew();
                }
                synchronized (histogramLock) {
                    int setIndex = setCount % MAX_NUM_TIMING_SAMPLES;

                    // Get a sampling time for latest set based on previous set sampling time
                    timingHistogram[setIndex] = System.currentTimeMillis() - lastSetTimeMillis;

                    // Set latest sampling time to now
                    lastSetTimeMillis = timingHistogram[setIndex];
                }


                // TODO: Populate data block
                // myNote:
                ++setCount;
                double timestamp = System.currentTimeMillis() / 1000d;

                dataBlock.setDoubleValue(0, timestamp);
                dataBlock.setBooleanValue(1, sensorDetectionReading);

                latestRecord = dataBlock;
                latestRecordTime = System.currentTimeMillis();

                eventHandler.publish(new DataEvent(latestRecordTime, KY032Output.this, dataBlock));


                synchronized (processingLock) {
                    processSets = !stopProcessing;
                }

            }

        } catch (Exception e) {

            logger.error("Error in worker thread: {}", Thread.currentThread().getName(), e);

        } finally {

            // Reset the flag so that when driver is restarted loop thread continues
            // until doStop called on the output again
            stopProcessing = false;
            logger.debug("Terminating worker thread: {}", this.name);
        }
    }
}
