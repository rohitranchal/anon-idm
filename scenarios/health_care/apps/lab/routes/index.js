var common = require('../common.js');
var db = require('../db');

/* request in promise format */
var request = function(method, target) {
    return new Promise(function(resolve, reject) {
       if(method.toLowerCase() == 'post') {
            require('request').post(target, function(error, response, body) {
                if(error) reject(error);
                else resolve({ "response": response, "body": body });
            });
       }
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

/* GET: home page. */
exports.index = function(req, res){
    res.render('index', { title: 'Lab' });
};

/* GET: get register_record_page */
exports.register_record_page = function(req, res) {
    console.log('register_record_page is called');
    res.render('register_record_page');
}

exports.register_record = function(req, res) {
    console.log('register_record is called');
    console.log('record id: ' + req.body.record_id); 

    // TODO SELF 
    request('get', 'http://localhost:3003/get_parameters/' + req.body.record_id)
    .then(function(result) {
        var params = JSON.parse(result.body);
        console.log("Owner: " + params.owner);
        console.log("Read: " + params.read);
        return insert_new_empty_record(params.owner, params.read);
    })
    .then(function(result) {
        console.log("success in register_record!");
        respond("updated!");
    }, function(error) {
        console.log("Error in register_record: " + error);
    });

}

var insert_new_empty_record = function(own, read) {
    return new Promise(function(resolve, reject) {
        common.lab.initRecord(own, read, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

exports.update_record_page = function(req, res) {
    db.get_all_lab_records()
    .then(function(result) {
        console.log(result);
        res.render('update_record_page', { record_list: result });
    }, function(error) {
        console.log(error);
    });
}

exports.update_record = function(req, res) {
    console.log("test1: " + req.body.selection);
    console.log("test2: " + req.body.record_content);

    update_record_info(req.body.selection, req.body.record_content)
    .then(function(result) {
        console.log("Hello!");
        res.render('update_record', {home_url: 'http://localhost:3003/' , content: "update record success!"});
    },
    function(error) {
        console.log(error);
    });
};

var update_record_info = function(id, record) {
    return new Promise(function(resolve, reject) {
        common.lab.updateRecord(id, record, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};


/* GET: get public params from claimsdefs for record id 
 */
exports.get_parameters = function(req, res) {
    var param_names = null;
    var OwnerName = null;
    var ReadName = null;

    // 1. First, get names of claimdefs for the corresponding record id
    request('get', 'http://localhost:3001/param_names/' + req.params.id)
    .then(function(result) {
        param_names = JSON.parse(result.body)[0];
        OwnerName = param_names.OwnerName;
        ReadName = param_names.ReadName;
        console.log("OwnerName:" + OwnerName);
        console.log("ReadName: " + ReadName);

        // 2. Second, with this name, fetch claim defs and extract public defs
        var queue = [];
        queue.push(get_claim_and_extract_public_params(OwnerName));
        queue.push(get_claim_and_extract_public_params(ReadName));
        return common.promise.all(queue);
    })
    // 3. TODO
    .then(function(result) {
        console.log(result[0]);
        console.log(result[1]);
        //TODO
        public_params = { "owner": result[0], "read": result[1] };
        res.send(public_params);
    }, function(error) {
        console.log("Errors: " + error);
    });
};

var get_claim_and_extract_public_params = function(name) {
    return request('get', 'http://localhost:3001/claimdef/' + name)
        .then(function(result) {
            console.log(result.body);
            return extract_public_params_promise(result.body);
        });
};

var extract_public_params_promise = function(ip) {
    return new Promise(function(resolve, reject) {
        common.lab.extractPublicParams(ip, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

