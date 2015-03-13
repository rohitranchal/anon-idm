var common = require('../java_common.js');
var request = require('request');

/* GET home page. */
exports.index = function(req, res){
  res.render('index', { title: "Doctor's app" });
};

exports.req_permission_page = function(req, res) {
    console.log('req_permission_page is called');
    res.render('req_permission_page');
};

exports.req_permission = function(req, res) {
    console.log('req_permission is called');

    request.post('http://localhost:3001/add_user', 
        {form: {key: 'value'}},
        function(error, response, body) {
            if(!error && response.statusCode == 200) {
                // Call java function to store the response or ... whatever preprocessing
                res.send('Success');
            }
            else {
                if(error) { res.send('error in req_permission'); }
                else { res.send('error due to different status code: ' + response.statusCode); }
            }
        }
    );
};
