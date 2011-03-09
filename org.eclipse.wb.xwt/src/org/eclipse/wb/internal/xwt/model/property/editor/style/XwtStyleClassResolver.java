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
package org.eclipse.wb.internal.xwt.model.property.editor.style;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;

import org.apache.commons.lang.StringUtils;

/**
 * Implementation of {@link IStyleClassResolver} for XWT.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public final class XwtStyleClassResolver implements IStyleClassResolver {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IStyleClassResolver INSTANCE = new XwtStyleClassResolver();

  private XwtStyleClassResolver() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IStyleClassResolver
  //
  ////////////////////////////////////////////////////////////////////////////
  public String resolve(Property property, String className) {
    if (className.equals("org.eclipse.swt.SWT")) {
      return "";
    }
    //
    String packageName = StringUtils.substringBeforeLast(className, ".");
    String namespace = getPackageNamespace(property, packageName);
    String shortClassName = StringUtils.substringAfterLast(className, ".");
    return "(" + namespace + ":" + shortClassName + ").";
  }

  private static String getPackageNamespace(Property property, String packageName) {
    XmlObjectInfo object = ((XmlProperty) property).getObject();
    DocumentElement element = object.getCreationSupport().getElement();
    return NamespacesHelper.ensureName(element, "clr-namespace:" + packageName, "p");
  }
}