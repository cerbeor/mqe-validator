package org.immregistries.mqe.validator.engine.rules.patient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.immregistries.mqe.validator.detection.Detection;
import org.immregistries.mqe.validator.detection.ValidationReport;
import org.immregistries.mqe.validator.engine.ValidationRule;
import org.immregistries.mqe.validator.engine.rules.ValidationRuleEntityLists;
import org.immregistries.mqe.vxu.MqeMessageReceived;
import org.joda.time.DateTime;
import org.junit.Test;

/**
 * VaccineEvaluation tests built on a fixed, midnight birth date (see {@link VaccinationHistoryFixture})
 * instead of "now", so results do not depend on the day or time the tests run.
 */
public class VaccineEvaluationFixedBirthDateTest {

  /** Age-based detections both engines are expected to agree on for the full schedule. */
  private static final Set<Detection> FULL_SCHEDULE_AGE_DETECTIONS = EnumSet.of(
      Detection.VaccineEvaluationAt15MonthsPcv4,
      Detection.VaccineEvaluationAt15MonthsPolio3,
      Detection.VaccineEvaluationAt15MonthsMmr1,
      Detection.VaccineEvaluationAt15MonthsVar1,
      Detection.VaccineEvaluationAt15MonthsHib2,
      Detection.VaccineEvaluationAt18MonthsHepb3,
      Detection.VaccineEvaluationAt18MonthsDtap4,
      Detection.VaccineEvaluationAt18MonthsHepa2,
      Detection.VaccineEvaluationAt24MonthsDtap4,
      Detection.VaccineEvaluationAt24MonthsPolio3,
      Detection.VaccineEvaluationAt24MonthsMmr1,
      Detection.VaccineEvaluationAt24MonthsHib3,
      Detection.VaccineEvaluationAt24MonthsHepb3,
      Detection.VaccineEvaluationAt24MonthsHepa2,
      Detection.VaccineEvaluationAt24MonthsVar1,
      Detection.VaccineEvaluationAt24MonthsPcv4,
      Detection.VaccineCoverageAt24MonthsSeries4_3_1_3_3_1_4,
      Detection.VaccineForecastAt24MonthsHibComplete,
      Detection.VaccineForecastAt24MonthsPcvComplete,
      Detection.VaccineForecastAt24MonthsRotaComplete,
      Detection.VaccineForecastAt24MonthsHepbComplete);

  private static List<VaccineEvaluation<?, ?, ?>> allEngines() {
    return Arrays.asList(new VaccineEvaluationStepIntoCDSi(), new VaccineEvaluationLegacy(),
        new VaccineEvaluationLonestar());
  }

  private static Set<Detection> detect(VaccineEvaluation<?, ?, ?> rule, MqeMessageReceived m) {
    return rule.executeRule(m.getPatient(), m).getValidationDetections().stream()
        .map(vr -> (Detection) vr.getDetection())
        .collect(Collectors.toSet());
  }

  private static String name(VaccineEvaluation<?, ?, ?> rule) {
    return rule.getClass().getSimpleName();
  }

  @Test
  public void stepIntoCdsiIsTheOnlyActiveVaccineEvaluation() {
    List<Class<?>> active = ValidationRuleEntityLists.PATIENT_RULES.getRules().stream()
        .map(ValidationRule::getClass)
        .filter(VaccineEvaluation.class::isAssignableFrom)
        .collect(Collectors.toList());
    assertEquals(Arrays.<Class<?>>asList(VaccineEvaluationStepIntoCDSi.class), active);
  }

  @Test
  public void fullScheduleRaisesAllAgeDetections() {
    MqeMessageReceived m = VaccinationHistoryFixture.bornOnDefaultDate().fullSchedule().build();
    for (VaccineEvaluation<?, ?, ?> rule : allEngines()) {
      Set<Detection> found = detect(rule, m);
      for (Detection expected : FULL_SCHEDULE_AGE_DETECTIONS) {
        assertTrue(name(rule) + " should raise " + expected, found.contains(expected));
      }
      assertFalse(name(rule), found.contains(Detection.VaccineEvaluationHepb1Only));
    }
  }

  /** Each engine labels pneumococcal differently ("Pneumococcal" vs "Pneumo"). */
  @Test
  public void pcvIsCountedWithEachEnginesOwnLabel() {
    MqeMessageReceived m = VaccinationHistoryFixture.bornOnDefaultDate()
        .at(2, "133").at(4, "133").at(6, "133").at(12, "133")
        .build();
    for (VaccineEvaluation<?, ?, ?> rule : allEngines()) {
      Set<Detection> found = detect(rule, m);
      assertTrue(name(rule), found.contains(Detection.VaccineEvaluationAt15MonthsPcv4));
      assertTrue(name(rule), found.contains(Detection.VaccineEvaluationAt24MonthsPcv4));
    }
  }

  /** Doses given after 24 months must not make a series complete at 24 months. */
  @Test
  public void dosesAfter24MonthsAreIgnoredByThe24MonthForecast() {
    MqeMessageReceived m = VaccinationHistoryFixture.bornOnDefaultDate()
        .at(26, "08").at(28, "08").at(34, "08")
        .build();
    for (VaccineEvaluation<?, ?, ?> rule : allEngines()) {
      assertFalse(name(rule),
          detect(rule, m).contains(Detection.VaccineForecastAt24MonthsHepbComplete));
    }
  }

  @Test
  public void assessmentDateIsSetWithoutVaccinations() throws Exception {
    VaccineEvaluationStepIntoCDSi rule = new VaccineEvaluationStepIntoCDSi();
    MqeMessageReceived m = VaccinationHistoryFixture.bornOnDefaultDate().build();
    DateTime at24Months =
        new DateTime(m.getPatient().getBirthDate()).plusMonths(VaccineEvaluation.MONTHS_24);

    assertNotNull(rule.createForecasterInput(m.getPatient(), m,
        VaccineEvaluation.getCvxToVaccineIdMap(), null).getAssessmentDate());
    assertEquals(at24Months.toDate(), rule.createForecasterInput(m.getPatient(), m,
        VaccineEvaluation.getCvxToVaccineIdMap(), at24Months).getAssessmentDate());
    assertTrue(detect(rule, m).isEmpty());
  }

  /** Moving the whole history (birth + doses) by whole days must not change the outcome. */
  @Test
  public void detectionsAreStableWhenShiftingTheWholeHistory() {
    for (VaccineEvaluation<?, ?, ?> rule : allEngines()) {
      Set<Detection> baseline =
          detect(rule, VaccinationHistoryFixture.bornOnDefaultDate().fullSchedule().build());
      for (int days : new int[] {-1, 1, 15}) {
        MqeMessageReceived shifted = VaccinationHistoryFixture.bornOnDefaultDate()
            .shiftedByDays(days).fullSchedule().build();
        assertEquals(name(rule) + " shifted by " + days + " days", baseline, detect(rule, shifted));
      }
    }
  }
}
