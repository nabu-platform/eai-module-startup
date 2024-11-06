/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
