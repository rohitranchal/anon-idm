var db = require('../db');
fs = require('fs');

var java = require("java");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/base-0.1.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/bcprov-jdk16-1.46.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jackson-core-asl-1.9.4.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jackson-jaxrs-1.9.4.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jackson-mapper-asl-1.9.4.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jackson-mrbean-1.9.4.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jackson-xc-1.9.4.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jpbc-api-1.1.0.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jpbc-crypto-1.1.0.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jpbc-pbc-1.1.0.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/jpbc-plaf-1.1.0.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/junit-3.8.1.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/lib/mysql-connector-java-5.1.22.jar");
java.classpath.push("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/target/idp-1.0-SNAPSHOT.jar");

var IdentityManager = java.import('org.ruchith.research.idm.idp.IdentityManager');
var idm = new IdentityManager("/Users/ruchith/Documents/research/anon_idm/trunk/impl/java/config");

/*
 * GET home page.
 */

exports.index = function(req, res){
	db.getAllClaimDefs(function(val){
		res.render('index', { title: 'All Claims', entries : val  });
	});
  
};

exports.claims = function(req, res){
	db.getAllClaimDefs(function(val){
		res.send(val);
	});
  
};

exports.claim = function(req,res) {
	db.getClaimDetails(req.params.id, function(val) {
		res.render('claim', { claim : val  });
	});
};

exports.claimdef = function(req,res) {
	db.getClaimDetails(req.params.id, function(val) {
		res.send(val);
	});
};

exports.issue_claim = function(req, res) {
	idm.issueSerializedClaim(req.body.claim, req.body.user, req.body.anonId, function(err, result) {
		console.log(result);
		res.send(result);
	});
};


exports.claimdef_show = function(req,res) {
	res.render('claimdef');
};

exports.claimdef_process = function(req,res) {
	idm.generateNewClaimDefinition(req.body.name, req.body.description);
	res.redirect('/');
};


exports.useradd_show = function(req,res) {
	res.render('useradd');
};
exports.useradd_process = function(req,res) {
	idm.addUser(req.body.name, req.body.cert);
	res.redirect('/');	
};
exports.users = function(req,res) {
	db.getUserDetails(function(val) {
		res.render('users', { user_list : val  });
	});	
};


exports.admin_show = function(req, res) {
	res.render('admin');
}

exports.cert = function(req,res) {
	fs.readFile('/Users/ruchith/.idp/cert', 'utf8', function (err,data) {
		if (err) {
			return console.log(err);
		}
		res.send(data);
	});
};