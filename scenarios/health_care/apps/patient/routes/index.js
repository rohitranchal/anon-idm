var common = require('../common.js');
var db = require('../db');
var request = require('request');
var fs = require('fs');


//TODO list user button
//TODO add user button
//TODO allow permission page

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
        }
    );
};

exports.list_claimdef = function(req, res) {
    console.log("list_claimdef is called");
	db.getAllClaimDefs(function(val){
        console.log(val);
		res.send(val);
	});
};

// Adding user to database
exports.add_user = function(req, res) {
    console.log('add_user is called');

    // if undefined set to ""
    // Otherwise leave as it is
    // TODO: remove this adhoc code
    if (req.body.name === undefined) { req.body.name = "";  }
    if (req.body.cert === undefined) { req.body.cert="";    }

    console.log('name: ' + req.body.name);
    console.log('cert: ' + req.body.cert);

    common.idm.addUser(req.body.name, req.body.cert, function(err, val) {
        if(err) {
            console.log("Error in add_user: " + err);
            res.send("Error while adding user - processing error");
        }
        else {
            console.log("Adding user done successfully");
            res.send("Adding user success!");
        }
    });
    console.log("done");
};

// Simply list all the information consumer currently allowed
exports.list_user = function(req, res) {
    console.log('list_user is called');
    db.get_user_details(function(val) {
        res.render('list_user', { title: 'User List', user_list : val });
    });
};

exports.allow_permission_page = function(req, res) {
    console.log('allow_permission_page is called');
    //res.send('allow_permission_page is under construnction');
    db.get_user_details(function(val) {
        res.render('allow_permission_page', { title: 'User List', user_list : val });
    });
};

exports.allow_permission = function(req, res) {
    console.log('allow_permission is called');
    console.log('allow_permission value: ' + req.body.selection);
    // With this value, the target url needs to be updated
    // TODO what to do?
    request.post('http://localhost:3002/update_permission',
        {form: {key: req.body.selection}}, // TODO what to send?
        function(error, response, body) {
            if(error) {
                console.log("Error in allow_permission" + error);
                res.send("Error occur!");
            }
            else {
                console.log("Success in sending allow_permission");
                console.log("response from remote: " + body);
                // TODO response with previous call
                // TODO what to print out for response 

                // TODO delete user list from the databse for the registered one
                res.send("Success in sending allow_permission!");
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
	db.getAllClaimDefs(function(val){
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
		        console.log("success in issue_claim: " + result);
		        res.send(result);
            }
	    }
    );
};

exports.test = function(req, res) {
    res.send('Hello');
};
