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
package org.eclipse.wb.internal.core.xml.model.description;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.ILoadingContext;
import org.eclipse.wb.internal.core.model.description.resource.ClassResourceInfo;
import org.eclipse.wb.internal.core.model.description.resource.ResourceInfo;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.ui.ImageDisposer;
import org.eclipse.wb.internal.core.xml.IExceptionConstants;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.description.internal.AbstractConfigurableDescription;
import org.eclipse.wb.internal.core.xml.model.description.rules.ConfigurableObjectListParameterRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.ConfigurableObjectParameterRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.CreatePropertiesFieldRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.CreatePropertiesPropertyDescriptorRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.ModelClassRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.PropertiesCategoryRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.PropertiesNoDefaultValueRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.PropertyAccessRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.PropertyCategoryRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.PropertyDefaultRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.PropertyEditorRule;
import org.eclipse.wb.internal.core.xml.model.description.rules.PropertyTagRule;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.impl.NoOpLog;
import org.osgi.framework.Bundle;
import org.xml.sax.Attributes;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper for loading {@link ComponentDescription}.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class ComponentDescriptionHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ClassMap<ComponentDescription> m_getDescription_Class = ClassMap.create();

  /**
   * @return the {@link ComponentDescription} for {@link Class} with given name.
   */
  public static ComponentDescription getDescription(EditorContext context, String componentClassName)
      throws Exception {
    Class<?> componentClass = context.getClassLoader().loadClass(componentClassName);
    return getDescription(context, componentClass);
  }

  /**
   * @return the {@link ComponentDescription} for given {@link Class}.
   */
  public static ComponentDescription getDescription(EditorContext context, Class<?> componentClass)
      throws Exception {
    ComponentDescription description = m_getDescription_Class.get(componentClass);
    if (description == null) {
      description = getDescription0(context, componentClass);
      m_getDescription_Class.put(componentClass, description);
    }
    return description;
  }

  /**
   * Implementation for {@link #getDescription(EditorContext, Class)}.
   */
  private static ComponentDescription getDescription0(EditorContext context, Class<?> componentClass)
      throws Exception {
    try {
      return getDescriptionEx(context, componentClass);
    } catch (Throwable e) {
      throw new DesignerException(IExceptionConstants.DESCRIPTION_LOADING,
          e,
          componentClass.getName());
    }
  }

  private static ComponentDescription getDescriptionEx(EditorContext context,
      Class<?> componentClass) throws Exception {
    ComponentDescription componentDescription = new ComponentDescription(componentClass);
    // prepare description resources, from generic to specific
    LinkedList<ClassResourceInfo> descriptionInfos;
    {
      descriptionInfos = Lists.newLinkedList();
      DescriptionHelper.addDescriptionResources(
          descriptionInfos,
          context.getLoadingContext(),
          componentClass);
      Assert.isTrueException(
          !descriptionInfos.isEmpty(),
          IExceptionConstants.DESCRIPTION_NO_DESCRIPTIONS,
          componentClass.getName());
    }
    // prepare Digester
    Digester digester;
    {
      digester = new Digester();
      digester.setLogger(new NoOpLog());
      addRules(digester, context, componentClass);
    }
    // read descriptions from generic to specific
    for (ClassResourceInfo descriptionInfo : descriptionInfos) {
      ResourceInfo resourceInfo = descriptionInfo.resource;
      // read next description
      {
        //componentDescription.setCurrentClass(descriptionInfo.clazz);
        digester.push(componentDescription);
        // do parse
        InputStream is = resourceInfo.getURL().openStream();
        try {
          digester.parse(is);
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
      // clear parts that can not be inherited
      if (descriptionInfo.clazz == componentClass) {
        setDescriptionWithInnerTags(componentDescription, resourceInfo);
      } else {
        componentDescription.clearCreations();
        componentDescription.setDescription(null);
      }
    }
    // set toolkit
    if (componentDescription.getToolkit() == null) {
      for (int i = descriptionInfos.size() - 1; i >= 0; i--) {
        ClassResourceInfo descriptionInfo = descriptionInfos.get(i);
        ToolkitDescription toolkit = descriptionInfo.resource.getToolkit();
        if (toolkit != null) {
          componentDescription.setToolkit(toolkit);
          break;
        }
      }
    }
    // mark for caching presentation
    if (shouldCachePresentation(descriptionInfos.getLast(), componentClass)) {
      componentDescription.setPresentationCached(true);
    }
    // final operations
    setIcon(context, componentDescription, componentClass);
    useDescriptionProcessors(context, componentDescription);
    componentDescription.postProcess();
    // done
    return componentDescription;
  }

  /**
   * Sets icon for {@link ComponentDescription}.
   */
  private static void setIcon(EditorContext context,
      ComponentDescription componentDescription,
      Class<?> currentClass) throws Exception {
    if (currentClass != null) {
      // check current Class
      if (componentDescription.getIcon() == null) {
        Image icon = DescriptionHelper.getIconImage(context.getLoadingContext(), currentClass);
        if (icon != null) {
          componentDescription.setIcon(icon);
          {
            String name = componentDescription.getComponentClass().getName();
            ImageDisposer.add(componentDescription, name, icon);
          }
          return;
        }
      }
      // check super Class
      if (componentDescription.getIcon() == null) {
        setIcon(context, componentDescription, currentClass.getSuperclass());
      }
    }
  }

  /**
   * Uses all {@link IDescriptionProcessor}s.
   */
  private static void useDescriptionProcessors(EditorContext context,
      ComponentDescription componentDescription) throws Exception {
    List<IDescriptionProcessor> processors =
        ExternalFactoriesHelper.getElementsInstances(
            IDescriptionProcessor.class,
            "org.eclipse.wb.core.xml.descriptionProcessors",
            "processor");
    for (IDescriptionProcessor processor : processors) {
      processor.process(context, componentDescription);
    }
  }

  /**
   * Usually XML parsers don't allow to get content of element with all inner tags, but we want
   * these tags for description. So, we need special way to get description. Right now it is not
   * very accurate, but may be will enough for practical purposes.
   */
  private static void setDescriptionWithInnerTags(ComponentDescription componentDescription,
      ResourceInfo resourceInfo) throws Exception {
    InputStream stream = resourceInfo.getURL().openStream();
    String string = IOUtils2.readString(stream);
    String description = StringUtils.substringBetween(string, "<description>", "</description>");
    if (description != null) {
      componentDescription.setDescription(description);
    }
  }

  private static boolean shouldCachePresentation(ClassResourceInfo descriptionInfo,
      Class<?> componentClass) throws Exception {
    if (descriptionInfo.clazz == componentClass) {
      Bundle bundle = descriptionInfo.resource.getBundle();
      if (bundle != null) {
        return bundle.getEntry("wbp-meta/.wbp-cache-presentations") != null;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rules
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link Rule}'s required for {@link ComponentDescription} parsing.
   */
  private static void addRules(Digester digester, EditorContext context, Class<?> componentClass) {
    digester.addRule("component/x-model", new ModelClassRule());
    // properties
    addPropertiesRules(digester, context);
    // creations
    addCreationRules(digester, context, "component/creation", "addCreation");
    // untyped parameters
    {
      String pattern = "component/parameters/parameter";
      digester.addCallMethod(pattern, "addParameter", 2);
      digester.addCallParam(pattern, 0, "name");
      digester.addCallParam(pattern, 1);
    }
    // add dynamic rules
    {
      List<IDescriptionRulesProvider> providers =
          ExternalFactoriesHelper.getElementsInstances(
              IDescriptionRulesProvider.class,
              "org.eclipse.wb.core.xml.descriptionRulesProviders",
              "provider");
      for (IDescriptionRulesProvider provider : providers) {
        provider.addRules(digester, context, componentClass);
      }
    }
  }

  /**
   * Adds {@link Rule}'s for changing {@link GenericPropertyDescription}'s.
   */
  private static void addPropertiesRules(Digester digester, EditorContext context) {
    digester.addRule(
        "component/standard-bean-properties",
        new CreatePropertiesPropertyDescriptorRule());
    digester.addRule("component/public-field-properties", new CreatePropertiesFieldRule());
    digester.addRule(
        "component/properties-preferred",
        new PropertiesCategoryRule(PropertyCategory.PREFERRED));
    digester.addRule(
        "component/properties-normal",
        new PropertiesCategoryRule(PropertyCategory.NORMAL));
    digester.addRule(
        "component/properties-advanced",
        new PropertiesCategoryRule(PropertyCategory.ADVANCED));
    digester.addRule(
        "component/properties-hidden",
        new PropertiesCategoryRule(PropertyCategory.HIDDEN));
    digester.addRule("component/properties-noDefaultValue", new PropertiesNoDefaultValueRule());
    digester.addRule("component/property-tag", new PropertyTagRule());
    // configure
    {
      String propertyAccessPattern = "component/property";
      digester.addRule(propertyAccessPattern, new PropertyAccessRule());
      addPropertyConfigurationRules(digester, context, propertyAccessPattern);
    }
  }

  /**
   * Adds {@link Rule}'s for configuring {@link GenericPropertyDescription} on stack.
   */
  private static void addPropertyConfigurationRules(Digester digester,
      EditorContext context,
      String propertyAccessPattern) {
    // category
    {
      String pattern = propertyAccessPattern + "/category";
      digester.addRule(pattern, new PropertyCategoryRule());
    }
    // editor
    {
      String pattern = propertyAccessPattern + "/editor";
      digester.addRule(pattern, new PropertyEditorRule(context));
      addConfigurableObjectParametersRules(digester, pattern);
    }
    // defaultValue
    {
      String pattern = propertyAccessPattern + "/defaultValue";
      ClassLoader classLoader = context.getClassLoader();
      digester.addRule(pattern, new PropertyDefaultRule(classLoader));
    }
    /*// getter
    {
    	String pattern = propertyAccessPattern + "/getter";
    	digester.addRule(pattern, new PropertyGetterRule());
    }*/
  }

  /**
   * Adds {@link Rule}'s for configuring {@link AbstractConfigurableDescription}.
   */
  public static void addConfigurableObjectParametersRules(Digester digester, String pattern) {
    digester.addRule(pattern + "/parameter", new ConfigurableObjectParameterRule());
    digester.addRule(pattern + "/parameter-list", new ConfigurableObjectListParameterRule());
  }

  /**
   * Adds {@link Rule}'s for parsing {@link CreationDescription}'s.
   */
  private static void addCreationRules(Digester digester,
      final EditorContext context,
      String basePattern,
      String setCreationMethod) {
    digester.addFactoryCreate(basePattern, new AbstractObjectCreationFactory() {
      @Override
      public Object createObject(Attributes attributes) throws Exception {
        ComponentDescription componentDescription = (ComponentDescription) getDigester().peek();
        // prepare creation
        String id = attributes.getValue("id");
        String name = attributes.getValue("name");
        CreationDescription creation = new CreationDescription(componentDescription, id, name);
        // set optional specific icon
        if (id != null) {
          Class<?> componentClass = componentDescription.getComponentClass();
          String suffix = "_" + id;
          Image icon = getIcon(context, componentClass, suffix);
          creation.setIcon(icon);
        }
        // OK, configured creation
        return creation;
      }
    });
    digester.addSetNext(basePattern, setCreationMethod);
    // description
    {
      String pattern = basePattern + "/description";
      digester.addCallMethod(pattern, "setDescription", 1);
      digester.addCallParam(pattern, 0);
    }
    // attribute
    {
      String pattern = basePattern + "/x-attribute";
      digester.addFactoryCreate(pattern, new AbstractObjectCreationFactory() {
        @Override
        public Object createObject(Attributes attributes) throws Exception {
          String space = attributes.getValue("ns");
          String name = attributes.getValue("name");
          String value = attributes.getValue("value");
          return new CreationAttributeDescription(space, name, value);
        }
      });
      digester.addSetNext(pattern, "addAttribute");
    }
    // content
    {
      String pattern = basePattern + "/x-content";
      digester.addCallMethod(pattern, "setContent", 1);
      digester.addCallParam(pattern, 0);
    }
    // untyped parameters
    {
      String pattern = basePattern + "/parameter";
      digester.addCallMethod(pattern, "addParameter", 2);
      digester.addCallParam(pattern, 0, "name");
      digester.addCallParam(pattern, 1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} which is in same place as icon of component, but has "suffix" in
   *         name.
   */
  public static Image getIcon(EditorContext context, Class<?> componentClass, String suffix)
      throws Exception {
    ILoadingContext loadingContext = context.getLoadingContext();
    return DescriptionHelper.getIconImage(loadingContext, componentClass, suffix);
  }
}
