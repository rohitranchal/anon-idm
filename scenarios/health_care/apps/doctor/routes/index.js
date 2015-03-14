var common = require('../common.js');
var request = require('request');
var fs = require('fs');

/* GET home page. */
exports.index = function(req, res){
    console.log('index is called');
    res.render('index', { title: "Doctor's app" });
};

exports.req_permission_page = function(req, res) {
    console.log('req_permission_page is called');
    res.render('req_permission_page');
};

// TODO dynamically change the user
exports.req_permission = function(req, res) {
    console.log('req_permission is called');

    // TODO dynamically read the certificate
    // read from wallet
    var id = 'bob';
    var cert_content = fs.readFileSync(common.wallet_dir + id + '.cert', 'utf8').trim();
    console.log("id: " + id);
    console.log("cert_content: " + cert_content);

    request.post('http://localhost:3001/add_user', 
        {form: {name: id, cert: cert_content}},
        function(error, response, body) {
            if(error) {
                res.send('error in req_permission');
            }
            else if (response.statusCode != 200) {
                res.send('error due to different status code: ' + response.statusCode);
            }
            else { // When status code is 200
                // TODO error handling
                res.send(body);
            }
        }
    );
};
