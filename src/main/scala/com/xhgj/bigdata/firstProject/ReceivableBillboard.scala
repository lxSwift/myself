package com.xhgj.bigdata.firstProject

import com.xhgj.bigdata.util.{Config, MysqlConnect, TableName}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import java.sql.{DriverManager, Statement}
import java.util.Properties

/**
 * @Author luoxin
 * @Date 2023/6/30 9:23
 * @PackageName:com.xhgj.bigdata.firstProject
 * @ClassName: ReceivableBillboard
 * @Description: 应收看板
 * @Version 1.0
 */
object ReceivableBillboard {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Spark task job ReceivableBillboard.scala")
      .enableHiveSupport()
      .getOrCreate()

    runRES(spark)

    //关闭SparkSession
    spark.stop()
  }
  def runRES(spark: SparkSession): Unit = {
    spark.sql(
      s"""
        |SELECT DS.FNAME AS SALENAME
        |	,MIN(SUBSTRING(OER.FCREATEDATE,1,10)) AS BUSINESSDATE
        |	,DP.FNUMBER	PROJECTNO
        |	,DP.FNAME PROJECTNAME
        |	,CASE WHEN (OES.F_PAEZ_CHECKBOX = 1 OR OERE.F_PXDF_TEXT LIKE 'HZXM%') THEN '非自营'
        |		ELSE '自营' END AS PERFORMANCEFORM
        |	,CASE WHEN DWP.PROJECTSHORTNAME IS NOT NULL THEN DWP.PROJECTSHORTNAME
        |		ELSE '其他' END AS PROJECTSHORTNAME
        |	,SUM(OERE.FPRICEQTY * OERE.FTAXPRICE) AS SALEAMOUNT
        |FROM ${TableName.ODS_ERP_RECEIVABLE} OER
        |LEFT JOIN ${TableName.ODS_ERP_RECEIVABLEENTRY} OERE ON OER.FID = OERE.FID
        |LEFT JOIN ${TableName.DIM_CUSTOMER} DC ON OER.FCUSTOMERID = DC.FCUSTID
        |LEFT JOIN ${TableName.DIM_PROJECTBASIC} DP ON OERE.F_PXDF_TEXT = DP.FNUMBER
        |LEFT JOIN ${TableName.ODS_ERP_SALORDER} OES ON IF(OERE.FORDERNUMBER='',0,OERE.FORDERNUMBER) = OES.FBILLNO
        |LEFT JOIN ${TableName.DWD_WRITE_PROJECTNAME} DWP ON DP.FNAME = DWP.PROJECTNAME
        |LEFT JOIN ${TableName.DIM_SALEMAN} DS ON OERE.F_PAEZ_BASE2 = DS.FID
        |WHERE OER.FDOCUMENTSTATUS = 'C' AND OER.FSETTLEORGID = '2297156' AND DC.FNAME != 'DP咸亨国际科技股份有限公司'
        |	AND SUBSTRING(OER.FCREATEDATE,1,10) <= '2023-05-31'
        |GROUP BY DS.FNAME
        |	,DP.FNUMBER
        |	,DP.FNAME
        |	,CASE WHEN (OES.F_PAEZ_CHECKBOX = 1 OR OERE.F_PXDF_TEXT LIKE 'HZXM%') THEN '非自营'
        |		ELSE '自营' END
        |	,CASE WHEN DWP.PROJECTSHORTNAME IS NOT NULL THEN DWP.PROJECTSHORTNAME
        |		ELSE '其他' END
        |UNION ALL
        |SELECT DS.FNAME AS SALENAME
        |	,MIN(SUBSTRING(OER.FCREATEDATE,1,10)) AS BUSINESSDATE
        |	,DP.FNUMBER	PROJECTNO
        |	,DP.FNAME PROJECTNAME
        |	,CASE WHEN (OES.F_PAEZ_CHECKBOX = 1 OR OERE.F_PXDF_TEXT LIKE 'HZXM%') THEN '非自营'
        |		ELSE '自营' END AS PERFORMANCEFORM
        |	,CASE WHEN DWP.PROJECTSHORTNAME IS NOT NULL THEN DWP.PROJECTSHORTNAME
        |		ELSE '其他' END AS PROJECTSHORTNAME
        |	,SUM(OERE.FPRICEQTY * OERE.FTAXPRICE) AS SALEAMOUNT
        |FROM ${TableName.ODS_ERP_RECEIVABLE} OER
        |LEFT JOIN ${TableName.ODS_ERP_RECEIVABLEENTRY} OERE ON OER.FID = OERE.FID
        |LEFT JOIN ${TableName.DIM_CUSTOMER} DC ON OER.FCUSTOMERID = DC.FCUSTID
        |LEFT JOIN ${TableName.DIM_PROJECTBASIC} DP ON OERE.F_PXDF_TEXT = DP.FNUMBER
        |LEFT JOIN ${TableName.ODS_ERP_SALORDER} OES ON IF(OERE.FORDERNUMBER='',0,OERE.FORDERNUMBER) = OES.FBILLNO
        |LEFT JOIN ${TableName.DWD_WRITE_PROJECTNAME} DWP ON DP.FNAME = DWP.PROJECTNAME
        |LEFT JOIN ${TableName.DIM_SALEMAN} DS ON OERE.F_PAEZ_BASE2 = DS.FID
        |WHERE OER.FDOCUMENTSTATUS = 'C' AND OER.FSETTLEORGID = '910474' AND OER.F_PAEZ_TEXT22 = '咸亨国际电子商务有限公司'
        |	AND DWP.PROJECTSHORTNAME != '中核集团' AND SUBSTRING(OER.FCREATEDATE,1,10) <= '2023-05-31'
        |GROUP BY DS.FNAME
        |	,DP.FNUMBER
        |	,DP.FNAME
        |	,CASE WHEN (OES.F_PAEZ_CHECKBOX = 1 OR OERE.F_PXDF_TEXT LIKE 'HZXM%') THEN '非自营'
        |		ELSE '自营' END
        |	,CASE WHEN DWP.PROJECTSHORTNAME IS NOT NULL THEN DWP.PROJECTSHORTNAME
        |		ELSE '其他' END
        |""".stripMargin).createOrReplaceTempView("A1")

    spark.sql(
      s"""
         |SELECT
         |PROJECTNO,
         |MIN(KPDATE) KPDATE,
         |SUM(RECAMOUNT) AS RECAMOUNT
         |FROM ${TableName.DWD_HISTORY_RECEIVABLE} DHR
         |GROUP BY
         |PROJECTNO
         |""".stripMargin).createOrReplaceTempView("A2")

    spark.sql(
      s"""
         |SELECT DS.FNAME AS SALENAME
         |	,MIN(SUBSTRING(OER.FCREATEDATE,1,10)) AS BUSINESSDATE
         |	,DP.FNUMBER	PROJECTNO
         |	,DP.FNAME PROJECTNAME
         |	,CASE WHEN (OES.F_PAEZ_CHECKBOX = 1 OR OERE.F_PXDF_TEXT LIKE 'HZXM%') THEN '非自营'
         |		ELSE '自营' END AS PERFORMANCEFORM
         |	,CASE WHEN DWP.PROJECTSHORTNAME IS NOT NULL THEN DWP.PROJECTSHORTNAME
         |		ELSE '其他' END AS PROJECTSHORTNAME
         |	,SUM(OERE.FPRICEQTY * OERE.FTAXPRICE) AS SALEAMOUNT
         |FROM ${TableName.ODS_ERP_RECEIVABLE} OER
         |LEFT JOIN ${TableName.ODS_ERP_RECEIVABLEENTRY} OERE ON OER.FID = OERE.FID
         |LEFT JOIN ${TableName.DIM_CUSTOMER} DC ON OER.FCUSTOMERID = DC.FCUSTID
         |LEFT JOIN ${TableName.DIM_PROJECTBASIC} DP ON OERE.F_PXDF_TEXT = DP.FNUMBER
         |LEFT JOIN ${TableName.ODS_ERP_SALORDER} OES ON IF(OERE.FORDERNUMBER='',0,OERE.FORDERNUMBER) = OES.FBILLNO
         |LEFT JOIN ${TableName.DWD_WRITE_PROJECTNAME} DWP ON DP.FNAME = DWP.PROJECTNAME
         |LEFT JOIN ${TableName.DIM_SALEMAN} DS ON OERE.F_PAEZ_BASE2 = DS.FID
         |WHERE OER.FDOCUMENTSTATUS = 'C' AND OER.FSETTLEORGID = '2297156' AND DC.FNAME != 'DP咸亨国际科技股份有限公司'
         |	AND SUBSTRING(OER.FCREATEDATE,1,10) >= '2023-06-01'
         |GROUP BY DS.FNAME
         |	,DP.FNUMBER
         |	,DP.FNAME
         |	,CASE WHEN (OES.F_PAEZ_CHECKBOX = 1 OR OERE.F_PXDF_TEXT LIKE 'HZXM%') THEN '非自营'
         |		ELSE '自营' END
         |	,CASE WHEN DWP.PROJECTSHORTNAME IS NOT NULL THEN DWP.PROJECTSHORTNAME
         |		ELSE '其他' END
         |UNION ALL
         |SELECT DS.FNAME AS SALENAME
         |	,MIN(SUBSTRING(OER.FCREATEDATE,1,10)) AS BUSINESSDATE
         |	,DP.FNUMBER	PROJECTNO
         |	,DP.FNAME PROJECTNAME
         |	,CASE WHEN (OES.F_PAEZ_CHECKBOX = 1 OR OERE.F_PXDF_TEXT LIKE 'HZXM%') THEN '非自营'
         |		ELSE '自营' END AS PERFORMANCEFORM
         |	,CASE WHEN DWP.PROJECTSHORTNAME IS NOT NULL THEN DWP.PROJECTSHORTNAME
         |		ELSE '其他' END AS PROJECTSHORTNAME
         |	,SUM(OERE.FPRICEQTY * OERE.FTAXPRICE) AS SALEAMOUNT
         |FROM ${TableName.ODS_ERP_RECEIVABLE} OER
         |LEFT JOIN ${TableName.ODS_ERP_RECEIVABLEENTRY} OERE ON OER.FID = OERE.FID
         |LEFT JOIN ${TableName.DIM_CUSTOMER} DC ON OER.FCUSTOMERID = DC.FCUSTID
         |LEFT JOIN ${TableName.DIM_PROJECTBASIC} DP ON OERE.F_PXDF_TEXT = DP.FNUMBER
         |LEFT JOIN ${TableName.ODS_ERP_SALORDER} OES ON IF(OERE.FORDERNUMBER='',0,OERE.FORDERNUMBER) = OES.FBILLNO
         |LEFT JOIN ${TableName.DWD_WRITE_PROJECTNAME} DWP ON DP.FNAME = DWP.PROJECTNAME
         |LEFT JOIN ${TableName.DIM_SALEMAN} DS ON OERE.F_PAEZ_BASE2 = DS.FID
         |WHERE OER.FDOCUMENTSTATUS = 'C' AND OER.FSETTLEORGID = '910474' AND OER.F_PAEZ_TEXT22 = '咸亨国际电子商务有限公司'
         |	AND DWP.PROJECTSHORTNAME != '中核集团' AND SUBSTRING(OER.FCREATEDATE,1,10) >= '2023-06-01'
         |GROUP BY DS.FNAME
         |	,DP.FNUMBER
         |	,DP.FNAME
         |	,CASE WHEN (OES.F_PAEZ_CHECKBOX = 1 OR OERE.F_PXDF_TEXT LIKE 'HZXM%') THEN '非自营'
         |		ELSE '自营' END
         |	,CASE WHEN DWP.PROJECTSHORTNAME IS NOT NULL THEN DWP.PROJECTSHORTNAME
         |		ELSE '其他' END
         |""".stripMargin).createOrReplaceTempView("A3")

    spark.sql(
      s"""
         |SELECT DP.FNUMBER,DP.FNAME,DWP.PROJECTSHORTNAME ,SUM(FRECAMOUNTFOR_E) REAMOUNT
         |FROM ${TableName.ODS_ERP_RECEIVEBILL} OER
         |LEFT JOIN ${TableName.ODS_ERP_RECEIVEBILLENTRY} OERE ON OER.FID = OERE.FID
         |LEFT JOIN ${TableName.DIM_CUSTOMER} DC ON OER.FPAYUNIT = DC.FCUSTID
         |LEFT JOIN ${TableName.DIM_PROJECTBASIC} DP ON OERE.FPROJECTNO = DP.FID
         |LEFT JOIN ${TableName.DWD_WRITE_PROJECTNAME} DWP ON DP.FNAME = DWP.PROJECTNAME
         |WHERE OER.FPAYORGID = '2297156' AND OER.FDOCUMENTSTATUS = 'C'
         |	AND DC.FNAME != 'DP咸亨国际科技股份有限公司' AND SUBSTRING(OER.FCREATEDATE,1,10) >= '2023-06-01'
         |GROUP BY DP.FNUMBER,DP.FNAME,DWP.PROJECTSHORTNAME
         |UNION ALL
         |SELECT DP.FNUMBER,DP.FNAME,DWP.PROJECTSHORTNAME,SUM(FRECAMOUNTFOR_E) REAMOUNT
         |FROM ${TableName.ODS_ERP_RECEIVEBILL} OER
         |LEFT JOIN ${TableName.ODS_ERP_RECEIVEBILLENTRY} OERE ON OER.FID = OERE.FID
         |LEFT JOIN ${TableName.DIM_CUSTOMER} DC ON OER.FPAYUNIT = DC.FCUSTID
         |LEFT JOIN ${TableName.DIM_PROJECTBASIC} DP ON OERE.FPROJECTNO = DP.FID
         |LEFT JOIN ${TableName.DWD_WRITE_PROJECTNAME} DWP ON DP.FNAME = DWP.PROJECTNAME
         |WHERE OER.FDOCUMENTSTATUS = 'C' AND OER.FPAYORGID = '910474' AND OER.F_PAEZ_TEXT = '咸亨国际电子商务有限公司'
         |	AND DWP.PROJECTSHORTNAME != '中核集团' AND SUBSTRING(OER.FCREATEDATE,1,10) >= '2023-06-01'
         |GROUP BY DP.FNUMBER,DP.FNAME,DWP.PROJECTSHORTNAME
         |""".stripMargin).createOrReplaceTempView("A4")

    spark.sql(
      s"""
         |SELECT DP.FNUMBER,DP.FNAME,DWP.PROJECTSHORTNAME,SUM(OERE.FREALREFUNDAMOUNTFOR)*-1 AS REAMOUNT
         |FROM ${TableName.ODS_ERP_REFUNDBILL} OER
         |LEFT JOIN ${TableName.ODS_ERP_REFUNDBILLENTRY} OERE ON OER.FID = OERE.FID
         |LEFT JOIN ${TableName.DIM_CUSTOMER} DC ON OER.FRECTUNIT = DC.FCUSTID
         |LEFT JOIN ${TableName.DIM_PROJECTBASIC} DP ON OERE.FPROJECTNO = DP.FID
         |LEFT JOIN ${TableName.DWD_WRITE_PROJECTNAME} DWP ON DP.FNAME = DWP.PROJECTNAME
         |WHERE OER.FPAYORGID = '2297156' AND OER.FDOCUMENTSTATUS = 'C' AND DC.FNAME != 'DP咸亨国际科技股份有限公司'
         |	AND SUBSTRING(OER.FCREATEDATE,1,10) >= '2023-06-01'
         |GROUP BY DP.FNUMBER,DP.FNAME,DWP.PROJECTSHORTNAME
         |UNION ALL
         |SELECT DP.FNUMBER,DP.FNAME,DWP.PROJECTSHORTNAME,SUM(OERE.FREALREFUNDAMOUNTFOR)*-1 AS REAMOUNT
         |FROM ${TableName.ODS_ERP_REFUNDBILL} OER
         |LEFT JOIN ${TableName.ODS_ERP_REFUNDBILLENTRY} OERE ON OER.FID = OERE.FID
         |LEFT JOIN ${TableName.DIM_CUSTOMER} DC ON OER.FRECTUNIT = DC.FCUSTID
         |LEFT JOIN ${TableName.DIM_PROJECTBASIC} DP ON OERE.FPROJECTNO = DP.FID
         |LEFT JOIN ${TableName.DWD_WRITE_PROJECTNAME} DWP ON DP.FNAME = DWP.PROJECTNAME
         |WHERE OER.FDOCUMENTSTATUS = 'C' AND OER.FPAYORGID = '910474' AND OER.F_PAEZ_TEXT = '咸亨国际电子商务有限公司'
         |	AND DWP.PROJECTSHORTNAME != '中核集团' AND SUBSTRING(OER.FCREATEDATE,1,10) >= '2023-06-01'
         |GROUP BY DP.FNUMBER,DP.FNAME,DWP.PROJECTSHORTNAME
         |""".stripMargin).createOrReplaceTempView("A5")
////    初始化
//    val result =spark.sql(
//      s"""
//         |SELECT A1.SALENAME,
//         |	COALESCE(A2.KPDATE,A1.BUSINESSDATE) AS BUSINESSDATE,
//         |	COALESCE(A2.PROJECTNO,A1.PROJECTNO) AS PROJECTNO,
//         |	A1.PROJECTNAME,
//         |	A1.PERFORMANCEFORM,
//         |	A1.PROJECTSHORTNAME,
//         |	A1.SALEAMOUNT,
//         |	NULL AS PAYBACKAMOUNT,
//         |	NULL AS REFAMOUNT,
//         |	A2.RECAMOUNT,
//         |	DATEDIFF(FROM_UNIXTIME(UNIX_TIMESTAMP(),'yyyy-MM-dd'),DATE_FORMAT(A2.KPDATE,'yyyy-MM-dd')) AGING,
//         | '2023-05' UPDATEMONTH
//         |FROM A1
//         |FULL JOIN A2 ON A2.PROJECTNO = A1.PROJECTNO
//         |UNION ALL
//         |SELECT A3.SALENAME,
//         |	A3.BUSINESSDATE,
//         |	A3.PROJECTNO,
//         |	A3.PROJECTNAME,
//         |	A3.PERFORMANCEFORM,
//         |	A3.PROJECTSHORTNAME,
//         |	A3.SALEAMOUNT,
//         |	A4.REAMOUNT AS PAYBACKAMOUNT,
//         |	A5.REAMOUNT AS REFAMOUNT,
//         |	CAST(A3.SALEAMOUNT AS DECIMAL(19,2)) - CAST(A4.REAMOUNT AS DECIMAL(19,2)) - CAST(A5.REAMOUNT AS DECIMAL(19,2)) AS RECAMOUNT,
//         |	DATEDIFF(FROM_UNIXTIME(UNIX_TIMESTAMP(),'yyyy-MM-dd'),DATE_FORMAT(A3.BUSINESSDATE,'yyyy-MM-dd')) AGING,
//         | '2023-05' UPDATEMONTH
//         |FROM A3
//         |LEFT JOIN A4 ON A3.PROJECTNO = A4.FNUMBER
//         |LEFT JOIN A5 ON A3.PROJECTNO = A5.FNUMBER
//         |""".stripMargin)

    val result: DataFrame = spark.sql(
      s"""
         |SELECT
         |	A1.SALENAME,
         |	COALESCE(A2.KPDATE,A1.BUSINESSDATE) AS BUSINESSDATE,
         |	COALESCE(A2.PROJECTNO,A1.PROJECTNO) AS PROJECTNO,
         |	A1.PROJECTNAME,
         |	A1.PERFORMANCEFORM,
         |	A1.PROJECTSHORTNAME,
         |	A1.SALEAMOUNT,
         |	A4.REAMOUNT AS PAYBACKAMOUNT,
         |	A5.REAMOUNT AS REFAMOUNT,
         |	CAST(COALESCE(A2.RECAMOUNT,0) AS DECIMAL(19,2))-CAST(COALESCE(A4.REAMOUNT,0) AS DECIMAL(19,2))-CAST(COALESCE(A5.REAMOUNT,0) AS DECIMAL(19,2)) AS RECAMOUNT,
         |	DATEDIFF(FROM_UNIXTIME(UNIX_TIMESTAMP(),'yyyy-MM-dd'),DATE_FORMAT(A2.KPDATE,'yyyy-MM-dd')) AGING,
         |	SUBSTRING(DATE_ADD(CURRENT_TIMESTAMP() ,-1),1,7) AS UPDATEMONTH
         |FROM A1
         |FULL JOIN A2 ON A2.PROJECTNO = A1.PROJECTNO
         |LEFT JOIN A4 ON A2.PROJECTNO = A4.FNUMBER
         |LEFT JOIN A5 ON A2.PROJECTNO = A5.FNUMBER
         |UNION ALL
         |SELECT
         |	A3.SALENAME,
         |	A3.BUSINESSDATE,
         |	A3.PROJECTNO,
         |	A3.PROJECTNAME,
         |	A3.PERFORMANCEFORM,
         |	A3.PROJECTSHORTNAME,
         |	A3.SALEAMOUNT,
         |	A4.REAMOUNT AS PAYBACKAMOUNT,
         |	A5.REAMOUNT AS REFAMOUNT,
         |	CAST(COALESCE(A3.SALEAMOUNT,0) AS DECIMAL(19,2)) - CAST(COALESCE(A4.REAMOUNT,0) AS DECIMAL(19,2)) - CAST(COALESCE(A5.REAMOUNT,0) AS DECIMAL(19,2)) AS RECAMOUNT,
         |	DATEDIFF(FROM_UNIXTIME(UNIX_TIMESTAMP(),'yyyy-MM-dd'),DATE_FORMAT(A3.BUSINESSDATE,'yyyy-MM-dd')) AGING,
         |	SUBSTRING(DATE_ADD(CURRENT_TIMESTAMP() ,-1),1,7) AS UPDATEMONTH
         |FROM A3
         |LEFT JOIN A4 ON A3.PROJECTNO = A4.FNUMBER
         |LEFT JOIN A5 ON A3.PROJECTNO = A5.FNUMBER
         |""".stripMargin)

    println("result:"+result.count())

    //先删除前一天所属月份的数据
    val deleteQuery = s"DELETE FROM ads_xhgj.ads_fin_receivableboard WHERE UPDATEMONTH = DATE_FORMAT(CURRENT_DATE() - INTERVAL 1 DAY, '%Y-%m')"
    MysqlConnect.executeUpdateTable(deleteQuery)
    //导出至mysql库
    val table = "ads_fin_receivableboard"
    MysqlConnect.appendTable(table,result)

  }
}
