.PHONY: build clean run stop refresh

default: build

build:
	mvn clean package -DskipTests
	docker-compose build

clean:
	docker-compose down --rmi all

run: build
	docker-compose up -d

stop:
	docker-compose down

refresh:
	make clean
	make build
	make run
