/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.clinical;

import com.divudi.bean.lab.*;
import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.UtilityController;
import com.divudi.bean.report.InstitutionLabSumeryController;
import com.divudi.data.SymanticType;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.Department;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.PatientItem;
import com.divudi.entity.Sms;
import com.divudi.entity.lab.Investigation;
import com.divudi.entity.lab.InvestigationItem;

import com.divudi.entity.lab.PatientReport;
import com.divudi.entity.lab.ReportItem;
import com.divudi.facade.BillFacade;
import com.divudi.facade.InvestigationFacade;
import com.divudi.facade.InvestigationItemFacade;
import com.divudi.facade.PatientItemFacade;
import com.divudi.facade.PatientReportFacade;
import com.divudi.facade.ReportItemFacade;
import com.divudi.facade.SmsFacade;
import com.divudi.facade.util.JsfUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class PatientItemController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private PatientItemFacade ejbFacade;
    private PatientItem current;
    Item currentItem;
    private List<PatientItem> currentItems = null;
    Patient currentPatient;
    
    
    public void addPatientDiagnosis(){
        if(currentPatient==null){
            JsfUtil.addErrorMessage("Patient ?");
            return ;
        }
        if(currentItem==null){
            JsfUtil.addErrorMessage("Diagnosis ?");
            return ;
        }
        PatientItem pi = new PatientItem();
        pi.setItem(currentItem);
        pi.setPatient(currentPatient);
        pi.setCreatedAt(new Date());
        pi.setCreater(sessionController.getLoggedUser());
        getEjbFacade().create(pi);
        fillCurrentPatientDiagnoses();
    }
    
    public void removePatientDiagnosis(){
        if(currentPatient==null){
            JsfUtil.addErrorMessage("Patient ?");
            return ;
        }
        if(current==null){
            JsfUtil.addErrorMessage("Patient Diagnosis ?");
            return ;
        }
        current.setRetired(true);
        current.setRetiredAt(new Date());
        current.setRetirer(sessionController.getLoggedUser());
        getEjbFacade().edit(current);
        JsfUtil.addSuccessMessage("Removed");
        fillCurrentPatientDiagnoses();
    }
    
    
    public void fillCurrentPatientDiagnoses(Patient patient){
        currentPatient = patient;
        fillCurrentPatientDiagnoses();
    }
    
    public void fillCurrentPatientDiagnoses(){
        if(currentPatient==null){
            JsfUtil.addErrorMessage("Patient ?");
            return ;
        }
        String j;
        Map m = new HashMap();
        j="select pi from PatientItem pi "
                + " where pi.patient=:p "
                + " and pi.item.symanticType=:st "
                + " and pi.retired=:r";
        m.put("p", currentPatient);
        m.put("st", SymanticType.Disease_or_Syndrome);
        m.put("r", false);
        currentItems = getEjbFacade().findBySQL(j, m);
    }

    public PatientItemFacade getEjbFacade() {
        return ejbFacade;
    }

    public PatientItem getCurrent() {
        return current;
    }

    public void setCurrent(PatientItem current) {
        this.current = current;
    }

    public List<PatientItem> getCurrentItems() {
        return currentItems;
    }

    public void setCurrentItems(List<PatientItem> currentItems) {
        this.currentItems = currentItems;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public void setCurrentPatient(Patient currentPatient) {
        this.currentPatient = currentPatient;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public void setEjbFacade(PatientItemFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public Item getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(Item currentItem) {
        this.currentItem = currentItem;
    }

    
    
    
    
    /**
     *
     */
    @FacesConverter(forClass = PatientItem.class)
    public static class PatientItemControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PatientItemController controller = (PatientItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "patientItemController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof PatientItem) {
                PatientItem o = (PatientItem) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + PatientItemController.class.getName());
            }
        }
    }
}
