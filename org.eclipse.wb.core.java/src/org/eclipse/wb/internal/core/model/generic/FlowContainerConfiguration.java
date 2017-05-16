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

import com.google.common.base.Predicate;

import org.eclipse.wb.core.model.association.AssociationObjectFactory;

/**
 * Configuration for {@link FlowContainerConfigurable}.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public class FlowContainerConfiguration {
  private final ContainerObjectValidator m_componentValidator;
  private final ContainerObjectValidator m_referenceValidator;
  private final Predicate<Object> m_horizontalPredicate;
  private final Predicate<Object> m_rtlPredicate;
  private final AssociationObjectFactory m_associationObjectFactory;
  private final String m_groupName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlowContainerConfiguration(Predicate<Object> horizontalPredicate,
      Predicate<Object> rtlPredicate,
      AssociationObjectFactory associationObjectFactory,
      ContainerObjectValidator componentValidator,
      ContainerObjectValidator referenceValidator,
      String groupName) {
    m_horizontalPredicate = horizontalPredicate;
    m_rtlPredicate = rtlPredicate;
    m_associationObjectFactory = associationObjectFactory;
    m_componentValidator = componentValidator;
    m_referenceValidator = referenceValidator;
    m_groupName = groupName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Predicate<Object> getHorizontalPredicate() {
    return m_horizontalPredicate;
  }

  public Predicate<Object> getRtlPredicate() {
    return m_rtlPredicate;
  }

  public AssociationObjectFactory getAssociationObjectFactory() {
    return m_associationObjectFactory;
  }

  public ContainerObjectValidator getComponentValidator() {
    return m_componentValidator;
  }

  public ContainerObjectValidator getReferenceValidator() {
    return m_referenceValidator;
  }

  public String getGroupName() {
    return m_groupName;
  }
}
