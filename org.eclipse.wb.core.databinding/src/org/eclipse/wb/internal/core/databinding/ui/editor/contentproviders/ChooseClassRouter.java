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

/**
 * This class used for route choose class events from <code>source</code>
 * {@link ChooseClassUiContentProvider} to <code>target</code> {@link ChooseClassUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ChooseClassRouter {
  private final Runnable m_listener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassRouter(ChooseClassUiContentProvider source, Runnable listener) {
    m_listener = listener;
    source.setRouter(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  void handle() {
    m_listener.run();
  }
}