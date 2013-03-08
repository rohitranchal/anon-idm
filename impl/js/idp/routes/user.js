var db = require('../db');

/*
 * GET users listing.
 */

exports.list = function(req, res){
  	db.getUserDetails(function(val) {
		res.send(val);
	});
};