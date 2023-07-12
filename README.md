## OpenSensorHub Build and Deployment
 
### Repositories

osh-node-dev-template

https://github.com/opensensorhub/osh-node-dev-template.git

 
#### Synopsis
The current “node” template source code of OpenSensorHub is located at GitLab.  The repositories contain the source necessary to build a new OSH node, driver, processes, libraries, but also make use of OpenSensorHub open source core and addon.  These open source technologies are referred to by the respective repositories they are employed in as “submodules”  therefore it is important to note than when using git commands to “checkout” any one of these repositories that you do so with the following command
 
         git clone -–recursive https://github.com/opensensorhub/osh-node-dev-template.git
 
Each can be built and deployed individually and manually or can be built and deployed as a single package using the Jenkinsfile and/or docker file(s) in the osh-node-dev-template repo.  Using the Jenkinsfile will require modifications necessary for your particular environment, such as git repos, credentials, docker image repositories, etc.  Review the Jenkinsfile and dockerfile and update as necessary.
 
### Building and Deploying the Node

#### Gradle

Building the Node with Jetty deployable web server from the command line is as simple as checking the repository out and building with a simple command
 
         git clone --recursive https://github.com/opensensorhub/osh-node-dev-template.git
         cd osh-node-template
         ./gradlew build -x test
 
The resulting build will be contained in /osh-node-template/build/distributions/osh-node-*.*.*.zip
 
Deploying is as simple as copying the zip file to the target destination and unzipping the file.  You can then run ./launch.sh in Linux or ./launch.bat in Windows environment to startup OpenSensorHub.

#### Docker

Building a docker image is equally simple and the resulting image will deploy the Node with Jetty using NginX as a reverse proxy for network routing and connectivity.

The docker file was originally intended to be used within the context of Jenkins build, and as such it already is configured to run out of the box.
However, building outside of Jenkins requires the addition of the __-build-arg version=[VERSION]__ switch as shown below
and the command to be executed from within the osh-node-dev-template directory after building with gradle.


         apt-get update && \
        	DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-11-jdk git
 
         git clone --recursive https://github.com/opensensorhub/osh-node-dev-template.git
 
         cd osh-node-dev-template
         ./gradlew build -x test
         docker build -t [repo]:[tag] . -f dockerfile -build-arg version=[OSH-VERSION-FROM-build.gradle]

#### Understanding Docker Commands

It is highly recommended that the user be or become familiar with docker and the following commands

●     To run docker image detached exposing ports

         sudo docker run -d -p443:443 [repo]:[tag or image id]

- ***-p port_visible_to_world:port_exposed_docker_container***:
    - Expose additional ports by including more -p switches, one for each port to be mapped

●     To list docker images:

         sudo docker images

●     To see which docker image is/are running:

         sudo docker ps

●     To see which docker all docker images running or stopped:

         sudo docker ps -a

●     To kill a docker image:

         sudo docker kill <container id>

●     To gracefully start a stopped a docker image:

         sudo docker start <container id | friendly name>

●     To gracefully stop a docker image:

         sudo docker stop <container id | friendly name>

●     To build and tag an image:

         sudo docker build -t <repository>:<tag> . -f <dockerfile>

●     To export an image:

         sudo docker save --output [filename].tar <docker_registry>/<repository>:<tag> . -f <dockerfile>

●     To import an image:

         sudo docker load --input [filename].tar

##### Executing or Running a Docker Container

●     To run docker image detached with mounted file system & name, using present working directory for filesystem source

**_Important_**  - make sure to create **_osh user_** and **_group_** on the host system for the volume to be mounted and set owner
and group to **_osh:osh_** for the volume being mounted


         docker run -d \
         -it \
         --name [container-friendly-name] \
         --mount type=bind,source=[mount-path],target=/opt/[osh-node-path]/[target-dir] \
         [repo]:[tag or image id]


- **container-friendly-name**: a friendly name for the docker container
- **mount-path**: the absolute path to the directory to be mounted as a volume
- **osh-node-path**: the absolute path to the directory where OpenSensorHub lives within the container
- **target-dir**: the name of the directory the source should be mounted within the target

It is recommended to start the image using the following command if you want to mount a host filesystem path
directory where data is typically stored in osh making this data accessible outside the docker instance and
persisting across executions of the instance.  The config.json can be stored in this path to persist configuration.
The launch script needs to be updated, if doing this so that config.json and db are correctly referenced.


         docker run -d -p 443:443 -p80:80 \
         -it --name [container-friendly-name] \
         --mount type=bind,source=[mount-path]/data,target=/opt/[osh-node-path]/data \
         [repo]:[tag or image id]

- **container-friendly-name**: a friendly name for the docker container
- **mount-path**: the absolute path to the directory to be mounted as a volume
- **osh-node-path**: the absolute path to the directory where OpenSensorHub lives within the container
- **target-dir**: in this example it has been set to **_data_** and will contain the config and recorded data

If using mounted volumes and configuration file (config.json) is hosted on mounted volume then change launch.[sh | bat]
to point to the correct path for the mounted volumes.  Similarly, if data is to be stored external to the container update
path to location for database files

         java -Xmx2g -Dlogback.configurationFile=./logback.xml -cp "lib/*" \
         -Djava.system.class.loader="org.sensorhub.utils.NativeClassLoader" \
         -Djavax.net.ssl.keyStore="./osh-keystore.p12" -Djavax.net.ssl.keyStorePassword="atakatak" \
         -Djavax.net.ssl.trustStore="./osh-keystore.p12" -Djavax.net.ssl.trustStorePassword="atakatak" \
         org.sensorhub.impl.SensorHub ./data/config.json ./data/db

***
# Adding Additional Inbound Streams to NGINX and Docker
Sometimes it may be necessary to support services that establish TCP connections with OpenSensorHub.  In such instances,
if deploying OpenSensorHub in a containerized environment modifications need to be made to NGINX and Dockerfile.

## Modifying Dockerfile
Dockerfile will need to be updated with the port or ports to expose.

`EXPOSE 80 443 8500`

Find the above line and add the port numbers to be exposed.  Make sure that when deploying the container the internal
target ports are mapped to the externally available ports.  See examples above in **Docker** section

## Modifying NGINX Configuration
The _nginx.conf_ file needs to be updated to provide a new **stream** configuration block or entry.
The file can be found in the ./container directory and currently contains the following
stream configuration:

```
stream {
	server {
		listen 8500;
		#TCP traffic will be forwarded for Iridium connections
		proxy_pass localhost:8500;
		proxy_protocol on;
		
		access_log /var/log/nginx/iridium_access.log;
		
		error_log /var/log/nginx/iridium_error.log;
	}
}
```

***
# Importing an Image

Importing an image locally is as simple as running


         docker load --input [filename].tar


if the user does not have superuser privileges, otherwise


         sudo docker load --input [filename].tar

If using AWS, GCP, or other cloud based computing service the procedure for importing
the container to the respective registry is dependent on the organization and its
internally defined procedures.

## Setting Up to Execute the Container

It is necessary for the administrator of the OpenSensorHub deployments to set up
external storage for the container.  This storage will be used to maintain configuration
information as well as provide a location for OpenSensorHub database and log files to be
maintained.  Failure to provide external storage runs the risk of data loss associated with
updating the container to a different (whether newer or older) version.


**_Important_**: Setting up this external storage volume(s) is outside the scope of this documentation
as it pertains to the specific environment, organization procedures, and usage of desired cloud
based service.

### Necessary Configuration to Support External Volumes

The instructions in this section pertain to the cloud based instance hosting the storage volumes

#### Fresh Install

The following procedures should be followed for a ***_fresh install_*** of OpenSensorHub

###### Ensure the configuration of a user and group

The following commands add a group with group id ***osh*** and a user with id ***osh***

     groupadd -g 4242 osh
     useradd -g osh -u 4242 -m -r -s /bin/false/ osh

###### Create Directory for Mapping to Container

Within the home directory of the user created in the previous step create a subdirectory by executing

    cd ~/osh
    mkdir osh_config

###### Default OSH Configuration

With the deployment package, there is a ***config.json*** file containing a default configuration of
OpenSensorHub.  Within this configuration only default users and services are configured.
The default administrative credentials are

    uname: admin
    password: admin

The default url to access admin panel is:

    https://<address>/sensorhub/admin

where **address** is the URL or IP address of the system hosting OpenSensorHub

#### Update or Upgrade

The following procedures should be followed for an update/upgrade/refresh (pick your favorite term)
of OpenSensorHub

###### Ensure the configuration of a user and group exist

Ensure that ***osh*** group and ***osh*** user exist

     id osh

Both user and group ids should be 4242

If not then the following commands add a group with group id ***osh*** and a user with id ***osh***

     groupadd -g 4242 osh
     useradd -g osh -u 4242 -r -s /bin/false/ osh

If user and group id exist ensure they use correct id's

     groupmod -g 4242 osh
     usermod -u 4242 osh

###### Ensure directory for mapping to container exists

Within the home directory of the ***osh*** user a directory exists

    ls ~/osh/osh_config

**_Important_**: Do not delete the contents of the directory

Back up the contents of the directory.  
You can create a new split Zip file using the -s option followed by a specified size. The multiplier
can be k (kilobytes), m (megabytes), g (gigabytes), or t (terabytes).

    zip -s 1g -r <archivename>.zip <directory_name>

The command above will keep creating new archives in a set after it reaches the specified size limit.

    <archivename>.zip
    <archivename>.z01
    <archivename>.z02
    <archivename>.z03
    <archivename>.z04

## Execute the Container

In this section the commands to load and execute the container are provided.  It is important to note
the version number of the OpenSensorHub installation and use it when specifying the target for the mounted
volumes.


    docker run --name [container-friendly-name] -p443:443 -p80:80 -p10800:10800 \
    -d --mount type=bind,source=[mount-path]/osh_config,target=/opt/[osh-node-path]/osh_config \
    [repo]:[tag or image id]

- **container-friendly-name**: a friendly name for the docker container
- **mount-path**: the absolute path to the directory to be mounted as a volume
- **osh-node-path**: the absolute path to the directory where OpenSensorHub lives within the container, e.g. "/opt/osh_node-2.2.4/osh_config"
- **target-dir**: in this example it has been set to **_data_** and will contain the config and recorded data


## Viewing and Harvesting Log Files

General log file is accessible through the external volumes at

    /home/osh/osh_config/.moduledata/log.txt

Log files for drivers, services, etc. are accessible through the external volumes at

    /home/osh/osh_config/.moduledata

For a specific module, the log files are contained within subdirectory given the module's unique identifier
