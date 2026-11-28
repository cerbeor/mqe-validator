package org.immregistries.mqe.validator.engine.rules.patient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.immregistries.mqe.vxu.MqeMessageReceived;
import org.immregistries.mqe.vxu.MqePatient;
import org.immregistries.mqe.vxu.MqeVaccination;

/**
 * Builds a patient + vaccination history relative to a fixed birth date (midnight), so tests do not
 * depend on the current date or time of day.
 *
 * <pre>
 * MqeMessageReceived m = VaccinationHistoryFixture.bornOn(2015, 1, 1)
 *     .at(2, "08", "20")   // HepB + DTaP at 2 months
 *     .build();
 * </pre>
 */
public class VaccinationHistoryFixture {

  public static final int DEFAULT_YEAR = 2015;
  public static final int DEFAULT_MONTH = 1;
  public static final int DEFAULT_DAY = 1;

  private final Calendar birth;
  private int dayShift = 0;
  private final List<String[]> doses = new ArrayList<>(); // {monthsAfterBirth, cvx}

  private VaccinationHistoryFixture(Calendar birth) {
    this.birth = birth;
  }

  /** @param month 1-based month */
  public static VaccinationHistoryFixture bornOn(int year, int month, int day) {
    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(year, month - 1, day);
    return new VaccinationHistoryFixture(c);
  }

  public static VaccinationHistoryFixture bornOnDefaultDate() {
    return bornOn(DEFAULT_YEAR, DEFAULT_MONTH, DEFAULT_DAY);
  }

  /** Adds the given CVX codes administered {@code monthsAfterBirth} months after birth. */
  public VaccinationHistoryFixture at(int monthsAfterBirth, String... cvxCodes) {
    for (String cvx : cvxCodes) {
      doses.add(new String[] {String.valueOf(monthsAfterBirth), cvx});
    }
    return this;
  }

  /** Shifts the birth date and every dose by the same number of whole days. */
  public VaccinationHistoryFixture shiftedByDays(int days) {
    this.dayShift = days;
    return this;
  }

  /** The routine schedule used across the VaccineEvaluation tests. */
  public VaccinationHistoryFixture fullSchedule() {
    return at(0, "08")                                   // Hep B at birth
        .at(2, "122", "20", "10", "17", "08", "133")     // Rota, DTaP, IPV, Hib, HepB, PCV13
        .at(4, "122", "20", "10", "17", "08", "133")
        .at(6, "122", "20", "10", "17", "08", "133")
        .at(12, "133", "94", "83")                       // PCV13, MMRV, HepA
        .at(15, "20", "10", "17", "08")                  // DTaP, IPV, Hib, HepB
        .at(18, "83")                                    // HepA
        .at(23, "20", "10")                              // DTaP, IPV
        .at(47, "20");                                   // DTaP
  }

  public Date birthDate() {
    return plus(0);
  }

  public MqeMessageReceived build() {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    MqeMessageReceived m = new MqeMessageReceived();
    MqePatient p = new MqePatient();
    p.setBirthDate(birthDate());
    m.setPatient(p);
    List<MqeVaccination> vaccinations = new ArrayList<>();
    for (String[] dose : doses) {
      MqeVaccination v = new MqeVaccination();
      v.setAdminCvxCode(dose[1]);
      v.setAdminDateString(df.format(plus(Integer.parseInt(dose[0]))));
      vaccinations.add(v);
    }
    m.setVaccinations(vaccinations);
    return m;
  }

  private Date plus(int months) {
    Calendar c = (Calendar) birth.clone();
    c.add(Calendar.DAY_OF_MONTH, dayShift);
    c.add(Calendar.MONTH, months);
    return c.getTime();
  }
}
