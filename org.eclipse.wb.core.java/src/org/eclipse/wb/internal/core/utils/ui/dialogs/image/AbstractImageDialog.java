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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.util.Map;

/**
 * Abstract dialog for image choosing.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractImageDialog extends ResizableDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected AbstractImageDialog(Shell parentShell, AbstractUIPlugin plugin) {
    super(parentShell, plugin);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private ImageInfo m_imageInfo;
  private String m_input_pageId;
  private Object m_input_pageData;

  /**
   * Sets the initial page and data for it.
   */
  public final void setInput(String pageId, Object pageData) {
    m_input_pageId = pageId;
    m_input_pageData = pageData;
    if (m_input_pageId != null) {
      AbstractImagePage page = m_idToPage.get(m_input_pageId);
      if (page != null) {
        page.init(m_input_pageData);
      }
    }
  }

  /**
   * @return the resulting {@link ImageInfo}.
   */
  public final ImageInfo getImageInfo() {
    return m_imageInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Group m_pageButtonsGroup;
  private Group m_pagesGroup;
  private ImagePreviewComposite m_previewComposite;

  @Override
  protected final Control createDialogArea(Composite parent) {
    Composite area = new Composite(parent, SWT.NONE);
    GridDataFactory.create(area).grab().fill();
    GridLayoutFactory.create(area).columns(2).equalColumns();
    // create Group for Button's with page titles
    {
      m_pageButtonsGroup = new Group(area, SWT.NONE);
      GridDataFactory.create(m_pageButtonsGroup).spanH(2).grabH().fill();
      GridLayoutFactory.create(m_pageButtonsGroup);
      m_pageButtonsGroup.setText(Messages.AbstractImageDialog_modeLabel);
    }
    // create Group for page content
    {
      m_pagesGroup = new Group(area, SWT.NONE);
      GridDataFactory.create(m_pagesGroup).grab().fill();
      m_pagesGroup.setLayout(new StackLayout());
      m_pagesGroup.setText(Messages.AbstractImageDialog_parametersGroup);
    }
    // create Group for preview
    {
      Group previewGroup = new Group(area, SWT.NONE);
      GridDataFactory.create(previewGroup).hint(300, 350).fill();
      previewGroup.setLayout(new FillLayout());
      previewGroup.setText(Messages.AbstractImageDialog_previewGroup);
      //
      m_previewComposite = new ImagePreviewComposite(previewGroup, SWT.NONE);
    }
    // add pages
    addPages(m_pagesGroup);
    return area;
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    // try to set initial page
    if (m_input_pageId != null) {
      AbstractImagePage page = m_idToPage.get(m_input_pageId);
      if (page != null) {
        // set selected button
        Button button = m_idToButton.get(m_input_pageId);
        button.setSelection(true);
        // activate page
        setActivePage(page);
        page.setInput(m_input_pageData);
      }
    }
    //
    return contents;
  }

  @Override
  protected final void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.AbstractImageDialog_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, AbstractImagePage> m_idToPage = Maps.newTreeMap();
  private final Map<String, Button> m_idToButton = Maps.newTreeMap();

  protected final void addPage(AbstractImagePage page) {
    String id = page.getId();
    String title = page.getTitle();
    //
    m_idToPage.put(id, page);
    // add radio button
    final Button button = new Button(m_pageButtonsGroup, SWT.RADIO);
    m_idToButton.put(id, button);
    button.setText(title);
    button.setData(page);
    button.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        if (button.getSelection()) {
          AbstractImagePage newPage = (AbstractImagePage) button.getData();
          setActivePage(newPage);
        }
      }
    });
  }

  /**
   * Activates given {@link AbstractImagePage}.
   */
  private void setActivePage(AbstractImagePage page) {
    // notify page
    page.activate();
    // show page
    StackLayout stackLayout = (StackLayout) m_pagesGroup.getLayout();
    stackLayout.topControl = page.getPageControl();
    m_pagesGroup.layout();
  }

  /**
   * Adds {@link AbstractImagePage}'s.
   */
  protected abstract void addPages(Composite parent);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets that {@link ImageInfo} result.
   */
  public final void setResultImageInfo(ImageInfo imageInfo) {
    m_imageInfo = imageInfo;
    if (m_previewComposite != null) {
      m_previewComposite.setImageInfo(m_imageInfo);
    }
    // update Ok button
    getButton(IDialogConstants.OK_ID).setEnabled(m_imageInfo != null);
  }

  /**
   * Closes dialog with "OK" result.
   */
  public final void closeOk() {
    if (m_imageInfo != null) {
      setReturnCode(IDialogConstants.OK_ID);
      close();
    }
  }
}
