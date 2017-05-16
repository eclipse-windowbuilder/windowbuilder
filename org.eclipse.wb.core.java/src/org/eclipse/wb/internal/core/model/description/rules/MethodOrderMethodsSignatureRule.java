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
package org.eclipse.wb.internal.core.model.description.rules;

import org.apache.commons.digester.Rule;

import java.util.List;

/**
 * The {@link Rule} that adds single method signature for {@link MethodOrderMethodsRule}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderMethodsSignatureRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  public void body(String namespace, String name, String text) throws Exception {
    List<String> signatures = (List<String>) digester.peek();
    String signature = text.trim();
    signatures.add(signature);
  }
}
