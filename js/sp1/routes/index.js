var fs = require('fs');
var uuid = require('node-uuid');

var files = fs.readdirSync('./claim_defs/');
var claim_defs = new Array();
var claim_def_student = null;
var claim_def_cand = null;

for(var i = 0; i < files.length; i++) {
	fs.readFile('./claim_defs/' + files[i], 'utf8', function(err, data) {

		var name = JSON.parse(data).name;
		if(name == 'student') {
			claim_def_student = data;
		} else if(name == 'candidate') {
			claim_def_cand = data;
		} else {
			var index = name.substring(5);
			claim_defs[index] = data;
		}
	});
}



var jars_dir = fs.readFileSync('jars_dir', 'utf8').trim();

console.log('Loading Java Libraries From : ' + jars_dir);

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
var last_session = null;

exports.authenticate = function(req, res) {
	var user_req = req.body.request;

	//Encrypt session key
	sp.createChallange(user_req, claim_def_student, function(err, val){
		if(typeof err != 'undefined') {
			console.log(err);
		}

		val = JSON.parse(val);
		last_session = val.SessionKey;
		sessions[sessions.length] = val.SessionKey;
		res.send(val.student);

	});
};

exports.authenticate_two_claims = function(req, res) {
	var user_req1 = req.body.request1;
	var user_req2 = req.body.request2;

	//Encrypt session key
	sp.createChallangeTwoClaims(user_req1, user_req2, claim_def_student, claim_def_cand, function(err, val){
		if(typeof err != 'undefined') {
			console.log(err);
		}
		val = JSON.parse(val);

		var session_key = val.SessionKey;

		sessions[sessions.length] = session_key;
		last_session = session_key;
		var result = {};
		result.student = val.student;
		result.candidate = val.candidate;
		res.send(result);

	});
};

exports.authenticate_n_claims = function(req, res) {
	var user_req = req.body.request;
	var claim_count = req.body.claims;

	claim_defs = JSON.stringify(claim_defs.slice(0, claim_count));

	//Encrypt session key
	sp.createChallangeNClaims(user_req, claim_defs, function(err, val){
		if(typeof err != 'undefined') {
			console.log(err);
		}
		val = JSON.parse(val);

		var session_key = val.SessionKey;

		sessions[sessions.length] = session_key;
		last_session = session_key;
		
		delete val.SessionKey;

		res.send(val);

	});
};


exports.auth_empty = function(req, res) {
	last_session = uuid.v4();
	sessions[sessions.length] = last_session;
	res.send(last_session);
};

exports.operation = function(req, res) {
	var session_key = req.body.session_key;

	//first check last session
	if(last_session === session_key) {
		res.send('Access Granted');
		return;
	}

	//not the last session check others
	for(var i = 0; i < sessions.length; i++) {
		console.log(session_key + ' == ' + sessions[i]);
		if(sessions[i] === session_key) {
			res.send('Access Granted');
			return;
		}
	}

	res.send('Access Denied');
};
