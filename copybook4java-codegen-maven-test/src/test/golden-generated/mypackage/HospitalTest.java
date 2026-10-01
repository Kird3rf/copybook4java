package mypackage;

import com.nordea.oss.copybook.annotations.CopyBook;
import com.nordea.oss.copybook.annotations.CopyBookLine;

@CopyBook(charset = "UTF-8")
public class HospitalTest {
    @CopyBookLine("01 HOSPITAL.")
    private Hospital hospital;

    @CopyBookLine("01 WARD.")
    private Ward ward;

    @CopyBookLine("01 PATIENT.")
    private Patient patient;

    @CopyBookLine("01 SYMPTOM.")
    private Symptom symptom;

    @CopyBookLine("01 TREATMNT.")
    private Treatmnt treatmnt;

    @CopyBookLine("01 DOCTOR.")
    private Doctor doctor;

    @CopyBookLine("01 FACILITY.")
    private Facility facility;

    @CopyBook(charset = "UTF-8")
    public static class Hospital {
        @CopyBookLine("03 HOSPNAME PIC X(20).")
        private String hospname;

        @CopyBookLine("03 HOSP-ADDRESS PIC X(30).")
        private String hospAddress;

        @CopyBookLine("03 HOSP-PHONE PIC X(10).")
        private String hospPhone;

        @CopyBookLine("03 ADMIN PIC X(20).")
        private String admin;
    }

    @CopyBook(charset = "UTF-8")
    public static class Ward {
        @CopyBookLine("03 WARDNO PIC XX.")
        private String wardno;

        @CopyBookLine("03 TOT-ROOMS PIC XXX.")
        private String totRooms;

        @CopyBookLine("03 TOT-BEDS PIC XXX.")
        private String totBeds;

        @CopyBookLine("03 BEDAVAIL PIC XXX.")
        private String bedavail;

        @CopyBookLine("03 WARDTYPE PIC X(20).")
        private String wardtype;
    }

    @CopyBook(charset = "UTF-8")
    public static class Patient {
        @CopyBookLine("03 PATNAME PIC X(20).")
        private String patname;

        @CopyBookLine("03 PATADDRESS PIC X(30).")
        private String pataddress;

        @CopyBookLine("03 PAT-PHONE PIC X(10).")
        private String patPhone;

        @CopyBookLine("03 BEDIDENT PIC X(4).")
        private String bedident;

        @CopyBookLine("03 DATEADMT PIC X(6).")
        private String dateadmt;

        @CopyBookLine("03 PREV-STAY-FLAG PIC X.")
        private String prevStayFlag;

        @CopyBookLine("03 PREV-HOSP PIC X(20).")
        private String prevHosp;

        @CopyBookLine("03 PREV-DATE PIC X(4).")
        private String prevDate;

        @CopyBookLine("03 PREV-REASON PIC X(30).")
        private String prevReason;
    }

    @CopyBook(charset = "UTF-8")
    public static class Symptom {
        @CopyBookLine("03 DIAGNOSE PIC X(20).")
        private String diagnose;

        @CopyBookLine("03 SYMPDATE PIC X(6).")
        private String sympdate;

        @CopyBookLine("03 PREV-TREAT-FLAG PIC X.")
        private String prevTreatFlag;

        @CopyBookLine("03 TREAT-DESC PIC X(20).")
        private String treatDesc;

        @CopyBookLine("03 SYMP-DOCTOR PIC X(20).")
        private String sympDoctor;

        @CopyBookLine("03 SYMP-DOCT-PHONE PIC X(10).")
        private String sympDoctPhone;
    }

    @CopyBook(charset = "UTF-8")
    public static class Treatmnt {
        @CopyBookLine("03 TRTYPE PIC X(20).")
        private String trtype;

        @CopyBookLine("03 TRDATE PIC X(6).")
        private String trdate;

        @CopyBookLine("03 MEDICATION-TYPE PIC X(20).")
        private String medicationType;

        @CopyBookLine("03 DIET-COMMENT PIC X(30).")
        private String dietComment;

        @CopyBookLine("03 SURGERY-FLAG PIC X.")
        private String surgeryFlag;

        @CopyBookLine("03 SURGERY-DATE PIC X(6).")
        private String surgeryDate;

        @CopyBookLine("03 SURGERY-COMMENT PIC X(30).")
        private String surgeryComment;
    }

    @CopyBook(charset = "UTF-8")
    public static class Doctor {
        @CopyBookLine("03 DOCTNAME PIC X(20).")
        private String doctname;

        @CopyBookLine("03 DOCT-ADDRESS PIC X(30).")
        private String doctAddress;

        @CopyBookLine("03 DOCT-PHONE PIC X(10).")
        private String doctPhone;

        @CopyBookLine("03 SPECIALT PIC X(20).")
        private String specialt;
    }

    @CopyBook(charset = "UTF-8")
    public static class Facility {
        @CopyBookLine("03 FACTYPE PIC X(20).")
        private String factype;

        @CopyBookLine("03 TOT-FACIL PIC XXX.")
        private String totFacil;

        @CopyBookLine("03 FACAVAIL PIC XXX.")
        private String facavail;
    }
}
