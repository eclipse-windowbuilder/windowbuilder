/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.converter.FloatConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPageLayout;

import java.util.Map;

/**
 * Model for any top-level part of {@link IPageLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public abstract class AbstractPartInfo extends AbstractComponentInfo
implements
IRenderableInfo,
IPageLayoutTopLevelInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractPartInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link PageLayoutInfo}, i.e. just casted parent.
	 */
	public final PageLayoutInfo getPage() {
		return (PageLayoutInfo) getParent();
	}

	/**
	 * Registers {@link Control}s for each top level part. These {@link Control}s are used for layout
	 * and as references later.
	 */
	protected void registerLayoutControls(Map<String, Control> idToControl) {
		idToControl.put(getId(), (Control) getComponentObject());
	}

	/**
	 * Method {@link IPageLayout#addStandaloneView(String, boolean, int, float, String)} is exception,
	 * because for it relationship/ratio/refId arguments are shifted by 1.
	 *
	 * @return the offset index, 1 for <code>addStandaloneView</code>, 0 in other case.
	 */
	protected int getIndexOffest() {
		return 0;
	}

	@Override
	public final String getId() {
		return (String) getInvocationArgument(0);
	}

	@Override
	public final String getIdSource() {
		Expression expression = DomGenerics.arguments(getInvocation()).get(0);
		return getEditor().getSource(expression);
	}

	/**
	 * @return the position relative to the reference part; one of {@link IPageLayout#TOP},
	 *         {@link IPageLayout#BOTTOM}, {@link IPageLayout#LEFT} or {@link IPageLayout#RIGHT}.
	 */
	public final int getRelationship() {
		return (Integer) getInvocationArgument(1 + getIndexOffest());
	}

	/**
	 * Sets the "relationship" argument source.
	 */
	final void setRelationshipSource(String source) throws Exception {
		setInvocationArgumentSource(1 + getIndexOffest(), source);
	}

	/**
	 * @return the ratio specifying how to divide the space currently occupied by the reference part,
	 *         in the range <code>0.05f</code> to <code>0.95f</code>.
	 */
	public final float getRatio() {
		return (Float) getInvocationArgument(2 + getIndexOffest());
	}

	/**
	 * Sets the "ratio" argument source.
	 */
	final void setRatioSource(String source) throws Exception {
		setInvocationArgumentSource(2 + getIndexOffest(), source);
	}

	/**
	 * @return the <code>ID</code> of other {@link AbstractTopPart_Info}, relative to which this
	 *         {@link AbstractTopPart_Info} should be placed.
	 */
	public final String getRefId() {
		return (String) getInvocationArgument(3 + getIndexOffest());
	}

	public final String getRefIdSource() {
		Expression expression = DomGenerics.arguments(getInvocation()).get(3 + getIndexOffest());
		return getEditor().getSource(expression);
	}

	/**
	 * Sets the "refId" argument source.
	 */
	final void setRefIdSource(String source) throws Exception {
		setInvocationArgumentSource(3 + getIndexOffest(), source);
	}

	/**
	 * @return the signature of underlying {@link MethodInvocation}.
	 */
	protected final String getInvocationSignature() {
		MethodInvocation invocation = getInvocation();
		return AstNodeUtils.getMethodSignature(invocation);
	}

	/**
	 * @return the underlying {@link MethodInvocation}.
	 */
	protected final MethodInvocation getInvocation() {
		return (MethodInvocation) getCreationSupport().getNode();
	}

	/**
	 * @return the source of argument of underlying {@link MethodInvocation}.
	 */
	protected final String getInvocationArgumentSource(int index) {
		MethodInvocation invocation = getInvocation();
		Expression argument = DomGenerics.arguments(invocation).get(index);
		return getEditor().getSource(argument);
	}

	/**
	 * Sets the source of argument of underlying {@link MethodInvocation}.
	 */
	private void setInvocationArgumentSource(int index, String source) throws Exception {
		MethodInvocation invocation = getInvocation();
		Expression argument = DomGenerics.arguments(invocation).get(index);
		getEditor().replaceExpression(argument, source);
	}

	/**
	 * @return the value of argument of underlying {@link MethodInvocation}.
	 */
	private Object getInvocationArgument(int index) {
		MethodInvocation invocation = getInvocation();
		Expression argument = DomGenerics.arguments(invocation).get(index);
		return JavaInfoEvaluationHelper.getValue(argument);
	}

	/**
	 * Morph {@link MethodInvocation} to have different name and arguments.<br>
	 * Note however, that order of arguments is always same: id, relationship, ratio, refId.
	 */
	protected final void morphInvocation(final String pattern) throws Exception {
		AstEditor editor = getEditor();
		MethodInvocation invocation = getInvocation();
		int indexOffset = getIndexOffest();
		// prepare arguments
		String source_id = getInvocationArgumentSource(0);
		String source_rel = getInvocationArgumentSource(1 + indexOffset);
		String source_ratio = getInvocationArgumentSource(2 + indexOffset);
		String source_refId = getInvocationArgumentSource(3 + indexOffset);
		// replace MethodInvocation
		MethodInvocation newInvocation =
				(MethodInvocation) editor.replaceExpression(invocation, String.format(
						editor.getSource(invocation.getExpression()) + "." + pattern,
						source_id,
						source_rel,
						source_ratio,
						source_refId));
		// update CreationSupport
		((PageLayoutAddCreationSupport) getCreationSupport()).setInvocation(newInvocation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	private SashLineInfo m_line;

	/**
	 * Sets the {@link SashLineInfo} for this {@link AbstractPartInfo}, to access later from
	 * {@link #getSashLine()}.
	 */
	final void setLine(SashLineInfo line) {
		m_line = line;
	}

	/**
	 * @return the {@link SashLineInfo} to access bounds information for this {@link AbstractPartInfo}
	 *         and referenced {@link IPageLayoutTopLevelInfo}.
	 */
	public final SashLineInfo getSashLine() {
		return m_line;
	}

	/**
	 * Updates "ratio" using old and new size.
	 */
	public final void resize(int delta) throws Exception {
		int fullSize;
		int oldSize;
		if (m_line.isHorizontal()) {
			fullSize = m_line.getRefBounds().width;
			oldSize = m_line.getPartBounds().width;
		} else {
			fullSize = m_line.getRefBounds().height;
			oldSize = m_line.getPartBounds().height;
		}
		// prepare new "ratio"
		float newRatio;
		{
			float newSize = oldSize + delta;
			newRatio = normalizeRatio(newSize / fullSize);
			newRatio = (int) (100 * newRatio) / 100f;
			newRatio = Math.max(newRatio, IPageLayout.RATIO_MIN);
			newRatio = Math.min(newRatio, IPageLayout.RATIO_MAX);
		}
		// set new "ratio"
		{
			String code = FloatConverter.INSTANCE.toJavaSource(this, newRatio);
			setRatioSource(code);
		}
	}

	/**
	 * @return <code>true</code> if this {@link AbstractPartInfo} is "passive", i.e. its "ratio" means
	 *         that this amount of side is left to referenced part.
	 */
	private boolean isPassive() {
		int relationship = getRelationship();
		return relationship == IPageLayout.RIGHT || relationship == IPageLayout.BOTTOM;
	}

	/**
	 * @return the "normalized" ratio, i.e. coefficient that show fraction of size consumed by this
	 *         part, may be <code>ratio</code, may be <code>1 - ratio</code>.
	 */
	private float normalizeRatio(float ratio) {
		if (isPassive()) {
			return 1 - ratio;
		}
		return ratio;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public ImageDescriptor getIcon() throws Exception {
			return getPresentationIcon();
		}

		@Override
		public String getText() throws Exception {
			return getPresentationText();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	/**
	 * @return the icon to show in component tree.
	 */
	protected abstract ImageDescriptor getPresentationIcon() throws Exception;

	/**
	 * @return the text to show in component tree.
	 */
	protected abstract String getPresentationText() throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		{
			Control control = (Control) getComponentObject();
			setModelBounds(new Rectangle(control.getBounds()));
		}
		super.refresh_fetch();
	}
}
