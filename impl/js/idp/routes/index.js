var db = require('../db');

/*
 * GET home page.
 */

exports.index = function(req, res){
	db.getAllClaimDefs(function(val){
		res.render('index', { title: 'All Claims', entries : val  });		
	});
  
};