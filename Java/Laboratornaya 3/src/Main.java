import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        int numWaiters = 5;
        int numCooks = 3;
        BlockingQueue<Waiter.CustomerOrder> orderQueue = new LinkedBlockingQueue<>();// Очередь клиентов
        Kitchen kitchen = new Kitchen(numCooks);  // Кухня
        AtomicInteger customerIdCounter = new AtomicInteger(1);  // Счетчик id
        AtomicInteger processedOrders=new AtomicInteger(0);//Счетчик обработанных заказов для последнего отчета
        CustomerFlow customerFlow = new CustomerFlow(orderQueue, customerIdCounter);//Генерация потока клиентов
        Thread clientThread = new Thread(customerFlow);
        clientThread.start();

        List<Waiter> waiters = new ArrayList<>();//Лист с официантами
        List<Thread> waiterThreads = new ArrayList<>();
        for (int i = 1; i <= numWaiters; i++) {
            Waiter waiter = new Waiter(i, orderQueue, kitchen, processedOrders);
            Thread thread = new Thread(waiter);
            thread.start();
            waiters.add(waiter);
            waiterThreads.add(thread);
        }
        try {
            Thread.sleep(10000);// Сколько времени будут спавниться клиенты
        } catch (InterruptedException e) {

        }
        // Прерываем все потоки
        customerFlow.stopRunning();
        clientThread.interrupt();
        waiters.forEach(Waiter::stopWorking);
        waiterThreads.forEach(Thread::interrupt);
        kitchen.shutdown();

        System.out.println("Ресторан закрыт");
        System.out.println("Повара в количестве "+numCooks+" и официанты в количестве "+
                numWaiters+" Смогли обслужить "+processedOrders+" Клиентов");
    }


    }

