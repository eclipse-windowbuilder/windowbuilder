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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;

import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * {@link IContentAssistProcessor} for {@code EL} properties.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class ContentAssistProcessor implements IContentAssistProcessor {
	private static final char[] TOP_LEVEL_AUTO_ACTIVATION_CHARACTERS = {'$', '#'};
	private static final char[] AUTO_ACTIVATION_CHARACTERS = {'.'};
	private final IBeanPropertiesSupport m_propertiesSupport;
	private final ResourceManager m_resourceManager;
	private final boolean m_topLevel;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ContentAssistProcessor(IBeanPropertiesSupport propertiesSupport, ResourceManager resourceManager, boolean topLevel) {
		m_propertiesSupport = propertiesSupport;
		m_resourceManager = resourceManager;
		m_topLevel = topLevel;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IContentAssistProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			if (m_propertiesSupport.getTopLevelBean() == null) {
				return null;
			}
			IDocument document = viewer.getDocument();
			if (m_topLevel) {
				//
				String begin = "${";
				if (offset > 0) {
					switch (document.getChar(offset - 1)) {
					case '$' :
					case '#' :
						begin = "{";
						break;
					case '{' :
						switch (document.getChar(offset - 2)) {
						case '$' :
						case '#' :
							begin = "";
							break;
						}
						break;
					}
				}
				return createProposals(createTopProperties(), offset, begin, "}");
			} else if (document.getChar(offset - 1) == '.') {
				int endOffset = offset - 1;
				int startOffset = endOffset - 1;
				while (startOffset > 0) {
					if (document.getChar(startOffset) == '{') {
						int propertyOffset = startOffset + 1;
						int length = endOffset - propertyOffset;
						if (length == 0) {
							break;
						}
						String property = document.get(propertyOffset, length).trim();
						List<ObserveInfo> properties =
								resolveProperties(StringUtils.split(property, '.'), createTopProperties(), 0);
						return properties == null ? null : createProposals(properties, offset, "", "");
					}
					startOffset--;
				}
			}
			//
			return null;
		} catch (Throwable e) {
			return new ICompletionProposal[]{new ErrorCompletionProposal(e)};
		}
	}

	private List<ObserveInfo> createTopProperties() throws Exception {
		BeanSupport beanSupport = new BeanSupport();
		beanSupport.doAddELProperty(false);
		beanSupport.doAddSelfProperty(false);
		//
		List<ObserveInfo> properties =
				beanSupport.createProperties(
						null,
						new ClassGenericType(m_propertiesSupport.getTopLevelBean(), null, null));
		return properties;
	}

	private List<ObserveInfo> resolveProperties(String[] properties,
			List<ObserveInfo> observes,
			int index) throws Exception {
		String property = properties[index++];
		for (ObserveInfo observe : observes) {
			if (observe.getPresentation().getText().equals(property)) {
				List<ObserveInfo> children =
						CoreUtils.cast(observe.getChildren(ChildrenContext.ChildrenForPropertiesTable));
				return index == properties.length ? children : resolveProperties(
						properties,
						children,
						index);
			}
		}
		return null;
	}

	private ICompletionProposal[] createProposals(List<ObserveInfo> properties,
			int offset,
			String begin,
			String end) throws Exception {
		ICompletionProposal[] proposals = new ICompletionProposal[properties.size()];
		// add proposals
		for (int i = 0; i < proposals.length; i++) {
			ObserveInfo observe = properties.get(i);
			String propertyName = observe.getPresentation().getText();
			String data = begin + propertyName + end;
			Image image = Optional.ofNullable(observe.getPresentation().getImageDescriptor()) //
					.map(m_resourceManager::createImage) //
					.orElse(null);
			// add proposal
			proposals[i] =
					new CompletionProposal(propertyName,
							image,
							offset,
							data,
							offset + data.length());
		}
		return proposals;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return m_topLevel ? TOP_LEVEL_AUTO_ACTIVATION_CHARACTERS : AUTO_ACTIVATION_CHARACTERS;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
}