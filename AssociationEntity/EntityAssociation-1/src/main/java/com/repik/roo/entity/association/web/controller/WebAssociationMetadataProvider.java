package com.repik.roo.entity.association.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import com.repik.roo.entity.association.domain.AssociationMetadata;
import com.repik.roo.entity.association.domain.RooAssociation;

/**
 * Provides {@link AssociationMetadata}. This type is called by Roo to retrieve the metadata for this add-on.
 * Use this type to reference external types and services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @since 1.1
 */
@Component
@Service
public final class WebAssociationMetadataProvider extends AbstractItdMetadataProvider {

    /**
     * The activate method for this OSGi component, this will be called by the OSGi container upon bundle activation 
     * (result of the 'addon install' command) 
     * 
     * @param context the component context can be used to get access to the OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(RooWebAssociation.class.getName()));
    }
    
    /**
     * The deactivate method for this OSGi component, this will be called by the OSGi container upon bundle deactivation 
     * (result of the 'addon uninstall' command) 
     * 
     * @param context the component context can be used to get access to the OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(RooWebAssociation.class.getName()));    
    }
    
	private JavaType rooWebScaffoldType = new JavaType( RooWebScaffold.class ) ;
    private JavaType rooAssociationType = new JavaType(RooAssociation.class.getName());

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata, String itdFilename) {
    	
    	//	get the form backing object from the web scaffold annotation
    	ClassOrInterfaceTypeDetails typeDetails = governorPhysicalTypeMetadata.getMemberHoldingTypeDetails() ;
		AnnotationMetadata scaffoldAnnotation = typeDetails.getAnnotation(rooWebScaffoldType);
        AnnotationAttributeValue<JavaType> formBackingAttribute = scaffoldAnnotation.getAttribute("formBackingObject") ;
		JavaType formBackingType = formBackingAttribute.getValue() ;

		//	now collect the associated types from the backing objects assoication annotation
		ClassOrInterfaceTypeDetails entityDetails = typeLocationService.getTypeDetails(formBackingType) ;
		AnnotationMetadata associationAnnotation = entityDetails.getAnnotation( rooAssociationType ) ;
		AnnotationAttributeValue<List<ClassAttributeValue>> annotationValue = associationAnnotation.getAttribute("value");
		List<ClassAttributeValue> associatedEntities = annotationValue.getValue() ;

		List<JavaType> associatedTypes = new ArrayList<JavaType>() ;
		for ( ClassAttributeValue associatedEntity : associatedEntities ) {
			associatedTypes.add( associatedEntity.getValue()) ;
		}

        // Pass dependencies required by the metadata in through its constructor
        return new WebAssociationMetadata(metadataIdentificationString, aspectName, governorPhysicalTypeMetadata, formBackingType, associatedTypes );
    }
    
    /**
     * Define the unique ITD file name extension, here the resulting file name will be **_ROO_Association.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "Association";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = WebAssociationMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = WebAssociationMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }
    
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return WebAssociationMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return WebAssociationMetadata.getMetadataIdentiferType();
    }
}