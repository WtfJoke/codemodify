package de.simonscholz.junit4converter.converters;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class JTestCaseConverter implements Converter {
	private static final String JTESTCASE_CLASSNAME = "JTestCase";
	private static final String JTESTCASE_QUALIFIEDNAME = "com.foo.res.JTestCase";
	private static final String DBRULE_QUALIFIEDNAME = "com.foo.res.BisonDBRule";
	private static final String DBRULE_CLASSNAME = "BisonDBRule";
	private static final String DBRULE_VARIABLENAME = "_functions";
	private static final String RULE_QUALIFIEDNAME = "org.junit.Rule";
	private static final String GETSESSIONMETHODCALL = "getSession()";

	private final ASTRewrite rewriter;
	private final ImportRewrite importRewriter;
	private boolean wasModified;

	JTestCaseConverter(ASTRewrite rewriter, ImportRewrite importRewriter) {
		this.rewriter = rewriter;
		this.importRewriter = importRewriter;
	}

	@Override
	public boolean isConveratable(TypeDeclaration typeDeclaration) {
		return isJTestCase(typeDeclaration.getSuperclassType());
	}

	private boolean isJTestCase(Type superclassType) {
		if (superclassType != null && superclassType.isSimpleType()) {
			SimpleType superType = (SimpleType) superclassType;
			return JTESTCASE_CLASSNAME.equals(superType.getName()
					.getFullyQualifiedName());
		}
		return false;
	}

	@Override
	public void convert(TypeDeclaration typeDeclaration) {
		wasModified = true;
		Type superclassType = typeDeclaration.getSuperclassType();
		removeJTestSuperClass(superclassType);
		addDBRule(typeDeclaration);
		replaceCallsOfSuperClass(typeDeclaration);
	}

	@Override
	public boolean wasConverted() {
		return wasModified;
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
	}

	private void replaceCallsOfSuperClass(TypeDeclaration typeDeclaration) {
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			List<Statement> statements = methodDeclaration.getBody()
					.statements();
			for (Statement statement : statements) {
				String statementAsString = statement.toString();
				if (statementAsString.contains(GETSESSIONMETHODCALL)) {
					String newStatement = statementAsString.replace(
							GETSESSIONMETHODCALL, DBRULE_VARIABLENAME + '.'
									+ GETSESSIONMETHODCALL);
					ASTNode delegateToRule = rewriter.createStringPlaceholder(
							newStatement, ASTNode.EMPTY_STATEMENT);
					rewriter.replace(statement, delegateToRule, null);
				}
			}
		}
	}
}
