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
package org.eclipse.wb.internal.core.editor.palette;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.model.description.CreationDescription.TypeParameterDescription;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Dialog provides ability to select argument for type parameters of creating generic component.
 *
 * @author sablin_aa
 * @coverage core.editor.palette.ui
 */
public final class TypeParametersDialog extends ResizableTitleAreaDialog {
  private final IJavaProject m_javaProject;
  private final Map<String, TypeParameterDescription> m_typeParameters;
  private final Map<String, String> m_typeArguments = Maps.newTreeMap();
  private final Map<String, TypeParameterComposite> m_typeParameterComposites = Maps.newTreeMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TypeParametersDialog(Shell parentShell,
      IJavaProject javaProject,
      Map<String, TypeParameterDescription> typeParameters) {
    super(parentShell, DesignerPlugin.getDefault());
    m_javaProject = javaProject;
    m_typeParameters = typeParameters;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Map<String, String> getArguments() {
    return m_typeArguments;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Messages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Control createContents(Composite parent) {
    Control control = super.createContents(parent);
    configureMessages();
    return control;
  }

  /**
   * Subclasses override this methods to set title and message for this {@link TitleAreaDialog}.
   */
  protected void configureMessages() {
    getShell().setText(Messages.TypeParametersDialog_shellTitle);
    setTitle(Messages.TypeParametersDialog_title);
    setMessage(Messages.TypeParametersDialog_message);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    //
    Composite container = new Composite(area, SWT.NONE);
    GridDataFactory.create(container).grab().fill();
    GridLayoutFactory.create(container).columns(1);
    // create {@link TypeParameterComposite}s
    for (Entry<String, TypeParameterDescription> typeParameter : m_typeParameters.entrySet()) {
      String key = typeParameter.getKey();
      TypeParameterDescription paramenerInfo = typeParameter.getValue();
      // create parameters composites
      TypeParameterComposite parameterComposite =
          new TypeParameterComposite(container, SWT.NONE, m_javaProject, paramenerInfo);
      GridDataFactory.create(parameterComposite).grabH().fillH();
      m_typeParameterComposites.put(key, parameterComposite);
      // fill result arguments by default
      m_typeArguments.put(key, paramenerInfo.getTypeName());
    }
    return area;
  }

  @Override
  protected void okPressed() {
    // fill selected arguments
    for (Entry<String, TypeParameterComposite> parameterComposite : m_typeParameterComposites.entrySet()) {
      m_typeArguments.put(parameterComposite.getKey(), parameterComposite.getValue().getArgument());
    }
    // OK
    super.okPressed();
  }
}
