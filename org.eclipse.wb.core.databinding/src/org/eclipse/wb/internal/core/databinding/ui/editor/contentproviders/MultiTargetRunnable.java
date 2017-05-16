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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import com.google.common.collect.Lists;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class MultiTargetRunnable implements Runnable {
  private final ChooseClassUiContentProvider m_source;
  private final List<ChooseClassUiContentProvider> m_targets = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiTargetRunnable(ChooseClassUiContentProvider source) {
    m_source = source;
  }

  public MultiTargetRunnable(ChooseClassUiContentProvider source,
      ChooseClassUiContentProvider[] targets) {
    this(source);
    for (ChooseClassUiContentProvider target : targets) {
      addTarget(target, false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addTarget(ChooseClassUiContentProvider target, boolean update) {
    m_targets.add(target);
    target.getDialogField().setEnabled(false);
    if (update) {
      target.setClassName(m_source.getClassName());
    }
  }

  public void removeTarget(ChooseClassUiContentProvider target) {
    m_targets.remove(target);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Runnable
  //
  ////////////////////////////////////////////////////////////////////////////
  public void run() {
    for (ChooseClassUiContentProvider target : m_targets) {
      target.setClassName(m_source.getClassName());
    }
  }
}