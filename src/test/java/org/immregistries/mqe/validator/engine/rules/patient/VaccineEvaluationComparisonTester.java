package org.immregistries.mqe.validator.engine.rules.patient;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.immregistries.mqe.validator.detection.ValidationReport;
import org.immregistries.mqe.validator.engine.ValidationRuleResult;
import org.immregistries.mqe.vxu.MqeMessageReceived;
import org.immregistries.mqe.vxu.MqePatient;
import org.immregistries.mqe.vxu.MqeVaccination;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compares the detection output of {@link VaccineEvaluation} (new, StepIntoCDSI-based path) against
 * {@link VaccineEvaluationLonestar} (original lonestar-based path) for the same input, to ensure the
 * refactor did not change behavior.
 */
public class VaccineEvaluationComparisonTester {

  private static final Logger logger =
      LoggerFactory.getLogger(VaccineEvaluationComparisonTester.class);

  private VaccineEvaluation stepRule = new VaccineEvaluation();
  private VaccineEvaluationLonestar newLonestarRule = new VaccineEvaluationLonestar();
  private VaccineEvaluationLegacy legacyRule = new VaccineEvaluationLegacy();

  private MqeMessageReceived mr = new MqeMessageReceived();
  private MqePatient p = new MqePatient();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

  @Before
  public void setUpTheObjects() {
    mr.setPatient(p);
  }

  @Test
  public void positiveScenarioMatches() {
    List<MqeVaccination> vaccinations = createPatientVaccinations(true);
    mr.setVaccinations(vaccinations);

    ValidationRuleResult rStep = stepRule.executeRule(p, mr);
    ValidationRuleResult rLonestar = newLonestarRule.executeRule(p, mr);
    ValidationRuleResult rLegacy = legacyRule.executeRule(p, mr);

    logger.info("NEW STEPINTOCDSI:    " + rStep.getValidationDetections());
    logger.info("LEGACY LONESTAR: " + rLegacy.getValidationDetections());
    logger.info("NEW LONESTAR: " + rLonestar.getValidationDetections());

    assertEquals("passed flag should match", rLonestar.isRulePassed(), rStep.isRulePassed());
    assertEquals("detection sets should match", detectionKeySet(rLonestar), detectionKeySet(rStep));
  }

  @Test
  public void randomSubsetScenarioMatches() {
    mr = new MqeMessageReceived();
    p = new MqePatient();
    mr.setPatient(p);

    List<MqeVaccination> all = createPatientVaccinations(true);
    List<MqeVaccination> subset = new ArrayList<>();
    java.util.Random rnd = new java.util.Random(42);
    for (MqeVaccination v : all) {
      if (rnd.nextBoolean()) {
        subset.add(v);
      }
    }
    mr.setVaccinations(subset);

    ValidationRuleResult rNew = stepRule.executeRule(p, mr);
    ValidationRuleResult rLonestar = newLonestarRule.executeRule(p, mr);
    ValidationRuleResult rLegacy = legacyRule.executeRule(p, mr);

    logger.info("RANDOM SUBSET NEW STEP:    " + rNew.getValidationDetections());
    logger.info("RANDOM SUBSET NEW LONESTAR: " + rLonestar.getValidationDetections());
    logger.info("RANDOM SUBSET LEGACY:    " + rLegacy.getValidationDetections());

    assertEquals("passed flag should match", rLonestar.isRulePassed(), rNew.isRulePassed());
    assertEquals("detection sets should match", detectionKeySet(rLonestar), detectionKeySet(rNew));
  }

  @Test
  public void everyOtherDoseScenarioMatches() {
    mr = new MqeMessageReceived();
    p = new MqePatient();
    mr.setPatient(p);

    List<MqeVaccination> all = createPatientVaccinations(true);
    List<MqeVaccination> everyOther = new ArrayList<>();
    for (int i = 0; i < all.size(); i += 2) {
      everyOther.add(all.get(i));
    }
    mr.setVaccinations(everyOther);

    ValidationRuleResult rNew = stepRule.executeRule(p, mr);
    ValidationRuleResult rLonestar = newLonestarRule.executeRule(p, mr);
    ValidationRuleResult rLegacy = legacyRule.executeRule(p, mr);

    logger.info("EVERY OTHER NEW STEP:    " + rNew.getValidationDetections());
    logger.info("EVERY OTHER NEW LONESTAR: " + rLonestar.getValidationDetections());
    logger.info("EVERY OTHER LEGACY: " + rLegacy.getValidationDetections());

    assertEquals("passed flag should match", rLonestar.isRulePassed(), rNew.isRulePassed());
    assertEquals("detection sets should match", detectionKeySet(rLonestar), detectionKeySet(rNew));
  }

  @Test
  public void negativeScenarioMatches() {
    // reset patient/message between scenarios
    mr = new MqeMessageReceived();
    p = new MqePatient();
    mr.setPatient(p);

    List<MqeVaccination> vaccinations = createPatientVaccinations(false);
    mr.setVaccinations(vaccinations);

    ValidationRuleResult rStep = stepRule.executeRule(p, mr);
    ValidationRuleResult rLonestar = newLonestarRule.executeRule(p, mr);
    ValidationRuleResult rLegacy = legacyRule.executeRule(p, mr);

    logger.info("NEW STEPINTOCDSI:    " + rStep.getValidationDetections());
    logger.info("LEGACY LONESTAR: " + rLegacy.getValidationDetections());
    logger.info("NEW LONESTAR: " + rLonestar.getValidationDetections());

    assertEquals("passed flag should match", rLonestar.isRulePassed(), rStep.isRulePassed());
    assertEquals("detection sets should match", detectionKeySet(rLonestar), detectionKeySet(rStep));
  }

  @Test
  public void allInvalidScenarioMatches() {
    // reset patient/message between scenarios
    mr = new MqeMessageReceived();
    p = new MqePatient();
    mr.setPatient(p);

    List<MqeVaccination> vaccinations = createWrongPatientVaccinations();
    mr.setVaccinations(vaccinations);

    ValidationRuleResult rStep = stepRule.executeRule(p, mr);
    ValidationRuleResult rLonestar = newLonestarRule.executeRule(p, mr);
    ValidationRuleResult rLegacy = legacyRule.executeRule(p, mr);

    logger.info("NEW STEPINTOCDSI:    " + rStep.getValidationDetections());
    logger.info("LEGACY LONESTAR: " + rLegacy.getValidationDetections());
    logger.info("NEW LONESTAR: " + rLonestar.getValidationDetections());

    assertEquals("passed flag should match", rLonestar.isRulePassed(), rStep.isRulePassed());
    assertEquals("detection sets should match", detectionKeySet(rLonestar), detectionKeySet(rStep));
  }

  private Set<String> detectionKeySet(ValidationRuleResult r) {
    Set<String> keys = new HashSet<>();
    for (ValidationReport vr : r.getValidationDetections()) {
      keys.add(String.valueOf(vr.getDetection()));
    }
    return keys;
  }

  // ---- reused data builders (mirrored from VaccineEvaluationTester) ----

  private List<MqeVaccination> createWrongPatientVaccinations() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -10);

    p.setBirthDate(cal.getTime());
    List<MqeVaccination> vaccinations = new ArrayList<>();


    cal.add(Calendar.MONTH, 2);
    addVaccination("0157", vaccinations, cal);
    cal.add(Calendar.MONTH, 2);
    addVaccination("notAcvx", vaccinations, cal);
    cal.add(Calendar.MONTH, 2);
    addVaccination("066", vaccinations, cal);
    return vaccinations;
  }

  private List<MqeVaccination> createPatientVaccinations(boolean allVaccines) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -10);

    // birth
    p.setBirthDate(cal.getTime());
    List<MqeVaccination> vaccinations = new ArrayList<>();
    if (allVaccines) {
      addVaccination("08", vaccinations, cal); // Hep B at Birth
    }

    // 2 months
    cal.add(Calendar.MONTH, 2);
    if (allVaccines) {
      addVaccination("122", vaccinations, cal); // Rotavirus
      addVaccination("20", vaccinations, cal); // DTaP
      addVaccination("10", vaccinations, cal); // IPV
      addVaccination("17", vaccinations, cal); // Hib
      addVaccination("08", vaccinations, cal); // Hep B
      addVaccination("133", vaccinations, cal); // PCV13
    }

    // 4 months
    cal.add(Calendar.MONTH, 2);
    if (allVaccines) {
      addVaccination("122", vaccinations, cal); // Rotavirus
      addVaccination("20", vaccinations, cal); // DTaP
      addVaccination("10", vaccinations, cal); // IPV
      addVaccination("17", vaccinations, cal); // Hib
      addVaccination("08", vaccinations, cal); // Hep B
      addVaccination("133", vaccinations, cal); // PCV13
    }

    // 6 months
    cal.add(Calendar.MONTH, 2);
    if (allVaccines) {
      addVaccination("122", vaccinations, cal); // Rotavirus
      addVaccination("20", vaccinations, cal); // DTaP
      addVaccination("10", vaccinations, cal); // IPV
      addVaccination("17", vaccinations, cal); // Hib
      addVaccination("08", vaccinations, cal); // Hep B
      addVaccination("133", vaccinations, cal); // PCV13
    }

    // 12 months
    cal.add(Calendar.MONTH, 6);
    addVaccination("133", vaccinations, cal); // PCV13
    addVaccination("94", vaccinations, cal); // MMRV
    addVaccination("83", vaccinations, cal); // Hep A

    // 15 months
    cal.add(Calendar.MONTH, 3);
    addVaccination("20", vaccinations, cal); // DTaP
    addVaccination("10", vaccinations, cal); // IPV
    addVaccination("17", vaccinations, cal); // Hib
    addVaccination("08", vaccinations, cal); // Hep B

    // 18 months
    cal.add(Calendar.MONTH, 3);
    addVaccination("83", vaccinations, cal); // Hep A

    // 23 months
    cal.add(Calendar.MONTH, 5);
    addVaccination("20", vaccinations, cal); // DTaP
    addVaccination("10", vaccinations, cal); // IPV

    // 47 months
    cal.add(Calendar.MONTH, 24);
    addVaccination("20", vaccinations, cal); // DTaP
    return vaccinations;
  }

  private void addVaccination(String cvxCode, List<MqeVaccination> vaccinations, Calendar cal) {
    MqeVaccination tmp = new MqeVaccination();
    tmp.setAdminDateString(dateFormat.format(cal.getTime()));
    tmp.setAdminCvxCode(cvxCode);
    vaccinations.add(tmp);
  }

}