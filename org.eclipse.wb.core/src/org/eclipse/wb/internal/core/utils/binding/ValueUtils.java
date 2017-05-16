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
package org.eclipse.wb.internal.core.utils.binding;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public final class ValueUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Boolean
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Convert <code>boolean</code> to <code>{@link Boolean}</code> object.
   */
  public static Object booleanToObject(boolean value) {
    return value ? Boolean.TRUE : Boolean.FALSE;
  }

  /**
   * Extract <code>boolean</code> from given object.
   */
  public static boolean objectToBoolean(Object value) {
    // extract from Boolean
    if (value instanceof Boolean) {
      Boolean booleanObject = (Boolean) value;
      return booleanObject.booleanValue();
    }
    // extract from Object
    String stringObject = ObjectUtils.toString(value);
    return BooleanUtils.toBoolean(stringObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Boolean Array
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Extract <code>boolean</code> array from given object.
   */
  public static boolean[] objectToBooleanArray(Object value) {
    // check boolean array
    if (value instanceof boolean[]) {
      return (boolean[]) value;
    }
    // check list of boolean's or String's
    if (value instanceof List) {
      // prepare list
      List listValues = (List) value;
      // check empty list
      if (listValues.isEmpty()) {
        return null;
      }
      // fill boolean array
      boolean[] values = new boolean[listValues.size()];
      for (int i = 0; i < values.length; i++) {
        values[i] = objectToBoolean(listValues.get(i));
      }
      return values;
    }
    return null;
  }
}