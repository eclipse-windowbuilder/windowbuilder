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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.ASTNode;

import java.awt.Component;
import java.awt.GridBagConstraints;

/**
 * Implementation of {@link CreationSupport} for virtual {@link AbstractGridBagConstraintsInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class VirtualConstraintsCreationSupport extends CreationSupport {
  private final VirtualConstraintsCreationSupport m_this = this;
  private final AbstractGridBagLayoutInfo m_layout;
  private final ComponentInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualConstraintsCreationSupport(ComponentInfo component) throws Exception {
    m_component = component;
    m_layout = (AbstractGridBagLayoutInfo) ((ContainerInfo) m_component.getParent()).getLayout();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isJavaInfo(ASTNode node) {
    return false;
  }

  @Override
  public ASTNode getNode() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    // evaluate during each "refresh"
    m_javaInfo.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshAfterCreate() throws Exception {
        if (m_javaInfo.getCreationSupport() == m_this) {
          evaluateConstraints();
        } else {
          m_javaInfo.removeBroadcastListener(this);
        }
      }
    });
    // evaluate first time now
    evaluateConstraints();
  }

  /**
   * Evaluates current {@link GridBagConstraints} value.
   */
  private void evaluateConstraints() throws Exception {
    Component component = m_component.getComponent();
    // prepare constraints
    Object constraints = m_layout.getConstraintsObject(component);
    // set constraints object
    m_javaInfo.setObject(constraints);
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
    m_component.removeChild(m_javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "virtual-GBL-constraints";
  }
}