import java.util.Arrays;

public class Main{
    public static void main(String[] args){
        Integer[] arr={3,2,-1,6,5,4};
        Fenwick_tree<Integer> ft =new Fenwick_tree<>();
        ft.build(arr);
        System.out.print("Дерево построено"+ Arrays.toString(arr)+"\n");

        System.out.print("Префиксная сумма первых 4 элементов массива "+ft.prefixSum(3)+" \n");
        System.out.print("Префиксная сумма в некотором диапазоне "+ft.rangeSum(2,4)+" \n");
        System.out.print(ft.prefixSum(0)+"\n");
        ft.update(3,2);
        System.out.print(ft.getValue(3)+"\n");
        System.out.print("Тест функции суммы в обновленном массиве "+ft.rangeSum(2,4)+"\n");
        ft.setValue(1,7);
        System.out.print("Обновленное значение "+ ft.getValue(1)+"\n");
        System.out.print("Префиксная сумма первых 2 элементов массива(обновленного) "+ft.prefixSum(1)+" \n");
        System.out.print("Префиксная сумма первых 4 элементов массива(обновленного) "+ft.prefixSum(3)+" \n");
        ft.increaseRange(2,4,1);
        ft.getValue(2); ft.getValue(3); ft.getValue(4);
        System.out.print(ft.prefixSum(3)+"\n"+ ft.rangeSum(2,4)+"\n");
        for(int i=0;i< arr.length;i++){
            System.out.print(ft.prefixSum(i)+" ");
        }
        System.out.print("\n"+ft.findPrefixSum(9)+" "+"\n");
        /*try{
            ft.update(-1,5);
            ft.prefixSum(20);
            ft.rangeSum(-1,20);
            Fenwick_tree<Integer> ft2=new Fenwick_tree<>();
            ft2.prefixSum(0);
        }
        catch(Exception e){
            System.out.println("Exception "+e.getMessage()+"\n");
        }*/
        ft.printArray();


    }
}