package com.repik.roo.builders;

import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * This class wraps Roo method building support into a series
 * of methods that can be changed together.  Doing this allows
 * simplier representation of method definitions.
 * 
 * @author Dan Repik
 *
 */
public class MethodBuilder extends IndentifableAssetBuilder<MethodMetadataBuilder> {

	private List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
	
	private List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

	public MethodBuilder( String metadataId ) {
		super( new MethodMetadataBuilder( metadataId )) ;
	}
	
	public MethodBuilder modifier(int modifier) {
		builder.setModifier(modifier) ;
		return this ;
	}

	public MethodBuilder type(String name) {
		builder.setReturnType( new JavaType( name )) ;
		return this ;
	}

	public MethodBuilder type( JavaType javaType ) {
		builder.setReturnType( javaType ) ;
		return this ;
	}

	public MethodBuilder parameter( AnnotatedJavaType parameterType, String parameterName ) {
		parameterTypes.add( parameterType ) ;
		parameterNames.add( new JavaSymbolName(parameterName )) ;
		return this ;
	}
	
	public MethodBuilder named(String name) {
		builder.setMethodName( new JavaSymbolName( name )) ;
		return this ;
	}

	public MethodBuilder annotation(String type) {
		addAnnotation( new AnnotationBuilder( type ) ) ;
		return this;
	}
	
	public MethodBuilder annotation(AnnotationBuilder annotationBuilder) {
		addAnnotation( annotationBuilder ) ;
		return this;
	}
	
	public MethodBuilder parameter( String parameterType, String parameterName ) {
		return parameter( new JavaType( parameterType ), parameterName ) ;
	}

	public MethodBuilder parameter( JavaType parameterType, String parameterName ) {
		return parameter( AnnotatedJavaType.convertFromJavaType( parameterType ), parameterName ) ;
	}

	public MethodBuilder parameter( JavaType parameterType, AnnotationBuilder parameterAnnotation, String parameterName )  {
		return parameter(new AnnotatedJavaType(parameterType, parameterAnnotation.build()), parameterName ) ;

	}
	
	public MethodBuilder body( InvocableMemberBodyBuilder body ) {
		builder.setBodyBuilder( body ) ;
		return this ;
	}
	
	public MethodMetadata build() {
		builder.setParameterTypes(parameterTypes) ;
		builder.setParameterNames(parameterNames) ;
		return builder.build() ;
	}
}
