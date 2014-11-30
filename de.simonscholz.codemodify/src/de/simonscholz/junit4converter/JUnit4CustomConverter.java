package de.simonscholz.junit4converter;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class JUnit4CustomConverter {

	private static final String NODB_TESTRULES_PROVIDER = "NoDBTestRulesProvider";
	private static final String NODB_TESTRULES_PROVIDER_QUALIFIEDNAME = "com.foo.res.NoDBTestRulesProvider";
	private static final String NODB_TESTCASE_CLASSNAME = "NoDBTestCase";
	private static final String NODB_TESTCASE_QUALIFIEDNAME = "com.foo.res.NoDBTestCase";
	private static final String JTESTCASE_CLASSNAME = "JTestCase";
	private static final String JTESTCASE_QUALIFIEDNAME = "com.foo.res.JTestCase";
	private static final String DBRULE_QUALIFIEDNAME = "com.foo.res.BisonDBRule";
	private static final String DBRULE_CLASSNAME = "BisonDBRule";
	private static final String DBRULE_VARIABLENAME = "_functions";
	private static final String RULE_QUALIFIEDNAME = "org.junit.Rule";

	private final ImportRewrite importRewriter;
	private final ASTRewrite rewriter;
	private final AST ast;
	private boolean _wasModified;

	JUnit4CustomConverter(AST ast, ASTRewrite rewriter,
			ImportRewrite importRewriter) {
		this.ast = ast;
		this.rewriter = rewriter;
		this.importRewriter = importRewriter;
	}

	void convert(TypeDeclaration typeDeclaration) {
		convertNoDBTestCase(typeDeclaration.getSuperclassType());
		convertJTestCase(typeDeclaration);
	}

	boolean wasConverted() {
		return _wasModified;
	}

	private void convertJTestCase(TypeDeclaration typeDeclaration) {
		Type superclassType = typeDeclaration.getSuperclassType();
		if (isJTestCase(superclassType)) {
			removeJTestSuperClass(superclassType);
			addDBRule(typeDeclaration);
		}
	}

	private boolean isJTestCase(Type superclassType) {
		if (superclassType != null && superclassType.isSimpleType()) {
			SimpleType superType = (SimpleType) superclassType;
			return JTESTCASE_CLASSNAME.equals(superType.getName()
					.getFullyQualifiedName());
		}
		return false;
	}

	private void addDBRule(TypeDeclaration typeDeclaration) {
		StringBuilder variableBuilder = new StringBuilder();
		variableBuilder.append("@Rule \n");
		variableBuilder.append("public ").append(DBRULE_CLASSNAME).append(' ')
				.append(DBRULE_VARIABLENAME).append(" = new ")
				.append(DBRULE_CLASSNAME).append("();");
		ASTNode variableDeclarationNode = rewriter.createStringPlaceholder(
				variableBuilder.toString(), ASTNode.EMPTY_STATEMENT);

		ListRewrite listRewrite = rewriter.getListRewrite(typeDeclaration,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(variableDeclarationNode, null);
		importRewriter.addImport(DBRULE_QUALIFIEDNAME);
		importRewriter.addImport(RULE_QUALIFIEDNAME);
	}

	private void removeJTestSuperClass(Type superType) {
		rewriter.remove(superType, null);
		importRewriter.removeImport(JTESTCASE_QUALIFIEDNAME);
		_wasModified = true;
	}

	private void convertNoDBTestCase(Type superclassType) {
		if (superclassType != null && superclassType.isSimpleType()) {
			SimpleType superType = (SimpleType) superclassType;
			if (NODB_TESTCASE_CLASSNAME.equals(superType.getName()
					.getFullyQualifiedName())) {
				SimpleType newNoDBTestRulesProviderSuperType = ast
						.newSimpleType(ast
								.newSimpleName(NODB_TESTRULES_PROVIDER));
				rewriter.replace(superType, newNoDBTestRulesProviderSuperType,
						null);
				importRewriter.removeImport(NODB_TESTCASE_QUALIFIEDNAME);
				importRewriter.addImport(NODB_TESTRULES_PROVIDER_QUALIFIEDNAME);
				_wasModified = true;
			}
		}

	}
}
