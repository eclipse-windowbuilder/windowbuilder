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

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;

/**
 * Property for single binding.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public abstract class SingleObserveBindingProperty extends AbstractProperty {
  private static final TextDialogPropertyEditor EDITOR = new TextDialogPropertyEditor() {
    @Override
    protected String getText(Property property) throws Exception {
      SingleObserveBindingProperty observeProperty = (SingleObserveBindingProperty) property;
      return observeProperty.getText();
    }

    @Override
    protected void openDialog(Property property) throws Exception {
      SingleObserveBindingProperty observeProperty = (SingleObserveBindingProperty) property;
      observeProperty.editBinding();
    }
  };
  protected final IObserveInfo m_observeProperty;
  private final String m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SingleObserveBindingProperty(Context context, IObserveInfo observeProperty)
      throws Exception {
    super(EDITOR, context);
    m_observeProperty = observeProperty;
    m_title = m_observeProperty.getPresentation().getText();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IObserveInfo getObserveProperty() {
    return m_observeProperty;
  }

  public final void editBinding() throws Exception {
    IBindingInfo binding = getBinding();
    if (binding == null) {
      AbstractObserveProperty.createBinding(m_context, m_observeProperty);
    } else {
      AbstractBindingProperty.editBinding(m_context, binding);
    }
  }

  /**
   * @return {@link IBindingInfo} for this property.
   */
  protected abstract IBindingInfo getBinding() throws Exception;

  /**
   * @return the text for displaying value of this property.
   */
  protected abstract String getText() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getTitle() {
    return m_title;
  }

  @Override
  public final boolean isModified() throws Exception {
    return getBinding() != null;
  }

  @Override
  public final void setValue(Object value) throws Exception {
    Assert.isTrue(value == UNKNOWN_VALUE);
    IBindingInfo binding = getBinding();
    if (binding != null) {
      AbstractBindingProperty.deleteBinding(m_context, binding);
    }
  }
}