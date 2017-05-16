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
package org.eclipse.wb.internal.core.model.generation;

import org.eclipse.wb.internal.core.utils.binding.DataBindManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract description for code generation.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public abstract class GenerationDescription {
  private final String m_id;
  private final String m_name;
  private final String m_description;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected GenerationDescription(String id, String name, String description) {
    m_id = id;
    m_name = name;
    m_description = description;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the id of this description.
   */
  public final String getId() {
    return m_id;
  }

  /**
   * @return the name of this description.
   */
  public final String getName() {
    return m_name;
  }

  /**
   * @return the textual description to show for user.
   */
  public final String getDescription() {
    return m_description;
  }

  /**
   * Sets the default values for preferences.
   */
  public void configureDefaultPreferences(IPreferenceStore store) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties composite
  //
  ////////////////////////////////////////////////////////////////////////////
  public abstract GenerationPropertiesComposite createPropertiesComposite(Composite parent,
      DataBindManager bindManager,
      IPreferenceStore store);
}
