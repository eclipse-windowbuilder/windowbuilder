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

import org.eclipse.wb.internal.core.databinding.ui.editor.EmptyPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;

import java.util.List;

/**
 * {@link IUiContentProvider} which is a container for other {@link IUiContentProvider}'s.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class UIContentContainer<T extends BindingInfo>
    extends
      org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.UIContentContainer<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIContentContainer(T binding,
      List<BindingInfo> bindings,
      String errorPrefix,
      DatabindingsProvider provider) throws Exception {
    super(binding, errorPrefix);
    m_binding.createContentProviders(bindings, m_providers, EmptyPageListener.INSTANCE, provider);
  }
}