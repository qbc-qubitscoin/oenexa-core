package main

import (
	"log"
	"os"

	"github.com/oenexa/matching-engine/kafka"
	"github.com/oenexa/matching-engine/orderbook"
)

func main() {
	log.Println("Starting OENEXA Matching Engine...")

	kafkaBrokers := []string{"localhost:9092"}
	if broker := os.Getenv("KAFKA_BROKERS"); broker != "" {
		kafkaBrokers = []string{broker}
	}

	ob := orderbook.NewOrderBook("BTC-USD") // We can support multiple later

	tradeProducer := kafka.NewTradeProducer(kafkaBrokers, "trading.order.matched")
	defer tradeProducer.Close()

	consumer := kafka.NewEventConsumer(
		kafkaBrokers,
		"trading.order.created",
		"oenexa-matching-engine-group",
		ob,
		tradeProducer,
	)

	// Start consuming in a goroutine or block main
	consumer.Start()
}
