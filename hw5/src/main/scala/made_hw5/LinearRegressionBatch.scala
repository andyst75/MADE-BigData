package made_hw5

import breeze.linalg.{sum, DenseVector => BDV}
import org.apache.spark.ml.linalg.{DenseVector, Vector, Vectors}
import org.apache.spark.ml.stat.Summarizer
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.functions.{avg, col, rand}
import org.apache.spark.sql.{Dataset, Row, SparkSession}


class LinearRegressionBatch(val spark: SparkSession, val tol: Double, val lr: Double, val columns: Int)  extends Serializable {
  private val TOL: Double = tol
  private val LR: Double = lr
  private val COLS: Int = columns

  private var slope = BDV.zeros[Double](COLS + 1)

  import spark.implicits._

  def fit(df: Dataset[_], epochs: Int, batchSize: Int, verbose: Boolean = false): Unit = {
    val lossUdf = df.sqlContext.udf.register(Identifiable.randomUID("mae"),
      (feat: Vector, y: Double) => {
        val one = BDV(1.0)
        val x = BDV.vertcat(BDV.create(feat.toArray, 0, 1, feat.size).toDenseVector, one)
        val loss = sum(x * slope) - y
        loss
      }
    )

    val gradUdf = df.sqlContext.udf.register(Identifiable.randomUID("grad"),
      (feat: Vector, loss: Double) => {
        val one = BDV(1.0)
        val x = BDV.vertcat(BDV.create(feat.toArray, 0, 1, feat.size).toDenseVector, one)
        val grad = x * loss
        Vectors.dense(grad.toArray)
      }
    )

    var epoch = 1
    var currentLoss = 1e9
    do {
      val dtTemp = df
        .orderBy(rand())
        .limit(batchSize)
        .withColumn("loss", lossUdf(col("features"), col("label")))
        .withColumn("grad", gradUdf(col("features"), col("loss")))
      val Row(Row(grad), loss) = dtTemp
        .select(
          Summarizer.metrics("mean").summary($"grad").as("grad"),
          avg($"loss").as("loss")
        ).first()

      currentLoss = loss.asInstanceOf[Double]

      val grad_mean = grad.asInstanceOf[DenseVector]
      slope = slope - BDV.create(grad_mean.toArray, 0, 1, grad_mean.size).map(_ * LR)
      if (verbose && (epoch % Math.max(1, epochs % 100) == 0)) {
        val slope_str = slope.data.take(COLS).mkString(", ")
        val intercept = slope.toArray.toList(COLS)
        println(f"Epoch=$epoch%03d slope=($slope_str%s) intercept=$intercept%.6f loss=$currentLoss%.8f")
      }

      epoch += 1
    } while ((epoch < epochs) && (currentLoss.abs > TOL))
  }
}
