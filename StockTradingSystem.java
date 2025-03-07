import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// ==============================
// Order Class (Encapsulation)
// ==============================
class Order {
    enum OrderType {BUY, SELL}

    private final OrderType orderType;
    private final int ticker;
    private final double price;
    private int quantity;
    private final long entryTime;

    public Order(final OrderType orderType, final int ticker, final double price, final int quantity) {
        this.orderType = orderType;
        this.ticker = ticker;
        this.price = price;
        this.quantity = quantity;
        this.entryTime = System.currentTimeMillis();
    }

    public OrderType getOrderType() { return orderType; }
    public int getTicker() { return ticker; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public long getEntryTime() { return entryTime; }

    public void setQuantity(final int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "Order{" +
                "orderType=" + orderType +
                ", ticker=" + ticker +
                ", price=" + price +
                ", quantity=" + quantity +
                ", entryTime=" + entryTime +
                '}';
    }
}

// ==============================
// OrderBook (Optimized with LinkedList)
// ==============================
class OrderBook {
    private static final int MAX_TICKERS = 1024;
    private final LinkedList<Order>[] buyOrders;
    private final LinkedList<Order>[] sellOrders;
    private final ReentrantLock[] locks;

    @SuppressWarnings("unchecked")
    public OrderBook() {
        buyOrders = new LinkedList[MAX_TICKERS];
        sellOrders = new LinkedList[MAX_TICKERS];
        locks = new ReentrantLock[MAX_TICKERS];

        for (int i = 0; i < MAX_TICKERS; i++) {
            buyOrders[i] = new LinkedList<>();
            sellOrders[i] = new LinkedList<>();
            locks[i] = new ReentrantLock();
        }
    }

    public void addOrder(Order order) {
        int tickerId = order.getTicker();
        locks[tickerId].lock();
        try {
            if (order.getOrderType() == Order.OrderType.BUY) {
                buyOrders[tickerId].add(order);
                buyOrders[tickerId].sort(Comparator.comparingDouble(Order::getPrice).reversed()
                                                   .thenComparing(Order::getEntryTime));
            } else {
                sellOrders[tickerId].add(order);
                sellOrders[tickerId].sort(Comparator.comparingDouble(Order::getPrice)
                                                    .thenComparing(Order::getEntryTime));
            }
            matchOrders(tickerId);
        } finally {
            locks[tickerId].unlock();
        }
    }

    private void matchOrders(int ticker) {
        locks[ticker].lock();
        try {
            LinkedList<Order> buyList = buyOrders[ticker];
            LinkedList<Order> sellList = sellOrders[ticker];

            while (!buyList.isEmpty() && !sellList.isEmpty()) {
                Order buyOrder = buyList.getFirst();
                Order sellOrder = sellList.getFirst();

                if (buyOrder.getPrice() >= sellOrder.getPrice()) { 
                    int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

                    System.out.println("Matched " + tradeQuantity + " shares of ticker " +
                            ticker + " at $" + sellOrder.getPrice());

                    buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
                    sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);

                    if (buyOrder.getQuantity() == 0) buyList.removeFirst(); 
                    if (sellOrder.getQuantity() == 0) sellList.removeFirst(); 
                } else {
                    break;
                }
            }
        } finally {
            locks[ticker].unlock();
        }
    }
}

// ==============================
// Trading Simulation (Multi-Threading)
// ==============================
class TradingSimulator implements Runnable {
    private final OrderBook orderBook;
    private static final int MAX_TICKERS = 1024;
    private static final int MAX_ORDERS = 500;
    private static final Random rand = new Random();

    public TradingSimulator(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    @Override
    public void run() {
        for (int i = 0; i < MAX_ORDERS; i++) {
            int ticker = rand.nextInt(MAX_TICKERS);
            Order.OrderType orderType = rand.nextBoolean() ? Order.OrderType.BUY : Order.OrderType.SELL;
            int quantity = rand.nextInt(100) + 1;
            double price = rand.nextInt(491) + 10;

            Order order = new Order(orderType, ticker, price, quantity);
            orderBook.addOrder(order);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

// ==============================
// Main Execution (Runs Threads)
// ==============================
public class StockTradingSystem {
    private static final int NUM_THREADS = 4;

    public static void main(String[] args) {
        OrderBook stockOrderBook = new OrderBook();
        Thread[] traders = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            traders[i] = new Thread(new TradingSimulator(stockOrderBook));
            traders[i].start();
        }

        for (Thread trader : traders) {
            try {
                trader.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Trading session completed.");
    }
}
