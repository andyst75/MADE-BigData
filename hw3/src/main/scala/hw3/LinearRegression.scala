package hw3

import breeze.linalg.{DenseMatrix, DenseVector, pinv}
import breeze.stats.mean

class LinearRegression {
  private var intercept = 0.0
  private var coef: Option[DenseVector[Double]] = None

  def getIntercept: Double = intercept

  def getCoef: DenseVector[Double] = coef match {
    case Some(coef) => coef
    case None       => throw new RuntimeException("Model is not fitted!")
  }

  def fit(x: DenseMatrix[Double], y: DenseVector[Double]): Unit = {
    coef = Some(pinv(x.t * x) * x.t * y)

    intercept = mean(y) -
      coef
        .get
        .activeValuesIterator
        .zipWithIndex
        .foldLeft(0.0) {
          case (acc, (coef, index)) =>
            val vector: DenseVector[Double] = x(::, index)
            val meadVector = mean(vector)
            acc + coef * meadVector
        }
  }

  def predict(x: DenseMatrix[Double]): DenseVector[Double] = {
    x * getCoef + getIntercept
  }

}
