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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.EnumPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditorProvider;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.IConfigurationElement;

import java.beans.PropertyDescriptor;
import java.util.List;

/**
 * Helper for accessing {@link Property}'s parts.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public class DescriptionPropertiesHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DescriptionPropertiesHelper() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property editors
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_PROPERTY_EDITORS = "org.eclipse.wb.core.propertyEditors";

  /**
   * @return the {@link PropertyEditor} for given type.
   */
  public static PropertyEditor getEditorForType(Class<?> type) throws Exception {
    // enum editor
    if (type.isEnum()) {
      return EnumPropertyEditor.INSTANCE;
    }
    // try to find editor using PropertyEditorProvider
    for (PropertyEditorProvider provider : getPropertyEditorProviders()) {
      PropertyEditor editor = provider.getEditorForType(type);
      if (editor != null) {
        return editor;
      }
    }
    // try to find editor using type name
    {
      String typeName = ReflectionUtils.getFullyQualifiedName(type, false);
      List<IConfigurationElement> elements =
          ExternalFactoriesHelper.getElements(POINT_PROPERTY_EDITORS, "editor");
      for (IConfigurationElement element : elements) {
        if (typeName.equals(element.getAttribute("type"))) {
          return ExternalFactoriesHelper.createExecutableExtension(element, "class");
        }
      }
    }
    // not found
    return null;
  }

  /**
   * @return the {@link PropertyEditor} for given {@link PropertyDescriptor} or <code>null</code>.
   */
  public static PropertyEditor getEditorForPropertyDescriptor(PropertyDescriptor descriptor)
      throws Exception {
    List<PropertyEditorProvider> providers = getPropertyEditorProviders();
    for (PropertyEditorProvider provider : providers) {
      PropertyEditor editor = provider.getEditorForPropertyDescriptor(descriptor);
      if (editor != null) {
        return editor;
      }
    }
    // not found
    return null;
  }

  /**
   * @return the {@link PropertyEditor} for given {@link java.beans.PropertyEditor} editor type or
   *         <code>null</code>.
   */
  public static PropertyEditor getEditorForEditorType(Class<?> editorType) throws Exception {
    List<PropertyEditorProvider> providers = getPropertyEditorProviders();
    for (PropertyEditorProvider provider : providers) {
      PropertyEditor editor = provider.getEditorForEditorType(editorType);
      if (editor != null) {
        return editor;
      }
    }
    // not found
    return null;
  }

  /**
   * @return contributed {@link PropertyEditorProvider}-s.
   */
  private static List<PropertyEditorProvider> getPropertyEditorProviders() {
    return ExternalFactoriesHelper.getElementsInstances(
        PropertyEditorProvider.class,
        POINT_PROPERTY_EDITORS,
        "provider");
  }

  /**
   * @return the configurable {@link PropertyEditor} for given <code>id</code>.
   */
  public static PropertyEditor getConfigurableEditor(String id) throws Exception {
    List<IConfigurationElement> elements =
        ExternalFactoriesHelper.getElements(POINT_PROPERTY_EDITORS, "configurableEditor");
    for (IConfigurationElement element : elements) {
      if (id.equals(element.getAttribute("id"))) {
        return ExternalFactoriesHelper.createExecutableExtension(element, "class");
      }
    }
    // not found
    throw new IllegalArgumentException("Can not find configurable editor with id '" + id + "'.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Converters
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_PROPERTY_CONVERTERS = "org.eclipse.wb.core.propertyConverters";

  /**
   * @return the {@link ExpressionConverter} for given type.
   */
  public static ExpressionConverter getConverterForType(Class<?> type) throws Exception {
    String typeName = ReflectionUtils.getFullyQualifiedName(type, false);
    List<IConfigurationElement> elements =
        ExternalFactoriesHelper.getElements(POINT_PROPERTY_CONVERTERS, "converter");
    for (IConfigurationElement element : elements) {
      if (typeName.equals(element.getAttribute("type"))) {
        return ExternalFactoriesHelper.createExecutableExtension(element, "class");
      }
    }
    // not found
    return null;
  }
}
