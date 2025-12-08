
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Kitchen {
    private final ExecutorService KitchenPool;
    private final AtomicInteger cookIDs=new AtomicInteger(1);
    public Kitchen(int numOfCooks){
        this.KitchenPool= Executors.newFixedThreadPool(numOfCooks);

    }
    public Future<String> cookDish(Dish dish, int orderID){
        Cook cook=new Cook(dish.getName(), dish.getCookingtime(), cookIDs.getAndIncrement());
        Cook.CookTask task = cook.new CookTask();
        return KitchenPool.submit(task);
    }
    public void shutdown(){
        KitchenPool.shutdown();
    }

}
