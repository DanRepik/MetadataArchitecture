package com.repik.roo.entity.association.domain;

import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers. Typically used by a command type or an external add-on.
 *
 * @since 1.1
 */
public interface AssociationOperations {

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    /**
     * Annotate the provided Java type with the trigger of this add-on
     */
    void newAssociation(JavaType target1, JavaType target2);
    
    /**
     * Setup all add-on artifacts (dependencies in this case)
     */
    void setup();
}