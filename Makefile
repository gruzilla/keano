build:
	docker run --rm -v $(shell pwd):/app -w /app/map -ti node:10.17-alpine npm run build

update:
	git pull

update-map:
	docker run --rm -v $(shell pwd):/app -w /app/map -ti node:10.17-alpine npm install

deploy:
	ssh ma@abendstille.at "cd applications/keano.abendstille.at; git pull"
	scp map/dist/bundle.js ma@abendstille.at:applications/keano.abendstille.at/map/dist
