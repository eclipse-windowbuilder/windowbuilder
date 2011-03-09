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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ReusableDialog;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.semantics.AbstractValue;
import org.eclipse.wb.internal.css.semantics.BackgroundProperty;
import org.eclipse.wb.internal.css.semantics.BorderProperty;
import org.eclipse.wb.internal.css.semantics.FontProperty;
import org.eclipse.wb.internal.css.semantics.IValueListener;
import org.eclipse.wb.internal.css.semantics.OtherProperty;
import org.eclipse.wb.internal.css.semantics.Semantics;
import org.eclipse.wb.internal.css.semantics.TextProperty;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.Map;

/**
 * Dialog for editing {@link CssRuleNode}.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public class StyleEditDialog extends ReusableDialog {
  private final Semantics m_semantics = new Semantics();
  private final StyleEditOptions m_options = new StyleEditOptions();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<Shell, StyleEditDialog> m_parentShellToDialog = Maps.newHashMap();

  /**
   * @return new or already existing {@link StyleEditDialog} for given parent {@link Shell}.
   */
  public static StyleEditDialog get(Shell parentShell) {
    StyleEditDialog dialog = m_parentShellToDialog.get(parentShell);
    if (dialog == null) {
      dialog = new StyleEditDialog(parentShell);
      dialog.create();
      m_parentShellToDialog.put(parentShell, dialog);
    }
    return dialog;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private StyleEditDialog(Shell parentShell) {
    super(parentShell);
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Options
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleEditOptions getOptions() {
    return m_options;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Displays given {@link CssRuleNode} in this dialog.
   */
  public void setRule(CssRuleNode rule) {
    m_semantics.parse(rule);
    getShell().setText("CSS Rule Editor: " + rule.getSelector().getValue());
  }

  /**
   * Updates given {@link CssRuleNode} with state of this dialog.
   */
  public void updateRule(CssRuleNode rule) {
    m_semantics.update(rule);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    //
    GridLayoutFactory.create(area).columns(2);
    // create folder
    {
      Control control = createFolder(area);
      GridDataFactory.create(control).grab().fill();
    }
    // create browser for preview
    {
      Control control = createBrowser(area);
      GridDataFactory.create(control).hintC(50, 20).fill();
    }
    //
    return area;
  }

  /**
   * Creates buttons for this {@link Window} to make it behave like {@link Dialog}.
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    GridDataFactory.modify(parent).grabH().fillH();
    GridLayoutFactory.create(parent).columns(3);
    {
      Button clearButton = new Button(parent, SWT.NONE);
      GridDataFactory.create(clearButton).grabH().hintHC(20);
      clearButton.setText("Clear");
      clearButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_semantics.clear();
        }
      });
    }
    {
      Button okButton = new Button(parent, SWT.NONE);
      GridDataFactory.create(okButton).hintHC(20);
      okButton.setText(IDialogConstants.OK_LABEL);
      okButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          setReturnCode(OK);
          close();
        }
      });
    }
    {
      Button cancelButton = new Button(parent, SWT.NONE);
      GridDataFactory.create(cancelButton).hintHC(20);
      cancelButton.setText(IDialogConstants.CANCEL_LABEL);
      cancelButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          setReturnCode(CANCEL);
          close();
        }
      });
    }
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("CSS style editing dialog");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Browser for preview
  //
  ////////////////////////////////////////////////////////////////////////////
  private RulePreviewControl m_rulePreviewControl;

  private Control createBrowser(Composite parent) {
    m_rulePreviewControl = new RulePreviewControl(parent, SWT.NONE);
    updateBrowser();
    m_semantics.addListener(new IValueListener() {
      public void changed(AbstractValue value) {
        updateBrowser();
      }
    });
    return m_rulePreviewControl;
  }

  private void updateBrowser() {
    CssRuleNode rule = new CssRuleNode();
    m_semantics.update(rule);
    m_rulePreviewControl.showRule(rule);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style folder
  //
  ////////////////////////////////////////////////////////////////////////////
  private Control createFolder(Composite parent) {
    TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
    //long start = System.nanoTime();
    for (int i = 0; i < 1; i++) {
      createTabFont(tabFolder);
      createTabBackground(tabFolder);
      createTabBox(tabFolder);
      createTabBorder(tabFolder);
      createTabText(tabFolder);
      createTabOther(tabFolder);
    }
    //System.out.println("tabs time: " + (System.nanoTime() - start) / 1000000.0);
    return tabFolder;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Font" tab
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createTabFont(TabFolder tabFolder) {
    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText("Font");
    //
    FontProperty font = m_semantics.m_font;
    //
    Group fontGroup = new Group(tabFolder, SWT.NONE);
    tabItem.setControl(fontGroup);
    int numColumns = 4;
    GridLayoutFactory.create(fontGroup).columns(numColumns);
    fontGroup.setText("Font");
    // family
    {
      FontFamilyValueEditor editor =
          new FontFamilyValueEditor(m_options, "Family:", font.getFamily());
      editor.doFillGrid(fontGroup, numColumns);
    }
    // size
    {
      LengthValueEditor editor =
          new LengthValueEditor(m_options, font.getSize(), "Size:", new String[]{
              "",
              "8",
              "9",
              "10",
              "14",
              "16",
              "18",
              "24",
              "36",
              "smaller",
              "larger",
              "xx-small",
              "x-small",
              "small",
              "medium",
              "large",
              "x-large",
              "xx-large"});
      editor.doFillGrid(fontGroup, numColumns, true);
    }
    // style
    {
      String[] values = {"", "normal", "italic", "oblique"};
      SimpleValueEditor editor =
          new SimpleValueEditor(m_options, font.getStyle(), "Style:", values);
      editor.doFillGrid(fontGroup, numColumns, true, false);
    }
    // variant
    {
      String[] values = {"", "normal", "small-caps"};
      SimpleValueEditor editor =
          new SimpleValueEditor(m_options, font.getVariant(), "Variant:", values);
      editor.doFillGrid(fontGroup, numColumns, false, false);
    }
    // weight
    {
      SimpleValueEditor editor =
          new SimpleValueEditor(m_options, font.getWeight(), "Weight:", new String[]{
              "",
              "normal",
              "bold",
              "bolder",
              "lighter",
              "100",
              "200",
              "300",
              "400",
              "500",
              "600",
              "700",
              "800",
              "900"});
      editor.doFillGrid(fontGroup, numColumns, false, false);
    }
    // stretch
    {
      SimpleValueEditor editor =
          new SimpleValueEditor(m_options, font.getStretch(), "Stretch:", new String[]{
              "",
              "normal",
              "wider",
              "narrower",
              "ultra-condensed",
              "extra-condensed",
              "condensed",
              "semi-condensed",
              "semi-expanded",
              "expanded",
              "extra-expanded",
              "ultra-expanded"});
      editor.doFillGrid(fontGroup, numColumns, false, false);
    }
    // color
    {
      ColorValueEditor editor = new ColorValueEditor(m_options, "Color:", m_semantics.m_color);
      editor.doFillGrid(fontGroup, numColumns);
    }
    // decoration
    {
      SetsValueEditor editor =
          new SetsValueEditor(m_options,
              m_semantics.m_text.m_decoration,
              "Decoration",
              new String[][]{{"none"}, {"underline", "overline", "line-through", "blink"}});
      editor.doFillGrid(fontGroup, numColumns);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Box" tab
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createTabBox(TabFolder tabFolder) {
    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText("Box");
    //
    Composite composite = new Composite(tabFolder, SWT.NONE);
    GridLayoutFactory.create(composite);
    tabItem.setControl(composite);
    // margin
    {
      String[] values = {"auto", "1", "2", "3", "4", "5"};
      new LengthSidedPropertyGroup(composite,
          SWT.NONE,
          m_options,
          "Margin",
          m_semantics.m_margin,
          values);
    }
    // padding
    {
      String[] values = {"1", "2", "3", "4", "5"};
      new LengthSidedPropertyGroup(composite,
          SWT.NONE,
          m_options,
          "Padding",
          m_semantics.m_padding,
          values);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Border" tab
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createTabBorder(TabFolder tabFolder) {
    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText("Border");
    //
    Composite composite = new Composite(tabFolder, SWT.NONE);
    GridLayoutFactory.create(composite).columns(2);
    tabItem.setControl(composite);
    //
    BorderProperty border = m_semantics.m_border;
    String[] values = {"thin", "medium", "thick", "1", "2", "3", "4", "5"};
    new LengthSidedPropertyGroup(composite, SWT.NONE, m_options, "Width", border.getWidth(), values);
    new SimpleSidedPropertyGroup(composite,
        SWT.NONE,
        m_options,
        "Style",
        border.getStyle(),
        BorderProperty.STYLES);
    new ColorSidedPropertyGroup(composite, SWT.NONE, m_options, "Color", border.getColor());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Text" tab
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createTabText(TabFolder tabFolder) {
    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText("Text");
    //
    int numColumns = 4;
    TextProperty text = m_semantics.m_text;
    //
    Composite composite = new Composite(tabFolder, SWT.NONE);
    tabItem.setControl(composite);
    GridLayoutFactory.create(composite);
    // align's
    {
      Group alignGroup = new Group(composite, SWT.NONE);
      GridDataFactory.create(alignGroup).grabH().fillH();
      GridLayoutFactory.create(alignGroup).columns(numColumns);
      alignGroup.setText("Alignment");
      // horizontal
      {
        String[] values = {"", "left", "right", "center", "justify"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, text.m_align, "Horizontal:", values);
        editor.doFillGrid(alignGroup, numColumns, true, false);
      }
      // vertical
      {
        String[] values =
            {"baseline", "sub", "super", "top", "text-top", "middle", "bottom", "text-bottom"};
        LengthValueEditor editor =
            new LengthValueEditor(m_options, text.m_verticalAlign, "Vertical:", values);
        editor.doFillGrid(alignGroup, numColumns, true);
      }
    }
    // spacing
    {
      Group spacingGroup = new Group(composite, SWT.NONE);
      GridDataFactory.create(spacingGroup).grabH().fillH();
      GridLayoutFactory.create(spacingGroup).columns(numColumns);
      spacingGroup.setText("Spacing");
      //
      {
        String[] values = new String[]{"normal", "1", "2", "3", "4", "5"};
        // letter
        {
          LengthValueEditor editor =
              new LengthValueEditor(m_options, text.m_letterSpacing, "Letter spacing:", values);
          editor.doFillGrid(spacingGroup, numColumns, true);
        }
        // word
        {
          LengthValueEditor editor =
              new LengthValueEditor(m_options, text.m_wordSpacing, "Word spacing:", values);
          editor.doFillGrid(spacingGroup, numColumns, true);
        }
      }
      // line
      {
        String[] values = {"normal"};
        LengthValueEditor editor =
            new LengthValueEditor(m_options, text.m_lineHeight, "Line height:", values);
        editor.doFillGrid(spacingGroup, numColumns, true);
      }
    }
    // flow
    {
      Group flowGroup = new Group(composite, SWT.NONE);
      GridDataFactory.create(flowGroup).grabH().fillH();
      GridLayoutFactory.create(flowGroup).columns(numColumns);
      flowGroup.setText("Flow");
      // indent
      {
        String[] values = {"1", "2", "3", "4", "5"};
        LengthValueEditor editor =
            new LengthValueEditor(m_options, text.m_indent, "Indent:", values);
        editor.doFillGrid(flowGroup, numColumns, true);
      }
      // white-space
      {
        String[] values = {"", "normal", "pre", "nowrap"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, text.m_whiteSpace, "White space:", values);
        editor.doFillGrid(flowGroup, numColumns, true, false);
      }
      // direction
      {
        String[] values = {"", "ltr", "rtl"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, text.m_direction, "Direction:", values);
        editor.doFillGrid(flowGroup, numColumns, true, false);
      }
      // transform
      {
        String[] values = {"", "none", "capitalize", "uppercase", "lowercase"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, text.m_transform, "Transform:", values);
        editor.doFillGrid(flowGroup, numColumns, true, false);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Background" tab
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createTabBackground(TabFolder tabFolder) {
    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText("Background");
    //
    BackgroundProperty background = m_semantics.m_background;
    //
    Composite composite = new Composite(tabFolder, SWT.NONE);
    tabItem.setControl(composite);
    GridLayoutFactory.create(composite);
    // color
    {
      int numColumns = 4;
      Group colorGroup = new Group(composite, SWT.NONE);
      GridDataFactory.create(colorGroup).grabH().fillH();
      GridLayoutFactory.create(colorGroup).columns(numColumns);
      colorGroup.setText("Background color");
      //
      ColorValueEditor editor = new ColorValueEditor(m_options, "Color:", background.m_color);
      editor.doFillGrid(colorGroup, numColumns);
    }
    // image
    {
      int numColumns = 4;
      Group imageGroup = new Group(composite, SWT.NONE);
      GridDataFactory.create(imageGroup).grabH().fillH();
      GridLayoutFactory.create(imageGroup).columns(numColumns);
      imageGroup.setText("Background image");
      // image
      {
        String[] values =
            {
                "none",
                "url(home.gif)",
                "url(drafts.gif)",
                "url(blue_gradient.gif)",
                "url(grey_gradient.gif"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, background.m_image, "Image:", values);
        editor.doFillGrid(imageGroup, numColumns, true, true);
      }
      // repeat
      {
        String[] values = {"", "repeat", "repeat-x", "repeat-y", "no-repeat"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, background.m_repeat, "Repeat:", values);
        editor.doFillGrid(imageGroup, numColumns, true, false);
      }
      // attachment
      {
        String[] values = {"", "scroll", "fixed"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, background.m_attachment, "Attachment:", values);
        editor.doFillGrid(imageGroup, numColumns, true, false);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Other" tab
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createTabOther(TabFolder tabFolder) {
    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText("Other");
    //
    OtherProperty other = m_semantics.m_other;
    //
    Composite composite = new Composite(tabFolder, SWT.NONE);
    tabItem.setControl(composite);
    GridLayoutFactory.create(composite);
    // table
    {
      int numColumns = 3;
      Group group = new Group(composite, SWT.NONE);
      GridDataFactory.create(group).grabH().fillH();
      GridLayoutFactory.create(group).columns(numColumns);
      group.setText("Tables");
      // border-collapse
      {
        String[] values = {"", "collapse", "separate"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, other.m_borderCollapse, "Border collapse:", values);
        editor.doFillGrid(group, numColumns, true, false);
      }
      // table-layout
      {
        String[] values = {"", "auto", "fixed"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, other.m_tableLayout, "Table layout:", values);
        editor.doFillGrid(group, numColumns, true, false);
      }
    }
    // printing
    {
      int numColumns = 3;
      Group group = new Group(composite, SWT.NONE);
      GridDataFactory.create(group).grabH().fillH();
      GridLayoutFactory.create(group).columns(numColumns);
      group.setText("Printing pages");
      //
      String[] values = {"", "auto", "always", "avoid", "left", "right"};
      // before
      {
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, other.m_pageBreakBefore, "Break before:", values);
        editor.doFillGrid(group, numColumns, true, false);
      }
      // after
      {
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, other.m_pageBreakAfter, "Break after:", values);
        editor.doFillGrid(group, numColumns, true, false);
      }
    }
    // user interface
    {
      int numColumns = 3;
      Group group = new Group(composite, SWT.NONE);
      GridDataFactory.create(group).grabH().fillH();
      GridLayoutFactory.create(group).columns(numColumns);
      group.setText("User interface");
      // cursor
      {
        String[] values =
            {
                "auto",
                "crosshair",
                "default",
                "pointer",
                "move",
                "e-resize",
                "ne-resize",
                "nw-resize",
                "n-resize",
                "se-resize",
                "sw-resize",
                "s-resize",
                "w-resize",
                "text",
                "wait",
                "help"};
        SimpleValueEditor editor =
            new SimpleValueEditor(m_options, other.m_cursor, "Cursor:", values);
        editor.doFillGrid(group, numColumns, true, true);
      }
    }
  }
}
