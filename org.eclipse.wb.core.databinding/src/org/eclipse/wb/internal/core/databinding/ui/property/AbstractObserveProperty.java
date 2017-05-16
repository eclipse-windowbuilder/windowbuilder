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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.BindWizard;

import org.eclipse.jface.wizard.WizardDialog;

import java.util.List;

/**
 * Property for {@link IObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public abstract class AbstractObserveProperty extends AbstractProperty {
  protected final IObserveInfo m_observeProperty;
  protected final List<AbstractBindingProperty> m_bindingProperties = Lists.newArrayList();
  private final String m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractObserveProperty(Context context, IObserveInfo observeProperty) throws Exception {
    super(ObservePropertyEditor.EDITOR, context);
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

  public final List<AbstractBindingProperty> getBindingProperties() {
    return m_bindingProperties;
  }

  /**
   * Add all binding to given {@code bindings} for this observe.
   */
  public abstract void getBindings(List<IBindingInfo> bindings, List<Boolean> isTargets)
      throws Exception;

  /**
   * @return {@link AbstractBindingProperty} new binding property for this observe.
   */
  public abstract AbstractBindingProperty createBindingProperty() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createBinding() throws Exception {
    createBinding(m_context, m_observeProperty);
  }

  public static final void createBinding(Context context, IObserveInfo observeProperty)
      throws Exception {
    BindWizard wizard = new BindWizard(context, observeProperty);
    WizardDialog dialog = new WizardDialog(DesignerPlugin.getShell(), wizard);
    dialog.open();
  }

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
    ObservePropertyEditor.EDITOR.updateProperties(this);
    return !m_bindingProperties.isEmpty();
  }
}