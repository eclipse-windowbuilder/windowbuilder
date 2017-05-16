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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.internal.core.model.description.internal.AbstractConfigurableDescription;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyFactory;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Description of artificial {@link Property}.
 * <p>
 * For example in GWT <code>ListBox</code> has only <code>addItem(String)</code> methods, and no
 * method to set all items as single invocation, like <code>setItems(String[])</code>. So, to edit
 * items, we need some artificial {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ConfigurablePropertyDescription extends AbstractConfigurableDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ID
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_id;

  /**
   * @return the ID of {@link IConfigurablePropertyFactory} that should be used to create
   *         {@link Property}.
   */
  public String getId() {
    return m_id;
  }

  /**
   * Sets the ID of {@link IConfigurablePropertyFactory} that should be used to create
   * {@link Property}.
   */
  public void setId(String id) {
    m_id = id;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Title
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_title;

  /**
   * @return the title of {@link Property} to display for user.
   */
  public String getTitle() {
    return m_title;
  }

  /**
   * Sets the title of {@link Property} to display for user.
   */
  public void setTitle(String title) {
    m_title = title;
  }
}
