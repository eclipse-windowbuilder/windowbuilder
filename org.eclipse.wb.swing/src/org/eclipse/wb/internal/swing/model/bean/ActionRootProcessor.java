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
package org.eclipse.wb.internal.swing.model.bean;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;

import java.util.List;

/**
 * Support for binding {@link ActionInfo}'s to hierarchy.
 * 
 * @author sablin_aa
 * @coverage swing.model
 */
public final class ActionRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new ActionRootProcessor();

  private ActionRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
    for (JavaInfo javaInfo : components) {
      if (javaInfo instanceof ActionInfo) {
        ActionInfo actionInfo = (ActionInfo) javaInfo;
        ActionContainerInfo.get(root).addAction(actionInfo);
      }
    }
  }
}
