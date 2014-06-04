var request  = require('request');

var proj_dir = "/Users/ruchith/Documents/research/anon_idm/source/java";
var jars_dir = proj_dir + "/target/";
var wallet_dir = proj_dir + "/scripts/wallet/";

var java = require("java");
java.classpath.push(jars_dir + "lib/base-0.1.jar");
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
java.classpath.push(jars_dir + "idp-1.0-SNAPSHOT.jar");


var Client = java.import('org.ruchith.research.idm.user.Client');
var client = new Client(wallet_dir);

client.generateRequest('student', function(err, val1){

	client.generateRequest('candidate', function(err, val2){

		//Make request to authenticate
		request.post('http://localhost:8001/authenticate_two_claims', {form:{request1:val1, request2:val2}}, function (error, response, body) {
			if (!error && response.statusCode == 200) {
				
				var challenge = JSON.parse(body);

				client.extractSessionKeyDouble('student', 'candidate', JSON.stringify(challenge.student), JSON.stringify(challenge.candidate), function(err, sk){
					if(typeof err != 'undefined') {
						console.log(err);
					} else {
						//Make request to operation
						request.post('http://localhost:8001/operation', {form:{session_key:sk}}, function (error2, response2, body2) {
							console.log(body2);
						});
					}
				});
			}
		});

	});
});