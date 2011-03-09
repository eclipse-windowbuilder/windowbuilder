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
package org.eclipse.wb.internal.swing.customize;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;

import java.beans.BeanDescriptor;
import java.beans.Customizer;
import java.util.List;

/**
 * Contributes {@link Action} for customizing object using {@link Customizer}.
 * 
 * @author lobas_av
 * @coverage swing.customize
 */
public final class CustomizerSupport implements IJavaInfoInitializationParticipator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaInfoInitializationParticipator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final JavaInfo javaInfo) throws Exception {
    final Class<Customizer> customizerClass = getCustomizerClass(javaInfo);
    if (customizerClass != null) {
      javaInfo.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void addContextMenu(List<? extends ObjectInfo> objects,
            ObjectInfo object,
            IMenuManager manager) throws Exception {
          if (javaInfo == object && objects.size() == 1) {
            contribute(javaInfo, manager, customizerClass);
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bean utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Customizer} class, explicit for bean, of inherited from super class.
   */
  @SuppressWarnings("unchecked")
  private static Class<Customizer> getCustomizerClass(JavaInfo javaInfo) throws Exception {
    BeanDescriptor beanDescriptor = javaInfo.getDescription().getBeanDescriptor();
    if (beanDescriptor != null) {
      Class<?> customizerClass = beanDescriptor.getCustomizerClass();
      if (customizerClass != null && Customizer.class.isAssignableFrom(customizerClass)) {
        return (Class<Customizer>) customizerClass;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contribute "Customize" action.
   */
  private static void contribute(JavaInfo javaInfo,
      IContributionManager manager,
      Class<Customizer> customizerClass) {
    CustomizerAction action = new CustomizerAction(javaInfo, customizerClass);
    manager.appendToGroup(IContextMenuConstants.GROUP_ADDITIONAL, action);
  }
}