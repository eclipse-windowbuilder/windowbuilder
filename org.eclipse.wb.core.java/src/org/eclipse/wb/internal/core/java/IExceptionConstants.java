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
package org.eclipse.wb.internal.core.java;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;

/**
 * Constants for XML {@link DesignerException}'s.
 *
 * @author scheglov_ke
 * @coverage XML
 */
public interface IExceptionConstants {
  int DESCRIPTION_NO_DESCRIPTIONS = 5000;
  int DESCRIPTION_LOADING = 5001;
  int DESCRIPTION_NO_TOOLKIT = 5002;
  int DESCRIPTION_EDITOR_STATIC_FIELD = 5003;
  int NOT_JAVA_PROJECT = 5004;
  int __FORCE_EXECUTION = Math.abs(0);
}
