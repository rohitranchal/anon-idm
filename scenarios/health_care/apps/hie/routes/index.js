var common = require('../common.js');
var db = require('../db');

/* GET home page. */
exports.index = function(req, res){
    res.render('index', { title: 'Health Information Exchange(hie)' });
};

/* POST insert item into hie db */
exports.update_hie_record = function(req, res) {
    var param_owner = req.body.param_owner;
    var param_read = req.body.param_read
    var record = req.body.record;
    console.log("[received param owner  ]: " + param_owner);
    console.log("[received param read   ]: " + param_read);
    console.log("[received param record ]: " + record); 

    // 1. Extract g
    extract_g(param_read)
    .then(function(result) {
        console.log("[extracted g param from param_read ]: " + result);
        // 2. Store record into db
        return store_record(result, param_owner, param_read, record);
    })
    .then(function(result) {
        console.log("[update_hie_record success!]");
        res.send("Success in updating HIE database");
    }, function(error) {
        console.log("[update_hie_record error   ]: " + err);
        res.send("Error in updating HIE database");
    });
    
};

var extract_g = function(param) {
    return new Promise(function(resolve, reject) {
        common.hie.extractG(param, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

var store_record = function(g, owner, read, record) {
    return new Promise(function(resolve, reject) {
        common.hie.storeRecord(g, owner, read, record, 
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
};

/* GET get all records stored in hie */
exports.list_hie_record = function(req, res) {
    // TODO list of dbs
    res.send("temp response from list_hie_record");
};

exports.get_read_param = function(req, res) {
    console.log("get_read_param is called");

    var user_g = decodeURIComponent(req.query.g);
    console.log("g is received: " + user_g);
    db.get_read_param_by_g(user_g)
    .then(function(result) {
        console.log(result);
        res.send(result.ParamRead);
    }, function(error) {
        res.send('Error:' + error);
    });
};

exports.get_update_info = function(req, res) {
    console.log("get_update_info is called");

    var user_g = decodeURIComponent(req.query.g);
    db.get_latest_rekey_by_g(user_g)
    .then(function(result) {
        console.log(result);
        res.send(result.ReKeyInfo);
    },
    function(err) {
        console.log(err);
    });
    // then update read params 
    // It has separate datastructure for ReKey
};

// authenticate for the record corresponds to param g
exports.authenticate = function(req, res) {
    var user_req = req.body.request;
    var user_g = req.body.g;
    console.log("[TEST req                  ]");
    console.log(req.headers);
    console.log("[Request passed from user  ]: " + user_req);
    console.log("[g passed from user        ]: " + user_g);

    // 1. Retrieve paramter g
    db.get_read_param_by_g(user_g)
    .then(function(result) {
        var target_param = result.ParamRead;
        console.log("[readParam for g   ]: " + target_param);
        // 2. Create challenge based on read's public paramter
        return create_challange(user_req, target_param);
    })
    .then(function(result) {
        var result_json = JSON.parse(result);
        console.log("[SessionKey    ]: " + result_json.SessionKey);
        console.log("[Anonoymous    ]: " + result_json.Anonymous);

        // Allocate session
        req.session.regenerate(function() {
            req.session.g = user_g;
            req.session.key = result_json.SessionKey;
            req.session.user_req = user_req;
            console.log("[[Session] g is inserted into session]: " + user_g);
            console.log("Session key used: " + result_json.SessionKey);
            console.log("Anonymous: " + result_json.Anonymous);
            console.log("Req used: " + user_req);
            res.send(result_json.Anonymous);
        });
    }, function(error) {
        // if already session is created, destroy it
        if(req.session.g != undefined) {
            req.session.destroy(function() {
                console.log("[Session needs to be destroyed!]");
            });
        }
        console.log("[authenticate error    ]: " + error);
        res.send("Error in authenticate");
    });
};

var create_challange = function(request, param) {
    return new Promise(function(resolve, reject) {
        common.hie.createChallangeByParam(request, param, 
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
}

exports.check_hash_validity = function(req, res) {
    var user_g = req.body.g;
    var recv_dgst = req.body.dgst;
    var recv_sig = req.body.sig;
    var recv_cert = req.body.cert;
    
    // confirming mechanism
    console.log("[authenticate success! ]");
    console.log("user g: " + user_g);
    console.log("dgst: " + recv_dgst);
    console.log("sig: " + recv_sig);
    console.log("cert: " + recv_cert);

    var curr_key = req.session.key;
    console.log("curret Session key: " + curr_key);
    console.log(req.session.key);

    verify_session_dgst(curr_key, recv_dgst)
    .then(function(is_valid_dgst) {
        console.log("is valid dgst: " + is_valid_dgst);
        if(!is_valid_dgst) {
            // destory session
            req.session.destroy(function() {
                console.log("Session destroyed: invalid dgst!");
            });
            return Promise.reject("Failed in verifying dgst");
        }
        else {
           return verify_session_sig(curr_key, recv_cert, recv_sig);
        }
    })
    .then(function(is_valid_sig) {
        console.log("is valid sig: " + is_valid_sig);
        if(!is_valid_sig) {
            req.session.destroy(function() {
                console.log("Session destroyed: invalid sig!");
            });
            return Promise.reject("Failed in verifying sig");
        }
        else {
            res.send("Authenticated");
        }
    }, function(err) {
        console.log("Hello");
        res.send(err);
    });


};


var verify_session_dgst = function(content, recvDgst) {
    return new Promise(function(resolve, reject) {
        common.java.callStaticMethod('org.ruchith.research.scenarios.healthcare.Util', 'verifyB64Dgst', content, recvDgst,
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
};

var verify_session_sig = function(content, recvCert, recvSig) {
    return new Promise(function(resolve, reject) {
        common.hie.verifySig(content, recvCert, recvSig, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};


exports.get_result = function(req, res) {
    if(!req.session.g) {
    }
    else {
        console.log("Hello: " + req.session.g);
        db.get_result_by_g(req.session.g)
        .then(function(result) {
            console.log(result);
            return create_encrypted_result(result.Record, req.session.key);
        })
        .then(function(result) {

            console.log("Encrypted: " + result);
            var req_and_data = {encrypted_data: result, req: req.session.user_req };
            console.log(req_and_data);
            res.send(req_and_data);
        },
        function(error) {
            console.log(error);
        });
    }
}

var create_encrypted_result = function(plaintext, sessionkey) {
    return new Promise(function(resolve, reject) {
        common.java.callStaticMethod('org.ruchith.research.scenarios.healthcare.Util', 'encrypt', plaintext, sessionkey,
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
};


// TODO cookie handling
exports.logout = function(req, res) {
    if(!req.session.g) {
        console.log("Already logged out!");
        res.send("Session expired");
    }
    else {
        console.log("[TEST req                  ]");
        console.log(req.session);
        console.log(req.headers);
        console.log("[[Session] session is destroyed!]: " + req.session.g);
        req.session.destroy(function() {
            res.send("Success in logout");
        });
    }
};

exports.update_read_info = function(req, res) {
    console.log("update_read_info is called");

    var update_info_encoded = req.body.update_info;
    console.log("update info received: " + update_info_encoded);

    var update_info_decoded = new Buffer(update_info_encoded, 'base64').toString();
    var update_info_json = JSON.parse(update_info_decoded);
    var params = update_info_json.params;
    var params_encoded = new Buffer(JSON.stringify(params)).toString('base64');
    var g_param = params.g;

    /*
    console.log("update_info_json:");
    console.log(update_info_json);
    console.log("params:");
    console.log(params);
    console.log("params_encoded:");
    console.log(params_encoded);
    */

    update_public_param(g_param, params_encoded)
    .then(function(result) {
        console.log("update_public_param is done");
        return insert_update_info(g_param, update_info_encoded);
    })
    .then(function(result) {
        console.log("insert_update_info is done");
        res.send("Success");
    }, function(err) {
        console.log(err);
        res.send("Fail: " + err);
    });
};

var update_public_param = function(g_param, params_encoded) {
    return new Promise(function(resolve, reject) {
        common.hie.updatePublicParam(g_param, params_encoded, 
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
}; 

var insert_update_info = function(g_param, update_info_encoded) {
    return new Promise(function(resolve, reject) {
        common.hie.insertUpdateInfo(g_param, update_info_encoded, 
            function(err, res) {
                if(err) reject(err);
                else resolve(res);
            });
    });
};

exports.operation = function(req, res) {
};
