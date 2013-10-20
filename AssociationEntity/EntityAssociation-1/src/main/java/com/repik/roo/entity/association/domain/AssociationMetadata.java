package com.repik.roo.entity.association.domain;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import com.repik.roo.builders.MethodBuilder;

/**
 * This type produces metadata for a new ITD. It uses an
 * {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in
 * the ITD and a new method.
 * 
 * @since 1.1.0
 */
public class AssociationMetadata extends
		AbstractItdTypeDetailsProvidingMetadataItem {

	// Constants
	private static final String PROVIDES_TYPE_STRING = AssociationMetadata.class
			.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils
			.create(PROVIDES_TYPE_STRING);

	public static final String createIdentifier(JavaType javaType,
			LogicalPath path) {
		return PhysicalTypeIdentifierNamingUtils.createIdentifier(
				PROVIDES_TYPE_STRING, javaType, path);
	}

	public static final JavaType getJavaType(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getJavaType(
				PROVIDES_TYPE_STRING, metadataIdentificationString);
	}
	public static final String getMetadataIdentiferType() {
		return PROVIDES_TYPE;
	}
	public static final LogicalPath getPath(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
				metadataIdentificationString);
	}
	public static boolean isValid(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
				metadataIdentificationString);
	}
	private String thisClass = null;
	private String usingClass = null;
	private String withClass = null;

	private String thisObject;
	private String withObject;
	private String usingShort;
	private String withShort;

	public AssociationMetadata(String identifier, JavaType aspectName,
			PhysicalTypeMetadata governorPhysicalTypeMetadata,
			Set<JavaType> entities) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(isValid(identifier), "Metadata identification string '"
				+ identifier + "' does not appear to be a valid");

		JavaType type = governorTypeDetails.getType();
		thisClass = type.getSimpleTypeName();

		JavaType associationType = new JavaType(RooAssociation.class);
		AnnotationMetadata associationMetadata = governorTypeDetails.getAnnotation(associationType);

		AnnotationAttributeValue<List<ClassAttributeValue>> annotationValue = associationMetadata.getAttribute("value");
		List<ClassAttributeValue> associatedEntities = annotationValue.getValue() ;

		for ( ClassAttributeValue value : associatedEntities ) {
			withClass = value.getValue().getSimpleTypeName();
			usingClass = findRelationshipTable(thisClass, withClass, entities);
	
			thisObject = StringUtils.uncapitalize(thisClass);
			withObject = StringUtils.uncapitalize(withClass);
	
			usingShort = makeShort(usingClass);
			withShort = makeShort(withClass);
	
			builder.getImportRegistrationResolver().addImport(new JavaType("javax.persistence.EntityManager"));
			builder.getImportRegistrationResolver().addImport(new JavaType("javax.persistence.Query"));
			builder.getImportRegistrationResolver().addImport(new JavaType("javax.persistence.TypedQuery"));
	
			// Adding a new sample method definition
			builder.addMethod(getCreateAssociationMethod());
			builder.addMethod(getDeleteAssociationMethod());
			builder.addMethod(getFindAssociatedWithMethod());
			builder.addMethod(getFindNotAssociatedWithMethod());
	
			// Create a representation of the desired output ITD
			itdTypeDetails = builder.build();
		}
	}

	private String findRelationshipTable(String className1, String className2,
			Set<JavaType> entityTypes) {
		String testClassName = className1 + className2;
		for (JavaType entityType : entityTypes) {
			if (testClassName.equals(entityType.getSimpleTypeName()))
				return testClassName;
		}
		testClassName = className2 + className1;
		for (JavaType entityType : entityTypes) {
			if (testClassName.equals(entityType.getSimpleTypeName()))
				return testClassName;
		}
		return className2 + className1;
	}

	private MethodMetadata getCreateAssociationMethod() {

		String methodName = new String("create" + withClass);

		final MethodMetadata method = methodExists(methodName) ;
		if (method != null) {
			return method;
		}

		return new MethodBuilder( getId() )
			.modifier( Modifier.PUBLIC )
			.named( methodName )
			.type( JavaType.VOID_PRIMITIVE )
			.parameter( JavaType.LONG_OBJECT, withObject + "Id" )
			.body( new InvocableMemberBodyBuilder()
				.appendFormalLine(
						withClass + " " + withObject + " = " + withClass
								+ ".find" + withClass + "( " + withObject
								+ "Id ) ; ")
				.appendFormalLine("if (" + withObject + " == null) { ")
				.indent()
				.appendFormalLine(
						"throw new IllegalArgumentException(\"The "
								+ withObject + " argument is required\");")
				.indentRemove()
				.appendFormalLine("}")
				.appendFormalLine(
						usingClass + " " + usingShort + " = new " + usingClass
								+ "() ;")
				.appendFormalLine(
						usingShort + ".set" + withClass + "( " + withObject
								+ " ) ;")
				.appendFormalLine(
						usingShort + ".set" + thisClass + "( this ) ;")

				.appendFormalLine(usingShort + ".persist() ;"))
			.build();
	}

	private MethodMetadata getDeleteAssociationMethod() {

		String methodName = "delete" + withClass ;

		final MethodMetadata method = methodExists( methodName );
		if (method != null) {
			return method;
		}

		String parameterName = StringUtils.uncapitalize(withClass) + "Id";

		return new MethodBuilder( getId() )
			.modifier( Modifier.PUBLIC )
			.named( methodName )
			.type( JavaType.INT_OBJECT )
			.parameter( JavaType.LONG_OBJECT, parameterName )
			.body( new InvocableMemberBodyBuilder() 
				.appendFormalLine(
						"if ("
								+ parameterName
								+ " == null) throw new IllegalArgumentException(\"The "
								+ parameterName + " argument is required\");")
				.appendFormalLine(
						"EntityManager em = " + usingClass
								+ ".entityManager();")
				.appendFormalLine(
						"Query q = em.createQuery(\"" + "DELETE FROM "
								+ usingClass + " " + usingShort + " "
								+ "WHERE " + usingShort + "." + withObject
								+ ".id = :" + parameterName + " AND "
								+ usingShort + "." + thisObject + ".id = :"
								+ thisObject + "Id\" );")
				.appendFormalLine(
						"q.setParameter(\"" + parameterName + "\", "
								+ parameterName + ");")
				.appendFormalLine(
						"q.setParameter(\"" + thisObject + "Id\", id );")
				.appendFormalLine("try {").indent()
				.appendFormalLine("return q.executeUpdate();").indentRemove()
				.appendFormalLine("} catch (Exception e) {").indent()
				.appendFormalLine("e.printStackTrace();").indentRemove()
				.appendFormalLine("}").appendFormalLine("return 0 ;"))
			.build() ;
	}

	private MethodMetadata getFindAssociatedWithMethod() {

		// Specify the desired method name
		String methodName = "find" + withClass + "sAssociatedWith";

		// Check if a method with the same signature already exists in the
		// target type
		final MethodMetadata method = methodExists(methodName );
		if (method != null) {
			// If it already exists, just return the method and omit its
			// generation via the ITD
			return method;
		}

		String parameterName = thisObject + "Id";

		return new MethodBuilder(getId())
			.modifier(Modifier.PUBLIC | Modifier.STATIC)
			.named( methodName )
			.type( JavaType.getInstance("TypedQuery", 0, DataType.TYPE, null, new JavaType(withClass)))
			.parameter( JavaType.LONG_OBJECT, thisObject + "Id")
			.body( new InvocableMemberBodyBuilder()
				.appendFormalLine(
						"if ("
								+ parameterName
								+ " == null) throw new IllegalArgumentException(\"The "
								+ parameterName + " argument is required\");")
				.appendFormalLine(
						"EntityManager em = " + usingClass
								+ ".entityManager();")

				.appendFormalLine(
						"TypedQuery<" + withClass + "> q = em.createQuery(\""
								+ "SELECT " + usingShort + "." + withObject
								+ " " + "FROM " + usingClass + " " + usingShort
								+ " " + "WHERE " + usingShort + "."
								+ thisObject + ".id = :" + thisObject + "id "
								+ "\", " + withClass + ".class );")

				.appendFormalLine(
						"q.setParameter(\"" + thisObject + "id\", "
								+ thisObject + "Id );")
		 		.appendFormalLine("return q ;"))
		 	.build() ;
	}

	private MethodMetadata getFindNotAssociatedWithMethod() {

		String methodName = "find" + withClass + "sNotAssociatedWith";

		final MethodMetadata method = methodExists( methodName );
		if (method != null) {
			return method;
		}

		String parameterName = StringUtils.uncapitalize(thisClass) + "Id";

		return new MethodBuilder( getId() )
			.modifier(Modifier.PUBLIC | Modifier.STATIC )
			.named(methodName )
			.type( JavaType.getInstance("TypedQuery", 0, DataType.TYPE, null, new JavaType(withClass)))

			.parameter( JavaType.LONG_OBJECT, parameterName )
			.body( new InvocableMemberBodyBuilder()
				.appendFormalLine(
						"if ("
								+ parameterName
								+ " == null) throw new IllegalArgumentException(\"The "
								+ parameterName + " argument is required\");")
				.appendFormalLine(
						"EntityManager em = " + usingClass
								+ ".entityManager();")

				.appendFormalLine(

						"TypedQuery<" + withClass + "> q = em.createQuery(\""
								+ "SELECT " + withShort + " " + "FROM "
								+ withClass + " " + withShort + " " + "WHERE "
								+ withShort + ".id not in ( " + "SELECT "
								+ usingShort + "." + withObject + ".id "
								+ "FROM " + usingClass + " " + usingShort + " "
								+ "WHERE " + usingShort + "." + thisObject
								+ ".id = :" + thisObject + "Id )" + "\", "
								+ withClass + ".class );")
				.appendFormalLine(
						"q.setParameter(\"" + thisObject + "Id\", "
								+ thisObject + "Id);")
				.appendFormalLine("return q ;"))
			.build() ;
	}

	private String makeShort(String camelCaseStr) {

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < camelCaseStr.length(); i++) {
			if (Character.isUpperCase(camelCaseStr.charAt(i)))
				result.append(Character.toLowerCase(camelCaseStr.charAt(i)));
		}
		return result.toString();
	}

	private MethodMetadata methodExists( String methodName ) {
		// We have no access to method parameter information, so we scan by name
		// alone and treat any match as authoritative
		// We do not scan the superclass, as the caller is expected to know
		// we'll only scan the current class
		
		JavaSymbolName methodSymbol = new JavaSymbolName( methodName ) ;
		for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
			if (method.getMethodName().equals(methodSymbol)) {
				// Found a method of the expected name; we won't check method
				// parameters though
				return method;
			}
		}
		return null;
	}

	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("identifier", getId());
		builder.append("valid", valid);
		builder.append("aspectName", aspectName);
		builder.append("destinationType", destination);
		builder.append("governor", governorPhysicalTypeMetadata.getId());
		builder.append("itdTypeDetails", itdTypeDetails);
		return builder.toString();
	}
}
