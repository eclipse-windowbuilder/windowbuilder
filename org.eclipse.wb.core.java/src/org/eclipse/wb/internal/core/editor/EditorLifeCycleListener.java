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
package org.eclipse.wb.internal.core.editor;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * This interface is used to notify external listeners about editor life cycle.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public abstract class EditorLifeCycleListener {
  /**
   * @return <code>false</code> if given {@link ICompilationUnit} can be parsed quickly, so no
   *         progress required; or <code>true</code> if progress should be displayed.
   */
  public boolean parseWithProgress(Object editor, ICompilationUnit unit) {
    return true;
  }

  /**
   * Parsing is about to start.
   */
  public void parseStart(Object editor) throws Exception {
  }

  /**
   * Parsing was finished (successfully or not).
   */
  public void parseEnd(Object editor) throws Exception {
  }

  /**
   * Hierarchy was disposed in editor, so may be context of editor also should be disposed.
   * Sometimes however we don't want to throw away all information about editor.
   *
   * @param force
   *          is <code>true</code> if user closes editor, so context should be disposed.
   */
  public void disposeContext(Object editor, boolean force) throws Exception {
  }
}
