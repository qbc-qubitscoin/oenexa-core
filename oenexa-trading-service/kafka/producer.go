package kafka

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	"github.com/segmentio/kafka-go"
)

type MessageWriter interface {
	WriteMessages(ctx context.Context, msgs ...kafka.Message) error
	Close() error
}

type EventProducer struct {
	writer MessageWriter
}

func NewEventProducerWithWriter(writer MessageWriter) *EventProducer {
	return &EventProducer{writer: writer}
}

func NewEventProducer(brokers []string, topic string) *EventProducer {
	w := &kafka.Writer{
		Addr:                   kafka.TCP(brokers...),
		Topic:                  topic,
		Balancer:               &kafka.LeastBytes{},
		AllowAutoTopicCreation: true,
		MaxAttempts:            10,
	}
	return &EventProducer{writer: w}
}

func (p *EventProducer) PublishOrderCreated(orderID string, userID int64, asset string, side string, size float64, price float64) error {
	// Java side expects JSON string payload
	event := map[string]interface{}{
		"orderId":   orderID,
		"userId":    userID,
		"asset":     asset,
		"side":      side,
		"size":      size,
		"price":     price,
		"timestamp": time.Now().UnixMilli(),
	}

	payloadBytes, err := json.Marshal(event)
	if err != nil {
		return fmt.Errorf("failed to marshal event: %w", err)
	}

	// Convert bytes to string as Java side expects String payloads directly
	payloadStr := string(payloadBytes)

	msg := kafka.Message{
		Key:   []byte(orderID),
		Value: []byte(payloadStr),
	}

	err = p.writer.WriteMessages(context.Background(), msg)
	if err != nil {
		log.Printf("Failed to produce message: %v\n", err)
		return err
	}

	log.Printf("Produced event to Kafka: %s\n", payloadStr)
	return nil
}

func (p *EventProducer) Close() error {
	if p.writer != nil {
		if err := p.writer.Close(); err != nil {
			log.Printf("failed to close writer: %v", err)
			return err
		}
	}
	return nil
}
