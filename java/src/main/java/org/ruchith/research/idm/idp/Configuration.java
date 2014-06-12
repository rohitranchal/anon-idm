package org.ruchith.research.idm.idp;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Base64;


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
	private boolean useSameH1AndH2;

	private String configDirPath;
	private byte[] paramFileContents;
	private byte[] h1;
	private byte[] h2;

	/**
	 * Read configuration file and populate properties.
	 * 
	 * @throws RuntimeException
	 */
	private Configuration(String configPath) throws Exception {
		this.configDirPath = configPath;
		String configFilePath = this.configDirPath + File.separator + IDPConstants.CONFIG_FILE;

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
			this.useSameH1AndH2 = prop.getProperty("same_h1_and_h2").equals("true");

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		this.paramFileContents = FileUtils
				.readFileToByteArray(new File(this.configDirPath + File.separator + "params"));

		this.h1 = Base64.decode(FileUtils.readFileToByteArray(new File(this.configDirPath + File.separator + "h1")));

		this.h2 = Base64.decode(FileUtils.readFileToByteArray(new File(this.configDirPath + File.separator + "h2")));

	}

	/**
	 * Create an instance of the {@link Configuration}.
	 * 
	 * @return Populated {@link Configuration} instance.
	 */
	public static Configuration getInstance(String configPath) throws Exception {
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

	public byte[] getParamFileContents() {
		return paramFileContents;
	}

	public boolean isUseSameH1AndH2() {
		return useSameH1AndH2;
	}

	public byte[] getH1() {
		return h1;
	}

	public byte[] getH2() {
		return h2;
	}

}
