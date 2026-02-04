.PHONY: help up down build restart logs ps clean-db reset test-unit test coverage

SERVICE ?= notification-service

help:
	@echo "Targets:"
	@echo "  make up        - build and start services in background"
	@echo "  make down      - stop services"
	@echo "  make build     - rebuild images"
	@echo "  make restart   - restart services"
	@echo "  make logs      - follow logs (SERVICE=notification-service)"
	@echo "  make ps        - show service status"
	@echo "  make clean-db  - truncate notifications table"
	@echo "  make reset     - stop services and remove volumes (FULL RESET)"
	@echo "  make test-unit - run unit tests only (exclude integration)"
	@echo "  make test      - run all tests"
	@echo "  make coverage  - run tests and generate coverage report"

up:
	docker compose up --build -d

down:
	docker compose down

build:
	docker compose build

restart:
	docker compose down
	docker compose up --build -d

logs:
	docker compose logs -f $(SERVICE)

ps:
	docker compose ps

clean-db:
	docker compose exec -T postgres psql -U notification -d notification_db -c "TRUNCATE TABLE notifications RESTART IDENTITY;"

reset:
	docker compose down -v

test-unit:
	mvn -Dtest=*Test,!*IntegrationTest test

test:
	mvn test

coverage:
	mvn test
