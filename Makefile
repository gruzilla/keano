build:
	docker run -v $(shell pwd):/app -w /app/map -ti node:10.17-alpine npm run build

deploy:
	ssh ma@abendstille.at "cd applications/keano.abendstille.at; git pull"
	scp map/dist/bundle.js ma@abendstille.at:applications/keano.abendstille.at/map/dist
