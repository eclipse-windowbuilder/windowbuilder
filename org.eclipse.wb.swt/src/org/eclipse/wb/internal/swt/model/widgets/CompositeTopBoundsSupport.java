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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Implementation of {@link TopBoundsSupport} for SWT {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage swt.model.widgets
 */
public abstract class CompositeTopBoundsSupport extends TopBoundsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeTopBoundsSupport(CompositeInfo composite) {
    super(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    // if one these methods is present, we already set size during AST evaluation
    if (hasMethodInvocations(new String[]{
        "setSize(int,int)",
        "setSize(org.eclipse.swt.graphics.Point)",
        "pack()"})) {
      ContainerSupport.layout(m_component.getObject());
      return;
    }
    // set size from resource properties (or default)
    {
      Dimension size = getResourceSize();
      ControlSupport.setSize(m_component.getObject(), size.width, size.height);
    }
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    // remember size in resource properties
    setResourceSize(width, height);
    // check for: setSize(org.eclipse.swt.graphics.Point)
    if (setSizePoint("setSize", width, height)) {
      return;
    }
    // prepare source elements
    String widthSource = IntegerConverter.INSTANCE.toJavaSource(m_component, width);
    String heightSource = IntegerConverter.INSTANCE.toJavaSource(m_component, height);
    // check for: setSize(int,int)
    {
      MethodInvocation invocation = m_component.getMethodInvocation("setSize(int,int)");
      if (invocation != null) {
        AstEditor editor = m_component.getEditor();
        List<Expression> arguments = DomGenerics.arguments(invocation);
        editor.replaceExpression(arguments.get(0), widthSource);
        editor.replaceExpression(arguments.get(1), heightSource);
        return;
      }
    }
    // always set size for Shell
    if (m_component instanceof ShellInfo) {
      m_component.addMethodInvocation("setSize(int,int)", widthSource + ", " + heightSource);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Show
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean show() throws Exception {
    Object control = m_component.getObject();
    show(m_component, control);
    return true;
  }

  /**
   * Shows given control for testing/preview.
   */
  public static void show(AbstractComponentInfo component, Object control) throws Exception {
    showBefore();
    try {
      show0(control);
    } finally {
      showAfter();
    }
  }

  /**
   * Prepares environment for showing control.
   */
  private static void showBefore() throws Exception {
    Shell eclipseShell = DesignerPlugin.getShell();
    // disable redraw to prevent outstanding paints after preview window closed and disposed
    if (EnvironmentUtils.IS_MAC) {
      eclipseShell.redraw();
      eclipseShell.update();
      eclipseShell.setRedraw(false);
    }
    // disable Shell to prevent its activation before closing preview
    eclipseShell.setEnabled(false);
  }

  /**
   * Updates environment after showing control.
   */
  private static void showAfter() throws Exception {
    Shell eclipseShell = DesignerPlugin.getShell();
    if (EnvironmentUtils.IS_MAC) {
      eclipseShell.setRedraw(true);
    }
    eclipseShell.setEnabled(true);
    eclipseShell.forceActive();
  }

  /**
   * Shows given control for testing/preview, raw.
   */
  private static void show0(Object control) throws Exception {
    final Object shell = ControlSupport.getShell(control);
    // handle wrapper
    if (control != shell) {
      ContainerSupport.setShellText(shell, ModelMessages.CompositeTopBoundsSupport_wrapperShellText);
      ContainerSupport.setFillLayout(shell);
      org.eclipse.draw2d.geometry.Rectangle controlBounds = ControlSupport.getBounds(control);
      org.eclipse.draw2d.geometry.Rectangle shellBounds =
          ContainerSupport.computeTrim(shell, 0, 0, controlBounds.width, controlBounds.height);
      ControlSupport.setSize(shell, shellBounds.width, shellBounds.height);
      ContainerSupport.layout(shell);
    }
    // close preview by pressing ESC key
    Runnable clearESC = closeOnESC(shell);
    // set location
    {
      Rectangle monitorClientArea = DesignerPlugin.getShell().getMonitor().getClientArea();
      // center on primary Monitor
      int x;
      int y;
      {
        org.eclipse.draw2d.geometry.Rectangle shellBounds = ControlSupport.getBounds(shell);
        x = monitorClientArea.x + (monitorClientArea.width - shellBounds.width) / 2;
        y = monitorClientArea.y + (monitorClientArea.height - shellBounds.height) / 2;
      }
      // ensure that top-left corner is visible
      x = Math.max(x, monitorClientArea.x + 10);
      y = Math.max(y, monitorClientArea.y + 10);
      // do set location
      ControlSupport.setLocation(shell, x, y);
    }
    // show Shell in modal state
    ContainerSupport.showShell(shell);
    clearESC.run();
  }

  /**
   * Add the display filter which closes preview by pressing ESC key.
   */
  private static Runnable closeOnESC(final Object shell) {
    final Display display = DesignerPlugin.getStandardDisplay();
    final Listener listener = new Listener() {
      @Override
      public void handleEvent(Event event) {
        if (event.keyCode == SWT.ESC) {
          ContainerSupport.closeShell(shell);
          event.doit = false;
        }
      }
    };
    // add filter
    display.addFilter(SWT.KeyDown, listener);
    // continuation
    return new Runnable() {
      @Override
      public void run() {
        display.removeFilter(SWT.KeyDown, listener);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Changes {@link org.eclipse.swt.graphics.Point} argument of given method.
   *
   * @return <code>true</code> if method was found and change done.
   */
  protected final boolean setSizePoint(String methodName, int width, int height) throws Exception {
    MethodInvocation invocation =
        m_component.getMethodInvocation(methodName + "(org.eclipse.swt.graphics.Point)");
    if (invocation != null) {
      AstEditor editor = m_component.getEditor();
      Expression dimensionExpression = (Expression) invocation.arguments().get(0);
      editor.replaceExpression(dimensionExpression, "new org.eclipse.swt.graphics.Point("
          + width
          + ", "
          + height
          + ")");
      return true;
    }
    // not found
    return false;
  }
}