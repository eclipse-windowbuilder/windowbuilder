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
package org.eclipse.wb.internal.core.xml.model.clipboard;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;

import java.util.Map;

/**
 * Command for applying properties of {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.clipboard
 */
public final class PropertiesClipboardCommand extends ClipboardCommand {
  private static final long serialVersionUID = 0L;
  private final Map<String, String> m_propertyTitleToSource = Maps.newTreeMap();
  private final Map<String, Object> m_propertyTitleToObject = Maps.newTreeMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertiesClipboardCommand(XmlObjectInfo object) throws Exception {
    for (Property property : object.getProperties()) {
      String title = property.getTitle();
      if (property instanceof GenericProperty && property.isModified()) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
        String clipboardSource = genericProperty.getClipboardSource();
        if (clipboardSource != null) {
          m_propertyTitleToSource.put(title, clipboardSource);
        }
      }
      if (property instanceof IClipboardObjectProperty) {
        IClipboardObjectProperty objectProperty = (IClipboardObjectProperty) property;
        Object value = objectProperty.getClipboardObject();
        if (value != Property.UNKNOWN_VALUE) {
          m_propertyTitleToObject.put(title, value);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute(XmlObjectInfo object) throws Exception {
    for (Property property : object.getProperties()) {
      String title = property.getTitle();
      if (property instanceof GenericPropertyImpl) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
        String clipboardSource = m_propertyTitleToSource.get(title);
        if (clipboardSource != null) {
          genericProperty.setExpression(clipboardSource, Property.UNKNOWN_VALUE);
        }
      }
      if (property instanceof IClipboardObjectProperty) {
        IClipboardObjectProperty objectProperty = (IClipboardObjectProperty) property;
        Object value = m_propertyTitleToObject.get(title);
        if (value != null) {
          objectProperty.setClipboardObject(value);
        }
      }
    }
  }
}
