package main

import (
	"log"
	"math/rand"
	"os"
	"time"

	"github.com/oenexa/trading-service/api"
	"github.com/oenexa/trading-service/kafka"
)

func main() {
	log.Println("Starting OENEXA Trading Service...")

	kafkaBrokers := []string{"localhost:9092"}
	if broker := os.Getenv("KAFKA_BROKERS"); broker != "" {
		kafkaBrokers = []string{broker}
	}

	producer := kafka.NewEventProducer(kafkaBrokers, "trading.order.created")
	defer producer.Close()

	// Initialize and start WebSocket Hub
	wsHub := api.NewWSHub()
	go wsHub.Run()

	// Background ticker to simulate real-time price updates for UI testing
	go func() {
		price := 63163.11 // Starting BTC price
		for {
			time.Sleep(2 * time.Second)

			// Randomly move price between -100 and +100
			delta := (rand.Float64() * 200) - 100
			price += delta

			wsHub.BroadcastData("ticker", "BTC", map[string]interface{}{
				"price":  price,
				"change": "+0.42%", // Mock change
			})
		}
	}()

	router := api.NewRouter(producer, wsHub)
	app := router.SetupRoutes()

	port := os.Getenv("PORT")
	if port == "" {
		port = "8084"
	}

	log.Printf("Trading Service running on port %s", port)
	if err := app.Run(":" + port); err != nil {
		log.Fatalf("Failed to run server: %v", err)
	}
}
