container=[1, 8, 1000, 2, 5, 4, 8, 3, 8]
left=0
right=len(container)-1
maxS=0
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

