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
        console.log("[authenticate success! ]");

        // Allocate session
        req.session.regenerate(function() {
            req.session.g = user_g;
            console.log("[[Session] g is inserted into session]: " + user_g);
            res.send(result_json.Anonymous);
        });
    }, function(error) {
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

exports.get_result = function(req, res) {
    if(!req.session.g) {
    }
    else {
        console.log("Hello: " + req.session.g);
        db.get_result_by_g(req.session.g)
        .then(function(result) {
            console.log(result);
            res.send(result.Record);
        },
        function(error) {
            console.log(error);
        });
    }
}

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

exports.operation = function(req, res) {
};
