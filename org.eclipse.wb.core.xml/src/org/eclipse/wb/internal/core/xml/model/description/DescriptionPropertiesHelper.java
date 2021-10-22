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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.property.converter.EnumConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.xml.model.property.editor.EnumPropertyEditor;

import org.eclipse.core.runtime.IConfigurationElement;

import java.util.List;

/**
 * Helper for accessing {@link Property}'s parts.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
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
  private static final String POINT_PROPERTY_EDITORS = "org.eclipse.wb.core.xml.propertyEditors";

  /**
   * @return the {@link PropertyEditor} for given type.
   */
  public static PropertyEditor getEditorForType(Class<?> type) throws Exception {
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
    // Enum editor
    if (type.isEnum()) {
      return EnumPropertyEditor.INSTANCE;
    }
    // not found
    return null;
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
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Converters
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_PROPERTY_CONVERTERS =
      "org.eclipse.wb.core.xml.propertyConverters";

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
    // Enum converter
    if (type.isEnum()) {
      return EnumConverter.INSTANCE;
    }
    // not found
    return null;
  }
}
