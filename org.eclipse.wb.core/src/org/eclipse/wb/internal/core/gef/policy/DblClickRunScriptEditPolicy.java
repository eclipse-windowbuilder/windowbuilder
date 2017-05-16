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
package org.eclipse.wb.internal.core.gef.policy;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

/**
 * {@link EditPolicy} that runs given MVEL script on double click.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class DblClickRunScriptEditPolicy extends EditPolicy {
  private final ObjectInfo m_component;
  private final String m_script;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DblClickRunScriptEditPolicy(ObjectInfo component, String script) {
    m_component = component;
    m_script = script;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (Request.REQ_OPEN.equals(request.getType())) {
      ExecutionUtils.run(m_component, new RunnableEx() {
        public void run() throws Exception {
          ScriptUtils.evaluate(m_script, m_component);
        }
      });
    }
  }
}
