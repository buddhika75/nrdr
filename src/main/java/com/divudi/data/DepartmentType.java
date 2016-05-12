/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data;

/**
 *
 * @author Buddhika
 */
public enum DepartmentType {
    Pharmacy,
    Lab,
    Store,
    Theatre,
    Kitchen,
    Opd,
    Inventry,
    Inward;
    
    public String getLabel(){
        switch (this){
            case Inward: return "Ward";
            default:return this.toString();
        }
    }
}
