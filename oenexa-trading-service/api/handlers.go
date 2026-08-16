package api

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

type OrderRequest struct {
	UserID int64   `json:"userId" binding:"required"`
	Asset  string  `json:"asset" binding:"required"`
	Side   string  `json:"side" binding:"required"`
	Size   float64 `json:"size" binding:"required,gt=0"`
	Price  float64 `json:"price" binding:"required,gt=0"`
}

type OrderProducer interface {
	PublishOrderCreated(orderID string, userID int64, asset string, side string, size float64, price float64) error
}

type Router struct {
	producer OrderProducer
	wsHub    *WSHub
}

func NewRouter(producer OrderProducer, wsHub *WSHub) *Router {
	return &Router{producer: producer, wsHub: wsHub}
}

func (r *Router) SetupRoutes() *gin.Engine {
	app := gin.Default()

	app.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "UP"})
	})

	// WebSocket Route
	app.GET("/ws", r.wsHub.HandleWS)

	api := app.Group("/api/v1/trading")
	{
		api.POST("/orders", r.createOrder)
	}

	return app
}

func (r *Router) createOrder(c *gin.Context) {
	var req OrderRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Basic generation of OrderID (in production use UUID)
	orderID := "ord-" + c.ClientIP() + "-" + string(rune(req.UserID))

	// Publish to Kafka (wallet service and matching engine listen to this)
	err := r.producer.PublishOrderCreated(
		orderID,
		req.UserID,
		req.Asset,
		req.Side,
		req.Size,
		req.Price,
	)

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to process order"})
		return
	}

	c.JSON(http.StatusAccepted, gin.H{
		"message": "Order accepted and queued for matching",
		"orderId": orderID,
	})
}
