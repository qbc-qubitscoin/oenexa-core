package kafka

import (
	"context"
	"errors"
	"testing"

	"github.com/segmentio/kafka-go"
)

type MockMessageWriter struct {
	writeErr error
	closeErr error
	messages []kafka.Message
}

func (m *MockMessageWriter) WriteMessages(ctx context.Context, msgs ...kafka.Message) error {
	if m.writeErr != nil {
		return m.writeErr
	}
	m.messages = append(m.messages, msgs...)
	return nil
}

func (m *MockMessageWriter) Close() error {
	return m.closeErr
}

func TestEventProducer_GivenValidOrder_WhenPublished_ThenWritesFormattedMessageToKafka(t *testing.T) {
	// Given: initialized EventProducer with mock writer
	mockWriter := &MockMessageWriter{}
	producer := NewEventProducerWithWriter(mockWriter)
	defer producer.Close()

	// When: PublishOrderCreated is called
	err := producer.PublishOrderCreated("test-ord-1", 101, "BTC-USD", "BUY", 0.5, 52000.0)

	// Then: message is successfully produced to Kafka with correct partition key
	if err != nil {
		t.Fatalf("Failed to publish order: %v", err)
	}

	if len(mockWriter.messages) != 1 {
		t.Fatalf("Expected 1 message published, got %d", len(mockWriter.messages))
	}
	if string(mockWriter.messages[0].Key) != "test-ord-1" {
		t.Errorf("Expected key 'test-ord-1', got '%s'", string(mockWriter.messages[0].Key))
	}
}

func TestEventProducer_GivenFailingWriter_WhenPublishAttempted_ThenReturnsError(t *testing.T) {
	// Given: EventProducer with failing writer
	mockWriter := &MockMessageWriter{writeErr: errors.New("kafka network error")}
	producer := NewEventProducerWithWriter(mockWriter)

	// When: PublishOrderCreated is attempted
	err := producer.PublishOrderCreated("test-ord-2", 102, "BTC-USD", "SELL", 0.5, 52000.0)

	// Then: write error is returned to caller
	if err == nil {
		t.Errorf("Expected error from WriteMessages, got nil")
	}
}

func TestEventProducer_GivenOpenOrNilWriter_WhenCloseIsInvoked_ThenHandlesCloseGracefully(t *testing.T) {
	// Given: producer with close error
	mockWriter := &MockMessageWriter{closeErr: errors.New("close failure")}
	producer := NewEventProducerWithWriter(mockWriter)

	// When: Close is called
	err := producer.Close()

	// Then: close error is propagated
	if err == nil {
		t.Errorf("Expected close error, got nil")
	}

	// Given: producer with nil writer
	nilProducer := &EventProducer{writer: nil}

	// When & Then: Close returns nil without panicking
	if err := nilProducer.Close(); err != nil {
		t.Errorf("Expected nil error on nil writer close, got %v", err)
	}
}

func TestNewEventProducer_GivenBrokerListAndTopic_WhenInstantiated_ThenCreatesConfiguredProducer(t *testing.T) {
	// Given: broker list and topic
	brokers := []string{"localhost:9092"}
	topic := "trading-orders"

	// When: NewEventProducer is called
	producer := NewEventProducer(brokers, topic)

	// Then: non-nil producer and writer are initialized
	if producer == nil || producer.writer == nil {
		t.Fatalf("Expected non-nil EventProducer and writer")
	}
	_ = producer.Close()
}
