/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.pharmacy;

import com.divudi.entity.Category;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 *
 * @author buddhika
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Make extends Category implements Serializable {
   
    

  
}
