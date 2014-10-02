package de.simonscholz.junit4converter.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.simonscholz.junit4converter.jobs.JUnit4ConversionJob;

public class ConvertToJUnit4Handler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final List list = ((IStructuredSelection) selection).toList();

			JUnit4ConversionJob job = new JUnit4ConversionJob(list);
			job.schedule();
		}

		return null;
	}


}