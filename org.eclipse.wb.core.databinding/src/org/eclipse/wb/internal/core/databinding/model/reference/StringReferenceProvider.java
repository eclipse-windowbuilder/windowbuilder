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
 * {@link IReferenceProvider} for static (not changed) references.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class StringReferenceProvider implements IReferenceProvider {
  public static final IReferenceProvider EMPTY = new StringReferenceProvider("");
  private final String m_reference;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringReferenceProvider(String reference) {
    m_reference = reference;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReferenceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getReference() throws Exception {
    return m_reference;
  }
}