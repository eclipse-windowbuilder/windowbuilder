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
import org.eclipse.wb.core.branding.IBrandingDescription;
import org.eclipse.wb.core.controls.BrowserComposite;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.errors.report2.CreateReportDialog;
import org.eclipse.wb.internal.core.editor.errors.report2.ZipFileErrorReport;
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
import org.eclipse.swt.widgets.Link;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

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
      GridDataFactory.create(titleComposite).alignHL().grabH();
      GridLayoutFactory.create(titleComposite).columns(2).margins(10);
      {
        Label label = new Label(titleComposite, SWT.NONE);
        GridDataFactory.create(label).alignVM();
        label.setImage(SwtResourceManager.getImage(SWT.ICON_ERROR));
      }
      {
        Link label = new Link(titleComposite, SWT.WRAP | SWT.NO_FOCUS);
        GridDataFactory.create(label).alignHL().grabH().alignVM();
        label.setText(MessageFormat.format(
            Messages.ExceptionComposite_message,
            BrandingUtils.getBranding().getProductName()));
        label.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent event) {
            IBrandingDescription branding = BrandingUtils.getBranding();
            String url = null;
            if ("bug tracking system".equals(event.text)) {
              url = branding.getSupportInfo().getBugtrackingUrl();
            } else if ("discussion group".equals(event.text)) {
              url = branding.getSupportInfo().getForumUrl();
            }
            if (!StringUtils.isEmpty(url)) {
              DesignerExceptionUtils.openBrowser(url);
            }
          }
        });
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
        contactSupportButton.setText(Messages.ExceptionComposite_reportButton);
        contactSupportButton.setImage(EnvironmentUtils.IS_MAC
            ? null
            : DesignerPlugin.getImage("actions/errors/support32.png"));
        contactSupportButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            CreateReportDialog dialog =
                new CreateReportDialog(getShell(), m_screenshotImage, getZipFileErrorReport());
            dialog.open();
          }
        });
      }
      {
        Button refreshButton = new Button(buttonsComposite, SWT.NONE);
        GridDataFactory.create(refreshButton).fillH();
        refreshButton.setText(Messages.ExceptionComposite_reparseButton);
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
        m_switchButton.setText(Messages.ExceptionComposite_switchButton);
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
      m_switchButton.setText(Messages.ExceptionComposite_goProblemButton);
    } else {
      m_switchButton.setText(Messages.ExceptionComposite_switchButton);
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
   * @return new ZipFileErrorReport to be used by {@link CreateReportDialog}.
   */
  protected abstract ZipFileErrorReport getZipFileErrorReport();

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
    updateForSourcePosition(e);
  }
}