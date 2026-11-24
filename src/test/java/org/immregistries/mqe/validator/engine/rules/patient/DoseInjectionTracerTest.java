package org.immregistries.mqe.validator.engine.rules.patient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.model.Immunization;
import org.junit.Test;

public class DoseInjectionTracerTest {

  private static Date d(String s) {
    // parse "yyyy-MM-dd" loosely via Date constructor offsets is unreliable; use a fixed epoch base instead.
    return new Date(0);
  }

  private Immunization inj(String cvx, Date date) {
    Immunization i = new Immunization();
    i.setCvx(cvx);
    i.setDateOfShot(date);
    return i;
  }

  private VaccinationDoseDataBean dose(String cvx, Date date) {
    VaccinationDoseDataBean v = new VaccinationDoseDataBean();
    v.setCvxCode(cvx);
    v.setAdminDate(date);
    return v;
  }

  @Test
  public void singleAntigenTraces() {
    Date when = new Date(1_000_000L);
    List<ImmunizationInterface> injections = new ArrayList<>();
    injections.add(inj("08", when));
    VaccinationDoseDataBean dd = dose("8", when); // leading-zero normalization
    assertTrue(DoseInjectionTracer.isTraced(dd, injections));
    assertEquals(1, DoseInjectionTracer.trace(dd, injections).size());
  }

  @Test
  public void combinationMmrvFansOutToFourDoses() {
    Date when = new Date(2_000_000L);
    List<ImmunizationInterface> injections = new ArrayList<>();
    injections.add(inj("94", when)); // one MMRV injection
    // Four antigen-specific doses, all sharing the same (date, cvx).
    String[] antigens = {"Rubella", "Measles", "Mumps", "Varicella"};
    int traced = 0;
    for (String a : antigens) {
      VaccinationDoseDataBean dd = dose("94", when);
      if (DoseInjectionTracer.isTraced(dd, injections)) traced++;
    }
    assertEquals("all four combination doses must trace to the single injection",
        antigens.length, traced);
  }

  @Test
  public void missedDoseNotTraced() {
    Date when = new Date(3_000_000L);
    List<ImmunizationInterface> injections = new ArrayList<>();
    injections.add(inj("10", when));
    // A dose on a different date is not traced.
    VaccinationDoseDataBean dd = dose("10", new Date(4_000_000L));
    assertFalse(DoseInjectionTracer.isTraced(dd, injections));
    assertEquals(0, DoseInjectionTracer.trace(dd, injections).size());
  }

  @Test
  public void removeTracedDosesInPlace() {
    Date when = new Date(5_000_000L);
    List<ImmunizationInterface> injections = new ArrayList<>();
    injections.add(inj("20", when));
    List<VaccinationDoseDataBean> doses = new ArrayList<>();
    doses.add(dose("20", when)); // traced -> removed
    doses.add(dose("20", new Date(6_000_000L))); // untraced -> kept
    int removed = DoseInjectionTracer.removeTracedDoses(doses, injections);
    assertEquals(1, removed);
    assertEquals(1, doses.size());
  }
}
