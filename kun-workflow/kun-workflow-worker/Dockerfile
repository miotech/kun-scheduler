FROM hub.miotech.com/library/ec2-spark

ENV LANG en_US.utf8
ENV TINI_VERSION v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--", "/docker-entrypoint.sh"]

WORKDIR /server/target

COPY dockerize-alpine-linux-amd64-v0.6.1.tar.gz /server/target/dockerize-alpine-linux-amd64-v0.6.1.tar.gz
RUN tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-v0.6.1.tar.gz \
    && rm dockerize-alpine-linux-amd64-v0.6.1.tar.gz

COPY build/libs/kunWorker.jar /server/target/kubernetesOperatorLauncher.jar
