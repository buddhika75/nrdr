SET FOREIGN_KEY_CHECKS=0;
DELETE from billfee where `ID` > 0;
DELETE from bill where `ID` > 0;
DELETE from billitem where `ID` > 0;
DELETE from bill where `ID` > 0;
delete from billnumber WHERE `ID`>0;
drop table bill;
drop TABLE billcomponent;
drop TABLE billentry;
drop TABLE billfee;
drop TABLE billnumber;
drop TABLE billsession;
SET FOREIGN_KEY_CHECKS=1;