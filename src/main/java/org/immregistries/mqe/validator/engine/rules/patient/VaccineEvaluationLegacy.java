package org.immregistries.mqe.validator.engine.rules.patient;

import java.util.*;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immregistries.lonestar.ForecastManagerSingleton;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.api.impl.CvxCode;
import org.immregistries.lonestar.core.api.impl.ForecastHandlerCore;
import org.immregistries.lonestar.core.api.impl.ForecastOptions;
import org.immregistries.lonestar.core.model.Immunization;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;
import org.immregistries.mqe.validator.detection.Detection;
import org.immregistries.mqe.validator.detection.ValidationReport;
import org.immregistries.mqe.validator.engine.rules.ValidationRuleEntry;
import org.immregistries.mqe.vxu.MqeMessageReceived;
import org.immregistries.mqe.vxu.MqePatient;
import org.immregistries.mqe.vxu.MqeVaccination;
import org.immregistries.mqe.vxu.TargetType;
import org.joda.time.DateTime;


@ValidationRuleEntry(TargetType.Patient)
public class VaccineEvaluationLegacy extends  AbstractVaccineEvaluation<VaccineEvaluationLegacy.ForecastInput, ForecastHandlerCore, ImmunizationForecastDataBean> {

    public VaccineEvaluationLegacy() {
        super();
    }

    protected @NonNull List<ImmunizationForecastDataBean> getImmunizationForecastDataBeans(ForecastInput forecastInput) throws Exception {
        ForecastHandlerCore forecastHandlerCore = ForecastManagerSingleton.getForecastManagerSingleton().getForecastHandlerCore();
        List<ImmunizationForecastDataBean> resultList =
                new ArrayList<ImmunizationForecastDataBean>();
        Map<String, List<Trace>> traceMap = new HashMap<String, List<Trace>>();
        forecastHandlerCore.forecast(forecastInput.doseList, forecastInput.patient,
                forecastInput.imms, forecastInput.forecastDate, traceMap, resultList,
                forecastInput.forecastOptions);
        return resultList;
    }

    protected @NonNull ForecastInput createForecasterInput(MqePatient target, MqeMessageReceived m, Map<String, CvxCode> cvxToVaccineIdMap, DateTime age) {
        ForecastInput forecastInput = new ForecastInput();

        forecastInput.patient
                .setDob(new org.immregistries.lonestar.core.DateTime(target.getBirthDate()));
        forecastInput.patient.setSex(target.getSexCode());

        for (MqeVaccination v : m.getVaccinations()) {
            if (this.common.isValidDate(v.getAdminDateString())) {
                DateTime adminDate = this.datr.parseDateTime(v.getAdminDateString());
                Immunization imm = new Immunization();
                String vaccineCvx = v.getAdminCvxCode();
                if (vaccineCvx != null) {
                    CvxCode cvxCode = null;
                    if (cvxToVaccineIdMap.containsKey(vaccineCvx)) {
                        cvxCode = cvxToVaccineIdMap.get(vaccineCvx);
                    } else {
                        cvxCode = cvxToVaccineIdMap.get("0" + vaccineCvx);
                    }
                    if (cvxCode != null && (age == null || adminDate.isBefore(age))) {
                        imm.setVaccineId(cvxCode.getVaccineId());
                        imm.setCvx(vaccineCvx);
                        imm.setMvx(v.getManufacturerCode());
                        imm.setDateOfShot(adminDate.toDate());
                        forecastInput.imms.add(imm);
                    }
                }
            }
        }
        if (age != null) {
            try {
                forecastInput.forecastDate = new org.immregistries.lonestar.core.DateTime(age.toDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            forecastInput.forecastDate = new org.immregistries.lonestar.core.DateTime(new Date());
        }
        return forecastInput;
    }

    protected AbstractVaccineEvaluation.CountEvalMapAndInvalidDoseCount executeForecasterGetCountEvalMapAndInvalidDoseCount(ForecastInput forecastInput, List<ImmunizationForecastDataBean> resultList, DateTime birthDate) throws Exception {
        ForecastHandlerCore forecastHandlerCore = ForecastManagerSingleton.getForecastManagerSingleton().getForecastHandlerCore();
        Map<String, List<Trace>> traceMap = new HashMap<String, List<Trace>>();
        forecastHandlerCore.forecast(forecastInput.doseList, forecastInput.patient,
                forecastInput.imms, forecastInput.forecastDate, traceMap, resultList,
                forecastInput.forecastOptions);
        int invalidDoseCount = 0;
        HashMap<Integer, Map<String, Integer>> countEvalMapList = new HashMap<>();
        for (int i = 0; i < MONTHS.length; i++) {
            Map<String, Integer> countEvalMap = new HashMap<>();
            countEvalMapList.put(MONTHS[i], countEvalMap);
        }
        for (VaccinationDoseDataBean dose : forecastInput.doseList) {
            if (dose.getStatusCode().equals(VaccinationDoseDataBean.STATUS_VALID)) {
                DateTime adminDate = new DateTime(dose.getAdminDate());
                for (int i = 0; i < MONTHS.length; i++) {
                    Integer month = MONTHS[i];
                    DateTime age = birthDate.plusMonths(month);
                    Map<String, Integer> countEvalMap = countEvalMapList.get(month);
                    if (month == 0 || adminDate.isBefore(age)) {
                        Integer count = countEvalMap.getOrDefault(dose.getForecastCode(), 0);
                        countEvalMap.put(dose.getForecastCode(), count + 1);
                    }
                }
            } else if (dose.getStatusCode().equals(VaccinationDoseDataBean.STATUS_INVALID)) {
                invalidDoseCount++;
//                        System.out.println("==== LEGACY Invalid Dose " + invalidDoseCount  + " ====");
//                        printAllFields(dose);
            }
        }
        CountEvalMapAndInvalidDoseCount result = new CountEvalMapAndInvalidDoseCount(invalidDoseCount, countEvalMapList);
        printSummary("Legacy", forecastInput.imms.size(), (forecastInput.imms.size() - result.invalidDoseCount), result.invalidDoseCount);
        return result;
    }

    protected void fillIssues(MqePatient target, ImmunizationForecastDataBean forecast, List<ValidationReport> issues) {
        String statusDescriptionExternal = forecast.getStatusDescriptionExternal();
        String forecastLabel = forecast.getForecastLabel();
        if (statusDescriptionExternal.equals("complete")) {
            if (forecastLabel.equals("Hib")) {
                issues.add(Detection.VaccineForecastAt24MonthsHibComplete.build(target));
            } else if (forecastLabel.equals("PCV13")) {
                issues.add(Detection.VaccineForecastAt24MonthsPcvComplete.build(target));
            } else if (forecastLabel.equals("Rota")) {
                issues.add(Detection.VaccineForecastAt24MonthsRotaComplete.build(target));
            } else if (forecastLabel.equals("HepB")) {
                issues.add(Detection.VaccineForecastAt24MonthsHepbComplete.build(target));
            }
        }
    }

    protected static class ForecastInput {
        protected List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
        protected List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();
        protected PatientRecordDataBean patient = new PatientRecordDataBean();
        protected org.immregistries.lonestar.core.DateTime forecastDate =
                new org.immregistries.lonestar.core.DateTime("today");
        protected ForecastOptions forecastOptions = new ForecastOptions();
        protected boolean dueUseEarly = false;

    }

}
