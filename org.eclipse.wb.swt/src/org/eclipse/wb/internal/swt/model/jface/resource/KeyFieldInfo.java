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
package org.eclipse.wb.internal.swt.model.jface.resource;

/**
 * Additional class with information about registry key (describe as field).
 * 
 * @author lobas_av
 * @coverage swt.model.jface
 */
public final class KeyFieldInfo {
  /**
   * Key field name.
   */
  public final String keyName;
  /**
   * Key source code: <code>qualifiedClassName.fieldName</code>.
   */
  public final String keySource;
  /**
   * Key field value.
   */
  public final String keyValue;
  /**
   * Registry value for current key. Maybe is <code>null</code>.
   */
  public Object value;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public KeyFieldInfo(Class<?> declaringClass, String keyName, String keyValue) {
    this.keyName = keyName;
    keySource = declaringClass.getName() + "." + keyName;
    this.keyValue = keyValue;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int hashCode() {
    return keyName.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof KeyFieldInfo) {
      KeyFieldInfo info = (KeyFieldInfo) object;
      return keyName.equals(info.keyName);
    }
    return false;
  }
}