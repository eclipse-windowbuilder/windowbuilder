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
package org.eclipse.wb.internal.swing.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;

import java.util.Collections;
import java.util.List;

/**
 * Fake {@link ObserveInfo} for undefined properties.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class UndefineObserveInfo extends ObserveInfo {
  private final IObservePresentation m_presentation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UndefineObserveInfo(String text, IReferenceProvider referenceProvider) {
    super(null, referenceProvider);
    m_presentation = new SimpleObservePresentation(text, text, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return null;
  }

  @Override
  public ObserveCreationType getCreationType() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveInfo getParent() {
    return null;
  }

  public List<IObserveInfo> getChildren(ChildrenContext context) {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObservePresentation getPresentation() {
    return m_presentation;
  }
}