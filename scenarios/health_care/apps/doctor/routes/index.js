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
    var cert_content = fs.readFileSync(common.keystore_dir + "/" + id + '.cert', 'utf8').trim();
    console.log("id: " + id);
    console.log("cert_content: " + cert_content);

    request.post('http://localhost:3001/add_user', 
        {form: {name: id, cert: cert_content, type: 1, record_id: req.body.record_id }},
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

/*
String idpUrl, String claimName, String user, String storePath,
			String storePass, String alias, String keyPass
*/

// TODO dynamically change
// claimName: claim from identity provider
// user: user name from the current program
// storePath: keystore's path
// storePass: keystore's password
// alias: alias used for private key in keystore
// keyPass: private key's password
exports.update_permission = function(req, res) {
    var idpUrl = 'http://localhost:3001';
    var claimName = req.body.key;
    var user = 'bob';
    var storePath = common.keystore_dir + "/bob.jks";
    var storePass = common.keystore_pass;
    var alias = 'bob';
    var keyPass = 'bobkey';
    console.log('update_permission is called');

    common.java.callStaticMethod('org.ruchith.research.idm.user.IDPTool2', 'reqClaim',
        idpUrl, claimName, user, storePath, storePass, alias, keyPass,
        function(err, val) {
            if(err) {
                console.log("Error in reqClaim(java): " + err);
                res.send("Error during processing(java)");
            }
            else {
                console.log("Success in reqClaim(java)");
                res.send("Sucess in reqClaim(java)");

            }
        }
    );
};
