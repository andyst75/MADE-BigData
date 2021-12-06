package hw4

import org.apache.spark.sql._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

object TfIdf {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local")
      .appName("tfidf100")
      .getOrCreate()

    import spark.implicits._

    val root = "./"

    val data = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(root + "data/tripadvisor_hotel_reviews.csv")
      .select(regexp_replace(lower(col("Review")), "^\\w\\d ", "").as("Rewiew"))
      .select(split(col("Rewiew"), " ").as("value"))
      .withColumn("reviewId", monotonically_increasing_id() + 1)
      .select(
        col("reviewId"),
        explode(col("value")).as("word")
      )
      .filter(col("word").notEqual(""))
      .groupBy("reviewId", "word")
      .count()

    val reviewWindow = Window.partitionBy("reviewId")

    val wordFreq = data
      .withColumn("len", sum("count") over reviewWindow)
      .withColumn("tf", col("count") / col("len"))
      .select(col("word"), col("reviewId"), col("tf"))

    val wordByDoc = data
      .groupBy(col("word"))
      .agg(count(col("reviewId")) as "count")
      .orderBy(desc("count"))
      .limit(100)

    val tfIdf = wordByDoc
      .select(col("word"), log(lit(data.count) / (col("count") + 1) + 1) as "idf")
      .join(wordFreq, "word")
      .withColumn("tfIdf", col("tf") * col("idf"))
      .select(col("word"), col("reviewId"), col("tfIdf").name("tfIdf"))

    val pivotTable = tfIdf
      .groupBy("reviewId")
      .pivot(col("word"))
      .agg(bround(sum("tfIdf"), 4))
      .na.fill(0.0)

      pivotTable.show(30)

    pivotTable.columns.length
  }
}
