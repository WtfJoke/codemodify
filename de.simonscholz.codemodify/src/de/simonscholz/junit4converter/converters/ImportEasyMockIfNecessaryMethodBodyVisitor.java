package de.simonscholz.junit4converter.converters;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

class ImportEasyMockIfNecessaryMethodBodyVisitor extends ASTVisitor {

	private static final String EASY_MOCK = "EasyMock";
	private static final String EASY_MOCK_QUALIFIEDNAME = "CH.obj.Libraries.UnitTesting.BTestCase.EasyMock";
	private final ImportRewrite importRewriter;

	public ImportEasyMockIfNecessaryMethodBodyVisitor(
			ImportRewrite importRewriter) {
		this.importRewriter = importRewriter;
	}

	@Override
	public boolean visit(Block methodBody) {
		for (Statement currentStatement : getStatements(methodBody)) {
			String statement = currentStatement.toString();
			if (statement.contains(EASY_MOCK)) {
				importRewriter.addImport(EASY_MOCK_QUALIFIEDNAME);
				break;
			}
		}
		return true;

	}

	@SuppressWarnings("unchecked")
	private List<Statement> getStatements(Block methodBody) {
		return methodBody.statements();
	}
}
