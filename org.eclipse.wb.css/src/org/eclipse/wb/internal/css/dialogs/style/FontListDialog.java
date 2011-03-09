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
package org.eclipse.wb.internal.css.dialogs.style;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.css.Activator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Dialog for choosing font list.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public class FontListDialog extends Dialog {
  private String[] m_fonts;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FontListDialog(Shell parentShell) {
    super(parentShell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setFontsString(String fonts) {
    m_fonts = StringUtils.split(fonts, ",");
    if (m_fonts != null) {
      for (int i = 0; i < m_fonts.length; i++) {
        String font = m_fonts[i];
        m_fonts[i] = font.trim();
      }
    }
  }

  public String getFontsString() {
    return StringUtils.join(m_fonts, ", ");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private org.eclipse.swt.widgets.List m_selectedList;
  private Text m_customText;

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.create(area).columns(2);
    // selected list
    {
      Group selectedComposite = new Group(area, SWT.NONE);
      GridDataFactory.create(selectedComposite).spanV(3).fill();
      GridLayoutFactory.create(selectedComposite).noMargins();
      selectedComposite.setText("Selected fonts");
      {
        m_selectedList = new org.eclipse.swt.widgets.List(selectedComposite, SWT.BORDER);
        GridDataFactory.create(m_selectedList).grab().fill().hintC(50, 15);
        if (m_fonts != null) {
          m_selectedList.setItems(m_fonts);
          m_selectedList.select(0);
        }
      }
      {
        ToolBar toolBar = new ToolBar(selectedComposite, SWT.FLAT | SWT.RIGHT);
        {
          ToolItem upItem = new ToolItem(toolBar, SWT.NONE);
          upItem.setImage(Activator.getImage("arrow_up.gif"));
          upItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              int index = m_selectedList.getSelectionIndex();
              if (index > 0) {
                String item = m_selectedList.getItem(index);
                m_selectedList.remove(index);
                m_selectedList.add(item, index - 1);
                m_selectedList.select(index - 1);
              }
            }
          });
        }
        {
          ToolItem downItem = new ToolItem(toolBar, SWT.NONE);
          downItem.setImage(Activator.getImage("arrow_down.gif"));
          downItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              int index = m_selectedList.getSelectionIndex();
              if (index != -1 && index < m_selectedList.getItemCount() - 1) {
                String item = m_selectedList.getItem(index);
                m_selectedList.remove(index);
                m_selectedList.add(item, index + 1);
                m_selectedList.select(index + 1);
              }
            }
          });
        }
        {
          ToolItem removeItem = new ToolItem(toolBar, SWT.NONE);
          removeItem.setImage(Activator.getImage("remove.gif"));
          removeItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              int index = m_selectedList.getSelectionIndex();
              if (index != -1) {
                m_selectedList.remove(index);
                if (index < m_selectedList.getItemCount()) {
                  m_selectedList.select(index);
                } else if (index > 0) {
                  m_selectedList.select(index - 1);
                }
              }
            }
          });
        }
      }
    }
    // generic fonts
    {
      createFontListGroup(area, "Generic fonts", 6, new String[]{
          "serif",
          "sans-serif",
          "cursive",
          "fantasy",
          "monospace"});
    }
    // installed fonts
    {
      String[] fonts;
      {
        List<String> fontList = Lists.newArrayList();
        appendFonts(fontList, false);
        appendFonts(fontList, true);
        Collections.sort(fontList);
        fonts = fontList.toArray(new String[fontList.size()]);
      }
      createFontListGroup(area, "Installed fonts", 10, fonts);
    }
    // custom font
    {
      Group customComposite = new Group(area, SWT.NONE);
      GridDataFactory.create(customComposite).fillH();
      GridLayoutFactory.create(customComposite).columns(2);
      customComposite.setText("Custom font");
      {
        ToolBar toolBar = new ToolBar(customComposite, SWT.FLAT | SWT.RIGHT);
        GridDataFactory.create(toolBar).grabV().alignVM();
        //
        ToolItem addItem = new ToolItem(toolBar, SWT.NONE);
        addItem.setImage(Activator.getImage("arrow_left.gif"));
        addItem.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            m_selectedList.add(m_customText.getText());
          }
        });
      }
      {
        m_customText = new Text(customComposite, SWT.BORDER);
        GridDataFactory.create(m_customText).grab().fill();
      }
    }
    //
    return area;
  }

  private static void appendFonts(List<String> fonts, boolean scalable) {
    FontData[] datas = Display.getDefault().getFontList(null, scalable);
    for (int i = 0; i < datas.length; i++) {
      FontData data = datas[i];
      String name = data.getName();
      if (!fonts.contains(name)) {
        fonts.add(name);
      }
    }
  }

  private void createFontListGroup(Composite area, String title, int hintVC, String[] fonts) {
    Group composite = new Group(area, SWT.NONE);
    GridDataFactory.create(composite).grab().hintHC(40).fill();
    GridLayoutFactory.create(composite).columns(2);
    composite.setText(title);
    // add item
    ToolItem addItem;
    {
      ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
      GridDataFactory.create(toolBar).grabV().alignVM();
      //
      addItem = new ToolItem(toolBar, SWT.NONE);
      addItem.setImage(Activator.getImage("arrow_left.gif"));
    }
    // list
    final org.eclipse.swt.widgets.List fontList;
    {
      fontList =
          new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      GridDataFactory.create(fontList).grab().hintVC(hintVC).fill();
      fontList.setItems(fonts);
    }
    // add listeners
    addItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] items = fontList.getSelection();
        if (items.length == 1) {
          m_selectedList.add(items[0]);
        }
      }
    });
    fontList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDoubleClick(MouseEvent e) {
        String[] items = fontList.getSelection();
        if (items.length == 1) {
          m_selectedList.add(items[0]);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Edit font list");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void okPressed() {
    m_fonts = m_selectedList.getItems();
    super.okPressed();
  }
}
