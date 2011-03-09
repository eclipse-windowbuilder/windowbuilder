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
package org.eclipse.wb.internal.swing.databinding.ui.providers;

import org.eclipse.wb.internal.swing.databinding.Activator;

import org.eclipse.swt.graphics.Image;

import java.util.Collection;

/**
 * Helper for association {@link Class} with {@link Image}.
 * 
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class TypeImageProvider {
  public static final Image OBJECT_IMAGE = Activator.getImage("types/Object.png");
  public static final Image STRING_IMAGE = Activator.getImage("types/String.png");
  public static final Image BOOLEAN_IMAGE = Activator.getImage("types/Boolean.png");
  public static final Image NUMBER_IMAGE = Activator.getImage("types/Number.png");
  public static final Image IMAGE_IMAGE = Activator.getImage("types/Image.png");
  public static final Image COLOR_IMAGE = Activator.getImage("types/Color.png");
  public static final Image FONT_IMAGE = Activator.getImage("types/Font.png");
  public static final Image ARRAY_IMAGE = Activator.getImage("types/Array.png");
  public static final Image COLLECTION_IMAGE = Activator.getImage("types/Collection.png");
  public static final Image EL_PROPERTY_IMAGE = Activator.getImage("el_property2.gif");
  public static final Image OBJECT_PROPERTY_IMAGE = Activator.getImage("SelfObject.png");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Image} association with given {@link Class}.
   */
  public static Image getImage(Class<?> type) {
    // unknown type accept as object
    if (type == null) {
      return OBJECT_IMAGE;
    }
    // string
    if (type == String.class || type == byte.class || type == char.class) {
      return STRING_IMAGE;
    }
    // boolean
    if (type == boolean.class || type == Boolean.class) {
      return BOOLEAN_IMAGE;
    }
    // arithmetic
    if (type == int.class
        || type == short.class
        || type == long.class
        || type == float.class
        || type == double.class) {
      return NUMBER_IMAGE;
    }
    // array
    if (type.isArray()) {
      return ARRAY_IMAGE;
    }
    // Collection
    if (Collection.class.isAssignableFrom(type)) {
      return COLLECTION_IMAGE;
    }
    // AWT image
    if (type == java.awt.Image.class || type == javax.swing.Icon.class) {
      return IMAGE_IMAGE;
    }
    // AWT color
    if (type == java.awt.Color.class) {
      return COLOR_IMAGE;
    }
    // AWT font
    if (type == java.awt.Font.class) {
      return FONT_IMAGE;
    }
    // other accept as object
    return OBJECT_IMAGE;
  }
}