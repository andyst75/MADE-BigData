package hw3

import hw3.Csv.writeCsv
import scopt.OParser

import java.util.NoSuchElementException

case class Config(
                   trainDataFile: String = "",
                   trainTargetFile: String = "",
                   testDataFile: String = "",
                   predictFile: String = "",
                 )

object Main extends App {

  val builder = OParser.builder[Config]
  val cli_parser = {
    import builder._
    OParser.sequence(
      programName("hw3"),
      head("LinearRegression", "0.1a"),

      opt[String]("trainDataFile")
        .required()
        .action((x, c) => c.copy(trainDataFile = x))
        .text("path to train data CSV file"),

      opt[String]("trainTargetFile")
        .required()
        .action((x, c) => c.copy(trainTargetFile = x))
        .text("path to train target CSV file"),

      opt[String]("testDataFile")
        .action((x, c) => c.copy(testDataFile = x))
        .text("path to test data CSV file (optional)"),

      opt[String]("predictFile")
        .action((x, c) => c.copy(predictFile = x))
        .text("path to predicted file (optional)"),

      note(
        """
          |for example:
          |hw3 --trainDataFile data/train_data.csv --trainTargetFile data/train_target.csv \
          | --testDataFile data/test_data.csv --predictFile data/target.csv
          |
          |another example (for model fitting):
          |hw3 --trainDataFile data/train_data.csv --trainTargetFile data/train_target.csv
          |""".stripMargin),

      help("help") text ("prints this usage text"),

      checkConfig(
        c =>
          if ((c.predictFile.nonEmpty && c.testDataFile.isEmpty) ||
            (c.predictFile.isEmpty && c.testDataFile.nonEmpty))
            failure("use --testDataFile with --predictFile")
          else success)
    )
  }

  val config = try {
    OParser.parse(cli_parser, args, Config()).get
  } catch { case _: NoSuchElementException => sys.exit() }

  val trainDataFile = config.trainDataFile
  val testDataFile = config.testDataFile
  val trainTargetFile = config.trainTargetFile
  val predictFile = config.predictFile

  val trainData = Csv.readDataCsv(trainDataFile)
  val trainTarget = Csv.readTargetCsv(trainTargetFile)

  val lr = new LinearRegression()

  lr.fit(trainData, trainTarget)

  println("Intercept: " + lr.getIntercept)
  println("Coefficients: " + lr.getCoef.data.toList)

  if (testDataFile.nonEmpty) {
    val testData = Csv.readDataCsv(testDataFile)

    val predict = lr.predict(testData)
    writeCsv(predictFile, predict)

    println("Predict saved.")
  }

}
