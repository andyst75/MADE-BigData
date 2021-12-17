package made_hw5

import breeze.linalg.{DenseVector => BDV}
import breeze.stats.distributions.Rand
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col


object SparkLinearRegression extends App {

  final val ROWS = 100000
  final val COLS = 3
  final val TOL: Double = 1e-14
  final val EPOCH: Long = 100

  val spark: SparkSession = SparkSession.builder()
    .master("local")
    .appName("HW5")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  val df = spark.createDataFrame(Seq.fill(ROWS)(
    Vectors.dense(BDV.rand[Double](COLS, Rand.gaussian).toArray),
    Vectors.dense(Array(1.5, 0.3, -0.7))))
    .select(col("_1").as("features"), col("_2").as("hidden"))

  val labelUdf = df.sqlContext.udf.register("label",
    (feat: Vector, hid: Vector) => feat.dot(hid)
  )

  val dfWithLabel = df
    .withColumn("label", labelUdf(col("features"), col("hidden")))
    .select(col("features"), col("label"))

  println("Start train LinearRegression")
  val clf = new LinearRegression(spark, 1e-14, 1.0, COLS)
  clf.fit(dfWithLabel, 100, verbose = true)

  println
  println("Start train LinearRegression with batches")
  val clfBatches = new LinearRegressionBatch(spark, 1e-14, COLS)
  clfBatches.fit(dfWithLabel, 100, batchSize=1000, verbose = true)

}