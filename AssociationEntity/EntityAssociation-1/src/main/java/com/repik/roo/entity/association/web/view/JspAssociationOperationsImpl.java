package com.repik.roo.entity.association.web.view;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;

@Component
@Service
public class JspAssociationOperationsImpl extends AbstractOperations implements JspAssociationOperations {

    @Reference private PathResolver pathResolver;
    @Reference private ProjectOperations projectOperations;

	public void installCommonArtifacts() {
		installCommonArtifacts( projectOperations.getFocusedModuleName()) ;
	}

	public void installCommonArtifacts( String moduleName ) {
        final LogicalPath webappPath = Path.SRC_MAIN_WEBAPP.getModulePathId(moduleName);
		copyDirectoryContents("multiselectTable.tagx", 
				pathResolver.getIdentifier( webappPath, "WEB-INF/tags/form/fields" ), true ) ;
	}


    /*
     * <div xmlns:jsp="http://java.sun.com/JSP/Page" 
	xmlns:form="urn:jsptagdir:/WEB-INF/tags/form" 
	xmlns:table="urn:jsptagdir:/WEB-INF/tags/form/fields" 
	version="2.0">
<jsp:directive.page contentType="text/html;charset=UTF-8"/>
<jsp:output omit-xml-declaration="yes"/>

     */
    public Document getListDocument( JavaTypeMetadataDetails entityType, String controllerPath ) {
    	
    
        final DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        final Document document = builder.newDocument();

        /*       
        // Add document namespaces
        final Element div = new XmlElementBuilder("div", document)
                .addAttribute("xmlns:form", "urn:jsptagdir:/WEB-INF/tags/form")
                .addAttribute("xmlns:table", "urn:jsptagdir:/WEB-INF/tags/form/fields")
                .addAttribute("xmlns:jsp", "http://java.sun.com/JSP/Page")
                .addAttribute("version", "2.0")
                .addChild(
                        new XmlElementBuilder("jsp:directive.page", document)
                                .addAttribute("contentType", "text/html;charset=UTF-8")
                                .build())
                .addChild(
                        new XmlElementBuilder("jsp:output", document)
                                .addAttribute("omit-xml-declaration", "yes")
                                .build())
                .build();
        document.appendChild(div);

        final Element fieldTable = new XmlElementBuilder("table:multiselectionTable", document)
                .addAttribute( 
                        "id",
                        XmlUtils.convertId("l:" + entityType.getFullyQualifiedTypeName()))
                .addAttribute(
                        "data",
                        "${" + entityType.getPlural().toLowerCase() + "}")
                .addAttribute("path", controllerPath).build();

        fieldTable.setAttribute("update", "false");
        fieldTable.setAttribute("delete", "false");
        if (!entityType.getIdentifierField()
                .getFieldName().getSymbolName().equals("id")) {
            fieldTable.setAttribute("typeIdFieldName",
                    formBackingTypePersistenceMetadata.getIdentifierField()
                            .getFieldName().getSymbolName());
        }
        fieldTable.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(fieldTable));

        int fieldCounter = 0;
        for (final FieldMetadata field : fields) {
            if (++fieldCounter < 7) {
                final Element columnElement = new XmlElementBuilder(
                        "table:column", document)
                        .addAttribute(
                                "id",
                                XmlUtils.convertId("c:"
                                        + formBackingType
                                                .getFullyQualifiedTypeName()
                                        + "."
                                        + field.getFieldName().getSymbolName()))
                        .addAttribute(
                                "property",
                                uncapitalize(field.getFieldName()
                                        .getSymbolName())).build();
                final String fieldName = uncapitalize(field.getFieldName()
                        .getSymbolName());
                if (field.getFieldType().equals(DATE)) {
                    columnElement.setAttribute("date", "true");
                    columnElement.setAttribute("dateTimePattern", "${"
                            + entityName + "_" + fieldName.toLowerCase()
                            + "_date_format}");
                }
                else if (field.getFieldType().equals(CALENDAR)) {
                    columnElement.setAttribute("calendar", "true");
                    columnElement.setAttribute("dateTimePattern", "${"
                            + entityName + "_" + fieldName.toLowerCase()
                            + "_date_format}");
                }
                else if (field.getFieldType().isCommonCollectionType()
                        && field.getCustomData().get(
                                CustomDataKeys.ONE_TO_MANY_FIELD) != null) {
                    continue;
                }
                columnElement.setAttribute("z",
                        XmlRoundTripUtils.calculateUniqueKeyFor(columnElement));
                fieldTable.appendChild(columnElement);
            }
        }
    	<form:update id="fu_com_repik_multitenant_security_domain_Organization" 
    			modelAttribute="organization" 
    			path="/organizations/userId/" 
    			versionField="Version" 
    			z="hMlBx35CwRa27QU9n6OPFtab7/Q=">

        // Create page:list element
        final Element pageList = new XmlElementBuilder("form:update", document)
        		.addAttribute( "modelAttribute", formBackingType.getSimpleName() )
                .addAttribute(
                        "id",
                        XmlUtils.convertId("ms:" + formBackingType.getFullyQualifiedTypeName()))
                .addAttribute(
                        "items",
                        "${" + formBackingTypeMetadata.getPlural().toLowerCase() + "}")
                .addChild(fieldTable).build();
        pageList.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(pageList));
        div.appendChild(pageList);
*/
        return document;
    }

    private void installView(final String path, final String viewName,
            final String title, final String category, Document document,
            final boolean registerStaticController, final LogicalPath webappPath) {
        Validate.notBlank(path, "Path required");
        Validate.notBlank(viewName, "View name required");
        Validate.notBlank(title, "Title required");
/*
        final String cleanedPath = cleanPath(path);
        final String cleanedViewName = cleanViewName(viewName);
        final String lcViewName = cleanedViewName.toLowerCase();

        
        if (document == null) {
            try {
                document = getDocumentTemplate("listSelect.jspx");
                XmlUtils.findRequiredElement("/div/message",
                        document.getDocumentElement()).setAttribute(
                        "code",
                        "label" + cleanedPath.replace("/", "_").toLowerCase()
                                + "_" + lcViewName);
            }
            catch (final Exception e) {
                throw new IllegalStateException(
                        "Encountered an error during copying of resources for controller class.",
                        e);
            }
        }
*/
    }
}
