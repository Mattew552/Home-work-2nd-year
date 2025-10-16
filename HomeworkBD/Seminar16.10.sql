/*select name from "GoodCategory" where
"ageMin" = (select max("ageMin") from "GoodCategory");*/
/*select id, name from "Good" where id not in(
select "id_good" from "Good Warehouse" where count>0

union
select "id_good" from "Good_Shop" where count>0);*/
select "Good".id, "Good".name from "Good" left outer join
"Good Warehouse" on ("Good".id="Good Warehouse".id_good) left outer join "Good_Shop" on 
("Good".id="Good_Shop".id_good)
where ("Good Warehouse".count=0) and
("Good_Shop".count=0);