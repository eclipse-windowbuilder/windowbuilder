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
package org.eclipse.wb.internal.rcp.model.jface.viewers;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import java.util.List;

/**
 * Model for any JFace {@link org.eclipse.jface.viewers.ViewerColumn}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage rcp.model.jface.viewers
 */
public final class TableViewerColumnInfo extends ViewerColumnInfo {
  private final TableViewerColumnInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableViewerColumnInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.add(createSorterProperty());
    return properties;
  }

  private JavaProperty createSorterProperty() {
    return new JavaProperty(this, "sorter", TableViewerColumnSorterPropertyEditor.INSTANCE) {
      @Override
      public boolean isModified() throws Exception {
        return getValue() != null;
      }

      @Override
      public Object getValue() throws Exception {
        for (ASTNode node : getRelatedNodes()) {
          if (node.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
            ClassInstanceCreation creation = (ClassInstanceCreation) node.getParent();
            if (AstNodeUtils.isSuccessorOf(creation, "org.eclipse.wb.swt.TableViewerColumnSorter")) {
              return creation;
            }
          }
        }
        return null;
      }

      @Override
      public void setValue(Object value) throws Exception {
        final ASTNode node = (ASTNode) getValue();
        if (node != null) {
          ExecutionUtils.run(m_this, new RunnableEx() {
            @Override
            public void run() throws Exception {
              m_this.getEditor().removeEnclosingStatement(node);
            }
          });
        }
      }
    };
  }
}
