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
package org.eclipse.wb.internal.rcp.nebula.grid;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Model {@link Grid}
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class GridInfo extends CompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final GridInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListener_forTarget();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that all children are added before association {@link Statement}.
   */
  private void addBroadcastListener_forTarget() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void target_isTerminalStatement(JavaInfo parent,
          JavaInfo child,
          Statement statement,
          boolean[] terminal) {
        if (parent == m_this
            && (child instanceof GridColumnInfo || child instanceof GridColumnGroupInfo)) {
          List<GridItemInfo> items = getChildren(GridItemInfo.class);
          for (GridItemInfo item : items) {
            List<ASTNode> itemNodes = item.getRelatedNodes();
            for (ASTNode node : itemNodes) {
              ASTNode current = node;
              do {
                if (statement.equals(current)) {
                  terminal[0] = true;
                  return;
                }
                current = current.getParent();
              } while (current != null);
            }
          }
        }
      }
    });
  }
}
