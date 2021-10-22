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

import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.properties.ElPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el.ElPropertyUiConfiguration;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Composite;

/**
 * Editor for {@code EL} properties.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class ElPropertyUiContentProvider
    extends
      org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el.ElPropertyUiContentProvider {
  private ElPropertyInfo m_property;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElPropertyUiContentProvider(ElPropertyUiConfiguration configuration,
      ElPropertyInfo property) {
    super(configuration);
    m_property = property;
    setEnabled(m_property != null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElPropertyInfo getProperty() {
    return m_property;
  }

  public void setProperty(ElPropertyInfo property) throws Exception {
    m_property = property;
    setEnabled(m_property != null);
    if (m_property == null) {
      setText(Messages.ElPropertyUiContentProvider_0);
    } else {
      updateFromObject();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContent(Composite parent, int columns) {
    super.createContent(parent, columns);
    m_sourceViewer.getDocument().addDocumentListener(new IDocumentListener() {
      public void documentChanged(DocumentEvent event) {
        calculateFinish();
      }

      public void documentAboutToBeChanged(DocumentEvent event) {
      }
    });
  }

  public Class<?> getTopLevelBean() throws Exception {
    if (m_property == null) {
      return null;
    }
    PropertyInfo baseProperty = m_property.getBaseProperty();
    if (baseProperty != null) {
      return baseProperty.getValueType().getRawType();
    }
    return m_property.getSourceObjectType().getRawType();
  }

  public void calculateFinish() {
    if (getText().length() == 0) {
      setErrorMessage(Messages.ElPropertyUiContentProvider_errEmpty);
    } else {
      setErrorMessage(null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    setText(m_property.getExpression());
  }

  public void saveToObject() throws Exception {
    m_property.setExpression(getText());
  }
}