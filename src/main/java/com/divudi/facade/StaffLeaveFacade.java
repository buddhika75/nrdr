/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.divudi.facade;

import com.divudi.entity.hr.StaffLeave;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author safrin
 */
@Stateless
public class StaffLeaveFacade extends AbstractFacade<StaffLeave> {
    @PersistenceContext(unitName = "pu")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        if(em==null){
            
        }
        if(em == null){}return em;
    }

    public StaffLeaveFacade() {
        super(StaffLeave.class);
    }
    
}
