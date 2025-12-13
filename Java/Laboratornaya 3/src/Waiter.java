import java.util.concurrent.BlockingQueue;
public class Waiter implements Runnable{
    private int waiterNumber;
    private BlockingQueue<CustomerOrder> orderQueue;//очередь заказов от клиентов
    private Kitchen kitchen;
    private volatile boolean working=true;//флаг, что бы по нему потом быстро завершить + volatile видны всем потокам


    public static class CustomerOrder {
        public final int customerId;
        public final Dish dish;
        private volatile String result;

        public CustomerOrder(int customerId, Dish dish) {
            this.customerId = customerId;
            this.dish = dish;
            this.result=null;
        }
        public String getResult(){
            return result;
        }
        public void setResult(String result){
            this.result=result;
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
                // Берем заказ из очереди
                CustomerOrder order = orderQueue.take();
                synchronized (System.out) {  // Синхронизированный вывод (Чтобы не было гонки)
                    System.out.println("Официант " + waiterNumber + " принял заказ " + order.customerId + ": " + order.dish.getName());
                }
                // Отправляем на кухню
                kitchen.prepareOrder(order);
                synchronized (order) {  // Захватываем монитор заказа
                    while (order.getResult() == null) {
                        order.wait();  // Ждем notify от повара
                    }
                }
                synchronized (System.out) {
                    System.out.println("Официант " + waiterNumber + " отдал заказ " + order.customerId + " (блюдо: " + order.dish.getName() + "): " + order.getResult());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.out.println("Ошибка у официанта " + waiterNumber + ": " + e.getMessage());
            }
        }
        System.out.println("Официант #" + waiterNumber + " завершил работу");
    }

    public void stopWorking() {
        working = false;  // Остановка цикла
    }
}

