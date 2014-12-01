package de.simonscholz.junit4converter.converters;

import static de.simonscholz.junit4converter.converters.JTestCaseConverter.DBRULE_VARIABLENAME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class RedirectCallsOfSuperClassToRuleMethodBodyVisitor extends
		ASTVisitor {
	private ASTRewrite rewriter;
	private Map<String, Boolean> toBeReplacedMethodCalls;

	public RedirectCallsOfSuperClassToRuleMethodBodyVisitor(ASTRewrite rewriter) {
		this.rewriter = rewriter;
		createListOfToBeReplacedMethods();
	}

	private void createListOfToBeReplacedMethods() {
		toBeReplacedMethodCalls = new HashMap<>();
		toBeReplacedMethodCalls.put("getSession()", Boolean.FALSE);
		toBeReplacedMethodCalls.put("addImplementation(", Boolean.TRUE);
		toBeReplacedMethodCalls.put("createMockView(", Boolean.TRUE);
		toBeReplacedMethodCalls.put("addDatatypeProvider(", Boolean.TRUE);
	}

	@Override
	public boolean visit(Block methodBody) {
		for (Statement currentStatement : getStatements(methodBody)) {
			String statement = currentStatement.toString();
			for (Entry<String, Boolean> entry : toBeReplacedMethodCalls
					.entrySet()) {
				Boolean replaceOnlyStartsWith = entry.getValue();
				String toBeReplacedMethodCall = entry.getKey();

				boolean shouldReplace = false;
				if (replaceOnlyStartsWith) {
					if (statement.startsWith(toBeReplacedMethodCall)) {
						shouldReplace = true;
					}
				} else if (statement.contains(toBeReplacedMethodCall)) {
					shouldReplace = true;
				}

				if (shouldReplace) {
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
