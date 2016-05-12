/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.common;

import com.divudi.data.AreaType;
import com.divudi.entity.Area;
import com.divudi.facade.AreaFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class AreaController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private AreaFacade ejbFacade;
    private Area current;
    private List<Area> items = null;
    private List<Area> districts = null;
    private List<Area> dss = null;
    private List<Area> gns = null;

    public List<Area> completeArea(String qry) {
        List<Area> list;
        String sql;
        HashMap hm = new HashMap();
        sql = "select c from Area c "
                + " where c.retired=false "
                + " and upper(c.name) like :q "
                + " order by c.name";
        hm.put("q", "%" + qry.toUpperCase() + "%");
        list = getFacade().findBySQL(sql, hm);

        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }
    
    

    public List<Area> completeDistricts(String qry) {
        return completeArea(qry, AreaType.District);
    }

    public List<Area> completeDivSecs(String qry) {
        return completeArea(qry, AreaType.Divisional_Secretariat);
    }

    public List<Area> completeGnds(String qry) {
        return completeArea(qry, AreaType.Grama_Niladhari_Divisions);
    }

    public List<Area> completeArea(String qry, AreaType type) {
        List<Area> list;
        String sql;
        HashMap hm = new HashMap();
        sql = "select c from Area c "
                + " where c.retired=false ";
        if (type != null) {
            hm.put("ty", type);
            sql += " and c.areaType=:ty ";
        }
        if (qry != null) {
            qry += " and upper(c.name) like :q ";
            hm.put("q", "%" + qry.toUpperCase() + "%");
        }
        sql += " order by c.name";

        list = getFacade().findBySQL(sql, hm);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    
    
    
    public Area findArea(String areaName, AreaType type) {
        Area area;
        String sql;
        HashMap hm = new HashMap();
        sql = "select c from Area c "
                + " where c.retired=false ";
        if (type != null) {
            hm.put("ty", type);
            sql += " and c.areaType=:ty ";
        }
        if (areaName != null) {
            sql += " and upper(c.name) like :q ";
            hm.put("q", "%" + areaName.toUpperCase() + "%");
        }
        area = getFacade().findFirstBySQL(sql, hm);
        if (area == null) {
            area = new Area();
            area.setName(areaName);
            getFacade().create(area);
        }
        return area;
    }

    public Area findArea(String areaName, AreaType type, Area superArea) {
        Area area;
        String sql;
        HashMap hm = new HashMap();
        sql = "select c from Area c "
                + " where c.retired=false ";
        if (type != null) {
            hm.put("ty", type);
            sql += " and c.areaType=:ty ";
        }
        if (areaName != null) {
            sql += " and upper(c.name) like :q ";
            hm.put("q", "%" + areaName.toUpperCase() + "%");
        }
        if (superArea != null) {
            sql += " and c.superArea =:supa ";
            hm.put("supa", superArea);
        }
        area = getFacade().findFirstBySQL(sql, hm);
        if (area == null) {
            area = new Area();
            area.setName(areaName);
            area.setSuperArea(superArea);
            getFacade().create(area);
        }
        return area;
    }
    
    
    
    
    public void prepareAdd() {
        current = new Area();
    }

    private void recreateModel() {
        items = null;
    }

    public void saveSelected() {

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Updated Successfully.");
        } else {
            current.setCreatedAt(new Date());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("Saved Successfully");
        }
        recreateModel();
        getItems();
    }

    public AreaFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(AreaFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public AreaController() {
    }

    public Area getCurrent() {
        if (current == null) {
            current = new Area();
        }
        return current;
    }

    public void setCurrent(Area current) {
        this.current = current;
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private AreaFacade getFacade() {
        return ejbFacade;
    }

    public List<Area> getItems() {
        if (items == null) {
            String j;
            j = "select a "
                    + " from Area a "
                    + " where a.retired=false "
                    + " "
                    + " order by a.name";
            items = getFacade().findBySQL(j);
        }
        return items;
    }

    public List<Area> getDistricts() {
        if (districts == null) {
            districts = completeDistricts(null);
        }
        return districts;
    }

    public void setDistricts(List<Area> districts) {
        this.districts = districts;
    }

    public List<Area> getDss() {
        if(dss==null){
            dss= completeDivSecs(null);
        }
        return dss;
    }

    public void setDss(List<Area> dss) {
        this.dss = dss;
    }

    public List<Area> getGns() {
        if(gns==null){
            gns = completeGnds(null);
        }
        return gns;
    }

    public void setGns(List<Area> gns) {
        this.gns = gns;
    }

    
    
    /**
     *
     */
    @FacesConverter(forClass = Area.class)
    public static class AreaConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + AreaController.class.getName());
            }
        }
    }

    @FacesConverter("areaCon")
    public static class AreaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + AreaController.class.getName());
            }
        }
    }
}
