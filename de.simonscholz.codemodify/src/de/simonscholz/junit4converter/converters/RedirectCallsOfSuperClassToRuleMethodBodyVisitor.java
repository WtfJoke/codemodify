package de.simonscholz.junit4converter.converters;

import static de.simonscholz.junit4converter.converters.JTestCaseConverter.DBRULE_VARIABLENAME;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class RedirectCallsOfSuperClassToRuleMethodBodyVisitor extends
		ASTVisitor {
	private ASTRewrite rewriter;
	private List<String> toBeReplacedMethodCalls;

	public RedirectCallsOfSuperClassToRuleMethodBodyVisitor(ASTRewrite rewriter) {
		this.rewriter = rewriter;
		createListOfToBeReplacedMethods();
	}

	private void createListOfToBeReplacedMethods() {
		toBeReplacedMethodCalls = new ArrayList<>();
		toBeReplacedMethodCalls.add("getSession()");
		toBeReplacedMethodCalls.add("addImplementation(");
		toBeReplacedMethodCalls.add("createMockView(");
		toBeReplacedMethodCalls.add("addImplementation(");
		toBeReplacedMethodCalls.add("addDatatypeProvider(");
	}

	@Override
	public boolean visit(Block methodBody) {
		for (Statement currentStatement : getStatements(methodBody)) {
			String statement = currentStatement.toString();
			for (String toBeReplacedMethodCall : toBeReplacedMethodCalls) {
				if (statement.contains(toBeReplacedMethodCall)) {
					int startOfReplacement = statement
							.indexOf(toBeReplacedMethodCall);
					int previousIndex = startOfReplacement - 1;
					if (previousIndex >= 0) {
						char previousChar = statement.charAt(previousIndex);
						if (previousChar == '.') {
							continue;
						}
					}
					String newStatement = createReplacingStatement(statement,
							toBeReplacedMethodCall);
					ASTNode delegateToRule = rewriter.createStringPlaceholder(
							newStatement, ASTNode.EMPTY_STATEMENT);
					rewriter.replace(currentStatement, delegateToRule, null);
				}
			}
		}
		return true;
	}

	private String createReplacingStatement(String currentStatement,
			String toReplacingMethodCall) {
		String newMethodCall = DBRULE_VARIABLENAME + '.'
				+ toReplacingMethodCall;
		String newStatement = currentStatement.replace(toReplacingMethodCall,
				newMethodCall);
		return newStatement;
	}

	@SuppressWarnings("unchecked")
	private List<Statement> getStatements(Block methodBody) {
		return methodBody.statements();
	}

}
