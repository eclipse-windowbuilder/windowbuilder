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
package org.eclipse.wb.internal.rcp;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;

/**
 * Constants for RCP {@link DesignerException}'s.
 *
 * @author scheglov_ke
 * @coverage rcp
 */
public interface IExceptionConstants {
  int NO_FORM_TOOLKIT = 3000;
  int NO_DESIGN_WIZARD = 3001;
  int NO_DESIGN_MP_EDITOR = 3002;
  int NO_CONTROL_IN_WIZARD_PAGE = 3003;
  int SWT_DIALOG_NO_OPEN_METHOD = 3004;
  int NOT_CONFIGURED_FOR_SWT = 3005;
  int SWT_DIALOG_NO_MAIN_SHELL = 3006;
  int NO_DESIGN_WIDGET = 3007;
}
