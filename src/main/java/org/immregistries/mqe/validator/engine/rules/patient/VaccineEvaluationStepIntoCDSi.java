package org.immregistries.mqe.validator.engine.rules.patient;

import java.util.*;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immregistries.lonestar.core.api.impl.CvxCode;
import org.immregistries.mqe.validator.detection.Detection;
import org.immregistries.mqe.validator.detection.ValidationReport;
import org.immregistries.mqe.validator.engine.rules.ValidationRuleEntry;
import org.immregistries.mqe.vxu.MqeMessageReceived;
import org.immregistries.mqe.vxu.MqePatient;
import org.immregistries.mqe.vxu.MqeVaccination;
import org.immregistries.mqe.vxu.TargetType;
import org.joda.time.DateTime;
import org.openimmunizationsoftware.cdsi.core.data.DataModel;
import org.openimmunizationsoftware.cdsi.core.data.DataModelLoader;
import org.openimmunizationsoftware.cdsi.core.data.ForecastInput;
import org.openimmunizationsoftware.cdsi.core.domain.*;
import org.openimmunizationsoftware.cdsi.core.domain.datatypes.TargetDoseStatus;
import org.openimmunizationsoftware.cdsi.core.logic.LogicStep;
import org.openimmunizationsoftware.cdsi.core.logic.LogicStepFactory;
import org.openimmunizationsoftware.cdsi.core.logic.LogicStepType;


@ValidationRuleEntry(TargetType.Patient)
public class VaccineEvaluationStepIntoCDSi extends VaccineEvaluation<ForecastInput, DataModel, VaccineGroupForecast> {

  public static final String SUPPORTING_DATA_SET = "USA-DEMO-HPV9-0.1.zip";

  public VaccineEvaluationStepIntoCDSi() {
    super();
  }

  @Override
  String getDiseaseLabel(Disease disease) {
    switch (disease) {
      case HEPB:
        return "HepB";
      case HEPA:
        return "HepA";
      case POLIO:
        return "Polio";
      case MEASLES:
        return "Measles";
      case VARICELLA:
        return "Varicella";
      case HIB:
        return "Hib";
      case DIPHTHERIA:
        return "Diphtheria";
      case PNEUMOCOCCAL:
        return "Pneumococcal";
      default:
        throw new IllegalArgumentException("No label for disease " + disease);
    }
  }

  @Override
  void fillIssues(MqePatient target, VaccineGroupForecast forecast, List<ValidationReport> issues) {
    String forecastLabel = forecast.getVaccineGroup().getName();
    if (forecast.getVaccineGroupStatus().equals(VaccineGroupStatus.COMPLETE)) {
      if (forecastLabel.equals("Hib")) {
        issues.add(Detection.VaccineForecastAt24MonthsHibComplete.build(target));
      } else if (forecastLabel.equals("Pneumococcal")) {
        issues.add(Detection.VaccineForecastAt24MonthsPcvComplete.build(target));
      } else if (forecastLabel.equals("Rotavirus")) {
        issues.add(Detection.VaccineForecastAt24MonthsRotaComplete.build(target));
      } else if (forecastLabel.equals("HepB")) {
        issues.add(Detection.VaccineForecastAt24MonthsHepbComplete.build(target));
      }
    }
  }

  @Override
  @NonNull
  List<VaccineGroupForecast> getImmunizationForecastDataBeans(ForecastInput forecastInput) throws Exception {
    DataModel dataModel = executeForecasting(forecastInput);
    List<VaccineGroupForecast> vaccineGroupForecastList = dataModel.getVaccineGroupForecastList();
    if (vaccineGroupForecastList != null) {
      return vaccineGroupForecastList;
    }
    return Collections.emptyList();
  }

  @Override
  @NonNull
  ForecastInput createForecasterInput(MqePatient target, MqeMessageReceived m, Map<String, CvxCode> cvxToVaccineIdMap, DateTime age) {
    ForecastInput forecastInput = new ForecastInput();

    forecastInput.setPatientDateOfBirth(target.getBirthDate());
    forecastInput.setPatientSex(target.getSex());

    for (MqeVaccination v : m.getVaccinations()) {
      if (this.common.isValidDate(v.getAdminDateString())) {
        DateTime adminDate = this.datr.parseDateTime(v.getAdminDateString());
        String vaccineCvx = v.getAdminCvxCode();
        if (vaccineCvx != null) {
          CvxCode cvxCode = null;
          if (cvxToVaccineIdMap.containsKey(vaccineCvx)) {
            cvxCode = cvxToVaccineIdMap.get(vaccineCvx);
          } else {
            cvxCode = cvxToVaccineIdMap.get("0" + vaccineCvx);
          }
          if (cvxCode != null) {
            ForecastInput.VaccinationInput vaccinationInput  = forecastInput.addVaccination();
            vaccinationInput.setVaccineCvx(cvxCode.getCvxCode());
            vaccinationInput.setDateAdministered(adminDate.toDate());
            vaccinationInput.setVaccineMvx(v.getManufacturerCode());
//              vaccinationInput.setDoseCondition();
          }
        }
      }
      if (age != null) {
        try {
          forecastInput.setAssessmentDate(age.toDate());
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        forecastInput.setAssessmentDate(new Date());

      }
    }
    return forecastInput;
  }

  @Override
  protected VaccineEvaluation.CountEvalMapAndInvalidDoseCount executeForecasterGetCountEvalMapAndInvalidDoseCount(ForecastInput forecastInput, List<VaccineGroupForecast> resultList, DateTime birthDate) throws Exception {
      DataModel dataModel = executeForecasting(forecastInput);
      int invalidDoseCount = 0;
      HashMap<Integer, Map<String, Integer>> countEvalMapList = new HashMap<>();
      for (int i = 0; i < MONTHS.length; i++) {
        Map<String, Integer> countEvalMap = new HashMap<>();
        countEvalMapList.put(MONTHS[i], countEvalMap);
      }
      // Filtering invalid doses from count and from detections
      List<ForecastInput.VaccinationInput> collectVaccinationInput = new ArrayList<>(forecastInput.getVaccinationList());
      List<AntigenAdministeredRecord> antigenAdministeredRecords = dataModel.getAntigenAdministeredRecordList();
      List<PatientSeries> patientSeriesList = dataModel.getBestPatientSeriesList();
      for (PatientSeries patientSeries : patientSeriesList) {
        List<TargetDose> targetDoseList = patientSeries.getTargetDoseList();
        for (TargetDose dose : targetDoseList) {
          if (dose.getTargetDoseStatus().equals(TargetDoseStatus.SATISFIED)) {
            ForecastInput.VaccinationInput vaccinationInput = dataModel.getVaccinationInputMap().get(dose.getSatisfiedByVaccineDoseAdministered());
            collectVaccinationInput.remove(vaccinationInput);
//              System.out.println("=== STEP is valid input ===");
//              printAllFields(vaccinationInput);
            DateTime adminDate = new DateTime(dose.getEvaluation().getVaccineDoseAdministered().getDateAdministered());
            for (int i = 0; i < MONTHS.length; i++) {
              Integer month = MONTHS[i];
              DateTime age = birthDate.plusMonths(month);
              Map<String, Integer> countEvalMap = countEvalMapList.get(month);
              if (month == 0 || adminDate.isBefore(age)) {
                String forecastCode = dose.getTrackedSeriesDose().getAntigenSeries().getTargetDisease().getName();
                Integer count = countEvalMap.getOrDefault(forecastCode, 0);
                countEvalMap.put(forecastCode, count + 1);
              }
            }
          }
        }
      }
      List<ForecastInput.VaccinationInput> invalidDoses = new ArrayList<>(forecastInput.getVaccinationList());
      invalidDoses.removeAll(dataModel.getSatisfiedVaccinationInputList());
      invalidDoseCount = collectVaccinationInput.size();
      System.out.println("=== Invalid VaccinationInputs StepIntoCDSI summary ===");
      for (ForecastInput.VaccinationInput vaccinationInput : collectVaccinationInput) {
        printAllFields(vaccinationInput);
      }

      CountEvalMapAndInvalidDoseCount result = new CountEvalMapAndInvalidDoseCount(invalidDoseCount, countEvalMapList);
      printSummary("Step Into CDSI", forecastInput.getVaccinationList().size(), forecastInput.getVaccinationList().size() - invalidDoseCount, invalidDoseCount);
      return result;
  }

  private static @NonNull DataModel executeForecasting(ForecastInput forecastInput) {
    DataModel dataModel = null;
    try {
      List<String> names = DataModelLoader.listBundledSupportingDataZipNames();
      dataModel = DataModelLoader.createDataModel(names.get(0));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    dataModel.setForecastInput(forecastInput);
    LogicStep firstStep =
          LogicStepFactory.createLogicStep(LogicStepType.GATHER_NECESSARY_DATA_FOR_VALIDATION, dataModel);
    // Execute the first step separately so its exception handling can differ:
    // on failure the caller cleans the offending input and retries.
    try {
      dataModel.setNextLogicStep(firstStep.process());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    int count = 0;
    while (dataModel.getLogicStep().getLogicStepType() != LogicStepType.END) {
      LogicStep nextLogicStep = null;
      try {
        nextLogicStep = dataModel.getLogicStep().process();
        dataModel.setNextLogicStep(nextLogicStep);
        count++;
        if (count > 100000) {
          System.err.println(
                  "Appear to be caught in a loop at this step: " + dataModel.getLogicStep().getTitle());
          // too many steps!
          if (count > 100100) {
            throw new RuntimeException(
                    "Logic steps seem to be caught in a loop, " + dataModel.getLogicStep().getTitle()
                            + ", unable to get results");
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return dataModel;
  }


}
