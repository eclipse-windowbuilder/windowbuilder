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

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * {@link IReferenceProvider} over {@link VariableDeclarationFragment}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class FragmentReferenceProvider implements IReferenceProvider {
  private VariableDeclarationFragment m_fragment;
  private IReferenceProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public FragmentReferenceProvider(VariableDeclarationFragment fragment) {
    setFragment(fragment);
  }

  public FragmentReferenceProvider(IReferenceProvider provider) {
    m_provider = provider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IReferenceProvider getProvider() {
    return m_provider;
  }

  public VariableDeclarationFragment getFragment() {
    return m_fragment;
  }

  public void setFragment(VariableDeclarationFragment fragment) {
    m_fragment = fragment;
    m_provider = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReferenceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getReference() throws Exception {
    return m_provider == null ? m_fragment.getName().getIdentifier() : m_provider.getReference();
  }
}