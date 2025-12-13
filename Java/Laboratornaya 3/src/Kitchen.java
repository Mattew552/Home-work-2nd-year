
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Kitchen {
    private final ExecutorService kitchenPool;  // Пул потоков поваров
    private final BlockingQueue<Waiter.CustomerOrder> kitchenQueue = new LinkedBlockingQueue<>();    // Очередь кухни
    private final AtomicInteger cookIds = new AtomicInteger(1);  // Атомарный id поваров
    private volatile boolean running = true;  // Флаг работы

    public Kitchen(int numOfCooks) {
        this.kitchenPool = Executors.newFixedThreadPool(numOfCooks);  // Фиксированный пул поваров
        for (int i = 0; i < numOfCooks; i++) {
            kitchenPool.execute(new CookRunner(cookIds.getAndIncrement()));  // Запуск нужного количества поваров
        }
    }


    private class CookRunner implements Runnable {
        private final int cookId;

        CookRunner(int cookId) {
            this.cookId = cookId;
        }

        @Override
        public void run() {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Берем заказ из очереди
                    Waiter.CustomerOrder order = kitchenQueue.take();
                    String dishName = order.dish.getName();
                    int cookingTime = order.dish.getCookingTime();
                    synchronized (System.out) {
                        System.out.println("Повар " + cookId + " начинает готовить заказ #" + order.customerId + ": " + dishName);
                    }
                    Thread.sleep(cookingTime);  // Симуляция приготовления
                    synchronized (System.out) {
                        System.out.println("Повар " + cookId + " приготовил заказ " + order.customerId + ": " + dishName);
                    }
                    String result = "Блюдо '" + dishName + "' (повар " + cookId + ") готово";
                    synchronized (order) {  // Захватываем монитор заказа
                        order.setResult(result);  // Устанавливаем результат
                        order.notify();  // Сигнал официанту
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("Повар " + cookId + " завершил работу");
        }
    }

  //Помещаем заказ в очередь кухни
    public void prepareOrder(Waiter.CustomerOrder order) {
        try {
            kitchenQueue.put(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



    public void shutdown() {
        running = false;
        kitchenPool.shutdownNow();  // Прерывает потоки
    }
}

