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
package org.netbeans.modules.form.layoutdesign;

/**
 * Interface between netbeans and GEF for drawing feedbacks. Some methods not used, but needed to
 * compile netbeans code.
 * 
 * @author mitin_aa
 */
public interface IFeedbacksDrawer {
  public static final int BOTH_DIMENSIONS = 2;

  void drawLine(int x1, int y1, int x2, int y2);

  void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);

  void drawLinkBadge(int x, int y, int dimension);
}
