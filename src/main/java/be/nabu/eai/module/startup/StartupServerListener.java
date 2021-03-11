package be.nabu.eai.module.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.server.Server;
import be.nabu.eai.server.api.ServerListener;
import be.nabu.libs.http.api.server.HTTPServer;

public class StartupServerListener implements ServerListener {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void listen(Server server, HTTPServer httpServer) {
		for (StartupServiceArtifact artifact : server.getRepository().getArtifacts(StartupServiceArtifact.class)) {
			if (artifact.getConfig().isRunAfterStartup()) {
				logger.info("Running service after startup: " + artifact.getId());
				try {
					artifact.runDirectly();
				}
				catch (Exception e) {
					logger.error("Failed to run service after startup: " + artifact.getId(), e);	
				}
			}
		}
	}

	// run last in line
	@Override
	public Priority getPriority() {
		return Priority.LOW;
	}
	
}
