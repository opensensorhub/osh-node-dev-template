## OpenSensorHub Build and Deployment
[![OpenSensorHub Discord](https://user-images.githubusercontent.com/7288322/34429117-c74dbd12-ecb8-11e7-896d-46369cd0de5b.png)](https://discord.gg/6k3QYRSh9F)
 
### Repositories

osh-node-dev-template

https://github.com/opensensorhub/osh-node-dev-template.git

 
#### Synopsis
The current “node” template source code of OpenSensorHub is located at GitLab.  The repositories contain the source code necessary to build a new OSH node, driver, processes, and libraries, but they also make use of the OpenSensorHub open source core and addons.  These open source technologies are referred to by the respective repositories they are employed in as "submodules." Therefore, it is important to note that when using git commands to “checkout” any one of these repositories, you do so with the following command:
 
         git clone -–recursive https://github.com/opensensorhub/osh-node-dev-template.git

### Building and Deploying the Node

#### Gradle

Building the Node with Jetty deployable web server from the command line is as simple as checking the repository out and building with a simple command
 
         git clone --recursive https://github.com/opensensorhub/osh-node-dev-template.git
         cd osh-node-template
         ./gradlew build -x test
 
The resulting build will be contained in /osh-node-template/build/distributions/osh-node-*.*.*.zip
 
Deploying is as simple as copying the zip file to the target destination and unzipping the file.  You can then run ./launch.sh in Linux or ./launch.bat in Windows environment to startup OpenSensorHub.

###### Default OSH Configuration

With the deployment package, there is a ***config.json*** file containing a default configuration of
OpenSensorHub.  Within this configuration, only default users and services are configured.
The default administrative credentials are

    uname: admin
    password: admin

The default URL to access the admin panel is:

    https://<address>/sensorhub/admin

where **address** is the URL or IP address of the system hosting OpenSensorHub

## Viewing Log Files

The general log file is accessible through the external volumes at

    .moduledata/log.txt

Log files for drivers, services, etc. are accessible through 

    /home/osh/osh_config/.moduledata

For a specific module, the log files are contained within subdirectory given the module's unique identifier
