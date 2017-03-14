package com.divudi.bean.common;

import com.divudi.bean.clinical.PatientEncounterController;
import com.divudi.bean.clinical.PatientItemController;
import com.divudi.bean.clinical.PracticeBookingController;
import com.divudi.bean.lab.PatientInvestigationController;
import com.divudi.data.OccupationType;
import com.divudi.data.Race;
import com.divudi.data.Religion;
import com.divudi.data.Sex;
import com.divudi.data.SymanticType;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.ejb.BillNumberGenerator;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.PatientInstitution;
import com.divudi.entity.Person;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PatientInstitutionFacade;
import com.divudi.facade.PersonFacade;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class PatientController implements Serializable {

    /**
     * EJBs
     */
    @EJB
    private PatientFacade ejbFacade;
    @EJB
    PatientInstitutionFacade patientInstitutionFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    BillNumberGenerator billNumberBean;
    @EJB
    CommonFunctions commonFunctions;
    @Inject
    PatientEncounterController PatientEncounterController;
    @Inject
    PatientItemController patientItemController;
    @Inject
    ItemController itemController;
    /**
     * Controllers
     */
    @Inject
    SessionController sessionController;
    @Inject
    PracticeBookingController practiceBookingController;
    /**
     * Properties
     */
    private Patient current;
    List<Item> currentDiagnoses;
    private List<Patient> items = null;

    private Date dob;

    StreamedContent barcode;
    private static final long serialVersionUID = 1L;

    public void patientSelected() {
        getPatientEncounterController().fillCurrentPatientLists(current);
        getPatientItemController().fillCurrentPatientDiagnoses(current);
    }

    public void createPatientBarcode() {
        File barcodeFile = new File("ptbarcode");
        if (current != null && current.getCode() != null && !current.getCode().trim().equals("")) {
            try {
                BarcodeImageHandler.saveJPEG(BarcodeFactory.createCode128(getCurrent().getCode()), barcodeFile);
                barcode = new DefaultStreamedContent(new FileInputStream(barcodeFile), "image/jpeg");

            } catch (Exception ex) {
                //   //System.out.println("ex = " + ex.getMessage());
            }
        } else {
            //   //System.out.println("else = ");
            try {
                Barcode bc = BarcodeFactory.createCode128A("0000");
                bc.setBarHeight(5);
                bc.setBarWidth(3);
                bc.setDrawingText(true);
                BarcodeImageHandler.saveJPEG(bc, barcodeFile);
                //   //System.out.println("12");
                barcode = new DefaultStreamedContent(new FileInputStream(barcodeFile), "image/jpeg");
            } catch (Exception ex) {
                //   //System.out.println("ex = " + ex.getMessage());
            }
        }
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    private YearMonthDay yearMonthDay;

    public YearMonthDay getYearMonthDay() {
        if (yearMonthDay == null) {
            yearMonthDay = new YearMonthDay();

        }
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public void dateChangeListen() {
        getCurrent().getPerson().setDob(getCommonFunctions().guessDob(yearMonthDay));
    }

    public StreamedContent getPhoto(Patient p) {
        ////System.out.println("p is " + p);
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            return new DefaultStreamedContent();
        } else if (p == null) {
            return new DefaultStreamedContent();
        } else {
            if (p.getId() != null && p.getBaImage() != null) {
                ////System.out.println("giving image");
                return new DefaultStreamedContent(new ByteArrayInputStream(p.getBaImage()), p.getFileType(), p.getFileName());
            } else {
                return new DefaultStreamedContent();
            }
        }

    }

    public StreamedContent getPhotoByByte(byte[] p) {
        ////System.out.println("p is " + p);
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            return new DefaultStreamedContent();
        } else if (p == null) {
            return new DefaultStreamedContent();
        } else {
            //   //System.out.println("giving image");
            return new DefaultStreamedContent(new ByteArrayInputStream(p), "image/png", "photo.");
        }
    }

    public Title[] getTitles() {
        return Title.values();
    }

    public Sex[] getSexs() {
        return Sex.values();
    }

    public Race[] getRaces() {
        return Race.values();
    }

    public Religion[] getReligions() {
        return Religion.values();
    }
    
    public OccupationType[] getOccupationTypes() {
        return OccupationType.values();
    }

    public void prepareAdd() {
        setCurrent(null);
        yearMonthDay = null;
        getCurrent();
        getYearMonthDay();
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successfull");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private void recreateModel() {
        items = null;
    }

    public void createRandomPatient(String ptName) {
        Person person = new Person();
        Patient pt = new Patient();
        person.setName(ptName);
        pt.setPerson(person);
        getPersonFacade().create(person);
        getFacade().create(pt);
    }

    public List<Patient> completePatient(String query) {
        List<Patient> suggestions;
        String sql;
        HashMap hm = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select p from Patient p where p.retired=false "
                    + " and upper(p.person.name) like :q "
                    + "  order by p.person.name";
            hm.put("q", "%" + query.toUpperCase() + "%");
            ////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, hm, 20);
        }
        return suggestions;
    }

    List<Patient> patientList;

    public List<Patient> completePatientByNameOrCode(String query) {
        if (query == null) {
            return null;
        }
        String sql;
        HashMap hm = new HashMap();
        sql = "select p from Patient p where p.retired=false "
                + " and ( upper(p.person.name) like  :q "
                + " or upper(p.code) like :q "
                + " or upper(p.person.nic) like :q "
                + " or upper(p.person.mobile) like :q "
                + " or upper(p.person.phone) like :q "
                + " or upper(p.person.address) like :q )"
                + "order by p.person.name";
        hm.put("q", "%" + query.toUpperCase() + "%");
        patientList = getFacade().findBySQL(sql, hm, 20);
        System.err.println("patientList.size() = " + patientList.size());
        return patientList;
    }

    public void saveAndUpdateQueue() {
        saveSelected();
        getPracticeBookingController().listBillSessions();
    }

    public String getCountPatientCode() {

        String sql;

        sql = "select count(p) FROM Patient p where p.code is not null";

        long lng = getEjbFacade().countBySql(sql);
        lng++;
        String str = "";
        str += lng;
        return str;
    }

    public void saveSelected() {
        System.out.println("saving using edit and commit  ");
        if (getCurrent() == null) {
            UtilityController.addErrorMessage("No Current. Error. NOT SAVED");
            return;
        }
        if (getCurrent().getPerson() == null) {
            UtilityController.addErrorMessage("No Person. Not Saved");
            return;
        }
        if (getCurrent().getPerson().getName().trim().equals("")) {
            UtilityController.addErrorMessage("Please enter a name");
            return;
        }
        if (getCurrent().getPerson().getId() == null) {
            getCurrent().getPerson().setCreatedAt(Calendar.getInstance().getTime());
            getCurrent().getPerson().setCreater(getSessionController().getLoggedUser());
            getPersonFacade().create(getCurrent().getPerson());
            System.out.println("1.getCurrent().getPerson().getTitle() = " + getCurrent().getPerson().getTitle());
            System.out.println("getCurrent().getPerson().getNameWithTitle() = " + getCurrent().getPerson().getNameWithTitle());
        } else {
            getCurrent().getPerson().setEditedAt(Calendar.getInstance().getTime());
            getCurrent().getPerson().setEditer(getSessionController().getLoggedUser());
            getPersonFacade().editAndCommit(getCurrent().getPerson());
            System.out.println("2.getCurrent().getPerson().getTitle() = " + getCurrent().getPerson().getTitle());
            System.out.println("getCurrent().getPerson().getNameWithTitle() = " + getCurrent().getPerson().getNameWithTitle());
        }
        if (getCurrent().getId() == null) {
            getCurrent().setCreatedAt(new Date());
            getCurrent().setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            System.out.println("3.getCurrent().getPerson().getTitle() = " + getCurrent().getPerson().getTitle());
            System.out.println("getCurrent().getPerson().getNameWithTitle() = " + getCurrent().getPerson().getNameWithTitle());
            UtilityController.addSuccessMessage("Saved as a new patient successfully.");
        } else {
            getCurrent().setEditedAt(Calendar.getInstance().getTime());
            getCurrent().setEditer(getSessionController().getLoggedUser());
            getFacade().editAndCommit(getCurrent());
            System.out.println("4.getCurrent().getPerson().getTitle() = " + getCurrent().getPerson().getTitle());
            System.out.println("getCurrent().getPerson().getNameWithTitle() = " + getCurrent().getPerson().getNameWithTitle());
            UtilityController.addSuccessMessage("Updated the patient details successfully.");
        }
        setClinicNumber(current, current.getClinicNumber());
        System.out.println("5.getCurrent().getPerson().getTitle() = " + getCurrent().getPerson().getTitle());
        System.out.println("getCurrent().getPerson().getNameWithTitle() = " + getCurrent().getPerson().getNameWithTitle());
    }

    public PatientFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(PatientFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public String getClinicNumber(Patient pt) {
        String j;
        Map m = new HashMap();
        j = "select pi from PatientInstitution pi "
                + " where pi.retired=false "
                + " and pi.department=:dept "
                + " and pi.patient=:pt";
        m.put("pt", pt);
        m.put("dept", sessionController.getDepartment());
        PatientInstitution pi = patientInstitutionFacade.findFirstBySQL(j, m);
        if (pi != null) {
            return pi.getPatientNumber();
        } else {
            return "";
        }
    }

    public void setClinicNumber(Patient pt, String clinicNumber) {
        String j;
        Map m = new HashMap();
        j = "select pi from PatientInstitution pi "
                + " where pi.retired=false "
                + " and pi.department=:dept "
                + " and pi.patient=:pt";
        m.put("pt", pt);
        m.put("dept", sessionController.getDepartment());
        PatientInstitution pi = patientInstitutionFacade.findFirstBySQL(j, m);
        if (pi != null) {
            pi.setPatientNumber(clinicNumber);
            patientInstitutionFacade.edit(pi);
        } else {
            pi = new PatientInstitution();
            pi.setDepartment(getSessionController().getDepartment());
            pi.setPatient(current);
            pi.setPatientNumber(clinicNumber);
            pi.setCreatedAt(new Date());
            pi.setCreater(getSessionController().getLoggedUser());
            patientInstitutionFacade.create(pi);
        }
    }

    public PatientController() {
    }

    public Patient getCurrent() {
        if (current == null) {
            Person p = new Person();
            current = new Patient();
            current.setCode(getCountPatientCode());
            current.setPerson(p);
        }
        return current;
    }

    public void setCurrent(Patient current) {
        this.current = current;
        getYearMonthDay();
        if (current == null) {
            yearMonthDay.setDay("0");
            yearMonthDay.setMonth("0");
            yearMonthDay.setYear("0");
        } else {
            yearMonthDay.setDay(current.getAgeDays() + "");
            yearMonthDay.setMonth(current.getAgeMonths() + "");
            yearMonthDay.setYear(current.getAgeYears() + "");

            getPatientEncounterController().fillCurrentPatientLists(current);
            getPatientItemController().fillCurrentPatientDiagnoses(current);
            current.setClinicNumber(getClinicNumber(current));

        }

    }

    private PatientFacade getFacade() {
        return ejbFacade;
    }

    public List<Patient> getItems() {
        if (items == null || items.isEmpty()) {
            fillAllPatients();
        }
        return items;
    }

    public void fillAllPatients() {
        String sql;
        sql = "select p from Patient p where p.retired = false order by p.person.name";
        items = getFacade().findBySQL(sql);
    }

    public List<Patient> getItemsByDob() {
        String sql;
        Map m = new HashMap();
        m.put("dob", dob);
        sql = "select p from Patient p where p.retired = false and p.person.dob=:dob order by p.person.name";
        return getFacade().findBySQL(sql, m);
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public BillNumberGenerator getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberGenerator billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public StreamedContent getBarcode() {
        return barcode;
    }

    public void setBarcode(StreamedContent barcode) {
        this.barcode = barcode;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public List<Item> getCurrentDiagnoses() {
        return currentDiagnoses;
    }

    public void setCurrentDiagnoses(List<Item> currentDiagnoses) {
        this.currentDiagnoses = currentDiagnoses;
    }

    
    
    /**
     *
     * Set all Patients to null
     *
     */
    /**
     *
     */
    /**
     *
     * Delete the current Patient
     *
     */
    /**
     *
     */
    @FacesConverter(forClass = Patient.class)
    public static class PatientControllerConverter implements Converter {

        /**
         *
         * @param facesContext
         * @param component
         * @param value
         * @return
         */
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PatientController controller = (PatientController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "patientController");
            ////System.out.println("value at converter getAsObject is " + value);
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            ////System.out.println(value);
            if (value == null || value.equals("null") || value.trim().equals("")) {
                key = 0l;
            } else {
                key = Long.valueOf(value);
                ////System.out.println(key);
                ////System.out.println(value);
            }
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        /**
         *
         * @param facesContext
         * @param component
         * @param object
         * @return
         */
        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Patient) {
                Patient o = (Patient) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + PatientController.class.getName());
            }
        }
    }

    public PracticeBookingController getPracticeBookingController() {
        return practiceBookingController;
    }

    public void setPracticeBookingController(PracticeBookingController practiceBookingController) {
        this.practiceBookingController = practiceBookingController;
    }

    public PatientEncounterController getPatientEncounterController() {
        return PatientEncounterController;
    }

    public void setPatientEncounterController(PatientEncounterController PatientEncounterController) {
        this.PatientEncounterController = PatientEncounterController;
    }

    public PatientItemController getPatientItemController() {
        return patientItemController;
    }

    public PatientInstitutionFacade getPatientInstitutionFacade() {
        return patientInstitutionFacade;
    }

    public void setPatientInstitutionFacade(PatientInstitutionFacade patientInstitutionFacade) {
        this.patientInstitutionFacade = patientInstitutionFacade;
    }

    public List<Patient> getPatientList() {
        return patientList;
    }

    public void setPatientList(List<Patient> patientList) {
        this.patientList = patientList;
    }

    
    
    @FacesConverter("patientConverter")
    public static class PatientConverter implements Converter {

        /**
         *
         * @param facesContext
         * @param component
         * @param value
         * @return
         */
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PatientController controller = (PatientController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "patientController");
            ////System.out.println("value at converter getAsObject is " + value);
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            ////System.out.println(value);
            if (value == null || value.equals("null") || value.trim().equals("")) {
                key = 0l;
            } else {
                key = Long.valueOf(value);
                ////System.out.println(key);
                ////System.out.println(value);
            }
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        /**
         *
         * @param facesContext
         * @param component
         * @param object
         * @return
         */
        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Patient) {
                Patient o = (Patient) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + PatientController.class.getName());
            }
        }
    }

}
