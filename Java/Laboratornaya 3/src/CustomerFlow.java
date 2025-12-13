import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomerFlow implements Runnable {
    private final BlockingQueue<Waiter.CustomerOrder> orderQueue;
    private final AtomicInteger customerIdCounter;
    private volatile boolean running=true;
    public CustomerFlow(BlockingQueue<Waiter.CustomerOrder> orderQueue, AtomicInteger customerIdCounter){
        this.orderQueue=orderQueue;
        this.customerIdCounter=customerIdCounter;
    }
    @Override
    public void run(){
        Random random=new Random();
        while (running&& !Thread.currentThread().isInterrupted()){
            try{
                Dish dish = Menu.getRandomDish();  // Случайное блюдо
                int customerId = customerIdCounter.getAndIncrement();  // Задаем уникальный id
                Waiter.CustomerOrder order = new Waiter.CustomerOrder(customerId, dish);
                synchronized (System.out) {
                    System.out.println("Клиент создал заказ " + customerId + ": " + dish.getName());
                }
                orderQueue.put(order);  // Помещаем в очередь
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Клиенты закончились");
    }
    public void stopRunning() {
        running = false;
            }
        }
