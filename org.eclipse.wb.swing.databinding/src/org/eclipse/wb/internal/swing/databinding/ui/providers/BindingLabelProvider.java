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
package org.eclipse.wb.internal.swing.databinding.ui.providers;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.model.bindings.AutoBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.ColumnBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.DetailBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JComboBoxBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JListBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JTableBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.VirtualBindingInfo;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.lang.StringUtils;

/**
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class BindingLabelProvider extends LabelProvider
    implements
      ITableLabelProvider,
      IColorProvider {
  private static final Image AUTO_BINDING_IMAGE = Activator.getImage("autobinding2.png");
  private static final Image JLIST_BINDING_IMAGE = Activator.getImage("JList.gif");
  private static final Image JLIST_DETAIL_BINDING_IMAGE = Activator.getImage("JListDetail2.png");
  private static final Image JCOMBO_BOX_BINDING_IMAGE = Activator.getImage("JComboBox.gif");
  private static final Image JTABLE_BINDING_IMAGE = Activator.getImage("JTable.gif");
  private static final Image JTABLE_COLUMN_BINDING_IMAGE =
      Activator.getImage("JTableColumnBinding.png");
  public static final BindingLabelProvider INSTANCE = new BindingLabelProvider();

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITableLabelProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getColumnText(final Object element, final int column) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        BindingInfo binding = (BindingInfo) element;
        switch (column) {
          case 1 :
            // target
            return binding.getTargetPresentationText(true);
          case 2 :
            // model
            return binding.getModelPresentationText(true);
          case 3 :
            // strategy
            if (binding instanceof AutoBindingInfo) {
              AutoBindingInfo autoBinding = (AutoBindingInfo) binding;
              return autoBinding.getStrategyInfo().getStrategyValue();
            }
            return null;
          case 4 :
            // binding
          {
            String variable = binding.getVariableIdentifier();
            if (variable != null) {
              String name = binding.getName();
              if (StringUtils.isEmpty(name)) {
                return variable;
              }
              return variable + " - " + name;
            }
          }
        }
        return null;
      }
    }, "<exception, see log>");
  }

  public Image getColumnImage(Object element, int column) {
    if (column == 0) {
      if (element instanceof JListBindingInfo) {
        return JLIST_BINDING_IMAGE;
      }
      if (element instanceof DetailBindingInfo) {
        return JLIST_DETAIL_BINDING_IMAGE;
      }
      if (element instanceof JComboBoxBindingInfo) {
        return JCOMBO_BOX_BINDING_IMAGE;
      }
      if (element instanceof JTableBindingInfo) {
        return JTABLE_BINDING_IMAGE;
      }
      if (element instanceof ColumnBindingInfo) {
        return JTABLE_COLUMN_BINDING_IMAGE;
      }
      if (element instanceof AutoBindingInfo) {
        return AUTO_BINDING_IMAGE;
      }
      if (element instanceof VirtualBindingInfo) {
        VirtualBindingInfo binding = (VirtualBindingInfo) element;
        switch (binding.getSwingType()) {
          case JListBinding :
            return JLIST_BINDING_IMAGE;
          case JTableBinding :
            return JTABLE_BINDING_IMAGE;
          case JComboBoxBinding :
            return JCOMBO_BOX_BINDING_IMAGE;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IColorProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public Color getForeground(Object element) {
    if (element instanceof VirtualBindingInfo) {
      return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
    }
    return null;
  }

  public Color getBackground(Object element) {
    return null;
  }
}