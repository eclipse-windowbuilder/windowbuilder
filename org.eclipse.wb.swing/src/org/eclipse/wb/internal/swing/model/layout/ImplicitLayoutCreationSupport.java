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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.awt.Container;

/**
 * Implementation of {@link CreationSupport} for implicit {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class ImplicitLayoutCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport {
  private final ContainerInfo m_container;
  private final Object m_objectListener1 = new ObjectInfoChildAddBefore() {
    public void invoke(ObjectInfo parent, ObjectInfo child, ObjectInfo[] nextChild)
        throws Exception {
      if (isAddNewLayout(parent, child) && parent.getChildren().contains(m_javaInfo)) {
        if (nextChild[0] == m_javaInfo) {
          nextChild[0] = GenericsUtils.getNextOrNull(parent.getChildren(), m_javaInfo);
        }
        parent.removeChild(m_javaInfo);
      }
    }
  };
  private final Object m_objectListener2 = new ObjectEventListener() {
    @Override
    public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
      if (useImplicitLayout() && isAddNewLayout(parent, child)) {
        parent.addChild(m_javaInfo);
        ((LayoutInfo) m_javaInfo).onSet();
      }
    }
  };
  private final Object m_javaListener = new JavaInfoSetObjectAfter() {
    public void invoke(JavaInfo target, Object o) throws Exception {
      if (target == m_container) {
        if (m_javaInfo.getObject() == null) {
          Container container = (Container) o;
          m_javaInfo.setObject(container.getLayout());
        }
      }
    }
  };

  /**
   * @return <code>true</code> if implicit layout should be added/removed.
   */
  private boolean useImplicitLayout() {
    return m_container.getArbitraryValue(ContainerInfo.KEY_DONT_SET_IMPLICIT_LAYOUT) != Boolean.TRUE;
  }

  /**
   * @return <code>true</code> if given combination of parent/child is adding new {@link LayoutInfo}
   *         on our {@link ContainerInfo}.
   */
  private boolean isAddNewLayout(ObjectInfo parent, ObjectInfo child) {
    return parent == m_container && child instanceof LayoutInfo && child != m_javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImplicitLayoutCreationSupport(ContainerInfo containerInfo) {
    m_container = containerInfo;
    m_container.addBroadcastListener(m_objectListener1);
    m_container.addBroadcastListener(m_objectListener2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    Class<?> layoutClass = getComponentClass();
    // check for absolute layout
    if (layoutClass == null) {
      return "implicit-layout: absolute";
    }
    // "real" layout
    return "implicit-layout: " + layoutClass.getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    m_container.addBroadcastListener(m_javaListener);
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      return invocation.arguments().isEmpty()
          && invocation.getName().getIdentifier().equals("getLayout")
          && m_container.isRepresentedBy(invocation.getExpression());
    }
    return false;
  }

  @Override
  public ASTNode getNode() {
    return m_container.getCreationSupport().getNode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void removeForever() throws Exception {
    m_container.removeBroadcastListener(m_objectListener1);
    m_container.removeBroadcastListener(m_objectListener2);
    m_container.removeBroadcastListener(m_javaListener);
    m_container.removeChild(m_javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    String layoutClassName = m_javaInfo.getDescription().getComponentClass().getName();
    return TemplateUtils.format("({0}) {1}.getLayout()", layoutClassName, m_container);
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return true;
  }

  @Override
  public void delete() throws Exception {
    JavaInfoUtils.deleteJavaInfo(m_javaInfo, false);
    // if implicit layout was materialized, so has real variable, restore implicit variable
    if (!(m_javaInfo.getVariableSupport() instanceof ImplicitLayoutVariableSupport)) {
      m_javaInfo.setVariableSupport(new ImplicitLayoutVariableSupport(m_javaInfo));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardImplicitCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public IClipboardImplicitCreationSupport getImplicitClipboard() {
    return null;
  }
}
