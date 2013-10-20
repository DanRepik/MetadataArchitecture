package com.repik.roo.entity.association.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class WebAssociationOperationsImpl implements WebAssociationOperations {
    
    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;

    private JavaType rooAssociationType = new JavaType(RooWebAssociation.class.getName());
	private JavaType rooWebAssociationType = new JavaType( RooWebAssociation.class ) ;
	private JavaType rooWebScaffoldType = new JavaType( RooWebScaffold.class ) ;
    @Reference protected MemberDetailsScanner memberDetailsScanner;


    /**
     * Use TypeManagementService to change types
     */
    @Reference private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // Check if a project has been created
        return projectOperations.isFocusedProjectAvailable();
    }

    /** {@inheritDoc} */
    public void newAssociation(JavaType entity1, JavaType entity2 ) {
    	
        // Use Roo's Assert type for null checks
        Validate.notNull(entity1, "Java type required");
        Validate.notNull(entity2, "Java type required");

        addWebControllerAnnotation( entity1, entity2 ) ;
        
    }

	private void addWebControllerAnnotation(JavaType entity1, JavaType entity2) {
		for ( JavaType webScaffoldType : typeLocationService.findTypesWithAnnotation(rooWebScaffoldType)) {
	        final ClassOrInterfaceTypeDetails typeDetails = typeLocationService.getTypeDetails(webScaffoldType);
	        if (typeDetails != null) {
		        MemberDetails memberDetails = memberDetailsScanner.getMemberDetails(getClass().getName(),
		                typeDetails);
		        AnnotationMetadata metadata = memberDetails.getAnnotation(rooWebScaffoldType) ;
		        AnnotationAttributeValue<JavaType> value = metadata.getAttribute("formBackingObject") ;
		        String valueStr = value.getValue().getFullyQualifiedTypeName() ;
		        if ( valueStr.equals(entity1.getFullyQualifiedTypeName()) || valueStr.equals(entity2.getFullyQualifiedTypeName())) {
		        	injectWebAssociationAnnotation( webScaffoldType ) ;
		        }
	        }
		}
	}

	private void injectWebAssociationAnnotation(JavaType webScaffoldType) {
        ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(webScaffoldType );
        if ( existing != null ) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
        	AnnotationMetadata metadata = MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), rooAssociationType) ;
        	if ( metadata == null ) {
        		AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder( rooWebAssociationType ) ;
            	classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
                typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        	}
        }
	}

    /** {@inheritDoc} */
    public void setup() {
        // Install the add-on Google code repository needed to get the annotation 
        //projectOperations.addRepository("", new Repository("Association Roo add-on repository", "Association Roo add-on repository", "https://com-repik-roo-entity-association.googlecode.com/svn/repo"));
        
        List<Dependency> dependencies = new ArrayList<Dependency>();
        
        // Install the dependency on the add-on jar (
        dependencies.add(new Dependency("com.repik.roo.entity.association", "com.repik.roo.entity.association", "0.1.0.BUILD-SNAPSHOT", DependencyType.JAR, DependencyScope.PROVIDED));
        
        // Install dependencies defined in external XML file
        for (Element dependencyElement : XmlUtils.findElements("/configuration/batch/dependencies/dependency", XmlUtils.getConfiguration(getClass()))) {
            dependencies.add(new Dependency(dependencyElement));
        }

        // Add all new dependencies to pom.xml
        projectOperations.addDependencies("", dependencies);
    }
    
}