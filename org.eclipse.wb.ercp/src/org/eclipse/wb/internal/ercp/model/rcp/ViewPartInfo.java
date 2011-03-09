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
package org.eclipse.wb.internal.ercp.model.rcp;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.swt.support.FillLayoutSupport;
import org.eclipse.wb.internal.swt.support.SwtSupport;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

/**
 * Implementation of {@link AbstractComponentInfo} for {@link ViewPart}.
 * 
 * @author scheglov_ke
 * @coverage ercp.model.rcp
 */
public final class ViewPartInfo extends AbstractComponentInfo implements IJavaInfoRendering {
  private Object m_shell;
  private Object m_pageParent;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewPartInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    JavaInfoUtils.scheduleSpecialRendering(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Renders this {@link PreferencePage} using {@link PreferenceDialog}.
   */
  public void render() throws Exception {
    // create shell
    m_shell = ContainerSupport.createShell();
    ContainerSupport.setLayout(m_shell, FillLayoutSupport.newInstance());
    ContainerSupport.setShellImage(m_shell, getDescription().getIcon());
    ContainerSupport.setShellText(m_shell, getPresentation().getText());
    // create "parent" parameter for page
    m_pageParent = ContainerSupport.createComposite(m_shell, SwtSupport.NONE);
    ContainerSupport.setLayout(m_pageParent, FillLayoutSupport.newInstance());
    // perform ViewPart GUI creation
    ReflectionUtils.invokeMethod(
        getObject(),
        "createPartControl(org.eclipse.swt.widgets.Composite)",
        m_pageParent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new ViewPartTopBoundsSupport(this);
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
  public Object getComponentObject() {
    return m_shell;
  }

  /**
   * @return the {@link Shell} on which this {@link ViewPart} GUI should be created.
   */
  Object getShell() {
    return m_shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    ControlSupport.dispose(m_shell);
    m_shell = null;
    // call "super"
    super.refresh_dispose();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ControlInfo.refresh_fetch(this, new RunnableEx() {
      public void run() throws Exception {
        ViewPartInfo.super.refresh_fetch();
      }
    });
  }
}
