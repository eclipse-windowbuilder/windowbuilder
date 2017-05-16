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
package org.eclipse.wb.internal.core.model.description;

/**
 * Description for morphing target.<br>
 * Each target is pointer on {@link CreationDescription}, i.e. target component class and creation
 * id.
 * <p>
 * Morphing is process of replacing component of one class with component of other class. It keeps
 * method invocations and field assignments that are possible for target component, and removes
 * other ones. For example, morphing SWT <code>Button</code> into <code>Text</code> will look so:
 * <p>
 * Before:
 *
 * <pre>
 *   Button button = new Button(parent, SWT.NONE);
 *   button.setText("my button");
 *   button.setSelection(true);
 * </pre>
 *
 * <p>
 * After:
 *
 * <pre>
 *   Text button = new Text(parent, SWT.NONE);
 *   button.setText("my button");
 * </pre>
 *
 * Note that <code>setSelection(true)</code> was removed because in <code>Text</code> this method
 * has different signature.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MorphingTargetDescription {
  private final Class<?> m_componentClass;
  private final String m_creationId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MorphingTargetDescription(Class<?> componentClass, String creationId) {
    m_componentClass = componentClass;
    m_creationId = creationId;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the target component class.
   */
  public Class<?> getComponentClass() {
    return m_componentClass;
  }

  /**
   * @return the creation id for target class.
   */
  public String getCreationId() {
    return m_creationId;
  }
}
