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
		res.send(val);
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

    console.log('name: ' + req.body.name);
    console.log('cert: ' + req.body.cert);
    console.log('type: ' + req.body.type);

    // TODO check whether the doctors are already registered or not
    // TODO if already registered, skip to the request_permission queue step
    db.get_one_user(req.body.name, function(value) {
        console.log("Hello length: " + value.length);
        if(value.length == 0) {
            common.idm.addUser(req.body.name, req.body.cert, function(err, val) {
                if(err) {
                    console.log("Error in add_user: " + err);
                    res.send("Error while adding user - processing error");
                }
                else {
                    console.log("Adding user done successfully");
                    res.send("Adding user success!");
                    add_request_permission_queue(req.body.record_id, req.body.name, parseInt(req.body.type), res);
                }
            });
        }
        else if (value.length >= 1) {
            console.log("This case");
            add_request_permission_queue(req.body.record_id, req.body.name, parseInt(req.body.type), res);
        }
    });

    console.log("done");
};

var add_request_permission_queue = function(r_id, name, type, response) {
    common.idm.addRequestPermissionQueue(r_id, name, type, function(err, val) {
        if(err) {
            console.log("Error in Adding request permission queue!");
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
        res.render('allow_permission_page', { title: 'User List', user_list : val });
    });
};

exports.allow_permission = function(req, res) {
    console.log('allow_permission is called');
    console.log('allow_permission value: ' + req.body.selection);

    var read_claim_name = req.body.selection + "_read";
    var owner_claim_name = req.body.selection + "_owner";
    var record_id = req.body.selection.split("_")[1];

    create_claimdef(owner_claim_name, "owner", req, res, false);
    create_claimdef(read_claim_name, "read", req, res, true, update_permission);

    common.idm.updateRecordPair(record_id, owner_claim_name, read_claim_name, function(err, val) {
        if(err) {
            console.log("error in updateRecordPair");
        }
        else {
            console.log("updateRecordPair success!");
        }
    });
};

var create_claimdef = function(name, desc, req, res, iscb, cb) {
    common.idm.generateNewClaimDefinition(name, desc, 
        function(err, val) {
            if(err) {
                console.log("error in create_claimdef");
                res.send("error in create_claimdef");
                throw err;
            }
            else {
                console.log("success in create_claimdef");
                res.send("success in create_claimdef");
                if(iscb) {
                    cb(req, res);
                }
            }
        });
};


var update_permission = function(req, res) {
    request.post('http://localhost:3002/update_permission',
        {form: {key: req.body.selection + "_read"}}, // TODO what to send?
        function(error, response, body) {
            if(error) {
                console.log("Error in allow_permission" + error);
                res.send("Error occur!");
            }
            else {
                var tmp_str = req.body.selection;
                tmp_str = tmp_str.split("_");
                var target_name = tmp_str[0];
                var target_record_id = tmp_str[1];

                console.log("Success in sending allow_permission");
                console.log("response from remote: " + body);
                update_register_state(target_record_id, target_name, res);
            }
        });
};

var update_register_state = function (record_id, name, response) {
    common.idm.updateRegisteration(record_id, name, true, 
        function (err, val) {
            if(err) {
                console.log("Error in updateRegisteration " + err);
                response.send("Error while updating registeration");
            }
            else {
                console.log("Update user success!");
                response.send("Success in sending allow_permission!");
            }
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
