package com.vitessedata.examples.quark

import org.apache.spark.sql.vitesse.VitesseContext
import org.apache.spark.{Logging, SparkConf, SparkContext}
import org.apache.spark.sql._
import org.apache.spark.sql.types._

import org.apache.log4j.Logger
import org.apache.log4j.Level

class Tpch(val sc: SparkContext, val vc: VitesseContext) extends Logging {
  def prepareParquetTables(): Unit = {
    val nation = vc.vitesseRead.parquet("file:///nx/tpch1/parquet/nation")
    nation.registerTempTable("nation")

    val region = vc.vitesseRead.parquet("file:///nx/tpch1/parquet/region")
    region.registerTempTable("region")

    val part = vc.vitesseRead.parquet("file:///nx/tpch1/parquet/part")
    part.registerTempTable("part")

    val supplier = vc.vitesseRead.parquet("file:///nx/tpch1/parquet/supplier")
    supplier.registerTempTable("supplier")

    val partsupp = vc.vitesseRead.parquet("file:///nx/tpch1/parquet/partsupp")
    partsupp.registerTempTable("partsupp")

    val customer = vc.vitesseRead.parquet("file:///nx/tpch1/parquet/customer")
    customer.registerTempTable("customer")

    val orders = vc.vitesseRead.parquet("file:///nx/tpch1/parquet/orders")
    orders.registerTempTable("orders")

    val lineitem = vc.vitesseRead.parquet("file:///nx/tpch1/parquet/lineitem")
    lineitem.registerTempTable("lineitem")
  }

  // where clause changed, not sure how spark deals with date.
  def run_q1(): Unit = {
    val qstr = """ select l_returnflag, l_linestatus, sum(l_quantity) as sum_qty,
      sum(l_extendedprice) as sum_base_price,
      sum(l_extendedprice * (1 - l_discount)) as sum_disc_price,
      sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) as sum_charge,
      avg(l_quantity) as avg_qty,
      sum(l_extendedprice) / count(l_extendedprice) as avg_price,
      avg(l_discount) as avg_disc,
      max(l_shipdate) as shipd,
      count(*) as count_order
      from  lineitem
      where l_shipdate <= cast('1998-08-01' as date)
      -- spark does not have time interval type '1998-12-01' - interval '112 day'
      -- it does have a function date_add, but there is a BUG.
      group by l_returnflag, l_linestatus
      order by l_returnflag, l_linestatus """
    run_query(qstr)
  }

  def run_q2(): Unit = {
    // Q2: Cannot run.  OOM.
    val qstr = """
      SELECT supplier.s_acctbal,
    supplier.s_name,
    nation.n_name,
    part.p_partkey,
    part.p_mfgr,
    supplier.s_address,
    supplier.s_phone,
    supplier.s_comment
    FROM part, supplier, partsupp, nation, region,
    ( SELECT partsupp_1.ps_partkey AS f_partkey,
    min(partsupp_1.ps_supplycost) AS f_mincost
    FROM partsupp partsupp_1, supplier supplier_1, nation nation_1, region region_1
      WHERE supplier_1.s_suppkey = partsupp_1.ps_suppkey AND supplier_1.s_nationkey = nation_1.n_nationkey
            AND nation_1.n_regionkey = region_1.r_regionkey AND region_1.r_name = 'MIDDLE EAST'
      GROUP BY partsupp_1.ps_partkey) foo
    WHERE part.p_partkey = partsupp.ps_partkey AND supplier.s_suppkey = partsupp.ps_suppkey AND part.p_size = 9
            AND part.p_type like '%TIN' AND supplier.s_nationkey = nation.n_nationkey
            AND nation.n_regionkey = region.r_regionkey AND region.r_name = 'MIDDLE EAST'
            AND partsupp.ps_supplycost = foo.f_mincost AND part.p_partkey = foo.f_partkey
    ORDER BY supplier.s_acctbal DESC, nation.n_name, supplier.s_name, part.p_partkey
    """
    run_query(qstr)
  }

  def run_q3(): Unit = {
    val qstr = """
    SELECT lineitem.l_orderkey,
    sum(lineitem.l_extendedprice * (1.0 - lineitem.l_discount)) AS revenue,
    orders.o_orderdate,
    orders.o_shippriority
    FROM customer, orders, lineitem
    WHERE customer.c_mktsegment = 'HOUSEHOLD' AND customer.c_custkey = orders.o_custkey
          AND lineitem.l_orderkey = orders.o_orderkey
          AND o_orderdate < cast('1995-03-29' as date)
          AND l_shipdate > cast('1995-03-29' as date)
      GROUP BY lineitem.l_orderkey, orders.o_orderdate, orders.o_shippriority
    ORDER BY revenue DESC, orders.o_orderdate
    """
    run_query(qstr)
  }

  def run_q4(): Unit = {
    /*
    val qstr = """
    SELECT orders.o_orderpriority,
    count(*) AS order_count
    FROM orders
    WHERE
    o_orderdate >= '1997-07-01'
    AND o_orderdate < '1997-10-01'
    -- AND o_orderdate < date '1997-07-01' + interval '3 month'
    AND (EXISTS ( SELECT lineitem.l_orderkey,
      lineitem.l_partkey,
      lineitem.l_suppkey,
      lineitem.l_linenumber,
      lineitem.l_quantity,
      lineitem.l_extendedprice,
      lineitem.l_discount,
      lineitem.l_tax,
      lineitem.l_returnflag,
      lineitem.l_linestatus,
      lineitem.l_shipdate,
      lineitem.l_commitdate,
      lineitem.l_receiptdate,
      lineitem.l_shipinstruct,
      lineitem.l_shipmode,
      lineitem.l_comment
        FROM lineitem
        WHERE lineitem.l_orderkey = orders.o_orderkey AND lineitem.l_commitdate < lineitem.l_receiptdate))
    GROUP BY orders.o_orderpriority
    ORDER BY orders.o_orderpriority
    """
    */
    // Spark does not support (cannot parse) AND( EXISTS( SELECT ..., rewrite as outer join.  Note the group by in
    // JOIN foo, this is a must otherwise we may produce dup results.
    val qstr = """
    SELECT orders.o_orderpriority,
    count(*) AS order_count
    FROM orders JOIN (
      select lineitem.l_orderkey from lineitem where lineitem.l_commitdate < lineitem.l_receiptdate
      group by lineitem.l_orderkey
    ) foo
    ON orders.o_orderkey = foo.l_orderkey
    WHERE
    o_orderdate >= cast('1997-07-01' as date)
    AND o_orderdate < cast('1997-10-01' as date)
    GROUP BY orders.o_orderpriority
    ORDER BY orders.o_orderpriority
    """

    run_query(qstr)
  }

  def run_q5(): Unit = {
    val qstr = """
    SELECT nation.n_name,
    sum(lineitem.l_extendedprice * (1.0 - lineitem.l_discount)) AS revenue
    FROM customer, orders, lineitem, supplier, nation, region
    WHERE customer.c_custkey = orders.o_custkey AND lineitem.l_orderkey = orders.o_orderkey
          AND lineitem.l_suppkey = supplier.s_suppkey AND customer.c_nationkey = supplier.s_nationkey
          AND supplier.s_nationkey = nation.n_nationkey AND nation.n_regionkey = region.r_regionkey
          AND region.r_name = 'AMERICA'
          AND orders.o_orderdate >= cast('1994-01-01' as date)
          -- AND orders.o_orderdate < date '1994-01-01' + interval '1 year'
          AND orders.o_orderdate < cast('1995-01-01' as date)
    GROUP BY nation.n_name
    ORDER BY revenue DESC
    """
    run_query(qstr)
  }

  def run_q6(): Unit = {
    //
    // XXX XXX BUG BUG.
    // BUG: Query result seems wrong.   How come?   This one is so simple.
    //
    val qstr =
      """ select sum(l_extendedprice * l_discount) as revenue
          from lineitem
          where l_shipdate >= cast('1994-01-01' as date)
          -- and l_shipdate < '1994-01-01' + interval '1 year'
          and l_shipdate < cast('1995-01-01' as date)
          and l_discount >= 0.02 and l_discount <= 0.04
          -- and l_discount between 0.03 - 0.01 and 0.03 + 0.01
          and l_quantity < 24
      """
    run_query(qstr)
  }

  def run_q7(): Unit = {
    // Spark BUG: No such element exception, year not found.  Cannot convert shipdate to year.
    // We commented out the l_year part, result is 2 rows instead of 4.
    val qstr =
      """ select supp_nation, cust_nation, -- l_year,
                 sum(volume) as revenue
          from ( select n1.n_name as supp_nation, n2.n_name as cust_nation,
                        -- extract(year from l_shipdate) as l_year,
                        -- BUG: year(l_shipdate) as l_year,
                        l_extendedprice * (1 - l_discount) as volume
                 from supplier, lineitem, orders, customer, nation n1, nation n2
                 where s_suppkey = l_suppkey
                 and o_orderkey = l_orderkey
                 and c_custkey = o_custkey
                 and s_nationkey = n1.n_nationkey
                 and c_nationkey = n2.n_nationkey
                 and ( (n1.n_name = 'FRANCE' and n2.n_name = 'ARGENTINA')
                       or (n1.n_name = 'ARGENTINA' and n2.n_name = 'FRANCE') )
                 and l_shipdate between cast('1995-01-01' as date) and cast('1996-12-31' as date)
          ) as shipping
          group by supp_nation, cust_nation -- , l_year
          order by supp_nation, cust_nation -- , l_year
      """
    run_query(qstr)
  }

  def run_q8(): Unit = {
    val qstr =
      """ select o_year, sum(case when nation = 'ARGENTINA' then volume else 0 end) / sum(volume) as mkt_share
          -- BUG: year, same as q7
          -- from ( select extract(year from o_orderdate) as o_year,
          from ( select case when o_orderdate < cast('1996-01-01' as date) then 1995 else 1996 end as o_year,
                        l_extendedprice * (1 - l_discount)  as volume,
                        n2.n_name as nation
                 from part, supplier, lineitem, orders, customer, nation n1, nation n2, region
                 where p_partkey = l_partkey
                 and s_suppkey = l_suppkey
                 and l_orderkey = o_orderkey
                 and o_custkey = c_custkey
                 and c_nationkey = n1.n_nationkey
                 and n1.n_regionkey = r_regionkey
                 and r_name = 'AMERICA'
                 and s_nationkey = n2.n_nationkey
                 and o_orderdate between cast('1995-01-01' as date) and cast('1996-12-31' as date)
                 and p_type = 'ECONOMY BURNISHED TIN'
          ) as all_nations
          group by o_year
          order by o_year
      """
    run_query(qstr)
  }

  def run_q9(): Unit = {
    // BUG: same year bug as Q7.
    // Spark: OOM.
    val qstr =
    """ select nation, -- o_year,
               sum(amount) as sum_profit
        from ( select n_name as nation,
                      -- extract(year from o_orderdate) as o_year,
                      l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity as amount
               from part, supplier, lineitem, partsupp, orders, nation
               where s_suppkey = l_suppkey
               and ps_suppkey = l_suppkey
               and ps_partkey = l_partkey
               and p_partkey = l_partkey
               and o_orderkey = l_orderkey
               and s_nationkey = n_nationkey
               and p_name like '%pink%'
        ) as profit
        group by nation -- , o_year
        order by nation -- , o_year
    """
    run_query(qstr)
  }

  def run_q10(): Unit = {
    val qstr =
    """ select c_custkey, c_name, sum(l_extendedprice * (1 - l_discount)) as revenue,
               c_acctbal, n_name, c_address, c_phone, c_comment
        from customer, orders, lineitem, nation
        where c_custkey = o_custkey
        and l_orderkey = o_orderkey
        and o_orderdate >= cast('1993-03-01' as date)
        -- and o_orderdate < '1993-03-01' + interval '3 month'
        and o_orderdate < cast('1993-06-01' as date)
        and l_returnflag = 'R'
        and c_nationkey = n_nationkey
        group by c_custkey, c_name, c_acctbal, c_phone, n_name, c_address, c_comment
        order by revenue desc
    """
    run_query(qstr)
  }

  def run_q11(): Unit = {
    /*
      > ( select ...) is not supported.    Rewrite as a join.
    val qstr =
      """ select ps_partkey, sum(ps_supplycost * ps_availqtr) as value
          from partsupp, supplier, nation
          where ps_suppkey = s_suppkey
          and s_nationkey = n_nationkey
          and n_name = 'JAPAN'
          group by ps_partkey having sum(ps_supplycost * ps_availqty) > (
                                          select sum(ps_supplycost * ps_availqty) * 0.0001
                                          from partsupp, supplier, nation
                                          where ps_suppkey = s_suppkey
                                          and s_nationkey = n_nationkey
                                          and n_name = 'JAPAN')
          order by value desc
      """
      */
     val qstr =
      """ select foo.ps_partkey, foo.value from
          ( select ps_partkey, sum(ps_supplycost * ps_availqty) as value
            from partsupp, supplier, nation
            where ps_suppkey = s_suppkey
            and s_nationkey = n_nationkey
            and n_name = 'JAPAN'
            group by ps_partkey ) foo,
          ( select sum(ps_supplycost * ps_availqty) * 0.0001 as value
            from partsupp, supplier, nation
            where ps_suppkey = s_suppkey
            and s_nationkey = n_nationkey
            and n_name = 'JAPAN') bar
          where foo.value > bar.value
          order by foo.value desc
      """

    run_query(qstr)
  }

  def run_q12(): Unit = {
    val qstr =
      """ select l_shipmode,
                 sum(case when o_orderpriority = '1-URGENT' or o_orderpriority = '2-HIGH' then 1
                          else 0 end) as high_line_count,
                 sum(case when o_orderpriority <> '1-URGENT' and o_orderpriority <> '2-HIGH' then 1
                          else 0 end) as low_line_count
         from orders, lineitem
         where o_orderkey = l_orderkey
         and (l_shipmode = 'FOB' or l_shipmode = 'TRUCK')
         and l_commitdate < l_receiptdate
         and l_shipdate < l_commitdate
         and l_receiptdate >= cast('1996-01-01' as date)
         -- and l_receiptdate < date '1996-01-01' + interval '1 year'
         and l_receiptdate < cast('1997-01-01' as date)
         group by l_shipmode
         order by l_shipmode
      """
    run_query(qstr)
  }

  def run_q13(): Unit = {
    val qstr =
      """ select c_count, count(*) as custdist
          from (select c_custkey, count(o_orderkey) as c_count
                from customer left outer join orders on
                     c_custkey = o_custkey
                     and o_comment not like '%pending%accounts%'
                group by c_custkey
          ) c_orders
          group by c_count
          order by custdist desc, c_count desc
      """
    run_query(qstr)
  }

  def run_q14(): Unit = {
    val qstr =
      """ select 100.00 * sum(case when p_type like 'PROMO%' then l_extendedprice * (1 - l_discount)
                                   else 0 end)
                        / sum(l_extendedprice * (1 - l_discount)) as promo_revenue
          from lineitem, part
          where l_partkey = p_partkey
          and l_shipdate >= cast('1996-04-01' as date)
          -- and l_shipdate < date '1996-04-01' + interval '1 month'
          and l_shipdate < cast('1996-05-01' as date)
      """
    run_query(qstr)
  }

  def run_q15(): Unit = {
     val qstr =
      """ select s_suppkey, s_name, s_address, s_phone, total_revenue
          from supplier,
                   ( select l_suppkey as supplier_no, sum(l_extendedprice * (1 - l_discount)) as total_revenue
                   from lineitem
                   where l_shipdate >= cast('1995-12-01' as date)
                         and l_shipdate < cast('1996-03-01' as date)
                   group by l_suppkey ) revenue0,

              (
             select max(total_revenue) as revenue_max from (
                   select l_suppkey as supplier_no, sum(l_extendedprice * (1 - l_discount)) as total_revenue
                   from lineitem
                   where l_shipdate >= cast('1995-12-01' as date)
                         and l_shipdate < cast('1996-03-01' as date)
                   group by l_suppkey ) tmpt ) foo
          where s_suppkey = supplier_no and total_revenue = foo.revenue_max
          order by s_suppkey
      """
    run_query(qstr)
  }

  def run_q16(): Unit = {
      val qstr = """ select p_brand, p_type, p_size, count(distinct ps_suppkey) as supplier_cnt
          from ( select p_brand, p_type, p_size, ps_suppkey from
                partsupp
                join part on p_partkey = ps_partkey
                left outer join (select s_suppkey as foo_suppkey
                                 from supplier
                                 where s_comment like '%Customer%Complaints%'
                                 group by s_suppkey) foo
                                 on ps_suppkey = foo_suppkey
                where
                foo_suppkey is null and
                p_brand <> 'Brand#35' and
                p_type not like 'ECONOMY BURNISHED%' and
                p_size in (14, 7, 21, 24, 35, 33, 2, 20)) tmpt
          group by p_brand, p_type, p_size
          order by supplier_cnt desc, p_brand, p_type, p_size
      """

    run_query(qstr)
  }


  def run_q17(): Unit = {
    // Spark BUG: OOM
    val qstr =
      """ select sum(l_extendedprice) / 7.0 as avg_yearly
          from lineitem, part, (select l_partkey as f_partkey, 0.2 * avg(l_quantity) as f_qnt
                                from lineitem group by l_partkey) foo
          where p_partkey = l_partkey
          and p_brand = 'Brand#54'
          and p_container = 'LG BAG'
          and l_partkey = f_partkey
          and l_quantity < f_qnt
      """
    run_query(qstr)
  }

  def run_q18(): Unit = {
    // Rewrite to pull subquery into a join.
    val qstr =
      """ select c_name, c_custkey, o_orderkey, o_orderdate, o_totalprice, sum(l_quantity) as sum_q
          from orders, customer, lineitem, (select l_orderkey as foo_orderkey
                             from lineitem
                             group by l_orderkey
                             having sum(l_quantity) > 314) foo
          where o_orderkey = foo_orderkey
          and c_custkey = o_custkey
          and o_orderkey = l_orderkey
          group by c_name, c_custkey, o_orderkey, o_orderdate, o_totalprice
          order by o_totalprice desc, o_orderdate
      """
    run_query(qstr)
  }

  def run_q19(): Unit = {
    val qstr =
      """ select sum(l_extendedprice * (1 - l_discount)) as revenue
          from lineitem, part
          where
          ( p_partkey = l_partkey
            and p_brand = 'Brand#23'
            and p_container in ('SM CASE', 'SM BOX', 'SM PACK', 'SM PKG')
            and l_quantity >= 5 and l_quantity <= 5 + 10
            and p_size between 1 and 5
            and l_shipmode in ('AIR', 'AIR REG')
            and l_shipinstruct = 'DELIVER IN PERSON' )
          or
          ( p_partkey = l_partkey
            and p_brand = 'Brand#15'
            and p_container in ('MED BAG', 'MED BOX', 'MED PKG', 'MED PACK')
            and l_quantity >= 14 and l_quantity <= 14 + 10
            and p_size between 1 and 10
            and l_shipmode in ('AIR', 'AIR REG')
            and l_shipinstruct = 'DELIVER IN PERSON' )
          or
          ( p_partkey = l_partkey
            and p_brand = 'Brand#44'
            and p_container in ('LG CASE', 'LG BOX', 'LG PACK', 'LG PKG')
            and l_quantity >= 28 and l_quantity <= 28 + 10
            and p_size between 1 and 15
            and l_shipmode in ('AIR', 'AIR REG')
            and l_shipinstruct = 'DELIVER IN PERSON' )
      """
    run_query(qstr)
  }

  def run_q20(): Unit = {
      val qstr = """ select s_name, s_address from supplier, nation,
               (select ps_suppkey from partsupp,
                    (select l_partkey, l_suppkey, 0.5 * sum(l_quantity) s
                         from lineitem
                         where l_shipdate >= cast('1993-01-01' as date) and
                               l_shipdate < cast('1994-01-01' as date)
                         group by l_partkey, l_suppkey) ltab,
                    (select p_partkey from part where p_name like 'lime%'
                         group by p_partkey) ptab
                    where ps_partkey = ptab.p_partkey and ltab.l_suppkey = ps_suppkey
                         and ps_availqty > ltab.s
                ) pstab
          where s_nationkey = n_nationkey and n_name = 'VIETNAM' and s_suppkey = pstab.ps_suppkey
      """
      run_query(qstr)
  }


  def run_q21(): Unit = {
    val qstr =   """ select t2tab.s_name, count(t2tab.s_name) as numwait
          from (
               select t1tab.s_name, t1tab.l_orderkey from (
                    select s_name, l_orderkey, l_suppkey
                    from supplier, lineitem l1, orders, nation
                    where s_suppkey = l1.l_suppkey
                    and o_orderkey = l1.l_orderkey
                    and o_orderstatus = 'F'
                    and l1.l_receiptdate > l1.l_commitdate
                    and s_nationkey = n_nationkey
                    and n_name = 'BRAZIL'
               ) t1tab, lineitem l2
               where t1tab.l_orderkey = l2.l_orderkey
                     and l2.l_suppkey <> t1tab.l_suppkey
               group by t1tab.s_name, t1tab.l_orderkey
          ) t2tab left outer join (
               select t1tab.s_name, t1tab.l_orderkey from (
                    select s_name, l_orderkey, l_suppkey
                    from supplier, lineitem l1, orders, nation
                    where s_suppkey = l1.l_suppkey
                    and o_orderkey = l1.l_orderkey
                    and o_orderstatus = 'F'
                    and l1.l_receiptdate > l1.l_commitdate
                    and s_nationkey = n_nationkey
                    and n_name = 'BRAZIL'
               ) t1tab, lineitem l3
               where t1tab.l_orderkey = l3.l_orderkey
                     and l3.l_suppkey <> t1tab.l_suppkey
                     and l3.l_receiptdate > l3.l_commitdate
               group by t1tab.s_name, t1tab.l_orderkey
          ) t3tab
          on t2tab.s_name = t3tab.s_name and t2tab.l_orderkey = t3tab.l_orderkey
          where t3tab.s_name is null
          group by t2tab.s_name
          order by numwait desc, t2tab.s_name
          """
    run_query(qstr)
  }

  def run_q22(): Unit = {
    // subquery rewrite to join and left outer join (not exists).
    //
    // BUG: below.
    val qstr =
      """ select cntrycode, count(*) as numcust, sum(c_acctbal) as totacctbal
          from ( select substring(c.c_phone, 1, 2) as cntrycode,
                        c.c_acctbal
                 from customer c join
                      (select sum(cfoo.c_acctbal) / count(cfoo.c_acctbal) as strange_avg from customer cfoo
                         where cfoo.c_acctbal > 0
                         and substring(cfoo.c_phone, 1, 2) in ('10', '11', '26', '22', '19', '20', '27')) foo
                      on c.c_acctbal > foo.strange_avg
                      left outer join orders o
                      on c.c_custkey = o.o_custkey
                 where o.o_custkey is null
                 --- BUG: what is wrong with the following?
                 --- and substring(c.c_phone, 1, 2) in ('10', '11', '26', '22', '19', '20', '27')
                 ) custsale
          --- BUG: move predicate outside?   does not work either.
          --- where cntrycode in ('10', '11', '26', '22', '19', '20', '27')
          group by cntrycode
          order by cntrycode
      """
    run_query(qstr)
  }

  def run_warmup() {
    run_query("select count(l_orderkey) from lineitem group by l_linenumber")
  }

  def run_tpch(): Unit = {
    run_warmup()
    run_q1()
    // run_q2(),
    run_q3()
    run_q4()
    run_q5()
    run_q6()
    // run_q7()
    run_q8()
    // run_q9()
    run_q10()
    run_q11()
    run_q12()
    run_q13()
    run_q14()
    run_q15()
    run_q16()
    run_q17()
    run_q18()
    run_q19()
    run_q20()
    run_q21()
    run_q22()
  }

  def run_query(str: String): Unit = {
    logInfo(s"Vitesse: Running query $str, vc is " + vc)
    val q = vc.sql(str)
    q.explain(true)
    //
    // Prefix query start/stop and result with QQQQ.   Grep it.
    // We grep QQQQ TTTT to time query runs.   This does not includes
    // cluster start time, etc.
    //
    logInfo("QQQQ TTTT: Query Start ...")
    q.collect().foreach(x => println(s"QQQQ RRRR: $x"))
    logInfo("QQQQ TTTT: Query Once Done.")

    //
    // Run it again, to see if warm run is any better.   Seems it does
    // not matter.
    //
    // q.collect().foreach(x => println(s"Vitesse QResult: $x"))
    // logWarning("Vitesse: Query Twice ...")
    //
  }
}

object Tpch extends Logging {
  def main(args: Array[String]): Unit = {

    // Shut up very verbose crap.
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("org.apache.spark.sql.vitesse").setLevel(Level.INFO)
    Logger.getLogger("parquet").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val sparkConf = new SparkConf().setAppName("Vitesse-Tpch")
    val flavor = args(0)
    logWarning(s"Vitesse: ============== VITESSE USING $flavor CONTEXT ====================== ")

    if (flavor.equals("sql")) {
      // Use spark SQL context.
      logInfo("Sparkconf set sql dialect")
      sparkConf.set("spark.sql.dialect", "sql")
    }
    // else, use Vitesse Context.

    val sc = new SparkContext(sparkConf)
    val vc = new VitesseContext(sc)
    val tpch = new Tpch(sc, vc)

    tpch.prepareParquetTables()

    args(1) match {
      case "q1" => tpch.run_q1()
      case "q2" => tpch.run_q2()
      case "q3" => tpch.run_q3()
      case "q4" => tpch.run_q4()
      case "q5" => tpch.run_q5()
      case "q6" => tpch.run_q6()
      case "q7" => tpch.run_q7()
      case "q8" => tpch.run_q8()
      case "q9" => tpch.run_q9()
      case "q10" => tpch.run_q10()
      case "q11" => tpch.run_q11()
      case "q12" => tpch.run_q12()
      case "q13" => tpch.run_q13()
      case "q14" => tpch.run_q14()
      case "q15" => tpch.run_q15()
      case "q16" => tpch.run_q16()
      case "q17" => tpch.run_q17()
      case "q18" => tpch.run_q18()
      case "q19" => tpch.run_q19()
      case "q20" => tpch.run_q20()
      case "q21" => tpch.run_q21()
      case "q22" => tpch.run_q22()
      case "warmup" => tpch.run_warmup()
      case "all" => tpch.run_tpch()
      case s => tpch.run_query(s)
    }
  }
}

class TpchLoad(val sc: SparkContext, val vc: VitesseContext) {
  def loadNation(): Unit = {
    val schema = StructType(
      List(StructField("n_nationkey", IntegerType, false),
        StructField("n_name", StringType, false),
        StructField("n_regionkey", IntegerType, false),
        StructField("n_comment", StringType))
    )

    val tbl = sc.textFile("file:///nx/tpch1/load/data/nation.tbl")
      .map(_.split("[|]"))
      .map(p => Row(p(0).toInt, p(1), p(2).toInt, p(3)))

    val df = vc.createDataFrame(tbl, schema)
    df.write.parquet("file:///nx/tpch1/parquet/nation")
  }

  def loadRegion(): Unit = {
    val schema = StructType(
      List(StructField("r_regionkey", IntegerType, false),
        StructField("r_name", StringType, false),
        StructField("r_comment", StringType))
    )

    val tbl = sc.textFile("file:///nx/tpch1/load/data/region.tbl")
      .map(_.split("[|]"))
      .map(p => Row(p(0).toInt, p(1), p(2)))

    val df = vc.createDataFrame(tbl, schema)
    df.write.parquet("file:///nx/tpch1/parquet/region")
  }

  def loadPart(): Unit = {
    val schema = StructType(
      List(StructField("p_partkey", IntegerType, false),
        StructField("p_name", StringType, false),
        StructField("p_mfgr", StringType, false),
        StructField("p_brand", StringType, false),
        StructField("p_type", StringType, false),
        StructField("p_size", IntegerType, false),
        StructField("p_container", StringType, false),
        StructField("p_retailprice", FloatType, false),
        StructField("p_comment", StringType))
    )

    val tbl = sc.textFile("file:///nx/tpch1/load/data/part.tbl")
      .map(_.split("[|]"))
      .map(p => Row(p(0).toInt, p(1), p(2), p(3), p(4), p(5).toInt, p(6), p(7).toFloat, p(8)))

    val df = vc.createDataFrame(tbl, schema)
    df.write.parquet("file:///nx/tpch1/parquet/part")
  }

  def loadSupplier(): Unit = {
    val schema = StructType(
      List(StructField("s_suppkey", IntegerType, false),
        StructField("s_name", StringType, false),
        StructField("s_address", StringType, false),
        StructField("s_nationkey", IntegerType, false),
        StructField("s_phone", StringType, false),
        StructField("s_acctbal", FloatType, false),
        StructField("s_comment", StringType))
    )

    val tbl = sc.textFile("file:///nx/tpch1/load/data/supplier.tbl")
      .map(_.split("[|]"))
      .map(p => Row(p(0).toInt, p(1), p(2), p(3).toInt, p(4), p(5).toFloat, p(6)))

    val df = vc.createDataFrame(tbl, schema)
    df.write.parquet("file:///nx/tpch1/parquet/supplier")
  }

  def loadPartSupp(): Unit = {
    val schema = StructType(
      List(StructField("ps_partkey", IntegerType, false),
        StructField("ps_suppkey", IntegerType, false),
        StructField("ps_availqty", IntegerType, false),
        StructField("ps_supplycost", FloatType, false),
        StructField("ps_comment", StringType))
    )

    val tbl = sc.textFile("file:///nx/tpch1/load/data/partsupp.tbl")
      .map(_.split("[|]"))
      .map(p => Row(p(0).toInt, p(1).toInt, p(2).toInt, p(3).toFloat, p(4)))

    val df = vc.createDataFrame(tbl, schema)
    df.write.parquet("file:///nx/tpch1/parquet/partsupp")
  }

  def loadCustomer(): Unit = {
    val schema = StructType(
      List(StructField("c_custkey", IntegerType, false),
        StructField("c_name", StringType, false),
        StructField("c_address", StringType, false),
        StructField("c_nationkey", IntegerType, false),
        StructField("c_phone", StringType, false),
        StructField("c_acctbal", FloatType, false),
        StructField("c_mktsegment", StringType, false),
        StructField("c_comment", StringType))
    )

    val tbl = sc.textFile("file:///nx/tpch1/load/data/customer.tbl")
      .map(_.split("[|]"))
      .map(p => Row(p(0).toInt, p(1), p(2), p(3).toInt, p(4), p(5).toFloat, p(6), p(7)))

    val df = vc.createDataFrame(tbl, schema)
    df.write.parquet("file:///nx/tpch1/parquet/customer")
  }

  // orderdate: Parquet does not support DateType yet.  Use string.
  def loadOrders(): Unit = {
    val schema = StructType(
      List(StructField("o_orderkey", IntegerType, false),
        StructField("o_custkey", IntegerType, false),
        StructField("o_orderstatus", StringType, false),
        StructField("o_totalprice", FloatType, false),
        StructField("o_orderdate", DateType, false),
        StructField("o_orderpriority", StringType, false),
        StructField("o_clerk", StringType, false),
        StructField("o_shippriority", IntegerType, false),
        StructField("o_comment", StringType))
    )

    val tbl = sc.textFile("file:///nx/tpch1/load/data/orders.tbl")
      .map(_.split("[|]"))
      .map(p => Row(p(0).toInt, p(1).toInt, p(2), p(3).toFloat, java.sql.Date.valueOf(p(4)),
                    p(5), p(6), p(7).toInt, p(8)))

    val df = vc.createDataFrame(tbl, schema)
    df.write.parquet("file:///nx/tpch1/parquet/orders")
  }

  // date: Parquet does not support DateType yet.  Use string.
  def loadLineitem(): Unit = {
    val schema = StructType(
      List(StructField("l_orderkey", IntegerType, false),
        StructField("l_partkey", IntegerType, false),
        StructField("l_suppkey", IntegerType, false),
        StructField("l_linenumber", IntegerType, false),
        StructField("l_quantity", IntegerType, false),
        StructField("l_extendedprice", FloatType, false),
        StructField("l_discount", FloatType, false),
        StructField("l_tax", FloatType, false),
        StructField("l_returnflag", StringType, false),
        StructField("l_linestatus", StringType, false),
        StructField("l_shipdate", DateType, false),
        StructField("l_commitdate", DateType, false),
        StructField("l_receiptdate", DateType, false),
        StructField("l_shipinstruct", StringType, false),
        StructField("l_shipmode", StringType, false),
        StructField("l_comment", StringType))
    )

    val tbl = sc.textFile("file:///nx/tpch1/load/data/lineitem.tbl")
      .map(_.split("[|]"))
      .map(p => Row(p(0).toInt, p(1).toInt, p(2).toInt, p(3).toInt, p(4).toInt,
                    p(5).toFloat, p(6).toFloat, p(7).toFloat, p(8), p(9),
                    java.sql.Date.valueOf(p(10)), java.sql.Date.valueOf(p(11)), java.sql.Date.valueOf(p(12)),
                    p(13), p(14), p(15)))

    val df = vc.createDataFrame(tbl, schema)
    df.write.parquet("file:///nx/tpch1/parquet/lineitem")
  }

  def loadTextTables(): Unit = {
    loadNation()
    loadRegion()
    loadPart()
    loadSupplier()
    loadPartSupp()
    loadCustomer()
    loadOrders()
    loadLineitem()
  }
}

object TpchLoad {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("Vitesse-Tpch")
    sparkConf.set("spark.sql.parquet.compression.codec", "uncompressed")
    val sc = new SparkContext(sparkConf)

    val vitesseContext = new VitesseContext(sc)
    val tpch = new TpchLoad(sc, vitesseContext)
    tpch.loadTextTables()
  }
}
