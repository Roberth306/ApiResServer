package app;

import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig{

	public Application() {
		packages("server");
	}
}
