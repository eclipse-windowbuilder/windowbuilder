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
package org.eclipse.wb.internal.core.parser;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;

/**
 * The context that is used by {@link JavaInfoParser} as information about root.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public final class ParseRootContext {
  private final JavaInfo m_root;
  private final ExecutionFlowDescription m_flowDescription;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ParseRootContext(JavaInfo root, ExecutionFlowDescription flowDescription) {
    m_root = root;
    m_flowDescription = flowDescription;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the root {@link JavaInfo}.
   */
  public JavaInfo getRoot() {
    return m_root;
  }

  /**
   * @return the {@link ExecutionFlowDescription} from which parsing should be started.
   */
  public ExecutionFlowDescription getFlowDescription() {
    return m_flowDescription;
  }
}
