package org.apache.spark.ml.made_hw6

import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}
import org.scalatest.flatspec._
import org.scalatest.matchers._


trait TestData {
  lazy val spark: SparkSession = SparkSession.builder()
    .master("local")
    .appName("HW5")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  lazy val sqlc: SQLContext = spark.sqlContext

  lazy val vectors: Seq[Vector] = Seq(
    Vectors.dense(1, 8, 9, 7),
    Vectors.dense(2, 7, 1, 6),
    Vectors.dense(3, 6, 2, 5),
    Vectors.dense(4, 5, 3, 4)
  )
  lazy val data: DataFrame = {
    import sqlc.implicits._
    vectors.map(x => Tuple1(x)).toDF("features")
  }
  lazy val hyperplanes: Array[Vector] = Array(
    Vectors.dense(1.0, 1.0, -1.0, 1.0),
    Vectors.dense(-1.0, -1.0, 1.0, -1.0),
    Vectors.dense(1.0, 1.0, -1.0, 1.0),
    Vectors.dense(1.0, -1.0, 1.0, 1.0)
  )
  val eps = 1e-6
}


class RandomLSHTest extends AnyFlatSpec with should.Matchers with TestData {

  "RandomLSH" should "test hash function" in {
    val randomLSHModel: RandomLSHModel =
      new RandomLSHModel(randomHyperPlanes = hyperplanes)
      .setInputCol("features")
      .setOutputCol("hashes")
    val testVector = Vectors.dense(1, 2, 3, 4)
    val result = randomLSHModel.hashFunction(testVector)

    result.length should be(4)
    result(0)(0) should be(1.0)
    result(1)(0) should be(-1.0)
    result(2)(0) should be(1.0)
    result(3)(0) should be(1.0)
  }

  "RandomLSH" should "positive test hash distance" in {
    val randomLSHModel: RandomLSHModel =
      new RandomLSHModel(randomHyperPlanes = hyperplanes)
      .setInputCol("features")
      .setOutputCol("hashes")
    val result1 = randomLSHModel.hashFunction(
      Vectors.dense(1, 2, 3, 4)
    )
    val result2 = randomLSHModel.hashFunction(
      Vectors.dense(9, 8, 7, 6)
    )
    val similarity = randomLSHModel.hashDistance(result1, result2)

    println("RandomLSH positive test hash distance")
    println(result1.toSeq)
    println(result2.toSeq)

    similarity should be(1.0 +- eps)
  }

  "RandomLSH" should "negative test hash distance" in {
    val randomLSHModel: RandomLSHModel =
      new RandomLSHModel(randomHyperPlanes = hyperplanes)
      .setInputCol("features")
      .setOutputCol("hashes")
    val result1 = randomLSHModel.hashFunction(
      Vectors.dense(1, 2, 3, 4)
    )
    val result2 = randomLSHModel.hashFunction(
      Vectors.dense(-1, -2, -2, -1)
    )
    val similarity = randomLSHModel.hashDistance(result1, result2)

    println("RandomLSH negative test hash distance")
    println(result1.toSeq)
    println(result2.toSeq)

    similarity should be(0.0 +- eps)
  }

  "RandomLSH" should "test key distance" in {
    val randomLSHModel: RandomLSHModel =
      new RandomLSHModel(randomHyperPlanes = hyperplanes)
        .setInputCol("features")
        .setOutputCol("hashes")
    val testVector1 = Vectors.dense(1, 2, 3, 4)
    val testVector2 = Vectors.dense(4, 3, 2, 1)
    val keyDistance = randomLSHModel.keyDistance(testVector1, testVector2)

    keyDistance should be(1.0/3.0 +- eps)
  }

  "RandomLSH" should "transform data" in {
    val randomLSH: RandomLSH = new RandomLSH(
    ).setNumHashTables(2)
      .setInputCol("features")
      .setOutputCol("hashes")
    val transformedData = randomLSH.fit(data).transform(data)

    transformedData.count() should be(4)
  }

}
