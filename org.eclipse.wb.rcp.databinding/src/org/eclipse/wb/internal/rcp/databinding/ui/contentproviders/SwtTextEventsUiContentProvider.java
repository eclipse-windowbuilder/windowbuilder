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

import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.CheckedListDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.TextSwtObservableInfo;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import java.util.Arrays;

/**
 * Content provider for edit (choose event type over combo) {@link TextSwtObservableInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class SwtTextEventsUiContentProvider extends DialogFieldUiContentProvider {
  private final TextSwtObservableInfo m_observable;
  private final CheckedListDialogField m_dialogField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwtTextEventsUiContentProvider(TextSwtObservableInfo observable) {
    m_observable = observable;
    m_dialogField = new CheckedListDialogField(null, null, new LabelProvider()) {
      @Override
      public int getNumberOfControls() {
        return 2;
      }

      @Override
      public Control[] doFillIntoGrid(Composite parent, int columns) {
        assertEnoughColumns(columns);
        //
        Label label = getLabelControl(parent);
        label.setLayoutData(gridDataForLabel(1));
        //
        Control list = getListControl(parent);
        GridDataFactory.create(list).fillH().grabH().spanH(columns - 1);
        //
        return new Control[]{label, list};
      }
    };
    m_dialogField.setLabelText(Messages.SwtTextEventsUiContentProvider_eventsLabel);
    m_dialogField.addElements(Arrays.asList(TextSwtObservableInfo.TEXT_EVENTS));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractUIContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContent(Composite parent, int columns) {
    super.createContent(parent, columns);
    CheckboxTableViewer tableViewer = (CheckboxTableViewer) m_dialogField.getTableViewer();
    tableViewer.addCheckStateListener(new ICheckStateListener() {
      @Override
      public void checkStateChanged(CheckStateChangedEvent event) {
        calculateBetterValues(event);
      }
    });
  }

  @Override
  public DialogField getDialogField() {
    return m_dialogField;
  }

  private void calculateBetterValues(CheckStateChangedEvent event) {
    if (event.getChecked()) {
      Object checkedElement = event.getElement();
      ICheckable checkable = event.getCheckable();
      //
      if ("SWT.NONE".equals(checkedElement)) {
        for (Object element : m_dialogField.getCheckedElements()) {
          if (element != checkedElement) {
            checkable.setChecked(element, false);
          }
        }
      } else {
        for (Object element : m_dialogField.getCheckedElements()) {
          if ("SWT.NONE".equals(element)) {
            checkable.setChecked(element, false);
            break;
          }
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void updateFromObject() {
    m_dialogField.setCheckedElements(m_observable.getUpdateEvents());
  }

  @Override
  public void saveToObject() {
    m_observable.setUpdateEvents(CoreUtils.<String>cast(m_dialogField.getCheckedElements()));
  }
}