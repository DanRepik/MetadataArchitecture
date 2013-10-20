package com.repik.roo.entity.association.domain;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

import sun.rmi.log.ReliableLog.LogFile;

import com.repik.roo.builders.AnnotationBuilder;
import com.repik.roo.builders.ClassBuilder;
import com.repik.roo.builders.FieldBuilder;
import com.repik.roo.entity.association.web.controller.WebAssociationOperations;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class AssociationOperationsImpl implements AssociationOperations {
    
	private Logger log = Logger.getLogger( AssociationOperationsImpl.class.getSimpleName()) ;
	
    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;

    private JavaType rooAssociationType = new JavaType("com.repik.roo.entity.association.domain.RooAssociation");
    @Reference protected MemberDetailsScanner memberDetailsScanner;
    @Reference protected WebAssociationOperations webAssociationOperations ;

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

        //	new association entity is concatenation based on order
    	JavaType entityType = getAssociationEntityType(entity1, entity2);

        buildAssociationEntity(entityType, entity1, entity2 ) ;
        addEntityAnnotation( entity1, entity2 ) ;
        addEntityAnnotation( entity2, entity1 ) ;
        
        webAssociationOperations.newAssociation( entity1, entity2 ) ;
    }

    /**
     * figure out the canonical association entity domain object name.
     * @param target1
     * @param target2
     * @return
     */
	private JavaType getAssociationEntityType(JavaType target1, JavaType target2) {
    	if ( target1.getSimpleTypeName().compareTo( target2.getSimpleTypeName()) < 0 ) {
        	return new JavaType( target1.getFullyQualifiedTypeName() + target2.getSimpleTypeName()) ;
    	}
    	return new JavaType( target2.getFullyQualifiedTypeName() + target1.getSimpleTypeName()) ;
	}
    
	/**
	 * Inject the association entity annotation into the domain class
	 * @param entityType
	 * @param with
	 */
    private void addEntityAnnotation( JavaType entityType, JavaType with ) {
        
        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails entityDetails = typeLocationService.getTypeDetails(entityType);

        if ( entityDetails != null ) {
        
            //	assemble the set of associated entities
            Set<ClassAttributeValue> values = new HashSet<ClassAttributeValue>() ;

            AnnotationMetadata associationMetadata = MemberFindingUtils.getAnnotationOfType(entityDetails.getAnnotations(), rooAssociationType) ;
            if ( associationMetadata != null ) {
        		AnnotationAttributeValue<List<ClassAttributeValue>> attributeValue = associationMetadata.getAttribute( "value" );
	            values.addAll(attributeValue.getValue()) ;
        	}        	
            values.add( new ClassAttributeValue( new JavaSymbolName( "value" ), with )) ;

        	ClassBuilder builder = new ClassBuilder( entityDetails )
        		.annotation( new AnnotationBuilder( rooAssociationType )
        			.attribute( "value", new ArrayList<ClassAttributeValue>( values ))) ;

        	// Save changes to disk
            typeManagementService.createOrUpdateTypeOnDisk(builder.build());
        }
    }

    /**
     * Build the association domain entity
     * @param entityType
     * @param target1
     * @param target2
     */
    private void buildAssociationEntity( JavaType entityType, JavaType target1, JavaType target2 ) {

        final String metadataId = PhysicalTypeIdentifier
                .createIdentifier(entityType, projectOperations
                        .getPathResolver().getFocusedPath(Path.SRC_MAIN_JAVA));

        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails entityDetail = typeLocationService.getTypeDetails(entityType);

        String identifierColumn = target1.getSimpleTypeName().toUpperCase() + "_" + target2.getSimpleTypeName().toUpperCase() + "_ID" ; 

        ClassBuilder classBuilder = new ClassBuilder( metadataId, entityDetail )
        	.type( PhysicalTypeCategory.CLASS )
        	.modifier( Modifier.PUBLIC | Modifier.FINAL )
        	.named( entityType ) 
        	.annotation( "org.springframework.roo.addon.javabean.RooJavaBean" )
        	.annotation(new AnnotationBuilder( "org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord")
        		.attribute( "identifierColumn", identifierColumn ))
        	.annotation( "org.springframework.roo.addon.tostring.RooToString") 
        	.field( getEntityAssociationField( metadataId, target1)) 
        	.field( getEntityAssociationField( metadataId, target2)) ;
        
       	typeManagementService.createOrUpdateTypeOnDisk(classBuilder.build());
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
    
	/**
	 * Create metadata for a field definition.
	 * 
	 * @param entityBuilder the entity builder to add the field to
	 * @param metaDataId 
	 * @param type the entity reference to add
	 * @return a FieldMetadata object
	 */
	private FieldBuilder getEntityAssociationField( 
			String metadataId, JavaType entityType ) {

        ClassOrInterfaceTypeDetails entityDetails = typeLocationService.getTypeDetails(entityType);
        AnnotationMetadata annotation = entityDetails.getAnnotation( 
        		new JavaType( "org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord" )) ;
        if ( annotation == null ) 
        	return null ;
        
        String identifierColumn = (String) annotation.getAttribute( "identifierColumn").getValue();
        if ( identifierColumn == null ) {
        	return null ;
        }

		return new FieldBuilder( metadataId )
			.named(StringUtils.uncapitalize( entityType.getSimpleTypeName())) 
			.modifier( 0 )
			.type( entityType )
			.annotation("javax.persistence.ManyToOne")
			.annotation( new AnnotationBuilder( "javax.persistence.JoinColumn" )
					.attribute( "name", identifierColumn )) ;
	}
}