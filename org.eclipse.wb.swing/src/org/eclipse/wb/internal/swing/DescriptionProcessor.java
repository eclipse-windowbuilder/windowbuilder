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
package org.eclipse.wb.internal.swing;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.IDescriptionProcessor;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;

import org.eclipse.swt.graphics.Image;

import java.awt.Component;
import java.beans.BeanInfo;
import java.lang.reflect.Method;

/**
 * Implementation of {@link IDescriptionProcessor} for AWT/Swing components.
 * 
 * @author scheglov_ke
 * @coverage swing
 */
public final class DescriptionProcessor implements IDescriptionProcessor {
  private ComponentDescription componentDescription;
  private BeanInfo beanInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(AstEditor editor, ComponentDescription componentDescription) throws Exception {
    this.componentDescription = componentDescription;
    beanInfo = componentDescription.getBeanInfo();
    try {
      if (beanInfo != null) {
        configureIconFromBeanInfo();
        configureContainerFlag();
        configureMethods_add();
      }
    } finally {
      this.componentDescription = null;
      beanInfo = null;
    }
  }

  private void configureIconFromBeanInfo() throws Exception {
    java.awt.Image awtIcon = beanInfo.getIcon(BeanInfo.ICON_COLOR_16x16);
    if (awtIcon != null) {
      Image icon = ImageUtils.convertToSWT(awtIcon);
      componentDescription.setIcon(icon);
    }
  }

  /**
   * Support for "isContainer" flag.
   */
  private void configureContainerFlag() {
    Object value = beanInfo.getBeanDescriptor().getValue("isContainer");
    if (value instanceof Boolean) {
      if (!((Boolean) value).booleanValue()) {
        componentDescription.addParameter("layout.has", "false");
      }
    }
  }

  /**
   * We should execute all <code>add(Type)</code> methods, if <code>Type</code> is {@link Component}
   * subclass.
   */
  private void configureMethods_add() throws Exception {
    Class<?> componentClass = componentDescription.getComponentClass();
    // only for java.awt.Container
    if (!ReflectionUtils.isSuccessorOf(componentClass, "java.awt.Container")) {
      return;
    }
    // check all "add(Type)" methods
    for (Method method : componentClass.getMethods()) {
      if (method.getName().equals("add")) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length >= 1
            && ReflectionUtils.isSuccessorOf(parameterTypes[0], "java.awt.Component")) {
          String signature = ReflectionUtils.getMethodSignature(method);
          if (componentDescription.getMethod(signature) == null) {
            MethodDescription methodDescription = componentDescription.addMethod(method);
            methodDescription.getParameter(0).setChild(true);
            methodDescription.postProcess();
          }
        }
      }
    }
  }
}
