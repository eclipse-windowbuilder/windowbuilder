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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryContainerInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants;

import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Support for providing {@link FormToolkit} instance in creations.
 *
 * @author sablin_aa
 * @coverage rcp.model.forms
 */
public final class FormToolkitRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new FormToolkitRootProcessor();

  private FormToolkitRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
    root.addBroadcastListener(new JavaEventListener() {
      @Override
      public void associationTemplate(JavaInfo component, String[] source) throws Exception {
        final String FORM_TOOLKIT_NULL = "(org.eclipse.ui.forms.widgets.FormToolkit) null";
        if (IPreferenceConstants.TOOLKIT_ID.equals(component.getDescription().getToolkit().getId())
            && source[0].contains(FORM_TOOLKIT_NULL)) {
          InstanceFactoryInfo toolkitInstanceFactory = locateFormToolkitInfo(root);
          String toolkitSource =
              toolkitInstanceFactory != null
                  ? TemplateUtils.getExpression(toolkitInstanceFactory)
                  : "new org.eclipse.ui.forms.widgets.FormToolkit(org.eclipse.swt.widgets.Display.getCurrent())";
          source[0] = StringUtils.replace(source[0], FORM_TOOLKIT_NULL, toolkitSource);
        }
      }
    });
  }

  public static InstanceFactoryInfo locateFormToolkitInfo(JavaInfo root) {
    List<InstanceFactoryContainerInfo> instanceFactorieContainers =
        root.getChildren(InstanceFactoryContainerInfo.class);
    for (InstanceFactoryContainerInfo instanceFactoryContainerInfo : instanceFactorieContainers) {
      List<InstanceFactoryInfo> instanceFactoryInfos =
          instanceFactoryContainerInfo.getChildren(InstanceFactoryInfo.class);
      for (InstanceFactoryInfo instanceFactoryInfo : instanceFactoryInfos) {
        if (ReflectionUtils.isSuccessorOf(
            instanceFactoryInfo.getDescription().getComponentClass(),
            "org.eclipse.ui.forms.widgets.FormToolkit")) {
          return instanceFactoryInfo;
        }
      }
    }
    return null;
  }
}
