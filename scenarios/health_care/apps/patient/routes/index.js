var common = require('../common.js');
var db = require('../db');

/* GET home page. */
exports.index = function(req, res){
    console.log('index is called');
    res.render('index', { title: 'Health Record Manager' });
};

// Simply list all the information consumer currently allowed
exports.list_user = function(req, res) {
    console.log('list_user is called');
    db.get_user_details(function(val) {
        res.render('list_user', { title: 'User List', user_list : val });
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

exports.test = function(req, res) {
    res.send('Hello');
};
