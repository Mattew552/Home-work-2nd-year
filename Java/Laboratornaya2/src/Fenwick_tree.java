public class Fenwick_tree<T extends Number>{
    private int[] tree;
    private int n;
    public void build (T[] arr){
        if(arr==null){
            throw new IllegalArgumentException("Массив не может быть с нулевым значением");
        }
        if(arr.length==0){
            throw new IllegalArgumentException("Массив не может быть пустым");
        }
        this.n=arr.length;
        this.tree=new int[n+1];
        for (int i=0;i<n;i++){

            update(i, arr[i].intValue());
        }
    }
    public void update(int index,int delta){ //функция добавляет какое-то число к элементу
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if (index<0||index>=n){
            throw new IndexOutOfBoundsException("Индекс за границами " + index);
        }
        index+=1;//+1, тк во всех гайдах массивы с единицы, кроме того, так корректно работает index&-index
        while(index<=n){
            tree[index]+=delta;
            index+=index&-index;
        }

    }
    public int prefixSum( int index){//сумма от 0 до индекса
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if (index<0||index>=n){
            throw new IndexOutOfBoundsException("Индекс за границами " + index);
        }
        int sum=0;
        index+=1;
        while(index>0){
            sum+=tree[index];
            index-=index&-index;

        }
        return sum;
    }
    public int rangeSum(int left, int right){//сумма от левого элемента до правого
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if(left<0||right<0||left>=n||right>=n){
            throw new IndexOutOfBoundsException("Диапазон за границами");
        }

        if (left>right){
            throw new IllegalArgumentException("Левый индекс не может быть больше правого");
        }
        else{
            return prefixSum(right)-(left>0?prefixSum(left-1):0);
        }
    }
    //Доп математика, не участвующая в основной части лабы
    public int getValue(int index){//получает значение по индексу
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if (index<0||index>=n){
            throw new IndexOutOfBoundsException("Индекс за границами " + index);
        }
        int a=prefixSum(index);
        int b=(index==0)?0:prefixSum(index-1);//без тернарного оператора в функции принта выходит исключение из-за некорректного индекса
        return a-b;
    }
    public void setValue(int index, int value){//вместо дельты в update теперь можно просто задать значение
        if (tree == null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if (index < 0 || index >= n){
            throw new IndexOutOfBoundsException("Индекс за границами " + index);
        }
        int current=getValue(index);
        int d=value-current;
        update(index, d);
    }
    public void increaseRange(int left, int right, int delta){//добавляем некоторую дельту к нескольким элементам за запрос
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if(left<0||right<0||left>=n||right>=n){
            throw new IndexOutOfBoundsException("Диапазон за границами");
        }

        if (left>right){
            throw new IllegalArgumentException("Левый индекс не может быть больше правого");
        }
        for (int i=left; i<=right;i++){
            update(i, delta);
        }
    }
    public int findPrefixSum(int target){//бинарным поиском ищем первую префиксную сумму, которая больше таргета
        if (tree == null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if (target <= 0){
            throw new IllegalArgumentException("target должен быть положительным");
        }
        int sum = 0;
        int pos = 0;
        int i = Integer.highestOneBit(n);
        while (i!=0){
            if (pos+i<=n&&sum+tree[pos+i]<target){
                sum+=tree[pos+i];
                pos+=i;
            }
            i=i/2;
        }

        return pos;
    }
    public void printArray() {//вывод массива после применения разных функций
        if (tree == null) {
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }

        System.out.print("Текущий массив: [");
        for (int i = 0; i < n; i++) {
            int val = getValue(i);
            System.out.print(val);
            if (i < n - 1) System.out.print(", ");
        }

        System.out.println("]");
    }
}