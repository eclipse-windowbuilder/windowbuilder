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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;

/**
 * Model for {@link org.eclipse.wb.rcp.databinding.BeansSetObservableFactory}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class BeansSetObservableFactoryInfo extends BeansObservableFactoryInfo {
  private static final String FACTORY_CLASS =
      "org.eclipse.wb.rcp.databinding.BeansSetObservableFactory";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeansSetObservableFactoryInfo(String className) {
    super(className);
  }

  public BeansSetObservableFactoryInfo() {
    super(FACTORY_CLASS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configure(ChooseClassConfiguration configuration) {
    configuration.setValueScope(FACTORY_CLASS);
    configuration.setClearValue(FACTORY_CLASS);
    configuration.setBaseClassName(FACTORY_CLASS);
    configuration.setConstructorParameters(new Class[]{Class.class, String.class});
  }
}