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
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.core.controls.BrowserComposite;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.errors.report.ErrorReport;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link Composite} for displaying {@link Exception} on design pane.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.editor.errors
 */
public abstract class ExceptionComposite extends Composite {
  private Button m_switchButton;
  private BrowserComposite m_browserComposite;
  private Image m_screenshotImage;
  private int m_sourcePosition;
  private String m_exceptionTitle;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExceptionComposite(Composite parent, int style) {
    super(parent, style);
    // create GUI elements
    GridLayoutFactory.create(this);
    {
      Composite titleComposite = new Composite(this, SWT.NONE);
      GridDataFactory.create(titleComposite).alignHL();
      GridLayoutFactory.create(titleComposite).columns(2).margins(10);
      {
        Label label = new Label(titleComposite, SWT.NONE);
        label.setImage(SwtResourceManager.getImage(SWT.ICON_ERROR));
      }
      {
        Label label = new Label(titleComposite, SWT.NONE);
        label.setText(BrandingUtils.getBranding().getProductName()
            + " was not able to show the GUI.");
        GridDataFactory.create(label).alignHL();
      }
    }
    {
      m_browserComposite = new BrowserComposite(this, SWT.NONE);
      GridDataFactory.create(m_browserComposite).grab().alignVF().alignHF();
    }
    {
      Composite buttonsComposite = new Composite(this, SWT.NONE);
      GridDataFactory.create(buttonsComposite).alignHR();
      GridLayoutFactory.create(buttonsComposite).columns(3).equalColumns().marginsH(0);
      {
        Button contactSupportButton = new Button(buttonsComposite, SWT.NONE);
        GridDataFactory.create(contactSupportButton).fillH();
        contactSupportButton.setText("Contact Support...");
        contactSupportButton.setImage(EnvironmentUtils.IS_MAC
            ? null
            : DesignerPlugin.getImage("actions/errors/support32.png"));
        contactSupportButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            ContactSupportDialog dialog =
                new ContactSupportDialog(getShell(),
                    m_exceptionTitle,
                    m_screenshotImage,
                    getErrorReport());
            dialog.open();
          }
        });
      }
      {
        Button refreshButton = new Button(buttonsComposite, SWT.NONE);
        GridDataFactory.create(refreshButton).fillH();
        refreshButton.setText("Reparse");
        refreshButton.setImage(EnvironmentUtils.IS_MAC
            ? null
            : DesignerPlugin.getImage("actions/errors/refresh32.png"));
        refreshButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            doRefresh();
          }
        });
      }
      {
        m_switchButton = new Button(buttonsComposite, SWT.NONE);
        GridDataFactory.create(m_switchButton).fillH();
        m_switchButton.setText("Switch to code");
        m_switchButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent ev) {
            doShowSource(m_sourcePosition);
          }
        });
      }
    }
    // dispose
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (m_screenshotImage != null) {
          m_screenshotImage.dispose();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  private void updateForSourcePosition(Throwable e) {
    m_sourcePosition = DesignerExceptionUtils.getSourcePosition(e);
    boolean hasSourcePosition = m_sourcePosition != -1;
    // text
    if (hasSourcePosition) {
      m_switchButton.setText("Go to problem");
    } else {
      m_switchButton.setText("Switch to code");
    }
    // image
    if (!EnvironmentUtils.IS_MAC) {
      Image image;
      if (hasSourcePosition) {
        image = DesignerPlugin.getImage("actions/errors/switch32locate.png");
      } else {
        image = DesignerPlugin.getImage("actions/errors/switch32.png");
      }
      m_switchButton.setImage(image);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return new ErrorReport to be used by {@link ContactSupportDialog}.
   */
  protected abstract ErrorReport getErrorReport();

  /**
   * Handles the 'switch to source' action.
   * 
   * @param sourcePosition
   *          the position in the source code to switch to.
   */
  protected abstract void doShowSource(int sourcePosition);

  /**
   * Handles the 'refresh' action.
   */
  protected abstract void doRefresh();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final Image getScreenshotImage() {
    return m_screenshotImage;
  }

  /**
   * Sets the {@link Throwable} to display with additional information which may be included into
   * problem report.
   * 
   * @param e
   *          the {@link Throwable} to display.
   * @param screenshot
   *          the {@link Image} of entire shell just before error. Can be <code>null</code> in case
   *          of parse error when no screenshot need.
   */
  protected final void setException0(Throwable e, Image screenshot) {
    m_screenshotImage = screenshot;
    m_browserComposite.setText(DesignerExceptionUtils.getExceptionHTML(e));
    m_exceptionTitle = "";
    if (e instanceof DesignerException) {
      DesignerException designerException = (DesignerException) e;
      m_exceptionTitle = DesignerExceptionUtils.getExceptionTitle(designerException.getCode());
    }
    updateForSourcePosition(e);
  }
}
