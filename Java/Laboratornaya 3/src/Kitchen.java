
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Kitchen {
    private final ExecutorService KitchenPool;
    private int cookCounter= 1;
    public Kitchen(int numOfCooks){
        this.KitchenPool= Executors.newFixedThreadPool(numOfCooks);

    }
    public Future<String> cookDish(Dish dish, int orderID){
        Cook cook=new Cook(dish.getName(), dish.getCookingtime(), cookCounter++);
        Cook.CookTask=new Cook.CookTask(cook);
        return KitchenPool.submit(CookTask);
    }
    public void shutdown(){
        KitchenPool.shutdown();
    }

}
