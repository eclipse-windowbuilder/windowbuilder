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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.swt.model.property.editor.font.FontInfo;

import org.eclipse.jface.resource.ResourceRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub class for using eRCP JFace classes in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class JFaceSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if JFace is avialable.
   */
  public static boolean isAvialable() {
    try {
      getJFaceResourceClass();
      return true;
    } catch (Throwable e) {
      return false;
    }
  }

  /**
   * @return the {@link ResourceRegistry} class.
   */
  public static Class<?> getResourceRegistryClass() throws Exception {
    return loadClass("org.eclipse.jface.resource.ResourceRegistry");
  }

  private static Class<?> getJFaceResourceClass() throws Exception {
    return loadClass("org.eclipse.jface.resource.JFaceResources");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JFace
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return all JFace fonts.
   */
  public static List<FontInfo> getJFaceFonts() throws Exception {
    List<FontInfo> jfaceFonts = new ArrayList<>();
    Method[] methods = getJFaceResourceClass().getMethods();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      int modifiers = method.getModifiers();
      // check public static
      if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
        continue;
      }
      // check getXXXFont name
      String name = method.getName();
      if (!name.startsWith("get") || !name.endsWith("Font")) {
        continue;
      }
      // check empty parameters method
      if (method.getParameterTypes().length != 0) {
        continue;
      }
      // check return type
      if (!method.getReturnType().getName().equals("org.eclipse.swt.graphics.Font")) {
        continue;
      }
      // create font info
      Object font = method.invoke(null);
      jfaceFonts.add(
          new FontInfo(name + "()",
              font,
              "org.eclipse.jface.resource.JFaceResources." + name + "()",
              false));
    }
    return jfaceFonts;
  }
}