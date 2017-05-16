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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

/**
 * Implementation of {@link AbstractImagePage} that selects image from file system.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class FileImagePage extends AbstractImagePage {
  public static final String ID = "FILE";
  private final Map<String, ImageInfo> m_pathToImageInfo = Maps.newTreeMap();
  private final Text m_pathText;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FileImagePage(Composite parent, int style, AbstractImageDialog imageDialog) {
    super(parent, style, imageDialog);
    GridLayoutFactory.create(this).columns(2);
    // add dispose listener
    addListener(SWT.Dispose, new Listener() {
      public void handleEvent(Event event) {
        for (ImageInfo imageInfo : m_pathToImageInfo.values()) {
          imageInfo.getImage().dispose();
        }
      }
    });
    // path
    {
      m_pathText = new Text(this, SWT.BORDER);
      GridDataFactory.create(m_pathText).grabH().hintHC(50).fillH();
      // add listener
      m_pathText.addListener(SWT.Modify, new Listener() {
        public void handleEvent(Event event) {
          updateImageInfo();
        }
      });
    }
    // "Browse" button
    {
      Button button = new Button(this, SWT.NONE);
      GridDataFactory.create(button).hintHU(IDialogConstants.BUTTON_WIDTH).fill();
      button.setText(Messages.FileImagePage_browseButton);
      button.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
          fileDialog.setFilterPath(m_pathText.getText());
          String newPath = fileDialog.open();
          if (newPath != null) {
            m_pathText.setText(newPath);
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates {@link ImageInfo} according selected path.
   */
  private void updateImageInfo() {
    String path = m_pathText.getText();
    ImageInfo imageInfo = m_pathToImageInfo.get(path);
    if (imageInfo == null) {
      File file = new File(path);
      if (file.exists() && !file.isDirectory()) {
        // prepare image
        Image image;
        try {
          FileInputStream is = new FileInputStream(file);
          try {
            image = new Image(getDisplay(), is);
          } finally {
            IOUtils.closeQuietly(is);
          }
        } catch (Throwable e) {
          m_imageDialog.setResultImageInfo(null);
          return;
        }
        // prepare ImageInfo
        imageInfo = new ImageInfo(ID, path, image, file.length());
        m_pathToImageInfo.put(path, imageInfo);
      }
    }
    // set image to dialog
    m_imageDialog.setResultImageInfo(imageInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getTitle() {
    return Messages.FileImagePage_title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractImagePage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    updateImageInfo();
  }

  @Override
  public void setInput(Object data) {
    String path = (String) data;
    m_pathText.setText(path);
  }
}
