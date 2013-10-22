package com.repik.roo.builders;

import org.springframework.roo.classpath.details.AbstractIdentifiableAnnotatedJavaStructureBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;

public class IndentifableAssetBuilder<BUILDER extends AbstractIdentifiableAnnotatedJavaStructureBuilder<?>> {

	protected BUILDER builder;

	public IndentifableAssetBuilder(BUILDER builder) {
		this.builder = builder;
	}

	protected void addAnnotation(AnnotationBuilder annotationBuilder) {
		AnnotationMetadata metadata = annotationBuilder.build();
		AnnotationMetadataBuilder result = builder.getDeclaredTypeAnnotation(metadata.getAnnotationType());
		if (result == null) {
			builder.addAnnotation(metadata);
		}
	}

}