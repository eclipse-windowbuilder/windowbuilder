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

import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;

import java.util.Collection;
import java.util.List;

/**
 * This class used for route choose class and properties events from <code>source</code>
 * {@link ChooseClassAndPropertiesUiContentProvider} to <code>target</code>
 * {@link ChooseClassUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class ChooseClassAndPropertiesRouter {
  private final ChooseClassAndPropertiesUiContentProvider m_source;
  private final ChooseClassUiContentProvider m_target;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassAndPropertiesRouter(ChooseClassAndPropertiesUiContentProvider source,
      ChooseClassUiContentProvider target) {
    m_source = source;
    m_target = target;
    source.setRouter(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  void handle() {
    // prepare checked properties
    List<PropertyAdapter> properties = m_source.getChoosenProperties0();
    // reset target checked properties
    if (m_target instanceof ChooseClassAndPropertiesUiContentProvider) {
      ChooseClassAndPropertiesUiContentProvider target =
          (ChooseClassAndPropertiesUiContentProvider) m_target;
      target.getPropertiesViewer().setAllChecked(false);
    }
    // handle properties
    if (properties.isEmpty()) {
      // properties is empty
      setClearValue(false);
    } else {
      // prepare property type
      Class<?> elementType = properties.get(0).getType();
      // check type
      if (elementType.isPrimitive() || elementType.isArray()) {
        // skip arrays and primitive type
        setClearValue(false);
      } else if (Collection.class.isAssignableFrom(elementType)) {
        // if type is collection configure target to choose element type
        setClearValue(true);
        ComboDialogField combo = (ComboDialogField) m_target.getDialogField();
        combo.removeItem("");
      } else {
        // redirect value
        m_target.getDialogField().setEnabled(false);
        m_target.setClassName(elementType.getName());
      }
    }
  }

  private void setClearValue(boolean enabled) {
    m_target.getDialogField().setEnabled(enabled);
    m_target.setClearClassName();
  }
}