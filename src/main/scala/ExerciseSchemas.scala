object ExerciseSchemas {
  /**
   * Partial schema for Patient table, contains only fields needed for this exercise but could be expanded as needed
   * @param patient_id Unique key for each patient in this table
   * @param sex Patient's sex
   * @param age Patient's age
   * @param primary_care_provider ID of the patient's primary care provider
   */
  case class Patient(
                    patient_id: String,
                    sex: String,
                    age: Int,
                    primary_care_provider: String
                    )


  /**
   * Partial schema for Medications table, contains only fields needed for this exercise
   * @param encounter_id Foreign key linking this to the Encounters table
   * @param medication_simple_generic_name Generic name for the medication
   * @param minimum_dose Minimum dosage used in this encounter
   * @param dose_unit Unit of measurement for minimum dose
   * @param avg_minimum_dose Average minimum dose for this medication (grouped on medication_simple_generic_name)
   */
  case class Medication(
                       encounter_id: String,
                       medication_simple_generic_name: String,
                       minimum_dose: Int,
                       dose_unit: String,
                       avg_minimum_dose: Double
                       )

  /**
   * Partial schema for Encounters table, contains only fields needed for this exercise
   * @param patient_id Patient ID, as described in Patients table
   * @param encounter_id Unique ID for this encounter
   * @param admit_diagnosis Diagnosis code for this visit
   */
  case class Encounter(
                      patient_id: String,
                      encounter_id: String,
                      admit_diagnosis: String
                      )

  /**
   * Schema for the final output of this exercise
   * @param patient_id Patient ID, as described in Patients table
   * @param sex Patient's sex
   * @param age Patient's age
   * @param primary_care_provider Patient's primary care provider
   * @param medication_simple_generic_name Generic name of the medication the patient was treated with
   * @param avg_minimum_dose Average minimum dose given to all patients for the medication the patient was treated with
   * @param dose_unit Dose unit for this treatment
   * @param admit_diagnosis Diagnosis code for this patient's visit
   */
  case class OutputSchema(
                           patient_id: String,
                           sex: String,
                           age: String,
                           primary_care_provider: String,
                           medication_simple_generic_name: String,
                           avg_minimum_dose: Double,
                           dose_unit: String,
                           admit_diagnosis: String
  )
}