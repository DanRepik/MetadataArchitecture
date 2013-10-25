package com.repik.roo.builders;

import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * This class defines a facade to assist in building fields for Roo.
 * 
 * @author Dan Repik
 *
 */
public class FieldBuilder extends IndentifableAssetBuilder<FieldMetadataBuilder> {

	public FieldBuilder( String metadataId ) {
		super( new FieldMetadataBuilder( metadataId )) ;
	}
	
	public FieldBuilder modifier(int modifier) {
		builder.setModifier(modifier) ;
		return this ;
	}

	public FieldBuilder named(String name) {
		builder.setFieldName( new JavaSymbolName( name )) ;
		return this ;
	}

	public FieldBuilder annotation(String type) {
		addAnnotation( new AnnotationBuilder( type ) ) ;
		return this;
	}
	
	public FieldBuilder annotation(AnnotationBuilder annotationBuilder) {
		addAnnotation( annotationBuilder ) ;
		return this;
	}
	
	public FieldBuilder type(JavaType type) {
		builder.setFieldType( type ) ;
		return this ;
	}

	public FieldBuilder type(String name) {
		builder.setFieldType( new JavaType( name )) ;
		return this ;
	}

	public FieldMetadata build() {
		return builder.build() ;
	}
}
