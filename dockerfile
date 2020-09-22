# Multi-Stage Build
FROM ubuntu:18.04 AS osh-build

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-8-jdk git
COPY osh-node ./osh-node

RUN cd osh-node && \
    chmod 755 gradlew && \
    ./gradlew build -x test

FROM ubuntu:18.04 AS osh-deployment

ARG release=dev
ARG version=1.0.0

# General system setup
RUN useradd -r -s /bin/false/ osh && echo "US/Central" > /etc/timezone

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-8-jdk git unzip curl nano certbot wget nginx

# Install OSH Node
COPY --from=osh-build  ./osh-node/build/distributions/osh-node-$release-$version.zip .
ADD container/osh-service /etc/init.d/
RUN chmod 755 /etc/init.d/osh-service
RUN unzip osh-node.zip -d /opt && \
    rm osh-node.zip && \
    chown -R osh:osh /opt/osh-node* && \
    chmod 755 /opt/osh-node/launch.* && \
    update-rc.d osh-service defaults && \
    update-rc.d osh-service enable

# Configure nginx
ADD container/nginx-users container/nginx.crt container/nginx.key /etc/nginx/
ADD container/nginx-default /etc/nginx/sites-available/default
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

# Open ports
EXPOSE 80 443

CMD service osh-service start && \
    service nginx start
