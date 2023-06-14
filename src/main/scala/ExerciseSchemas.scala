object ExerciseSchemas {
  /**
   * Partial schema for Patient table, contains only fields needed for this exercise but could be expanded as needed
   * @param patientid Unique key for each patient in this table
   * @param sex Patient's sex
   * @param age Patient's age
   * @param primary_care_provider ID of the patient's primary care provider
   */
  case class Patient(
                    patientId: String,
                    sex: String,
                    age: Int,
                    primaryCareProvider: String
                    )


  /**
   * Partial schema for Medications table, contains only fields needed for this exercise
   * @param encounterId Foreign key linking this to the Encounters table
   * @param medicationSimpleGenericName Generic name for the medication
   * @param minimumDose Minimum dosage used in this encounter
   * @param doseUnit Unit of measurement for minimum dose
   */
  case class Medication(
                       encounterId: String,
                       medicationSimpleGenericName: String,
                       minimumDose: Int,
                       doseUnit: String,
                       avgMinimumDose: Double
                       )

  /**
   * Partial schema for Encounters table, contains only fields needed for this exercise
   * @param patientId Patient ID, as described in Patients table
   * @param encounterId Unique ID for this encounter
   * @param admitDiagnosis Diagnosis code for this visit
   */
  case class Encounter(
                      patientId: String,
                      encounterId: String,
                      admitDiagnosis: String
                      )

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