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
import org.eclipse.wb.core.model.association.EmptyAssociation;

import java.util.List;

/**
 * Support for binding {@link ButtonGroupInfo}'s to hierarchy.
 * 
 * @author sablin_aa
 * @coverage swing.model
 */
public final class ButtonGroupRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new ButtonGroupRootProcessor();

  private ButtonGroupRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
    for (JavaInfo javaInfo : components) {
      if (javaInfo instanceof ButtonGroupInfo) {
        ButtonGroupInfo buttonGroupInfo = (ButtonGroupInfo) javaInfo;
        buttonGroupInfo.setAssociation(new EmptyAssociation());
        ButtonGroupContainerInfo.get(root).addChild(buttonGroupInfo);
      }
    }
  }
}
