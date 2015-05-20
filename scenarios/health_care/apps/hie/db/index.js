var mysql      = require('mysql');
var connection = mysql.createConnection({
	host     : 'localhost',
	user     : 'root',
	password : '',
	database : 'hie',
	multipleStatements: true
});
connection.connect();

var promise = require('promise');

/*
    GParam VARCHAR(512) NOT NULL PRIMARY KEY,   # hash g in the parameter
    ParamOwner TEXT NOT NULL,           # ParamOwner
    ParamRead TEXT NOT NULL,            # ParamRead
    Record TEXT NOT NULL                # Medical Result
*/

exports.get_read_param_by_g = function(g_param) {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT ParamRead FROM HieRecord WHERE GParam='" + g_param + "'", 
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows[0]);
            });
    });
};

exports.get_result_by_g = function(g_param) {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT Record FROM HieRecord WHERE GParam='" + g_param + "'",
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows[0]);
            });
    });
};

exports.get_all_col_by_g = function(g_param) {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT * FROM HieRecord WHERE GParam='" + g_param + "'",
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows);
            });
    });
};

exports.get_latest_rekey_by_g = function(g_param) {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT ReKeyInfo FROM ReadParamReKey WHERE GParam='" + g_param + "'" + 
            " ORDER BY id DESC",
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows[0]);
            });
    });
};


/*
exports.get_all_lab_records = function() {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT Id as id, Record as record FROM LabRecord", 
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows);
            });
    });
}
*/

/* get owner param, read param and record which has lab identifier */
/*
exports.get_all_lab_record_pairs = function(id) {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT ParamOwner as param_owner, ParamRead as param_read, Record as record FROM LabRecord WHERE Id=" + id, 
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows);
            });
    });
}
*/
