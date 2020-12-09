FROM ubuntu:18.04 AS osh-deployment

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-8-jdk git unzip curl nano certbot wget nginx

ARG version=''

# General system setup
RUN useradd -r -s /bin/false/ osh && echo "US/Central" > /etc/timezone


# Install OSH Node
ADD build/distributions/osh-node-$version.zip .
ADD container/osh-service /etc/init.d/
RUN awk -v version=$version '{ if ( $0~"OSH_HOME=/opt/osh" ) { print $0"-"version } else { print $0 } }' /etc/init.d/osh-service > /etc/init.d/osh-service-versioned && \
    mv /etc/init.d/osh-service-versioned /etc/init.d/osh-service
RUN chmod 755 /etc/init.d/osh-service
RUN unzip osh-node-$version.zip -d /opt && \
    rm osh-node-$version.zip && \
    chown -R osh:osh /opt/osh-node* && \
    chmod 755 /opt/osh-node-$version/launch.* && \
    update-rc.d osh-service defaults && \
    update-rc.d osh-service enable

# Configure nginx
ADD container/nginx-users container/nginx.crt container/nginx.key /etc/nginx/
ADD container/nginx-default /etc/nginx/sites-available/default
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

# Open ports
EXPOSE 80 443

CMD sh -c 'ln -sfn /opt/osh-node /opt/osh-node-$version' && \
    service osh-service start && \
    service nginx start
