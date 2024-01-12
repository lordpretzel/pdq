python query-and-schema-generator.py translate_sql_and_query_and_prov -i ./tpchqs/tpch.sql -o ./tpchqs/sout.xml -P --query_file ./tpchqs/qout.xml -q "prov_lineitem(l_orderkey,x1,x2,x3,x4,l_extendedprice,l_discount,x5,x6,x7,l_shipdate,x8,x9,x10,x11,x12) :- customer(c_custkey,c1,c2,c3,c4,c5,BUILDING,c6),orders(l_orderkey,c_custkey,o1,o2,o_orderdate,o3,o4,o_shippriority,o5),lineitem(l_orderkey,x1,x2,x3,x4,l_extendedprice,l_discount,x5,x6,x7,l_shipdate,x8,x9,x10,x11,x12),o_orderdate < 1995-03-15,l_shipdate > 1995-03-15,rp(l_orderkey,o_orderdate,o_shippriority,v1)." --prov lineitem --replace_dt
java -jar pdq-main-2.0.0-executable.jar planner -s ./tpchqs/sout.xml -q ./tpchqs/qout.xml
