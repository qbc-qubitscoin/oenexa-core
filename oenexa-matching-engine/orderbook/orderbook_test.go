package orderbook

import (
	"testing"
)

func TestOrderBook_GivenRestingAsk_WhenTakerBuyCrossesSpread_ThenExecutesPartialFill(t *testing.T) {
	// Given: an initialized orderbook with a resting maker ask (sell 1.0 BTC @ $50,000)
	ob := NewOrderBook("BTC-USD")

	makerSell := &Order{
		ID:        "maker-sell-1",
		UserID:    1,
		Asset:     "BTC-USD",
		Size:      1.0,
		Price:     50000.0,
		Side:      Sell,
		Timestamp: 1000,
	}

	tradesInit := ob.AddOrder(makerSell)
	if len(tradesInit) != 0 {
		t.Fatalf("Expected 0 initial trades, got %d", len(tradesInit))
	}
	if len(ob.Asks) != 1 {
		t.Fatalf("Expected 1 resting order in Asks, got %d", len(ob.Asks))
	}

	// Given: incoming taker buy order crossing spread (buy 0.5 BTC @ $51,000)
	takerBuy := &Order{
		ID:        "taker-buy-1",
		UserID:    2,
		Asset:     "BTC-USD",
		Size:      0.5,
		Price:     51000.0,
		Side:      Buy,
		Timestamp: 2000,
	}

	// When: order is processed by matching engine
	trades := ob.AddOrder(takerBuy)

	// Then: trade executes at maker price for 0.5 BTC, leaving 0.5 BTC resting ask
	if len(trades) != 1 {
		t.Fatalf("Expected 1 trade executed, got %d", len(trades))
	}

	trade := trades[0]
	if trade.Price != 50000.0 {
		t.Errorf("Expected trade price 50000.0, got %f", trade.Price)
	}
	if trade.Size != 0.5 {
		t.Errorf("Expected trade size 0.5, got %f", trade.Size)
	}

	if len(ob.Asks) != 1 {
		t.Fatalf("Expected 1 remaining order in Asks, got %d", len(ob.Asks))
	}
	if ob.Asks[0].Size != 0.5 {
		t.Errorf("Expected remaining ask size 0.5, got %f", ob.Asks[0].Size)
	}
	if len(ob.Bids) != 0 {
		t.Fatalf("Expected 0 resting orders in Bids, got %d", len(ob.Bids))
	}
}

func TestOrderBook_GivenRestingBids_WhenTakerSellCrossesSpread_ThenExecutesFullAndPartialFills(t *testing.T) {
	// Given: orderbook with two resting bids at $49,000 and $50,000
	ob := NewOrderBook("BTC-USD")

	makerBuy1 := &Order{
		ID:        "maker-buy-1",
		UserID:    10,
		Asset:     "BTC-USD",
		Size:      1.0,
		Price:     49000.0,
		Side:      Buy,
		Timestamp: 1000,
	}
	makerBuy2 := &Order{
		ID:        "maker-buy-2",
		UserID:    11,
		Asset:     "BTC-USD",
		Size:      1.0,
		Price:     50000.0,
		Side:      Buy,
		Timestamp: 1100,
	}

	ob.AddOrder(makerBuy1)
	ob.AddOrder(makerBuy2)

	if ob.Bids[0].Price != 50000.0 {
		t.Errorf("Expected highest bid first at 50000, got %f", ob.Bids[0].Price)
	}

	// Given: incoming taker sell order for 1.5 BTC at $48,000
	takerSell := &Order{
		ID:        "taker-sell-1",
		UserID:    12,
		Asset:     "BTC-USD",
		Size:      1.5,
		Price:     48000.0,
		Side:      Sell,
		Timestamp: 1200,
	}

	// When: order is processed by matching engine
	trades := ob.AddOrder(takerSell)

	// Then: 2 trades executed (full fill of 50000 bid, partial 0.5 fill of 49000 bid)
	if len(trades) != 2 {
		t.Fatalf("Expected 2 trades, got %d", len(trades))
	}

	if trades[0].Price != 50000.0 || trades[0].Size != 1.0 {
		t.Errorf("Unexpected trade 0: %+v", trades[0])
	}
	if trades[1].Price != 49000.0 || trades[1].Size != 0.5 {
		t.Errorf("Unexpected trade 1: %+v", trades[1])
	}

	// Then: remaining bid in book has 0.5 size at $49,000
	if len(ob.Bids) != 1 {
		t.Fatalf("Expected 1 remaining bid, got %d", len(ob.Bids))
	}
	if ob.Bids[0].Size != 0.5 {
		t.Errorf("Expected remaining size 0.5, got %f", ob.Bids[0].Size)
	}
}

func TestOrderBook_GivenOrdersWithSamePrice_WhenAddedToBook_ThenEnforcesTimestampTieBreaking(t *testing.T) {
	// Given: orderbook with orders at identical price levels but differing timestamps
	ob := NewOrderBook("BTC-USD")

	bid1 := &Order{ID: "b1", UserID: 1, Price: 50000, Size: 1.0, Side: Buy, Timestamp: 2000}
	bid2 := &Order{ID: "b2", UserID: 2, Price: 50000, Size: 1.0, Side: Buy, Timestamp: 1000}

	// When: bids are added to the book
	ob.AddOrder(bid1)
	ob.AddOrder(bid2)

	// Then: earlier timestamp is positioned first (FIFO tie-breaking)
	if ob.Bids[0].ID != "b2" {
		t.Errorf("Expected b2 first in bids, got %s", ob.Bids[0].ID)
	}

	// Given: asks with identical price levels
	ask1 := &Order{ID: "a1", UserID: 3, Price: 51000, Size: 1.0, Side: Sell, Timestamp: 4000}
	ask2 := &Order{ID: "a2", UserID: 4, Price: 51000, Size: 1.0, Side: Sell, Timestamp: 3000}

	// When: asks are added
	ob.AddOrder(ask1)
	ob.AddOrder(ask2)

	// Then: earlier timestamp is prioritized first
	if ob.Asks[0].ID != "a2" {
		t.Errorf("Expected a2 first in asks, got %s", ob.Asks[0].ID)
	}

	// Given: higher ask order
	ask3 := &Order{ID: "a3", UserID: 5, Price: 52000, Size: 1.0, Side: Sell, Timestamp: 5000}

	// When: added
	ob.AddOrder(ask3)

	// Then: sorted ascending by price
	if ob.Asks[len(ob.Asks)-1].ID != "a3" {
		t.Errorf("Expected a3 last in asks, got %s", ob.Asks[len(ob.Asks)-1].ID)
	}
}

func TestOrderBook_GivenNonCrossingAndPartialOrders_WhenProcessed_ThenRestsUnfilledPortionInBook(t *testing.T) {
	// Given: orderbook with resting ask at 52000
	ob := NewOrderBook("BTC-USD")
	ob.AddOrder(&Order{ID: "ask1", UserID: 1, Price: 52000, Size: 1.0, Side: Sell, Timestamp: 1000})

	// When: non-crossing buy order arrives (51000 < 52000)
	trades := ob.AddOrder(&Order{ID: "buy1", UserID: 2, Price: 51000, Size: 2.0, Side: Buy, Timestamp: 1100})

	// Then: 0 trades executed and order rests in Bids
	if len(trades) != 0 {
		t.Fatalf("Expected 0 trades, got %d", len(trades))
	}
	if len(ob.Bids) != 1 || ob.Bids[0].ID != "buy1" {
		t.Fatalf("Expected buy1 in Bids")
	}

	// When: non-crossing sell order arrives (51500 > 51000)
	trades2 := ob.AddOrder(&Order{ID: "sell2", UserID: 3, Price: 51500, Size: 1.0, Side: Sell, Timestamp: 1200})

	// Then: 0 trades executed and order rests in Asks
	if len(trades2) != 0 {
		t.Fatalf("Expected 0 trades, got %d", len(trades2))
	}
	if len(ob.Asks) != 2 {
		t.Fatalf("Expected 2 asks, got %d", len(ob.Asks))
	}

	// When: taker sell arrives for 3.0 @ 51000 (partially fills 2.0 of buy1)
	trades3 := ob.AddOrder(&Order{ID: "sell3", UserID: 4, Price: 51000, Size: 3.0, Side: Sell, Timestamp: 1300})

	// Then: 1 trade for 2.0 executed, bids emptied, remaining 1.0 rests in asks
	if len(trades3) != 1 {
		t.Fatalf("Expected 1 trade, got %d", len(trades3))
	}
	if trades3[0].Size != 2.0 {
		t.Errorf("Expected trade size 2.0, got %f", trades3[0].Size)
	}
	if len(ob.Bids) != 0 {
		t.Fatalf("Expected 0 bids, got %d", len(ob.Bids))
	}
	foundSell3 := false
	for _, a := range ob.Asks {
		if a.ID == "sell3" && a.Size == 1.0 {
			foundSell3 = true
		}
	}
	if !foundSell3 {
		t.Errorf("Expected sell3 with remaining size 1.0 in Asks")
	}

	// When: taker buy for 2.5 @ 51000 arrives (fills remaining 1.0 of sell3)
	trades4 := ob.AddOrder(&Order{ID: "buy4", UserID: 5, Price: 51000, Size: 2.5, Side: Buy, Timestamp: 1400})

	// Then: 1 trade for 1.0 executed, and remaining 1.5 rests in bids
	if len(trades4) != 1 {
		t.Fatalf("Expected 1 trade, got %d", len(trades4))
	}
	if trades4[0].Size != 1.0 {
		t.Errorf("Expected trade size 1.0, got %f", trades4[0].Size)
	}
	if len(ob.Bids) != 1 || ob.Bids[0].ID != "buy4" || ob.Bids[0].Size != 1.5 {
		t.Errorf("Expected buy4 with remaining size 1.5 in Bids")
	}
}
