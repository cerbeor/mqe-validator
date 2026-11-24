package org.immregistries.mqe.validator.engine.rules.patient;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.immregistries.mqe.validator.engine.ValidationRuleResult;
import org.immregistries.mqe.vxu.MqeMessageReceived;
import org.immregistries.mqe.vxu.MqePatient;
import org.immregistries.mqe.vxu.MqeVaccination;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compares the behavior of {@link VaccineEvaluation} (new, STEP-based) against
 * {@link VaccineEvaluationLonestar} (legacy, lonestar-based) when fed CVX codes that are known to
 * trigger internal "Unrecognized cvx code" exceptions in the new path.
 */
public class ProblematicCvxComparisonTest {

  private static final Logger logger =
      LoggerFactory.getLogger(ProblematicCvxComparisonTest.class);

  private static final String NOT_A_CVX = "not-a-cvx";

  private static final String[] PROBLEMATIC_CVX_LIST = {
      "66", "69", "78", "86", "128", "157"
  };

  /**
   * For each problematic CVX code, runs both the new (STEP-based) and legacy (lonestar-based)
   * VaccineEvaluation implementations and logs their resulting detections. Verifies that neither
   * path propagates an exception, documenting any behavioral divergence between the two.
   */
  @Test
  public void problematicCvxComparison() {
    for (String cvx : PROBLEMATIC_CVX_LIST) {
      MqeMessageReceived mrNew = buildMessage(cvx);
      MqeMessageReceived mrLegacy = buildMessage(cvx);

      VaccineEvaluation newRule = new VaccineEvaluation();
      VaccineEvaluationLonestar legacyRule = new VaccineEvaluationLonestar();

      ValidationRuleResult rNew = null;
      ValidationRuleResult rLegacy = null;
      Throwable tNew = null;
      Throwable tLegacy = null;

      // Run the new STEP-based implementation, capturing any thrown exception.
      try {
        rNew = newRule.executeRule(mrNew.getPatient(), mrNew);
      } catch (Throwable t) {
        tNew = t;
      }
      // Run the legacy lonestar-based implementation, capturing any thrown exception.
      try {
        rLegacy = legacyRule.executeRule(mrLegacy.getPatient(), mrLegacy);
      } catch (Throwable t) {
        tLegacy = t;
      }

      logger.info("CVX " + cvx + " NEW detections:    " + (rNew == null ? tNew : rNew.getValidationDetections()));
      logger.info("CVX " + cvx + " LEGACY detections: " + (rLegacy == null ? tLegacy : rLegacy.getValidationDetections()));

      // Both paths should complete without propagating an exception.
      org.junit.Assert.assertNull("new path threw for CVX " + cvx, tNew);
      org.junit.Assert.assertNull("legacy path threw for CVX " + cvx, tLegacy);
    }
  }

  /**
   * Verifies that the new and legacy implementations agree on whether an exception is thrown
   * for each problematic CVX code: if one throws, the other must throw too (and vice versa).
   */
  @Test
  public void problematicCvxThrowParity() {
    for (String cvx : PROBLEMATIC_CVX_LIST) {
      MqeMessageReceived mrNew = buildMessage(cvx);
      MqeMessageReceived mrLegacy = buildMessage(cvx);

      VaccineEvaluation newRule = new VaccineEvaluation();
      VaccineEvaluationLonestar legacyRule = new VaccineEvaluationLonestar();

      boolean newThrew = false;
      boolean legacyThrew = false;
      // Track whether the new implementation throws for this CVX.
      try {
        newRule.executeRule(mrNew.getPatient(), mrNew);
      } catch (Throwable t) {
        newThrew = true;
      }
      // Track whether the legacy implementation throws for this CVX.
      try {
        legacyRule.executeRule(mrLegacy.getPatient(), mrLegacy);
      } catch (Throwable t) {
        legacyThrew = true;
      }

      logger.info("CVX " + cvx + " newThrew=" + newThrew + " legacyThrew=" + legacyThrew);
      assertEquals("throw behavior should match for CVX " + cvx, newThrew, legacyThrew);
    }
  }

  /**
   * Ensures that a non-CVX code ("not-a-cvx") is rejected by both the new (STEP-based) and
   * legacy (lonestar-based) VaccineEvaluation implementations: it must not be fed into the
   * forecast, so neither path should report invalid-dose detections for it.
   */
  @Test
  public void notACvxIsRejectedByBoth() {
    MqeMessageReceived mrNew = buildMessage(NOT_A_CVX);
    MqeMessageReceived mrLegacy = buildMessage(NOT_A_CVX);

    VaccineEvaluation newRule = new VaccineEvaluation();
    VaccineEvaluationLonestar legacyRule = new VaccineEvaluationLonestar();

    ValidationRuleResult rNew = newRule.executeRule(mrNew.getPatient(), mrNew);
    ValidationRuleResult rLegacy = legacyRule.executeRule(mrLegacy.getPatient(), mrLegacy);

    logger.info("NOT_A_CVX NEW detections:    " + rNew.getValidationDetections());
    logger.info("NOT_A_CVX LEGACY detections: " + rLegacy.getValidationDetections());

    org.junit.Assert.assertNotNull("new path should return a result for not-a-cvx", rNew);
    org.junit.Assert.assertNotNull("legacy path should return a result for not-a-cvx", rLegacy);
//    Dose should be counted as invalid ? if so both should return a detection on having a non existing CVX
    org.junit.Assert.assertEquals(1, rNew.getValidationDetections().size());
    org.junit.Assert.assertEquals(1, rLegacy.getValidationDetections().size());
    org.junit.Assert.assertEquals("passed flag should match", rLegacy.isRulePassed(), rNew.isRulePassed());
  }

  private MqeMessageReceived buildMessage(String cvx) {
    MqeMessageReceived mr = new MqeMessageReceived();
    MqePatient p = new MqePatient();
    mr.setPatient(p);
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -2);
    p.setBirthDate(cal.getTime());
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    List<MqeVaccination> vs = new ArrayList<>();
    MqeVaccination v = new MqeVaccination();
    v.setAdminDateString(df.format(cal.getTime()));
    v.setAdminCvxCode(cvx);
    vs.add(v);
    mr.setVaccinations(vs);
    return mr;
  }
}
