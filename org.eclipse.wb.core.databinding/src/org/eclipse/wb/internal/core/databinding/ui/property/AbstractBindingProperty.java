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
package org.eclipse.wb.internal.core.databinding.ui.property;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.ui.BindDialog;
import org.eclipse.wb.internal.core.databinding.ui.BindingElementsComposite;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jface.window.Window;

/**
 * Property for single binding.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public abstract class AbstractBindingProperty extends AbstractProperty {
  protected IBindingInfo m_binding;
  protected boolean m_isTarget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBindingProperty(Context context) {
    super(BindingPropertyEditor.EDITOR, context);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IBindingInfo getBinding() {
    return m_binding;
  }

  public final void setBinding(IBindingInfo binding, boolean isTarget) {
    m_binding = binding;
    m_isTarget = isTarget;
  }

  /**
   * @return the text for displaying value of this property.
   */
  public abstract String getText() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void editBinding() throws Exception {
    editBinding(m_context, m_binding);
  }

  public static void editBinding(Context context, IBindingInfo binding) throws Exception {
    BindDialog dialog =
        new BindDialog(DesignerPlugin.getShell(),
            context.plugin,
            context.provider,
            binding,
            false,
            false);
    if (dialog.open() == Window.OK) {
      context.provider.editBinding(binding);
    }
  }

  @Override
  public final void setValue(Object value) throws Exception {
    Assert.isTrue(value == UNKNOWN_VALUE);
    deleteBinding(m_context, m_binding);
  }

  public static void deleteBinding(Context context, IBindingInfo binding) throws Exception {
    if (BindingElementsComposite.canDeleteBinding(
        context.provider,
        binding,
        DesignerPlugin.getShell())) {
      context.provider.deleteBinding(binding);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getTitle() {
    return "";
  }

  @Override
  public final boolean isModified() throws Exception {
    return true;
  }
}