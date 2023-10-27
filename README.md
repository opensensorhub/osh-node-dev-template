## OpenSensorHub Build and Deployment
[![OpenSensorHub Discord](https://user-images.githubusercontent.com/7288322/34429117-c74dbd12-ecb8-11e7-896d-46369cd0de5b.png)](https://discord.gg/6k3QYRSh9F)
 
### Repositories

osh-node-dev-template

https://github.com/opensensorhub/osh-node-dev-template.git

 
#### Synopsis
The current “node” template source code of OpenSensorHub is located at GitLab.  The repositories contain the source necessary to build a new OSH node, driver, processes, libraries, but also make use of OpenSensorHub open source core and addon.  These open source technologies are referred to by the respective repositories they are employed in as “submodules”  therefore it is important to note than when using git commands to “checkout” any one of these repositories that you do so with the following command:
 
         git clone –recursive https://github.com/opensensorhub/osh-node-dev-template.git
 
Each can be built and deployed individually and manually or can be built and deployed as a single package using the Jenkinsfile and/or docker file(s) in the osh-node-template repo.  Using the Jenkinsfile will require modifications necessary for your particular environment, such as git repos, credentials, docker image repositories, etc.  Review the Jenkinsfile and dockerfile and update as necessary.
 

 
### Building and Deploying the Node

#### Gradle

Building the Node with Jetty deployable web server from the command line is as simple as checking the repository out and building with a simple command
 
         git clone --recursive https://github.com/opensensorhub/osh-node-dev-template.git
         cd osh-node-template
         ./gradlew build -x test
 
The resulting build will be contained in /osh-node-template/build/distributions/osh-node-*.*.*.zip
 
Deploying is as simple as copying the zip file to the target destination and unzipping the file.  You can then run ./launch.sh in Linux or ./launch.bat in Windows environment to startup OpenSensorHub.

#### Docker

Building a docker image is equally as simple and the resulting image will deploy the Node with Jetty using NginX as a reverse proxy for network routing and connectivity.
 
         apt-get update && \
        	DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-8-jdk git
 
         git clone --recursive https://github.com/opensensorhub/osh-node-dev-template.git
 
         cd osh-node-template
        	docker build -t [repo]:[tag] . -f dockerfile
 
The dockerfile is a multistage dockerfile that will build an Ubuntu 18.04 based image and configure OSH to run as a service and NginX the handle routing.
 
It is highly recommended that the user be or become familiar with docker and the following commands
 
●     To run docker image detached:

         sudo docker run -d -p 443:443 [repo]:[tag or image id]
 
●     To list docker images:

         sudo docker images
 
●     To see which docker image is/are running:

         sudo docker ps
 
●     To kill a docker image:

         sudo docker kill <container id>

●     To gracefully stop a docker image:

         sudo docker stop <container id>

●     To build and tag an image:

         sudo docker build -t <repository>:<tag> . -f <dockerfile>
 
●     To run docker image detached with mounted file system & name, using present working directory for filesystem source


**_Important  - change osh-node-*.*.* to the correct version number: e.g. osh-node-1.0.0_**
         

         docker run -d \
         -it \
         --name <name> \
         --mount type=bind,source="$(pwd)"/target,target=/opt/osh-node-*.*.*/db
         [repo]:[tag or image id]
 
 
It is recommended to start the image using the following command if you want to mount a host filesystem path to the db directory where data is typically stored in osh making this data accessible outside the docker instance and persisting across executions of the instance.

**_Important  - change osh-node-*.*.* to the correct version number: e.g. osh-node-1.0.0_**


         docker run -d -p 443:443 -p80:80 \
         -it --name osh \
         --mount type=bind,source="$(pwd)",target=/opt/osh-node-*.*.*/db \
         [repo]:[tag or image id]
