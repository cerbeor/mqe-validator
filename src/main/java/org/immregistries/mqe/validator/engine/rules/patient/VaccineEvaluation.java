package org.immregistries.mqe.validator.engine.rules.patient;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immregistries.lonestar.core.api.impl.CvxCode;
import org.immregistries.lonestar.core.api.impl.CvxCodes;
import org.immregistries.mqe.validator.detection.Detection;
import org.immregistries.mqe.validator.detection.ImplementationDetail;
import org.immregistries.mqe.validator.detection.ValidationReport;
import org.immregistries.mqe.validator.engine.ValidationRule;
import org.immregistries.mqe.validator.engine.ValidationRuleResult;
import org.immregistries.mqe.vxu.MqeMessageReceived;
import org.immregistries.mqe.vxu.MqePatient;
import org.joda.time.DateTime;

import java.util.*;


public abstract class VaccineEvaluation<GenericForecastInput, GenericForecastHandlerCore, ImmunizationForecastDataBean> extends ValidationRule<MqePatient> {

    public static final int MONTHS_0 = 0;
    public static final int MONTHS_24 = 25;
    public static final int MONTHS_18 = 19;
    public static final int MONTHS_15 = 16;

    public VaccineEvaluation() {

        this.addRuleDetection(Detection.VaccineEvaluationAt18MonthsHepb3);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt18MonthsHepb3);
            id.setImplementationDescription(
                    "Vaccination evaluation at 18 months of age for 3 valid doses of HepB.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt18MonthsDtap4);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt18MonthsDtap4);
            id.setImplementationDescription(
                    "Vaccination evaluation at 18 months of age for 4 valid doses of DTaP.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsPcv4);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsPcv4);
            id.setImplementationDescription(
                    "Vaccination evaluation at 15 months of age for 4 valid doses of PCV.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsPolio3);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsPolio3);
            id.setImplementationDescription(
                    "Vaccination evaluation at 15 months of age for 3 valid doses of Polio.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsMmr1);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsMmr1);
            id.setImplementationDescription(
                    "Vaccination evaluation at 15 months of age for 1 valid dose of MMR.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsVar1);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsVar1);
            id.setImplementationDescription(
                    "Vaccination evaluation at 15 months of age for 1 valid dose of Varicella.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt18MonthsHepa2);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt18MonthsHepa2);
            id.setImplementationDescription(
                    "Vaccination evaluation at 18 months of age for 1 valid dose of HepA.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsHib2);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt15MonthsHib2);
            id.setImplementationDescription(
                    "Vaccination evaluation at 15 months of age for 2 valid doses of Hib.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsDtap4);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsDtap4);
            id.setImplementationDescription(
                    "Vaccination evaluation at 24 months of age for 4 valid doses of DTaP.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsPolio3);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsPolio3);
            id.setImplementationDescription(
                    "Vaccination evaluation at 24 months of age for 3 valid doses of Polio.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsMmr1);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsMmr1);
            id.setImplementationDescription(
                    "Vaccination evaluation at 24 months of age for 1 valid dose of MMR.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsHib3);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsHib3);
            id.setImplementationDescription(
                    "Vaccination evaluation at 24 months of age for 3 valid doses of Hib.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsHepb3);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsHepb3);
            id.setImplementationDescription(
                    "Vaccination evaluation at 24 months of age for 3 valid doses of HepB.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsVar1);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsVar1);
            id.setImplementationDescription(
                    "Vaccination evaluation at 24 months of age for 1 valid dose of Varicella.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsPcv4);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsPcv4);
            id.setImplementationDescription(
                    "Vaccination evaluation at 24 months of age for 4 valid doses of PCV.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses1orMore);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses1orMore);
            id.setImplementationDescription(
                    "Vaccination evaluation has 1 or more invalid doses.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses2orMore);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses2orMore);
            id.setImplementationDescription(
                    "Vaccination evaluation has 2 or more invalid doses.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses3orMore);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses3orMore);
            id.setImplementationDescription(
                    "Vaccination evaluation has 3 or more invalid doses.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses4orMore);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses4orMore);
            id.setImplementationDescription(
                    "Vaccination evaluation has 4 or more invalid doses.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses5orMore);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses5orMore);
            id.setImplementationDescription(
                    "Vaccination evaluation has 5 or more invalid doses.");
        }
        this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses10orMore);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationHasInvalidDoses10orMore);
            id.setImplementationDescription(
                    "Vaccination evaluation has 10 or more invalid doses.");
        }
        this.addRuleDetection(Detection.VaccineCoverageAt24MonthsSeries4_3_1_3_3_1_4);
        {
            ImplementationDetail id =
                    this.addRuleDetection(Detection.VaccineCoverageAt24MonthsSeries4_3_1_3_3_1_4);
            id.setImplementationDescription(
                    "Vaccination coverage at 24 months of age for 4:3:1:3:3:1:4 series.");
        }
        this.addRuleDetection(Detection.VaccineCoverageAt36MonthsSeries4_3_1_3_3_1_4);
        {
            ImplementationDetail id =
                    this.addRuleDetection(Detection.VaccineCoverageAt36MonthsSeries4_3_1_3_3_1_4);
            id.setImplementationDescription(
                    "Vaccination coverage at 36 months of age for 4:3:1:3:3:1:4 series.");
        }


        this.addRuleDetection(Detection.VaccineEvaluationHepb1Only);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationHepb1Only);
            id.setImplementationDescription(
                    "Vaccination evaluation patient has only one Hep B");
        }
        this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsHepa2);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineEvaluationAt24MonthsHepa2);
            id.setImplementationDescription(
                    "Vaccination evaluation patient at 24 months of age for Help A series, has at least 2 doses");
        }
        this.addRuleDetection(Detection.VaccineForecastAt24MonthsHibComplete);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineForecastAt24MonthsHibComplete);
            id.setImplementationDescription(
                    "Vaccination evaluation patient at 24 months of age for Hib series is complete");
        }
        this.addRuleDetection(Detection.VaccineForecastAt24MonthsPcvComplete);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineForecastAt24MonthsPcvComplete);
            id.setImplementationDescription(
                    "Vaccination evaluation patient at 24 months of age for PVC series is complete");
        }
        this.addRuleDetection(Detection.VaccineForecastAt24MonthsRotaComplete);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineForecastAt24MonthsRotaComplete);
            id.setImplementationDescription(
                    "Vaccination evaluation patient at 24 months of age for Rotavirus series is complete");
        }
        this.addRuleDetection(Detection.VaccineForecastAt24MonthsHepbComplete);
        {
            ImplementationDetail id = this.addRuleDetection(Detection.VaccineForecastAt24MonthsHepbComplete);
            id.setImplementationDescription(
                    "Vaccination evaluation at 24 months of age for Hep B series complete.");
        }
    }

    private static Map<String, CvxCode> cvxToVaccineIdMap = null;

    public static Map<String, CvxCode> getCvxToVaccineIdMap() throws Exception {
        if (cvxToVaccineIdMap == null) {
            cvxToVaccineIdMap = CvxCodes.getCvxToCvxCodeMap();
        }
        return cvxToVaccineIdMap;
    }

    public static int MONTHS[] = {MONTHS_0, MONTHS_15, MONTHS_18, MONTHS_24};

    @Override
    protected ValidationRuleResult executeRule(MqePatient target, MqeMessageReceived m) {
        List<ValidationReport> issues = new ArrayList<ValidationReport>();
        boolean passed = true;

        DateTime birthDate = new DateTime(target.getBirthDate());

        Map<String, CvxCode> cvxToVaccineIdMap = null;
        try {
            cvxToVaccineIdMap = getCvxToVaccineIdMap();
        } catch (Exception e) {
            e.printStackTrace();
            return buildResults(issues, false);
        }
        Set<Detection> detectionSet = new HashSet<>();
        DateTime today = new DateTime();

        {

            GenericForecastInput forecastInput = createForecasterInput(target, m, cvxToVaccineIdMap, null);

            List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();


            try {
                CountEvalMapAndInvalidDoseCount result = executeForecasterGetCountEvalMapAndInvalidDoseCount(forecastInput, resultList, birthDate);

                if (result.invalidDoseCount >= 1) {
                    detectionSet.add(Detection.VaccineEvaluationHasInvalidDoses1orMore);
                    if (result.invalidDoseCount >= 2) {
                        detectionSet.add(Detection.VaccineEvaluationHasInvalidDoses2orMore);
                        if (result.invalidDoseCount >= 3) {
                            detectionSet.add(Detection.VaccineEvaluationHasInvalidDoses3orMore);
                            if (result.invalidDoseCount >= 4) {
                                detectionSet.add(Detection.VaccineEvaluationHasInvalidDoses4orMore);
                                if (result.invalidDoseCount >= 5) {
                                    detectionSet.add(Detection.VaccineEvaluationHasInvalidDoses5orMore);
                                    if (result.invalidDoseCount >= 10) {
                                        detectionSet.add(Detection.VaccineEvaluationHasInvalidDoses10orMore);
                                    }
                                }
                            }
                        }
                    }
                }

                for (int months = MONTHS.length - 1; months >= 0; months--) {
                    Integer month = MONTHS[months];
                    DateTime age = birthDate.plusMonths(month);
                    if (!age.isBefore(today)) {
                        break;
                    }

                    Map<String, Integer> countEvalMap = result.countEvalMapList.get(month);
                    if (month == MONTHS_0) {
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.HEPB), 0) == 1) {
                            detectionSet.add(Detection.VaccineEvaluationHepb1Only);
                        }
                    } else if (month == MONTHS_15) {
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.PNEUMOCOCCAL), 0) >= 4) {
                            detectionSet.add(Detection.VaccineEvaluationAt15MonthsPcv4);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.POLIO), 0) >= 3) {
                            detectionSet.add(Detection.VaccineEvaluationAt15MonthsPolio3);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.MEASLES), 0) >= 1) {
                            detectionSet.add(Detection.VaccineEvaluationAt15MonthsMmr1);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.VARICELLA), 0) >= 1) {
                            detectionSet.add(Detection.VaccineEvaluationAt15MonthsVar1);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.HIB), 0) >= 2) {
                            detectionSet.add(Detection.VaccineEvaluationAt15MonthsHib2);
                        }
                    } else if (month == MONTHS_18) {
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.HEPB), 0) >= 3) {
                            detectionSet.add(Detection.VaccineEvaluationAt18MonthsHepb3);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.DIPHTHERIA), 0) >= 4) {
                            detectionSet.add(Detection.VaccineEvaluationAt18MonthsDtap4);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.HEPA), 0) >= 2) {
                            detectionSet.add(Detection.VaccineEvaluationAt18MonthsHepa2);
                        }
                    } else if (month == MONTHS_24) {
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.DIPHTHERIA), 0) >= 4) {
                            detectionSet.add(Detection.VaccineEvaluationAt24MonthsDtap4);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.POLIO), 0) >= 3) {
                            detectionSet.add(Detection.VaccineEvaluationAt24MonthsPolio3);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.MEASLES), 0) >= 1) {
                            detectionSet.add(Detection.VaccineEvaluationAt24MonthsMmr1);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.HIB), 0) >= 3) {
                            detectionSet.add(Detection.VaccineEvaluationAt24MonthsHib3);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.HEPB), 0) >= 3) {
                            detectionSet.add(Detection.VaccineEvaluationAt24MonthsHepb3);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.HEPA), 0) >= 2) {
                            detectionSet.add(Detection.VaccineEvaluationAt24MonthsHepa2);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.VARICELLA), 0) >= 1) {
                            detectionSet.add(Detection.VaccineEvaluationAt24MonthsVar1);
                        }
                        if (countEvalMap.getOrDefault(getDiseaseLabel(Disease.PNEUMOCOCCAL), 0) >= 4) {
                            detectionSet.add(Detection.VaccineEvaluationAt24MonthsPcv4);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            for (Detection detection : detectionSet) {
                issues.add(detection.build(target));
            }

            if (detectionSet.contains(Detection.VaccineEvaluationAt24MonthsDtap4)
                    && detectionSet.contains(Detection.VaccineEvaluationAt24MonthsPolio3)
                    && detectionSet.contains(Detection.VaccineEvaluationAt24MonthsMmr1)
                    && detectionSet.contains(Detection.VaccineEvaluationAt24MonthsHib3)
                    && detectionSet.contains(Detection.VaccineEvaluationAt24MonthsHepb3)
                    && detectionSet.contains(Detection.VaccineEvaluationAt24MonthsVar1)
                    && detectionSet.contains(Detection.VaccineEvaluationAt24MonthsPcv4)) {
                issues.add(Detection.VaccineCoverageAt24MonthsSeries4_3_1_3_3_1_4.build(target));
            }

        }
        {

            DateTime age = birthDate.plusMonths(MONTHS_24);
            if (!today.isBefore(age)) {
                GenericForecastInput forecastInput =  createForecasterInput(target, m, cvxToVaccineIdMap, age);

                try {
                    List<ImmunizationForecastDataBean> resultList = getImmunizationForecastDataBeans(forecastInput);;
                    for (ImmunizationForecastDataBean forecast : resultList) {
                        fillIssues(target,forecast,issues);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return buildResults(issues, passed);
    }

    /**
     * Diseases counted by the evaluation. Each forecaster names them differently, so subclasses map
     * them to the label their engine uses.
     */
    public enum Disease {
        HEPB, HEPA, PNEUMOCOCCAL, POLIO, MEASLES, VARICELLA, HIB, DIPHTHERIA
    }

    /**
     * @return the label the underlying forecaster uses for the given disease, as found in the count
     * map built by {@link #executeForecasterGetCountEvalMapAndInvalidDoseCount}.
     */
    abstract String getDiseaseLabel(Disease disease);

    abstract void fillIssues(MqePatient target, ImmunizationForecastDataBean forecast, List<ValidationReport> issues);


    abstract @NonNull List<ImmunizationForecastDataBean> getImmunizationForecastDataBeans( GenericForecastInput forecastInput) throws Exception;


    abstract @NonNull GenericForecastInput createForecasterInput(MqePatient target, MqeMessageReceived m, Map<String, CvxCode> cvxToVaccineIdMap, DateTime age);

    abstract VaccineEvaluation.CountEvalMapAndInvalidDoseCount executeForecasterGetCountEvalMapAndInvalidDoseCount(GenericForecastInput forecastInput, List<ImmunizationForecastDataBean> resultList, DateTime birthDate) throws Exception;

    public static class CountEvalMapAndInvalidDoseCount {
        public final int invalidDoseCount;
        public final HashMap<Integer, Map<String, Integer>> countEvalMapList;

        public CountEvalMapAndInvalidDoseCount(int invalidDoseCount, HashMap<Integer, Map<String, Integer>> countEvalMapList) {
            this.invalidDoseCount = invalidDoseCount;
            this.countEvalMapList = countEvalMapList;
        }
    }

    static public void printAllFields(Object obj) {
        if (obj == null) {
            System.out.println("null");
            return;
        }
        System.out.println("=== " + obj.getClass().getName() + " ===");
        for (java.lang.reflect.Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            Object val = null;
            try {
                val = f.get(obj);
            } catch (Exception e) {
                val = "<error: " + e + ">";
            }
            System.out.println("  " + f.getName() + " = " + val);
        }
    }

    static public void printSummary(String label, int allInputSize, int validInputSize, int invalidInputSize) {
        System.out.println("========== " + label + " SUMMARY =============================================");
        System.out.println("Size of all inputs " + allInputSize);
        System.out.println("Marked Valid " + validInputSize);
        System.out.println("invalidDoseCount final value " + invalidInputSize);
        System.out.println("========================================================================================");

    }

}
