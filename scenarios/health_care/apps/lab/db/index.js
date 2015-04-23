var mysql      = require('mysql');
var connection = mysql.createConnection({
	host     : 'localhost',
	user     : 'root',
	password : '',
	database : 'lab',
	multipleStatements: true
});
connection.connect();

var promise = require('promise');

/* get lab identifier and record */
exports.get_all_lab_records = function() {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT Id as id, PrescriptionId as pid, Record as record FROM LabRecord", 
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows);
            });
    });
}

/* get owner param, read param and record which has lab identifier */
exports.get_all_lab_record_pairs = function(id) {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT ParamOwner as param_owner, ParamRead as param_read, Record as record FROM LabRecord WHERE Id=" + id, 
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows);
            });
    });
}
