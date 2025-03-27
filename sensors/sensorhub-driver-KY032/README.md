# [KY032]
Sensor adapter for [NAME].

This is my attempt to make a driver for the <a href="https://arduinomodules.info/ky-032-infrared-obstacle-avoidance-sensor-module/">KY-032 Infrared Obstacle Avoidance Sensor</a>. As you read through my code, you will notice several area's with the comment <em>"// myNote:"</em>. This note will represent all the area's where I manually updated existing code (Output.java, build.gradle, etc...).

## Files Added/Updated
### KY032.java
To initally test whether or not my Sensor was working, I wrote a Python script since that was a language I'm more familiar with. Once I was able to confirm that the Raspberry Pi was receiving signals from the Sensor, I re-wrote the Python program using Java. That program ultimately became this Java Class. This class uses the <a href="https://www.pi4j.com/1.2/pins/model-3b-rev1.html">PI4J Library</a> to take readings of the Sensor from the Raspberry Pi's GPIO signal. To construct the class, the GPIO BCM pin number reading the sensor's ouput must be provided. Once initialized, the class mainly has one method that I created called <em>readSensor()</em>. This method is used in the output class to read the Raspberry PI's GPIO output each time the run() method is used.  

### build.gradle
In order for the KY032.java class to work properly, I added the dependencies to use the <a href="https://www.pi4j.com/1.2/pins/model-3b-rev1.html">PI4J Library</a>, as well as some of the primary details (Description, version, etc).

### Output.java
In this file, I really only do (3) things:
- Create a Sensor Instance my KY032.java class
- Use sweFactory to create a data structure for my sensor
- Assign the <em>TTL</em> value of my data record in the <b>run()</b> method to read my GPIO signal.



## Configuration

Configuring the sensor requires:
Select ```Sensors``` from the left hand accordion control and right click for context sensitive menu in accordion control
- **Module Name:** A name for the instance of the driver
- **Serial Number:** The platforms serial number, or a unique identifier
- **Auto Start:** Check the box to start this module when OSH node is launched

Storage:
Select ```Storage``` from the left hand accordion control and right click for context sensitive menu in accordion control
Use a ```Real-Time Stream Storage Module``` providing the sensor module as the 
- **Data Source ID:** Select the identifier for the storage module create in configuring sensor step,
use looking glass to select it from list of know sensor modules 
- **Auto Start:** Check the box to start this module when OSH node is launched
- **Process Events:** Check the box if you want events to be stored as new records.
                 
And then configure the 
- **Storage Config** using a ```Perst Record Storage``` instance providing the 
  - **Storage Path** as the location where the OSH records are to be stored.

SOS Service:
Select ```Services``` from the left hand accordion control, then Offerings, then the **+**
symbol to add a new offering.
Provide the following:
- **Name:** A name for the offering
- **Description:** A description of the offering
- **StorageId:** Select the identifier for the storage module create in previous step,
 use looking glass to select it from list of know storage modules
- **SensorId:** Select the identifier for the storage module create in configuring sensor step,
                 use looking glass to select it from list of know sensor modules
- **Enable:** Check the box to enable this offering

## Sample Requests

The following are a list of example requests and their respective responses.  
The **IP ADDRESS** and **PORT** will need to be specified and point to the instance
of the OpenSensorHub node serving the data.

### [Observed Property] Request
- **HTTP**
   - http://[IP ADDRESS]:[PORT]/sensorhub/sos?service=SOS&version=2.0&request=GetResult&offering=[URN]&observedProperty=[OBSERVED_PROPERTY]&temporalFilter=phenomenonTime,[START_TIME]/[END_TIME]&replaySpeed=1&responseFormat=application/json

Response:
```

```
