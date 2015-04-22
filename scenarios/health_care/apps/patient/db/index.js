var mysql      = require('mysql');
var connection = mysql.createConnection({
	host     : 'localhost',
	user     : 'root',
	password : '',
	database : 'idp',
	multipleStatements: true
});
connection.connect();

// retrieve all the claim definitions
exports.get_all_claimdefs = function(cb) {
	connection.query('SELECT Name as name, Description as description, PublicParams as params, Digest as dgst, Sig as sig, Cert as cert, DateCreated FROM Claim_Definition', function(err, rows, fields) {
        if (err) throw err;
	    cb(rows);
    });
};

// retrieve specific claim definition
exports.get_claimdef_by_name = function(name, cb) {
	connection.query("SELECT Name as name, Description as description, PublicParams as params, Digest as dgst, Sig as sig, Cert as cert, DateCreated FROM Claim_Definition WHERE Name='" + name + "'", function(err, rows, fields) {
		if (err) throw err;
		cb(rows[0]);
	});

};

exports.get_one_user = function(name, cb) {
	connection.query("SELECT * FROM User WHERE name='" + name + "'", function(err, rows, fields) {
		if (err) throw err;
		cb(rows);
	});
}

// retrieve all user information
exports.get_all_users = function(cb) {
	connection.query("SELECT * FROM User", function(err, rows, fields) {
		if (err) throw err;
		cb(rows);
	});
};

exports.get_unregistered_requests = function(cb) {
    connection.query("SELECT * From RequestPermission where Registered=False", 
    function(err, rows, fields) {
        if(err) throw err;
        cb(rows);
    });
}

exports.get_req_src_url = function(name, id, cb) {
    connection.query("SELECT ReqSrcUrl From RequestPermission where name='" + name + "' and RecordId='" + id +"'",
    function(err, rows, fields) {
        if(err) throw err;
        cb(rows);
    });
}

exports.get_registered_users = function(cb) {
    connection.query("SELECT * From User JOIN User_state on User.name=User_state.name and User_state.Registered=True", 
    function(err, rows, fields) {
        if(err) throw err;
        cb(rows);
    });
}

exports.get_param_names_by_record_id = function (record_id, cb) {
    connection.query("SELECT OwnerName, ReadName FROM RecordPair WHERE RecordId='" + record_id + "'", function(err, rows, fields) {
        if(err) throw err;
        cb(rows);
    });
}
