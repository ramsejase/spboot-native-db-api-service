FROM ghcr.io/graalvm/graalvm-ce:ol7-java17-22.3.3

ADD . /build
WORKDIR /build

VOLUME ["/root/.m2/repository"]

RUN yum install -y unzip zip

RUN \
    curl -s "https://get.sdkman.io" | bash; \
    source "$HOME/.sdkman/bin/sdkman-init.sh"; \
    sdk install maven; \
    gu install native-image;

RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && mvn -version

RUN java -version && native-image --version

RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && mvn clean install

RUN cd db-api-service

RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && mvn -Pnative spring-boot:build-image --no-transfer-progress

FROM oraclelinux:9-slim

COPY --from=0 "/build/target/db-api-service" db-api-service

CMD [ "sh", "-c", "./db-api-service -Dserver.port=8080" ]