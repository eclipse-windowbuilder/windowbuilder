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
package org.eclipse.wb.internal.core.model.util.generic;

import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;

/**
 * Helper to configure any {@link JavaInfo} to support configuration based features (from
 * <code>parameter</code> tags).
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class DescriptionDrivenFeaturesParticipator
    implements
      IJavaInfoInitializationParticipator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaInfoInitializationParticipator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo javaInfo) throws Exception {
    CopyPropertyTopSupport.install(javaInfo);
    CopyPropertyTopChildSupport.install(javaInfo);
    ModelMethodPropertySupport.install(javaInfo, "modelMethodProperty ");
    ModelMethodPropertyChildSupport.install(javaInfo, "modelMethodChildProperty ");
  }
}
