package be.nabu.eai.module.startup;

import java.lang.Thread.State;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.RepositoryThreadFactory;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.artifacts.api.InterruptibleArtifact;
import be.nabu.libs.artifacts.api.DeployHookArtifact;
import be.nabu.libs.artifacts.api.StoppableArtifact;
import be.nabu.libs.artifacts.api.TwoPhaseOfflineableArtifact;
import be.nabu.libs.artifacts.api.TwoPhaseStartableArtifact;
import be.nabu.libs.artifacts.api.TwoPhaseStoppableArtifact;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.types.api.ComplexContent;

public class StartupServiceArtifact extends JAXBArtifact<StartupServiceConfiguration> implements TwoPhaseStartableArtifact, DeployHookArtifact, InterruptibleArtifact, TwoPhaseStoppableArtifact, TwoPhaseOfflineableArtifact {

	private volatile boolean interrupted;
	
	private class StartableRunner implements Runnable {

		private volatile boolean aborted;
		private ServiceRuntime runtime;

		public void run() {
			runtime = new ServiceRuntime(getConfig().getService(), getRepository().newExecutionContext(SystemPrincipal.ROOT));
			ComplexContent content = getConfig().getService().getServiceInterface().getInputDefinition().newInstance();
			for (String key : getConfig().getProperties().keySet()) {
				content.set(key, getConfig().getProperties().get(key));
			}
			runtime.setContext(new HashMap<String, Object>());
			runtime.getContext().put("service.source", "startup");
			// set the service context explicitly, otherwise it seems to have none in some very strange circumstances...?
			ServiceUtils.setServiceContext(runtime, getConfig().getService().getId());
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
			if (runtime != null) {
				runtime.abort();
			}
		}
	}
	
	private class StartableWatcher implements Runnable {
		
		private volatile boolean aborted;
		
		@Override
		public void run() {
			while (!aborted && started) {
				// we wait for the running thread to stop
				try {
					thread.join();
				}
				catch (InterruptedException e) {
					aborted = true;
				}
				// if we didn't get aborted and the running thread stopped, restart it
				if (!aborted && started) {
					// the start procedure will set up a new watch dog, let this one die
					aborted = true;
					try {
						start();
						finish();
					}
					catch (Exception e) {
						logger.error("Could not restart the daemon", e);
					}
				}
			}
		}
		private void abort() {
			aborted = true;
		}
	}

	private boolean started, finished;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Thread thread, watchThread;
	private StartableRunner runnable;
	private StartableWatcher watcher;
	
	public StartupServiceArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "startup-service.xml", StartupServiceConfiguration.class);
	}

	@Override
	public void start() {
		if (getConfig().isRunAtStartup()) {
			// stop previous if still running
			if (isStarted()) {
				stop();
			}
			// only start if you have a service
			if (getConfig().getService() != null) {
				runnable = new StartableRunner();
				started = true;
				if (getConfig().isAsynchronous()) {
					thread = new RepositoryThreadFactory(getRepository(), true).newThread(runnable);
					thread.setName(getId());
					
					watcher = new StartableWatcher(); 
					watchThread = new RepositoryThreadFactory(getRepository(), true).newThread(watcher);
					watchThread.setName(getId() + ":watcher");
				}
			}
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
			// only interrupt threads that are sleeping (there are other reasons for timed_waiting but they are less relevant to us at this point?)
			// interrupting a thread that is active, can really mess up some things like H2 (using nio), lucene... because apparently nio filechannel just closes it once an interrupt is detected
			if (thread.getState() == State.TIMED_WAITING) {
				thread.interrupt();
			}
		}
	}

	@Override
	public void stop() {
		started = false;
		// abort the existing watcher first (so it doesn't try to restart the runnable)
		if (watcher != null && !watcher.aborted){
			watcher.abort();
			watcher = null;
		}
		// abort the existing runnable
		if (runnable != null && !runnable.aborted) {
			runnable.abort();
			runnable = null;
		}
		// interrupt the running thread (if necessary)
		interrupt();
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

	@Override
	public void finish() {
		if (getConfig().isRunAtStartup()) {
			finished = true;
			if (getConfig().isAsynchronous() && thread != null) {
				thread.start();
				// start watch thread _after_ the run thread
				watchThread.start();
			}
			else if (runnable != null) {
				runnable.run();
				// if we ran it synchronously, it is no longer in a started state, it is finished
				started = false;
			}
		}
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	public boolean isRunning() {
		return getConfig().isAsynchronous() && thread != null && thread.isAlive();
	}
	
	@Override
	public void postDeployment() {
		if (getConfig().isRunPostDeployment()) {
			runDirectly();
		}
	}
	
	@Override
	public void preDeployment() {
		if (getConfig().isRunPreDeployment()) {
			runDirectly();
		}
	}

	@Override
	public void duringDeployment() {
		if (getConfig().isRunDuringDeployment()) {
			runDirectly();
		}
	}

	protected void runDirectly() {
		// this should not occur, as asynchronous should not be combined with deployment hooks
		// but it was here before, so I left it...
		if (isRunning()) {
			interrupt();
		}
		else {
			new StartableRunner().run();
		}
	}

	// we want an early stop, as early as possible!
	@Override
	public void halt() {
		stop();
	}
	
}
