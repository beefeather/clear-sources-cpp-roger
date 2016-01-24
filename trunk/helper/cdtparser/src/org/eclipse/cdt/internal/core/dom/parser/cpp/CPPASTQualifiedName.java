/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;

import ru.spb.rybin.eclipsereplacement.Assert;

/**
 * Qualified name, which can contain any other name (unqualified, operator-name, conversion name, 
 * template id).
 */
public class CPPASTQualifiedName extends CPPASTNameBase
		implements ICPPASTQualifiedName, ICPPASTCompletionContext {
	private IASTName[] names;
	private int namesPos= -1;
	private boolean isFullyQualified;
	private char[] signature;

	public CPPASTQualifiedName() {
	}

	@Override
	public CPPASTQualifiedName copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTQualifiedName copy(CopyStyle style) {
		CPPASTQualifiedName copy = new CPPASTQualifiedName();
		for (IASTName name : getNames()) {
			copy.addName(name == null ? null : name.copy(style));
		}
		copy.setFullyQualified(isFullyQualified);
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public final IBinding resolvePreBinding() {
		// The whole qualified name resolves to the same thing as the last name.
		return getLastName().resolvePreBinding();
	}

	@Override
	public IBinding resolveBinding() {
		// The whole qualified name resolves to the same thing as the last name.
		IASTName lastName= getLastName();
		return lastName == null ? null : lastName.resolveBinding();
	}

    @Override
	public final IBinding getPreBinding() {
		// The whole qualified name resolves to the same thing as the last name.
		return getLastName().getPreBinding();
    }
    
	@Override
	public IBinding getBinding() {
		return getLastName().getBinding();
	}

	@Override
	public void setBinding(IBinding binding) {
		getLastName().setBinding(binding);
	}

	@Override
	public void addName(IASTName name) {
        assertNotFrozen();
		assert !(name instanceof ICPPASTQualifiedName);
		if (name != null) {
			names = ArrayUtil.appendAt(IASTName.class, names, ++namesPos, name);
			name.setParent(this);
			name.setPropertyInParent(SEGMENT_NAME);
		}
	}

	@Override
	public IASTName[] getNames() {
		if (namesPos < 0)
			return IASTName.EMPTY_NAME_ARRAY;
        
		names = ArrayUtil.trimAt(IASTName.class, names, namesPos);
		return names;
	}

	@Override
	public IASTName getLastName() {
		if (namesPos < 0)
			return null;
		
		return names[namesPos];
	}

	@Override
	public char[] getSimpleID() {
		return names[namesPos].getSimpleID();
	}
	
	@Override
	public char[] getLookupKey() {
		return names[namesPos].getLookupKey();
	}
	
	@Override
	public char[] toCharArray() {
		if (signature == null) {
			StringBuilder buf= new StringBuilder();
			for (int i = 0; i <= namesPos; i++) {
				if (i > 0 || isFullyQualified) {
					buf.append(Keywords.cpCOLONCOLON);
				}
				buf.append(names[i].toCharArray());
			}

			final int len= buf.length();
			signature= new char[len];
			buf.getChars(0, len, signature, 0);
		}
		return signature;
	}

	@Override
	public boolean isFullyQualified() {
		return isFullyQualified;
	}

	@Override
	public void setFullyQualified(boolean isFullyQualified) {
        assertNotFrozen();
		this.isFullyQualified = isFullyQualified;
	}

	/**
	 * @deprecated there is no need to set the signature, it will be computed lazily.
	 */
	@Deprecated
	public void setSignature(String signature) {
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitNames) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		for (int i = 0; i <= namesPos; i++) {
			final IASTName name = names[i];
			if (i == namesPos) {
				// Pointer-to-member qualified names have a dummy name as the last segment
				// of the name, don't visit it.
				if (name.getLookupKey().length > 0 && !name.accept(action))
					return false;
			} else if (!name.accept(action))
				return false;
		}
		
		if (action.shouldVisitNames) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		
		return true;
	}
	
	@Override
	public int getRoleOfName(boolean allowResolution) {
        IASTNode parent = getParent();
        if (parent instanceof IASTInternalNameOwner) {
        	return ((IASTInternalNameOwner) parent).getRoleForName(this, allowResolution);
        }
        if (parent instanceof IASTNameOwner) {
            return ((IASTNameOwner) parent).getRoleForName(this);
        }
        return IASTNameOwner.r_unclear;
	}

	@Override
	public int getRoleForName(IASTName n) {
		for (int i=0; i < namesPos; ++i) {
			if (names[i] == n) 
				return r_reference;
		}
		if (getLastName() == n) {
			IASTNode p = getParent();
			if (p instanceof IASTNameOwner) {
				return ((IASTNameOwner)p).getRoleForName(this);
			}
		}
		return r_unclear;
	}
	
	@Override
	public boolean isConversionOrOperator() {
		final IASTName lastName= getLastName();
		if (lastName instanceof ICPPASTConversionName || lastName instanceof ICPPASTOperatorName) {
			return true;
		}
		
		// check templateId's name
		if (lastName instanceof ICPPASTTemplateId) {
			IASTName tempName = ((ICPPASTTemplateId)lastName).getTemplateName();
			if (tempName instanceof ICPPASTConversionName || tempName instanceof ICPPASTOperatorName) {
				return true;
			}
		}
		
		return false;
	}
    
	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
		
		if (namesPos > 0) {
			IBinding binding = names[namesPos - 1].resolveBinding();
			if (binding instanceof ICPPClassType) {
				ICPPClassType classType = (ICPPClassType) binding;
				final boolean isDeclaration = getParent().getParent() instanceof IASTSimpleDeclaration;
				List<IBinding> filtered = filterClassScopeBindings(classType, bindings, isDeclaration);
				if (isDeclaration && nameMatches(classType.getNameCharArray(),
						n.getLookupKey(), isPrefix)) {
					ICPPConstructor[] constructors = ClassTypeHelper.getConstructors(classType, n);
					for (int i = 0; i < constructors.length; i++) {
						if (!constructors[i].isImplicit()) {
							filtered.add(constructors[i]);
						}
					}
				}
				return filtered.toArray(new IBinding[filtered.size()]);
			}
		}

		return bindings;
	}
	
	private boolean canBeFieldAccess(ICPPClassType baseClass) {
		IASTNode parent= getParent();
		if (parent instanceof IASTFieldReference) {
			return true;
		}
		if (parent instanceof IASTIdExpression) {
			IScope scope= CPPVisitor.getContainingScope(this);
			try {
				while(scope != null) {
					if (scope instanceof ICPPClassScope) {
						ICPPClassType classType = ((ICPPClassScope) scope).getClassType();
						if (SemanticUtil.calculateInheritanceDepth(classType, baseClass, this) >= 0) {
							return true;
						}
					}
					scope= scope.getParent();
				}
			} catch (DOMException e) {
			}
		}
		return false;
	}

	private List<IBinding> filterClassScopeBindings(ICPPClassType classType,
			IBinding[] bindings, final boolean isDeclaration) {
		List<IBinding> filtered = new ArrayList<IBinding>();
		final boolean canBeFieldAccess= canBeFieldAccess(classType);

		for (final IBinding binding : bindings) {
			if (binding instanceof IField) {
				IField field = (IField) binding;
				if (!canBeFieldAccess && !field.isStatic()) 
					continue;
			} else if (binding instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod) binding;
				if (method.isImplicit()) 
					continue;
				if (!isDeclaration) {
					if (method.isDestructor() || method instanceof ICPPConstructor
							|| (!canBeFieldAccess && !method.isStatic()))
						continue;
				}
			} else if (binding instanceof IEnumerator || binding instanceof IEnumeration) {
				if (isDeclaration)
					continue;
			} else if (binding instanceof IType) {
				IType type = (IType) binding;
				if (type.isSameType(classType)) 
					continue;
			} 
			filtered.add(binding);
		}
		
		return filtered;
	}
	
	private boolean nameMatches(char[] potential, char[] name, boolean isPrefix) {
		if (isPrefix)
			return ContentAssistMatcherFactory.getInstance().match(name, potential);
		return CharArrayUtils.equals(potential, name);
	}

	@Override
	protected IBinding createIntermediateBinding() {
		Assert.isLegal(false);
		return null;
	}
	
	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}
