/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Model for {@link FieldEditorPreferencePage}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class FieldEditorPreferencePageInfo extends PreferencePageInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FieldEditorPreferencePageInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// support special broadcasts for FieldEditor's
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void associationTemplate(JavaInfo component, String[] source) throws Exception {
				if (component.getParent() == FieldEditorPreferencePageInfo.this) {
					source[0] = StringUtils.replace(source[0], "%parentComposite%", "getFieldEditorParent()");
				}
			}

			@Override
			public void variable_emptyMaterializeBefore(EmptyVariableSupport variableSupport)
					throws Exception {
				if (variableSupport.getJavaInfo() instanceof FieldEditorInfo) {
					FieldEditorInfo fieldEditor = (FieldEditorInfo) variableSupport.getJavaInfo();
					convertStatementToBlock(fieldEditor);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link FieldEditorInfo} children.
	 */
	public List<FieldEditorInfo> getEditors() {
		return getChildren(FieldEditorInfo.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link FieldEditorInfo}.
	 */
	public void command_CREATE(FieldEditorInfo editor, FieldEditorInfo nextEditor) throws Exception {
		StatementTarget target = getTarget(editor, nextEditor);
		if (getDescription().getToolkit().getPreferences().getBoolean(
				IPreferenceConstants.PREF_FIELD_USUAL_CODE)) {
			AssociationObject association =
					AssociationObjects.invocationChild("%parent%.addField(%child%)", true);
			JavaInfoUtils.addTarget(editor, association, this, nextEditor, target);
		} else {
			JavaInfoUtils.add(
					editor,
					new EmptyInvocationVariableSupport(editor, "%parent%.addField(%child%)", 0),
					PureFlatStatementGenerator.INSTANCE,
					AssociationObjects.invocationChildNull(),
					this,
					nextEditor,
					target);
		}
	}

	/**
	 * Moves {@link FieldEditorInfo}.
	 */
	public void command_MOVE(FieldEditorInfo editor, FieldEditorInfo nextEditor) throws Exception {
		Assert.isLegal(editor.getParent() == this, Objects.toString(editor.getParent()));
		if (editor.getVariableSupport() instanceof EmptyVariableSupport) {
			ObjectInfo oldParent = editor.getParent();
			getBroadcastJava().moveBefore0(editor, oldParent, this);
			getBroadcastJava().moveBefore(editor, oldParent, this);
			// update source/AST
			{
				ASTNode editorNode = editor.getCreationSupport().getNode();
				Statement editorStatement = AstNodeUtils.getEnclosingStatement(editorNode);
				StatementTarget newTarget = getTarget(editor, nextEditor);
				getEditor().moveStatement(editorStatement, newTarget);
			}
			// move model
			oldParent.removeChild(editor);
			addChild(editor, nextEditor);
			getBroadcastJava().moveAfter(editor, oldParent, this);
		} else {
			JavaInfoUtils.move(editor, AssociationObjects.invocationChildNull(), this, nextEditor);
		}
	}

	/**
	 * @return the {@link StatementTarget} to move some {@link FieldEditorInfo} before
	 *         <code>nextEditor</code>.
	 */
	private StatementTarget getTarget(FieldEditorInfo editor, FieldEditorInfo nextEditor)
			throws Exception {
		if (nextEditor != null) {
			return JavaInfoUtils.getTarget(this, editor, nextEditor);
		} else {
			TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(this);
			MethodDeclaration targetMethod =
					AstNodeUtils.getMethodBySignature(typeDeclaration, "createFieldEditors()");
			return new StatementTarget(targetMethod, false);
		}
	}

	/**
	 * {@link #command_CREATE(FieldEditorInfo, FieldEditorInfo)} adds {@link FieldEditorInfo}'s with
	 * {@link EmptyVariableSupport}, using single {@link Statement} - such code looks concise and
	 * nice. But when we try to set {@link Property} value, {@link EmptyVariableSupport} will
	 * materialize {@link JavaInfo} with {@link LocalUniqueVariableSupport}. So, to keep code good we
	 * should convert existing {@link Statement} into {@link Block}.
	 */
	static void convertStatementToBlock(FieldEditorInfo fieldEditor) throws Exception {
		AstEditor editor = fieldEditor.getEditor();
		// prepare existing Statement
		Statement creationStatement;
		{
			ASTNode creationNode = fieldEditor.getCreationSupport().getNode();
			creationStatement = AstNodeUtils.getEnclosingStatement(creationNode);
		}
		// enclose in Block
		editor.encloseInBlock(creationStatement);
	}
}
