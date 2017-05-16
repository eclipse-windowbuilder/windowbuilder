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
package org.eclipse.wb.core.model;

/**
 * Participator of {@link JavaInfo} initialization, used from {@link JavaInfo#initialize()}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IJavaInfoInitializationParticipator {
  /**
   * Participates in given {@link JavaInfo} initialization.
   */
  void process(JavaInfo javaInfo) throws Exception;
}
