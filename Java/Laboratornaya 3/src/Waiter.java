import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

public class Waiter implements Runnable{
    private int waiterNumber;
    private BlockingQueue<CustomerOrder> orderQueue;
    private Kitchen kitchen;
    private volatile boolean working=true;
    public static class CustomerOrder {
        public final int customerId;
        public final Dish dish;

        public CustomerOrder(int customerId, Dish dish) {
            this.customerId = customerId;
            this.dish = dish;
        }
    }
    public Waiter(int waiterNumber, BlockingQueue<CustomerOrder> orderQueue, Kitchen kitchen) {
        this.waiterNumber = waiterNumber;
        this.orderQueue = orderQueue;
        this.kitchen = kitchen;
    }









    @Override
    public void run() {
        System.out.println("Официант " + waiterNumber + " начал работу");
        while (working && !Thread.currentThread().isInterrupted()) {
            try {
                CustomerOrder order = orderQueue.take();

                System.out.println("Официант #" + waiterNumber +
                        " принял заказ #" + order.customerId +
                        ": " + order.dish.getName());
                Future<String> cookingResult = kitchen.cookDish(order.dish, order.customerId);
                String result = cookingResult.get();
                System.out.println("Официант #" + waiterNumber +
                        " отдал заказ #" + order.customerId +
                        " (блюдо: " + order.dish.getName() + ")");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;


            } catch (Exception e) {
                System.out.println("Ошибка у официанта #" + waiterNumber + ": " + e.getMessage());
            }

        }
        System.out.println("Официант #" + waiterNumber + " завершил работу");



    }
    public void stopWorking(){
        working=false;
    }
}
