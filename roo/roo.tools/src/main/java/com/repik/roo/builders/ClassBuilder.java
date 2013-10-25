package com.repik.roo.builders;

import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.ImportMetadata;
import org.springframework.roo.classpath.details.ImportMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * This class acts as a facade for building classes (or interfaces) in Roo.
 * Methods generally return the instance of this class allowing those
 * methods to be chained together.
 * 
 * @author Dan Repik
 *
 */
public class ClassBuilder extends IndentifableAssetBuilder<ClassOrInterfaceTypeDetailsBuilder> {

	public ClassBuilder(String metadataId, ClassOrInterfaceTypeDetails details) {
		super(details == null ? new ClassOrInterfaceTypeDetailsBuilder(metadataId) : new ClassOrInterfaceTypeDetailsBuilder(details));
	}

	public ClassBuilder(ClassOrInterfaceTypeDetails details) {
		super(new ClassOrInterfaceTypeDetailsBuilder(details));
	}

	public ClassBuilder modifier(int modifier) {
		builder.setModifier(modifier);
		return this;
	}

	public ClassBuilder named(JavaType name) {
		builder.setName(name);
		return this;
	}

	public ClassBuilder type(PhysicalTypeCategory physicalTypeCategory) {
		builder.setPhysicalTypeCategory(physicalTypeCategory);
		return this;
	}

	/**
	 * Fields are only added if already present in the class.
	 * 
	 * @param fieldBuilder
	 * @return
	 */
	public ClassBuilder field(FieldBuilder fieldBuilder) {
		if ( fieldBuilder == null )
			return this ;
		
		FieldMetadata metadata = fieldBuilder.build();
		
		// check if the field already exists if so then do nothing
		JavaSymbolName fieldName = metadata.getFieldName() ;
		for (FieldMetadataBuilder field : builder.getDeclaredFields()) {
			if (fieldName.equals(field.getFieldName()))
				return this;
		}
		
		builder.addField(metadata);
		return this;
	}

	public ClassOrInterfaceTypeDetails build() {
		return builder.build();
	}

	public ClassBuilder annotation(String type) {
		addAnnotation(new AnnotationBuilder(type));
		return this;
	}
	
	
	public ClassBuilder method( MethodBuilder methodBuilder ) {
		if ( methodBuilder == null ) {
			return this ;
		}
		
		MethodMetadata metadata = methodBuilder.build() ;
		for ( MethodMetadataBuilder method : builder.getDeclaredMethods() ) {
			if ( metadata.getMethodName().equals( method.getMethodName()))
				return this ;
		}
		
		builder.addMethod(metadata) ;
		return this ;
	}
	
	/*
	public ClassBuilder _import( String importStr ) {
	
		
		for ( ImportMetadata _import : builder.getRegisteredImports() ) {
			if ( importStr.equals( _import.getImportType().getFullyQualifiedTypeName())) {
				return this ;
			}
		}
		
		builder.
		builder.add(new ImportMetadataBuilder( ))
		return this ;
	}

	public ClassBuilder annotation(AnnotationBuilder annotationBuilder) {
		addAnnotation(annotationBuilder);
		return this;
	}
	*/
}
