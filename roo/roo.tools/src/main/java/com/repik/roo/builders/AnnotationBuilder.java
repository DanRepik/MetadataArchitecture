package com.repik.roo.builders;

import java.util.ArrayList;

import org.springframework.roo.classpath.details.annotations.AbstractAnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * This class wraps the Roo metadata builder for annotations providing methods
 * that can be chained together to define annotations.
 * 
 * @author Dan Repik
 * 
 */
public class AnnotationBuilder {
	private AnnotationMetadataBuilder annotationBuilder;

	public AnnotationBuilder(JavaType annotationType) {
		annotationBuilder = new AnnotationMetadataBuilder(annotationType);
	}

	public AnnotationBuilder(String annotationType) {
		annotationBuilder = new AnnotationMetadataBuilder(new JavaType(annotationType));
	}

	public <T extends AbstractAnnotationAttributeValue<?>> AnnotationBuilder attribute(String name, ArrayList<T> values) {
		annotationBuilder.addAttribute(new ArrayAttributeValue<T>(new JavaSymbolName(name), values));
		return this;
	}

	public AnnotationBuilder attribute(String name, Boolean value) {
		annotationBuilder.addBooleanAttribute(name, value);
		return this;
	}

	public AnnotationBuilder attribute(String name, EnumDetails value) {
		annotationBuilder.addEnumAttribute(name, value);
		return this;
	}

	public AnnotationBuilder attribute(String name, String value) {
		annotationBuilder.addStringAttribute(name, value);
		return this;
	}

	public AnnotationMetadata build() {
		return annotationBuilder.build();
	}
}