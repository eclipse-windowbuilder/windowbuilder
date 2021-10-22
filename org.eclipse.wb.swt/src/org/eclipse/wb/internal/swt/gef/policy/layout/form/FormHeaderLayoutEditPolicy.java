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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.header.AbstractHeaderLayoutEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.draw2d.geometry.Transposer;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.requests.SelectionRequest;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swt.gef.GefMessages;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutPreferences.PercentsInfo;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ICompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.apache.commons.lang.ArrayUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Policy for headers.
 *
 * @author mitin_aa
 */
final class FormHeaderLayoutEditPolicy<C extends IControlInfo>
    extends
      AbstractHeaderLayoutEditPolicy {
  private final boolean isHorizontal;
  private final IFormLayoutInfo<C> layout;
  private final LayoutEditPolicy mainPolicy;
  private final Transposer t;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  FormHeaderLayoutEditPolicy(IFormLayoutInfo<C> layout,
      LayoutEditPolicy mainPolicy,
      boolean isHorizontal) {
    super(mainPolicy);
    this.layout = layout;
    this.mainPolicy = mainPolicy;
    this.isHorizontal = isHorizontal;
    this.t = new Transposer(!isHorizontal);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    child.installEditPolicy(
        EditPolicy.SELECTION_ROLE,
        new FormHeaderSelectionEditPolicy(mainPolicy));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Double-click
  //
  ////////////////////////////////////////////////////////////////////////////
  private void handleDoubleClick(SelectionRequest request) {
    Point location = request.getLocation().getCopy();
    int percent = calcPercent(location);
    if (percent > 0) {
      layout.getPreferences().addPercent(percent, isHorizontal);
      getHost().refresh();
    }
  }

  @Override
  public void performRequest(Request request) {
    if (Request.REQ_OPEN.equals(request.getType())) {
      handleDoubleClick((SelectionRequest) request);
      return;
    }
    super.performRequest(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Moving
  //
  ////////////////////////////////////////////////////////////////////////////
  private TextFeedback hintFeedback;
  private Figure feedback;

  @Override
  protected void showLayoutTargetFeedback(Request request) {
    super.showLayoutTargetFeedback(request);
    int percent = calcPercent((ChangeBoundsRequest) request);
    if (percent > 0 && percent < 100) {
      {
        if (hintFeedback == null) {
          Layer layer =
              mainPolicy.getHost().getViewer().getLayer(IEditPartViewer.FEEDBACK_LAYER_ABV_1);
          hintFeedback = new TextFeedback(layer, true);
          hintFeedback.add();
        }
        hintFeedback.setText(percent + "%");
        hintFeedback.setLocation(new Point(0, 0));
      }
      {
        if (feedback == null) {
          Layer layer = getHost().getViewer().getLayer(IEditPartViewer.FEEDBACK_LAYER_ABV_1);
          feedback = new FormHeaderEditPart.PercentFigure(isHorizontal);
          layer.add(feedback);
        }
        int figureSize = t.t(getHostFigure().getSize()).height;
        Point location = t.t(((ChangeBoundsRequest) request).getLocation().getCopy());
        Point position = t.t(new Point(location.x - figureSize / 2, 0));
        feedback.setLocation(position);
        feedback.setSize(figureSize, figureSize);
      }
    } else {
      removeFeedbacks();
    }
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    removeFeedbacks();
    super.eraseLayoutTargetFeedback(request);
  }

  private void removeFeedbacks() {
    if (hintFeedback != null) {
      hintFeedback.remove();
      hintFeedback = null;
      FigureUtils.removeFigure(feedback);
      feedback = null;
    }
  }

  @Override
  public boolean understandsRequest(Request request) {
    return Request.REQ_MOVE.equals(request.getType());
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    final int percent = calcPercent(request);
    if (percent > 0 && percent < 100) {
      EditPart editPart = request.getEditParts().get(0);
      final int oldPercent = ((PercentsInfo) editPart.getModel()).value;
      return new Command() {
        @Override
        public void execute() throws Exception {
          layout.getPreferences().removePercent(oldPercent, isHorizontal);
          layout.getPreferences().addPercent(percent, isHorizontal);
          getHost().refresh();
        }
      };
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Coordinates/Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private int calcPercent(ChangeBoundsRequest request) {
    Point location = request.getLocation().getCopy();
    return calcPercent(location);
  }

  private int calcPercent(Point location) {
    translateToModel(location);
    int marginOffset =
        isHorizontal ? FormUtils.getLayoutMarginLeft(layout) : FormUtils.getLayoutMarginTop(layout);
    int place = t.t(location).x - marginOffset;
    int size = t.t(layout.getContainerSize()).width;
    return place * 100 / size;
  }

  private void translateToModel(Translatable t) {
    t.translate(getOffset(mainPolicy.getHost().getFigure(), layout.getComposite()).getNegated());
  }

  /**
   * @return the offset of {@link Figure} with headers relative to the absolute layer.
   */
  public static Point getOffset(Figure containerFigure, ICompositeInfo composite) {
    Point offset = new Point(0, 0);
    FigureUtils.translateFigureToAbsolute2(containerFigure, offset);
    offset.translate(composite.getClientAreaInsets());
    return offset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    IEditPartViewer viewer = getHost().getViewer();
    Tool tool = viewer.getEditDomain().getActiveTool();
    Point location = tool.getLocation().getCopy();
    final int percent = calcPercent(location);
    // add actions
    if (percent > 0) {
      IAction action =
          new Action(MessageFormat.format(
              GefMessages.FormHeaderLayoutEditPolicy_addSnapPoint,
              percent)) {
            @Override
            public void run() {
              layout.getPreferences().addPercent(percent, isHorizontal);
              getHost().refresh();
            }
          };
      manager.add(action);
      manager.add(new Separator());
    }
    // add 'remove' actions
    List<Integer> percents =
        isHorizontal
            ? layout.getPreferences().getHorizontalPercents()
            : layout.getPreferences().getVerticalPercents();
    for (final Integer integer : percents) {
      IAction action =
          new Action(MessageFormat.format(
              GefMessages.FormHeaderLayoutEditPolicy_removePercent,
              integer)) {
            @Override
            public void run() {
              layout.getPreferences().removePercent(integer, isHorizontal);
              getHost().refresh();
            }
          };
      manager.add(action);
    }
    manager.add(new Separator());
    {
      // restore defaults
      IAction action = new Action(GefMessages.FormHeaderLayoutEditPolicy_restoreDefaults) {
        @Override
        public void run() {
          layout.getPreferences().defaultPercents(isHorizontal);
          getHost().refresh();
        }
      };
      manager.add(action);
    }
    {
      // configure defaults
      IAction action = new Action(GefMessages.FormHeaderLayoutEditPolicy_useAsDefaults) {
        @Override
        public void run() {
          if (MessageDialog.openQuestion(
              DesignerPlugin.getShell(),
              GefMessages.FormHeaderLayoutEditPolicy_confirmDefaultsTitle,
              GefMessages.FormHeaderLayoutEditPolicy_confirmDefaultsMessage)) {
            layout.getPreferences().setAsDefaultPercents(isHorizontal);
          }
        }
      };
      manager.add(action);
    }
    {
      // configure defaults
      IAction action = new Action(GefMessages.FormHeaderLayoutEditPolicy_configureDefaults) {
        @Override
        public void run() {
          ToolkitDescription toolkit = GlobalState.getToolkit();
          String id = toolkit.getId() + ".preferences.layout.FormLayoutPreferencePage";
          PreferencesUtil.createPreferenceDialogOn(
              DesignerPlugin.getShell(),
              id,
              ArrayUtils.EMPTY_STRING_ARRAY,
              null).open();
          getHost().refresh();
        }
      };
      manager.add(action);
    }
  }
}
