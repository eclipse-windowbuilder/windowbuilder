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
package org.eclipse.wb.internal.core.utils.external;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.BundleResourceProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.osgi.framework.Bundle;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper for accessing external factories contributed via extension points.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class ExternalFactoriesHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ExternalFactoriesHelper() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle class loading
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String CLASS_LOADING_CONTRIBUTORS =
      "org.eclipse.wb.core.classLoadingContributor";

  /**
   * @param className
   *          the name of {@link Class} to load.
   *
   * @return the {@link Class} loaded from this {@link ClassLoader} or from {@link Bundle} that
   *         specifies that it can load class from some namespace.
   */
  public static Class<?> loadBundleClass(String className) throws ClassNotFoundException {
    try {
      List<IConfigurationElement> contributors =
          getElements(CLASS_LOADING_CONTRIBUTORS, "contributor");
      for (IConfigurationElement element : contributors) {
        String namespace = getRequiredAttribute(element, "namespace");
        if (className.contains(namespace)) {
          try {
            return getExtensionBundle(element).loadClass(className);
          } catch (Throwable e) {
          }
        }
      }
    } catch (Throwable e) {
      throw new ClassNotFoundException("Exception during loading class " + className, e);
    }
    return Class.forName(className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Caching and reloading
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<String, List<IExtension>> m_extensions = Maps.newHashMap();
  private static Map<String, Map<String, List<IConfigurationElement>>> m_configurationElements =
      Maps.newHashMap();
  private static Map<String, Map<String, List<?>>> m_configurationObjects = Maps.newHashMap();

  /**
   * Clears caches in this helper.
   */
  private static synchronized void clearCache(String pointId) {
    m_extensions.remove(pointId);
    m_configurationElements.remove(pointId);
    m_configurationObjects.remove(pointId);
  }

  /**
   * {@link IRegistryChangeListener} for tracking changes for interesting extensions.
   */
  private static final IRegistryChangeListener m_descriptionProcessorsListener =
      new IRegistryChangeListener() {
        public void registryChanged(IRegistryChangeEvent event) {
          for (IExtensionDelta extensionDelta : event.getExtensionDeltas()) {
            String pointId = extensionDelta.getExtensionPoint().getUniqueIdentifier();
            clearCache(pointId);
          }
        }
      };
  /**
   * Install {@link IRegistryChangeListener}.
   */
  static {
    Platform.getExtensionRegistry().addRegistryChangeListener(m_descriptionProcessorsListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurationElement's access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return instances for objects contributed to specified extension point.
   * <p>
   * Objects are sorted by their optional "priority" attribute (descending). If "priority" is
   * absent, "0" is used.
   *
   * @param clazz
   *          the {@link Class} of elements instances.
   * @param pointId
   *          the qualified id of extension, e.g. <code>"org.eclipse.core.builders"</code>.
   * @param elementName
   *          the name of element inside of extension, e.g. <code>"builder"</code>.
   *
   * @return the instances for objects contributed to specified extension point.
   */
  @SuppressWarnings("unchecked")
  public static synchronized <T> List<T> getElementsInstances(Class<T> clazz,
      String pointId,
      String elementName) {
    // prepare: elementName -> List<?>
    Map<String, List<?>> elementName_to_objects = m_configurationObjects.get(pointId);
    if (elementName_to_objects == null) {
      elementName_to_objects = Maps.newHashMap();
      m_configurationObjects.put(pointId, elementName_to_objects);
    }
    // check for cached: List<?>
    List<T> objects = (List<T>) elementName_to_objects.get(elementName);
    if (objects == null) {
      objects = Lists.newArrayList();
      elementName_to_objects.put(elementName, objects);
      List<IConfigurationElement> elements = getElements(pointId, elementName);
      // create object
      for (IConfigurationElement element : elements) {
        T object = ExternalFactoriesHelper.<T>createExecutableExtension(element, "class");
        objects.add(object);
      }
    }
    // OK, objects created
    return objects;
  }

  /**
   * @return the result of {@link IConfigurationElement#createExecutableExtension(String)}.
   */
  @SuppressWarnings("unchecked")
  public static synchronized <T> T createExecutableExtension(final IConfigurationElement element,
      final String classAttributeName) {
    return ExecutionUtils.runObject(new RunnableObjectEx<T>() {
      public T runObject() throws Exception {
        Bundle extensionBundle = getExtensionBundle(element);
        String className = getRequiredAttribute(element, classAttributeName);
        Class<?> clazz = extensionBundle.loadClass(className);
        // try to find singleton INSTANCE
        {
          Field instanceField = ReflectionUtils.getFieldByName(clazz, "INSTANCE");
          if (instanceField != null && instanceField.getDeclaringClass() == clazz) {
            return (T) instanceField.get(null);
          }
        }
        // well, create new instance
        return (T) element.createExecutableExtension(classAttributeName);
      }
    });
  }

  /**
   * Returns {@link IConfigurationElement}'s, contributed to extension point.<br>
   *
   * @param pointId
   *          the qualified id of extension, e.g. <code>"org.eclipse.core.resources.builders"</code>
   *          .
   * @param elementName
   *          the name of element inside of extension, e.g. <code>"builder"</code>.
   *
   * @return {@link IConfigurationElement}'s of all elements for specified extension point and
   *         element name.
   */
  public static synchronized List<IConfigurationElement> getElements(String pointId,
      String elementName) {
    // prepare: elementName -> List<IConfigurationElement>
    Map<String, List<IConfigurationElement>> elementName_to_elements =
        m_configurationElements.get(pointId);
    if (elementName_to_elements == null) {
      elementName_to_elements = Maps.newHashMap();
      m_configurationElements.put(pointId, elementName_to_elements);
    }
    // check for cached: List<IConfigurationElement>
    List<IConfigurationElement> elements = elementName_to_elements.get(elementName);
    if (elements == null) {
      elements = Lists.newArrayList();
      elementName_to_elements.put(elementName, elements);
      // load elements
      for (IExtension extension : getExtensions(pointId)) {
        for (IConfigurationElement element : extension.getConfigurationElements()) {
          if (elementName.equals(element.getName())) {
            elements.add(element);
          }
        }
      }
      // sort
      sortByPriority(elements);
    }
    // OK, we loaded: List<IConfigurationElement>
    removeInvalidElements(elements);
    return elements;
  }

  /**
   * {@link IConfigurationElement} may become invalid when their {@link IExtension} was removed, or
   * even when corresponding {@link Bundle} was uninstalled.
   */
  private static void removeInvalidElements(List<IConfigurationElement> elements) {
    for (Iterator<IConfigurationElement> I = elements.iterator(); I.hasNext();) {
      IConfigurationElement element = I.next();
      if (!isValid(element)) {
        I.remove();
      }
    }
  }

  private static boolean isValid(IConfigurationElement element) {
    if (!element.isValid()) {
      return false;
    }
    if (element.getParent() instanceof IExtension) {
      IExtension extension = (IExtension) element.getParent();
      return extension.isValid();
    }
    return isValid((IConfigurationElement) element.getParent());
  }

  private static void sortByPriority(List<IConfigurationElement> elements) {
    Collections.sort(elements, new Comparator<IConfigurationElement>() {
      public int compare(IConfigurationElement o1, IConfigurationElement o2) {
        return getPriority(o2) - getPriority(o1);
      }

      private int getPriority(IConfigurationElement element) {
        String priorityString = element.getAttribute("priority");
        return priorityString == null ? 0 : Integer.parseInt(priorityString);
      }
    });
  }

  /**
   * Finds extension with given point ID and extension ID.
   *
   * @param pointId
   *          the ID of extension point to find extension for.
   * @param extensionId
   *          the qualified ID of extension, such as
   *          <code>"com.google.gdt.eclipse.designer.GWTNature"</code>.
   *
   * @return the {@link IConfigurationElement} for found extension, or <code>null</code> if not
   *         found.
   */
  public static synchronized IExtension getExtension(String pointId, String extensionId) {
    for (IExtension extension : getExtensions(pointId)) {
      if (extension.isValid()) {
        if (ObjectUtils.equals(extension.getUniqueIdentifier(), extensionId)) {
          return extension;
        }
      } else {
        clearCache(pointId);
        return getExtension(pointId, extensionId);
      }
    }
    // no such extension
    return null;
  }

  /**
   * @return extensions for given point ID.
   *
   * @param pointId
   *          the ID of extension point to get extensions for.
   */
  private static List<IExtension> getExtensions(String pointId) {
    List<IExtension> extensions = m_extensions.get(pointId);
    if (extensions == null) {
      extensions = Lists.newArrayList();
      m_extensions.put(pointId, extensions);
      IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(pointId);
      if (extensionPoint != null) {
        CollectionUtils.addAll(extensions, extensionPoint.getExtensions());
      }
    }
    return extensions;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link org.osgi.framework.Bundle} with given id.
   */
  public static org.osgi.framework.Bundle getRequiredBundle(String id) {
    Bundle bundle = Platform.getBundle(id);
    if (bundle == null) {
      throw new IllegalArgumentException("Unable to find Bundle " + id);
    }
    return bundle;
  }

  /**
   * @return the {@link org.osgi.framework.Bundle} that defines given {@link IConfigurationElement}.
   */
  public static org.osgi.framework.Bundle getExtensionBundle(IConfigurationElement element) {
    IExtension extension = element.getDeclaringExtension();
    return getExtensionBundle(extension);
  }

  /**
   * @return the {@link org.osgi.framework.Bundle} that defines given {@link IExtension}.
   */
  public static org.osgi.framework.Bundle getExtensionBundle(IExtension extension) {
    String id = extension.getNamespace();
    return Platform.getBundle(id);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the not-<code>null</code> value of {@link String} attribute.
   *
   * @exception IllegalArgumentException
   *              if no attribute with such name found.
   */
  public static String getRequiredAttribute(IConfigurationElement element, String attribute) {
    String value = element.getAttribute(attribute);
    if (value == null) {
      throw new IllegalArgumentException("Attribute '"
          + attribute
          + "' expected, but not found in "
          + element);
    }
    return value;
  }

  /**
   * @return the value of <code>int</code> attribute.
   *
   * @exception IllegalArgumentException
   *              if no attribute with such name found.
   */
  public static int getRequiredAttributeInteger(IConfigurationElement element, String attribute) {
    String valueString = getRequiredAttribute(element, attribute);
    return Integer.parseInt(valueString);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} from extension {@link Bundle} with path in given attribute. May be
   *         <code>null</code> if no value for attribute.
   */
  public static Image getImage(IConfigurationElement element, String attribute) {
    String path = element.getAttribute(attribute);
    if (path != null) {
      Bundle bundle = getExtensionBundle(element);
      BundleResourceProvider resourceProvider = BundleResourceProvider.get(bundle);
      return resourceProvider.getImage(path);
    }
    return null;
  }

  /**
   * @return the {@link getImageDescriptor} from extension {@link Bundle} with path in given
   *         attribute. May be <code>null</code> if no value for attribute.
   */
  public static ImageDescriptor getImageDescriptor(IConfigurationElement element, String attribute) {
    String path = element.getAttribute(attribute);
    if (path != null) {
      Bundle bundle = getExtensionBundle(element);
      BundleResourceProvider resourceProvider = BundleResourceProvider.get(bundle);
      return resourceProvider.getImageDescriptor(path);
    }
    return null;
  }
}
