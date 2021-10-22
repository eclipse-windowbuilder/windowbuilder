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
package org.eclipse.wb.tests.designer.swt.model.menu;

import com.google.common.base.Predicate;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.gef.policy.menu.MenuSelectionEditPolicy;
import org.eclipse.wb.tests.gef.GraphicalRobot;

/**
 * Tester for feedbacks on {@link IEditPartViewer#MENU_FEEDBACK_LAYER}.
 *
 * @author scheglov_ke
 */
public final class MenuFeedbackTester {
  private final GraphicalRobot canvas;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuFeedbackTester(GraphicalRobot canvas) {
    this.canvas = canvas;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Predicate} that checks if feedback is {@link MenuSelectionEditPolicy}.
   */
  private static Predicate<Figure> getSelectionPredicate(GraphicalEditPart part) {
    // prepare "part" Figure bounds in absolute
    final Rectangle partBounds;
    {
      partBounds = part.getFigure().getBounds().getCopy();
      FigureUtils.translateFigureToAbsolute(part.getFigure(), partBounds);
    }
    // return predicate
    return new Predicate<Figure>() {
      public boolean apply(Figure feedback) {
        return partBounds.equals(feedback.getBounds());
      }
    };
  }

  private Predicate<Figure> getSelectionPredicate(Object object) {
    return getSelectionPredicate(canvas.getEditPart(object));
  }

  /**
   * Asserts that there are no any "menu" feedback.
   */
  public void assertMenuNoFeedbacks() {
    canvas.assertFigures(IEditPartViewer.MENU_FEEDBACK_LAYER);
  }

  @SuppressWarnings("unchecked")
  public void assertMenuFeedbacks(Predicate<Figure> p) {
    assertMenuFeedbacks(new Predicate[]{p});
  }

  @SuppressWarnings("unchecked")
  public void assertMenuFeedbacks(Predicate<Figure> p1, Predicate<Figure> p2) {
    assertMenuFeedbacks(new Predicate[]{p1, p2});
  }

  /**
   * Asserts that {@link IEditPartViewer#MENU_FEEDBACK_LAYER} has feedback {@link Figure}'s that
   * satisfy to given {@link Predicate}'s.
   */
  public void assertMenuFeedbacks(Predicate<Figure>... predicates) {
    canvas.assertFigures(IEditPartViewer.MENU_FEEDBACK_LAYER, predicates);
  }

  public void assertFeedback_selection(Object object) {
    assertMenuFeedbacks(getSelectionPredicate(object));
  }

  public void assertFeedback_selection_line(Object selection, Object lineObject, int location) {
    Predicate<Figure> linePredicate = canvas.getLinePredicate(lineObject, location);
    Predicate<Figure> selectionPredicate = getSelectionPredicate(selection);
    assertMenuFeedbacks(selectionPredicate, linePredicate);
  }

  public void assertFeedback_selection_target(Object selection, Object targetObject) {
    Predicate<Figure> targetPredicate = canvas.getTargetPredicate(targetObject);
    Predicate<Figure> selectionPredicate = getSelectionPredicate(selection);
    assertMenuFeedbacks(selectionPredicate, targetPredicate);
  }

  public void assertFeedback_selection_emptyFlow(Object selection,
      Object hostObject,
      boolean horizontal) {
    Predicate<Figure> targetPredicate =
        canvas.getEmptyFlowContainerPredicate(hostObject, horizontal);
    Predicate<Figure> flowPredicate = getSelectionPredicate(selection);
    assertMenuFeedbacks(flowPredicate, targetPredicate);
  }

  /**
   * Asserts that there is only one feedback, that satisfies
   * {@link #getLinePredicate(GraphicalEditPart, int)}.
   */
  public void assertMenuLineFeedback(Object object, int location) {
    assertMenuFeedbacks(canvas.getLinePredicate(object, location));
  }

  /**
   * Asserts that there is only one feedback, that satisfies
   * {@link #getTargetPredicate(GraphicalEditPart)}.
   */
  public void assertMenuTargetFeedback(Object object) {
    assertMenuFeedbacks(canvas.getTargetPredicate(object));
  }
}
