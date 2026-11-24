package org.immregistries.mqe.validator.engine.rules.patient;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;

public final class DoseInjectionTracer {

  private DoseInjectionTracer() {}

  public static List<ImmunizationInterface> listAllNonTraced(List<VaccinationDoseDataBean> doseList,
                                                  List<ImmunizationInterface> injections) {
    for (VaccinationDoseDataBean dose : doseList) {
      injections.removeAll(trace(dose,injections));
    }
    return injections;
  }

  public static List<ImmunizationInterface> trace(VaccinationDoseDataBean dose,
      List<ImmunizationInterface> injections) {
    List<ImmunizationInterface> matches = new ArrayList<>();
    if (dose == null || injections == null) return matches;
    Date doseDate = dose.getAdminDate();
    String doseCvx = normalize(dose.getCvxCode());
    for (ImmunizationInterface inj : injections) {
      if (inj == null) continue;
      if (!sameDate(doseDate, inj.getDateOfShot())) continue;
      if (!doseCvx.equals(normalize(inj.getCvx()))) continue;
      matches.add(inj);
    }
    return matches;
  }

  public static boolean isTraced(VaccinationDoseDataBean dose, List<ImmunizationInterface> injections) {
    return !trace(dose, injections).isEmpty();
  }

  public static int removeTracedInputs(
      List<org.openimmunizationsoftware.cdsi.core.data.ForecastInput.VaccinationInput> vaccinationList,
      List<ImmunizationInterface> injections) {
    if (vaccinationList == null || injections == null) return 0;
    int removed = 0;
    Iterator<org.openimmunizationsoftware.cdsi.core.data.ForecastInput.VaccinationInput> it =
        vaccinationList.iterator();
    while (it.hasNext()) {
      org.openimmunizationsoftware.cdsi.core.data.ForecastInput.VaccinationInput vi = it.next();
      if (vi == null) continue;
      String cvx = normalize(vi.getVaccineCvx());
      Date date = vi.getDateAdministered();
      boolean traced = false;
      for (ImmunizationInterface inj : injections) {
        if (inj == null) continue;
        if (!cvx.equals(normalize(inj.getCvx()))) continue;
        if (!sameDate(date, inj.getDateOfShot())) continue;
        traced = true;
        break;
      }
      if (traced) {
        it.remove();
        removed++;
      }
    }
    return removed;
  }

  public static int removeTracedDoses(List<VaccinationDoseDataBean> doses,
      List<ImmunizationInterface> injections) {
    if (doses == null || injections == null) return 0;
    int removed = 0;
    Iterator<VaccinationDoseDataBean> it = doses.iterator();
    while (it.hasNext()) {
      VaccinationDoseDataBean d = it.next();
      if (d == null) continue;
      if (!trace(d, injections).isEmpty()) {
        it.remove();
        removed++;
      }
    }
    return removed;
  }

  private static String normalize(String cvx) {
    if (cvx == null) return "";
    String t = cvx.trim();
    int i = 0;
    while (i < t.length() - 1 && t.charAt(i) == '0') i++;
    return t.substring(i);
  }

  private static boolean sameDate(Date a, Date b) {
    if (a == null || b == null) return false;
    return a.getTime() == b.getTime() || a.equals(b);
  }
}
