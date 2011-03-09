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
package org.eclipse.wb.internal.css.model;

import org.eclipse.wb.internal.css.model.root.Model;
import org.eclipse.wb.internal.css.model.root.ModelChangedEvent;

/**
 * Abstract node for any CSS element.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public abstract class CssNode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  private Model m_model;

  /**
   * Sets {@link Model} in which this {@link CssNode} exists.
   */
  public void setModel(final Model model) {
    accept(new CssVisitor() {
      @Override
      public void postVisit(CssNode node) {
        node.m_model = model;
      }
    });
  }

  /**
   * @return the {@link Model} in which this {@link CssNode} exists.
   */
  public final Model getModel() {
    return m_model;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent/child utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks that given node is <code>null</code>, i.e. we set it first time, or throws
   * {@link IllegalStateException}.
   */
  protected final void checkNull(CssNode node) {
    if (node != null) {
      throw new IllegalStateException("This method can be used only during parse.");
    }
  }

  /**
   * Adapts given node this node as parent.
   */
  protected final void adapt(CssNode node) {
    node.setParent(this);
    node.setModel(m_model);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent
  //
  ////////////////////////////////////////////////////////////////////////////
  private CssNode m_parent;

  public final void setParent(CssNode parent) {
    m_parent = parent;
  }

  public final CssNode getParent() {
    return m_parent;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_offset;
  private int m_length;

  /**
   * Sets offset.
   */
  public final void setOffset(int offset) {
    m_offset = offset;
  }

  /**
   * @return the offset - beginning of first character in document.
   */
  public final int getOffset() {
    return m_offset;
  }

  /**
   * Sets length.
   */
  public final void setLength(int length) {
    m_length = length;
  }

  /**
   * @return the length - count of characters in document for this node.
   */
  public final int getLength() {
    return m_length;
  }

  /**
   * Sets length using "end offset".
   */
  public final void setEnd(int end) {
    m_length = end - m_offset;
  }

  /**
   * @return the end position - offset + length.
   */
  public final int getEnd() {
    return m_offset + m_length;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Notification utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Property was changed.
   */
  protected final void firePropertyChanged(CssNode object,
      String property,
      Object oldValue,
      Object newValue) {
    ModelChangedEvent e = new ModelChangedEvent(object, property, newValue);
    fireModelChanged(e);
  }

  /**
   * Child {@link CssNode} was inserted or removed.
   */
  protected final void fireStructureChanged(CssNode child, int changeType) {
    ModelChangedEvent e = new ModelChangedEvent(changeType, child);
    fireModelChanged(e);
  }

  /**
   * Notifies {@link Model} about the {@link ModelChangedEvent}.
   */
  private void fireModelChanged(ModelChangedEvent e) {
    if (m_model != null) {
      m_model.fireModelChanged(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  public abstract void accept(CssVisitor visitor);
}
