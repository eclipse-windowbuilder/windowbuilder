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

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.ManagedForm;

import java.lang.reflect.Constructor;

/**
 * Model for {@link IDetailsPage}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class DetailsPageInfo extends AbstractComponentInfo implements IJavaInfoRendering {
  private InstanceFactoryInfo m_toolkit;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DetailsPageInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    JavaInfoUtils.scheduleSpecialRendering(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    prepare_hosting_ManagedForm();
    m_toolkit = FormToolkitAccessUtils.createFormToolkit_usingAccess(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaInfoRendering
  //
  ////////////////////////////////////////////////////////////////////////////
  private Shell m_shell;
  private Object m_ManagedForm;
  private Composite m_ManagedFormBody;

  @Override
  public void render() throws Exception {
    m_toolkit.setObject(ReflectionUtils.invokeMethod(m_ManagedForm, "getToolkit()"));
    // execute life cycle for IDetailsPage
    ReflectionUtils.invokeMethod(
        getObject(),
        "initialize(org.eclipse.ui.forms.IManagedForm)",
        m_ManagedForm);
    ReflectionUtils.invokeMethod(
        getObject(),
        "createContents(org.eclipse.swt.widgets.Composite)",
        m_ManagedFormBody);
  }

  /**
   * Prepares hosting {@link Shell} and {@link ManagedForm}.
   */
  private void prepare_hosting_ManagedForm() throws Exception {
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    // use same Shell and ManagedForm (so same FormToolkit instance) during life of this model
    // We should use same FormToolkit, because some default values for Color/Font properties are
    // from this FormToolkit, so will be disposed with FormToolkit.
    if (m_shell == null) {
      m_shell = new Shell();
      m_shell.setLayout(new FillLayout());
      // prepare ManagedForm instance
      {
        Class<?> class_ManagedForm = editorLoader.loadClass("org.eclipse.ui.forms.ManagedForm");
        Constructor<?> constructor =
            ReflectionUtils.getConstructor(class_ManagedForm, Composite.class);
        m_ManagedForm = constructor.newInstance(m_shell);
      }
      // initialize ManagedForm.getForm().getBody()
      {
        Object formObject = ReflectionUtils.invokeMethod2(m_ManagedForm, "getForm");
        m_ManagedFormBody = (Composite) ReflectionUtils.invokeMethod2(formObject, "getBody");
      }
      // schedule dispose
      addBroadcastListener(new ObjectEventListener() {
        @Override
        public void dispose() throws Exception {
          if (m_ManagedForm != null) {
            ReflectionUtils.invokeMethod2(m_ManagedForm, "dispose");
            m_ManagedForm = null;
          }
          if (m_shell != null) {
            m_shell.dispose();
            m_shell = null;
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new DetailsPageTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canBeRoot() {
    return true;
  }

  @Override
  public Composite getComponentObject() {
    return m_ManagedFormBody;
  }

  /**
   * @return the top level {@link Shell}.
   */
  Shell getShell() {
    return m_shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    for (Control control : m_ManagedFormBody.getChildren()) {
      control.dispose();
    }
    super.refresh_dispose();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ControlInfo.refresh_fetch(this, new RunnableEx() {
      @Override
      public void run() throws Exception {
        DetailsPageInfo.super.refresh_fetch();
      }
    });
  }
}
