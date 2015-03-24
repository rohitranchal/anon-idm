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

exports.get_all_lab_records = function() {
    return new Promise(function(resolve, reject) {
        connection.query("SELECT Id as id, Record as record FROM LabRecord", 
            function(error, rows, fields) {
                if(error) reject(error);
                else resolve(rows);
            });
    });
}
