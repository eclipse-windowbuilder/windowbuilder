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
package org.eclipse.wb.internal.layout.group.gef;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Translatable;

/**
 * Helper for drawing feedbacks by {@link FeedbacksDrawer}.
 * 
 * @author mitin_aa
 */
public interface IFeedbacksHelper {
  /**
   * Translates the {@link Translatable} from model to feedback coordinate systems.
   */
  void translateModelToFeedback(Translatable t);

  /**
   * Adds a figure to appropriate layer.
   */
  void addFeedback2(Figure figure);
}
