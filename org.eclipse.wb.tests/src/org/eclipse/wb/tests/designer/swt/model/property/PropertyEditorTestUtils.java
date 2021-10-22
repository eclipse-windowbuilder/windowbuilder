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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Utilities for testing SWT Color/Image/Font property editors.
 *
 * @author scheglov_ke
 * @author lobas_av
 */
public final class PropertyEditorTestUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the result of {@link Color/Image/Font/PropertyEditor#getText()} invocation.
   */
  public static String getText(Property property) throws Exception {
    return (String) ReflectionUtils.invokeMethod(
        property.getEditor(),
        "getText(org.eclipse.wb.internal.core.model.property.Property)",
        property);
  }

  /**
   * @return the result of {@link Color/Image/Font/PropertyEditor#getClipboardSource()} invocation.
   */
  public static String getClipboardSource(Property property) throws Exception {
    return (String) ReflectionUtils.invokeMethod(
        property.getEditor(),
        "getClipboardSource(org.eclipse.wb.internal.core.model.property.GenericProperty)",
        property);
  }
}