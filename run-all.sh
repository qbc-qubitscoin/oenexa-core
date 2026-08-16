#!/bin/bash
echo "Starting OENEXA Core Project locally..."

# Start Kafka & MySQL (Docker Compose)
echo "Starting Docker containers..."
docker compose up -d || docker-compose up -d
sleep 5

echo "Starting Java Wallet Service..."
./gradlew :oenexa-wallet-service:bootRun &
WALLET_PID=$!

echo "Starting Go Trading Service (with WebSocket Hub)..."
# shellcheck disable=SC2164
cd oenexa-trading-service
go run main.go &
TRADING_PID=$!
# shellcheck disable=SC2103
cd ..

echo "Starting Go Matching Engine..."
# shellcheck disable=SC2164
cd oenexa-matching-engine
go run main.go &
MATCHING_PID=$!
cd ..

echo "Starting React UI..."
# shellcheck disable=SC2164
if [ -d "../oenexa-ui" ]; then
  cd ../oenexa-ui
  npm run dev &
  UI_PID=$!
  # shellcheck disable=SC2103
  cd - > /dev/null
fi

echo "All services started! Press Ctrl+C to stop."

# Wait for termination signal
# shellcheck disable=SC2064
trap "kill $WALLET_PID $TRADING_PID $MATCHING_PID $UI_PID; exit" SIGINT SIGTERM
wait
