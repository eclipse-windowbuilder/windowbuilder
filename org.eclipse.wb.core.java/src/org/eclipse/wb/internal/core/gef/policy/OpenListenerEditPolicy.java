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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.event.EventsProperty;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.apache.commons.lang.StringUtils;

/**
 * {@link EditPolicy} which adds listener code by handling "Open" request (double-clicking on its
 * {@link EditPart}). Component description should have lines like this:
 *
 * <pre><code>
 * &lt;parameters&gt;
 *   &lt;parameter name="x.double-click.listener" value="selectionListener/widgetSelected"/&gt;
 * &lt;/parameters&gt; </code></pre>
 *
 * Where parameter <code>double-click.listener</code> is '/' separated name of listener method to be
 * created.
 *
 * @author mitin_aa
 * @coverage core.gef.policy
 */
public final class OpenListenerEditPolicy extends EditPolicy {
  private static final String DOUBLE_CLICK_LISTENER = "double-click.listener";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_listenerName;
  private final JavaInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public OpenListenerEditPolicy(JavaInfo javaInfo) {
    m_javaInfo = javaInfo;
    m_listenerName = JavaInfoUtils.getParameter(javaInfo, DOUBLE_CLICK_LISTENER);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    if (!StringUtils.isEmpty(m_listenerName) && Request.REQ_OPEN.equals(request.getType())) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          EventsProperty eventsProperty = (EventsProperty) m_javaInfo.getPropertyByTitle("Events");
          eventsProperty.openStubMethod(m_listenerName);
        }
      });
    }
    super.performRequest(request);
  }
}
