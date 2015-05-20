console.log("Including common.js");

var fs = require('fs');
var jars_dir = fs.readFileSync('./jars_dir', 'utf8').trim();
console.log("Reading jars_dir: " + jars_dir);

var java = require("java");
java.classpath.push(jars_dir + "lib/base-1.0-SNAPSHOT.jar");
java.classpath.push(jars_dir + "lib/bcprov-jdk16-1.46.jar");
java.classpath.push(jars_dir + "lib/commons-io-1.3.2.jar");
java.classpath.push(jars_dir + "lib/jackson-core-asl-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jackson-jaxrs-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jackson-mapper-asl-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jackson-mrbean-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jackson-xc-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jpbc-api-1.1.0.jar");
java.classpath.push(jars_dir + "lib/jpbc-crypto-1.1.0.jar");
java.classpath.push(jars_dir + "lib/jpbc-pbc-1.1.0.jar");
java.classpath.push(jars_dir + "lib/jpbc-plaf-1.1.0.jar");
java.classpath.push(jars_dir + "lib/junit-3.8.1.jar");
java.classpath.push(jars_dir + "lib/mysql-connector-java-5.1.22.jar");
java.classpath.push(jars_dir + "lib/httpclient-4.3.jar");
java.classpath.push(jars_dir + "lib/httpcore-4.3.jar");
java.classpath.push(jars_dir + "lib/commons-logging-1.1.3.jar");
java.classpath.push(jars_dir + "lib/idp-1.0-SNAPSHOT.jar");
java.classpath.push(jars_dir + "healthcare-0.1-SNAPSHOT.jar");

var keystore_dir = require('path').dirname(process.mainModule.filename) + "/keystore";
console.log("Setting keystore_dir: " + keystore_dir);
var wallet_dir = require('path').dirname(process.mainModule.filename) + "/wallet";
console.log("Setting wallet_dir: " + wallet_dir);

var Doctor = java.import('org.ruchith.research.scenarios.healthcare.consumer.Doctor');
var doctor = new Doctor(wallet_dir);

var promise = require('promise');

// export java related vars
exports.doctor = doctor;
exports.java = java;

// export promise
exports.promise = promise;

// export dir locations
exports.keystore_dir = keystore_dir;
exports.wallet_dir = wallet_dir;

// export user settings: this must be provided before execution
// [Certificate]
// The format for certificate file name is "id.cert"
exports.certname = "bob.cert"
// [Keystore]
// The format for keystore file name is "id.jks"
exports.id = 'bob'
exports.keystore_file = 'bob.jks';
exports.keystore_pass = 'bobkey';
exports.alias = 'bob';
exports.privatekey_pass = 'bobkey';
exports.self_domain = 'localhost';
exports.hie_address = 'localhost:3004';
