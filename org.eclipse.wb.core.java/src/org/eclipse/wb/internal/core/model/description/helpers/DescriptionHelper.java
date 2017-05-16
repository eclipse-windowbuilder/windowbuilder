/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.description.helpers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.description.IToolkitProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.resource.ClassResourceInfo;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.ResourceInfo;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Helper for accessing descriptions resources.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class DescriptionHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassResourceInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link ClassResourceInfo}'s for given component class and all its super classes and
   * interfaces.
   */
  public static void addDescriptionResources(LinkedList<ClassResourceInfo> descriptions,
      ILoadingContext context,
      Class<?> currentClass) throws Exception {
    if (currentClass != null) {
      ResourceInfo resource = getComponentDescriptionResource(context, currentClass);
      if (resource != null) {
        validateComponentDescription(resource);
        descriptions.addFirst(new ClassResourceInfo(currentClass, resource));
      }
      // handle interfaces
      for (Class<?> interfaceClass : currentClass.getInterfaces()) {
        addDescriptionResources(descriptions, context, interfaceClass);
      }
      // handle super class
      addDescriptionResources(descriptions, context, currentClass.getSuperclass());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Schema m_wbpComponentSchema;

  /**
   * Validates <code>*.wbp-component.xml</code> against its schema.
   */
  public static synchronized void validateComponentDescription(ResourceInfo resource)
      throws Exception {
    // validate on developers computers
    if (EnvironmentUtils.isTestingTime()) {
      // prepare Schema
      if (m_wbpComponentSchema == null) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        InputStream schemaStream = DesignerPlugin.getFile("schema/wbp-component.xsd");
        m_wbpComponentSchema = factory.newSchema(new StreamSource(schemaStream));
      }
      // validate
      InputStream contents = resource.getURL().openStream();
      try {
        Validator validator = m_wbpComponentSchema.newValidator();
        validator.validate(new StreamSource(contents));
      } catch (Throwable e) {
        throw new Exception("Exception during validation " + resource.getURL(), e);
      } finally {
        contents.close();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final String[] ICON_EXTS = new String[]{".png", ".gif"};

  /**
   * @return the icon {@link Image} for given component.
   */
  public static Image getIconImage(ILoadingContext context, Class<?> componentClass)
      throws Exception {
    return getIconImage(context, componentClass, StringUtils.EMPTY);
  }

  /**
   * Attempts to load icon with one of the supported extensions from description resources.
   *
   * @param context
   *          the {@link EditorState} for accessing resource from {@link ClassLoader}.
   * @param componentClass
   *          the {@link Class} of component, for example <code>"javax.swing.JButton"</code>.
   * @param suffix
   *          optional suffix, may be empty, but not <code>null</code>. We use it loading
   *          creation-specific icons.
   *
   * @return the icon {@link Image}, or <code>null</code>.
   */
  public static Image getIconImage(ILoadingContext context, Class<?> componentClass, String suffix)
      throws Exception {
    String iconPath = componentClass.getName().replace('.', '/') + suffix;
    return getIconImage(context, iconPath);
  }

  /**
   * Attempt to load icon with one of the supported extensions from description resources.
   *
   * @param context
   *          the {@link EditorState} for accessing resource from {@link ClassLoader}.
   * @param iconPath
   *          the path to icon file, without extension.
   *
   * @return the icon {@link Image}, or <code>null</code>.
   */
  public static Image getIconImage(ILoadingContext context, String iconPath) throws Exception {
    for (String ext : ICON_EXTS) {
      String iconName = iconPath + ext;
      ResourceInfo resourceInfo = getResourceInfo0(context, iconName, true);
      if (resourceInfo != null) {
        InputStream stream = resourceInfo.getURL().openStream();
        try {
          return new Image(null, stream);
        } finally {
          stream.close();
        }
      }
    }
    // not found
    return null;
  }

  /**
   * @return <code>true</code> if there is <code>.wbp-forced-toolkit.txt</code> file exactly for
   *         given component.
   */
  public static boolean hasForcedToolkitForComponent(ILoadingContext context,
      String toolkitId,
      String componentClassName) throws Exception {
    String name = componentClassName.replace('.', '/') + ".wbp-forced-toolkit.txt";
    ResourceInfo resourceInfo = getResourceInfo0(context, name, true);
    if (resourceInfo != null) {
      return IOUtils2.readString(resourceInfo.getURL().openStream()).equals(toolkitId);
    }
    return false;
  }

  /**
   * @return <code>true</code> if there is <code>.wbp-component.xml</code> file exactly for given
   *         component.
   */
  public static boolean hasComponentDescriptionResource(ILoadingContext context,
      Class<?> componentClass) throws Exception {
    return getComponentDescriptionResource(context, componentClass) != null;
  }

  /**
   * @return the {@link ResourceInfo} with description file of given component in project
   *         {@link ClassLoader}'s.
   */
  public static ResourceInfo getComponentDescriptionResource(ILoadingContext context,
      Class<?> componentClass) throws Exception {
    String name = componentClass.getName().replace('.', '/') + ".wbp-component.xml";
    return getResourceInfo(context, componentClass, name);
  }

  /**
   * Same as {@link #getResourceInfo0(EditorState, String)}, but tries first versions corresponding
   * to toolkit of given {@link Class}.
   *
   * @return the {@link ResourceInfo} for resource with given name or <code>null</code> if no such
   *         resource found.
   */
  static ResourceInfo getResourceInfo(ILoadingContext context,
      Class<?> resourceClass,
      String defaultPath) throws Exception {
    // try to find versioned path
    for (IDescriptionVersionsProvider provider : context.getDescriptionVersionsProviders()) {
      List<String> versions = provider.getVersions(resourceClass);
      for (String version : versions) {
        String versionedPath = version + "/" + defaultPath;
        ResourceInfo resource = getResourceInfo0(context, versionedPath, false);
        if (resource != null) {
          return resource;
        }
      }
    }
    // use default path XXX
    return getResourceInfo0(context, defaultPath, true);
  }

  /**
   * This method tries to find resource in following places:
   * <p>
   * <ol>
   * <li>Project {@link ClassLoader} from {@link EditorState}.
   * <li>Plugins that contribute toolkits, from "wbp-meta".
   * </ol>
   *
   * @return the {@link ResourceInfo} for resource with given name or <code>null</code> if no such
   *         resource found.
   */
  @SuppressWarnings("unchecked")
  private static ResourceInfo getResourceInfo0(ILoadingContext context,
      String name,
      boolean tryContext) throws Exception {
    // cache with results
    Map<String, ResourceInfo> cacheResult;
    {
      String key = "ComponentDescriptionHelper.getResourceInfo0.hasResult";
      cacheResult = (Map<String, ResourceInfo>) context.getGlobalValue(key);
      if (cacheResult == null) {
        cacheResult = Maps.newHashMap();
        context.putGlobalValue(key, cacheResult);
      }
      ResourceInfo result = cacheResult.get(name);
      if (result != null) {
        return result;
      }
    }
    // cache without results
    Set<String> cacheNoResult;
    {
      String key = "ComponentDescriptionHelper.getResourceInfo0.noResult";
      cacheNoResult = (Set<String>) context.getGlobalValue(key);
      if (cacheNoResult == null) {
        cacheNoResult = Sets.newHashSet();
        context.putGlobalValue(key, cacheNoResult);
      }
      if (cacheNoResult.contains(name)) {
        return null;
      }
    }
    // prepare result XXX
    ResourceInfo result = getResourceInfo00(context, name, tryContext);
    // fill caches
    if (result != null) {
      cacheResult.put(name, result);
    } else {
      cacheNoResult.add(name);
    }
    // done
    return result;
  }

  /**
   * Non-caching implementation of {@link #getResourceInfo0(EditorState, String)}.
   *
   * @param tryContext
   *          is <code>false</code> if {@link ILoadingContext} should not be used, because version
   *          was attached to the name, and custom components can not have version.
   */
  private static ResourceInfo getResourceInfo00(ILoadingContext context,
      String name,
      boolean tryContext) throws Exception {
    // check bundles with toolkits
    {
      ResourceInfo resource = getResourceInfo(name, context.getToolkitId());
      if (resource != null) {
        return resource;
      }
    }
    // may be standard resource
    for (IConfigurationElement toolkitElement : DescriptionHelper.getToolkitElements()) {
      if (hasMatchingResourcePrefix(toolkitElement, name)) {
        return null;
      }
    }
    // ask context XXX
    if (tryContext) {
      URL resource = context.getResource(name);
      if (resource != null) {
        return new ResourceInfo(null, null, resource);
      }
    }
    // no resource
    return null;
  }

  /**
   * @param toolkitElement
   *          the {@link IConfigurationElement} with toolkit description.
   * @param name
   *          the name of resource.
   * @return the {@link ResourceInfo} for resource with given name or <code>null</code> if no such
   *         resource found in given plugin.
   */
  private static ResourceInfo getResourceInfo(IConfigurationElement toolkitElement, String name)
      throws Exception {
    if (!canHaveResource(toolkitElement, name)) {
      return null;
    }
    String toolkitId = ExternalFactoriesHelper.getRequiredAttribute(toolkitElement, "id");
    Bundle bundle = ExternalFactoriesHelper.getExtensionBundle(toolkitElement);
    return getResourceInfo(toolkitId, bundle, name);
  }

  /**
   * @return <code>true</code> if toolkit {@link IConfigurationElement} has description that it may
   *         be has resource with given name. This allows us prevent checking many {@link Bundle}s
   *         and get big speed up gain.
   */
  private static boolean canHaveResource(IConfigurationElement toolkitElement, String name) {
    IConfigurationElement[] prefixElements = getResourcePrefixElements(toolkitElement);
    if (prefixElements.length != 0) {
      return hasMatchingResourcePrefix(prefixElements, name);
    }
    return true;
  }

  /**
   * @return <code>true</code> if toolkit has "resourcePrefix" which matches given name.
   */
  private static boolean hasMatchingResourcePrefix(IConfigurationElement toolkitElement, String name) {
    IConfigurationElement[] prefixElements = getResourcePrefixElements(toolkitElement);
    return hasMatchingResourcePrefix(prefixElements, name);
  }

  /**
   * @return the "resourcePrefix" elements of given toolkit.
   */
  private static IConfigurationElement[] getResourcePrefixElements(IConfigurationElement toolkitElement) {
    IConfigurationElement[] prefixContainers = toolkitElement.getChildren("resourcePrefixes");
    for (IConfigurationElement containerElement : prefixContainers) {
      return containerElement.getChildren("resourcePrefix");
    }
    return new IConfigurationElement[0];
  }

  /**
   * @return <code>true</code> if one of the prefixes matches given name.
   */
  private static boolean hasMatchingResourcePrefix(IConfigurationElement[] prefixElements,
      String name) {
    for (IConfigurationElement prefixElement : prefixElements) {
      String prefix = prefixElement.getValue().replace('.', '/');
      // direct prefix
      if (name.startsWith(prefix)) {
        return true;
      }
      // "name" can have version prefix, remove it and try again
      String nameWithoutVersion = StringUtils.substringAfter(name, "/");
      if (nameWithoutVersion.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the {@link ResourceInfo} for resource with given name or <code>null</code> if no such
   *         resource found in given plugin.
   */
  public static ResourceInfo getResourceInfo(String toolkitId, Bundle bundle, String name)
      throws Exception {
    // check that resource exists in bundle
    URL resource = bundle.getEntry("wbp-meta/" + name);
    if (resource != null) {
      ToolkitDescription toolkit = toolkitId != null ? getToolkit(toolkitId) : null;
      return new ResourceInfo(bundle, toolkit, resource);
    }
    // not found
    return null;
  }

  /**
   * @return the {@link ResourceInfo} for resource with given name or <code>null</code> if no such
   *         resource found in all plugins.
   */
  public static ResourceInfo getResourceInfo(String name) throws Exception {
    return getResourceInfo(name, null);
  }

  public static ResourceInfo getResourceInfo(String name, String activeToolkitId) throws Exception {
    // check bundles with toolkits
    List<IConfigurationElement> toolkitElements = getToolkitElements();
    // check bundles with active toolkit
    if (!StringUtils.isEmpty(activeToolkitId)) {
      for (IConfigurationElement toolkitElement : toolkitElements) {
        String toolkitId = ExternalFactoriesHelper.getRequiredAttribute(toolkitElement, "id");
        if (toolkitId.equals(activeToolkitId)) {
          ResourceInfo resourceInfo = getResourceInfo(toolkitElement, name);
          if (resourceInfo != null) {
            return resourceInfo;
          }
        }
      }
    }
    // check all bundles
    for (IConfigurationElement toolkitElement : toolkitElements) {
      ResourceInfo resourceInfo = getResourceInfo(toolkitElement, name);
      if (resourceInfo != null) {
        return resourceInfo;
      }
    }
    // not found
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Toolkit
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_TOOLKITS = "org.eclipse.wb.core.toolkits";
  private static Map<String, ToolkitDescription> m_idToToolkit;

  /**
   * @return the all {@link IConfigurationElement}'s that contribute to GUI toolkits. This is not
   *         only elements that describe GUI toolkit, but may be also contribution to toolkit
   *         palette.
   */
  public static List<IConfigurationElement> getToolkitElements() {
    return ExternalFactoriesHelper.getElements(POINT_TOOLKITS, "toolkit");
  }

  /**
   * @return the all {@link IConfigurationElement}'s that contribute to GUI toolkits. This is not
   *         only elements that describe GUI toolkit, but may be also contribution to toolkit
   *         palette.
   */
  public static List<IConfigurationElement> getToolkitElements(String toolkitId) {
    List<IConfigurationElement> toolkitElements = Lists.newArrayList();
    for (IConfigurationElement element : getToolkitElements()) {
      if (ExternalFactoriesHelper.getRequiredAttribute(element, "id").equals(toolkitId)) {
        toolkitElements.add(element);
      }
    }
    return toolkitElements;
  }

  /**
   * @return the {@link ToolkitDescription}'s for registered GUI toolkits.
   */
  public static ToolkitDescription[] getToolkits() throws Exception {
    List<ToolkitDescription> toolkits = Lists.newArrayList();
    //
    Set<String> addedToolkits = Sets.newHashSet();
    for (IConfigurationElement toolkitElement : getToolkitElements()) {
      String id = toolkitElement.getAttribute("id");
      if (!addedToolkits.contains(id)) {
        addedToolkits.add(id);
        toolkits.add(getToolkit(id));
      }
    }
    //
    return toolkits.toArray(new ToolkitDescription[toolkits.size()]);
  }

  /**
   * @return the {@link ToolkitDescription} for toolkit with given id.
   */
  public static synchronized ToolkitDescription getToolkit(String toolkitId) throws Exception {
    Assert.isNotNull(toolkitId);
    // prepare all toolkits
    if (m_idToToolkit == null) {
      m_idToToolkit = Maps.newTreeMap();
      for (IConfigurationElement toolkitElement : getToolkitElements()) {
        // ask each toolkit provider
        IConfigurationElement[] providerElements = toolkitElement.getChildren("provider");
        for (IConfigurationElement providerElement : providerElements) {
          IToolkitProvider toolkitProvider =
              (IToolkitProvider) providerElement.createExecutableExtension("class");
          // add toolkit description
          ToolkitDescription toolkitDescription = toolkitProvider.getDescription();
          m_idToToolkit.put(toolkitDescription.getId(), toolkitDescription);
        }
      }
    }
    // return required toolkit
    ToolkitDescription toolkitDescription = m_idToToolkit.get(toolkitId);
    Assert.isNotNull(toolkitDescription, "Can not find description for toolkit: " + toolkitId);
    return toolkitDescription;
  }

  /**
   * Loads {@link Class} from {@link Bundle}'s that contribute some toolkit.
   */
  public static Class<?> loadModelClass(String className) throws Exception {
    for (IConfigurationElement toolkitElement : getToolkitElements()) {
      Bundle bundle = ExternalFactoriesHelper.getExtensionBundle(toolkitElement);
      try {
        return bundle.loadClass(className);
      } catch (ClassNotFoundException e) {
      }
    }
    throw new ClassNotFoundException(className);
  }
}
