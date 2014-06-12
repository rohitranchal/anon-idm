var request  = require('request');

var proj_dir = "/Users/ruchith/Documents/research/anon_idm/source/java";
var jars_dir = proj_dir + "/target/";
var wallet_dir = proj_dir + "/scripts/wallet/";

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
java.classpath.push(jars_dir + "idp-1.0-SNAPSHOT.jar");


var Client = java.import('org.ruchith.research.idm.user.Client');
var client = new Client(wallet_dir);

console.log(process.argv[2]);

var n = parseInt(process.argv[2]);


var start = new Date().getTime();
// client.generateANRequests(n, function(err, val){
client.generateANRequestsThreads(n, function(err, val){
	var t1 = new Date().getTime();
	request.post('http://ec2-54-82-231-14.compute-1.amazonaws.com:8001/authenticate_a_n_claims', {form:{request:val, num:n}}, function (error, response, body) {
		if (!error && response.statusCode == 200) {
			var t2 = new Date().getTime();
			client.extractSessionKeyAN(n, body, function(err, sk){
				var t3 = new Date().getTime();
				if(typeof err != 'undefined') {
					console.log(err);
				} else {
					//Make request to operation
					request.post('http://ec2-54-82-231-14.compute-1.amazonaws.com:8001/operation', {form:{session_key:sk}}, function (error2, response2, body2) {
						var end = new Date().getTime();

												console.log(body2);
						console.log('total : ' + (end - start));
						console.log('req_creation : ' + (t1 - start));
						console.log('auth call : ' + (t2 - t1));
						console.log('decryption : ' + (t3 - t2));
						console.log('verify call : ' + (end - t3));
					});
				}
			});
		} else {
			console.log(error);
		}
	});

});




