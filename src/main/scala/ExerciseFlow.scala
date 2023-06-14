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

object ExerciseFlow {
  def main(args: Array[String]): Unit = {
    val inputPath = "C:/Users/Oliver/IdeaProjects/tendo-candidate-exercise-p1/src/main/resources/Exercise Data.xlsx"
    val tempPath = "./temp"
    val outputPath = "./data"

    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    val spark = SparkSession.builder
      .appName("Simple Application")
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
      .select($"patientid" as "patientId", $"Sex" as "sex", $"Age".cast(IntegerType) as "age", $"primary_care_provider" as "primaryCareProvider")
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
      .withColumn("minimumDose", $"minimum_dose".cast(IntegerType))
      .withColumn("avgMinimumDose", avg("minimumDose").over(medicationWindowSpec))
      .select($"encounterid" as "encounterId" ,$"medication_simple_generic_name" as "medicationSimpleGenericName", $"minimumDose", $"dose_unit" as "doseUnit", $"avgMinimumDose")
      .as[Medication]

    val encountersDs = spark.read.excel(
      header = true,
      dataAddress = "'encounter_e1'!A1",
      treatEmptyValuesAsNulls = true,
      inferSchema = false,
      usePlainNumberFormat = true
    ).load(inputPath)
      .select($"patientid" as "patientId", $"encounterid" as "encounterId", $"admit_diagnosis" as "admitDiagnosis")
      .as[Encounter]

    // Debugging statements
    patientDs.show(false)
    patientDs.printSchema()
    medicationsDs.show()
    medicationsDs.printSchema()
    encountersDs.printSchema()
    encountersDs.show()

    val resultDs = patientDs.join(encountersDs, usingColumn = "patientId")
      .join(medicationsDs, usingColumn = "encounterId")
      //.as[OutputSchema] TODO rename columns so this works

    resultDs.show(false)

    // Save as single csv part file
    resultDs.coalesce(1).write.format("csv")
      .option("header", true)
      .option("delimiter", "|")
      .option("lineSep", "\n")
      .mode(SaveMode.Overwrite)
      .save(tempPath)

    // Use Hadoop FS to move and rename file to expected format
    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)

    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val fileName = fs.globStatus(new Path(tempPath + "/part*"))(0).getPath.getName
    fs.rename(new Path(tempPath + "/" + fileName), new Path(outputPath + s"/target_1_$currentDate.txt"))

    // Remove the temp folder
    fs.delete(new Path(tempPath), true)

    // Metrics on missing data
    val nullMedicationCount = resultDs.where($"medicationSimpleGenericName".isNull).count()
    val blankMedicationCount = resultDs.where($"medicationSimpleGenericName" === "").count()
    val totalCount = resultDs.count()
    println(s"Number of null medication generic name records: $nullMedicationCount")
    println(s"Number of blank medication records: $blankMedicationCount")
    println(s"Number of total records: $totalCount")

  }
}