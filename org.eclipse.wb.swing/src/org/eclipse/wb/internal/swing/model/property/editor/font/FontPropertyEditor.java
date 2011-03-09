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
package org.eclipse.wb.internal.swing.model.property.editor.font;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jface.window.Window;

import java.awt.Font;
import java.util.Iterator;

/**
 * {@link PropertyEditor} for {@link Font}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class FontPropertyEditor extends TextDialogPropertyEditor
    implements
      IClipboardSourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new FontPropertyEditor();

  private FontPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    FontInfo fontInfo = getFontInfo(genericProperty);
    if (fontInfo != null) {
      return fontInfo.getText();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    FontInfo fontInfo = getFontInfo(property);
    if (fontInfo != null) {
      return fontInfo.getClipboardSource();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    FontDialog fontDialog = new FontDialog(DesignerPlugin.getShell());
    fontDialog.create();
    //
    GenericProperty genericProperty = (GenericProperty) property;
    // configure pages
    for (Iterator<AbstractFontPage> I = fontDialog.getPages().iterator(); I.hasNext();) {
      AbstractFontPage page = I.next();
      if (page instanceof DerivedFontPage) {
        DerivedFontPage derivedPage = (DerivedFontPage) page;
        boolean success = derivedPage.configure(genericProperty);
        if (!success) {
          I.remove();
        }
      }
    }
    // set initial value
    {
      FontInfo fontInfo = getFontInfo(genericProperty);
      fontDialog.setFontInfo(fontInfo);
    }
    // open dialog
    if (fontDialog.open() == Window.OK) {
      String source = fontDialog.getFontInfo().getSource();
      genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link FontInfo} that corresponds to the value of {@link GenericProperty}.
   */
  private static FontInfo getFontInfo(GenericProperty property) throws Exception {
    Font font;
    {
      Object value = property.getValue();
      if (value == null) {
        return new NullFontInfo();
      }
      if (!(value instanceof Font)) {
        return null;
      }
      font = (Font) value;
    }
    // analyze Expression
    Expression expression = property.getExpression();
    AstEditor editor = property.getJavaInfo().getEditor();
    //
    // UIManager.getFont(key)
    //
    if (AstNodeUtils.isMethodInvocation(
        expression,
        "javax.swing.UIManager",
        "getFont(java.lang.Object)")) {
      MethodInvocation invocation = (MethodInvocation) expression;
      Expression keyExpression = DomGenerics.arguments(invocation).get(0);
      String key = (String) JavaInfoEvaluationHelper.getValue(keyExpression);
      return new UiManagerFontInfo(key, font);
    }
    //
    // Font.deriveFont(*)
    //
    boolean deriveStyle =
        AstNodeUtils.isMethodInvocation(expression, "java.awt.Font", "deriveFont(int)");
    boolean deriveSize =
        AstNodeUtils.isMethodInvocation(expression, "java.awt.Font", "deriveFont(float)");
    boolean deriveStyleSize =
        AstNodeUtils.isMethodInvocation(expression, "java.awt.Font", "deriveFont(int,float)");
    if (deriveStyle || deriveSize || deriveStyleSize) {
      MethodInvocation invocation = (MethodInvocation) expression;
      // prepare base Font
      Font baseFont;
      String baseFontSource;
      String baseFontClipboardSource;
      {
        Expression baseFontExpression = invocation.getExpression();
        baseFont = (Font) JavaInfoEvaluationHelper.getValue(baseFontExpression);
        baseFontSource = editor.getSource(baseFontExpression);
        baseFontClipboardSource = getBaseFontClipboardSource(editor, baseFontExpression);
      }
      // Font.deriveFont(newStyle)
      if (deriveStyle) {
        Expression styleExpression = DomGenerics.arguments(invocation).get(0);
        Boolean[] styles = getFontInfo_decodeStyle(editor, baseFontSource, styleExpression);
        return new DerivedFontInfo(baseFont,
            baseFontSource,
            baseFontClipboardSource,
            null,
            styles[0],
            styles[1],
            null,
            null);
      }
      // Font.deriveFont(newSize)
      if (deriveSize) {
        Expression sizeExpression = DomGenerics.arguments(invocation).get(0);
        Integer[] sizes = getFontInfo_decodeSize(editor, baseFontSource, sizeExpression);
        return new DerivedFontInfo(baseFont,
            baseFontSource,
            baseFontClipboardSource,
            null,
            null,
            null,
            sizes[0],
            sizes[1]);
      }
      // Font.deriveFont(newStyle,newSize)
      if (deriveStyleSize) {
        Expression styleExpression = DomGenerics.arguments(invocation).get(0);
        Expression sizeExpression = DomGenerics.arguments(invocation).get(1);
        Boolean[] styles = getFontInfo_decodeStyle(editor, baseFontSource, styleExpression);
        Integer[] sizes = getFontInfo_decodeSize(editor, baseFontSource, sizeExpression);
        return new DerivedFontInfo(baseFont,
            baseFontSource,
            baseFontClipboardSource,
            null,
            styles[0],
            styles[1],
            sizes[0],
            sizes[1]);
      }
    }
    //
    // new Font(newFamily, derivedStyle, derivedSize)
    //
    if (AstNodeUtils.isCreation(expression, "java.awt.Font", "<init>(java.lang.String,int,int)")) {
      ClassInstanceCreation creation = (ClassInstanceCreation) expression;
      Expression familyExpression = DomGenerics.arguments(creation).get(0);
      Expression styleExpression = DomGenerics.arguments(creation).get(1);
      Expression sizeExpression = DomGenerics.arguments(creation).get(2);
      // prepare Expression for base Font 
      final Expression[] baseFontExpression = new Expression[1];
      {
        expression.accept(new ASTVisitor() {
          @Override
          public void endVisit(MethodInvocation node) {
            String expressionName = AstNodeUtils.getFullyQualifiedName(node.getExpression(), false);
            if (expressionName.equals("java.awt.Font")) {
              baseFontExpression[0] = node.getExpression();
            }
          }
        });
      }
      // if found reference on base Font...
      if (baseFontExpression[0] != null) {
        // prepare base Font
        Font baseFont;
        String baseFontSource;
        String baseFontClipboardSource;
        {
          baseFont = (Font) JavaInfoEvaluationHelper.getValue(baseFontExpression[0]);
          baseFontSource = editor.getSource(baseFontExpression[0]);
          baseFontClipboardSource = null;
        }
        // prepare updates
        String newFamily = (String) JavaInfoEvaluationHelper.getValue(familyExpression);
        Boolean[] styles = getFontInfo_decodeStyle(editor, baseFontSource, styleExpression);
        Integer[] sizes = getFontInfo_decodeSize(editor, baseFontSource, sizeExpression);
        return new DerivedFontInfo(baseFont,
            baseFontSource,
            baseFontClipboardSource,
            newFamily,
            styles[0],
            styles[1],
            sizes[0],
            sizes[1]);
      }
    }
    // no better FontInfo variant that explicit
    return new ExplicitFontInfo(font);
  }

  private static String getBaseFontClipboardSource(AstEditor editor, Expression baseFontExpression) {
    if (baseFontExpression instanceof MethodInvocation) {
      MethodInvocation invocation2 = (MethodInvocation) baseFontExpression;
      return "%this%."
          + editor.getSourceBeginEnd(
              invocation2.getName().getStartPosition(),
              AstNodeUtils.getSourceEnd(invocation2));
    } else {
      return null;
    }
  }

  /**
   * @return the array with 2 {@link Boolean} values - for <code>newBold</code> and for
   *         <code>newItalic</code>, any of them should be <code>null</code>.
   */
  private static Boolean[] getFontInfo_decodeStyle(final AstEditor editor,
      final String baseFontSource,
      Expression styleExpression) {
    final Boolean[] styles = new Boolean[]{null, null, false};
    styleExpression.accept(new ASTVisitor() {
      @Override
      public void endVisit(QualifiedName node) {
        // OR
        if (node.getParent() instanceof InfixExpression) {
          InfixExpression infixExpression = (InfixExpression) node.getParent();
          if (infixExpression.getOperator() == InfixExpression.Operator.OR) {
            int mask = ((Integer) JavaInfoEvaluationHelper.getValue(node)).intValue();
            if (mask == Font.BOLD) {
              styles[0] = true;
            }
            if (mask == Font.ITALIC) {
              styles[1] = true;
            }
          }
        }
        // AND complement
        if (node.getParent() instanceof PrefixExpression
            && node.getParent().getParent() instanceof InfixExpression) {
          PrefixExpression prefixExpression = (PrefixExpression) node.getParent();
          InfixExpression infixExpression = (InfixExpression) node.getParent().getParent();
          if (prefixExpression.getOperator() == PrefixExpression.Operator.COMPLEMENT
              && infixExpression.getOperator() == InfixExpression.Operator.AND) {
            int mask = ((Integer) JavaInfoEvaluationHelper.getValue(node)).intValue();
            if (mask == Font.BOLD) {
              styles[0] = false;
            }
            if (mask == Font.ITALIC) {
              styles[1] = false;
            }
          }
        }
      }

      @Override
      public void endVisit(MethodInvocation node) {
        if (editor.getSource(node).equals(baseFontSource + ".getStyle()")) {
          styles[2] = true;
        }
      }
    });
    return styles;
  }

  /**
   * @return the array with 2 {@link Integer} values - for <code>deltaSize</code> and for
   *         <code>newSize</code>, only one of them should be not <code>null</code>.
   */
  private static Integer[] getFontInfo_decodeSize(AstEditor editor,
      String baseFontSource,
      Expression sizeExpression) {
    // size as +/-
    if (sizeExpression instanceof InfixExpression) {
      InfixExpression infixExpression = (InfixExpression) sizeExpression;
      Expression leftOperand = infixExpression.getLeftOperand();
      Expression rightOperand = infixExpression.getRightOperand();
      boolean isBaseFontSize = editor.getSource(leftOperand).equals(baseFontSource + ".getSize()");
      if (isBaseFontSize && rightOperand instanceof NumberLiteral) {
        int delta = ((Number) JavaInfoEvaluationHelper.getValue(rightOperand)).intValue();
        if (infixExpression.getOperator() == InfixExpression.Operator.PLUS) {
          return new Integer[]{delta, null};
        }
        if (infixExpression.getOperator() == InfixExpression.Operator.MINUS) {
          return new Integer[]{-delta, null};
        }
      }
    }
    // size as value
    int size = ((Number) JavaInfoEvaluationHelper.getValue(sizeExpression)).intValue();
    return new Integer[]{null, size};
  }
}
