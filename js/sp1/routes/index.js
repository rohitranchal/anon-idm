var fs = require('fs');
var uuid = require('node-uuid');

var files = fs.readdirSync('./claim_defs/');
var claim_defs = new Array();
var claim_def_student = null;
var claim_def_prof = null;

for(var i = 0; i < files.length; i++) {
	fs.readFile('./claim_defs/' + files[i], 'utf8', function(err, data) {
		claim_defs[claim_defs.length] = data;
		var name = JSON.parse(data).name;
		if(name == 'student') {
			claim_def_student = data;
		} else {
			claim_def_prof = data;
		}
	});
}


var jars_dir = "/Users/ruchith/Documents/research/anon_idm/source/java/target/";

var java = require("java");
java.classpath.push(jars_dir + "lib/base-0.1.jar");
java.classpath.push(jars_dir + "lib/bcprov-jdk16-1.46.jar");
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


var ServiceProvider = java.import('org.ruchith.research.idm.sp.ServiceProvider');
var sp = new ServiceProvider();



exports.index = function(req, res){
  res.render('index', { title: 'Express' });
};


var sessions = new Array();

exports.authenticate = function(req, res) {
	var user_req = req.body.request;

	// var session_key = uuid.v4();

	//Encrypt session key
	sp.createChallange(user_req, claim_def_student, function(err, val){
		val = JSON.parse(val);
		console.log(val.SessionKey);
		sessions[sessions.length] = val.SessionKey;
		res.send(val.EncryptedKey);
		if(typeof err != 'undefined') {
			console.log(err);
		}
	});
};

exports.operation = function(req, res) {

};
