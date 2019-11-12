start:
	docker-compose up -d

stop:
	docker-compose down

rebuild-jar:
	mvn -DskipTests clean package

rebuild: rebuild-jar restart

restart:
	docker-compose restart api

retry: rebuild restart

logs:
	docker logs -f decarbnow_api_1

backup:
	docker exec -t decarbnow_database_1 pg_dumpall -c -U decarbnow > backups/dump_`date +%d-%m-%Y"_"%H_%M_%S`.sql

restore:
	cat backups/dump_05-02-2019_20_42_36.sql | docker exec -i decarbnow_database_1 psql -U decarbnow

save:
	docker exec -t decarbnow_database_1 pg_dump --role decarbnow -U decarbnow > backups/data.sql
