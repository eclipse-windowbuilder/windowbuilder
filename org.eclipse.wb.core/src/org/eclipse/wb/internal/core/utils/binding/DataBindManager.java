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
package org.eclipse.wb.internal.core.utils.binding;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Support binding data sources and data editors.
 *
 * @author lobas_av
 */
public final class DataBindManager {
  private final List/*<Binding>*/m_bindings = new ArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Binding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Bind data editor to data source.
   */
  public void bind(IDataEditor editor, IDataProvider provider) {
    bind(editor, provider, false);
  }

  /**
   * Bind data editor to data source.
   */
  public void bind(IDataEditor editor, IDataProvider provider, boolean setValue) {
    m_bindings.add(new Binding(editor, provider));
    // set value
    if (setValue) {
      editor.setValue(provider.getValue(false));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List/*<Runnable>*/m_updateRunnables = new ArrayList();

  /**
   * Adds {@link Runnable} that should be run after values modifications, for example during
   * {@link #performUpdate()} and {@link #performDefault()} or on event in {@link Widget}.
   */
  public void addUpdateRunnable(Runnable runnable) {
    m_updateRunnables.add(runnable);
  }

  /**
   * Specifies that registered update {@link Runnable}'s should be executed after event with given
   * type in {@link Widget}.
   */
  public void addUpdateEvent(Widget widget, int eventType) {
    widget.addListener(eventType, new Listener() {
      public void handleEvent(Event event) {
        runUpdateRunnables();
      }
    });
  }

  /**
   * Runs {@link Runnable}'s.
   */
  private void runUpdateRunnables() {
    for (Iterator I = m_updateRunnables.iterator(); I.hasNext();) {
      Runnable runnable = (Runnable) I.next();
      runnable.run();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Update all editors of current data values.
   */
  public void performUpdate() {
    updateEditors(false);
  }

  /**
   * Update all editors of default data values.
   */
  public void performDefault() {
    updateEditors(true);
  }

  /**
   * Save all editor values into providers.
   */
  public void performCommit() {
    updateProviders();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  private void updateProviders() {
    MultiStatus multiStatus = BindingStatus.ok();
    for (Iterator I = m_bindings.iterator(); I.hasNext();) {
      Binding binding = (Binding) I.next();
      IStatus status = binding.updateProvider();
      if (!mergeStatus(multiStatus, status)) {
        return;
      }
    }
  }

  private void updateEditors(boolean def) {
    for (Iterator I = m_bindings.iterator(); I.hasNext();) {
      Binding binding = (Binding) I.next();
      binding.updateEditor(def);
    }
    runUpdateRunnables();
  }

  /**
   * Incorporates the provided <code>newStatus</code> into the <code>multiStatus</code>.
   *
   * @return <code>true</code> if the update should proceed.
   */
  private boolean mergeStatus(MultiStatus multiStatus, IStatus newStatus) {
    if (!newStatus.isOK()) {
      multiStatus.add(newStatus);
      return multiStatus.getSeverity() < IStatus.ERROR;
    }
    return true;
  }
}