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
package org.eclipse.wb.internal.core.utils.state;

import com.google.common.collect.Sets;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Collection;
import java.util.Set;

/**
 * Information about visited or evaluated {@link ASTNode}s.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public final class VisitedNodes {
  private final Set<ASTNode> m_nodes = Sets.newHashSet();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Clears visited nodes before performing new visiting operation.
   */
  public void clear() {
    m_nodes.clear();
  }

  /**
   * Adds {@link ASTNode} which was visited or evaluated.
   */
  public void add(ASTNode node) {
    m_nodes.add(node);
  }

  /**
   * @return the visited {@link ASTNode}s.
   */
  public Collection<ASTNode> getNodes() {
    return m_nodes;
  }
}
