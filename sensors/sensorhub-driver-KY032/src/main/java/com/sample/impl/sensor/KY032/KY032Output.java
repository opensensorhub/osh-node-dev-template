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

// SWE
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;


import org.sensorhub.api.data.DataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// pi4J imports
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;

/**
 * Output specification and provider for {@link KY032Sensor}.
 * @author Bill Brown
 * @since date
 */
public class KY032Output extends AbstractSensorOutput<KY032Sensor> implements DigitalStateChangeListener {


    private static final String SENSOR_OUTPUT_NAME = "KY-032 SENSOR TEST";
    private static final String SENSOR_OUTPUT_LABEL = "KY-032 Test";
    private static final String SENSOR_OUTPUT_DESCRIPTION = "The KY-032 Obstacle Avoidance Sensor module is a distance-adjustable, infrared proximity sensor designed for wheeled robots";

    private static final Logger logger = LoggerFactory.getLogger(KY032Output.class);

    // PROPERTIES ARE DEFINIED AND VALUES INITIALIZED IN DOINIT()
    private DataRecord dataStruct;                  // Used to describe and define the structure of each output
    private DataEncoding dataEncoding;              // used to provide the default encoding method for each output

    // PROPERTIES DEFINED BY OUTPUT TEMPLATE
    private static final int MAX_NUM_TIMING_SAMPLES = 10;
    private int setCount = 0;
    private final long[] timingHistogram = new long[MAX_NUM_TIMING_SAMPLES];
    private final Object histogramLock = new Object();

    private long lastSetTimeMillis = System.currentTimeMillis();

    // myNote:
    // private DigitalInput sensorInputDetection;
    private volatile boolean sensorDetectionReading;

    /**
     CONSTRUCTOR
     * @param parentSensor Sensor driver providing this output
     */
    KY032Output(KY032Sensor parentSensor) {
        super(SENSOR_OUTPUT_NAME, parentSensor);    // Creates an instance of the OUPUT
        logger.debug("Output created");
        System.out.println("Output Created using "+ parentSensor.getName());
    }


    /**
     * INITIALIZE the data structure for the output, defining the fields, their ordering,
     * and data types.
     */


    void doInit() {

        logger.debug("Initializing Output");
        System.out.println("Initializing Output");

        // Get an instance of SWE Factory suitable to build components
        GeoPosHelper geoHelper = new GeoPosHelper();
        SWEHelper sweFactory = new SWEHelper();         // Create sweFactory Record using SWEHelper Data Model:

        // TODO: Create data record description
        // MyNote:
        // CREATE DATA STRUCTURE FOR THE KY032 SENSOR RECORD OUTPUT
        // Every Record needs name, label, definition, and description
        dataStruct = sweFactory.createRecord()
                .name(SENSOR_OUTPUT_NAME)
                .label(SENSOR_OUTPUT_LABEL)
                .definition(SWEHelper.getPropertyUri("KY032Output"))
                .description(SENSOR_OUTPUT_DESCRIPTION)
                .addField("time", sweFactory.createTime()
                        .asSamplingTimeIsoUTC()
                        .label("Sample Time")
                        .description("Time of data collection"))
                // MyNote:
                // ADDED FIELDS
                .addField("ObstructionDetected", sweFactory.createBoolean()
                        .label("Obstruction Detected:")
                        .definition(SWEHelper.getPropertyUri("obstruction")))
                .build();
        dataEncoding = sweFactory.newTextEncoding(",", "\n");
        logger.debug("Initializing Output Complete");
        System.out.println("Output Initialized...");
    }


    /**
     * Check to validate data processing is still running
     * @return true if worker thread is active, false otherwise
     */
    public boolean isAlive() {

        return true;
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

    // myNote:
    // pi4j uses the DigitalStateChangeListener() class which is bound to this Output class using 'implements'
    // DigitalStateChangeListener() requires uses onDigitalStateChange method to listen to a DigitalStateChangeEvent()--another pi4j class--to listen to.
    // This allows Sensor class to pass Output instance as an argument in pi4j listener
    // https://www.pi4j.com/pi4j-example-crowpi/com.pi4j.crowpi/com/pi4j/crowpi/components/events/DigitalEventListener.html

    @Override
    public void onDigitalStateChange(DigitalStateChangeEvent digitalStateChangeEvent) {
        // GET BOOLEAN READING FOR SENSOR
        sensorDetectionReading = digitalStateChangeEvent.state() == DigitalState.LOW;

        // REPORT READING IN TERMINAL FOR DEBUGGING
        System.out.println("Output:");
        System.out.println("\tDetection: " + sensorDetectionReading);

        // The below code is template code for OSH
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
        // Populate Data Block

        ++setCount;
        double timestamp = System.currentTimeMillis() / 1000d;

        dataBlock.setDoubleValue(0, timestamp);
        dataBlock.setBooleanValue(1, sensorDetectionReading);


        latestRecord = dataBlock;
        latestRecordTime = System.currentTimeMillis();

        eventHandler.publish(new DataEvent(latestRecordTime, KY032Output.this, dataBlock));


    }
}
