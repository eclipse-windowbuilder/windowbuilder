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
package org.eclipse.wb.internal.swing.model.component.menu;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.core.model.association.InvocationAssociation;
import org.eclipse.wb.internal.core.model.generation.statement.AbstractInsideStatementGenerator;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.lang.StringUtils;

import java.awt.Component;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JPopupMenu;

/**
 * Implementation of {@link Association} between {@link JPopupMenuInfo} and {@link ComponentInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public final class JPopupMenuAssociation extends InvocationAssociation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public JPopupMenuAssociation() {
  }

  public JPopupMenuAssociation(MethodInvocation invocation) {
    super(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(JavaInfo javaInfo, StatementTarget target, String[] leadingComments)
      throws Exception {
    AstEditor editor = javaInfo.getEditor();
    // ensure addPopup() method
    ensure_addPopup(editor, target);
    // initialize MethodInvocation instance
    {
      String source =
          AssociationUtils.replaceTemplates(javaInfo, "addPopup(%parent%, %child%);", target);
      List<String> lines = GenericsUtils.asList(leadingComments, source);
      ExpressionStatement statement = (ExpressionStatement) editor.addStatement(lines, target);
      m_invocation = (MethodInvocation) statement.getExpression();
    }
    // add related nodes
    AbstractInsideStatementGenerator.addRelatedNodes(javaInfo, m_invocation);
    // set association
    setInModelNoCompound(javaInfo);
  }

  @Override
  public boolean remove() throws Exception {
    Statement statement = getStatement();
    m_editor.removeEnclosingStatement(statement);
    // continue
    return super.remove();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that {@link TypeDeclaration} has method "addPopup()", that is used by Designer to
   * attach {@link JPopupMenu} to {@link Component}.
   */
  private void ensure_addPopup(AstEditor editor, StatementTarget target) throws Exception {
    TypeDeclaration typeDeclaration = editor.getEnclosingType(target.getPosition());
    if (AstNodeUtils.getMethodBySignature(
        typeDeclaration,
        "addPopup(java.awt.Component,javax.swing.JPopupMenu)") == null) {
      // prepare method lines
      List<String> lines = Lists.newArrayList();
      {
        lines.add("component.addMouseListener(new java.awt.event.MouseAdapter() {");
        lines.add("  public void mousePressed(java.awt.event.MouseEvent e) {");
        lines.add("    if (e.isPopupTrigger()) {");
        lines.add("      showMenu(e);");
        lines.add("    }");
        lines.add("  }");
        lines.add("  public void mouseReleased(java.awt.event.MouseEvent e) {");
        lines.add("    if (e.isPopupTrigger()) {");
        lines.add("      showMenu(e);");
        lines.add("    }");
        lines.add("  }");
        lines.add("  private void showMenu(java.awt.event.MouseEvent e) {");
        lines.add("    popup.show(e.getComponent(), e.getX(), e.getY());");
        lines.add("  }");
        lines.add("});");
        // replace spaces with "\t"
        for (ListIterator<String> I = lines.listIterator(); I.hasNext();) {
          String line = I.next();
          // prepare count of leading spaces
          int spaceCount = 0;
          for (char c : line.toCharArray()) {
            if (c != ' ') {
              break;
            }
            spaceCount++;
          }
          // replace each two leading spaces with one \t
          line = StringUtils.repeat("\t", spaceCount / 2) + line.substring(spaceCount);
          I.set(line);
        }
      }
      // add method
      {
        String header =
            "private static void addPopup(java.awt.Component component, "
                + "final javax.swing.JPopupMenu popup)";
        BodyDeclarationTarget methodTarget = new BodyDeclarationTarget(typeDeclaration, false);
        editor.addMethodDeclaration(header, lines, methodTarget);
      }
    }
  }
}
