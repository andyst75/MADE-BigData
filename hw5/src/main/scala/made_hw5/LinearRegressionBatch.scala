package made_hw5

import breeze.linalg.{inv, sum, DenseMatrix => BDM, DenseVector => BDV}
import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.sql.functions.rand
import org.apache.spark.sql.{Dataset, Row, SparkSession}


class LinearRegressionBatch(val spark: SparkSession,
                            val tol: Double, val columns: Int)  extends Serializable {
  private val TOL: Double = tol
  private val COLS: Int = columns

  private var slope = BDV.zeros[Double](COLS + 1)

  import spark.implicits._

  def fit(df: Dataset[_], epochs: Int, batchSize: Int, verbose: Boolean = false): Unit = {
    var epoch = 1
    var currentLoss = 1e9

    do {

      val dataf = df
        .select("features", "label")
        .orderBy(rand())
        .limit(batchSize)
        .collect().toSeq

      val (data_ft: Array[Double], data_y: Array[Double]) = dataf
        .foldLeft((Array[Double](), Array[Double]())) {
          case ((denseVectorList, doubleList), Row(ft: DenseVector, y: Double)) =>
            (denseVectorList :++ ft.values, doubleList :+ y)
        }

      val ft_ = BDM.create(COLS, batchSize, data_ft).t
      val one = BDM.ones[Double](batchSize, 1)

      val ft = BDM.horzcat(ft_, one)
      val y = BDV.create(data_y, 0, 1, batchSize).toDenseVector

      if (epoch == 1) {
        slope = inv(ft.t * ft) * ft.t * y
      } else {
        slope = (slope + inv(ft.t * ft) * ft.t * y) / 2.0
      }

      currentLoss = sum(y - ft * slope)

      if (verbose && (epoch % Math.max(1, epochs % 100) == 0)) {
        val slope_str = slope.data.take(COLS).mkString(", ")
        val intercept = slope.toArray.toList(COLS)
        println(f"Epoch=$epoch%03d slope=($slope_str%s) intercept=$intercept%.6f loss=$currentLoss%.8f")
      }

      epoch += 1
    } while ((epoch < epochs) && (currentLoss.abs > TOL))
  }
}