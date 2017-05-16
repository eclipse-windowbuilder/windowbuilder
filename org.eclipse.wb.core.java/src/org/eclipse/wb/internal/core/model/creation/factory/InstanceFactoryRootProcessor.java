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
package org.eclipse.wb.internal.core.model.creation.factory;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;

import java.util.List;

/**
 * Support for binding {@link InstanceFactoryInfo}'s to hierarchy.
 *
 * @author sablin_aa
 * @coverage core.model.creation
 */
public final class InstanceFactoryRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new InstanceFactoryRootProcessor();

  private InstanceFactoryRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
    for (JavaInfo javaInfo : components) {
      if (javaInfo instanceof InstanceFactoryInfo) {
        InstanceFactoryInfo instanceFactoryInfo = (InstanceFactoryInfo) javaInfo;
        InstanceFactoryContainerInfo containerInfo = InstanceFactoryContainerInfo.get(root);
        containerInfo.addChild(instanceFactoryInfo);
        instanceFactoryInfo.setAssociation(new EmptyAssociation());
      }
    }
  }
}
