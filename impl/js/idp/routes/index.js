var db = require('../db');

/*
 * GET home page.
 */

exports.index = function(req, res){
	db.getAllClaimDefs(function(val){
		res.render('index', { title: 'All Claims', entries : val  });
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

