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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.WrapperByMethod;
import org.eclipse.wb.core.model.WrapperMethodInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Implementation of {@link CreationSupport} for {@link ControlInfo} of {@link WrapperMethodInfo}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.creation
 */
public class WrapperMethodControlCreationSupport extends CreationSupport
    implements
      IWrapperControlCreationSupport {
  private final WrapperByMethod m_wrapper;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WrapperMethodControlCreationSupport(WrapperByMethod wrapper) {
    m_wrapper = wrapper;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "viewer: " + m_wrapper.getControlMethod();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link JavaInfo} that wraps this {@link JavaInfo}.
   */
  public final JavaInfo getWrapperInfo() {
    return m_wrapper.getWrapperInfo();
  }

  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    m_javaInfo.addBroadcastListener(new JavaInfoSetObjectAfter() {
      public void invoke(JavaInfo target, Object o) throws Exception {
        if (target == m_wrapper.getWrapperInfo()) {
          Object object = m_wrapper.getControlMethod().invoke(o);
          m_javaInfo.setObject(object);
        }
      }
    });
  }

  @Override
  public ASTNode getNode() {
    return m_wrapper.getWrapperInfo().getCreationSupport().getNode();
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return m_wrapper.isWrappedInfo(node);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReorder() {
    return true;
  }

  @Override
  public boolean canReparent() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    return TemplateUtils.format(
        "{0}.{1}()",
        m_wrapper.getWrapperInfo(),
        m_wrapper.getControlMethod().getName());
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
    CreationSupport wrapperCreation = m_wrapper.getWrapperInfo().getCreationSupport();
    boolean isImplicitWrapper = wrapperCreation instanceof IImplicitCreationSupport;
    JavaInfoUtils.deleteJavaInfo(m_javaInfo, !isImplicitWrapper);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IClipboardCreationSupport getClipboard() throws Exception {
    final JavaInfoMemento viewerMemento = JavaInfoMemento.createMemento(m_wrapper.getWrapperInfo());
    return new IClipboardCreationSupport() {
      private static final long serialVersionUID = 0L;

      @Override
      public CreationSupport create(JavaInfo rootObject) throws Exception {
        IWrapperInfo wrapperInfo = (IWrapperInfo) viewerMemento.create(rootObject);
        return new WrapperMethodCreationSupport((WrapperByMethod) wrapperInfo.getWrapper());
      }

      @Override
      public void apply(JavaInfo javaInfo) throws Exception {
        viewerMemento.apply();
      }
    };
  }
}
