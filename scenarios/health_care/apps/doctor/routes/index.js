var common = require('../common.js');
var request = require('request');
var fs = require('fs');
var tough = require('tough-cookie');

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


exports.authenticate_hie_page = function(req, res) {
    get_doctor_claimdef_list()
    .then(function(result) {
        console.log(result);
        def_list = JSON.parse(result);
        res.render('authenticate_hie_page', def_list);
    }, function(error) {
        console.log(error);
    })
};

var get_doctor_claimdef_list = function() {
    return new Promise(function(resolve, reject) {
        common.doctor.getClaimdefNameJson(function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

exports.authenticate_hie = function(req, res) {
    var claimdefname = req.body.selection;
    console.log("[Claim chosen  ]: " + claimdefname);

    var queue = [];
    queue.push(generate_request(claimdefname));
    queue.push(extract_g_from_claim_name(claimdefname));

    var cookie_id;

    common.promise.all(queue)
    .then(function(result) {
        var request_generated = result[0];
        var g_extracted = result[1];

        console.log("[Request generated     ]: " + request_generated);
        console.log("[g extracted           ]: " + g_extracted);
        return request_promise('post', 'http://localhost:3004/authenticate',
            {form: {request:request_generated, g:g_extracted}});
    })
    .then(function(result) {
        var server_response = result.body;
        console.log("[Value returned from authenticate  ]: " + server_response);

        var set_cookie_header = result.response.headers['set-cookie'][0];
        cookie_id = set_cookie_header.split(";")[0]
        console.log("[Cookie to be stored in session    ]: " + cookie_id);
        return extract_session_key(claimdefname, server_response);
    })
    .then(function (result) {
        var session_key_extracted = result;
        console.log("[SessionKey extracted: ]" + result);
        console.log("in plain...: " + new Buffer(result, 'base64').toString('ascii'));
        console.log("length: " + new Buffer(result, 'base64').toString('ascii').length);
        // TODO confirming mechanism

        req.session.regenerate(function() {
            req.session.hie_cookie = cookie_id;
            req.session.current_def = claimdefname;
            console.log("[[Session] cookie for hie acess is inserted into session]: " + cookie_id);
            // TODO when to?
            res.send("complete!");
        });

    }, function(err) {
        console.log(err);
    });
};

var extract_g_from_claim_name = function(name) {
    return new Promise(function(resolve, reject) {
        common.doctor.extractG(name, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

var generate_request = function(claimdefname) {
    return new Promise(function(resolve, reject) {
        common.doctor.generateRequest(claimdefname, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

var extract_session_key = function(claimdefname, server_response) {
    return new Promise(function(resolve, reject) {
        common.doctor.extractSessionKey(claimdefname, server_response, 
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
};

exports.logout = function(req, res) {
    var hie_cookie = req.session.hie_cookie;
    if(hie_cookie == undefined) {
        console.log("cookie is already removed");
        res.send("Already logged out!");
    }
    else {
        console.log("[logout with the cookie    ]: " + req.session.hie_cookie);
        request_promise('get', 'http://localhost:3004/logout', null, req.session.hie_cookie)
        .then(function(result) {
            console.log("[Response from HIE ]: " + result.body);
            // TODO a little bit more case..
            req.session.destroy(function() {
                console.log("[[Session] session is destroyed!]");
                console.log("[logout success!   ]");
                res.send("Success in logout");
            });
        }, function(error) {
        console.log("[logout error      ]: " + error);
        });
    }
}

exports.get_data = function(req, res) {
    var hie_cookie = req.session.hie_cookie;
    if(hie_cookie == undefined) {
        res.send("Not logged in");
    }
    else {
        console.log("Halo?");
        request_promise('get', 'http://localhost:3004/get_result', null, req.session.hie_cookie)
        .then(function(result) {
            res.send("RESULT: " + result.body);
        }, function(error) {
            console.log("error " + error);
        });
    }
}

/* request in promise format */
var request_promise = function(method, target, form, cookie_input) {
    var temp_request = require('request');

    return new Promise(function(resolve, reject) {
        if(method.toLowerCase() == 'post') {
            temp_request.post(target, form, function(error, response, body) {
                if(error) reject(error);
                else { 
                    console.log(response.headers['set-cookie'][0]);
                    resolve({ "response": response, "body": body });
                }
            });
        }
        // When get is called, 'val' is not used
        else if(method.toLowerCase() == 'get') {
            if(cookie_input == undefined) {
                temp_request.get(target, function(error, response, body) {
                    if(error) reject(error); 
                    else resolve({ "response": response, "body": body });
                });
            }
            else {
                temp_request.get({url: target, headers: {cookie: cookie_input}}, 
                    function(error, response, body) {
                        if(error) reject(error); 
                        else resolve({ "response": response, "body": body });
                    });
            }
       }
       else {
           reject("Incorrect method!");
       }
    });
};
