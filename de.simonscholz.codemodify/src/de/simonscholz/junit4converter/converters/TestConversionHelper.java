package de.simonscholz.junit4converter.converters;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

final class TestConversionHelper {

	private static final String DBRULE_QUALIFIEDNAME = "CH.obj.Libraries.UnitTesting.BisonDbFunctions";
	private static final String DBRULE_CLASSNAME = "BisonDbFunctions";
	private static final String CUSTOMRULE_QUALIFIEDNAME = "CH.obj.Libraries.UnitTesting.BisonFunctions";
	private static final String CUSTOMRULE_CLASSNAME = "BisonFunctions";
	static final String DBRULE_VARIABLENAME = "_functions";
	private static final String RULE_QUALIFIEDNAME = "org.junit.Rule";
	private final ASTRewrite rewriter;
	private final ImportRewrite importRewriter;

	TestConversionHelper(ASTRewrite rewriter, ImportRewrite importRewriter) {
		this.rewriter = rewriter;
		this.importRewriter = importRewriter;
	}

	void addDBRule(TypeDeclaration typeDeclaration) {
		addRuleDeclaration(typeDeclaration, DBRULE_CLASSNAME,
				DBRULE_QUALIFIEDNAME);
	}

	void addRule(TypeDeclaration typeDeclaration) {
		addRuleDeclaration(typeDeclaration, CUSTOMRULE_CLASSNAME,
				CUSTOMRULE_QUALIFIEDNAME);
	}

	void replaceCallsOfSuperClass(TypeDeclaration typeDeclaration) {
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		for (MethodDeclaration method : methods) {
			method.accept(new RedirectCallsOfSuperClassToRuleMethodBodyVisitor(
					rewriter));
			method.accept(new ImportEasyMockIfNecessaryMethodBodyVisitor(
					importRewriter));
		}
	}

	boolean isTestCase(String superClassName, TypeDeclaration typeDeclaration) {
		Type superclassType = typeDeclaration.getSuperclassType();
		if (superclassType != null && superclassType.isSimpleType()) {
			SimpleType superType = (SimpleType) superclassType;
			return superClassName.equals(superType.getName()
					.getFullyQualifiedName());
		}
		return false;
	}

	void removeSuperClass(Type superType, String superclassQualifiedname) {
		rewriter.remove(superType, null);
		importRewriter.removeImport(superclassQualifiedname);
	}

	private void addRuleDeclaration(TypeDeclaration typeDeclaration,
			String ruleClassName, String ruleQualifiedName) {
		StringBuilder variableBuilder = new StringBuilder();
		variableBuilder.append("@Rule \n");
		variableBuilder.append("public ").append(ruleClassName).append(' ')
				.append(DBRULE_VARIABLENAME).append(" = new ")
				.append(ruleClassName).append("();");
		ASTNode variableDeclarationNode = rewriter.createStringPlaceholder(
				variableBuilder.toString(), ASTNode.EMPTY_STATEMENT);

		ListRewrite listRewrite = rewriter.getListRewrite(typeDeclaration,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(variableDeclarationNode, null);
		importRewriter.addImport(ruleQualifiedName);
		importRewriter.addImport(RULE_QUALIFIEDNAME);
	}
}
