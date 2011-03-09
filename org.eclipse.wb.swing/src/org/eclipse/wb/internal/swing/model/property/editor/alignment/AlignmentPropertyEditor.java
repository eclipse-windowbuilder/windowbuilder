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
package org.eclipse.wb.internal.swing.model.property.editor.alignment;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.FloatPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.CompoundPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import java.awt.Component;
import java.util.Map;

import javax.swing.JComponent;

/**
 * The {@link PropertyEditor} for {@link JComponent#setAlignmentX(float)} or
 * {@link JComponent#setAlignmentY(float)}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
abstract class AlignmentPropertyEditor extends FloatPropertyEditor {
  private final Map<Float, ButtonPropertyEditorPresentation> m_valueToPresentation =
      Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AlignmentPropertyEditor(String[] fields, String[] images) {
    Assert.equals(fields.length, images.length);
    for (int i = 0; i < fields.length; i++) {
      final String field = fields[i];
      final float value = ReflectionUtils.getFieldFloat(Component.class, field);
      final Image image = Activator.getImage("info/alignment/" + images[i]);
      ButtonPropertyEditorPresentation presentation =
          new ButtonPropertyEditorPresentation(SWT.TOGGLE) {
            @Override
            protected Image getImage() {
              return image;
            }

            @Override
            protected String getTooltip() {
              return field;
            }

            @Override
            protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
              GenericProperty genericProperty = (GenericProperty) property;
              genericProperty.setExpression("java.awt.Component." + field, value);
            }
          };
      m_presentation.add(presentation);
      // remember presentation for value
      m_valueToPresentation.put(value, presentation);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final CompoundPropertyEditorPresentation m_presentation =
      new CompoundPropertyEditorPresentation() {
        @Override
        public int show(final PropertyTable propertyTable,
            final Property property,
            int x,
            int y,
            int width,
            int height) {
          int presentationWidth = super.show(propertyTable, property, x, y, width, height);
          ExecutionUtils.runLog(new RunnableEx() {
            public void run() throws Exception {
              selectButtonByValue(propertyTable, property);
            }
          });
          return presentationWidth;
        }
      };

  @Override
  public PropertyEditorPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Selects {@link ButtonPropertyEditorPresentation} that corresponds to current value of
   * {@link Property}.
   */
  private void selectButtonByValue(PropertyTable propertyTable, Property property) throws Exception {
    Object value = property.getValue();
    for (Map.Entry<Float, ButtonPropertyEditorPresentation> entry : m_valueToPresentation.entrySet()) {
      ButtonPropertyEditorPresentation presentation = entry.getValue();
      if (entry.getKey().equals(value)) {
        presentation.setSelection(propertyTable, property, true);
      } else {
        presentation.setSelection(propertyTable, property, false);
      }
    }
  }
}
