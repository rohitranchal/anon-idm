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
    var self_url = 'http://' + common.self_domain + ":" + req.app.this_http_port;
    console.log("self_url: " + self_url);
    res.render('req_permission_page', { title: "Request permission", this_url: self_url });
};

// TODO more details
// Assumptions for changing users
// 00. certificate filename is specified in "common.js"
// 01. certificate is located in the keystore folder
// 02. The format for certificate name is "id.cert"
exports.req_permission = function(req, res) {

    var id = common.certname.split(".")[0];
    var cert_content = fs.readFileSync(common.keystore_dir + "/" + common.certname, 'utf8').trim();
    console.log("id: " + id);
    console.log("cert_content: " + cert_content);

    var user_idp_url = req.body.user_address;

    request.post(user_idp_url + '/add_user', 
        {form: {name: id, cert: cert_content, type: 1, record_id: req.body.record_id, req_src_address: req.body.req_src_address }},
        function(error, response, body) {
            if(error) {
                res.send('error in req_permission');
            }
            else if (response.statusCode != 200) {
                res.send('error due to different status code: ' + response.statusCode);
            }
            else { // When status code is 200
                // TODO error handling
                console.log("Halo");
                res.render('response_result', 
                    { 
                        redirect_url: 'http://' + common.self_domain + ":" + req.app.this_http_port, 
                        result_msg : body 
                    });
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
    var idpUrl = req.body.idp_url;
    console.log("TEST IDP: " +  idpUrl);
    var claimName = req.body.key;
    console.log("claimname: " + claimName);
    var user = common.id;
    var storePath = common.keystore_dir + "/" + common.keystore_file;
    var storePass = common.keystore_pass;
    var alias = common.alias;
    var keyPass = common.privatekey_pass;
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

    // declaration of the variables used in the promise chains
    var g_extracted, cookie_id, session_key_extracted; 

    common.promise.all(queue)
    .then(function(result) {
        var request_generated = result[0];
        g_extracted = result[1];

        console.log("[Request generated     ]: " + request_generated);
        console.log("[g extracted           ]: " + g_extracted);
        return request_promise('post', 'http://localhost:3004/authenticate',
            {form: {request:request_generated, g:g_extracted}});
    })
    .then(function(result) {
        var server_response = result.body;
        console.log("[Value returned from authenticate  ]: " + server_response);

        // TODO: change it so that it can work more in more general way
        // Currently, cookie is stored as a first entry in set-cookie
        // What needs to store is the 'sid' value
        var set_cookie_header = result.response.headers['set-cookie'][0];
        cookie_id = set_cookie_header.split(";")[0]
        console.log("[Cookie to be stored in session    ]: " + cookie_id);

        return extract_session_key(claimdefname, server_response);
    })
    .then(function (result) {
        session_key_extracted = result;
        console.log("[SessionKey extracted: ]" + result);
        console.log("in plain...: " + new Buffer(result, 'base64').toString('ascii'));
        console.log("length: " + new Buffer(result, 'base64').toString('ascii').length);

        // Confirming mechanism
        var storePath = common.keystore_dir + "/" + common.keystore_file;
        var storePass = common.keystore_pass;
        var alias = common.alias;
        var keyPass = common.privatekey_pass;

        var queue_for_hash = [];
        queue_for_hash.push(create_session_dgst(session_key_extracted));
        queue_for_hash.push(create_session_sig(session_key_extracted, 
            storePath, storePass, alias, keyPass));

        return common.promise.all(queue_for_hash);
    })
    .then(function (result_array) {
        var created_dgst = result_array[0];
        var created_sig = result_array[1];
        console.log("dgst: " + created_dgst);
        console.log("sig: " + created_sig);

        var doctor_cert = fs.readFileSync(common.keystore_dir + "/" + common.certname, 'utf8').trim();

        // request for validity of hash
        // if correct, session generation
        // otherwise, don't create session management

        return request_promise('post', 'http://localhost:3004/check_hash_validity',
            {form: {g:g_extracted, dgst: created_dgst, sig: created_sig, cert: doctor_cert}},
            cookie_id);
    })
    .then(function (result) {
        // When failed?
        //console.log(result);
        //console.log(result.response);
        //console.log(result.body);


        // TODO when failed


        req.session.regenerate(function() {
            req.session.hie_cookie = cookie_id;
            req.session.current_def = claimdefname;
            req.session.key = session_key_extracted;
            console.log("[[Session] cookie for hie acess is inserted into session]: " + cookie_id);
            // TODO when to?
            res.render("response_result", 
                { 
                    redirect_url: 'http://' + common.self_domain + ":" + req.app.this_http_port , 
                    result_msg: result.body
                });
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

var create_session_dgst = function(content) {
    return new Promise(function(resolve, reject) {
        common.java.callStaticMethod('org.ruchith.research.scenarios.healthcare.Util', 'createB64Dgst', content,
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
};

var create_session_sig = function(content, storePath, storePass, alias, keyPass) {
    return new Promise(function(resolve, reject) {
        common.doctor.sessionSig(content, storePath, storePass, alias, keyPass,
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
}

exports.logout = function(req, res) {
    var hie_cookie = req.session.hie_cookie;
    if(hie_cookie == undefined) {
        console.log("cookie is already removed");
        res.render("response_result", 
            { 
                redirect_url: 'http://' + common.self_domain + ":" + req.app.this_http_port, 
                result_msg: "Already logged out"
            });
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
                res.render("response_result", 
                    { 
                        redirect_url: 'http://' + common.self_domain + ":" + req.app.this_http_port, 
                        result_msg: "Success in logout"
                    });
            });
        }, function(error) {
        console.log("[logout error      ]: " + error);
        });
    }
}

exports.get_data = function(req, res) {
    var hie_cookie = req.session.hie_cookie;
    var curr_key = req.session.key;
    var encrypted_data, decrypted_data;

    if(hie_cookie == undefined) {
        res.send("Not logged in");
    }
    else {
        console.log("Halo?");
        request_promise('get', 'http://localhost:3004/get_result', null, req.session.hie_cookie)
        .then(function(result) {
            encrypted_data = result.body;
            console.log("Encrypted: " + encrypted_data);
            return decrypted_result(encrypted_data, curr_key);
        })
        .then(function(result) {
            // Decode base64 encoded string
            decrypted_data = new Buffer(result, 'base64').toString();
            res.render("get_data", { title: "Result", encrypted_result: encrypted_data, decrypted_result: decrypted_data });
        }, function(error) {
            console.log("error " + error);
        });
    }
}

var decrypted_result = function(ciphertext, sessionkey) {
    return new Promise(function(resolve, reject) {
        common.java.callStaticMethod('org.ruchith.research.scenarios.healthcare.Util', 'decrypt', ciphertext, sessionkey,
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
};

/* request in promise format */
var request_promise = function(method, target, form, cookie_input) {
    var temp_request = require('request');

    return new Promise(function(resolve, reject) {
        if(method.toLowerCase() == 'post') {
            if(cookie_input == undefined) {
                console.log("Cookie input is not defined");
                temp_request.post(target, form, function(error, response, body) {
                    // response
                    if(error) reject(error);
                    else { 
                        console.log("Testing....");
                        if(response.headers['set-cookie'] === undefined) {
                            console.log("cookie is not defined in this request!");
                        }
                        else {
                            console.log(response.headers['cet-cookie']);
                            console.log(response.headers['set-cookie'][0]);
                        }
                        resolve({ "response": response, "body": body });
                    }
                });
            }
            else {
                var temp_form = form.form;
                console.log("TEST here: " + temp_form);
                temp_request.post({url: target, headers: {cookie: cookie_input}, form: temp_form }, 
                    function(error, response, body) {
                        // response
                        if(error) reject(error);
                        else { 
                            if(response.headers['set-cookie'] === undefined) {
                                console.log("cookie is not defined in this request!");
                            }
                            else {
                                console.log(response.headers['cet-cookie']);
                                console.log(response.headers['set-cookie'][0]);
                            }
                            resolve({ "response": response, "body": body });
                        }
                });
            }
        }
        // When get is called, 'val' is not used
        else if(method.toLowerCase() == 'get') {
            if(cookie_input == undefined) {
                temp_request.get(target, function(error, response, body) {
                    // response
                    if(error) reject(error); 
                    else resolve({ "response": response, "body": body });
                });
            }
            else {
                temp_request.get({url: target, headers: {cookie: cookie_input}}, 
                    function(error, response, body) {
                        // response
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
