SELECT DISTINCT customer.c_custkey, orders.o_orderkey, lineitem.l_linenumber
FROM lineitem, part, orders, customer
WHERE lineitem.l_partkey = part.p_partkey
AND lineitem.l_shipdate = orders.o_orderdate
AND orders.o_custkey = customer.c_custkey
AND orders.o_orderpriority = '5-LOW'
AND part.p_brand = 'Brand#13'
AND lineitem.l_shipinstruct = 'NONE'
AND lineitem.l_linestatus = 'O'
AND lineitem.l_shipmode = 'FOB'