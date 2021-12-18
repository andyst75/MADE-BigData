package org.apache.spark.ml.made_hw6

import org.apache.hadoop.fs.Path
import org.apache.spark.ml.feature.{LSH, LSHModel}
import org.apache.spark.ml.linalg.{Matrices, Matrix, Vector, VectorUDT, Vectors}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util._
import org.apache.spark.sql.types.StructType

import scala.util.Random


/**
 * Locality Sensitive Hashing. Random implementation.
 */
class RandomLSH(override val uid: String) extends LSH[RandomLSHModel] {

  def this() = {
    this(Identifiable.randomUID("randomLSH"))
  }

  override def copy(extra: ParamMap): this.type = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), new VectorUDT)
    validateAndTransformSchema(schema)
  }

  override protected[this] def createRawLSHModel(inputDim: Int): RandomLSHModel = {
    val rand = new Random(42)
    val randomHyperPlanes: Array[Vector] = {
      Array.fill($(numHashTables)) {
        Vectors.dense(Array.fill(inputDim)({
          if (rand.nextGaussian() > 0) 1.0 else -1.0
        }))
      }
    }
    new RandomLSHModel(uid, randomHyperPlanes)
  }

}

class RandomLSHModel private[made_hw6](
                                        override val uid: String,
                                        private[made_hw6] val randomHyperPlanes: Array[Vector]
                                      ) extends LSHModel[RandomLSHModel] {

  override def write: MLWriter = {
    new RandomLSHModelWriter(this)
  }

  override def copy(extra: ParamMap): RandomLSHModel = {
    val copied = new RandomLSHModel(uid, randomHyperPlanes).setParent(parent)
    copyValues(copied, extra)
  }

  override protected[ml] def hashFunction(elems: Vector): Array[Vector] = {
    val hashValues = randomHyperPlanes.map(
      rhp => if (elems.dot(rhp) >= 0) 1 else -1
    )
    hashValues.map(Vectors.dense(_))
  }

  override protected[ml] def keyDistance(x: Vector, y: Vector): Double = {
    if (Vectors.norm(x, 2) == 0 || Vectors.norm(y, 2) == 0) {
      1.0
    } else {
      1.0 - x.dot(y) / (Vectors.norm(x, 2) * Vectors.norm(y, 2))
    }
  }

  override protected[ml] def hashDistance(x: Array[Vector], y: Array[Vector]): Double = {
    x.zip(y).map(item => if (item._1 == item._2) 1 else 0).sum.toDouble / x.length
  }

  private[made_hw6] def this(randomHyperPlanes: Array[Vector]) =
    this(Identifiable.randomUID("randomLSH"), randomHyperPlanes)

}


private[made_hw6] class RandomLSHModelWriter(instance: RandomLSHModel) extends MLWriter {

  override protected def saveImpl(path: String): Unit = {
    DefaultParamsWriter.saveMetadata(instance, path, sc)
    val randMatrix = Matrices.dense(instance.randomHyperPlanes.length,
      instance.randomHyperPlanes.head.size,
      instance.randomHyperPlanes.map(_.toArray).reduce(Array.concat(_, _)))
    val dataPath = new Path(path, "data").toString
    sparkSession.createDataFrame(Seq(Data(randMatrix))).repartition(1).write.parquet(dataPath)
  }

  private case class Data(randHyperPlanes: Matrix)
}