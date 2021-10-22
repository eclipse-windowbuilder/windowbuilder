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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

/**
 * Configuration for {@link ChooseClassAndPropertiesUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class ChooseClassAndPropertiesConfiguration
    extends
      org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration {
  private boolean m_workWithELProperty = true;

  ////////////////////////////////////////////////////////////////////////////
  //
  // ELProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isWorkWithELProperty() {
    return m_workWithELProperty;
  }

  public void setWorkWithELProperty(boolean workWithElProperty) {
    m_workWithELProperty = workWithElProperty;
  }
}