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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodCreationSupport;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;

import org.eclipse.jdt.core.dom.Expression;

import org.apache.commons.lang.StringUtils;

/**
 * Implementation of {@link CreationSupport} for creating {@link ViewerColumnInfo} during creating
 * corresponding column {@link WidgetInfo}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.jface.viewers
 */
public final class ViewerColumnCreationSupport extends WrapperMethodCreationSupport {
  private final ViewerColumnInfo m_viewer;
  private final boolean m_addInvocations;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerColumnCreationSupport(ViewerColumnInfo viewer) {
    this(viewer, true);
  }

  public ViewerColumnCreationSupport(ViewerColumnInfo viewer, boolean addInvocations) {
    super(viewer.getWrapper());
    m_viewer = viewer;
    m_addInvocations = addInvocations;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Association getAssociation() throws Exception {
    return new ViewerColumnWidgetAssociation(m_viewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    // ViewerColumn accepts Viewer as %parent%, not Control
    m_javaInfo.addBroadcastListener(new JavaEventListener() {
      @Override
      public void associationTemplate(JavaInfo component, String[] source) throws Exception {
        if (component == m_javaInfo) {
          ControlInfo control = (ControlInfo) m_javaInfo.getParent();
          ViewerInfo viewer = control.getChildren(ViewerInfo.class).get(0);
          source[0] =
              StringUtils.replace(source[0], "%parent%", TemplateUtils.getExpression(viewer));
        }
      }
    });
    // prepare ViewerColumn creation source
    return super.add_getSource(target);
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
    super.add_setSourceExpression(expression);
    // add invocations for column
    if (m_addInvocations) {
      for (int index = 0;; index++) {
        // get invocation from parameters
        String invocationText;
        {
          invocationText = JavaInfoUtils.getParameter(m_viewer, "ViewerColumn.invocation." + index);
          if (invocationText == null) {
            break;
          }
        }
        // add single invocation
        int spaceIndex = invocationText.indexOf(' ');
        String signature = invocationText.substring(0, spaceIndex);
        String arguments = invocationText.substring(spaceIndex + 1);
        m_javaInfo.addMethodInvocation(signature, arguments);
      }
    }
  }

  @Override
  protected CreationSupport newControlCreationSupport() {
    return new ViewerColumnWidgetCreationSupport(m_viewer);
  }
}
