package be.nabu.eai.module.startup;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.RepositoryThreadFactory;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.artifacts.api.InterruptibleArtifact;
import be.nabu.libs.artifacts.api.StartableArtifact;
import be.nabu.libs.artifacts.api.StoppableArtifact;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.types.api.ComplexContent;

public class StartupServiceArtifact extends JAXBArtifact<StartupServiceConfiguration> implements StartableArtifact, InterruptibleArtifact, StoppableArtifact {

	private boolean interrupted;
	
	private class StartableRunner implements Runnable {
		private boolean aborted;
		private ServiceRuntime runtime;

		public void run() {
			runtime = new ServiceRuntime(getConfig().getService(), getRepository().newExecutionContext(SystemPrincipal.ROOT));
			ComplexContent content = getConfig().getService().getServiceInterface().getInputDefinition().newInstance();
			for (String key : getConfig().getProperties().keySet()) {
				content.set(key, getConfig().getProperties().get(key));
			}
			try {
				runtime.run(content);
			}
			catch (Exception e) {
				logger.error("Could not run startup service: " + getConfig().getService().getId(), e);
			}
			finally {
				if (!aborted) {
					started = false;
				}
			}
		}
		
		private void abort() {
			aborted = true;
			runtime.abort();
		}
	}

	private boolean started;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Thread thread;
	private StartableRunner runnable;
	
	public StartupServiceArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "startup-service.xml", StartupServiceConfiguration.class);
	}

	@Override
	public void start() throws IOException {
		// stop previous if still running
		if (isStarted()) {
			stop();
		}
		runnable = new StartableRunner();
		started = true;
		if (getConfig().isAsynchronous()) {
			thread = new RepositoryThreadFactory(getRepository(), true).newThread(runnable);
			thread.setName(getId());
			thread.start();
		}
		else {
			runnable.run();
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public void interrupt() {
		if (thread != null) {
			interrupted = true;
			thread.interrupt();
		}
	}

	@Override
	public void stop() throws IOException {
		// abort the existing runnable
		if (runnable != null && !runnable.aborted) {
			runnable.abort();
			started = false;
		}
	}

	@Override
	public StartPhase getPhase() {
		return StartPhase.LATE;
	}

	@Override
	public boolean interrupted() {
		boolean tmp = interrupted;
		interrupted = false;
		return tmp;
	}

}
