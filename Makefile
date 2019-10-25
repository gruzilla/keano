build:
	docker run -v /home/ma/applications/keano.abendstille.at:/app -w /app/map -ti node:10.17-alpine npm run build
