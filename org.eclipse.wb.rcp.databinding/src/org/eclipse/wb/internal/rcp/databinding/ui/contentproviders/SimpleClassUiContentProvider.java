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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.model.SimpleClassObjectInfo;

/**
 * Content provider for edit (choose class over dialog and combo) {@link SimpleClassObjectInfo}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class SimpleClassUiContentProvider extends ChooseClassUiContentProvider {
  private final SimpleClassObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleClassUiContentProvider(ChooseClassConfiguration configuration,
      SimpleClassObjectInfo object) {
    super(configuration);
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    setClassName(m_object.getClassName());
  }

  public void saveToObject() throws Exception {
    m_object.setClassName(getClassName());
  }
}