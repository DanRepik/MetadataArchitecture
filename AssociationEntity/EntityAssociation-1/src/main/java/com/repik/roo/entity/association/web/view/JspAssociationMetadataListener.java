package com.repik.roo.entity.association.web.view;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.jsp.roundtrip.XmlRoundTripFileManager;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;

import com.repik.roo.entity.association.web.controller.WebAssociationMetadata;

/**
 * Listens for {@link WebScaffoldMetadata} and produces JSPs when requested by
 * that metadata.
 * 
 * @author Stefan Schmidt
 * @author Ben Alex
 * @since 1.0
 */
@Component(immediate = true)
@Service
public class JspAssociationMetadataListener implements MetadataProvider,
        MetadataNotificationListener {

    private static final String WEB_INF_VIEWS = "/WEB-INF/views/";

    @Reference private FileManager fileManager;
    // @Reference private JspOperations jspOperations;
//    @Reference private MenuOperations menuOperations;
    @Reference private MetadataDependencyRegistry metadataDependencyRegistry;
    @Reference private MetadataService metadataService;
    @Reference private ProjectOperations projectOperations;
    @Reference private PropFileOperations propFileOperations;
    @Reference private TilesOperations tilesOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private WebMetadataService webMetadataService;
    @Reference private XmlRoundTripFileManager xmlRoundTripFileManager;
    
    @Reference private JspAssociationOperations jspAssociationOperations ;

    private final Map<JavaType, String> formBackingObjectTypesToLocalMids = new HashMap<JavaType, String>();

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                WebAssociationMetadata.getMetadataIdentiferType(),
                getProvidesType());
        metadataDependencyRegistry.addNotificationListener(this);
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                WebAssociationMetadata.getMetadataIdentiferType(),
                getProvidesType());
        metadataDependencyRegistry.removeNotificationListener(this);
    }

    public MetadataItem get(final String jspMetadataId) {
    	
        // Work out the MIDs of the other metadata we depend on
        // NB: The JavaType and Path are to the corresponding web scaffold
        // controller class

        final String webAsociationMetadataKey = WebAssociationMetadata.createIdentifier(
                		JspAssociationMetadata.getJavaType(jspMetadataId),
                        JspAssociationMetadata.getPath(jspMetadataId));
        final WebAssociationMetadata webAssociationMetadata = (WebAssociationMetadata) metadataService.get(webAsociationMetadataKey);
        if (webAssociationMetadata == null || !webAssociationMetadata.isValid()) {
            // Can't get the corresponding scaffold, so we certainly don't need
            // to manage any JSPs at this time
            return null;
        }

        //	Get the web scaffold metadata to resolve the domain entity
        final String webScaffoldMetadataKey = WebScaffoldMetadata.createIdentifier(JspAssociationMetadata.getJavaType(jspMetadataId), JspAssociationMetadata.getPath(jspMetadataId));
        final WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService.get(webScaffoldMetadataKey);
  
        final LogicalPath path = JspAssociationMetadata.getPath(jspMetadataId);
        final LogicalPath webappPath = LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, path.getModule());

        installCommonViewArtifacts(webScaffoldMetadata, path, webappPath);
        
        final JavaType formBackingType = webScaffoldMetadata .getAnnotationValues().getFormBackingObject();
        final JavaType entityType = webAssociationMetadata.getEntityType();
        final MemberDetails memberDetails = webMetadataService.getMemberDetails(entityType);
        final JavaTypeMetadataDetails formBackingTypeMetadataDetails = webMetadataService
                .getJavaTypeMetadataDetails(formBackingType, memberDetails, jspMetadataId);

        /*
        final JavaTypeMetadataDetails entityTypeMetadataDetails = webMetadataService.getJavaTypeMetadataDetails(entityType, memberDetails, jspMetadataId);



        Validate.notNull(
        		entityTypeMetadataDetails,
                "Unable to obtain metadata for type " + formBackingType.getFullyQualifiedTypeName());
        formBackingObjectTypesToLocalMids.put(formBackingType, jspMetadataId);

        final SortedMap<JavaType, JavaTypeMetadataDetails> relatedTypeMd = webMetadataService.getRelatedApplicationTypeMetadata(formBackingType, memberDetails, jspMetadataId);
        final JavaTypeMetadataDetails formbackingTypeMetadata = relatedTypeMd.get(formBackingType);
        Validate.notNull(formbackingTypeMetadata,"Form backing type metadata required");

        final JavaTypePersistenceMetadataDetails formBackingTypePersistenceMetadata = formbackingTypeMetadata.getPersistenceDetails();
        if (formBackingTypePersistenceMetadata == null) {
            return null;
        }
        
        
        final List<FieldMetadata> eligibleFields = webMetadataService.getScaffoldEligibleFieldMetadata(formBackingType, memberDetails, jspMetadataId);
        if (eligibleFields.isEmpty()
                && formBackingTypePersistenceMetadata.getRooIdentifierFields().isEmpty()) {
            return null;
        }
        
        final ClassOrInterfaceTypeDetails formBackingTypeDetails = typeLocationService.getTypeDetails(formBackingType);
        final LogicalPath formBackingTypePath = PhysicalTypeIdentifier.getPath(formBackingTypeDetails.getDeclaredByMetadataId());
        metadataDependencyRegistry.registerDependency(
        		PhysicalTypeIdentifier.createIdentifier(formBackingType, formBackingTypePath),
                JspAssociationMetadata.createIdentifier(formBackingType, formBackingTypePath));
        
        // Install web artifacts only if Spring MVC config is missing
        // TODO: Remove this call when 'controller' commands are gone
//        final PathResolver pathResolver = projectOperations.getPathResolver();
        final Map<String, String> properties = new LinkedHashMap<String, String>();
		final JavaSymbolName categoryName = new JavaSymbolName(formBackingType.getSimpleTypeName());
		
        String controllerPath = webScaffoldMetadata.getAnnotationValues().getPath();
        if (controllerPath.startsWith("/")) {
            controllerPath = controllerPath.substring(1);
        }

        // Make the holding directory for this controller
        final PathResolver pathResolver = projectOperations.getPathResolver();
        final String destinationDirectory = pathResolver.getIdentifier( webappPath, WEB_INF_VIEWS + controllerPath);
        if (!fileManager.exists(destinationDirectory)) {
            fileManager.createDirectory(destinationDirectory);
        }
        else {
            final File file = new File(destinationDirectory);
            Validate.isTrue(file.isDirectory(), destinationDirectory
                    + " is a file, when a directory was expected");
        }

        installCommonViewArtifacts(webScaffoldMetadata, path, webappPath);
        /*
        buildViewJspArtifacts(webScaffoldMetadata, webappPath, controllerPath );
		buildViewMenuArtifacts(categoryName, webScaffoldMetadata, formBackingType, webappPath, controllerPath, destinationDirectory, properties);

        // Setup labels for i18n support
        final String resourceId = XmlUtils.convertId("label." + formBackingType.getFullyQualifiedTypeName().toLowerCase());
        properties.put(resourceId,
                new JavaSymbolName(formBackingType.getSimpleTypeName())
                        .getReadableSymbolName());

        Validate.notNull(
                formBackingTypeMetadataDetails,
                "Unable to obtain metadata for type "
                        + formBackingType.getFullyQualifiedTypeName());

        final String pluralResourceId = XmlUtils.convertId(resourceId + ".plural");
        final String plural = formBackingTypeMetadataDetails.getPlural();
        properties.put(pluralResourceId, new JavaSymbolName(plural).getReadableSymbolName());

        final JavaTypePersistenceMetadataDetails javaTypePersistenceMetadataDetails = formBackingTypeMetadataDetails
                .getPersistenceDetails();
        Validate.notNull(javaTypePersistenceMetadataDetails,
                "Unable to determine persistence metadata for type "
                        + formBackingType.getFullyQualifiedTypeName());

        for (final FieldMetadata idField : javaTypePersistenceMetadataDetails.getRooIdentifierFields()) {
            properties.put(
                    XmlUtils.convertId(resourceId
                            + "."
                            + javaTypePersistenceMetadataDetails
                                    .getIdentifierField().getFieldName()
                                    .getSymbolName()
                            + "."
                            + idField.getFieldName().getSymbolName()
                                    .toLowerCase()), idField.getFieldName()
                            .getReadableSymbolName());
        }

        for (final MethodMetadata method : memberDetails.getMethods()) {
            if (!BeanInfoUtils.isAccessorMethod(method)) {
                continue;
            }

            final FieldMetadata field = BeanInfoUtils.getFieldForJavaBeanMethod(memberDetails, method);
            if (field == null) {
                continue;
            }
            
            final JavaSymbolName fieldName = field.getFieldName();
            final String fieldResourceId = XmlUtils.convertId(resourceId + "."+ fieldName.getSymbolName().toLowerCase());
            
            if (typeLocationService.isInProject(method.getReturnType())
                    && webMetadataService.isRooIdentifier(method.getReturnType(), 
                    		webMetadataService.getMemberDetails(method.getReturnType()))) {
            	
                final JavaTypePersistenceMetadataDetails typePersistenceMetadataDetails = 
                		webMetadataService.getJavaTypePersistenceMetadataDetails(
                        		method.getReturnType(), 
                                webMetadataService.getMemberDetails(method.getReturnType()),
                                jspMetadataId);
                if (typePersistenceMetadataDetails != null) {
                
                	for (final FieldMetadata f : typePersistenceMetadataDetails.getRooIdentifierFields()) {
                        final String sb = f.getFieldName().getReadableSymbolName();
                        properties.put(
                                XmlUtils.convertId(resourceId
                                        + "."
                                        + javaTypePersistenceMetadataDetails.getIdentifierField().getFieldName().getSymbolName()
                                        + "."
                                        + f.getFieldName().getSymbolName().toLowerCase()),
                                StringUtils.isNotBlank(sb) ? sb : fieldName.getSymbolName());
                    }
                }
            }
            else if (!method.getMethodName().equals(javaTypePersistenceMetadataDetails.getIdentifierAccessorMethod().getMethodName())
                    || javaTypePersistenceMetadataDetails.getVersionAccessorMethod() != null
                    && !method.getMethodName().equals(javaTypePersistenceMetadataDetails.getVersionAccessorMethod().getMethodName())) {
                final String sb = fieldName.getReadableSymbolName();
                properties.put(fieldResourceId, StringUtils.isNotBlank(sb) ? sb : fieldName.getSymbolName());
            }
        }

        if (javaTypePersistenceMetadataDetails.getFindAllMethod() != null) {
            // Add 'list all' menu item
            final JavaSymbolName listMenuItemId = new JavaSymbolName("list");
            menuOperations
                    .addMenuItem(
                            categoryName,
                            listMenuItemId,
                            "global_menu_list",
                            "/"
                                    + controllerPath
                                    + "?page=1&size=${empty param.size ? 10 : param.size}",
                            MenuOperations.DEFAULT_MENU_ITEM_PREFIX, webappPath);
            properties.put("menu_item_"
                    + categoryName.getSymbolName().toLowerCase() + "_"
                    + listMenuItemId.getSymbolName().toLowerCase() + "_label",
                    new JavaSymbolName(plural).getReadableSymbolName());
        }
        else {
            menuOperations.cleanUpMenuItem(categoryName, new JavaSymbolName(
                    "list"), MenuOperations.DEFAULT_MENU_ITEM_PREFIX,
                    webappPath);
        }

        final String controllerPhysicalTypeId = PhysicalTypeIdentifier
                .createIdentifier(JspAssociationMetadata.getJavaType(jspMetadataId),
                        JspAssociationMetadata.getPath(jspMetadataId));
        final PhysicalTypeMetadata controllerPhysicalTypeMd = (PhysicalTypeMetadata) metadataService.get(controllerPhysicalTypeId);
        if (controllerPhysicalTypeMd == null) {
            return null;
        }
        
        if (controllerPhysicalTypeMd.getMemberHoldingTypeDetails() == null) {
            return null;
        }
        
        */
//        propFileOperations.addProperties(webappPath,"WEB-INF/i18n/application.properties", properties, true, false);

        return new JspAssociationMetadata(jspMetadataId, webScaffoldMetadata);
    }

	private void installCommonViewArtifacts(final WebScaffoldMetadata webScaffoldMetadata, final LogicalPath path, final LogicalPath webappPath) {
		jspAssociationOperations.installCommonArtifacts() ;
	}

    public String getProvidesType() {
        return JspAssociationMetadata.getMetadataIdentiferType();
    }

    /*
	private JavaSymbolName buildViewMenuArtifacts(JavaSymbolName categoryName, final WebScaffoldMetadata webScaffoldMetadata, final JavaType formBackingType, final LogicalPath webappPath, String controllerPath, final String destinationDirectory, final Map<String, String> properties) {
        final JavaSymbolName newMenuItemId = new JavaSymbolName("new");


        if (webScaffoldMetadata.getAnnotationValues().isCreate()) {
            final String listPath = destinationDirectory + "/create.jspx";
            // Add 'create new' menu item
            menuOperations.addMenuItem(categoryName, newMenuItemId,
                    "global_menu_new", "/" + controllerPath + "?form",
                    MenuOperations.DEFAULT_MENU_ITEM_PREFIX, webappPath);
        }
        else {
            menuOperations.cleanUpMenuItem(categoryName, new JavaSymbolName("new"),
                            MenuOperations.DEFAULT_MENU_ITEM_PREFIX, webappPath);
        }
        
        properties.put("menu_category_"
                + categoryName.getSymbolName().toLowerCase() + "_label",
                categoryName.getReadableSymbolName());

        properties.put("menu_item_"
                + categoryName.getSymbolName().toLowerCase() + "_"
                + newMenuItemId.getSymbolName().toLowerCase() + "_label",
                new JavaSymbolName(formBackingType.getSimpleTypeName())
                        .getReadableSymbolName());
		return categoryName;
	}

	private void buildViewJspArtifacts(final WebScaffoldMetadata webScaffoldMetadata, final LogicalPath webappPath, String controllerPath) {
/*
		final JspViewManager viewManager = new JspViewManager(eligibleFields,
                webScaffoldMetadata.getAnnotationValues(), relatedTypeMd);

		// By now we have a directory to put the JSPs inside
        // Make the holding directory for this controller
        final PathResolver pathResolver = projectOperations.getPathResolver();
        final String destinationDirectory = pathResolver.getIdentifier( webappPath, WEB_INF_VIEWS + controllerPath);
        if (!fileManager.exists(destinationDirectory)) {
            fileManager.createDirectory(destinationDirectory);
        }
        else {
            final File file = new File(destinationDirectory);
            Validate.isTrue(file.isDirectory(), destinationDirectory
                    + " is a file, when a directory was expected");
        }

        final String listPath1 = destinationDirectory + "/list.jspx";
//        xmlRoundTripFileManager.writeToDiskIfNecessary(listPath1, viewManager.getListDocument());
        tilesOperations.addViewDefinition(controllerPath, webappPath,
                controllerPath + "/" + "list",
                TilesOperations.DEFAULT_TEMPLATE, WEB_INF_VIEWS
                        + controllerPath + "/list.jspx");

        final String showPath = destinationDirectory + "/show.jspx";
//        xmlRoundTripFileManager.writeToDiskIfNecessary(showPath, viewManager.getShowDocument());
        tilesOperations.addViewDefinition(controllerPath, webappPath,
                controllerPath + "/" + "show",
                TilesOperations.DEFAULT_TEMPLATE, WEB_INF_VIEWS
                        + controllerPath + "/show.jspx");
        
        if (webScaffoldMetadata.getAnnotationValues().isCreate()) {
            final String listPath = destinationDirectory + "/create.jspx";
//            xmlRoundTripFileManager.writeToDiskIfNecessary(listPath, viewManager.getCreateDocument());
            tilesOperations.addViewDefinition(controllerPath, webappPath,
                    controllerPath + "/" + "create",
                    TilesOperations.DEFAULT_TEMPLATE, WEB_INF_VIEWS
                            + controllerPath + "/create.jspx");
        }
        else {
            tilesOperations.removeViewDefinition(controllerPath + "/"
                    + "create", controllerPath, webappPath);
        }

        if (webScaffoldMetadata.getAnnotationValues().isUpdate()) {
            final String listPath = destinationDirectory + "/update.jspx";
//            xmlRoundTripFileManager.writeToDiskIfNecessary(listPath, viewManager.getUpdateDocument());
            tilesOperations.addViewDefinition(controllerPath, webappPath,
                    controllerPath + "/" + "update",
                    TilesOperations.DEFAULT_TEMPLATE, WEB_INF_VIEWS
                            + controllerPath + "/update.jspx");
        }
        else {
            tilesOperations.removeViewDefinition(controllerPath + "/"
                    + "update", controllerPath, webappPath);
        }


	}

    private void installImage(final LogicalPath path, final String imagePath) {
        final PathResolver pathResolver = projectOperations.getPathResolver();
        final String imageFile = pathResolver.getIdentifier(path, imagePath);
        if (!fileManager.exists(imageFile)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(), imagePath);
                outputStream = fileManager.createFile(
                        pathResolver.getIdentifier(path, imagePath))
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final Exception e) {
                throw new IllegalStateException(
                        "Encountered an error during copying of resources for MVC JSP addon.",
                        e);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }
    */

    public void notify(final String upstreamDependency, String downstreamDependency) {
    	
    	System.out.println( "notify: " + upstreamDependency + " ---> " + downstreamDependency ) ;
    	
        if (MetadataIdentificationUtils.isIdentifyingClass(downstreamDependency)) {
        	
            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            if (WebAssociationMetadata.isValid(upstreamDependency)) {
                final JavaType javaType = WebAssociationMetadata.getJavaType(upstreamDependency);
                final LogicalPath path = WebAssociationMetadata.getPath(upstreamDependency);
                downstreamDependency = JspAssociationMetadata.createIdentifier(javaType, path);
            }

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (metadataDependencyRegistry.getDownstream(upstreamDependency).contains(downstreamDependency)) {
                return;
            }
        }
        else if (MetadataIdentificationUtils.isIdentifyingInstance(upstreamDependency)) {
            // This is the generic fallback listener, ie from
            // MetadataDependencyRegistry.addListener(this) in the activate()
            // method

            // Get the metadata that just changed
            final MetadataItem metadataItem = metadataService.get(upstreamDependency);

            // We don't have to worry about physical type metadata, as we
            // monitor the relevant .java once the DOD governor is first
            // detected
            if (metadataItem == null
                    || !metadataItem.isValid()
                    || !(metadataItem instanceof ItdTypeDetailsProvidingMetadataItem)) {
                // There's something wrong with it or it's not for an ITD, so
                // let's gracefully abort
                return;
            }

            // Let's ensure we have some ITD type details to actually work with
            final ItdTypeDetailsProvidingMetadataItem itdMetadata = (ItdTypeDetailsProvidingMetadataItem) metadataItem;
            final ItdTypeDetails itdTypeDetails = itdMetadata.getMemberHoldingTypeDetails();
            if (itdTypeDetails == null) {
                return;
            }

            final String localMid = formBackingObjectTypesToLocalMids
                    .get(itdTypeDetails.getGovernor().getName());
            if (localMid != null) {
                metadataService.evictAndGet(localMid);
            }
            return;
        }

        if (MetadataIdentificationUtils.isIdentifyingInstance(downstreamDependency)) {
        	System.out.println( "evictAndGet: " + downstreamDependency ) ;
            metadataService.evictAndGet(downstreamDependency);
        }
    }
}