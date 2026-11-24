package org.immregistries.mqe.validator.engine.rules.patient;

import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.immregistries.mqe.validator.engine.ValidationRuleResult;
import org.immregistries.mqe.vxu.MqeMessageReceived;
import org.immregistries.mqe.vxu.MqePatient;
import org.immregistries.mqe.vxu.MqeVaccination;
import org.junit.Test;

/**
 * Checks whether CVX codes trigger exceptions when fed as input to {@link VaccineEvaluation}.
 *
 * CVX codes known to make the underlying CDSI engine throw
 * "Unrecognized cvx code" (caught internally by VaccineEvaluation) are kept in a separate
 * {@link #PROBLEMATIC_CVX_LIST}; the rest are in {@link #OK_CVX_LIST}.
 */
public class CvxExceptionProbeTest {

  // CVX codes that trigger an internal "Unrecognized cvx code" exception in the CDSI engine.
  private static final String[] PROBLEMATIC_CVX_LIST = {
      "66", "69", "78", "86", "128", "157"
  };

  // Common CVX codes that are handled without an internal exception.
  private static final String[] OK_CVX_LIST = {
      "03", "08", "10", "17", "20", "21", "33", "83", "88", "94", "101", "110", "115",
      "121", "122", "133", "146", "150", "158", "185", "189", "197", "207", "208", "210", "213",
      "219"
  };

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

  /**
   * Problematic CVX codes (those that make the underlying CDSI engine raise
   * "Unrecognized cvx code") must not propagate an exception out of VaccineEvaluation;
   * the rule is expected to complete and return a non-null result.
   */
  @Test
  public void problematicCvxDoesNotThrow() {
    for (String cvx : PROBLEMATIC_CVX_LIST) {
      MqeMessageReceived mr = buildMessage(cvx);
      VaccineEvaluation rule = new VaccineEvaluation();
      try {
        ValidationRuleResult r = rule.executeRule(mr.getPatient(), mr);
        assertNotNull("rule result should not be null for CVX " + cvx, r);
      } catch (Throwable t) {
        throw new AssertionError("CVX " + cvx + " triggered an exception", t);
      }
    }
  }

  /**
   * Common CVX codes that the CDSI engine recognizes must not propagate an exception.
   */
  @Test
  public void okCvxDoesNotThrow() {
    for (String cvx : OK_CVX_LIST) {
      MqeMessageReceived mr = buildMessage(cvx);
      VaccineEvaluation rule = new VaccineEvaluation();
      try {
        ValidationRuleResult r = rule.executeRule(mr.getPatient(), mr);
        assertNotNull("rule result should not be null for CVX " + cvx, r);
      } catch (Throwable t) {
        throw new AssertionError("CVX " + cvx + " triggered an exception", t);
      }
    }
  }

  /**
   * When all problematic CVX codes are submitted together, VaccineEvaluation must still
   * complete without propagating an exception.
   */
  @Test
  public void combinedProblematicCvxListDoesNotThrow() {
    MqeMessageReceived mr = new MqeMessageReceived();
    MqePatient p = new MqePatient();
    mr.setPatient(p);
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -2);
    p.setBirthDate(cal.getTime());
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    List<MqeVaccination> vs = new ArrayList<>();
    for (String cvx : PROBLEMATIC_CVX_LIST) {
      MqeVaccination v = new MqeVaccination();
      v.setAdminDateString(df.format(cal.getTime()));
      v.setAdminCvxCode(cvx);
      vs.add(v);
    }
    mr.setVaccinations(vs);
    VaccineEvaluation rule = new VaccineEvaluation();
    try {
      ValidationRuleResult r = rule.executeRule(p, mr);
      assertNotNull(r);
    } catch (Throwable t) {
      throw new AssertionError("Combined problematic CVX list triggered an exception", t);
    }
  }
}
