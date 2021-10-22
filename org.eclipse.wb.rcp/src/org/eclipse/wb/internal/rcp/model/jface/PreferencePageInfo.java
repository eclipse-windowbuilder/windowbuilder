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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.ContainerSupport;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;

import java.lang.reflect.Constructor;

/**
 * Model for {@link PreferencePage}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public class PreferencePageInfo extends DialogPageInfo implements IJavaInfoRendering {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PreferencePageInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    JavaInfoUtils.scheduleSpecialRendering(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  Object getShell() {
    if (m_shell == null) {
      return ExecutionUtils.runObject(new RunnableObjectEx<Object>() {
        public Object runObject() throws Exception {
          return ReflectionUtils.invokeMethod(m_preferenceDialog, "getShell()");
        }
      });
    }
    return super.getShell();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Object m_parentShell;
  private Object m_preferenceDialog;

  public void render() throws Exception {
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    // prepare PreferenceNode
    Object preferenceNode;
    {
      Class<?> clazz = editorLoader.loadClass("org.eclipse.jface.preference.PreferenceNode");
      Constructor<?> constructor =
          ReflectionUtils.getConstructorBySignature(
              clazz,
              "<init>(java.lang.String,org.eclipse.jface.preference.IPreferencePage)");
      preferenceNode = constructor.newInstance("__wbp", getObject());
    }
    // prepare PreferenceManager
    Object preferenceManager;
    {
      Class<?> clazz = editorLoader.loadClass("org.eclipse.jface.preference.PreferenceManager");
      preferenceManager = clazz.newInstance();
    }
    // add this PreferencePage
    ReflectionUtils.invokeMethod(
        preferenceManager,
        "addToRoot(org.eclipse.jface.preference.IPreferenceNode)",
        preferenceNode);
    // prepare parent Shell for PreferenceDialog
    if (m_parentShell == null) {
      m_parentShell = ContainerSupport.createShell();
    }
    // create PreferenceDialog
    {
      Class<?> preferenceDialogClass =
          editorLoader.loadClass("org.eclipse.jface.preference.PreferenceDialog");
      Constructor<?> constructor =
          ReflectionUtils.getConstructorBySignature(
              preferenceDialogClass,
              "<init>(org.eclipse.swt.widgets.Shell,org.eclipse.jface.preference.PreferenceManager)");
      m_preferenceDialog = constructor.newInstance(m_parentShell, preferenceManager);
    }
    // open PreferenceDialog, so perform PreferencePage GUI creation
    ReflectionUtils.invokeMethod(m_preferenceDialog, "create()");
    m_shell = ReflectionUtils.invokeMethod(m_preferenceDialog, "getShell()");
    configureShell();
  }

  /**
   * Allows configuring {@link #m_shell} after opening {@link PreferenceDialog}.
   */
  protected void configureShell() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    // dispose PreferenceDialog
    if (m_preferenceDialog != null) {
      ReflectionUtils.invokeMethod(m_preferenceDialog, "close()");
      m_shell = null;
    }
    // call "super"
    super.refresh_dispose();
  }
}
