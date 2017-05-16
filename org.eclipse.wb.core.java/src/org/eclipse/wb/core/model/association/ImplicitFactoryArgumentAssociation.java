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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Implementation of {@link Association} for {@link JavaInfo} passed as argument into
 * <code>implicitFactory</code> methods. When any operation that breaks association is performed on
 * this "argument" {@link JavaInfo}, then component created using implicit factory should be
 * deleted.
 * <p>
 * For example in GWT <code>Tree.addItem(Widget)</code>, the <code>Widget</code> argument is
 * associated with created <code>TreeItem</code> using {@link ImplicitFactoryArgumentAssociation}.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class ImplicitFactoryArgumentAssociation extends InvocationAssociation {
  private final JavaInfo m_factoryJavaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param factoryJavaInfo
   *          the {@link JavaInfo} created by implicit factory, where this {@link JavaInfo} is
   *          argument.
   */
  public ImplicitFactoryArgumentAssociation(MethodInvocation invocation, JavaInfo factoryJavaInfo) {
    super(invocation);
    m_factoryJavaInfo = factoryJavaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(final JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    // argument of implicit factory can not be moved
    javaInfo.addBroadcastListener(new JavaEventListener() {
      @Override
      public void canMove(JavaInfo _javaInfo, boolean[] forceMoveEnable, boolean[] forceMoveDisable)
          throws Exception {
        if (_javaInfo == javaInfo) {
          forceMoveDisable[0] = true;
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean remove() throws Exception {
    // our JavaInfo is in delete/reparent process, so schedule delete m_factoryJavaInfo
    m_factoryJavaInfo.getRootJava().addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (child == m_javaInfo) {
          if (!m_factoryJavaInfo.isDeleting()) {
            m_factoryJavaInfo.delete();
          }
        }
      }
    });
    m_factoryJavaInfo.getRootJava().addBroadcastListener(new JavaEventListener() {
      @Override
      public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        if (child == m_javaInfo) {
          m_factoryJavaInfo.delete();
        }
      }
    });
    // continue with default implementation
    return super.remove();
  }
}
