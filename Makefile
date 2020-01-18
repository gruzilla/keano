restart:
	docker-compose restart collector

update:
	git pull

update-map: update
	docker run --rm -v "$(shell pwd)":/app -w /app/map -ti node:10.17-alpine npm install
	docker run --rm -v "$(shell pwd)":/app -w /app/map -ti node:10.17-alpine npm run build

update-collector: update
	mkdir -p .maven-repo
	docker run -it --rm --name collector -v "$(shell pwd)/.maven-repo":/root/.m2 -v "$(shell pwd)/collector":/app -w /app maven:3.3-jdk-8 mvn -DskipTests clean package
