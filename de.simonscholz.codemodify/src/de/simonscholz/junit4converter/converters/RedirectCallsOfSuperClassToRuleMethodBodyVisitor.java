package de.simonscholz.junit4converter.converters;

import static de.simonscholz.junit4converter.converters.JTestCaseConverter.DBRULE_VARIABLENAME;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class RedirectCallsOfSuperClassToRuleMethodBodyVisitor extends
		ASTVisitor {
	private static final String GETSESSIONMETHODCALL = "getSession()";
	private ASTRewrite rewriter;

	public RedirectCallsOfSuperClassToRuleMethodBodyVisitor(ASTRewrite rewriter) {
		this.rewriter = rewriter;
	}

	@Override
	public boolean visit(Block methodBody) {
		for (Statement statement : getStatements(methodBody)) {
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
		return true;
	}

	@SuppressWarnings("unchecked")
	private List<Statement> getStatements(Block methodBody) {
		return methodBody.statements();
	}

}
