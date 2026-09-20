package api

import (
	"bytes"
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
)

func init() {
	gin.SetMode(gin.TestMode)
}

type MockOrderProducer struct {
	publishErr error
	calls      int
}

func (m *MockOrderProducer) PublishOrderCreated(orderID string, userID int64, asset string, side string, size float64, price float64) error {
	m.calls++
	return m.publishErr
}

func TestHealthEndpoint_GivenRunningService_WhenHealthChecked_ThenReturnsStatusUp(t *testing.T) {
	// Given: initialized router and HTTP test recorder
	wsHub := NewWSHub()
	producer := &MockOrderProducer{}
	router := NewRouter(producer, wsHub)
	engine := router.SetupRoutes()

	w := httptest.NewRecorder()
	req, _ := http.NewRequest("GET", "/health", nil)

	// When: health endpoint is requested
	engine.ServeHTTP(w, req)

	// Then: HTTP 200 with status UP is returned
	if w.Code != http.StatusOK {
		t.Fatalf("Expected status 200, got %d", w.Code)
	}

	var body map[string]string
	if err := json.Unmarshal(w.Body.Bytes(), &body); err != nil {
		t.Fatalf("Failed to parse response body: %v", err)
	}
	if body["status"] != "UP" {
		t.Errorf("Expected status UP, got %s", body["status"])
	}
}

func TestCreateOrder_GivenInvalidPayloads_WhenSubmitted_ThenReturns400BadRequest(t *testing.T) {
	// Given: router configured with validation test cases
	wsHub := NewWSHub()
	producer := &MockOrderProducer{}
	router := NewRouter(producer, wsHub)
	engine := router.SetupRoutes()

	testCases := []struct {
		name       string
		payload    string
		expectCode int
	}{
		{
			name:       "Empty JSON",
			payload:    "{}",
			expectCode: http.StatusBadRequest,
		},
		{
			name:       "Missing asset",
			payload:    `{"userId": 1, "side": "BUY", "size": 1.0, "price": 50000.0}`,
			expectCode: http.StatusBadRequest,
		},
		{
			name:       "Negative size",
			payload:    `{"userId": 1, "asset": "BTC-USD", "side": "BUY", "size": -1.0, "price": 50000.0}`,
			expectCode: http.StatusBadRequest,
		},
		{
			name:       "Zero price",
			payload:    `{"userId": 1, "asset": "BTC-USD", "side": "BUY", "size": 1.0, "price": 0.0}`,
			expectCode: http.StatusBadRequest,
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			// Given: invalid payload
			w := httptest.NewRecorder()
			req, _ := http.NewRequest("POST", "/api/v1/trading/orders", bytes.NewBufferString(tc.payload))
			req.Header.Set("Content-Type", "application/json")

			// When: order creation endpoint is called
			engine.ServeHTTP(w, req)

			// Then: HTTP 400 Bad Request is returned
			if w.Code != tc.expectCode {
				t.Errorf("Test %s: expected status %d, got %d", tc.name, tc.expectCode, w.Code)
			}
		})
	}
}

func TestCreateOrder_GivenValidOrderRequest_WhenSubmitted_ThenAcceptsOrderAndPublishesEvent(t *testing.T) {
	// Given: initialized router with mock producer and valid order payload
	wsHub := NewWSHub()
	producer := &MockOrderProducer{}
	router := NewRouter(producer, wsHub)
	engine := router.SetupRoutes()

	orderReq := OrderRequest{
		UserID: 42,
		Asset:  "BTC-USD",
		Side:   "BUY",
		Size:   0.75,
		Price:  49500.0,
	}
	body, _ := json.Marshal(orderReq)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest("POST", "/api/v1/trading/orders", bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")

	// When: valid order is posted
	engine.ServeHTTP(w, req)

	// Then: HTTP 202 Accepted returned and order event published to Kafka
	if w.Code != http.StatusAccepted {
		t.Fatalf("Expected status 202 Accepted, got %d. Body: %s", w.Code, w.Body.String())
	}

	var resp map[string]interface{}
	if err := json.Unmarshal(w.Body.Bytes(), &resp); err != nil {
		t.Fatalf("Failed to parse response: %v", err)
	}
	if resp["orderId"] == nil || resp["orderId"] == "" {
		t.Errorf("Expected valid orderId in response")
	}
	if producer.calls != 1 {
		t.Errorf("Expected 1 call to producer, got %d", producer.calls)
	}
}

func TestCreateOrder_GivenFailingProducer_WhenOrderSubmitted_ThenReturns500InternalServerError(t *testing.T) {
	// Given: mock producer configured with Kafka failure
	wsHub := NewWSHub()
	producer := &MockOrderProducer{publishErr: errors.New("kafka connection failure")}

	router := NewRouter(producer, wsHub)
	engine := router.SetupRoutes()

	orderReq := OrderRequest{
		UserID: 42,
		Asset:  "BTC-USD",
		Side:   "BUY",
		Size:   1.0,
		Price:  50000.0,
	}
	body, _ := json.Marshal(orderReq)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest("POST", "/api/v1/trading/orders", bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")

	// When: order is submitted
	engine.ServeHTTP(w, req)

	// Then: HTTP 500 Internal Server Error returned
	if w.Code != http.StatusInternalServerError {
		t.Errorf("Expected status 500, got %d", w.Code)
	}
}

func TestWSHub_GivenConnectedClient_WhenDataBroadcast_ThenDeliversPayloadAndHandlesDisconnect(t *testing.T) {
	// Given: running WebSocket hub and test server
	wsHub := NewWSHub()
	go wsHub.Run()

	producer := &MockOrderProducer{}
	router := NewRouter(producer, wsHub)
	engine := router.SetupRoutes()

	server := httptest.NewServer(engine)
	defer server.Close()

	u, _ := url.Parse(server.URL)
	u.Scheme = "ws"
	u.Path = "/ws"

	// Given: connected WebSocket client
	wsConn, _, err := websocket.DefaultDialer.Dial(u.String(), nil)
	if err != nil {
		t.Fatalf("Failed to dial websocket: %v", err)
	}

	time.Sleep(50 * time.Millisecond)

	// When: market data is broadcast
	wsHub.BroadcastData("trade", "BTC-USD", map[string]interface{}{"price": 50000.0, "size": 0.5})

	// Then: message received by client
	_ = wsConn.SetReadDeadline(time.Now().Add(2 * time.Second))
	msgType, message, err := wsConn.ReadMessage()
	if err != nil {
		t.Fatalf("Failed to read WS message: %v", err)
	}
	if msgType != websocket.TextMessage {
		t.Errorf("Expected text message type, got %d", msgType)
	}

	var md MarketData
	if err := json.Unmarshal(message, &md); err != nil {
		t.Fatalf("Failed to parse MarketData: %v", err)
	}
	if md.Type != "trade" || md.Asset != "BTC-USD" {
		t.Errorf("Unexpected MarketData: %+v", md)
	}

	// When: client writes message and disconnects
	err = wsConn.WriteMessage(websocket.TextMessage, []byte("ping"))
	if err != nil {
		t.Errorf("Failed to write to WS: %v", err)
	}

	_ = wsConn.Close()
	time.Sleep(50 * time.Millisecond)
	// Then: client unregistered cleanly
}

func TestWS_GivenNonWebsocketRequest_WhenAccessingWsRoute_ThenReturns400UpgradeError(t *testing.T) {
	// Given: router with WebSocket endpoint
	wsHub := NewWSHub()
	producer := &MockOrderProducer{}
	router := NewRouter(producer, wsHub)
	engine := router.SetupRoutes()

	// When: standard HTTP GET requested without WS upgrade headers
	w := httptest.NewRecorder()
	req, _ := http.NewRequest("GET", "/ws", nil)
	engine.ServeHTTP(w, req)

	// Then: HTTP 400 Bad Request returned
	if w.Code != http.StatusBadRequest {
		t.Errorf("Expected status 400 for invalid upgrade, got %d", w.Code)
	}
}
