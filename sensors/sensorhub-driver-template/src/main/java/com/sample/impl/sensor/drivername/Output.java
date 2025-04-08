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

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.data.DataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.swe.helper.GeoPosHelper;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Output specification and provider for {@link Sensor}.
 */
public class Output extends AbstractSensorOutput<Sensor> implements Runnable {
    private static final String SENSOR_OUTPUT_NAME = "SensorOutput";
    private static final String SENSOR_OUTPUT_LABEL = "Sensor Output";
    private static final String SENSOR_OUTPUT_DESCRIPTION = "Sensor output data";

    private static final Logger logger = LoggerFactory.getLogger(Output.class);
    private static final int MAX_NUM_TIMING_SAMPLES = 10;

    private final Object processingLock = new Object();
    private final ArrayList<Double> intervalHistogram = new ArrayList<>(MAX_NUM_TIMING_SAMPLES);
    private final Object histogramLock = new Object();

    private DataRecord dataStruct;
    private DataEncoding dataEncoding;
    private Boolean stopProcessing = false;
    private Thread worker;

    /**
     * Creates a new output for the sensor driver.
     *
     * @param parentSensor Sensor driver providing this output.
     */
    Output(Sensor parentSensor) {
        super(SENSOR_OUTPUT_NAME, parentSensor);
    }

    /**
     * Initializes the data structure for the output, defining the fields, their ordering, and data types.
     */
    void doInit() {
        // Get an instance of SWE Factory suitable to build components
        GeoPosHelper sweFactory = new GeoPosHelper();

        // Create the data record description
        dataStruct = sweFactory.createRecord()
                .name(SENSOR_OUTPUT_NAME)
                .label(SENSOR_OUTPUT_LABEL)
                .description(SENSOR_OUTPUT_DESCRIPTION)
                .addField("sampleTime", sweFactory.createTime()
                        .asSamplingTimeIsoUTC()
                        .label("Sample Time")
                        .description("Time of data collection"))
                .addField("data", sweFactory.createText()
                        .label("Example Data"))
                .build();

        dataEncoding = sweFactory.newTextEncoding(",", "\n");
    }

    /**
     * Begins processing data for output.
     */
    public void doStart() {
        // Instantiate a new worker thread
        worker = new Thread(this, this.name);
        worker.start();
    }

    /**
     * Terminates processing data for output.
     */
    public void doStop() {
        synchronized (processingLock) {
            stopProcessing = true;
        }
    }

    /**
     * Verify if the data processing thread is still active.
     *
     * @return true if worker thread is active, false otherwise.
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
        synchronized (histogramLock) {
            double sum = 0;
            for (double sample : intervalHistogram)
                sum += sample;

            return sum / intervalHistogram.size();
        }
    }

    @Override
    public void run() {
        boolean processSets = true;

        try {
            while (processSets) {
                long timestamp = System.currentTimeMillis();
                DataBlock dataBlock = latestRecord == null ? dataStruct.createDataBlock() : latestRecord.renew();

                updateIntervalHistogram();

                // Populate the data block
                dataBlock.setDoubleValue(0, timestamp / 1000d);
                dataBlock.setStringValue(1, "Your data here");

                // Publish the data block
                latestRecord = dataBlock;
                latestRecordTime = timestamp;
                eventHandler.publish(new DataEvent(latestRecordTime, Output.this, dataBlock));

                // Simulate a delay between data samples
                sleep(100);

                synchronized (processingLock) {
                    processSets = !stopProcessing;
                }
            }
        } catch (Exception e) {
            logger.error("Error in worker thread: {}", Thread.currentThread().getName(), e);
        } finally {
            // Reset the flag so that when the driver is restarted loop thread continues
            // until doStop called is on the output again.
            stopProcessing = false;

            logger.debug("Terminating worker thread: {}", this.name);
        }
    }

    /**
     * Updates the interval histogram with the time between the latest record and the current time
     * for calculating the average sampling period.
     */
    private void updateIntervalHistogram() {
        synchronized (histogramLock) {
            if (latestRecord != null && latestRecordTime != Long.MIN_VALUE) {
                long interval = System.currentTimeMillis() - latestRecordTime;
                intervalHistogram.add(interval / 1000d);

                if (intervalHistogram.size() > MAX_NUM_TIMING_SAMPLES) {
                    intervalHistogram.remove(0);
                }
            }
        }
    }
}
