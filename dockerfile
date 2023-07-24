FROM ubuntu:22.04 AS osh-deployment

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-11-jdk git unzip curl nano certbot wget nginx

ARG version=''

# General system setup
RUN useradd -r -s /bin/bash osh && echo "US/Central" > /etc/timezone

# Install OSH Node
ADD build/distributions/osh-node-$version.zip .
ADD container/osh-service /etc/init.d/
RUN chmod 755 /etc/init.d/osh-service
RUN unzip osh-node-$version.zip -d /opt
RUN mv /opt/osh-node-$version /opt/opensensorhub
RUN rm osh-node-$version.zip
RUN chown -R osh:osh /opt/opensensorhub
RUN chmod 755 /opt/opensensorhub/launch.*

# Configure nginx
ADD container/nginx-users container/nginx.crt container/nginx.key /etc/nginx/
ADD container/nginx-default /etc/nginx/sites-available/default
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

# Open ports
EXPOSE 80 443

CMD sh -c 'ln -sfn /opt/osh-node /opt/osh-node-$version' && \
    /etc/init.d/osh-service start && \
    service nginx start
