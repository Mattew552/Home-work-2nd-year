import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Menu {
    private static final List<Dish> dishes= Arrays.asList(//название, время приготовления
            new Dish("Салат Оливье", 2000),
            new Dish("Шаурма с курицей", 2500),
            new Dish("Пицца", 6000),
            new Dish("Суп борщ", 10000),
            new Dish("Водичка (африканская)", 500)




    );
    private static final Random random=new Random();//рандомное блюдо из меню
    public static Dish getRandomDish(){
        return dishes.get(random.nextInt(dishes.size()));
    }
}

