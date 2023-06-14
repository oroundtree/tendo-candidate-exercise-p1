import ExerciseSchemas.{Encounter, Medication, OutputSchema, Patient}
import org.apache.spark.sql.{Dataset, Encoders, SaveMode, SparkSession}
import com.crealytics.spark.excel._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.avg
import org.apache.spark.sql.types.IntegerType

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Properties

object ExerciseFlow {
  def main(args: Array[String]): Unit = {
    val inputPath = Properties.envOrElse("INPUT_PATH", "./src/main/resources/Exercise Data.xlsx")
    val tempPath = "./temp"
    val outputPath = Properties.envOrElse("OUTPUT_PATH", "./data")

    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    val spark = SparkSession.builder
      .appName("Tendo Exercise")
      .config("spark.master", "local")
      .getOrCreate()

    import spark.implicits._

    // Load the dataframes in from their source spreadsheet tabs
    // Note, data in its source form is basically schemaless, so these casts could fail
    val patientDs: Dataset[Patient] = spark.read.excel(
      header = true,
      dataAddress = "'patient_e1'!A1",
      treatEmptyValuesAsNulls = true,
      inferSchema = false,
      usePlainNumberFormat = true
    ).load(inputPath)
      .select($"patientid" as "patient_id", $"Sex" as "sex", $"Age".cast(IntegerType) as "age", $"primary_care_provider")
      .as[Patient]

    // Partition on medication generic name to calculate average minimum dose
    val medicationWindowSpec = Window.partitionBy("medication_simple_generic_name")
    val medicationsDs = spark.read.excel(
      header = true,
      dataAddress = "'medications_e1'!A1",
      treatEmptyValuesAsNulls = true,
      inferSchema = false,
      usePlainNumberFormat = true
    ).load(inputPath)
      .withColumn("minimum_dose", $"minimum_dose".cast(IntegerType))
      .withColumn("avg_minimum_dose", avg("minimum_dose").over(medicationWindowSpec))
      .select($"encounterid" as "encounter_id" ,$"medication_simple_generic_name", $"minimum_dose", $"dose_unit", $"avg_minimum_dose")
      .as[Medication]

    val encountersDs = spark.read.excel(
      header = true,
      dataAddress = "'encounter_e1'!A1",
      treatEmptyValuesAsNulls = true,
      inferSchema = false,
      usePlainNumberFormat = true
    ).load(inputPath)
      .select($"patientid" as "patient_id", $"encounterid" as "encounter_id", $"admit_diagnosis")
      .as[Encounter]

    // Debugging statements
    patientDs.show(false)
    patientDs.printSchema()
    medicationsDs.show()
    medicationsDs.printSchema()
    encountersDs.printSchema()
    encountersDs.show()

    val resultDs = patientDs.join(encountersDs, usingColumn = "patient_id")
      .join(medicationsDs, usingColumn = "encounter_id")
      .as[OutputSchema]

    resultDs.show(false)

    // Save as single csv part file
    resultDs.coalesce(1).write.format("csv")
      .option("header", true)
      .option("delimiter", "|")
      .option("lineSep", "\n")
      .mode(SaveMode.Overwrite)
      .save(tempPath)

    // Use Hadoop FS to move and rename file to expected format
    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val fileName = fs.globStatus(new Path(tempPath + "/part*"))(0).getPath.getName
    fs.rename(new Path(tempPath + "/" + fileName), new Path(outputPath + s"/target_1_$currentDate.txt"))

    // Remove the temp folder
    fs.delete(new Path(tempPath), true)

    // Metrics on missing data
    val nullMedicationCount = resultDs.where($"medication_simple_generic_name".isNull).count()
    val blankMedicationCount = resultDs.where($"medication_simple_generic_name" === "").count()
    val totalCount = resultDs.count()
    println(s"Number of null medication generic name records: $nullMedicationCount")
    println(s"Number of blank medication records: $blankMedicationCount")
    println(s"Number of total records: $totalCount")

  }
}
