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
package org.eclipse.wb.internal.core.model.layout;

import com.google.common.collect.BiMap;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * General layout information for support convert layouts one to another.
 *
 * @author sablin_aa
 * @coverage core.model.layout
 */
public final class GeneralLayoutData {
  public static final Object KEY = GeneralLayoutData.class;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Fields: grid layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /*
   * Grab flags.
   */
  public Boolean horizontalGrab = null;
  public Boolean verticalGrab = null;

  /*
   * Horizontal alignment.
   */
  public enum HorizontalAlignment {
    NONE, LEFT, CENTER, RIGHT, FILL
  }

  public HorizontalAlignment horizontalAlignment = null;

  /*
   * Vertical alignment.
   */
  public enum VerticalAlignment {
    NONE, TOP, CENTER, BOTTOM, FILL
  }

  public VerticalAlignment verticalAlignment = null;
  /*
   * Grid position.
   */
  public Integer gridX = null;
  public Integer gridY = null;
  /*
   * Grid size.
   */
  public Integer spanX = null;
  public Integer spanY = null;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    final String null_str = "null";
    String x_str = gridX == null ? null_str : gridX.toString();
    String y_str = gridY == null ? null_str : gridY.toString();
    String sx_str = spanX == null ? null : spanX > 1 ? spanX.toString() : null;
    String sy_str = spanY == null ? null : spanY > 1 ? spanY.toString() : null;
    return "cell("
        + x_str
        + (sx_str == null ? "" : "+" + sx_str)
        + ";"
        + y_str
        + (sy_str == null ? "" : "+" + sy_str)
        + ") grab("
        + (horizontalGrab == null ? null_str : horizontalGrab.toString())
        + ";"
        + (verticalGrab == null ? null_str : verticalGrab.toString())
        + ") align("
        + (horizontalAlignment == null ? null_str : horizontalAlignment.toString())
        + ";"
        + (verticalAlignment == null ? null_str : verticalAlignment.toString())
        + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Work with {@link ObjectInfo}
  //
  ////////////////////////////////////////////////////////////////////////////
  public void putToInfo(ObjectInfo objectInfo) {
    objectInfo.putArbitraryValue(KEY, this);
  }

  public static GeneralLayoutData getFromInfo(ObjectInfo objectInfo) {
    GeneralLayoutData generalLayoutData = (GeneralLayoutData) objectInfo.getArbitraryValue(KEY);
    clearForInfo(objectInfo);
    return generalLayoutData;
  }

  public static GeneralLayoutData getFromInfoEx(ObjectInfo objectInfo) {
    GeneralLayoutData generalLayoutData = getFromInfo(objectInfo);
    return generalLayoutData == null ? new GeneralLayoutData() : generalLayoutData;
  }

  public static void clearForInfo(ObjectInfo objectInfo) {
    objectInfo.removeArbitraryValue(KEY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access layout data property values
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Object getLayoutPropertyValue(ObjectInfo layoutData, String propertyName)
      throws Exception {
    Property property = layoutData.getPropertyByTitle(propertyName);
    if (property != null) {
      Object value = property.getValue();
      if (value != Property.UNKNOWN_VALUE) {
        return value;
      }
    }
    return null;
  }

  public static void setLayoutPropertyValue(ObjectInfo layoutData,
      String propertyName,
      Object propertyValue) throws Exception {
    Property property = layoutData.getPropertyByTitle(propertyName);
    if (property != null && propertyValue != null && propertyValue != Property.UNKNOWN_VALUE) {
      property.setValue(propertyValue);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // access mapped property values
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param map
   *          the {@link BiMap} generic -> real.
   *
   * @return the real value that corresponds given generic one, may be <code>null</code>.
   */
  public static <K, T> T getRealValue(BiMap<K, T> map, K generic) {
    return generic == null ? null : map.get(generic);
  }

  /**
   * @param map
   *          the {@link BiMap} generic -> real.
   *
   * @return the generic value that corresponds given real one, may be <code>null</code>.
   */
  public static <K, T> K getGeneralValue(BiMap<K, T> map, T real) {
    return real == null ? null : map.inverse().get(real);
  }
}