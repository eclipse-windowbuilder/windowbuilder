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
package org.eclipse.wb.internal.core.model.clipboard;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;

import java.util.Map;

/**
 * Command for applying properties of {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public final class PropertiesClipboardCommand extends ClipboardCommand {
  private static final long serialVersionUID = 0L;
  private final Map<String, String> m_propertyTitleToSource = Maps.newTreeMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertiesClipboardCommand(JavaInfo javaInfo) throws Exception {
    for (Property property : javaInfo.getProperties()) {
      if (property instanceof GenericPropertyImpl
          && !property.getCategory().isSystem()
          && property.isModified()) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
        String clipboardSource = genericProperty.getClipboardSource();
        if (clipboardSource != null) {
          m_propertyTitleToSource.put(property.getTitle(), clipboardSource);
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
  public void execute(JavaInfo javaInfo) throws Exception {
    for (Property property : javaInfo.getProperties()) {
      if (property instanceof GenericPropertyImpl) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
        String clipboardSource = m_propertyTitleToSource.get(property.getTitle());
        if (clipboardSource != null) {
          genericProperty.setExpression(clipboardSource, Property.UNKNOWN_VALUE);
        }
      }
    }
  }
}
