package com.repik.roo.entity.association.web.controller;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import com.repik.roo.builders.AnnotationBuilder;
import com.repik.roo.builders.MethodBuilder;

/**
 * This type produces metadata for a new ITD. It uses an
 * {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in
 * the ITD and a new method.
 * 
 * @since 1.1.0
 */
public class WebAssociationMetadata extends
		AbstractItdTypeDetailsProvidingMetadataItem {

	// Constants
	private static final String PROVIDES_TYPE_STRING = WebAssociationMetadata.class.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

	public static final String getMetadataIdentiferType() {
		return PROVIDES_TYPE;
	}

	private JavaType entityType ;
	private String thisClass = null;
	private String thisObject;

	public static final String createIdentifier(JavaType javaType, LogicalPath path) {
		return PhysicalTypeIdentifierNamingUtils.createIdentifier(
				PROVIDES_TYPE_STRING, javaType, path);
	}

	public static final JavaType getJavaType(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getJavaType(
				PROVIDES_TYPE_STRING, metadataIdentificationString);
	}

	public static final LogicalPath getPath(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
				metadataIdentificationString);
	}

	public static boolean isValid(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
				metadataIdentificationString);
	}

	public JavaType getEntityType() {
		return entityType;
	}

	public WebAssociationMetadata(String identifier, JavaType aspectName,
			PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaType entityType, List<JavaType> associationEntities) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);

		Validate.isTrue(isValid(identifier), "Metadata identification string '"
				+ identifier + "' does not appear to be a valid");
		
		this.entityType = entityType ;
		try {
			thisClass = entityType.getSimpleTypeName();
			thisObject = StringUtils.uncapitalize(thisClass);

		} catch (Exception e) {
			e.printStackTrace();
		}

		buildAssociationITD(entityType, associationEntities);
	}

	private void buildAssociationITD(JavaType entityType, List<JavaType> associationEntities) {
		List<JavaType> importList = new ArrayList<JavaType>() ;
		importList.add( new JavaType( "javax.servlet.http.HttpServletRequest" )) ;
		importList.add( new JavaType( "org.springframework.transaction.annotation.Transactional" )) ;
		importList.add( new JavaType( "org.springframework.ui.Model" )) ;
		importList.add( new JavaType( "org.springframework.web.bind.annotation.PathVariable" )) ;
		importList.add( new JavaType( "org.springframework.web.bind.annotation.RequestMapping" )) ;
		importList.add( new JavaType( "org.springframework.web.bind.annotation.RequestMethod" )) ;
		importList.add( new JavaType( "org.springframework.web.bind.annotation.RequestParam" )) ;
		importList.add( new JavaType( "javax.persistence.TypedQuery" )) ;
		importList.add( new JavaType( "javax.servlet.http.HttpServletRequest" )) ;
		
		importList.add( entityType ) ;
	

		// Adding a new sample method definition
		for ( JavaType associationEntity : associationEntities ) {
			importList.add( associationEntity ) ;
			
			builder.addMethod(getAssociationCreateMethod( associationEntity ));
			builder.addMethod(getAssociationDeleteMethod( associationEntity ));
			builder.addMethod(getAssociationFormMethod( associationEntity ));
			builder.addMethod(getAssociationListMethod( associationEntity ));
		}

		builder.getImportRegistrationResolver().addImports( importList );
		
		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();
	}

	private MethodMetadata getAssociationListMethod( JavaType withType ) {
		String withClass = withType.getSimpleTypeName() ;
		String withObject = StringUtils.uncapitalize(withClass) ;

		return new MethodBuilder( getId() )
			.modifier( Modifier.PUBLIC )
			.type( JavaType.STRING )
			.named( "show" + withClass )
			.parameter( JavaType.LONG_OBJECT, 		
					new AnnotationBuilder("PathVariable" )
										.attribute("value", thisObject + "Id" ),
					thisObject + "Id" )
			.parameter( new JavaType( "Model" ), "uiModel" )
			.annotation( new AnnotationBuilder( "RequestMapping" )
							.attribute("value", "/{userId}/" + withObject )
							.attribute("produces", "text/html" ))
			.body( 							
				new InvocableMemberBodyBuilder()
					.appendFormalLine( "uiModel.addAttribute(\"" + withObject + "\", " + thisClass + ".find" + withClass + "sAssociatedWith(" + thisObject + "Id).getResultList());" )  
					.appendFormalLine( "uiModel.addAttribute(\"" + thisObject + "Id\", " + thisObject + "Id);" ) 
					.appendFormalLine( "return \"" + thisObject + "/" + withObject + "s/show\";" ) ) 
			.build() ;
	}

	private MethodMetadata getAssociationFormMethod( JavaType withType) {

		String withClass = withType.getSimpleTypeName() ;
		String withObject = StringUtils.uncapitalize(withClass) ;
		
		return new MethodBuilder( getId() ) 
			.named( "add" + withClass + "Form" )
			.type( JavaType.STRING )
			.modifier( Modifier.PUBLIC )
			
			.parameter( JavaType.LONG_OBJECT, 		
					new AnnotationBuilder( "PathVariable" )
							.attribute("value", "/{" + thisObject + "Id}/" + withObject + "s" ),
					thisObject + "Id")
			.parameter( JavaType.STRING, 
					new AnnotationBuilder("RequestParam" )
							.attribute("value", "form" )
							.attribute("required", true),
					"form")
			.parameter("Model", "uiModel")

			.annotation(new AnnotationBuilder( "RequestMapping" )
						.attribute("value", "/{" + thisObject + "Id}/" + withObject)
						.attribute("params", "form")
						.attribute("produces", "text/html" ))
					
			.body(
				new InvocableMemberBodyBuilder()
					.appendFormalLine( "if ( \"add\".equals( form )) { " ) 
					.indent() 
					.appendFormalLine( "uiModel.addAttribute( \"" + withObject + "s\", " + thisClass + ".find" + withClass + "sNotAssociatedWith(" + thisObject + "Id).getResultList());" ) 
					.appendFormalLine( "uiModel.addAttribute( \"method\", \"POST\" ) ;" ) 
					.indentRemove() 
					.appendFormalLine( "} else {" ) 
					.indent() 
					.appendFormalLine( "uiModel.addAttribute( \"" + withObject + "s\", " + thisClass + ".find" + withClass + "sAssociatedWith(" + thisObject + "Id).getResultList()); " ) 
					.appendFormalLine( "uiModel.addAttribute( \"method\", \"DELETE\" ) ;" ) 
					.appendFormalLine( "}") 
					.indentRemove() 
					.appendFormalLine( "uiModel.addAttribute( \"" + thisObject + "Id\", " + thisObject + "Id); " ) 
					.appendFormalLine( "return \"appusers/organizations/edit\";" ))
			.build() ;
	}

	private MethodMetadata getAssociationCreateMethod( JavaType withType ) {

		String withClass = withType.getSimpleTypeName() ;
		String withObject = StringUtils.uncapitalize(withClass) ;

		return new MethodBuilder( getId() ) 
			.modifier( Modifier.PUBLIC )
			.type( JavaType.STRING )
			.named("add" + withClass )
			
			.parameter( "HttpServletRequest", "request" )
			.parameter( JavaType.LONG_OBJECT, 		
					new AnnotationBuilder( "PathVariable" )
							.attribute( "value", "/{" + thisObject + "Id}/" + withObject + "s" ),
					thisObject + "Id")
			.parameter( "Model", "uiModel")

			.annotation( 
					new AnnotationBuilder( "RequestMapping" )
							.attribute( "value", "/{" + thisObject + "Id}/" + withObject ) 
							.attribute( "method", new EnumDetails( new JavaType( "RequestMethod" ), new JavaSymbolName( "POST" )))
							.attribute( "produces", "text/html" ))
			.body( new InvocableMemberBodyBuilder()
					.appendFormalLine( thisClass + " " + thisObject + " = " + thisClass + ".find" + thisClass + "(" + thisObject + "Id) ;" ) 
					.appendFormalLine( "if (" + thisObject + " == null) " )  
					.indent()
					.appendFormalLine( "throw new IllegalArgumentException(\"" + thisClass + " not found\");" ) 
					.indentRemove() 
					.appendFormalLine( "String[] " + withObject + "Ids = request.getParameterValues(\"" + withObject + "Id\" ) ;" ) 
					.appendFormalLine( "for ( int i = 0 ; i < " + withObject + "Ids.length ; i++ ) { " ) 
					.indent()
					.appendFormalLine( "Long " + withObject + "Id = Long.parseLong( " + withObject + "Ids[ i ] ) ;" )  
					.appendFormalLine( thisObject + ".create" + withClass + "(" + withObject + "Id) ; " ) 
					.indentRemove() 
					.appendFormalLine( "}") 
					.appendFormalLine( "return \"redirect:/" + thisObject + "s/\" + " + thisObject + "Id + \"/" + withObject + "s\";" ))
			.build() ;
	}

	private MethodMetadata getAssociationDeleteMethod( JavaType withType ) {

		String withClass = withType.getSimpleTypeName() ;
		String withObject = StringUtils.uncapitalize(withClass) ;

		return new MethodBuilder( getId() )
			.modifier(Modifier.PUBLIC ) 
			.type(JavaType.STRING ) 
			.named( "delete" + withClass )
				
			.parameter( "HttpServletRequest", "request" )
			.parameter(JavaType.LONG_OBJECT, 		
					new AnnotationBuilder("PathVariable" )
							.attribute("value", "" + thisObject + "Id" ),
					thisObject + "Id" )
			.parameter( "Model", "uiModel" )
						
			.annotation( 
					new AnnotationBuilder( "RequestMapping" )
							.attribute("value", "/{" + thisObject + "Id}/" + withObject )
							.attribute("method", new EnumDetails( new JavaType( "RequestMethod" ), new JavaSymbolName( "DELETE" )))
							.attribute("produces", "text/html" ))
			.annotation( new AnnotationBuilder( "Transactional") )

			.body(
				new InvocableMemberBodyBuilder()
						.appendFormalLine( thisClass + " " + thisObject + " = " + thisClass + ".find" + thisClass + "(" + thisObject + "Id) ;" )
						.appendFormalLine("if (" + thisObject + " == null)" ) 
						.indent() 
						.appendFormalLine("throw new IllegalArgumentException(\"" + thisClass + " not found\");" ) 
						.indentRemove() 
			 
						.appendFormalLine("String[] " + withObject + "Ids = request.getParameterValues( \"" + withObject + "Id\" ) ;")  
						.appendFormalLine("for ( int i = 0 ; i < " + withObject + "Ids.length ; i++ ) {" ) 
						.indent()
						.appendFormalLine("Long " + withObject + "Id = Long.parseLong( " + withObject + "Ids[ i ] ) ;") 
						.appendFormalLine( thisObject + ".delete" + withClass + "(" + withObject + "Id) ;" ) 
						.indentRemove() 
			 
						.appendFormalLine( "}" ) 
						.appendFormalLine("return \"redirect:/" + thisClass.toLowerCase() + "s/\" + " + thisObject + " + \"/" + withObject + "s\";"))
			.build() ;
	}
}
