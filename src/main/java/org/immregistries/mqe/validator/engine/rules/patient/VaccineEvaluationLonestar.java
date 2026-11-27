package org.immregistries.mqe.validator.engine.rules.patient;

import java.util.*;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immregistries.lonestar.ForecastManagerSingleton;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.api.impl.ForecastHandlerCore;
import org.immregistries.mqe.validator.engine.rules.ValidationRuleEntry;
import org.immregistries.mqe.vxu.TargetType;
import org.joda.time.DateTime;


@ValidationRuleEntry(TargetType.Patient)
public class VaccineEvaluationLonestar extends VaccineEvaluationLegacy {

    public VaccineEvaluationLonestar() {
        super();
    }

    @Override
    protected AbstractVaccineEvaluation.CountEvalMapAndInvalidDoseCount executeForecasterGetCountEvalMapAndInvalidDoseCount(VaccineEvaluationLonestar.ForecastInput forecastInput, List<ImmunizationForecastDataBean> resultList, DateTime birthDate) throws Exception {
        ForecastHandlerCore forecastHandlerCore = ForecastManagerSingleton.getForecastManagerSingleton().getForecastHandlerCore();
        int invalidDoseCount = 0;
        Map<String, List<Trace>> traceMap = new HashMap<String, List<Trace>>();
        forecastHandlerCore.forecast(forecastInput.doseList, forecastInput.patient,
                forecastInput.imms, forecastInput.forecastDate, traceMap, resultList,
                forecastInput.forecastOptions);
        HashMap<Integer, Map<String, Integer>> countEvalMapList = new HashMap<>();
        for (int i = 0; i < MONTHS.length; i++) {
            Map<String, Integer> countEvalMap = new HashMap<>();
            countEvalMapList.put(MONTHS[i], countEvalMap);
        }

//                for (int i = 0; i < forecastInput.imms.size(); i++) {
//                    System.out.println("=== Lonestar injection: " + i + " ===");
//                    printAllFields(forecastInput.imms.get(i));
//                }
        List<ImmunizationInterface> notValidListSubtract = new ArrayList<>(forecastInput.imms);
        for (VaccinationDoseDataBean dose : forecastInput.doseList) {
//                    printAllFields(dose);
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
//                        System.out.println("Tracing");
////                        printAllFields(dose);
//                        System.out.println(DoseInjectionTracer.trace(dose, notValidListSubtract).size());
                notValidListSubtract.removeAll(DoseInjectionTracer.trace(dose, notValidListSubtract));
            } else if (dose.getStatusCode().equals(VaccinationDoseDataBean.STATUS_INVALID)) {
//                        System.out.println("=== Lonestar invalid dose TEST " + ++invalidDoseCount  + " ===");
//                        printAllFields(dose);
            } else {
                notValidListSubtract.removeAll(DoseInjectionTracer.trace(dose, notValidListSubtract));
//                        invalidDoseCount--;
            }
        }

        for (ImmunizationInterface ii: notValidListSubtract) {
            System.out.println("==== Lonestar Invalid Dose =====");
            printAllFields(ii);
        }
        invalidDoseCount = notValidListSubtract.size();
        CountEvalMapAndInvalidDoseCount result = new CountEvalMapAndInvalidDoseCount(invalidDoseCount, countEvalMapList);
        printSummary("Lonestar", forecastInput.imms.size(), (forecastInput.imms.size() - invalidDoseCount), invalidDoseCount);
        return result;
    }

}
