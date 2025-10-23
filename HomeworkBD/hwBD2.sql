/* слияние
select "GoodCategory".id, "GoodCategory".name from "GoodCategory" left outer join "Good" on
("GoodCategory".id="Good"."id_goodCategory") left outer join "Good_Shop" on
("Good".id="Good_Shop"."id_good")
where ("Good_Shop".count=0) */


/* in/not in
select "GoodCategory".id, "GoodCategory".name from "GoodCategory" where
"GoodCategory".id in (select "Good"."id_goodCategory" from "Good" where "Good".id in(
select "Good_Shop".id_good from "Good_Shop" where "Good_Shop".count=0))
*/


/* all/any
select "GoodCategory".id, "GoodCategory".name from "GoodCategory" where
"GoodCategory".id =any (select "Good"."id_goodCategory" from "Good" where "Good".id =any(
select "Good_Shop".id_good from "Good_Shop" where "Good_Shop".count=0))
*/
/* exists/not exists
select id, name from "GoodCategory" gc where
not exists(select * from "Good" g join "Good_Shop" gs on g.id=gs.id_good
where g."id_goodCategory"=gc.id and gs.count is not null)
*/
/* агрегатные функции
select "GoodCategory".id, "GoodCategory".name from "GoodCategory" left join "Good" on
("GoodCategory".id="Good"."id_goodCategory") left join "Good_Shop" on
("Good".id="Good_Shop"."id_good")
group by "GoodCategory".id, "GoodCategory".name having count("Good_Shop".id_good)=0;*/