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
package org.eclipse.wb.internal.core.model.property.editor.style.actions;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

/**
 * Abstract action for {@link Property} context menu.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public abstract class SubStyleAction extends ObjectInfoAction {
  private final Property m_property;
  private final SubStylePropertyImpl m_propertyImpl;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SubStyleAction(Property property,
      SubStylePropertyImpl propertyImpl,
      String title,
      int style) {
    super(getHostInfo(property), title, style);
    m_property = property;
    m_propertyImpl = propertyImpl;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectInfoAction
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void runEx() throws Exception {
    m_propertyImpl.setValue(m_property, getActionValue());
  }

  /**
   * @return the new "action" value.
   */
  protected abstract Object getActionValue();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ObjectInfo getHostInfo(Property property) {
    return property.getAdapter(ObjectInfo.class);
  }
}