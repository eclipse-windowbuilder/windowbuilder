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

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

/**
 * Implementation of {@link Association} for {@link JavaInfo} passed as argument of
 * {@link SuperConstructorInvocation}.
 * <p>
 * For example: <code><pre>
 *   public class Test extends JPanel {
 *     public Test() {
 *       this(new BorderLayout());
 *     }
 *   }
 * </pre><code>
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class SuperConstructorArgumentAssociation extends Association {
  private final SuperConstructorInvocation m_invocation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SuperConstructorArgumentAssociation(SuperConstructorInvocation invocation) {
    m_invocation = invocation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Statement getStatement() {
    return m_invocation;
  }

  @Override
  public String getSource() {
    return m_editor.getSource(m_invocation);
  }

  @Override
  public boolean canDelete() {
    return false;
  }
}
