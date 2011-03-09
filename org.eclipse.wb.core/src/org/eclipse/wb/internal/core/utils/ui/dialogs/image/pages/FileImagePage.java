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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of {@link AbstractImagePage} that selects image from file system.
 * 
 * @author scheglov_ke
 */
public final class FileImagePage extends AbstractImagePage {
  public static final String ID = "FILE";
  private final Map/*<String, ImageInfo>*/m_pathToImageInfo = new TreeMap();
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
        for (Iterator I = m_pathToImageInfo.values().iterator(); I.hasNext();) {
          ImageInfo imageInfo = (ImageInfo) I.next();
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
      button.setText("&Browse...");
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
    ImageInfo imageInfo = (ImageInfo) m_pathToImageInfo.get(path);
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
    return "Absolute path in file system (use only for quick testing, never use in real application!)";
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
