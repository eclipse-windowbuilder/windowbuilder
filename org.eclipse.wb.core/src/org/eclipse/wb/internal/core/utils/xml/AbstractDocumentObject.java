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
package org.eclipse.wb.internal.core.utils.xml;

/**
 * Abstract superclass for all XML model objects.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public abstract class AbstractDocumentObject {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  private Model m_model;

  public final void setModel(Model model) {
    m_model = model;
  }

  public final Model getModel() {
    return m_model;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Notification utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void firePropertyChanged(AbstractDocumentObject object,
      String property,
      Object oldValue,
      Object newValue) {
    ModelChangedEvent e =
        new ModelChangedEvent(ModelChangedEvent.CHANGE, object, property, oldValue, newValue);
    fireModelChanged(e);
  }

  protected final void fireStructureChanged(AbstractDocumentObject child, int changeType) {
    ModelChangedEvent e = new ModelChangedEvent(changeType, child, null);
    fireModelChanged(e);
  }

  protected final void fireModelChanged(ModelChangedEvent e) {
    if (m_model != null) {
      m_model.fireModelChanged(e);
    }
  }
}
