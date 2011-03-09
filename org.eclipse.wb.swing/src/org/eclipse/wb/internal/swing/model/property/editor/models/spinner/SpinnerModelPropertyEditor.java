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
package org.eclipse.wb.internal.swing.model.property.editor.models.spinner;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jface.window.Window;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * {@link PropertyEditor} for {@link SpinnerModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class SpinnerModelPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new SpinnerModelPropertyEditor();

  private SpinnerModelPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  @Override
  protected String getText(Property property) throws Exception {
    return getText(property, false);
  }

  @Override
  protected PropertyTooltipProvider createPropertyTooltipProvider() {
    return new PropertyTooltipTextProvider() {
      @Override
      protected String getText(Property property) throws Exception {
        return SpinnerModelPropertyEditor.this.getText(property, true);
      }

      @Override
      public int getTooltipPosition() {
        return BELOW;
      }
    };
  }

  /**
   * @return the text to display for given {@link Property}.
   */
  private String getText(Property property, boolean forTooltip) throws Exception {
    Object value = property.getValue();
    if (value instanceof SpinnerModel) {
      SpinnerModel model = (SpinnerModel) value;
      // analyze known models
      if (model instanceof SpinnerNumberModel) {
        SpinnerNumberModel numberModel = (SpinnerNumberModel) model;
        // prepare labels
        String typeLabel = forTooltip ? "type=" : "";
        String valueLabel = forTooltip ? " value=" : ", ";
        String startLabel = forTooltip ? " start=" : ", ";
        String endLabel = forTooltip ? " end=" : ", ";
        String stepLabel = forTooltip ? " step=" : ", ";
        // text
        String typeText =
            typeLabel + CodeUtils.getShortClass(numberModel.getValue().getClass().getName());
        String valueText = valueLabel + numberModel.getValue();
        String startText = startLabel + numberModel.getMinimum();
        String endText = endLabel + numberModel.getMaximum();
        String stepText = stepLabel + numberModel.getStepSize();
        return typeText + valueText + startText + endText + stepText;
      } else if (model instanceof SpinnerListModel) {
        SpinnerListModel listModel = (SpinnerListModel) model;
        String separator = forTooltip ? "\n" : ", ";
        return StringUtils.join(listModel.getList().iterator(), separator);
      } else if (model instanceof SpinnerDateModel) {
        SpinnerDateModel dateModel = (SpinnerDateModel) model;
        // prepare labels
        String valueLabel = forTooltip ? "value=" : "";
        String startLabel = forTooltip ? "\nstart=" : ", ";
        String endLabel = forTooltip ? "\nend=" : ", ";
        String stepLabel = forTooltip ? "\nstep=" : ", ";
        // text
        String valueText = valueLabel + getDateText(dateModel.getValue());
        String startText = startLabel + getDateText(dateModel.getStart());
        String endText = endLabel + getDateText(dateModel.getEnd());
        String stepText = stepLabel + getDateStep(dateModel.getCalendarField());
        return valueText + startText + endText + stepText;
      } else {
        return model.toString();
      }
    }
    // unknown value
    return null;
  }

  /**
   * @return the text presentation of given {@link Date} object, may be <code>"null"</code>, if
   *         object is not {@link Date}.
   */
  private static String getDateText(Object value) {
    if (value instanceof Date) {
      return DATE_FORMAT.format((Date) value);
    } else {
      return "null";
    }
  }

  /**
   * @return the the name of step from {@link Calendar} fields.
   */
  private static String getDateStep(int calendarField) {
    switch (calendarField) {
      case Calendar.ERA :
        return "ERA";
      case Calendar.YEAR :
        return "YEAR";
      case Calendar.MONTH :
        return "MONTH";
      case Calendar.WEEK_OF_YEAR :
        return "WEEK_OF_YEAR";
      case Calendar.WEEK_OF_MONTH :
        return "WEEK_OF_MONTH";
      case Calendar.DAY_OF_MONTH :
        return "DAY_OF_MONTH";
      case Calendar.DAY_OF_YEAR :
        return "DAY_OF_YEAR";
      case Calendar.DAY_OF_WEEK :
        return "DAY_OF_WEEK";
      case Calendar.DAY_OF_WEEK_IN_MONTH :
        return "DAY_OF_WEEK_IN_MONTH";
      case Calendar.AM_PM :
        return "AM_PM";
      case Calendar.HOUR :
        return "HOUR";
      case Calendar.HOUR_OF_DAY :
        return "HOUR_OF_DAY";
      case Calendar.MINUTE :
        return "MINUTE";
      case Calendar.SECOND :
        return "SECOND";
      case Calendar.MILLISECOND :
        return "MILLISECOND";
      default :
        return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof SpinnerModel) {
      SpinnerModel model = (SpinnerModel) value;
      SpinnerModelDialog modelDialog =
          new SpinnerModelDialog(DesignerPlugin.getShell(), property.getTitle(), model);
      // open dialog
      if (modelDialog.open() == Window.OK) {
        GenericProperty genericProperty = (GenericProperty) property;
        genericProperty.setExpression(modelDialog.getSource(), Property.UNKNOWN_VALUE);
      }
    }
  }
}
