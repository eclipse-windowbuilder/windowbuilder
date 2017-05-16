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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;

/**
 * Notification that new {@link Association} will be set for {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface JavaInfoSetAssociationBefore {
  void invoke(JavaInfo javaInfo, Association association) throws Exception;
}