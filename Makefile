# Define a default value for TAG. Override it if necessary.
TAG ?= latest

all: install test docker

install:
	./mvnw clean install -pl :georchestra-gateway -ntp -DskipTests

test:
	./mvnw verify -pl :georchestra-gateway -ntp

docker:
	@VERSION=`./mvnw -f gateway/ help:evaluate -q -DforceStdout -Dexpression=imageTag` && \
	./mvnw package -f gateway/ -Pdocker -ntp -DskipTests && \
	echo tagging georchestra/gateway:$${VERSION} as georchestra/gateway:$${TAG} && \
	docker tag georchestra/gateway:$${VERSION} georchestra/gateway:$${TAG} && \
	docker images|grep "georchestra/gateway"|grep jdev

deb: install
	./mvnw package deb:package -f gateway/ -PdebianPackage
