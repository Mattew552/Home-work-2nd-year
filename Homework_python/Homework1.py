container=[1, 8, 6, 2, 5, 4, 8, 3, 7] #список стенок
left=0
right=len(container)-1 #принцип 2 указателей, идущих навстречу друг другу
maxS=0 #максимальная площадь
while left<right:
    h=min(container[left], container[right])
    l=right-left
    curS=h*l
    if curS>maxS:
        maxS=curS
    if container[left]<container[right]:
        left+=1
    else:
        right-=1
print(maxS)

