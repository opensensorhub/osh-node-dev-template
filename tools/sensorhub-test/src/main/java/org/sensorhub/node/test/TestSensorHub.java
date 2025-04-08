/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2020 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package org.sensorhub.node.test;

import org.sensorhub.impl.SensorHub;


public class TestSensorHub
{
    private TestSensorHub()
    {        
    }

    // Play this one for testing. use Server 8282
    public static void main(String[] args) throws Exception
    {
        try {
            SensorHub.main(new String[] {"osh-node-dev-template/tools/sensorhub-test/src/main/resources/config.json", "storage"});

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
