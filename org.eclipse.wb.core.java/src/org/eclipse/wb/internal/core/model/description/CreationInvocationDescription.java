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

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Description for adding {@link MethodInvocation} during creation.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class CreationInvocationDescription extends AbstractDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Signature
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_signature;

  public String getSignature() {
    return m_signature;
  }

  public void setSignature(String signature) {
    m_signature = signature;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arguments
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_arguments;

  public String getArguments() {
    return m_arguments;
  }

  public void setArguments(String arguments) {
    m_arguments = arguments;
  }
}
