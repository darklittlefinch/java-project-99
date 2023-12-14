run-dist:
	./build/install/app/bin/app

dev:
	./gradlew bootRun --args='--spring.profiles.active=dev'

build:
	./gradlew build

clean:
	./gradlew clean

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

build-run: build run

.PHONY: build
