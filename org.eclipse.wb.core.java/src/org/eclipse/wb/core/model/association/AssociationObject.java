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
package org.eclipse.wb.core.model.association;

/**
 * Container for {@link Association} and "requirement" flag.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class AssociationObject {
  private final Association m_association;
  private final boolean m_required;
  private final String m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AssociationObject(Association association, boolean required) {
    this(null, association, required);
  }

  public AssociationObject(String title, Association association, boolean required) {
    m_association = association;
    m_required = required;
    m_title = title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    if (m_title != null) {
      return m_title;
    } else {
      return m_association.toString();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Association getAssociation() {
    return m_association;
  }

  public boolean isRequired() {
    return m_required;
  }
}
