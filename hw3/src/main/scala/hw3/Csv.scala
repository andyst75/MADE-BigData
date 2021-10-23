package hw3

import java.io.File
import breeze.linalg.{DenseMatrix, DenseVector, csvread, csvwrite}

object Csv {
  def readDataCsv(path: String):DenseMatrix[Double] = {
    csvread(new File(path))
  }
  def readTargetCsv(path: String):DenseVector[Double] = {
    println(path)
    csvread(new File(path)).toDenseVector
  }
  def writeCsv(path: String, data: DenseVector[Double]) = {
    csvwrite(new File(path), data.asDenseMatrix)
  }
}
