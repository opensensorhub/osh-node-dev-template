/***************************** BEGIN LICENSE BLOCK ***************************
 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2020-2025 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package org.sensorhub.node.test;

import org.sensorhub.impl.SensorHub;

/**
 * The sensorhub-test module provides a convenient way to build and run an OpenSensorHub (OSH) node
 * without requiring a full build, extraction, and launch process.
 * This allows developers to set breakpoints in the IDE and debug the drivers directly.
 * <p>
 * Running this class will launch an OSH node on the address and port and with the credentials
 * specified in the config.json file.
 * By default, the OSH admin panel can be accessed via the web interface at localhost:8282/sensorhub/admin
 * with the default credentials admin/admin.
 * The API can be accessed at localhost:8282/sensorhub/api.
 * <p>
 * Additional drivers can be added to the build.gradle file in the dependencies section.
 */
public class TestSensorHub {
    private TestSensorHub() {
    }

    public static void main(String[] args) {
        SensorHub.main(new String[]{"tools/sensorhub-test/src/main/resources/config.json", "storage"});
    }
}
