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
package org.eclipse.wb.internal.core.databinding.model.reference;

/**
 * Implementation of {@link IReferenceProvider} that consists of two other
 * {@link IReferenceProvider}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class CompoundReferenceProvider implements IReferenceProvider {
  private final IReferenceProvider m_leftReferenceProvider;
  private final String m_rightReference;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompoundReferenceProvider(IReferenceProvider leftReferenceProvider, String rightReference) {
    m_leftReferenceProvider = leftReferenceProvider;
    m_rightReference = rightReference;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReferenceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getReference() throws Exception {
    return m_leftReferenceProvider.getReference() + m_rightReference;
  }
}