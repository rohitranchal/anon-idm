package org.ruchith.research.idm.idp;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Identity provider configuration data.
 * 
 * @author Ruchith Fernando
 * 
 */
public class Configuration {

	private static Configuration config;

	private String keystoreName;
	private String keystoreType;
	private String keystorePassword;
	private String privKeyAlias;
	private String privKeyPassword;

	private String dbName;
	private String dbUser;
	private String dbPassword;
	private String dbHost;

	private String configDirPath;

	/**
	 * Read configuration file and populate properties.
	 * 
	 * @throws RuntimeException
	 */
	private Configuration(String configPath) throws RuntimeException {
		this.configDirPath = configPath;
		String configFilePath = this.configDirPath + File.separator
				+ IDPConstants.CONFIG_FILE;

		// check whether the configuration file exists
		if (!new File(configFilePath).exists()) {
			throw new RuntimeException("Invalid configuration" + configFilePath);
		}

		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(configFilePath));
			this.dbHost = prop.getProperty("db_host");
			this.dbName = prop.getProperty("db_name");
			this.dbUser = prop.getProperty("db_user");
			this.dbPassword = prop.getProperty("db_password");

			this.keystoreName = prop.getProperty("keystore_name");
			this.keystoreType = prop.getProperty("keystore_type");
			this.keystorePassword = prop.getProperty("keystore_password");
			this.privKeyAlias = prop.getProperty("private_key_alias");
			this.privKeyPassword = prop.getProperty("private_key_password");

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Create an instance of the {@link Configuration}.
	 * 
	 * @return Populated {@link Configuration} instance.
	 */
	public static Configuration getInstance(String configPath) {
		if (config == null) {
			config = new Configuration(configPath);
		}

		return config;
	}

	/**
	 * Return the full path of the keystore file.
	 * 
	 * @return
	 */
	public String getKeystoreFilePath() {
		return this.configDirPath + File.separator + this.keystoreName;
	}

	public String getKeystoreName() {
		return keystoreName;
	}

	public String getKeystoreType() {
		return keystoreType;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public String getPrivKeyAlias() {
		return privKeyAlias;
	}

	public String getPrivKeyPassword() {
		return privKeyPassword;
	}

	public String getDbName() {
		return dbName;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public String getDbHost() {
		return dbHost;
	}

}
