var db = require('../db');
fs = require('fs');

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

exports.claimDef = function(req,res) {
	db.getClaimDetails(req.params.id, function(val) {
		res.send(val);
	});
};

exports.cert = function(req,res) {
	fs.readFile('/Users/ruchith/.idp/cert', 'utf8', function (err,data) {
		if (err) {
			return console.log(err);
		}
		res.send(data);
	});
};