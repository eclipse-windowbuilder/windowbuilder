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
package org.eclipse.wb.internal.swt.gef.part.delegate;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.internal.swt.gef.part.ControlEditPart;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Top level {@link ControlEditPart}, i.e. for root {@link ControlInfo}, delegates some of their
 * implementation to {@link IControlEditPartDelegate}. We use it in "eRCP" to show device image
 * around the {@link ControlInfo} image.
 *
 * @author scheglov_ke
 * @coverage swt.gef.part
 */
public interface IControlEditPartDelegate {
  Figure createFigure();

  void refreshVisuals();

  void addNotify();

  void removeNotify();

  void refreshEditPolicies();
}
