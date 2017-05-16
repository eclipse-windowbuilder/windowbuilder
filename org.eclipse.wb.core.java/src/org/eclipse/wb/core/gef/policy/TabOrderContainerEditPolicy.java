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
package org.eclipse.wb.core.gef.policy;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.tools.TabOrderContainerRequest;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.order.TabOrderInfo;

import java.util.List;

/**
 * Layout Edit Policy allowing reordering container children.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.gef.policy
 */
public final class TabOrderContainerEditPolicy extends GraphicalEditPolicy {
  public static final Request TAB_ORDER_REQUEST = new Request("tab order");
  public static final String TAB_CONTAINER_ROLE = "tab container role";
  public static final String REQ_CONTAINER_TAB_ORDER = "container tab order";
  private List<TextFeedback> m_indexFeedbacks;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean understandsRequest(Request request) {
    return request.getType() == REQ_CONTAINER_TAB_ORDER;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
  public void showTargetFeedback(Request request) {
    TabOrderContainerRequest containerRequest = (TabOrderContainerRequest) request;
    //
    if (containerRequest.getChildren() == null) {
      try {
        TabOrderInfo tabOrderInfo = getTabOrderValue();
        containerRequest.setPossibleChildren(tabOrderInfo.getInfos());
        containerRequest.setChildren(tabOrderInfo.getOrderedInfos());
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        return;
      }
    }
    //
    showChildrenIndexes(containerRequest.getChildren(), containerRequest.getSelectedChild());
  }

  private void showChildrenIndexes(List<?> children, Object selectedChild) {
    eraseChildrenIndexes();
    m_indexFeedbacks = Lists.newArrayList();
    Layer layer = getLayer(IEditPartViewer.CLICKABLE_LAYER);
    IEditPartViewer viewer = getHost().getViewer();
    for (int index = 0; index < children.size(); index++) {
      Object child = children.get(index);
      GraphicalEditPart part = (GraphicalEditPart) viewer.getEditPartByModel(child);
      // prepare bounds for child in feedback layer
      Figure figure = part.getFigure();
      Point location = figure.getBounds().getLocation();
      FigureUtils.translateFigureToFigure(figure, layer, location);
      // add feedback with index
      TextFeedback feedback = new TextFeedback(layer);
      feedback.setText(Integer.toString(index));
      feedback.add();
      feedback.moveTopLeftCenter(location);
      m_indexFeedbacks.add(feedback);
      // register feedback with edit part, so we can click on number feedback as
      // well as on EditPart's main figure
      feedback.setData(part);
      // set background
      feedback.setBackground(IColorConstants.yellow);
      if (child == selectedChild) {
        feedback.setBackground(IColorConstants.lightGreen);
      }
    }
  }

  @Override
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
  public void eraseTargetFeedback(Request request) {
    eraseChildrenIndexes();
    TabOrderContainerRequest containerRequest = (TabOrderContainerRequest) request;
    TabOrderCommand command = new TabOrderCommand(getContainer(), containerRequest.getChildren());
    containerRequest.setCommand(command);
  }

  private void eraseChildrenIndexes() {
    if (m_indexFeedbacks != null) {
      for (TextFeedback feedback : m_indexFeedbacks) {
        feedback.remove();
      }
      m_indexFeedbacks = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private TabOrderInfo getTabOrderValue() throws Exception {
    JavaInfo container = getContainer();
    Property property = container.getPropertyByTitle("tab order");
    return (TabOrderInfo) property.getValue();
  }

  private JavaInfo getContainer() {
    return (JavaInfo) getHostModel();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class TabOrderCommand extends EditCommand {
    private final JavaInfo m_container;
    private final List<AbstractComponentInfo> m_orderedInfos;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public TabOrderCommand(JavaInfo container, List<AbstractComponentInfo> orderedInfos) {
      super(container);
      m_container = container;
      m_orderedInfos = orderedInfos;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // EditCommand
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void executeEdit() throws Exception {
      // prepare tab value
      TabOrderInfo tabOrderInfo = new TabOrderInfo();
      tabOrderInfo.getOrderedInfos().addAll(m_orderedInfos);
      // prepare tab property
      Property property = m_container.getPropertyByTitle("tab order");
      // set new value
      property.setValue(tabOrderInfo);
    }
  }
}