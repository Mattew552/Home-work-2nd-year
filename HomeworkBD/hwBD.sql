/*select "Good".id, "Good".name, "Warehouse".address from "Good" left outer join "Good Warehouse"
on ("Good".id="Good Warehouse".id_good) left outer join "Warehouse" on ("Warehouse".id=
"Good Warehouse".id_warehouse)*/

/*select "Shop".id, "Shop".name, "GoodCategory".name from "Shop" left outer join "Good_Shop"
on ("Shop".id="Good_Shop".id_shop) left outer join "Good" on ("Good".id="Good_Shop".id_good)
left outer join "GoodCategory" on ("GoodCategory".id="Good"."id_goodCategory")*/



select "Good".id, "Good".name, "GoodCategory".name, "Warehouse".address from 
"Good Warehouse" left outer join "Warehouse"
on ("Good Warehouse"."id_warehouse"="Warehouse".id) left outer join "Good"
on ("Good Warehouse"."id_good"="Good".id) left outer join "GoodCategory" 
on ("Good"."id_goodCategory"="GoodCategory".id)