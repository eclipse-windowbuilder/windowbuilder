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
package org.eclipse.wb.internal.core.model.generic;

import org.eclipse.wb.core.model.association.AssociationObjectFactory;

/**
 * Configuration for {@link SimpleContainerConfigurable}.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public class SimpleContainerConfiguration {
  private final ContainerObjectValidator m_componentValidator;
  private final AssociationObjectFactory m_associationObjectFactory;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleContainerConfiguration(ContainerObjectValidator componentValidator,
      AssociationObjectFactory associationObjectFactory) {
    m_componentValidator = componentValidator;
    m_associationObjectFactory = associationObjectFactory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public AssociationObjectFactory getAssociationObjectFactory() {
    return m_associationObjectFactory;
  }

  public ContainerObjectValidator getComponentValidator() {
    return m_componentValidator;
  }
}
