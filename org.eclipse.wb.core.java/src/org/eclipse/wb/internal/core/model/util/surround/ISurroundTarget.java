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
package org.eclipse.wb.internal.core.model.util.surround;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.graphics.Image;

import java.util.List;

import javax.swing.JSplitPane;

/**
 * Target container to surround several {@link IAbstractComponentInfo}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public abstract class ISurroundTarget<C extends IAbstractComponentInfo, T extends IAbstractComponentInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the text to display for user. This can be some static text, variable name, etc.
   */
  public abstract String getText(AstEditor editor) throws Exception;

  /**
   * @return the icon to display for user.
   */
  public abstract Image getIcon(AstEditor editor) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given components can be surrounded by this target. For example
   *         {@link JSplitPane} can accept at max two components, so returns <code>false</code>, if
   *         number of components is greater than two.
   */
  public boolean validate(List<T> components) throws Exception {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the new target container to move components on.
   */
  public abstract C createContainer(AstEditor editor) throws Exception;

  /**
   * Initializes container after adding it to the components hierarchy.
   *
   * @param container
   *          the target container.
   * @param components
   *          the components that should be moved later. We can initialize target container
   *          differently, depending on components count/type.
   */
  public void afterContainerAdd(C container, List<T> components) throws Exception {
  }

  /**
   * Initializes container before moving components on this container using
   * {@link #move(AbstractComponentInfo, AbstractComponentInfo)}.
   *
   * @param container
   *          the target container.
   * @param components
   *          the components that will be moved later. We can initialize target container
   *          differently, depending on components count/type.
   */
  public void beforeComponentsMove(C container, List<T> components) throws Exception {
  }

  /**
   * Moves component to target container.
   *
   * @param container
   *          the target container.
   * @param component
   *          the component to move on target container.
   */
  public abstract void move(C container, T component) throws Exception;
}
