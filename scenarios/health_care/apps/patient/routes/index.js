var common = require('../common.js');
var db = require('../db');
var request = require('request');
var fs = require('fs');

/* GET home page. */
exports.index = function(req, res){
    console.log('index is called');
    res.render('index', { title: 'Health Record Manager' });
};

exports.add_claimdef_page = function(req, res) {
    console.log('add_claimdef_page is called');
    res.render('add_claimdef_page');
};

exports.add_claimdef = function(req, res) {
    console.log("add_claimdef is called");
	common.idm.generateNewClaimDefinition(req.body.name, req.body.description, 
        function(err, val) {
            if(err) {
                console.log("error in add_claimdef_page");
                res.send("error in add_claimdef_page");
            }
            else {
                console.log("success in add_claimdef_page");
                res.send("success in add_claimdef_page");
            }
        });
};

exports.list_claimdef = function(req, res) {
    console.log("list_claimdef is called");
	db.get_all_claimdefs(function(val){
        console.log(val);
        res.render("list_claimdef", { title: "List claim definitions", claimdef_list: val });
	});
};

// TODO change the name: such as add_request
// Adding user to database
exports.add_user = function(req, res) {
    console.log('add_user is called');

    // if undefined set to ""
    // Otherwise leave as it is
    // TODO: remove this adhoc code
    if (req.body.name === undefined) { req.body.name = "";  }
    if (req.body.cert === undefined) { req.body.cert = "";  }
    if (req.body.type === undefined) { req.body.type = "";  }
    if (req.body.req_src_address === undefined) {req.body.req_src_address = "" ; }

    console.log('name: ' + req.body.name);
    console.log('cert: ' + req.body.cert);
    console.log('type: ' + req.body.type);
    console.log('req_src_address: ' + req.body.req_src_address);

    db.get_one_user(req.body.name, function(value) {
        console.log("The number of rows returned with name(" + req.body.name + "): " + value.length);
        // Doctor is not registered yet
        if(value.length == 0) {
            console.log("New doctor");
            common.idm.addUser(req.body.name, req.body.cert, function(err, val) {
                if(err) {
                    console.log("Error in add_user: " + err);
                    res.send("Error while adding user - processing error");
                }
                else {
                    console.log("Adding user done successfully");
                    res.send("Adding user success!");
                    add_request_permission_queue(req.body.record_id, req.body.name, parseInt(req.body.type), req.body.req_src_address, res);
                }
            });
        }
        // Doctor is already registered
        else if (value.length >= 1) {
            console.log("Registered doctor");
            add_request_permission_queue(req.body.record_id, req.body.name, parseInt(req.body.type), req.body.req_src_address, res);
        }
    });

    console.log("done");
};

var add_request_permission_queue = function(r_id, name, type, src_url, response) {
    common.idm.addRequestPermissionQueue(r_id, name, type, src_url, function(err, val) {
        if(err) {
            console.log("Error in Adding request permission queue!: " + err);
            response.send("Error while adding request permission queue!");
        } 
        else {
            console.log("Adding reqeust Permission queue is done");
            response.send("Adding reqeust Permission queue is done");
        }
    });
}

// Simply list all the information consumer currently allowed
exports.list_user = function(req, res) {
    console.log('list_user is called');
    db.get_all_users (function(val) {
        res.render('list_user', { title: 'User List', user_list : val });
    });
};

exports.allow_permission_page = function(req, res) {
    console.log('allow_permission_page is called');
    db.get_unregistered_requests (function(val) {
        res.render('allow_permission_page', { title: 'Allow Permission', user_list : val });
    });
};

exports.allow_permission = function(req, res) {
    console.log('allow_permission is called');
    console.log('allow_permission value: ' + req.body.selection);

    var name_and_record_id = req.body.selection.split("_");

    var name = name_and_record_id[0];
    var record_id = name_and_record_id[1];

    var read_claim_name = record_id + "_read";
    var owner_claim_name = record_id + "_owner";

    var queue = [];
    var always = Promise.resolve(true);
    queue.push(always);

    db.get_param_names_by_record_id(record_id, function(row) {
        // TODO if registered in PermissionQueue is 1, abort
        if(row.length == 0) {
            queue.push(create_claimdef(owner_claim_name, "owner"));
            queue.push(create_claimdef(read_claim_name, "read"));
            queue.push(update_record_pair(record_id, owner_claim_name, read_claim_name));
        }

        common.promise.all(queue)
        .then(function(result_array) {
            // find src url
            return find_src_url(name, record_id);
        })
        .then(function(target_url) {
            // update doctor side
            console.log("Found target url: " + target_url);
            return promise_request('post', target_url + '/update_permission',
                    {form: {
                        key: record_id + "_read", 
                        idp_url: 'http://' + common.self_domain + ":" + req.app.this_http_port
                    }});
        })
        .then(function(result) {
            console.log("Response from doctor: " + result.body);
            return update_register_state(record_id, name);
        })
        .then(function(result) {
            res.render('response_result', 
                { redirect_url: 'http://' + common.self_domain + ":" + req.app.this_http_port, 
                  result_msg: "Success!" 
                });
        }, function(err) {
            res.render('response_result', 
                { redirect_url: 'http://' + common.self_domain + ":" + req.app.this_http_port, 
                  result_msg: err 
                });
        });
    });

};

var find_claim_by_name = function(name) {
    return new Promise(function(resolve, reject) {
        try {
            db.get_claimdef_by_name(name), 
                function(row) {
                    resolve(row);
                }
        }
        catch(err) {
            reject(err);
        }
    });
}


var create_claimdef = function(name, desc) {
    return new Promise(function(resolve, reject) {
            common.idm.generateNewClaimDefinition(name, desc, function(err, res) {
                if(err) reject(err);
                else resolve(res);
            })
        });
};

var find_src_url = function(user_name, presc_id) {
    return new Promise(function(resolve, reject) {
        try {
            db.get_req_src_url(user_name, presc_id,
                function(row) {
                    var target_domain = row[0].ReqSrcUrl;
                    resolve(target_domain);
                });
        }
        catch(err) {
            reject(err);
        }
    });
};

/* request in promise format */
var promise_request = function(method, target, val) {
    return new Promise(function(resolve, reject) {
       if(method.toLowerCase() == 'post') {
            require('request').post(target, val, function(error, response, body) {
                if(error) reject(error);
                else resolve({ "response": response, "body": body });
            });
       }
       // When get is called, 'val' is not used
       else if(method.toLowerCase() == 'get') {
           require('request').get(target, function(error, response, body) {
               if(error) reject(error); 
               else resolve({ "response": response, "body": body });
           });
       }
       else {
           reject("Incorrect method!");
       }
    });
};

var update_register_state = function(record_id, name) {
    return new Promise(function(resolve, reject) {
        common.idm.updateRegisteration(record_id, name, true, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

var update_record_pair = function(record_id, owner_claim_name, read_claim_name) {
    return new Promise(function(resolve, reject) {
        common.idm.updateRecordPair(record_id, owner_claim_name, read_claim_name, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

exports.revocate_user_page = function(req, res) {
    console.log('revocate_user_page is called');
    db.get_registered_requests (function(val) {
        res.render('revocate_user_page', { title: 'Revocate User', user_list : val });
    });
};

exports.revocate_user = function(req, res) {
    console.log('revocate_user is called');
    console.log('revocate_user value: ' + req.body.selection);

    var name_and_record_id = req.body.selection.split("_");

    var name = name_and_record_id[0];
    var record_id = name_and_record_id[1];

    var read_claim_name = record_id + "_read";

    var prev_param, new_param;
    
    db.get_claimdef_by_name(read_claim_name, function(val) {
        new_param = val.params;
        retrieve_revocate_info(val.name, val.params)
        .then(function(val) {
        }, function(err) {
            console.log("errors:" + err);
        });
    });
};


var retrieve_revocate_info = function(name, params) {
    return new Promise(function(resolve, reject) {
        common.idm.retrieveRevocateInfo(name, params, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};


// TODO another improvement?
exports.cert = function(req, res) {
    console.log("cert is called");
	fs.readFile(common.config_dir + '/cert', 'utf8', function (err,data) {
		if (err) {
            console.log("Error in reading file");
            res.send("Error in reading file");
            return;
		}
        else {
            console.log("cert succeed!");
		    res.send(data);
        }
	});
};

// TODO another improvement?
exports.claims = function(req, res) {
    console.log("claims is called");
	db.get_all_claimdefs(function(val){
        console.log("result of java execution:");
        console.log(val);
		res.send(val);
	});
};

exports.issue_claim = function(req, res) {
    console.log("issue_claim is called");
    common.idm.issueSerializedClaim(req.body.claim, req.body.user, req.body.anonId,
        function(err, result) {
            if(err) {
                console.log("error in issue_claim: " + err);
                res.send(err);
            }
            else {
		        console.log("success in issue_claim: \n" + result);
		        res.send(result);
            }
	    });
};

exports.claimdef = function(req, res) {
    db.get_claimdef_by_name(req.params.id, function(val) {
        res.send(val);
    });
};

exports.param_names = function(req, res) {
    db.get_param_names_by_record_id(req.params.id, function(val) {
        res.send(val);
    });
};

exports.test = function(req, res) {
    console.log(req);
    res.redirect('/claimdef/' + req.query.id);
};
