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
package org.eclipse.wb.internal.core.xml.model.property.event;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.jface.action.IMenuManager;

/**
 * Interface of single listener {@link Property}.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public abstract class AbstractListenerProperty extends AbstractEventProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractListenerProperty(XmlObjectInfo object, String title, PropertyEditor propertyEditor) {
    super(object, title, propertyEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens Java editor with event listener, creates it if needed and possible.
   */
  protected abstract void openListener() throws Exception;

  /**
   * Removes listener for this event.
   */
  protected abstract void removeListener() throws Exception;

  /**
   * Contributes actions into context menu.
   */
  protected abstract void addListenerActions(IMenuManager manager, IMenuManager implementMenuManager)
      throws Exception;
}
